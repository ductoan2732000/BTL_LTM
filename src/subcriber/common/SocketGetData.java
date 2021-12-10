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

    public SocketGetData(Socket clientSocketData, DataOutputStream outputData, DataInputStream inData) {
        this.clientSocketData = clientSocketData;
        this.outputData = outputData;
        this.inData = inData;
    }

    @Override
    public void run() {
        while (true){
            try {
                String data = inData.readUTF();
                if(Subcriber.isShow){
                    System.out.println((data));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
