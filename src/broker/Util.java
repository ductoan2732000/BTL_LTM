package broker;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Field;

public class Util {
    public  static void ConvertStringToObject (String stringToParse, Object object ) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(stringToParse);
        Field[] fields = object.getClass().getDeclaredFields();

    }
}
