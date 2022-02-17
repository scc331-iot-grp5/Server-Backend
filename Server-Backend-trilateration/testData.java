import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// import org.apache.commons.math4.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.RealVector;


public class testData {

    private String dbName = "";
    private String serverName = "";
    private String username = "";
    private String password = "";
    private int port = 0;
    private Connection conn;

    public testData (String dbName, String sName, String uName, String pWord, int port ) {
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

    public void updateDistance(int microbitID, int microbitIDTwo, int heartbeat, int rssi){
        String query = "REPLACE INTO Distances(microbitID, microbitIDTwo, heartbeat, rssi)" + " VALUES(" + microbitID + ", " + microbitIDTwo + ", " + heartbeat + ", " + rssi +");";
        try {
            Statement pst = conn.createStatement();
            pst.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetTable()
    {
        String query = "DELETE FROM Distances WHERE microbitID=1003;";
        try {
            Statement pst = conn.createStatement();
            pst.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testData dbHandler = new testData("sql4467174", "sql4.freesqldatabase.com", "sql4467174", "y4jcQacpxU", 3306);
        
        // try{
        //     TimeUnit.SECONDS.sleep(10);
        // }catch (Exception e){}
        
        
        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 2, -69);
        dbHandler.updateDistance(1003, 1001, 2, -71);
        dbHandler.updateDistance(1003, 1002, 2, -71);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 3, -68);
        dbHandler.updateDistance(1003, 1001, 3, -70);
        dbHandler.updateDistance(1003, 1002, 3, -70);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 4, -67);
        dbHandler.updateDistance(1003, 1001, 4, -70);
        dbHandler.updateDistance(1003, 1002, 4, -70);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}
        dbHandler.updateDistance(1003, 1000, 5, -66);
        dbHandler.updateDistance(1003, 1001, 5, -71);
        dbHandler.updateDistance(1003, 1002, 5, -70);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}
        dbHandler.updateDistance(1003, 1000, 6, -65);
        dbHandler.updateDistance(1003, 1001, 6, -71);
        dbHandler.updateDistance(1003, 1002, 6, -70);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 7, -64);
        dbHandler.updateDistance(1003, 1001, 7, -71);
        dbHandler.updateDistance(1003, 1002, 7, -71);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 8, -63);
        dbHandler.updateDistance(1003, 1001, 8, -72);
        dbHandler.updateDistance(1003, 1002, 8, -71);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 9, -62);
        dbHandler.updateDistance(1003, 1001, 9, -72);
        dbHandler.updateDistance(1003, 1002, 9, -72);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 10, -61);
        dbHandler.updateDistance(1003, 1001, 10, -73);
        dbHandler.updateDistance(1003, 1002, 10, -72);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 11, -60);
        dbHandler.updateDistance(1003, 1001, 11, -73);
        dbHandler.updateDistance(1003, 1002, 11, -71);

        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);
    }
}

