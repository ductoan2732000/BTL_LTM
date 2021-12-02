package broker;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;


enum  Indentified {
    Subscirber("1"),
    Publisher("0");
    private final String value;
    private Indentified(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public String toString(){
        switch(this){
            case Subscirber:
                return "1";
            case Publisher :
                return "0";
        }
        return null;
    }
}

class Instance{
    public String id = null;
    public  String topic = null;
    public  String name = null;

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
    private DataInputStream dis      = null;
    private DataOutputStream dos     = null;
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

                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

                // create a new thread object
                Thread t = new ClientHandler(socket, dis, dos);

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
        Broker server = new Broker(5056);
    }
}

// ClientHandler classSocket
class ClientHandler extends Thread
{
    DateFormat fordate        = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime        = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket socket;


    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.socket = s;
        this.dis = dis;
        this.dos = dos;
    }
    /// xác thực publish, subscriber
    public String processData(String received){
        String toreturn = null;
        switch (received) {
            case "HELLO Server" :
                toreturn = "200 Hello Client";
                break;

            case "QUIT" :
                toreturn = "500 byte";
                break;

            default:
                toreturn = "404 Invalid input";
                break;
        }
        return toreturn;
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
        if(instance.id.equals("0001")){
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
        String received      = "";
        String toreturn      = "";
        boolean isSubscriber = false;
        boolean isPublisher = false;
        Instance instance = null;
        while (!received.equals("bye"))
        {
            try {
                // receive the answer from client
                received = dis.readUTF();

                if(isPublisher){
                    //nhận dữ liệu
                    // đẩy vào file
                    WriteFile(instance, received);
                    toreturn = "200 Success . Data save in location . \"/location/Temperary/sensor1\"";
                    dos.writeUTF(toreturn);
                    // send data to subscriber

                }
                else if(isSubscriber){
                    // xử lý dữ liệu subsriber
                    //Broker : 200 Subcriber Success.
                    //Broker : {name : "sensor1", Temperature : "30 độ c", "Time" : "10:10:60 18/01/2021"}
                    // nhập topic : tìm kiếm location, lưu log
                    switch (received){
                        default :
                            toreturn = "210 Subscriber Success";
                            dos.writeUTF(toreturn);
                            // tìm trong thư  mục có tồn tại  topic không pending
                            // fix data
                            String path = "pnthuan/Location/thuan/thuan";
                            received = ReadFile(path);
                            dos.writeUTF(received);
                            break;
                    }
                }
                else {
                    String i = null;
                    String data = null;
                    try {
                         i = received.substring(0, 1);
                         data = received.substring(2);
                         instance = Instance.CreateInstance(data);

                    }
                    catch (Exception e){
                        instance = null;
                        i = "-1";
                        System.out.println("Lỗi parse data từ client" + e);
                    }
                    System.out.println("Giá trị i : " + i);

                    switch (i){
                        case  "2":
                            if(AuthenPublisher(data, instance)){
                                isPublisher = true;
                                isSubscriber = false;
                                toreturn = "210 Hello " + instance.name;
                            }
                            break;
                        case  "1":
                            if(AuthenSubscriber(data, instance))
                            {
                                isPublisher = false;
                                isSubscriber = true;
                                // tạm fix dữ liệu
                                toreturn = "210 Hello " + instance.name + "\n Topic : 1. Temperature 2. humidity 3.....";
                            }
                            break;
                        default:
                            toreturn = processData(received);
                            break;
                        }

                    System.out.println("Message from Client (port: " + this.socket.getPort()+ ") : " + received);
                    dos.writeUTF(toreturn);
                    System.out.println("Message to Client (port: "+this.socket.getPort()+") : "+toreturn);
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
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public  void GetDataFromFile(){

    }
    public void WriteFile(Instance  instance, String content) throws IOException {
        String directoryName = "pnthuan/Location/" + instance.topic  + "/";
        String fileName = instance.name;
        File directory = new File(directoryName);
        if (! directory.exists()){
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
        try (FileReader fin = new FileReader(path)) {
            int data = fin.read();
            StringBuilder line = new StringBuilder();
            while (data != -1) {
                if (((char)data == '\n') || ((char)data == '\r')) {
                    line.delete(0, line.length());
                    data = fin.read();
                    continue;
                }
                line.append((char)data);
                data = fin.read();
            }
            return  line.toString();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return  "Không có dữ liệu dkm";
    }

}