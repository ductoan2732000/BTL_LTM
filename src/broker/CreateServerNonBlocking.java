package broker;

import broker.cache.CacheServer;
import broker.cache.CacheTopic;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import util.ConfigCommon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class CreateServerNonBlocking extends Thread{


    private Socket socketData;
    private ServerSocket serverSocketData;
    private DataInputStream dataInputStreamData;
    private DataOutputStream dataOutputStreamData;
    private String id;

    public CreateServerNonBlocking(Socket socketData, ServerSocket serverSocketData, DataInputStream dataInputStreamData, DataOutputStream dataOutputStreamData,String  id) {
        this.socketData = socketData;
        this.serverSocketData = serverSocketData;
        this.dataInputStreamData = dataInputStreamData;
        this.dataOutputStreamData = dataOutputStreamData;
        this.id = id;
    }

    @Override
    public void run()
    {
        try {
            handleWrite();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void handleWrite() throws IOException, InterruptedException, ParseException {
        String msgToClient = "";
        while (CacheTopic.arrayTopic.size() > 0){
            Thread.sleep(3000);
            if(CacheServer.cacheArray.containsKey(this.id)){
                List<String> topic = CacheServer.cacheArray.get(this.id);
                for(int i =0;i < topic.size();i ++){
                    if(CacheTopic.arrayTopic.containsKey(topic.get(i))){
                        msgToClient += "\n" + CacheTopic.arrayTopic.get(topic.get(i));
                    }
                }

                if(msgToClient.trim() != "" && CacheServer.cacheIsShowData.get(this.id))
                    this.dataOutputStreamData.writeUTF(msgToClient + ConfigCommon.backOption);

                msgToClient = "";
            }
        }


    }
}

