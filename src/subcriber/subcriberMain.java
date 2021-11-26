package subcriber;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class subcriberMain {

    public static void main(String[] args) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse("{\"topic\": \"John\"}");

        System.out.println(json.get("topic"));
    }

}
