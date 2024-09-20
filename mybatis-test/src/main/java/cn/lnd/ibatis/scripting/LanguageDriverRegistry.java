package cn.lnd.ibatis.scripting;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:05
 */
public class LanguageDriverRegistry {

    private final Map<Class<?>, cn.lnd.ibatis.scripting.LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<Class<?>, cn.lnd.ibatis.scripting.LanguageDriver>();

    private Class<?> defaultDriverClass;

    public void register(Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        cn.lnd.ibatis.scripting.LanguageDriver driver = LANGUAGE_DRIVER_MAP.get(cls);
        if (driver == null) {
            try {
                driver = (cn.lnd.ibatis.scripting.LanguageDriver) cls.newInstance();
                LANGUAGE_DRIVER_MAP.put(cls, driver);
            } catch (Exception ex) {
                throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
            }
        }
    }

    public void register(cn.lnd.ibatis.scripting.LanguageDriver instance) {
        if (instance == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        cn.lnd.ibatis.scripting.LanguageDriver driver = LANGUAGE_DRIVER_MAP.get(instance.getClass());
        if (driver == null) {
            LANGUAGE_DRIVER_MAP.put(instance.getClass(), driver);
        }
    }

    public cn.lnd.ibatis.scripting.LanguageDriver getDriver(Class<?> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    public Class<?> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    public void setDefaultDriverClass(Class<?> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }

}
