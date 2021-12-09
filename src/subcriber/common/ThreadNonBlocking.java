package subcriber.common;

import subcriber.Subcriber;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class ThreadNonBlocking extends Thread {
    SocketChannel client;
    public ThreadNonBlocking(SocketChannel client){
        this.client = client;
    }
    @Override
    public void run() {
        while (true){
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            try {
                this.client.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String data = new String(buffer.array()).trim();
            if(Subcriber.isShow){
                System.out.println(data);
            }
            buffer.clear();
        }

    }

}
