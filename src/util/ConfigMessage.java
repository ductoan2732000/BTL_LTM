package util;

public class ConfigMessage {
    public static final String helloServer = "Hello Server";
    public static final String helloClient = ConfigCommon.requestSucceeded + " Hello Client";
    public static final String helloName = ConfigCommon.helloName+ " Hello";
    public static final String requestTimeout = ConfigCommon.requestTimeout+ " Request Timeout";
    public static final String quit = "Quit";
    public static final String bye = ConfigCommon.bye + " Bye";

    public static final String msgTopicNotAvailableSub = ConfigCommon.topicNotAvailable +" Topic not available. Please enter an existing topic!";
    public static final String msgTopicNotDataSub = ConfigCommon.successTopicData +" Subscribe to the topic successfully. No new data yet";

    public static final String msgDataSucceededPub = ConfigCommon.dataPublisherSucceeded +"  Success. Data save in location.";
    public static final String msgInvalidDataPub = ConfigCommon.invalidData +" Invalid data.";


    public static final String msgCacheClient1 = "FROM SERVER: ";
    public static final String msgCacheClient2 = "TO SERVER: ";


}
