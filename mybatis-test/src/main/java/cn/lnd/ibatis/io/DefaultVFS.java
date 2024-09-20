package cn.lnd.ibatis.io;

import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @Author lnd
 * @Description 继承 VFS 抽象类，默认的 VFS 实现类
 * @Date 2024/9/19 11:36
 */
public class DefaultVFS extends VFS {
    private static final Log log = LogFactory.getLog(DefaultVFS.class);

    /** The magic header that indicates a JAR (ZIP) file. */
    private static final byte[] JAR_MAGIC = { 'P', 'K', 3, 4 };

    /**
     * @return 都返回 true ，因为默认支持。
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * 递归的列出所有的资源们
     *
     * 代码有点长，重点读懂 <1> 和 <2> 处的代码，基本就可以了。
     * 大体逻辑就是，不断递归文件夹，获得到所有文件。
     * 涉及到对 Jar 的处理，感兴趣的胖友，可以自己理解下。😈 艿艿暂时没看的特别细。
     */
    @Override
    public List<String> list(URL url, String path) throws IOException {
        InputStream is = null;
        try {
            List<String> resources = new ArrayList<String>();

            // First, try to find the URL of a JAR file containing the requested resource. If a JAR
            // file is found, then we'll list child resources by reading the JAR.
            // 如果 url 指向的是 Jar Resource ，则返回该 Jar Resource ，否则返回 null
            URL jarUrl = findJarForResource(url);
            if (jarUrl != null) {
                is = jarUrl.openStream();
                if (log.isDebugEnabled()) {
                    log.debug("Listing " + url);
                }
                // 遍历 Jar Resource
                resources = listResources(new JarInputStream(is), path);
            }
            else {
                List<String> children = new ArrayList<String>();
                try {
                    // 判断为 JAR URL
                    if (isJar(url)) {
                        // Some versions of JBoss VFS might give a JAR stream even if the resource
                        // referenced by the URL isn't actually a JAR
                        is = url.openStream();
                        JarInputStream jarInput = new JarInputStream(is);
                        if (log.isDebugEnabled()) {
                            log.debug("Listing " + url);
                        }
                        for (JarEntry entry; (entry = jarInput.getNextJarEntry()) != null;) {
                            if (log.isDebugEnabled()) {
                                log.debug("Jar entry: " + entry.getName());
                            }
                            children.add(entry.getName());
                        }
                        jarInput.close();
                    }
                    else {
                        /*
                         * Some servlet containers allow reading from directory resources like a
                         * text file, listing the child resources one per line. However, there is no
                         * way to differentiate between directory and file resources just by reading
                         * them. To work around that, as each line is read, try to look it up via
                         * the class loader as a child of the current resource. If any line fails
                         * then we assume the current resource is not a directory.
                         */
                        // 【重点】<1> 获得路径下的所有资源
                        is = url.openStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        List<String> lines = new ArrayList<String>();
                        for (String line; (line = reader.readLine()) != null;) {
                            if (log.isDebugEnabled()) {
                                log.debug("Reader entry: " + line);
                            }
                            lines.add(line);
                            if (getResources(path + "/" + line).isEmpty()) {
                                lines.clear();
                                break;
                            }
                        }

                        if (!lines.isEmpty()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Listing " + url);
                            }
                            children.addAll(lines);
                        }
                    }
                } catch (FileNotFoundException e) {
                    /*
                     * For file URLs the openStream() call might fail, depending on the servlet
                     * container, because directories can't be opened for reading. If that happens,
                     * then list the directory directly instead.
                     */
                    if ("file".equals(url.getProtocol())) {
                        File file = new File(url.getFile());
                        if (log.isDebugEnabled()) {
                            log.debug("Listing directory " + file.getAbsolutePath());
                        }
                        if (file.isDirectory()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Listing " + url);
                            }
                            children = Arrays.asList(file.list());
                        }
                    }
                    else {
                        // No idea where the exception came from so rethrow it
                        throw e;
                    }
                }

                // The URL prefix to use when recursively listing child resources
                // 【重点】<2> 计算 prefix
                String prefix = url.toExternalForm();
                if (!prefix.endsWith("/")) {
                    prefix = prefix + "/";
                }

                // Iterate over immediate children, adding files and recursing into directories
                // 【重点】 <2> 遍历子路径
                for (String child : children) {
                    // 添加到 resources 中
                    String resourcePath = path + "/" + child;
                    resources.add(resourcePath);
                    // 递归遍历子路径，并将结果添加到 resources 中
                    URL childUrl = new URL(prefix + child);
                    resources.addAll(list(childUrl, resourcePath));
                }
            }

            return resources;
        } finally {
            // 关闭文件流
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * List the names of the entries in the given {@link JarInputStream} that begin with the
     * specified {@code path}. Entries will match with or without a leading slash.
     *
     * 遍历 Jar Resource
     *
     * @param jar The JAR input stream
     * @param path The leading path to match
     * @return The names of all the matching entries
     * @throws IOException If I/O errors occur
     */
    protected List<String> listResources(JarInputStream jar, String path) throws IOException {
        // Include the leading and trailing slash when matching names
        // 保证头尾都是 /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        // Iterate over the entries and collect those that begin with the requested path
        // 遍历条目并收集以请求路径开头的条目
        List<String> resources = new ArrayList<>();
        for (JarEntry entry; (entry = jar.getNextJarEntry()) != null; ) {
            if (!entry.isDirectory()) {
                // Add leading slash if it's missing
                String name = entry.getName();
                if (!name.startsWith("/")) {
                    name = "/" + name;
                }

                // Check file name
                if (name.startsWith(path)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found resource: " + name);
                    }
                    // Trim leading slash
                    resources.add(name.substring(1));
                }
            }
        }
        return resources;
    }

    /**
     * Attempts to deconstruct the given URL to find a JAR file containing the resource referenced
     * by the URL. That is, assuming the URL references a JAR entry, this method will return a URL
     * that references the JAR file containing the entry. If the JAR cannot be located, then this
     * method returns null.
     *
     * 如果 url 指向的是 Jar Resource ，则返回该 Jar Resource ，否则返回 null
     *
     * @param url The URL of the JAR entry.
     * @return The URL of the JAR file, if one is found. Null if not.
     * @throws MalformedURLException
     */
    protected URL findJarForResource(URL url) throws MalformedURLException {
        if (log.isDebugEnabled()) {
            log.debug("Find JAR URL: " + url);
        }

        // If the file part of the URL is itself a URL, then that URL probably points to the JAR
        // 这段代码看起来比较神奇，虽然看起来没有 break 的条件，但是是通过 MalformedURLException 异常进行
        // 正如上面英文注释，如果 URL 的文件部分本身就是 URL ，那么该 URL 可能指向 JAR
        try {
            for (; ; ) {
                url = new URL(url.getFile());
                if (log.isDebugEnabled()) {
                    log.debug("Inner URL: " + url);
                }
            }
        } catch (MalformedURLException e) {
            // This will happen at some point and serves as a break in the loop
        }

        // Look for the .jar extension and chop off everything after that
        // 判断是否意 .jar 结尾
        StringBuilder jarUrl = new StringBuilder(url.toExternalForm());
        int index = jarUrl.lastIndexOf(".jar");
        if (index >= 0) {
            jarUrl.setLength(index + 4);
            if (log.isDebugEnabled()) {
                log.debug("Extracted JAR URL: " + jarUrl);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Not a JAR: " + jarUrl);
            }
            return null; // 如果不以 .jar 结尾，则直接返回 null
        }

        // Try to open and test it
        try {
            URL testUrl = new URL(jarUrl.toString());
            // 判断是否为 Jar 文件
            if (isJar(testUrl)) {
                return testUrl;
            } else {
                // WebLogic fix: check if the URL's file exists in the filesystem.
                if (log.isDebugEnabled()) {
                    log.debug("Not a JAR: " + jarUrl);
                }
                // 获得文件
                jarUrl.replace(0, jarUrl.length(), testUrl.getFile()); // 替换
                File file = new File(jarUrl.toString());
                // File name might be URL-encoded
                if (!file.exists()) { // 处理路径编码问题
                    try {
                        file = new File(URLEncoder.encode(jarUrl.toString(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Unsupported encoding?  UTF-8?  That's unpossible.");
                    }
                }

                // 判断文件存在
                if (file.exists()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Trying real file: " + file.getAbsolutePath());
                    }
                    testUrl = file.toURI().toURL();
                    // 判断是否为 Jar 文件
                    if (isJar(testUrl)) {
                        return testUrl;
                    }
                }
            }
        } catch (MalformedURLException e) {
            log.warn("Invalid JAR URL: " + jarUrl);
        }

        if (log.isDebugEnabled()) {
            log.debug("Not a JAR: " + jarUrl);
        }
        return null;
    }

    /**
     * Converts a Java package name to a path that can be looked up with a call to
     * {@link ClassLoader#getResources(String)}.
     *
     * @param packageName The Java package name to convert to a path
     */
    protected String getPackagePath(String packageName) {
        return packageName == null ? null : packageName.replace('.', '/');
    }

    /**
     * Returns true if the resource located at the given URL is a JAR file.
     *
     * 判断是否为 JAR URL
     * @param url The URL of the resource to test.
     */
    protected boolean isJar(URL url) {
        return isJar(url, new byte[JAR_MAGIC.length]);
    }

    /**
     * Returns true if the resource located at the given URL is a JAR file.
     *
     * @param url The URL of the resource to test.
     * @param buffer A buffer into which the first few bytes of the resource are read. The buffer
     *            must be at least the size of {@link #JAR_MAGIC}. (The same buffer may be reused
     *            for multiple calls as an optimization.)
     */
    protected boolean isJar(URL url, byte[] buffer) {
        InputStream is = null;
        try {
            is = url.openStream();
            // 读取文件头
            is.read(buffer, 0, JAR_MAGIC.length);
            // 判断文件头的 magic number 是否符合 JAR
            if (Arrays.equals(buffer, JAR_MAGIC)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found JAR: " + url);
                }
                return true;
            }
        } catch (Exception e) {
            // Failure to read the stream means this is not a JAR
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return false;
    }
}
