package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author arosenfeld
 */
public final class Configuration {

    private static Configuration instance;
    private Properties props;
    private String componentPath;

    private Configuration() {
    }

    public void setFile(String path) throws FileNotFoundException, IOException {
        props = new Properties();
        FileInputStream in = new FileInputStream(path);
        props.load(in);
        in.close();
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String path) {
        componentPath = path;
    }

    public String getValueAsString(String key) {
        return props.getProperty(key);
    }

    public String[] getValueAsArray(String key) {
        return getValueAsString(key).split(",");
    }

    public int getValueAsInt(String key) {
        return Integer.parseInt(getValueAsString(key));
    }

    public boolean getValueAsBool(String key) {
        return getValueAsString(key).toLowerCase().equals("true");
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }
}
