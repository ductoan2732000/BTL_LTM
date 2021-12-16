package subcriber.common;

import com.sun.net.httpserver.Authenticator;
import subcriber.Subcriber;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class SocketGetData extends Thread {
    Socket clientSocketData;
    DataOutputStream outputData;
    DataInputStream inData;
    String Id;

    public SocketGetData(Socket clientSocketData, DataOutputStream outputData, DataInputStream inData, String id) {
        this.clientSocketData = clientSocketData;
        this.outputData = outputData;
        this.inData = inData;
        this.Id = id;
    }

    @Override
    public void run() {
        while (true){
            try {
                String data = inData.readUTF();
                if(true){
                    System.out.println((data));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
