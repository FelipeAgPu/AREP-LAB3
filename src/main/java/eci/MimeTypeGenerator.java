package eci;

import java.util.*;

public class MimeTypeGenerator {
    private final Map<String, String> map;

    public MimeTypeGenerator() {
        this.map  = new HashMap<String, String>();
        map.put("html", "text/html");
        map.put("css", "text/css");
        map.put("js", "text/javascript");
        map.put("png", "image/png");
    }

    public void addType(String key, String value){
        map.put(key, value);
    }

    public String getType(String key){
        return map.get(key);
    }
}
