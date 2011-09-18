package ants.core;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class Serialization {
    private static final XStream xstream;
    private static Map<String, Class<?>> classMap = new HashMap();

    static {
        xstream = new XStream() {
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    public Class realClass(String elementName) {
                        Class c = (Class) Serialization.classMap.get(elementName);
                        if (c != null) {
                            return c;
                        }
                        return super.realClass(elementName);
                    }
                };
            }
        };
    }

    public static Class<?> loadClass(File file) {
        String packageName = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            packageName = reader.readLine();
            if (!packageName.contains("package"))
                packageName = null;
            else
                packageName = packageName.substring("package ".length(), packageName.length() - 1);
        } catch (Exception e) {
            packageName = null;
        }
        try {
            URL url = file.getParentFile().toURI().toURL();
            URLClassLoader loader = URLClassLoader.newInstance(new URL[]{url}, ClassLoader.getSystemClassLoader());
            String fileName = file.getName();
            String name = fileName.substring(0, fileName.lastIndexOf('.'));
            if (packageName != null) {
                name = packageName + '.' + name;
            }
            Class ret = Class.forName(name, true, loader);
            classMap.put(name, ret);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static <T> T clone(T object) {
        String xml = xstream.toXML(object);
        return (T) xstream.fromXML(xml);
    }
}
