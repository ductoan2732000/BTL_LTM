package publisher.livingroom;

import org.json.simple.JSONObject;
import util.ConfigCommon;
import util.ConfigMessage;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Lamp extends LivingRoom
{
    private static final int enable = 1;
    private static final int disable = 0;
    // initialize socket and input output streams
    private Socket socket            = null;
    private static DataInputStream  input   = null;
    private static DataOutputStream out     = null;
    boolean isHello = false, isSendId = false;
    private static final int role = Integer.parseInt(ConfigCommon.rolePub), minTemp = disable, maxTemp = enable; //0: publisher, 1: subcriber
    private final  String id = "2"; // id
    private final  String name = "Lamp"; // id
    private final String topic = super.topic  + "/Lamp";
    private String line = "";
    private String recvBuf = "";
    // constructor to put ip address and port
    public Lamp(String address, int port)
    {
        // establish a connection
        try
        {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal
            input  = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // sends output to the socket
            out    = new DataOutputStream(socket.getOutputStream());
        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }

        // string to read message from input


        Scanner scanner = new Scanner(System.in);
        // keep reading until "Over" is input
        while (!line.equals(ConfigMessage.quit))
        {
            try
            {

                // Neu chua chao hoi
                if (!isHello){
                    line = ConfigMessage.helloServer;
                    isHello = true;
                }

                // Neu chao hoi roi ma chua gui chi tiet client
                else if(recvBuf.contains(ConfigCommon.requestSucceeded.toString())) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", id);
                    jsonObject.put("topic", topic);
                    jsonObject.put("name", name);
                    line = role + " " + jsonObject;
                    isSendId = true;
                }

                // Neu da gui chi tiet client thi gui data
                else if(isSendId){
                    while (true){
                        Thread.sleep(2000);
                        line = getData();
                        out.writeUTF(line);
                        recvBuf = input.readUTF();
                    }
                }

                out.writeUTF(line);
                if (line.length() < 0){
                    System.out.println("Error when sending data\n");
                    break;
                }

                recvBuf = input.readUTF();
                System.out.println(recvBuf);
                if (recvBuf.length() < 0){
                    System.out.println("Error when receiving data\n");
                    break;
                }
                System.out.println("Message received from server: " + recvBuf);
            }
            catch(IOException | InterruptedException i)
            {
                System.out.println(i);
            }
        }

        // close the connection
        try
        {
            out.writeUTF(ConfigMessage.quit);
            input.close();
            out.close();
            socket.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }


    /**
     * Tu dong sinh du lieu
     * @return du lieu duoc sinh ra
     * @author: PVTRONG (27/11/2021)
     */
    private String getData() throws IOException {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", (int) (Math.random() * (maxTemp - minTemp + 1) + minTemp));
        jsonObject.put("time", formatter.format(date));
        jsonObject.put("id", this.id);
        jsonObject.put("topicName", this.topic);
        jsonObject.put("name", this.name);
        System.out.println( jsonObject.toString());

        return jsonObject.toString();
    }


    public static void main(String args[])
    {
        Lamp client = new Lamp(ConfigCommon.host, ConfigCommon.port);
    }
}
