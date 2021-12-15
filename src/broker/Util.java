package broker;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class Util {
    private static String[] topics = new String[]{"Temperature", "Humidity", "Hardware", "Sensor"};
    private static String[] subTopics = new String[]{};

    public  static void ConvertStringToObject (String stringToParse, Object object ) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(stringToParse);
        Field[] fields = object.getClass().getDeclaredFields();
    }

    public static String[] ArrayTopic(){
        return topics;
    }

    public static String[] convertStringToArray(String data) {
        if(data == null) return subTopics;
        if(data.trim().length() > 2){
            subTopics = data.split(",");
            return subTopics;
        }

        subTopics = data.split(" ");
        return  subTopics;
    }
}
