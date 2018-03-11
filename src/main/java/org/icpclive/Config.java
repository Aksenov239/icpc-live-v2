package org.icpclive;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by icpclive on 2/6/2018.
 */
public class Config {

    public static Properties loadProperties(String name) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("config/" + name + ".properties"));
//        properties.load(ClassLoader.getSystemResourceAsStream(name + ".properties"));
        return properties;
    }
}
