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

    public Broker(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server start");
            System.out.println("Waiting a connection ...");
            while(true) {
                socket = serverSocket.accept();
                System.out.println("A new client is connected : " + socket);
                System.out.println("Assigning new thread for this client");

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                // create a new thread object
                Thread t = new ClientHandler(socket, dataInputStream, dataOutputStream);

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
        Broker server = new Broker(ConfigCommon.port);
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
    
    // Constructor
    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream)
    {
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
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
        boolean isRollback = true;
        boolean isSub = false;
        boolean isUnsub = false;
        Instance instance = null;
        String[] topics = Util.ArrayTopic();
        String[] subTopics = new String[topics.length];

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
                else if(isSubscriber && isRollback){
                    switch (msgFromClient){
                        case ConfigCommon.subTopic:
                            for(int i = 0; i < topics.length; i++) {
                                msgToClient += i + 1 + ". " + topics[i] + " ";
                            }
                            isSub = true;
                            isRollback = false;
                            break;
                        case ConfigCommon.unsubTopic:
                            break;
                        case  ConfigCommon.rollback:

                            break;

                        default :
                            String path = "pnthuan/Location/thuan/thuan";
                            msgFromClient = ReadFile(path);
                            dataOutputStream.writeUTF(msgFromClient);
                            break;
                    }
                    dataOutputStream.writeUTF(msgToClient);
                }
                else if(isSub) {
                    String[] dataSub = Util.convertStringToArray(msgFromClient);

                    for (int i = 0; i < dataSub.length; i++){
                        int number = Integer.parseInt(dataSub[i]);
                        if(topics.length < number ) {
                            msgToClient = "410 Topic not available. Please enter an existing topic!";
                            dataOutputStream.writeUTF(msgToClient);
                            break;
                        }else {
                            subTopics[i] = topics[number-1];
                        }
                    }
                    JSONArray topicArray = ReadTopicJsonFile();
                    msgToClient = "\n";

                    for(int index = 0; index < topicArray.size(); index ++ ){
                        JSONObject obj = (JSONObject) topicArray.get(index);

                        if(obj.get("topicName").toString().equals(subTopics[index])){
                            msgToClient += obj.toString() + "\n";
                        }
                    }
                    isSub = false;
                    dataOutputStream.writeUTF(msgToClient);
                    msgToClient = "";
                    Arrays.fill(dataSub, null);
                    Arrays.fill(subTopics, null);
                }
                else {
                    String roleClient = null;
                    String data = "";
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
                            case ConfigCommon.rollback:
//                            if(AuthenSubscriber(data, instance) || roleClient)
//                            {
                                isPublisher = false;
                                isSubscriber = true;
                                isRollback = true;
                                // tạm fix dữ liệu
                                msgToClient = ConfigMessage.helloName + instance.name + "\n 1. Subscribe. 2. Unsubscribe";

                                if(!data.isEmpty()) {
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

    public  void GetDataFromFile(){

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

}
