package broker.Model;

import java.util.ArrayList;

public class Topic {
    public String topicName = null;
    public String data = null;
    public ArrayList<Topic> subTopics = new ArrayList<Topic>();

    public Topic(String topicName, String data){
        this.topicName = topicName;
        this.data = data;
    }

    public Topic(String topicName, String data, ArrayList<Topic> subTopics){
        this.topicName = topicName;
        this.data = data;
        this.subTopics = subTopics;
    }
}