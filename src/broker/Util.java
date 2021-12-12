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


    // Lưu mảng vào file json
    public static void WriteSubscriberJsonFile(String data) throws ParseException {
        JSONParser dataParser = new JSONParser();
        JSONArray subscribersArray = new JSONArray();
//        JSONObject subscriberObject = new JSONObject();

        JSONObject json = (JSONObject) dataParser.parse(data);

        subscribersArray.add(json);
//        subscriberObject.put("subscribers", subscribersArray);
        try (FileWriter file = new FileWriter("src/broker/db/subscribers.json")) {
//            file.write(subscriberObject.toJSONString());
            file.write(subscribersArray.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Đọc từ file topic-detail.json
    public static JSONArray ReadTopicJsonFile() throws ParseException {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("src/broker/db/topic-detail.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray topicList = (JSONArray) obj;

            return topicList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


}
