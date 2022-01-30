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
        
        try{
            TimeUnit.SECONDS.sleep(10);
        }catch (Exception e){}
        
        
        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}
        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}
        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.updateDistance(1003, 1000, 1, -70);
        dbHandler.updateDistance(1003, 1001, 1, -70);
        dbHandler.updateDistance(1003, 1002, 1, -70);

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){}

        dbHandler.resetTable();
    }
}

