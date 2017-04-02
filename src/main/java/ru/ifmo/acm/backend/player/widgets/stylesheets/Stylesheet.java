package ru.ifmo.acm.backend.player.widgets.stylesheets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class Stylesheet {
    public static HashMap<String, String> styles = new HashMap<>();
    static Properties properties;

    static {
        Properties mainProperties = new Properties();
        properties = new Properties();
        try {
            mainProperties.load(Stylesheet.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
            String stylesheet = mainProperties.getProperty("stylesheet");

            properties.load(Stylesheet.class.getClassLoader().getResourceAsStream(stylesheet));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        for (String name : properties.stringPropertyNames()) {
            styles.put(name, properties.getProperty(name));
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (String name : properties.stringPropertyNames()) {
                if (styles.containsKey(styles.get(name))) {
                    changed = true;
                    styles.put(name, styles.get(styles.get(name)));
                }
            }
        }
    }
}
