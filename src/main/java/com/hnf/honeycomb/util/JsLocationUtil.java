package com.hnf.honeycomb.util;

import com.hnf.honeycomb.config.JsLocationsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.encoder.org.apache.commons.lang.ArrayUtils.isEmpty;

/**
 * @author lsj
 */
@Component
public class JsLocationUtil {
    @Autowired
    private JsLocationsConfig properties;

    Resource[] locations;

    @PostConstruct
    public void init() {
        locations = properties.resolveMapperLocations();
    }

    public Map<String, String> getJsLocation() {
        Map<String, String> locationMap = new HashMap<>();
        if (!isEmpty(this.locations)) {
            for (Resource mapperLocation : this.locations) {
                if (mapperLocation == null) {
                    continue;
                }
                // File file = new File(String.valueOf(mapperLocation));
                String string = null;
                InputStream inputStream = null;
                try {
                    inputStream = mapperLocation.getInputStream();
                    byte[] buf = new byte[inputStream.available()];
                    inputStream.read(buf);
                    string = new String(buf);
                    locationMap.put(mapperLocation.getFilename(), string);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
        return locationMap;

    }
}
