import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import uk.me.jstott.jcoord.OSRef;
import uk.me.jstott.jcoord.LatLng;

// import org.apache.commons.math4.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.RealVector;

import java.lang.reflect.Method;

public class DatabaseHandler {

    public class RssiCoords {

        double distance;
        OSRef osRef;
        Timestamp timestamp;
        int reading_id;

        public RssiCoords (double distance, OSRef osRef, Timestamp timestamp, int reading_id)
        {
            this.distance = distance;
            this.osRef = osRef;
            this.timestamp = timestamp;
            this.reading_id = reading_id;
        }
    }

    private String dbName = "";
    private String serverName = "";
    private String username = "";
    private String password = "";
    private int port = 0;
    private Connection conn;

    public DatabaseHandler (String dbName, String sName, String uName, String pWord, int port ) {
        this.dbName = dbName;
        this.serverName = sName;
        this.username = uName;
        this.password = pWord;
        this.port = port;
        dbCon();
    }

    public void dbCon () {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + serverName + ":" + port + "/" + dbName, username, password);
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e);
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getMicrobitIDs()
    {
        String query = "SELECT DISTINCT id FROM distance_readings INNER JOIN readings ON reading_id = id";
        ArrayList<Integer> microbitIDs = new ArrayList<>();
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                microbitIDs.add(rs.getInt(1));
            }
            return microbitIDs;
        } catch (Exception e) {
            e.printStackTrace();
            return microbitIDs;
        }
    }

    public ArrayList<RssiCoords> getRSSI(int microbitID) {
        String query = "SELECT distance_readings.distance_from, location_readings.latitude, location_readings.longitude, readings.reported_at, readings.id FROM distance_readings INNER JOIN readings ON distance_readings.reading_id = readings.id INNER JOIN devices ON readings.device_id = devices.id INNER JOIN location_readings on readings.id = location_readings.reading_id WHERE distance_readings.distance_from = " + microbitID + " AND devices.type = 1 AND readings.heartbeat in ( SELECT max(readings.heartbeat) FROM readings GROUP BY readings.device_id ) ORDER BY distance_readings.distance DESC LIMIT 3";
        ArrayList<RssiCoords> rssiCoords = new ArrayList<RssiCoords>();
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                LatLng latLng = new LatLng(rs.getDouble(2), rs.getDouble(3));
                OSRef osRef = latLng.toOSRef();
                rssiCoords.add(new RssiCoords(rs.getDouble(1), osRef, rs.getTimestamp(4), rs.getInt(5)));
            }
            return rssiCoords;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertLocation(int reading_id, double easting, double northing){
        OSRef osRef = new OSRef(easting, northing);
        LatLng latLng = osRef.toLatLng();
        String query = "INSERT INTO location_readings(reading_id, latitude, longitude)" + " VALUES(" + reading_id + ", " + latLng.getLat() + ", " + latLng.getLng() + ");";
        try {
            Statement pst = conn.createStatement();
            pst.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertSpeed(int reading_id, double speed){
        String query = "INSERT INTO speed_readings(reading_id, speed)" + " VALUES(" + reading_id + ", " + speed + ");";
        try {
            Statement pst = conn.createStatement();
            pst.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RssiCoords getPreviousLocation(int microbitID){
        String query = "SELECT latitude, longitude, reported_at FROM location_readings INNER JOIN readings ON id = reading_id WHERE device_id=" + microbitID + " AND reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                LatLng latLng = new LatLng(rs.getDouble(1), rs.getDouble(2));
                OSRef osRef = latLng.toOSRef();
                return new RssiCoords(0, osRef, rs.getTimestamp(3), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }


    public static void main(String[] args) {
        OSRef osRef = new OSRef(0,0);
        DatabaseHandler dbHandler = new DatabaseHandler("iota", "dodecahedron.noah.katapult.cloud", "root", "AdaLovelace1815", 3306);



        while (true){
            ArrayList<Integer> microbitIDs = dbHandler.getMicrobitIDs();

            for (int microbitID : microbitIDs)
            {
                System.out.println(microbitID);
                try{
                    ArrayList<RssiCoords> rssiCoords = dbHandler.getRSSI(microbitID);

                    double[][] positions = new double[][] { {rssiCoords.get(0).osRef.getEasting(), rssiCoords.get(0).osRef.getNorthing()}, {rssiCoords.get(1).osRef.getEasting(), rssiCoords.get(1).osRef.getNorthing()}, {rssiCoords.get(2).osRef.getEasting(), rssiCoords.get(2).osRef.getNorthing()} };
                    double[] distances = new double[] {rssiCoords.get(0).distance, rssiCoords.get(1).distance, rssiCoords.get(1).distance};

                    LinearLeastSquaresSolver solver = new LinearLeastSquaresSolver(new TrilaterationFunction(positions, distances));
                    RealVector linearCalculatedPosition = solver.solve();
                    
                    RssiCoords previousLocation = dbHandler.getPreviousLocation(microbitID);
                    double distance = Math.hypot(linearCalculatedPosition.getEntry(0)-previousLocation.osRef.getEasting(), linearCalculatedPosition.getEntry(1)-previousLocation.osRef.getNorthing())/1000;
                    double timeDiff = rssiCoords.get(0).timestamp.getTime() - previousLocation.timestamp.getTime();
                    double timeDiffHours =  timeDiff / (1000*60*60);
                    double speed = distance/timeDiffHours;
                    

                    dbHandler.insertSpeed(rssiCoords.get(0).reading_id, speed);
                    dbHandler.insertLocation(rssiCoords.get(0).reading_id, linearCalculatedPosition.getEntry(0), linearCalculatedPosition.getEntry(1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try{
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e){}
        }
    }
}

