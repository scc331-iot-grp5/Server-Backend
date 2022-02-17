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


public class DatabaseHandler {

    public class RssiCoords {

        int rssi;
        double xCoord;
        double yCoord;

        public RssiCoords (int rssi, double xCoord, double yCoord)
        {
            this.rssi = rssi;
            this.xCoord = xCoord;
            this.yCoord = yCoord;
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
        String query = "SELECT DISTINCT microbitID FROM Distances";
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

    public int getCurrentHeartbeat(int microbitID){
        String query = "SELECT MAX(heartbeat) FROM Distances WHERE microbitID=" + microbitID + ";";
        int currentHeartbeat = 0;
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                currentHeartbeat = rs.getInt(1);
            }
            return currentHeartbeat;
        } catch (Exception e) {
            e.printStackTrace();
            return currentHeartbeat;
        }
    }
    

    public ArrayList<RssiCoords> getRSSI(int microbitID) {
        int heartbeat = getCurrentHeartbeat(microbitID);
        String query = "SELECT Distances.rssi, Location.xCoord, Location.yCoord FROM Distances INNER JOIN Location ON Distances.microbitIDTwo = Location.microbitID INNER JOIN Microbits ON Distances.microbitIDTwo = Microbits.microbitID WHERE Distances.heartbeat=" + heartbeat + " AND Distances.microbitID=" + microbitID + " AND Microbits.configID=2 ORDER BY Distances.rssi DESC LIMIT 3;";
        ArrayList<RssiCoords> rssiCoords = new ArrayList<RssiCoords>();
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                LatLng latLng = new LatLng(rs.getDouble(3), rs.getDouble(2));
                OSRef osRef = latLng.toOSRef();

                rssiCoords.add(new RssiCoords(rs.getInt(1),osRef.getEasting(), osRef.getNorthing()));
            }
            return rssiCoords;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateLocation(int microbitID, double xCoord, double yCoord){
        OSRef osRef = new OSRef(xCoord, yCoord);
        LatLng latLng = osRef.toLatLng();
        System.out.println(latLng.getLat());
        System.out.println(latLng.getLng());
        String query = "REPLACE INTO Location(microbitID, xCoord, yCoord)" + " VALUES(" + microbitID + ", " + latLng.getLat() + ", " + latLng.getLng() + ");";
        try {
            Statement pst = conn.createStatement();
            pst.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double rssiToMetres(double rssi)
    {
        return Math.pow(10, (-40 - rssi) / (10 * 2));
    }

    public static void main(String[] args) {
        DatabaseHandler dbHandler = new DatabaseHandler("sql4467174", "sql4.freesqldatabase.com", "sql4467174", "y4jcQacpxU", 3306);

        while (true){
            ArrayList<Integer> microbitIDs = dbHandler.getMicrobitIDs();

            for (int microbitID : microbitIDs)
            {
                System.out.println(microbitID);
                try{
                    ArrayList<RssiCoords> rssiCoords = dbHandler.getRSSI(microbitID);

                    double[][] positions = new double[][] { {rssiCoords.get(0).xCoord, rssiCoords.get(0).yCoord}, {rssiCoords.get(1).xCoord, rssiCoords.get(1).yCoord}, {rssiCoords.get(2).xCoord, rssiCoords.get(2).yCoord} };
                    double[] distances = new double[] {dbHandler.rssiToMetres(rssiCoords.get(0).rssi), dbHandler.rssiToMetres(rssiCoords.get(1).rssi), dbHandler.rssiToMetres(rssiCoords.get(2).rssi)};

                    LinearLeastSquaresSolver solver = new LinearLeastSquaresSolver(new TrilaterationFunction(positions, distances));
                    RealVector linearCalculatedPosition = solver.solve();

                    // double easting = linearCalculatedPosition.getEntry(0);
                    // double northing = linearCalculatedPosition.getEntry(1);
                    
                    // OSRef osRef = new OSRef(easting, northing);
                    // LatLng latLng = osRef.toLatLng();

                    // System.out.println(latLng.getLat());
                    // System.out.println(latLng.getLng());


                    dbHandler.updateLocation(microbitID, linearCalculatedPosition.getEntry(0), linearCalculatedPosition.getEntry(1));
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

