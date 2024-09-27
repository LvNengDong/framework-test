package cn.lnd.ibatis.scripting;


import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 *  
 *  LanguageDriver 注册表
 * 
 * @Date 2024/9/20 21:05
 */
public class LanguageDriverRegistry {

    /* LanguageDriver 映射 */
    private final Map<Class<?>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

    /* 默认的 LanguageDriver 类 */
    @Getter
    private Class<? extends LanguageDriver> defaultDriverClass;

    public void register(Class<? extends LanguageDriver> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        // 创建 cls 对应的对象，并添加到 LANGUAGE_DRIVER_MAP 中
        if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
            try {
                LANGUAGE_DRIVER_MAP.put(cls, cls.newInstance());
            } catch (Exception ex) {
                throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
            }
        }
    }

    public void register(LanguageDriver instance) {
        if (instance == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        // 添加到 LANGUAGE_DRIVER_MAP 中
        Class<? extends LanguageDriver> cls = instance.getClass();
        if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
            LANGUAGE_DRIVER_MAP.put(cls, instance);
        }
    }

    public LanguageDriver getDriver(Class<? extends LanguageDriver> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    /**
     * 设置 {@link #defaultDriverClass}
     *
     * @param defaultDriverClass 默认的 LanguageDriver 类
     */
    public void setDefaultDriverClass(Class<? extends LanguageDriver> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }

}
