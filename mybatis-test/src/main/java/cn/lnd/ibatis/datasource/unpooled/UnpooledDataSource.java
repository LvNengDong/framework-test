package cn.lnd.ibatis.datasource.unpooled;

import org.apache.ibatis.io.Resources;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 22:16
 */
public class UnpooledDataSource implements DataSource {

    /**
     * Driver 类加载器
     */
    private ClassLoader driverClassLoader;

    /**
     * Driver 属性
     */
    private Properties driverProperties;

    /**
     * 已注册的 Driver 映射
     *
     * KEY：Driver 类名
     * VALUE：Driver 对象
     */
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

    /**
     * Driver 类名
     */
    private String driver;

    /**
     * 数据库URL
     */
    private String url;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 是否自动提交事务
     */
    private Boolean autoCommit;

    /**
     * 默认事务隔离级别
     */
    private Integer defaultTransactionIsolationLevel;

    static {
        // 从DriverManager中读取JDBC驱动
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            // 将已在 DriverManager 中注册的 JDBC 驱动器实例复制一份到 registeredDrivers 集合中
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }

    public UnpooledDataSource() {
    }

    public UnpooledDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(String driver, String url, Properties driverProperties) {
        this.driver = driver;
        this.url = url;
        this.driverProperties = driverProperties;
    }

    public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
        this.driverClassLoader = driverClassLoader;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
        this.driverClassLoader = driverClassLoader;
        this.driver = driver;
        this.url = url;
        this.driverProperties = driverProperties;
    }

    /**
     * 获得 Connection 连接
     * @return
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    public ClassLoader getDriverClassLoader() {
        return driverClassLoader;
    }

    public void setDriverClassLoader(ClassLoader driverClassLoader) {
        this.driverClassLoader = driverClassLoader;
    }

    public Properties getDriverProperties() {
        return driverProperties;
    }

    public void setDriverProperties(Properties driverProperties) {
        this.driverProperties = driverProperties;
    }

    public String getDriver() {
        return driver;
    }

    public synchronized void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return defaultTransactionIsolationLevel;
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
    }

    private Connection doGetConnection(String username, String password) throws SQLException {
        // 创建 Properties 对象
        Properties props = new Properties();
        // 设置 driverProperties 到 props 中
        if (driverProperties != null) {
            props.putAll(driverProperties);
        }
        // 设置 user 和 password 到 props 中
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        // 执行获得 Connection 连接
        return doGetConnection(props);
    }


    private Connection doGetConnection(Properties properties) throws SQLException {
        // <1> 初始化 Driver
        initializeDriver();
        // <2> 获得 Connection 对象
        Connection connection = DriverManager.getConnection(url, properties);
        // <3> 配置 Connection 对象
        configureConnection(connection);
        return connection;
    }

    private synchronized void initializeDriver() throws SQLException { // <1>
        // 判断 registeredDrivers 是否已经存在该 driver ，若不存在，进行初始化
        if (!registeredDrivers.containsKey(driver)) {
            Class<?> driverType;
            try {
                // <2> 获得 driver 类 【实际上，就是我们常见的 "Class.forName("com.mysql.jdbc.Driver")"】
                if (driverClassLoader != null) {
                    driverType = Class.forName(driver, true, driverClassLoader);
                } else {
                    driverType = Resources.classForName(driver);
                }
                // <3> 创建 Driver 对象
                // DriverManager requires the driver to be loaded via the system ClassLoader.
                // http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
                Driver driverInstance = (Driver)driverType.newInstance();
                // 创建 DriverProxy 对象，并注册到 DriverManager 中
                DriverManager.registerDriver(new UnpooledDataSource.DriverProxy(driverInstance));
                // 添加到 registeredDrivers 中
                registeredDrivers.put(driver, driverInstance);
            } catch (Exception e) {
                throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
            }
        }
    }

    /**
     * 配置 Connection 对象
     * @param conn
     * @throws SQLException
     */
    private void configureConnection(Connection conn) throws SQLException {
        // 设置自动提交
        if (autoCommit != null && autoCommit != conn.getAutoCommit()) {
            conn.setAutoCommit(autoCommit);
        }

        // 设置事务隔离级别
        if (defaultTransactionIsolationLevel != null) {
            conn.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
    }

    private static class DriverProxy implements Driver {

        private Driver driver;

        DriverProxy(Driver d) {
            this.driver = d;
        }

        @Override
        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        @Override
        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        @Override
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        // @Override only valid jdk7+
        public Logger getParentLogger() {
            return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // <4>
        }

        // 只有 <4> 处，使用 MyBatis 自定义的 Logger 对象。其他方法，实际就是直接调用 driver 对应的方法。
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    // @Override only valid jdk7+
    public Logger getParentLogger() {
        // requires JDK version 1.6
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

}
