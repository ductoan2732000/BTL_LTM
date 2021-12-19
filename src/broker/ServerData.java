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
import java.util.Arrays;
import java.util.List;

public class ServerData extends Thread{


    private Socket socketData;
    private ServerSocket serverSocketData;
    private DataInputStream dataInputStreamData;
    private DataOutputStream dataOutputStreamData;
    private String id;

    public ServerData(Socket socketData, ServerSocket serverSocketData, DataInputStream dataInputStreamData, DataOutputStream dataOutputStreamData, String  id) {
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
            Thread.sleep(5000);
            if(CacheServer.cacheArray.containsKey(this.id)){

                List <String> arrayTopicName = Util.getArrayTopicName();//dữ liệu publisher
                List<String> topic = CacheServer.cacheArray.get(this.id);// dữ liệu ng dùng nhập
                for(int i =0;i < topic.size();i ++){
                    String res = Util.getDataCacheTopic(topic.get(i).substring(1),CacheTopic.arrTopic);
                    if(res != null && !res.equals("")){
                        msgToClient += "\n" + Util.getDataCacheTopic(topic.get(i).substring(1),CacheTopic.arrTopic);
                    }
                }

                if(msgToClient.trim() != "" && CacheServer.cacheIsShowData.get(this.id))
                    this.dataOutputStreamData.writeUTF(msgToClient + ConfigCommon.backOption);

                msgToClient = "";
            }
        }


    }
}

