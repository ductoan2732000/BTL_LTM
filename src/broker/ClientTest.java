/**
 * @author: tdtoan 28.11.2021
 * file subcriber
 */
package broker;
import subcriber.model.SubcriberUnique;
import util.ConfigCommon;
import util.ConfigMessage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientTest {
    public static void main(String argv[]) throws Exception
    {
        Integer subscriber = 1;
        Boolean helo = false;
        Boolean send = false;
        Boolean listen = false;
        String string_to_server;
        String string_from_server;

        try{
            // khởi tạo socket đến server
            Socket clientSocket = new Socket(ConfigCommon.host, ConfigCommon.port);

            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

            if(helo == false){
                // kết nối xong thì chào hỏi ngay
                // Chỗ id để là string
                SubcriberUnique idenSub = new SubcriberUnique(1001, "tranductoan", "temperature");
                Integer id = idenSub.getId();
                String name = idenSub.getName();

                // 1 helo
                string_to_server = ConfigMessage.helloServer;
                output.writeUTF(string_to_server);
                System.out.println(ConfigMessage.msgCacheClient2 + string_to_server);
                string_from_server = in.readUTF();
                if(!string_from_server.contains(ConfigCommon.requestSucceeded.toString())){
                    System.out.println(ConfigMessage.requestTimeout);
                    System.exit(-1);
                }
                System.out.println(ConfigMessage.msgCacheClient1+ string_from_server);


                // 2 gui id vs name
                string_to_server = idenSub.getJsonClient();
                if(!string_to_server.equals("")){
                    output.writeUTF(string_to_server);
                    System.out.println(ConfigMessage.msgCacheClient2 + string_to_server);
                    string_from_server = in.readUTF();
                    if(!string_from_server.contains(ConfigCommon.helloName.toString()) ){ // Đoạn này có vấn đề
                        System.out.println(ConfigMessage.requestTimeout);
                        System.exit(-1);
                    }
                    System.out.println(ConfigMessage.msgCacheClient1 + string_from_server);
                }
                else{
                    System.exit(-1);
                }
            }
            helo = true; // đã chào hỏi xong


            // chào hỏi xong thì đến phần gửi topic
            while (helo == true && send == false){
                Scanner ip= new Scanner(System.in);

                System.out.print(ConfigMessage.msgCacheClient2);
                string_to_server = ip.nextLine();


                if(string_to_server.equals(ConfigMessage.quit) ){
                    output.writeUTF(string_to_server );
                    string_from_server = in.readUTF();
                    System.out.println(ConfigMessage.msgCacheClient1 + string_from_server);
                    clientSocket.close();
                    break;
                }
                else{
                    output.writeUTF(string_to_server );
                    string_from_server = in.readUTF();
//                    if(string_from_server.contains(ConfigCommon.successTopicData.toString())){
//                        System.out.println(ConfigMessage.msgCacheClient1 + string_from_server);
//                        send = true;
//                    }
                    System.out.println("Server: " + string_from_server) ;
                }
            }
            // đến phần lắng nghe server
//            while (helo == true && send == true && listen == false){
//                // todo:
//                //  1.xử lý phần đang nghe từ server nhưng lại nhập input và dừng ct
//                //  2.nếu bên public die thì dùng ct
//                //  3. nếu server die thì dừng ct
//                string_from_server = in.readUTF();
//
//                System.out.println(ConfigMessage.msgCacheClient1 + string_from_server);
//            }


        }
        catch (Exception e){
            System.out.println(e);
            System.exit(-1);
        }



        System.exit(-1);
    }
}



