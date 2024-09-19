package cn.lnd.ibatis.io;

import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 11:37
 */
public class JBoss6VFS extends VFS{
    private static final Log log = LogFactory.getLog(cn.lnd.ibatis.io.JBoss6VFS.class);

    /** A class that mimics a tiny subset of the JBoss VirtualFile class. */
    static class VirtualFile {
        static Class<?> VirtualFile;
        static Method getPathNameRelativeTo, getChildrenRecursively;

        Object virtualFile;

        VirtualFile(Object virtualFile) {
            this.virtualFile = virtualFile;
        }

        String getPathNameRelativeTo(cn.lnd.ibatis.io.JBoss6VFS.VirtualFile parent) {
            try {
                return invoke(getPathNameRelativeTo, virtualFile, parent.virtualFile);
            } catch (IOException e) {
                // This exception is not thrown by the called method
                log.error("This should not be possible. VirtualFile.getPathNameRelativeTo() threw IOException.");
                return null;
            }
        }

        List<cn.lnd.ibatis.io.JBoss6VFS.VirtualFile> getChildren() throws IOException {
            List<?> objects = invoke(getChildrenRecursively, virtualFile);
            List<cn.lnd.ibatis.io.JBoss6VFS.VirtualFile> children = new ArrayList<cn.lnd.ibatis.io.JBoss6VFS.VirtualFile>(objects.size());
            for (Object object : objects) {
                children.add(new cn.lnd.ibatis.io.JBoss6VFS.VirtualFile(object));
            }
            return children;
        }
    }

    /** A class that mimics a tiny subset of the JBoss VFS class. */
    static class VFS {
        static Class<?> VFS;
        static Method getChild;

        private VFS() {
            // Prevent Instantiation
        }

        static cn.lnd.ibatis.io.JBoss6VFS.VirtualFile getChild(URL url) throws IOException {
            Object o = invoke(getChild, VFS, url);
            return o == null ? null : new cn.lnd.ibatis.io.JBoss6VFS.VirtualFile(o);
        }
    }

    /** Flag that indicates if this VFS is valid for the current environment. */
    private static Boolean valid;

    /** Find all the classes and methods that are required to access the JBoss 6 VFS. */
    protected static synchronized void initialize() {
        if (valid == null) {
            // Assume valid. It will get flipped later if something goes wrong.
            valid = Boolean.TRUE;

            // Look up and verify required classes
            cn.lnd.ibatis.io.JBoss6VFS.VFS.VFS = checkNotNull(getClass("org.jboss.vfs.VFS"));
            cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.VirtualFile = checkNotNull(getClass("org.jboss.vfs.VirtualFile"));

            // Look up and verify required methods
            cn.lnd.ibatis.io.JBoss6VFS.VFS.getChild = checkNotNull(getMethod(cn.lnd.ibatis.io.JBoss6VFS.VFS.VFS, "getChild", URL.class));
            cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.getChildrenRecursively = checkNotNull(getMethod(cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.VirtualFile,
                    "getChildrenRecursively"));
            cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.getPathNameRelativeTo = checkNotNull(getMethod(cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.VirtualFile,
                    "getPathNameRelativeTo", cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.VirtualFile));

            // Verify that the API has not changed
            checkReturnType(cn.lnd.ibatis.io.JBoss6VFS.VFS.getChild, cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.VirtualFile);
            checkReturnType(cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.getChildrenRecursively, List.class);
            checkReturnType(cn.lnd.ibatis.io.JBoss6VFS.VirtualFile.getPathNameRelativeTo, String.class);
        }
    }

    /**
     * Verifies that the provided object reference is null. If it is null, then this VFS is marked
     * as invalid for the current environment.
     *
     * @param object The object reference to check for null.
     */
    protected static <T> T checkNotNull(T object) {
        if (object == null) {
            setInvalid();
        }
        return object;
    }

    /**
     * Verifies that the return type of a method is what it is expected to be. If it is not, then
     * this VFS is marked as invalid for the current environment.
     *
     * @param method The method whose return type is to be checked.
     * @param expected A type to which the method's return type must be assignable.
     * @see Class#isAssignableFrom(Class)
     */
    protected static void checkReturnType(Method method, Class<?> expected) {
        if (method != null && !expected.isAssignableFrom(method.getReturnType())) {
            log.error("Method " + method.getClass().getName() + "." + method.getName()
                    + "(..) should return " + expected.getName() + " but returns "
                    + method.getReturnType().getName() + " instead.");
            setInvalid();
        }
    }

    /** Mark this {@link cn.lnd.ibatis.io.JBoss6VFS.VFS} as invalid for the current environment. */
    protected static void setInvalid() {
        if (cn.lnd.ibatis.io.JBoss6VFS.valid == Boolean.TRUE) {
            log.debug("JBoss 6 VFS API is not available in this environment.");
            cn.lnd.ibatis.io.JBoss6VFS.valid = Boolean.FALSE;
        }
    }

    static {
        initialize();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public List<String> list(URL url, String path) throws IOException {
        cn.lnd.ibatis.io.JBoss6VFS.VirtualFile directory;
        directory = cn.lnd.ibatis.io.JBoss6VFS.VFS.getChild(url);
        if (directory == null) {
            return Collections.emptyList();
        }

        if (!path.endsWith("/")) {
            path += "/";
        }

        List<cn.lnd.ibatis.io.JBoss6VFS.VirtualFile> children = directory.getChildren();
        List<String> names = new ArrayList<String>(children.size());
        for (cn.lnd.ibatis.io.JBoss6VFS.VirtualFile vf : children) {
            names.add(path + vf.getPathNameRelativeTo(directory));
        }

        return names;
    }
}
