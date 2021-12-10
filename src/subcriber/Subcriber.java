/**
 * @author: tdtoan 28.11.2021
 * file subcriber
 */
package subcriber;
import subcriber.common.SocketGetData;
import subcriber.model.SubcriberUnique;
import util.ConfigCommon;
import util.ConfigMessage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
public class Subcriber {
    public static Boolean isShow = false;
    public static void main(String argv[])
    {
        Boolean helo = false;
        Boolean hasNonblockingSocet = false;
        String string_to_server;
        String string_from_server;


        try{
            // khởi tạo socket đến server
            Socket clientSocket = new Socket(ConfigCommon.host, ConfigCommon.port);

            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            // todo
            Thread.sleep(4000);

//            SocketChannel clientNon = SocketChannel.open(new InetSocketAddress("localhost", 8089));
//            try {
//                new Thread(new ThreadNonBlocking(clientNon)).start();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

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












                    Thread.sleep(3000);
                    Socket clientSocketData = new Socket(ConfigCommon.host, 8089);

                    DataOutputStream outputData = new DataOutputStream(clientSocketData.getOutputStream());
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(clientSocketData.getInputStream()));

                    new Thread(new SocketGetData(clientSocketData, outputData, inData)).start();









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

            Scanner ip= new Scanner(System.in);
            // chào hỏi xong thì đến phần gửi topic
            while (helo == true){
                // neeus có nhập input
                if(ip.hasNext()){
                    isShow = false;
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
                        System.out.println(ConfigMessage.msgCacheClient1 + string_from_server) ;
                        if(string_from_server.contains(ConfigCommon.successTopicData.toString())){
                            // nếu chưa có socket nonblocking thì tạo mới và nghe data

//                            if(!hasNonblockingSocet){
//                                Thread.sleep(5000);
//                                try {
//                                    new Thread(new ThreadNonBlocking()).start();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                hasNonblockingSocet = true;
//                            }
                            isShow = true;
                        }

                    }
                }


            }

        }
        catch (Exception e){
            System.out.println(e);
            System.exit(-1);
        }
        System.exit(-1);
    }
}



