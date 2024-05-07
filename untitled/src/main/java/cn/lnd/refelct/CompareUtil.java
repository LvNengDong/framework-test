package cn.lnd.refelct;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/7 15:49
 */
@Slf4j
public class CompareUtil {

    private static final ConcurrentHashMap<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<>();


    private static final Set<Class<?>> COMMON_CLASS_SET = Sets.newHashSet();

    static {
        COMMON_CLASS_SET.add(String.class);
        COMMON_CLASS_SET.add(Date.class);
        COMMON_CLASS_SET.add(Integer.class);
        COMMON_CLASS_SET.add(Float.class);
        COMMON_CLASS_SET.add(Double.class);
        COMMON_CLASS_SET.add(Boolean.class);
        COMMON_CLASS_SET.add(Long.class);
        COMMON_CLASS_SET.add(Byte.class);
        COMMON_CLASS_SET.add(Short.class);
        COMMON_CLASS_SET.add(List.class);
        COMMON_CLASS_SET.add(Map.class);
    }

    /**
     * 换行分隔符
     */
    public static final String LINE_FEED = "\n";

    public static void compareObject(Object oldObject, Object newObject, StringBuffer diffMessage, StringBuffer upperMsg) {
        //有对象为空，则直接返回
        if (Objects.isNull(oldObject) || Objects.isNull(newObject)) {
            return;
        }
        //如果两个对象为不同类型，则直接返回
        if (!oldObject.getClass().equals(newObject.getClass())) {
            return;
        }
        Set<Field> allFields = getFields(oldObject.getClass());

        for (Field field : allFields) {
            CompareDesc annotation = field.getAnnotation(CompareDesc.class);
            if (annotation == null) {
                continue;
            }
            String desc = annotation.desc();
            if (desc == null) {
                continue;
            }
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), oldObject.getClass());
                Method readMethod = pd.getReadMethod();
                Object oldObjectValue = readMethod.invoke(oldObject);
                Object newObjectValue = readMethod.invoke(newObject);
                // 如果不是普通类型对象
                if (!isPrimitive(field)) {
                    upperMsg.append(desc).append("_");
                    compareObject(oldObjectValue, newObjectValue, diffMessage, upperMsg);
                    continue;
                }

                String oldObjectStr = oldObjectValue.toString();
                String newObjectStr = newObjectValue.toString();
                if (StringUtils.isBlank(oldObjectStr) && StringUtils.isBlank(newObjectStr)) {
                    continue;
                }
                if (oldObjectValue != null && newObjectValue == null) {
                    diffMessage.append(upperMsg).append("删除" + desc + ":" + oldObjectValue + LINE_FEED);
                } else if (oldObjectValue == null && newObjectValue != null) {
                    diffMessage.append(upperMsg).append("新增" + desc + ":" + newObjectValue + LINE_FEED);
                } else if (oldObjectValue != null && newObjectValue != null && !oldObjectValue.equals(newObjectValue)) {
                    diffMessage.append(upperMsg).append(desc + ":" + oldObjectValue + " >> " + newObjectValue + LINE_FEED);
                }

            } catch (Exception e) {
                log.error("变更对比出错", e);
            }
        }

        upperMsg.delete(0, upperMsg.length());
    }

    //获取某个类的所有属性，使用了ConcurrentHashMap做缓存，提升了系统性能
    private static Set<Field> getFields(Class clazz) {
        Set<Field> fields = fieldCache.get(clazz);
        if (CollectionUtils.isEmpty(fields)) {
            Set<Field> allFields = ReflectionUtils.getAllFields(clazz);
            fieldCache.put(clazz, allFields);
            return allFields;
        }
        return fields;
    }


    //判断某个对象是否是普通类型或者String,Date这种可以打印的类型
    private static boolean isPrimitive(Field field) {
        Class<?> type = field.getType();
        //基本数据类型直接返回
        if (type.isPrimitive()) {
            return true;
        }
        //如果是基本数据的包装类或者String，Date
        if (COMMON_CLASS_SET.contains(type)) {
            return true;
        }
        return false;
    }

    public static String getObjectMessage(Object object) {
        StringBuffer newMessage = new StringBuffer();
        Class<?> clazz = object.getClass();
        Set<Field> fields = getFields(clazz);
        for (Field field : fields) {
            CompareDesc annotation = field.getAnnotation(CompareDesc.class);
            if (annotation == null) {
                continue;
            }
            String desc = annotation.desc();
            if (desc == null) {
                continue;
            }
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), object.getClass());
                Method readMethod = pd.getReadMethod();
                Object oldObjectValue = readMethod.invoke(object);
                String fieldValue = Objects.toString(oldObjectValue, StringUtils.EMPTY);
                if (StringUtils.isNotBlank(fieldValue)) {
                    newMessage.append(desc + ":" + fieldValue + LINE_FEED);
                }
            } catch (Exception e) {
                log.error("获取对象属性信息", e);
            }
        }
        return newMessage.toString();
    }
}
