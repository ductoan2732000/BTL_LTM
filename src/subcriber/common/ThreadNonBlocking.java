package subcriber.common;

import subcriber.Subcriber;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class ThreadNonBlocking extends Thread {
    Socket clientSocketData;
    DataOutputStream outputData;
    DataInputStream inData;

    public ThreadNonBlocking(Socket clientSocketData, DataOutputStream outputData, DataInputStream inData) {
        this.clientSocketData = clientSocketData;
        this.outputData = outputData;
        this.inData = inData;
    }

    @Override
    public void run() {
        while (Subcriber.isShow){
            try {

                String cccccc = inData.readUTF();
                System.out.println((cccccc));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
