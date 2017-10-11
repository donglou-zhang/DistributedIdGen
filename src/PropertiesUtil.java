import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Vincent on 2017/10/11.
 */
public class PropertiesUtil {

    public static HashMap<String, String> getAllProperties(String filePath){
        String realPath = PropertiesUtil.class.getClassLoader().getResource(filePath).getPath();
        HashMap<String, String> ret = new HashMap<>();
        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(realPath));
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Enumeration en = properties.propertyNames();
        while(en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String value = properties.getProperty(key);
            ret.put(key, value);
        }
        return ret;
    }
}
