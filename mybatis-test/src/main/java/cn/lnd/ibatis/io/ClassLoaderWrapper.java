package cn.lnd.ibatis.io;

import java.io.InputStream;
import java.net.URL;

/**
 * @Author lnd
 * @Description ClassLoader 包装器。可使用多个 ClassLoader 加载对应的资源，直到有一成功后返回资源。
 * @Date 2024/9/19 14:40
 */
public class ClassLoaderWrapper {

    // 默认 ClassLoader 对象
    // 目前不存在初始化该属性的构造方法。但是该属性不是私有的，可通过 ClassLoaderWrapper.defaultClassLoader = xxx 的方式，进行设置。
    ClassLoader defaultClassLoader;
    // 系统 ClassLoader 对象
    ClassLoader systemClassLoader;

    ClassLoaderWrapper() {
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ignored) {
            // AccessControlException on Google App Engine
        }
    }

    /*
        #getResourceAsURL(String resource, ...) 方法，获得指定资源的 URL 。
            1、先调用 #getClassLoaders(ClassLoader classLoader) 方法，获得 ClassLoader 数组。
            2、再调用 #getResourceAsURL(String resource, ClassLoader[] classLoader) 方法，获得指定资源的 InputStream 。
    */

    /*
     * Get a resource as a URL using the current class path
     *
     * @param resource - the resource to locate
     * @return the resource or null
     */
    public URL getResourceAsURL(String resource) {
        return getResourceAsURL(resource, getClassLoaders(null));
    }

    /*
     * Get a resource from the classpath, starting with a specific class loader
     *
     * @param resource    - the resource to find
     * @param classLoader - the first classloader to try
     * @return the stream or null
     */
    public URL getResourceAsURL(String resource, ClassLoader classLoader) {
        return getResourceAsURL(resource, getClassLoaders(classLoader));
    }

    /*
     * 获得指定资源的 InputStream 对象
     * Get a resource from the classpath
     *
     * @param resource - the resource to find
     * @return the stream or null
     */
    public InputStream getResourceAsStream(String resource) {
        // 1、先调用 #getClassLoaders(ClassLoader classLoader) 方法，获得 ClassLoader 数组。
        // 2、再调用 #getResourceAsStream(String resource, ClassLoader[] classLoader) 方法，获得指定资源的 InputStream 。
        return getResourceAsStream(resource, getClassLoaders(null));
    }

    /*
     * Get a resource from the classpath, starting with a specific class loader
     *
     * @param resource    - the resource to find
     * @param classLoader - the first class loader to try
     * @return the stream or null
     */
    public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
        return getResourceAsStream(resource, getClassLoaders(classLoader));
    }

    /*
     * 获得指定类名对应的类
     * Find a class on the classpath (or die trying)
     *
     * @param name - the class to look for
     * @return - the class
     * @throws ClassNotFoundException Duh.
     */
    public Class<?> classForName(String name) throws ClassNotFoundException {
        //先调用 #getClassLoaders(ClassLoader classLoader) 方法，获得 ClassLoader 数组。
        //再调用 #classForName(String name, ClassLoader[] classLoader) 方法，获得指定类名对应的类。
        return classForName(name, getClassLoaders(null));
    }

    /*
     * Find a class on the classpath, starting with a specific classloader (or die trying)
     *
     * @param name        - the class to look for
     * @param classLoader - the first classloader to try
     * @return - the class
     * @throws ClassNotFoundException Duh.
     */
    public Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
        return classForName(name, getClassLoaders(classLoader));
    }

    /*
     * Try to get a resource from a group of classloaders
     *
     * @param resource    - the resource to get
     * @param classLoader - the classloaders to examine
     * @return the resource or null
     */
    InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
        // 遍历 ClassLoader 数组
        for (ClassLoader cl : classLoader) {
            if (null != cl) {
                // 获得 InputStream ，不带 /
                // try to find the resource as passed
                InputStream returnValue = cl.getResourceAsStream(resource);
                // now, some class loaders want this leading "/", so we'll add it and try again if we didn't find the resource
                // 获得 InputStream ，带 /
                if (null == returnValue) {
                    returnValue = cl.getResourceAsStream("/" + resource);
                }

                // 成功获得到，返回
                if (null != returnValue) {
                    return returnValue;
                }
            }
        }
        return null;
    }

    /*
     * Get a resource as a URL using the current class path
     *
     * @param resource    - the resource to locate
     * @param classLoader - the class loaders to examine
     * @return the resource or null
     */
    URL getResourceAsURL(String resource, ClassLoader[] classLoader) {
        URL url;
        // 遍历 ClassLoader 数组
        for (ClassLoader cl : classLoader) {
            if (null != cl) {
                // 获得 URL ，不带 /
                // look for the resource as passed in...
                url = cl.getResource(resource);

                // ...but some class loaders want this leading "/", so we'll add it
                // and try again if we didn't find the resource
                // 获得 URL ，带 /
                if (null == url) {
                    url = cl.getResource("/" + resource);
                }
                // 成功获得到，返回
                // "It's always in the last place I look for it!"
                // ... because only an idiot would keep looking for it after finding it, so stop looking already.
                if (null != url) {
                    return url;
                }
            }
        }
        // didn't find it anywhere.
        return null;
    }

    /*
     *
     * Attempt to load a class from a group of classloaders
     *
     * @param name        - the class to load
     * @param classLoader - the group of classloaders to examine
     * @return the class
     * @throws ClassNotFoundException - Remember the wisdom of Judge Smails: Well, the world needs ditch diggers, too.
     */
    Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException {
        // 遍历 ClassLoader 数组
        for (ClassLoader cl : classLoader) {
            if (null != cl) {
                try {
                    // 获得类
                    Class<?> c = Class.forName(name, true, cl);

                    // 成功获得到，返回
                    if (null != c) {
                        return c;
                    }
                } catch (ClassNotFoundException e) {
                    // we'll ignore this until all classloaders fail to locate the class
                }
            }
        }
        // 获得不到，抛出 ClassNotFoundException 异常
        throw new ClassNotFoundException("Cannot find class: " + name);
    }

    /**
     * 获得 ClassLoader 数组
     *
     * @param classLoader
     * @return
     */
    ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                classLoader,
                defaultClassLoader,
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader(),
                systemClassLoader};
    }

}
