package cn.lnd.ibatis.builder;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 16:55
 */
public interface InitializingObject {

    /**
     * Initialize a instance.
     * <p>
     * This method will be invoked after it has set all properties.
     * </p>
     * @throws Exception in the event of misconfiguration (such as failure to set an essential property) or if initialization fails
     */
    void initialize() throws Exception;

}
