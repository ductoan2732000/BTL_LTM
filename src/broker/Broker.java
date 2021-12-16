package broker;

import broker.cache.CacheServer;
import broker.cache.CacheTopic;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.ConfigCommon;
import util.ConfigMessage;

import java.io.*;
import java.nio.channels.Selector;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Instance{
    public String id = null;
    public String topic = null;
    public String name = null;

    public  static Instance CreateInstance(String data) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(data);
        Instance instance = new Instance();
        instance.id = json.get("id").toString();
        instance.name = json.get("name").toString();
        return  instance;
    }
}


public class Broker
{
    private static Selector selector = null;
    private ServerSocket serverSocket= null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
    private Socket socket = null;
    private static Socket socketData = null;

    public Broker(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            ServerSocket serverSocketData = new ServerSocket(ConfigCommon.portData);
            System.out.println("Server start");
            System.out.println("Waiting a connection ...");

            while(true) {
                socket = serverSocket.accept();
                System.out.println("A new client is connected : " + socket);
                System.out.println("Assigning new thread for this client");

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());


                // create a new thread object
                Thread t = new ClientHandler(socket,socketData, serverSocketData, dataInputStream, dataOutputStream);

                // Invoking the start() method
                t.start();
            }
        }
        catch (IOException  ioe)
        {
            System.out.println(ioe);
        }
    }

    public static void main(String[] args) throws IOException
    {
        Broker server = new Broker(ConfigCommon.port);
    }
}

// ClientHandler classSocket
class ClientHandler extends Thread
{
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;
    final Socket socket;
    Socket socketData;
    final ServerSocket serverSocketData;
    // Constructor
    public ClientHandler(Socket socket, Socket socketData, ServerSocket serverSocketData, DataInputStream dataInputStream, DataOutputStream dataOutputStream)
    {
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.socketData = socketData;
        this.serverSocketData = serverSocketData;
    }
    /// xác thực publish, subscriber
    public String processData(String msgFromClient){
        String msgToClient = null;
        switch (msgFromClient) {
            case ConfigMessage.helloServer:
                msgToClient = ConfigMessage.helloClient;
                break;
            case ConfigMessage.quit:
                msgToClient = ConfigMessage.bye;
                break;
            default:
                msgToClient = ConfigMessage.msgInvalidDataPub ;
                break;
        }
        return msgToClient;
    }

    public boolean processPublisher(Instance instance, String data) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(data);
        try {
            String id = json.get("id").toString();
            if(id == null || id.trim().equals("")){
                return false;
            }
        }
        catch (Exception ex){
            //Dữ liệu publisher gửi sang lỗi
            return false;
        }
        // xác thực
        CacheTopic.arrayTopic.put(instance.id, data);
        return true;
    }


    public String getProperty(String data, String id, String name) throws ParseException {
        try {
            if (CacheTopic.arrayTopic.containsKey(id)){
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(data);
                return json.get(name).toString();
            }
        }catch (Exception ex){
        }
        return "";
    }

    public void checkShowData(String idSub, boolean isShowData){
        CacheServer.cacheIsShowData.replace(idSub, isShowData);
    }

    @Override
    public void run()
    {
        String msgFromClient = "";
        String msgToClient = "";

        boolean isSubscriber = false;
        boolean isPublisher = false;
        boolean isSubscriberOption = false;
        boolean isSub = false;
        boolean isUnsub = false;
        boolean isSubscribed = false;
        boolean isRole = false;

        Instance instance = null;

        while (!msgFromClient.equals(ConfigMessage.quit))
        {
            try {
                msgFromClient = "";
                try {
                    msgFromClient = dataInputStream.readUTF();
                }catch (Exception ex){
                    if(ex.getMessage().equals(ConfigCommon.resetConnection)){
                        msgFromClient = ConfigMessage.quit;
                    }
                }

                if(isPublisher){
                    if(msgFromClient.equals(ConfigMessage.quit)){
                        if(CacheTopic.arrayTopic.containsKey(instance.id)){
                            CacheTopic.arrayTopic.remove(instance.id);
                        }
                    }
                    if(processPublisher(instance, msgFromClient)){
                        msgToClient = ConfigMessage.msgDataSucceededPub;
                    }else {
                        msgToClient = ConfigMessage.msgInvalidDataPub;
                    }
                    dataOutputStream.writeUTF(msgToClient);
                }
                else if(isSubscriber && msgFromClient.equals(ConfigCommon.rollbackSubscriberOption)){
                    isPublisher = false;
                    isSubscriberOption = true;
                    checkShowData(instance.id, false);
                    msgToClient = ConfigCommon.option;
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
                else if(isSubscriber && isSubscriberOption){
                    switch (msgFromClient){
                        case ConfigCommon.subTopic:
                            if (isSubscribed){
                                for (int index = 0; index < CacheTopic.arrayTopic.size(); index++) {
                                    String key = CacheTopic.arrayTopic.keySet().toArray()[index].toString();
                                    String value = CacheTopic.arrayTopic.get(key);
                                    if (!CacheServer.cacheArray.get(instance.id).contains(key)) {
                                        msgToClient += key + ". " + getProperty(value, key, "topicName") + " ";
                                    }
                                }
                            } else {
                                for(int index = 0; index < CacheTopic.arrayTopic.size(); index ++ ){
                                    String key = CacheTopic.arrayTopic.keySet().toArray()[index].toString();
                                    String value = CacheTopic.arrayTopic.get(key);
                                    msgToClient += key + ". " + getProperty(value,key, "topicName") + " ";
                                }
                            }

                            if(msgToClient.equals("")){
                                msgToClient += ConfigMessage.msgTopicNotRegistered;
                            }

                            msgToClient += ConfigCommon.backOption;
                            isSub = true;
                            isUnsub = false;
                            isSubscriberOption = false;
                            break;
                        case ConfigCommon.unsubTopic:
                            if (isSubscribed){
                                for(int index = 0; index < CacheTopic.arrayTopic.size(); index ++ ){
                                    String key = CacheTopic.arrayTopic.keySet().toArray()[index].toString();
                                    String value = CacheTopic.arrayTopic.get(key);
                                    if(CacheServer.cacheArray.get(instance.id).contains(key)){
                                        msgToClient += key + ". " + getProperty(value,key, "topicName") + " ";
                                    }
                                }
                                isUnsub = true;
                            } else {
                                msgToClient += ConfigMessage.msgTopicNotRegistered;
                            }
                            msgToClient += ConfigCommon.backOption;
                            isSub = false;
                            isSubscriberOption = false;
                            break;
                        case ConfigCommon.showDataTopic:
                            isSubscriberOption = false;
                            isUnsub = false;
                            isSub = false;
                            checkShowData(instance.id, true);
                            msgToClient = showSubscribingToData(instance.id);
                            break;
                        default :
                            isUnsub = false;
                            isSub = false;
                            isSubscriberOption = false;
                            msgToClient = ConfigMessage.msgInvalidDataPub + ConfigCommon.backOption ;
                            break;
                    }
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
                else if(isSubscriber && isSub) {
                    List<String> temp = Arrays.asList(Util.convertStringToArray(msgFromClient));
                    if (CacheServer.cacheArray.containsKey(instance.id)){
                        List<String> data = new ArrayList<>(CacheServer.cacheArray.get(instance.id)) ;
                        if(data.size() == 0 ) data = temp;
                        else {
                            for(int i = 0;i< temp.size() ; i ++){
                                if(!data.contains(temp.get(i))){
                                    String a = temp.get(i);
                                    data.add(a);
                                }
                            }
                        }
                        temp = data;
                    }
                    CacheServer.cacheArray.put(instance.id, temp);

                    boolean isErrorNumber = false;
                    for (int i = 0; i < CacheServer.cacheArray.get(instance.id).size(); i++){
                        String idTopic = CacheServer.cacheArray.get(instance.id).get(i);
                        if(!CacheTopic.arrayTopic.containsKey(idTopic)) {
                            msgToClient = ConfigMessage.msgTopicNotAvailable + ConfigCommon.backOption ;
                            isSubscriberOption = false;
                            isErrorNumber = true;
                            break;
                        }
                    }

                    if(!isErrorNumber) {
                        checkShowData(instance.id, true);
                        isSubscribed = true;
                        msgToClient = showSubscribingToData(instance.id);
                    }

                    isSub = false;
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
                else if(isSubscriber && isUnsub) {
                    List<String> temp = Arrays.asList( Util.convertStringToArray(msgFromClient));
                    if (CacheServer.cacheArray.containsKey(instance.id)){
                        List<String> data = new ArrayList<>(CacheServer.cacheArray.get(instance.id)) ;
                        if(data.size() == 0 ) data = null;
                        else {
                            for(int i = 0;i< temp.size() ; i ++){
                                if(data.contains(temp.get(i))){
                                    data.remove(temp.get(i));
                                }
                            }
                        }
                        temp = data;
                    }
                    CacheServer.cacheArray.put(instance.id, temp);
                    boolean isErrorNumber = false;

                    for (int i = 0; i < CacheServer.cacheArray.get(instance.id).size(); i++){
                        String idTopic = CacheServer.cacheArray.get(instance.id).get(i);
                        if(!CacheTopic.arrayTopic.containsKey(idTopic)) {
                            msgToClient = ConfigMessage.msgTopicNotAvailable + ConfigCommon.backOption;
                            isSubscriberOption = false;
                            isErrorNumber = true;
                            break;
                        }
                    }
                    if(!isErrorNumber) {
                        msgToClient = showSubscribingToData(instance.id);
                    }

                    int countNull = 0;
                    int countNotNull = 0;
                    for (int i = 0; i < CacheServer.cacheArray.get(instance.id).size(); i++){
                        if( CacheServer.cacheArray.get(instance.id).get(i) != null ) {
                            countNotNull++;
                        } else {
                            countNull++;
                        }
                    }

                    if(countNull == CacheServer.cacheArray.get(instance.id).size()){
                        isSubscribed = false;
                    }

                    if(countNotNull > 0) {
                        checkShowData(instance.id, true);
                        isSubscribed = true;
                    }

                    isUnsub = false;
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
                else if(isSubscriber && isRole) {
                    dataOutputStream.writeUTF(ConfigMessage.msgInvalidDataPub + ConfigCommon.backOption);
                }
                else {
                    String roleClient = null;
                    String data = "";
                    isRole = true;

                    try {
                         roleClient = msgFromClient.substring(0, 1);
                         if(msgFromClient.length() > 1) {
                             data = msgFromClient.substring(2);
                             instance = Instance.CreateInstance(data);
                         }
                    }
                    catch (Exception e){
                        instance = null;
                        roleClient = "-1";
                    }

                    switch (roleClient){
                        case ConfigCommon.rolePub:
                            isPublisher = true;
                            isSubscriber = false;
                            msgToClient = ConfigMessage.helloName + instance.name;
                            break;
                        case ConfigCommon.roleSub:
                                if(!data.isEmpty()) {
                                    isPublisher = false;
                                    isSubscriber = true;
                                    isSubscriberOption = true;
                                    CacheServer.cacheIsShowData.put(instance.id, false);
                                    //
                                    if (!CacheServer.cacheArray.containsKey(instance.id)){
                                        CacheServer.cacheArray.put(instance.id, new ArrayList<String>());
                                    }


                                    socketData = serverSocketData.accept();
                                    DataInputStream dataInputStreamData = new DataInputStream(socketData.getInputStream());
                                    DataOutputStream dataOutputStreamData = new DataOutputStream(socketData.getOutputStream());

                                    // create a new thread object
                                    Thread n = new ServerData(socketData, serverSocketData, dataInputStreamData, dataOutputStreamData, instance.id);

                                    // Invoking the start() method
                                     n.start();
                                    msgToClient = ConfigMessage.helloName + instance.name + "\n " + ConfigCommon.option;
                                }
                            break;
                        default:
                            msgToClient = processData(msgFromClient);
                            break;
                        }

                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        try
        {
            System.out.println("Client " + this.socket + " sends exit...");
            System.out.println("Closing this connection.");
            System.out.println("Connection closed");
            System.out.println("Waiting a connection ...");
            this.socket.close();
            this.dataInputStream.close();
            this.dataOutputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public String showSubscribingToData(String id) {
        int countNotNull = 0;
        for (int i = 0; i < CacheServer.cacheArray.get(id).size(); i++){
            if( CacheServer.cacheArray.get(id).get(i) != null ) {
                countNotNull++;
            }
        }

        if(countNotNull > 0){
            return ConfigMessage.successSubscriber;
        }

        return ConfigMessage.msgTopicNotRegistered + ConfigCommon.backOption;
    }
}
