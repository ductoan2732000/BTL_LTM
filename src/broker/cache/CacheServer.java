package broker.cache;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class CacheServer {
    /**
     * @author: tdtoan 10.12.2021: phục vụ lưu cache các topic đã đăng ký của client
     * {
     *     "1001": [1,2],
     *     "1001: [1,2,3]
     * }
     * cách get: cacheArray.get("1001");
     * cách add: cacheArray.put("1003", new List<String>);
     */
    public static Hashtable<String, List<String>> cacheArray = new Hashtable<String, List<String>>();
}
