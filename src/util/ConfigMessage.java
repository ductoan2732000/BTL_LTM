package util;

public class ConfigMessage {
    public static final String helloServer = "Hello Server";
    public static final String helloClient = ConfigCommon.requestSucceeded + " Hello Client";
    public static final String helloName = ConfigCommon.helloName+ " Hello ";
    public static final String requestTimeout = ConfigCommon.requestTimeout+ " Request Timeout";
    public static final String quit = "Quit";
    public static final String bye = ConfigCommon.bye + " Bye";

    public static final String msgDataSucceededPub = ConfigCommon.dataPublisherSucceeded +"  Success.";
    public static final String msgInvalidDataPub = ConfigCommon.invalidData +" Invalid data.";
    public static final String msgTopicNotRegistered = ConfigCommon.topicNotRegistered +" There are no registered topics yet";
    public static final String msgTopicNotAvailable = ConfigCommon.topicNotAvailable +" Topic not available";


    public static final String msgCacheClient1 = "Broker: ";
    public static final String msgCacheClient2 = "Client: ";

    public static final  String successSubscriber = ConfigCommon.successTopicData + " Subscriber";


}
