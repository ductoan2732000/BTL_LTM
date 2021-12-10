package broker;

import broker.cache.CacheServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CreateServerNonBlocking extends Thread{


    private Socket socketData;
    private ServerSocket serverSocketData;
    private DataInputStream dataInputStreamData;
    private DataOutputStream dataOutputStreamData;

    public CreateServerNonBlocking(Socket socketData, ServerSocket serverSocketData, DataInputStream dataInputStreamData, DataOutputStream dataOutputStreamData) {
        this.socketData = socketData;
        this.serverSocketData = serverSocketData;
        this.dataInputStreamData = dataInputStreamData;
        this.dataOutputStreamData = dataOutputStreamData;
    }

    @Override
    public void run()
    {
        try {
            handleWrite();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void handleWrite() throws IOException, InterruptedException {
        while (true){
            Thread.sleep(3000);
            System.out.println("Writing");
            this.dataOutputStreamData.writeUTF("msgToClient");
        }


    }
}

