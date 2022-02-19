import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import net.objecthunter.exp4j.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class RulesEngine {

    private String dbName = "";
    private String serverName = "";
    private String username = "";
    private String password = "";
    private int port = 0;
    private Connection conn;
    GeometryFactory gf;
    GeoJsonReader reader;
    JSONParser jsonParser = new JSONParser();

    public RulesEngine (String dbName, String sName, String uName, String pWord, int port ) {
        this.dbName = dbName;
        this.serverName = sName;
        this.username = uName;
        this.password = pWord;
        this.port = port;
        dbCon();
        PrecisionModel pm = new PrecisionModel();
        gf = new GeometryFactory(pm);
        reader = new GeoJsonReader(gf);
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

    public JSONObject getJSON()
    {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("testRule.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            JSONObject employeeList = (JSONObject) obj;
            
            return employeeList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Geometry> zoneQuery(String query){
        ArrayList<Geometry> zones = new ArrayList<>();
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                JSONObject json = (JSONObject) jsonParser.parse(rs.getString(1));
                JSONArray trimmedJSON = (JSONArray) json.get("features");
                Iterator<JSONObject> iterator = trimmedJSON.iterator();
                JSONObject data = (JSONObject) iterator.next().get("geometry");
                Geometry polygon = reader.read(data.toJSONString());
                zones.add(polygon);
            }
            return zones;
        } catch (Exception e) {
            e.printStackTrace();
            return zones;
        }
    }

    public Map<Integer, Double> simpleQuery(String query){
        Map<Integer, Double> results =  new HashMap<>();
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                results.put(rs.getInt(1), rs.getDouble(2));
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return results;
        }
    }

    public Map<Integer, Point> locationQuery(String query){
        Map<Integer, Point> results =  new HashMap<>();
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                Point point = gf.createPoint(new Coordinate(rs.getDouble(2), rs.getDouble(3)));
                results.put(rs.getInt(1), point);
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return results;
        }
    }

    public String simpleGetQuery(String fact)
    {
        switch(fact){
            case "temperature":
                return "SELECT device_id, degrees FROM temperature_readings LEFT JOIN readings ON readings.id = reading_id WHERE reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
            case "direction":
                return "SELECT device_id, heading FROM compass_readings LEFT JOIN readings ON readings.id = reading_id WHERE reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
            case "noise":
                return "SELECT device_id, decibels FROM volume_readings LEFT JOIN readings ON readings.id = reading_id WHERE reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
            case "x acceleration":
                return "SELECT device_id, x FROM acceleration_readings LEFT JOIN readings ON readings.id = reading_id WHERE reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
            case "y acceleration":
                return "SELECT device_id, y FROM acceleration_readings LEFT JOIN readings ON readings.id = reading_id WHERE reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
            case "z acceleration":
                return "SELECT device_id, z FROM acceleration_readings LEFT JOIN readings ON readings.id = reading_id WHERE reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
            case "speed":
                return "SELECT device_id, z FROM speed_readings LEFT JOIN readings ON readings.id = reading_id WHERE reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
        }
        return null;
    }

    public boolean determine(double fact, String operator, double value)
    {
        switch(operator){
            case "equals":
                return fact == value;
            case "greaterThan":
                return fact > value;
            case "greaterThanEquals":
                return fact >= value;
            case "lessThan":
                return fact < value;
            case "lessThanEquals":
                return fact >= value;
        }
        return false;
    }

    // intersect sets to determine microbits that passed all conditions
    public HashSet<Integer> setIntersection(ArrayList<HashSet<Integer>> sets)
    {
        HashSet<Integer> intersection = new HashSet<>(sets.remove(0));
        for(Set<Integer> set : sets)
        {
            intersection.retainAll(set);
        }
        return intersection;
    }

    public HashSet<Integer> executeSimpleCondition(String fact, String operator, Double value)
    {
        // create and execute query for simple fact
        String query = simpleGetQuery(fact);
        Map<Integer, Double> facts = simpleQuery(query);

        // For each possible fact, check if its true if its true add to set
        HashSet<Integer> microbits = new HashSet<>();
        for(Map.Entry<Integer, Double> entry : facts.entrySet())
        {
            if(determine(entry.getValue(), operator, value))
            {
                microbits.add(entry.getKey());
            }
        }
        return microbits;
    }

    public HashSet<Integer> executeInCondition(Geometry zone)
    {
        String query = "SELECT device_id, longitude, latitude FROM location_readings LEFT JOIN readings ON readings.id = reading_id WHERE reported_at in (SELECT max(reported_at) FROM readings GROUP BY device_id)";
        Map<Integer, Point> facts = locationQuery(query);

        for(Coordinate coord : zone.getCoordinates())
        {
            System.out.println(coord.toString());
        }

        HashSet<Integer> microbits = new HashSet<>();
        for(Map.Entry<Integer, Point> entry : facts.entrySet())
        {
            if(zone.contains(entry.getValue()))
            {
                microbits.add(entry.getKey());
            }
        }

        return microbits;
    }

    public HashSet<Integer> getMicrobitsInGroup(String groupID)
    {
        String query = "SELECT devices.id FROM devices INNER JOIN device_types ON device_types.id = devices.type WHERE device_types.id = " + groupID;
        HashSet<Integer> microbitIDs = new HashSet<>();
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

    public String computeValue(String valueString, HashMap<String, String> zoneRules)
    {
        String[] valueSplit = valueString.split(" ");
        for(int i = 0; i < valueSplit.length; i++)
        {
            if(valueSplit[i].contains("$")){
                valueSplit[i] = valueSplit[i].replace("$", "");
                System.out.println(valueSplit[i]);
                if(zoneRules.containsKey(valueSplit[i])){
                    valueSplit[i] = zoneRules.get(valueSplit[i]);
                }
            }
        }
        String calculation = String.join(" ", valueSplit);
        System.out.println(calculation);
        Expression e =  new ExpressionBuilder(calculation).build();
        double result = e.evaluate();
        return String.valueOf(result);
    }

    public HashSet<Integer> computeCondition(JSONObject condition, Geometry zone)
    {
        String fact = (String) condition.get("fact");
        String operator = (String) condition.get("operator");
        double value = Double.valueOf((String) condition.get("value"));
        if(operator.equals("in") && zone!=null)
        {
            return executeInCondition(zone);
        }
        else{
            return executeSimpleCondition(fact, operator, value);
        }
    }

    public HashSet<Integer> microbitFiltering(String microbitGroup, HashSet<Integer> conditionResult)
    {
        HashSet<Integer> microbitFilter = getMicrobitsInGroup(microbitGroup);
        ArrayList<HashSet<Integer>> sets = new ArrayList<>();
        sets.add(conditionResult);
        sets.add(microbitFilter);
        return setIntersection(sets);
    }

    public HashSet<Integer> computeRule(ArrayList<JSONObject> conditions, Geometry zone)
    {
        HashMap<String, ArrayList<HashSet<Integer>>> microbitSets = new HashMap<>();
        Iterator<JSONObject> iterator = conditions.iterator();
        while (iterator.hasNext()){
            JSONObject condition = iterator.next();
            HashSet<Integer> conditionResult = computeCondition(condition, zone);
            
            if(condition.containsKey("microbitGroup")){

                String microbitGroup = (String) condition.get("microbitGroup");
                conditionResult = microbitFiltering(microbitGroup, conditionResult);
                if(!microbitSets.containsKey(microbitGroup))
                {
                    ArrayList<HashSet<Integer>> temp = new ArrayList<>();
                    temp.add(conditionResult);
                    microbitSets.put(microbitGroup, temp);
                }
                else{
                    microbitSets.get(microbitGroup).add(conditionResult);
                }
            } else if(condition.containsKey("microbit")){
                
                String microbit = (String) condition.get("microbit");
                HashSet<Integer> mtemp = new HashSet<>();
                if(conditionResult.contains(microbit)){
                    mtemp.add((Integer.valueOf(microbit)));
                }
                if(!microbitSets.containsKey(microbit))
                {
                    ArrayList<HashSet<Integer>> temp = new ArrayList<>();
                    temp.add(conditionResult);
                    microbitSets.put(microbit, temp);
                }
                else{
                    microbitSets.get(microbit).add(conditionResult);
                }
            } else {
                if(!microbitSets.containsKey("0")){
                    ArrayList<HashSet<Integer>> temp = new ArrayList<>();
                    temp.add(conditionResult);
                    microbitSets.put("0", temp);
                } else {
                    microbitSets.get("0").add(conditionResult);
                }
            }
            
        }
        HashMap<String, HashSet<Integer>> successfullMicrobits = new HashMap<>();
        for(Map.Entry<String, ArrayList<HashSet<Integer>>> entry : microbitSets.entrySet()){
            HashSet<Integer> microbitSet = setIntersection(entry.getValue());

            if(microbitSet.isEmpty()) //if Intersection is empty not all conditions met
            {
                return new HashSet<>();
            }
            successfullMicrobits.put(entry.getKey(), microbitSet);
        }

        HashSet<Integer> output = new HashSet<>();
        for(Map.Entry<String, HashSet<Integer>> entry : successfullMicrobits.entrySet()){
            if(!entry.getKey().equals("0"))
            {
                output.addAll(entry.getValue());
            }
        }
        if(successfullMicrobits.containsKey("0"))
        {
            output.retainAll(successfullMicrobits.get("0"));
        }

        return output;
    }
    
    
    public HashMap<String, HashMap<String, String>> getZoneRules(String ruleZone)
    {
        String query = "SELECT zone_id, name, value FROM zone_group_var_values zgvv INNER JOIN zone_group_vars zgv on zgvv.var_id = zgv.id WHERE zgv.group_id = " + ruleZone;
        HashMap<String, HashMap<String, String>> zoneRules = new HashMap<>();
        try {
            Statement dbPull = conn.createStatement();
            ResultSet rs = dbPull.executeQuery(query);
            while (rs.next()) {
                String zone_id = rs.getString(1);
                String name = rs.getString(2);
                String value = rs.getString(3);
                if(!zoneRules.containsKey(zone_id)){
                    zoneRules.put(zone_id, new HashMap<String, String>());
                }
                zoneRules.get(zone_id).put(name, value);
            }
            return zoneRules;
        } catch (Exception e) {
            e.printStackTrace();
            return zoneRules;
        }
    }

    public static void main(String[] args) {
        RulesEngine rulesEngine = new RulesEngine("iota", "dodecahedron.noah.katapult.cloud", "root", "AdaLovelace1815", 3306);
        
        JSONObject rule = rulesEngine.getJSON();
        JSONArray conditions = (JSONArray) rule.get("conditions");

        // ArrayList<HashSet<Integer>> microbitSets2 = new ArrayList<>();
        
        if(rule.containsKey("zone"))
        {
            String ruleZone = (String) rule.get("zone");
            String query = "SELECT geo_json FROM zones LEFT JOIN zone_groups ON zones.id = zone_groups.id WHERE zone_groups.id=" + ruleZone;
            ArrayList<Geometry> zones = rulesEngine.zoneQuery(query);

            HashMap<String, HashMap<String, String>> zoneRules = rulesEngine.getZoneRules(ruleZone);
            zoneRules.get("2").entrySet().forEach(entry -> {
                System.out.println(entry.getKey() + " " + entry.getValue());
            });
            
            ArrayList<JSONObject> newConditions = new ArrayList<>();
            Iterator<JSONObject> iterator = conditions.iterator();
            while (iterator.hasNext()){
                JSONObject condition = iterator.next();
                if(condition.containsKey("microbitGroup"))
                {
                    String microbitGroup = (String) condition.get("microbitGroup");
                    String value = (String) condition.get("value");
                    if(value.contains("$")){
                        value = rulesEngine.computeValue(value, zoneRules.get(microbitGroup));
                        System.out.println(value);
                        condition.put("value", value);
                    }
                }
                newConditions.add(condition);
            }

            for(Geometry zone : zones)
            {
                rulesEngine.computeRule(newConditions, zone);
            }
        }
        else{
            rulesEngine.computeRule(conditions, null);
        }


            

        // HashSet<Integer> intersection = rulesEngine.setIntersection(microbitSets);

        // intersection.forEach((e) -> { System.out.println(e); });
    }
}
