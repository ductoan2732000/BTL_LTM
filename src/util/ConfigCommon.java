package util;

public class ConfigCommon {
    public static final String host = "127.0.0.1";
    public static final Integer port = 5056;
    public static final Integer portNonblocking = 8089;

    // Message Code
    public static final Integer requestSucceeded = 200;
    public static final Integer helloName = 210;
    public static final Integer dataPublisherSucceeded = 220;
    public static final Integer successTopicData = 230;
    public static final Integer invalidData = 400;
    public static final Integer topicNotAvailable = 410;
    public static final Integer topicNotRegistered = 420;
    public static final Integer bye = 500;
    public static final Integer requestTimeout = 504;

    // Role Client
    public static final String roleSub = "1";
    public static final String rolePub = "2";

    // Sub and Unsub
    public static final String subTopic = "1";
    public static final String unsubTopic = "2";
    public static final String showDataTopic = "3";
    public static final String rollbackSubscriberOption = "!";

    // reset
    public  static  final String resetConnection = "Connection reset";

    // default
    public static  final  String option = "1. Subscribe. 2. Unsubscribe. 3. Show data subscribe last time";
    public static  final  String backOption = "\n(!: Mode Option)";
}
