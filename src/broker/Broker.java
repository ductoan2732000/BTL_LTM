package broker;

import broker.cache.CacheServer;
import broker.cache.CacheTopic;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.ConfigCommon;
import util.ConfigMessage;

import java.io.*;
import java.nio.channels.Selector;
import java.text.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* Phân biệt các role của client
* */
enum  Identified {
    Subscriber(ConfigCommon.roleSub),
    Publisher(ConfigCommon.rolePub);
    private final String value;
    private Identified(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public String toString(){
        switch(this){
            case Subscriber:
                return ConfigCommon.roleSub;
            case Publisher :
                return ConfigCommon.rolePub;
        }
        return null;
    }
}

/*
* Hứng dữ liêu ở publisher
* */
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
    public String GetInfoInstance(Instance instance){
        return  "ID : " + instance.id + ",Topic : " + instance.topic + ",name : " + instance.name;
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
            ServerSocket serverSocketData = new ServerSocket(8089);
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
        // server is listening on port 5056
        Broker server = new Broker(ConfigCommon.port);
    }
}

// ClientHandler classSocket
class ClientHandler extends Thread
{
    private boolean x = true;
    public Boolean isCreateNonBlocking = false;
    DateFormat fordate        = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime        = new SimpleDateFormat("hh:mm:ss");
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
            System.out.println("Dữ liệu publisher gửi sang lỗi");
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
            x = true;
            try {

                msgFromClient = "";
                try {
                    msgFromClient = dataInputStream.readUTF();
                }catch (Exception ex){
                    if(ex.getMessage().equals("Connection reset")){
                        msgFromClient = ConfigMessage.quit;
                    }
                }


                if(isPublisher){
                    if(msgFromClient.equals(ConfigMessage.quit)){
                        if(CacheTopic.arrayTopic.containsKey(instance.id)){
                            CacheTopic.arrayTopic.remove(instance.id);
                        }
                    }
                    //WriteFile(instance, msgFromClient);
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
                    msgToClient = "1. Subscribe. 2. Unsubscribe. 3. Show data subscribe last time";
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
                else if(isSubscriber && isSubscriberOption){ // Khi bam vao cac option
                    switch (msgFromClient){
                        case ConfigCommon.subTopic:
                            if (isSubscribed){
                                for(int index = 0; index < CacheTopic.arrayTopic.size(); index ++ ){
                                    // lấy
                                    String key = CacheTopic.arrayTopic.keySet().toArray()[index].toString();
                                    String value = CacheTopic.arrayTopic.get(key);
                                    // lấy danh sách đăng ký => hiển thị nhữn tg tk không nằm trong đó
                                    // [1, 2]
                                    if(!CacheServer.cacheArray.get(instance.id).contains(key)){
                                        msgToClient += key + ". " + getProperty(value,key, "topicName") + " ";
                                    }
                                }
                            } else {
                                for(int index = 0; index < CacheTopic.arrayTopic.size(); index ++ ){
                                    // lấy
                                    String key = CacheTopic.arrayTopic.keySet().toArray()[index].toString();
                                    String value = CacheTopic.arrayTopic.get(key);
                                    // lấy danh sách đăng ký => hiển thị nhữn tg tk không nằm trong đó
                                    msgToClient += key + ". " + getProperty(value,key, "topicName") + " ";

                                }
                            }
                            msgToClient += "\n(!: Mode Option)";
                            isSub = true;
                            isUnsub = false;
                            isSubscriberOption = false;
                            break;
                        case ConfigCommon.unsubTopic:
                            if (isSubscribed){
                                for(int index = 0; index < CacheTopic.arrayTopic.size(); index ++ ){
                                    // lấy
                                    String key = CacheTopic.arrayTopic.keySet().toArray()[index].toString();
                                    String value = CacheTopic.arrayTopic.get(key);
                                    // lấy danh sách đăng ký => hiển thị nhữn tg tk không nằm trong đó
                                    // [1, 2]
                                    if(CacheServer.cacheArray.get(instance.id).contains(key)){
                                        msgToClient += key + ". " + getProperty(value,key, "topicName") + " ";
                                    }
                                }
                                isUnsub = true;
                            } else {
                                msgToClient += "420 There are no registered topics yet";
                            }
                            msgToClient += "\n(!: Mode Option)";
                            isSub = false;
                            isSubscriberOption = false;
                            break;
                        case ConfigCommon.showDataTopic:
                            isSubscriberOption = false;
                            isUnsub = false;
                            isSub = false;
                            msgToClient = showSubscribingToData(msgToClient);
                            break;
                        default :
                            isUnsub = false;
                            isSub = false;
                            isSubscriberOption = false;
                            msgToClient = "400 Invalid data.\n(!: Mode Option)"; // để tạm, tính sau
                            break;
                    }
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
                else if(isSubscriber && isSub) { // Khi bam vao option va la sub
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
                    // Xử lý việc sub
                    for (int i = 0; i < CacheServer.cacheArray.get(instance.id).size(); i++){
                        String idTopic = CacheServer.cacheArray.get(instance.id).get(i);
                        // chứa 1 tk không nằm trong danh sách topic thì đẩy lỗi
                        if(!CacheTopic.arrayTopic.containsKey(idTopic)) {
                            // Xử lý việc nếu nhập không trong giới hạn của topic
                            msgToClient = "410 Topic not available. Please enter an existing topic!\n(!: Mode Option)";
                            isSubscriberOption = false;
                            isErrorNumber = true;
                            break;
                        }
                    }

                    if(!isErrorNumber) {
                        // nếu đăng ký thành công thì gán biến boolen để bên dưới ko phải writeUTF nữa
                        msgToClient = showSubscribingToData(msgToClient);
                        isSubscribed = true;
                    }

                    isSub = false;
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";

                }
                else if(isSubscriber && isUnsub) { // Khi bam vao option va la unsub

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
                    // 1 2
                    // 1
                    // output 2 :

                    boolean isErrorNumber = false;

                    for (int i = 0; i < CacheServer.cacheArray.get(instance.id).size(); i++){
                        String idTopic = CacheServer.cacheArray.get(instance.id).get(i);
                        // chứa 1 tk không nằm trong danh sách topic thì đẩy lỗi
                        if(!CacheTopic.arrayTopic.containsKey(idTopic)) {
                            // Xử lý việc nếu nhập không trong giới hạn của topic
                            msgToClient = "410 Topic not available. Please enter an existing topic!\n(!: Mode Option)";
                            isSubscriberOption = false;
                            isErrorNumber = true;
                            break;
                        }
                    }
                    if(!isErrorNumber) {
                        msgToClient = showSubscribingToData(msgToClient);
                    }

                    // Nếu mảng mà là null hết thì isSubscribed = false
                    // Nếu mảng mà có 1 phần tử k null hết thì isSubscribed = true
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
                        isSubscribed = true;
                    }

                    isUnsub = false;
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
                else if(isSubscriber && isRole) {
                    dataOutputStream.writeUTF(ConfigMessage.msgInvalidDataPub + " (!: Mode Option)");
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
                        System.out.println("Lỗi parse data từ client" + e);
                    }

                    switch (roleClient){
                        case ConfigCommon.rolePub:
                            isPublisher = true;
                            isSubscriber = false;
                            msgToClient = ConfigMessage.helloName + instance.name;
                            break;
                        case ConfigCommon.roleSub:
//                            if(AuthenSubscriber(data, instance) || roleClient)
//                            {
                                if(!data.isEmpty()) {
                                    isPublisher = false;
                                    isSubscriber = true;
                                    isSubscriberOption = true;
                                    // Hiện tại đang chỉ vào đây 1 lần chào hỏi duy nhất. Có thể bỏ số 3 đi
                                    socketData = serverSocketData.accept();
                                    DataInputStream dataInputStreamData = new DataInputStream(socketData.getInputStream());
                                    DataOutputStream dataOutputStreamData = new DataOutputStream(socketData.getOutputStream());

                                    // create a new thread object
                                    Thread n = new CreateServerNonBlocking(socketData, serverSocketData, dataInputStreamData, dataOutputStreamData, instance.id);

                                    // Invoking the start() method
                                     n.start();
                                    msgToClient = ConfigMessage.helloName + instance.name + "\n 1. Subscribe. 2. Unsubscribe. 3. Show data subscribe last time";

                                   Util.WriteSubscriberJsonFile(data);
                                }
//                            }
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
            // closing resources
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

    public void WriteFile(Instance  instance, String content) throws IOException {
        String directoryName = "pnthuan/Location/" + instance.topic  + "/";
        String fileName = instance.name;
        File directory = new File(directoryName);
        if (!directory.exists()){
            directory.mkdir();
        }
        File file = new File(directoryName + "/" + fileName);
        if(!file.exists()){
            file.createNewFile();
        }

        try{
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public String  ReadFile(String path) throws FileNotFoundException {
        File infile = new File(path);
        FileInputStream fis = new FileInputStream(infile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try (FileReader fileReader = new FileReader(path)) {
            int data = fileReader.read();
            StringBuilder line = new StringBuilder();
            while (data != -1) {
                if (((char)data == '\n') || ((char)data == '\r')) {
                    line.delete(0, line.length());
                    data = fileReader.read();
                    continue;
                }
                line.append((char)data);
                data = fileReader.read();
            }
            return  line.toString();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return  "Không có dữ liệu dkm";
    }

    public String showSubscribingToData(String msgToClient){
        return "230 Subscribe sdssdsd";
    }


}
