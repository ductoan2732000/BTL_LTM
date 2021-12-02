package publisher;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
public class Publisher1
{
    // initialize socket and input output streams
    private Socket socket            = null;
    private static DataInputStream  input   = null;
    private static DataOutputStream out     = null;
    boolean isHello = false, isSendId = false;
    private static final int role = 0, maxTemp = 50, minTemp = 20; //0: publisher, 1: subcriber
    private static final int id = 0; // id
    private static final String topic = "/myhome/template";
    private static String line = "";
    private static String recvBuf = "";
    // constructor to put ip address and port
    public Publisher1(String address, int port)
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
        while (!line.equals("QUIT"))
        {
            try
            {

                // Neu chua chao hoi
                if (!isHello){
                    line = "Hello Broker";
                    isHello = true;
                }

                // Neu chao hoi roi ma chua gui chi tiet client
                else if(recvBuf.contains("200")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", id);
                        jsonObject.put("topic", topic);
                        line = role + " " + jsonObject;
                        isSendId = true;
                }

                // Neu da gui chi tiet client thi gui data
                else if(isSendId){
                    setTimeout(() -> {
                        line = getData();
                        try {
                            out.writeUTF(line);
                            recvBuf = input.readUTF();
                        }
                        catch(IOException i)
                        {
                            System.out.println(i);
                        }
                    }, 1200000);

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
            catch(IOException i)
            {
                System.out.println(i);
            }
        }

        // close the connection
        try
        {
            out.writeUTF("QUIT");
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
     * setTimeout
     * @author: PVTRONG (27/11/2021)
     */
    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

    /**
     * Tu dong sinh du lieu
     * @return du lieu duoc sinh ra
     * @author: PVTRONG (27/11/2021)
     */
    private static String getData() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        System.out.println(formatter.format(date));
        String data = new String();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Temperature", Math.random() * (maxTemp - minTemp + 1) + minTemp);
        jsonObject.put("Time", formatter.format(date));
        return jsonObject.toString();
    }


    public static void main(String args[])
    {
        Publisher1 client = new Publisher1("127.0.0.1", 5000);
    }
}