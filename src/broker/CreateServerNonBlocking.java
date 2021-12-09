package broker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CreateServerNonBlocking extends Thread{

    SocketChannel nonServer;
    // laays mangr đã đăng ký của client từ biến global ra để gửi data

    public CreateServerNonBlocking(SocketChannel nonServer ) {
        this.nonServer = nonServer;
    }
    @Override
    public void run()
    {
        try {
            while (true){
                // khoong check điều kiện là true nữa mà check điều kiện bên trọng có dữ liệu đẩy sang không, nếu có thì mới write sang bên client
                Thread.sleep(5000);
                handleWrite(this.nonServer);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static void handleWrite(SocketChannel client) throws IOException {
        System.out.println("Writing...");

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("I like non-blocking servers".getBytes());
        buffer.flip();
        client.write(buffer);
    }
}

