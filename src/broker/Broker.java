package broker;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.ConfigCommon;
import util.ConfigMessage;

import java.io.*;
import java.text.*;
import java.net.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
/*
* Socket nonblocking
* */

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
        /// tạm fix tĩnh value
        Instance instance = new Instance();
        instance.id = json.get("id").toString();
        instance.topic = json.get("topic").toString();
        instance.name = json.get("name").toString();
        return  instance;
    }
    public String GetInfoInstance(Instance instance){
        return  "ID : " + instance.id + ",Topic : " + instance.topic + ",name : " + instance.name;
    }
}


public class Broker
{
    private ServerSocket serverSocket= null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
    private Socket socket = null;

    private static Selector selector = null;

    public Broker(int port, int portNonblocking) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server start");
            System.out.println("Waiting a connection ...");
            //

            selector = Selector.open();
            ServerSocketChannel nonSocket = ServerSocketChannel.open();
            ServerSocket serverNonSocket = nonSocket.socket();
            serverNonSocket.bind(new InetSocketAddress("localhost", portNonblocking));
            nonSocket.configureBlocking(false);
            int ops = nonSocket.validOps();
            nonSocket.register(selector, ops, null);


            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> i = selectedKeys.iterator();


            while(true) {
                socket = serverSocket.accept();
                System.out.println("A new client is connected : " + socket);
                System.out.println("Assigning new thread for this client");

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                // create a new thread object
                Thread t = new ClientHandler(socket, dataInputStream, dataOutputStream, nonSocket);

                // Invoking the start() method
                t.start();
            }
        }
        catch (IOException ioe)
        {
            System.out.println(ioe);
        }
    }

    public static void main(String[] args) throws IOException
    {
        // server is listening on port 5056
        Broker server = new Broker(ConfigCommon.port, 8089);
    }
}

// ClientHandler classSocket
class ClientHandler extends Thread
{
    DateFormat fordate        = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime        = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;
    final Socket socket;
    private ServerSocketChannel nonSocket;
    private static Selector selector = null;

    // Constructor
    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream, ServerSocketChannel nonSocket)
    {
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.nonSocket = nonSocket;
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

    public String processPublisher(String data) throws ParseException {
        // xác thực

        Instance instance = Instance.CreateInstance(data);
        System.out.println(instance.GetInfoInstance(instance));
        return null;
    }

    public  boolean AuthenSubscriber(String data, Instance instance) throws ParseException {
        // kiểm tra dữ liệu từ file
        // xác thực
        if(instance.id.equals("1001")){
            return true;
        }
        return false;
    }

    public  boolean AuthenPublisher(String data, Instance instance) throws ParseException {
        // kiểm tra dữ liệu từ file
        // xác thực
        if(instance.id.equals("0002")){
            return true;
        }
        return false;
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
        JSONArray topicArray = null;

        try {
            topicArray = ReadTopicJsonFile();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] subscribedArray = new String[topicArray.size()];

        while (!msgFromClient.equals(ConfigMessage.quit))
        {
            try {
                // Chỗ này có vấn đề rồi
                // receive the answer from client
                msgFromClient = "";
                msgFromClient = dataInputStream.readUTF();

                if(isPublisher){
                    // ???: Chưa hiểu
                    //nhận dữ liệu
                    // đẩy vào file
                    WriteFile(instance, msgFromClient);

                    // ???: Chỗ location thì sẽ như thế nào
                    // msgToClient = "200 Success. Data save in location . \"/location/Temperary/sensor1\"";

                    msgToClient = ConfigMessage.msgDataSucceededPub;
                    dataOutputStream.writeUTF(msgToClient);
                    System.out.println(msgToClient);
                    // send data to subscriber
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
                                for(int index = 0; index < topicArray.size(); index ++ ){
                                    JSONObject obj = (JSONObject) topicArray.get(index);

                                    if(!obj.get("topicName").toString().equals(subscribedArray[index])){
                                        msgToClient += index + 1 + ". " + obj.get("topicName") + " ";
                                    }
                                }
                            } else {
                                for(int index = 0; index < topicArray.size(); index++) {
                                    JSONObject obj = (JSONObject) topicArray.get(index);
                                    msgToClient += index + 1 + ". " + obj.get("topicName") + " ";
                                }
                            }
                            msgToClient += "\n(!: Mode Option)";
                            isSub = true;
                            isUnsub = false;
                            isSubscriberOption = false;
                            break;
                        case ConfigCommon.unsubTopic:
                            if (isSubscribed){
                                for(int index = 0; index < topicArray.size(); index ++ ){
                                    JSONObject obj = (JSONObject) topicArray.get(index);

                                    if(obj.get("topicName").toString().equals(subscribedArray[index])){
                                        msgToClient += index + 1 + ". " + obj.get("topicName") + " ";
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
//                            msgToClient = showSubscribingToData(msgToClient, topicArray, subscribedArray);
                            showSubscribingToData(msgToClient, topicArray, subscribedArray);
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
                    String[] dataSub = Util.convertStringToArray(msgFromClient); // Mảng lưu số của topic
                    boolean isErrorNumber = false;
                    // Xử lý việc sub
                    for (int i = 0; i < dataSub.length; i++){
                        int number = Integer.parseInt(dataSub[i]);
                        if(number > topicArray.size()) {
                            // Xử lý việc nếu nhập không trong giới hạn của topic
                            msgToClient = "410 Topic not available. Please enter an existing topic!\n(!: Mode Option)";
                            isSubscriberOption = false;
                            isErrorNumber = true;
                            break;
                        } else {
                            JSONObject obj = (JSONObject) topicArray.get(number - 1);
                            subscribedArray[number - 1] = (String) obj.get("topicName");
                        }
                    }

                    if(!isErrorNumber) {
//                        msgToClient = showSubscribingToData(msgToClient, topicArray, subscribedArray);
                        isSub = false;
                        showSubscribingToData(msgToClient, topicArray, subscribedArray);
                        isSubscribed = true;
                    }

                    isSub = false;
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                }
                else if(isSubscriber && isUnsub) { // Khi bam vao option va la unsub
                    String[] dataSub = Util.convertStringToArray(msgFromClient);
                    boolean isErrorNumber = false;
                    // Xử lý việc unsub
                    for (int i = 0; i < dataSub.length; i++){
                        int number = Integer.parseInt(dataSub[i]);
                        if(topicArray.size() < number ) {
                            msgToClient = "410 Topic not available. Please enter an existing topic!\n(!: Mode Option)";
                            isSubscriberOption = false;
                            isErrorNumber = true;
                            break;
                        } else {
                            subscribedArray[number - 1] = null;
                        }
                    }
                    if(!isErrorNumber) {
//                        msgToClient = showSubscribingToData(msgToClient, topicArray, subscribedArray);
                        isUnsub = false;
                        showSubscribingToData(msgToClient, topicArray, subscribedArray);
                    }

                    // Nếu mảng mà là null hết thì isSubscribed = false
                    // Nếu mảng mà có 1 phần tử k null hết thì isSubscribed = true
                    int countNull = 0;
                    int countNotNull = 0;
                    for (int i = 0; i < subscribedArray.length; i++){
                        if(subscribedArray[i] != null ) {
                            countNotNull++;
                        } else {
                            countNull++;
                        }
                    }

                    if(countNull == subscribedArray.length){
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
                            if(AuthenPublisher(data, instance)){
                                isPublisher = true;
                                isSubscriber = false;
                                msgToClient = ConfigMessage.helloName + instance.name;
                            }
                            break;
                        case ConfigCommon.roleSub:
//                            if(AuthenSubscriber(data, instance) || roleClient)
//                            {
                                if(!data.isEmpty()) {
                                    isPublisher = false;
                                    isSubscriber = true;
                                    isSubscriberOption = true;
                                    // Hiện tại đang chỉ vào đây 1 lần chào hỏi duy nhất. Có thể bỏ số 3 đi
                                    msgToClient = ConfigMessage.helloName + instance.name + "\n 1. Subscribe. 2. Unsubscribe. 3. Show data subscribe last time";

                                    WriteSubscriberJsonFile(data);
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
            } catch (InterruptedException e) {
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
        // ???: Chưa hiểu
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

    // Lưu mảng vào file json
    public void WriteSubscriberJsonFile(String data) throws ParseException {
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

    public JSONArray ReadTopicJsonFile() throws ParseException {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("src/broker/db/topic-detail.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray employeeList = (JSONArray) obj;

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

    public void showSubscribingToData(String msgToClient, JSONArray topicArray, String[] subscribedArray) throws IOException, InterruptedException {
        for(int index = 0; index < topicArray.size(); index ++ ){
            JSONObject obj = (JSONObject) topicArray.get(index);

            if(obj.get("topicName").toString().equals(subscribedArray[index])){
                msgToClient += "\n" + obj;
            }
        }

        if(msgToClient.isEmpty()) {
            msgToClient += "420 There are no registered topics yet";
        }

        msgToClient += "\n(!: Mode Option)";

        handleAccept(nonSocket, msgToClient);

//        return msgToClient;
    }

    private static void handleAccept(ServerSocketChannel mySocket, String msgToClient) throws IOException, InterruptedException {

        System.out.println("Connection Accepted...");

        // Accept the connection and set non-blocking mode
        SocketChannel client = mySocket.accept();
        client.configureBlocking(false);
        int ops = mySocket.validOps();
        // Register that client is reading this channel
        client.register(selector, SelectionKey.OP_WRITE);
        while (true){
            Thread.sleep(3000);
            handleWrite(client, msgToClient);
        }


    }

    private static void handleWrite(SocketChannel client, String msgToClient) throws IOException {
        System.out.println("Writing...");

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(msgToClient.getBytes());
        buffer.flip();
        client.write(buffer);
    }
}
