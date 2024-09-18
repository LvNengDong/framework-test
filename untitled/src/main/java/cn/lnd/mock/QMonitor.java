package cn.lnd.mock;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/7 17:35
 */
public class QMonitor {
    public static void recordOne(String metricName) {}
    public static void recordOne(String metricName, long time) {}
    public static void recordSize(String metricName, long count) {}
    public static void generateRate(String metricName, String denominatorKey, String numeratorKey) {}
}
