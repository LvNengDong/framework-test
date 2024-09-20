package cn.lnd.ibatis.io;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @Author lnd
 * @Description Resource 工具类
 * @Date 2024/9/19 00:11
 */
public class Resources {

    // ClassLoaderWrapper 对象
    private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

    // 字符集
    private static Charset charset;

    Resources() {
    }

    /*
     * Returns the default classloader (may be null).
     *
     * @return The default classloader
     */
    public static ClassLoader getDefaultClassLoader() {
        return classLoaderWrapper.defaultClassLoader;
    }

    /*
     * Sets the default classloader
     *
     * @param defaultClassLoader - the new default ClassLoader
     */
    public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        classLoaderWrapper.defaultClassLoader = defaultClassLoader;
    }

    /*
        getResource： 基于 classLoaderWrapper 属性的封装。
    */


    /*
    * 静态方法，获得指定资源的 URL
    * */
    public static URL getResourceURL(String resource) throws IOException {
        // issue #625
        return getResourceURL(null, resource);
    }

    public static URL getResourceURL(ClassLoader loader, String resource) throws IOException {
        URL url = classLoaderWrapper.getResourceAsURL(resource, loader);
        if (url == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return url;
    }


    /**
     * 静态方法，获得指定资源的 InputStream
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(null, resource);
    }

    public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return in;
    }

    /**
     * 静态方法，获得指定资源的 Properties
     */
    public static Properties getResourceAsProperties(String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = getResourceAsStream(resource);
        props.load(in);
        in.close();
        return props;
    }

    /*
        静态方法，获得指定资源的 Reader
     */
    public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = getResourceAsStream(loader, resource);
        props.load(in);
        in.close();
        return props;
    }

    public static Reader getResourceAsReader(String resource) throws IOException {
        Reader reader;
        if (charset == null) {
            reader = new InputStreamReader(getResourceAsStream(resource));
        } else {
            reader = new InputStreamReader(getResourceAsStream(resource), charset);
        }
        return reader;
    }

    public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
        Reader reader;
        if (charset == null) {
            reader = new InputStreamReader(getResourceAsStream(loader, resource));
        } else {
            reader = new InputStreamReader(getResourceAsStream(loader, resource), charset);
        }
        return reader;
    }

    /*
     * 静态方法，获得指定资源的 File
     */
    public static File getResourceAsFile(String resource) throws IOException {
        return new File(getResourceURL(resource).getFile());
    }

    public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
        return new File(getResourceURL(loader, resource).getFile());
    }


    /*
     * 静态方法，获得指定 URL
     */
    public static InputStream getUrlAsStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        // 打开 URLConnection
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }

    /*
     * 静态方法，指定 URL 的 Reader
     */
    public static Reader getUrlAsReader(String urlString) throws IOException {
        Reader reader;
        if (charset == null) {
            reader = new InputStreamReader(getUrlAsStream(urlString));
        } else {
            reader = new InputStreamReader(getUrlAsStream(urlString), charset);
        }
        return reader;
    }

    /*
     * 静态方法，指定 URL 的 Properties
     */
    public static Properties getUrlAsProperties(String urlString) throws IOException {
        Properties props = new Properties();
        InputStream in = getUrlAsStream(urlString);
        props.load(in);
        in.close();
        return props;
    }

    /*
     * 静态方法，获得指定类名对应的类
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return classLoaderWrapper.classForName(className);
    }

    public static Charset getCharset() {
        return charset;
    }

    public static void setCharset(Charset charset) {
        Resources.charset = charset;
    }

}
