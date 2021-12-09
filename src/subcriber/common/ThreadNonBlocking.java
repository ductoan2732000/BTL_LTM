package subcriber.common;

import subcriber.Subcriber;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class ThreadNonBlocking implements Runnable {

    @Override
    public void run() {
        try {
            SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 8089));
            while (true){
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                client.read(buffer);
                String data = new String(buffer.array()).trim();
                if(Subcriber.isShow){
                    System.out.println(data);
                }
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
