package broker;

import broker.cache.CacheServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
        JSONArray topicList = Util.ReadTopicJsonFile();
        String msgToClient = "";
        while (!topicList.isEmpty()){
            Thread.sleep(3000);
            for(int index = 0; index < topicList.size(); index++ ){
                JSONObject obj = (JSONObject) topicList.get(index);
                if(CacheServer.cacheArray.get(this.id).contains(obj.get("id"))){
                    msgToClient += "\n" + obj;
                }
            }
            this.dataOutputStreamData.writeUTF(msgToClient);
            msgToClient = "";
        }

    }
}

