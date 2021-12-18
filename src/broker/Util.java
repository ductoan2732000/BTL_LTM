package broker;


import broker.Model.Topic;
import broker.cache.CacheTopic;
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
import java.util.ArrayList;

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

    /**
     * @author pnthuan 17/12/2021
     * @param content
     * @param data
     * @param topics
     */
    public  static  void upgradeArrayTopic(String content, String data, ArrayList<Topic> topics){
        if(content == null || content.trim().equals("")) return ;
        String[] level = content.split("/");
        if(level.length == 1){
            ArrayList<Topic> subTopics = new ArrayList<Topic>();
            for(int i = 0;i< topics.size() ;i ++){
                if(topics.get(i).topicName.equals(level[0])){
                    level[0] = "";
                    subTopics = topics.get(i).subTopics;
                    topics.remove(i);
                }
            }
            topics.add((new Topic(content, data, subTopics)));
            return ;
        }
        if(level.length > 1){
            boolean check = false;
            String pathCurrent = level[0];
            level[0] = "";

            String content1 = null;
            try {
                content1 = String.join("/",level).substring(1);
            }catch (Exception ex){
                return ;
            }
            if(level.length == 2){
                content1 = level[1];
            }
            for(int i = 0;i< topics.size() ;i ++){
                if(topics.get(i).topicName.equals(pathCurrent)){
                    upgradeArrayTopic(content1, data,topics.get(i).subTopics);
                    check= true;
                }
            }
            if(check == false){

                Topic a = new Topic(pathCurrent, "Data null");
                upgradeArrayTopic(content1, data, a.subTopics);
                topics.add(a);
            }
        }
    }

    /**
     * @author pnthuan 17/12/2021
     * @param path
     * @param topics
     * @return
     */
    public static String getDataCacheTopic(String path, ArrayList<Topic> topics){
        if(topics.size() == 0) return "";
        String[] level = path.split("/");
        if(level.length == 1){
            for(int i = 0;i< topics.size() ;i ++){
                if(topics.get(i).topicName.equals(level[0])){
                    String msg = topics.get(i).data;
                    if(msg.equals("Data null")){
                        msg = "";
                    }else {
                        msg = msg + "\n";
                    }
                    if(topics.get(i).subTopics.size() > 0){
                        for(int j = 0;j< topics.get(i).subTopics.size(); j ++){
                            String topic = topics.get(i).subTopics.get(j).topicName;
                            msg =  msg + getDataCacheTopic(topic ,topics.get(i).subTopics);
                        }
                        return msg;
                    }
                    return msg;
                }
            }
        }
        if(level.length > 1){
            boolean check = false;
            String pathCurrent = level[0];
            level[0] = "";

            String subPath = null;
            try {
                subPath = String.join("/",level).substring(1);
            }catch (Exception ex){
                System.out.println(ex);
                return "";
            }
            if(level.length == 2){
                subPath = level[1];
            }
            for(int i = 0;i< topics.size() ;i ++){
                if(topics.get(i).topicName.equals(pathCurrent)){
                    return getDataCacheTopic(subPath,topics.get(i).subTopics);
                }
            }
        }
        return "";
    }

    /**
     * @author pnthuan
     * @param path
     * @param topics
     */
    public  static void removeDataCacheTopic(String path, ArrayList<Topic> topics){
        if(topics.size() == 0) return ;
        String[] level = path.split("/");
        if(level.length == 1){
            for(int i = 0;i< topics.size() ;i ++){
                if(topics.get(i).topicName.equals(level[0])){
                    topics.remove(i);
                }
            }
            return ;
        }
        if(level.length > 1){
            String pathCurrent = level[0];
            level[0] = "";

            String subPath = null;
            try {
                subPath = String.join("/",level).substring(1);
            }catch (Exception ex){
                System.out.println(ex);
                return ;
            }
            if(level.length == 2){
                subPath = level[1];
            }
            for(int i = 0;i< topics.size() ;i ++){
                if(topics.get(i).topicName.equals(pathCurrent)){
                    removeDataCacheTopic(subPath,topics.get(i).subTopics);
                }
            }
        }
        return ;
    }

    public static String getAllTopic(ArrayList<Topic> topics, String path){
        if(topics.size() == 0) return path;
        String res = "";
        String a = "";
        for(int i = 0;i< topics.size() ;i ++){
            if(topics.get(i).subTopics.size() > 0){
                a += getAllTopic(topics.get(i).subTopics, path + "/" +topics.get(i).topicName);
                continue;
            }
            res += getAllTopic(topics.get(i).subTopics, path + "/" +topics.get(i).topicName) + "\n";
        }
        if(res.equals("")) return a;
        return res;
    }

}
