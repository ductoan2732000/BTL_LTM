package broker.cache;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class CacheServer {
    /**
     * @author: tdtoan 10.12.2021: phục vụ lưu cache các topic đã đăng ký của client
     * {
     *     "1001": [1,2],
     *     "1002": [1,2,3]
     * }
     *
     * Todo:
     *  - Khi mà subscriber đăng ký thì thì sẽ lưu id và mảng string id mà subscriber nhập lên
     *  - Khi mà subscriber huỷ đăng ký thì thì sẽ lưu id và mảng string id mà subscriber nhập lên
     * cách get: cacheArray.get("1001");
     * cách add: cacheArray.put("1003", new List<String>);
     */
    public static Hashtable<String, List<String>> cacheArray = new Hashtable<String, List<String>>();
//    public static List<String> idTopicArray = new ArrayList<String>();
}


/**
 * todo :
 * 1. thêm id cho topic detail
 * 2. mảng đange ký của toản sẽ lưu theo id chứ ko lưu theo theo topic name
 * 3. bỏ array đi => List<String>()
 * 4. Xử lý hàm private static void handleWrite => đang chưa lấy được data của trọng
 */