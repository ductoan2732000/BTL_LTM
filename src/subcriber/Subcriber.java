/**
 * @author: tdtoan 28.11.2021
 * file subcriber
 */
package subcriber;
import org.json.simple.JSONObject;
import subcriber.config.ConfigClient;
import subcriber.model.SubcriberUnique;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
public class Subcriber {
    public static void main(String argv[]) throws Exception
    {
        Boolean helo = false;
        Boolean send = false;
        Boolean listen = false;
        String string_to_server;
        String string_from_server;


        try{
            // khởi tạo socket đến server
            Socket clientSocket = new Socket(ConfigClient.host, ConfigClient.port);

            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

            if(helo == false){
                // kết nối xong thì chào hỏi ngay
                SubcriberUnique idenSub = new SubcriberUnique(1001, "tranductoan");
                Integer id = idenSub.getId();
                String name = idenSub.getName();

                // 1 helo
                string_to_server = "HELLO Server";
                output.writeBytes(string_to_server + '\n');
                string_from_server = in.readLine();
                if(!string_from_server.equals("200 Hello Client")){
                    System.exit(-1);
                }
                System.out.println("FROM SERVER: " + string_from_server);


                // 2 gui id vs name
                JSONObject jsonIden = new JSONObject();
                jsonIden.put("id", id);
                jsonIden.put("name", name);
                string_to_server = "1 " + jsonIden.toJSONString();
                output.writeBytes(string_to_server + '\n');
                string_from_server = in.readLine();

                if(!(string_from_server.contains("200") && string_from_server.contains(name)) ){
                    System.exit(-1);
                }
                System.out.println("FROM SERVER: " + string_from_server);
            }
            helo = true; // đã chào hỏi xong


            // chào hỏi xong thì đến phần gửi topic
            while (helo == true && send == false){
                Scanner ip= new Scanner(System.in);

                System.out.print("Input from client: ");
                string_to_server = ip.nextLine();


                if(string_to_server.equals("QUIT") ){
                    output.writeBytes(string_to_server + '\n');
                    string_from_server = in.readLine();
                    System.out.println("FROM SERVER: " + string_from_server);
                    clientSocket.close();
                    break;
                }
                else{
                    output.writeBytes(string_to_server + '\n');
                    string_from_server = in.readLine();
                    if(string_from_server.contains("200")){
                        System.out.println("FROM SERVER: " + string_from_server);
                        send = true;
                    }
                }
            }
            // đến phần lắng nghe server
            while (helo == true && send == true && listen == false){
                // todo:
                //  1.xử lý phần đang nghe từ server nhưng lại nhập input và dừng ct
                //  2.nếu bên public die thì dùng ct
                //  3. nếu server die thì dừng ct
                string_from_server = in.readLine();
                System.out.println("FROM SERVER: " + string_from_server);
            }


        }
        catch (Exception e){
            System.out.println(e);
            System.exit(-1);
        }



        System.exit(-1);
    }
}



