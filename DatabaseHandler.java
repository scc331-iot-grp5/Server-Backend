import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

// import org.apache.commons.math4.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.RealVector;


public class DatabaseHandler {

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
    

    public HashMap<Integer, Integer> getRSSI(int microbitID) {
        int heartbeat = getCurrentHeartbeat(microbitID);

        String query = "SELECT microbitIDTwo, rssi FROM Distances WHERE heartbeat=" + heartbeat + " AND microbitID=" + microbitID + ";";
        HashMap<Integer, Integer> hm = new HashMap<>(); 
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                System.out.println(rs.getInt(1));
                System.out.println(rs.getInt(2));
                hm.put(rs.getInt(1), rs.getInt(2));
            }
            return hm;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        
    }

    public HashMap<Integer, ArrayList<Integer>> getDistances(ArrayList<Integer> microbitIDs){
        String microbitIDString = "(";
        for(Integer key : microbitIDs)
        {
            microbitIDString += key + ",";
        }
        microbitIDString = microbitIDString.substring(0, microbitIDString.length() - 1);
        microbitIDString += ")";
        
        String query2 = "SELECT * FROM Location WHERE microbitID IN " + microbitIDString + ";";
        HashMap<Integer, ArrayList<Integer>> hm = new HashMap<>(); 
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query2);
            while (rs.next()) {
                System.out.println(rs.getInt(1));
                System.out.println(rs.getInt(2));
                System.out.println(rs.getInt(3));
                ArrayList<Integer> coords= new ArrayList<>(Arrays.asList(rs.getInt(2), rs.getInt(3)));
                hm.put(rs.getInt(1), coords);
            }
            return hm;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        DatabaseHandler dbHandler = new DatabaseHandler("sql4467174", "sql4.freesqldatabase.com", "sql4467174", "y4jcQacpxU", 3306);
        // HashMap<Integer, Integer> RSSIs = dbHandler.getRSSI(6969);
        // ArrayList<Integer> microbitIDs = new ArrayList<>(RSSIs.keySet());
        // HashMap<Integer, ArrayList<Integer>> Location = dbHandler.getDistances(microbitIDs);

        double[][] positions = new double[][] { {50,50}, {60,60}, {70,50} };
        double[] distances = new double[] {15, 20, 15};

        LinearLeastSquaresSolver solver = new LinearLeastSquaresSolver(new TrilaterationFunction(positions, distances));
        RealVector linearCalculatedPosition = solver.solve();
        System.out.println(linearCalculatedPosition);
    }
}

