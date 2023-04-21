import com.mrl.util.HttpUtils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @Auther: MrL
 * @Date: 2023-04-21-10:07
 * @Description: PACKAGE_NAME-chatgptForJava
 * @Version: 1.0
 */
public class ChatGPTTest {
    public static void main(String[] args) {
        HashMap map = new HashMap<String,String>();
        map.put("d", "[]&,");
        System.out.println(HttpUtils.asUrlParams(map));

    }
}
