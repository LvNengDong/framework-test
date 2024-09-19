package cn.lnd.ibatis.mapping;

import cn.lnd.ibatis.mapping.ResultMapping;
import cn.lnd.ibatis.session.Configuration;

import java.util.Collections;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 15:16
 */
public class Discriminator {

    private ResultMapping resultMapping;
    private Map<String, String> discriminatorMap;

    Discriminator() {
    }

    public static class Builder {
        private cn.lnd.ibatis.mapping.Discriminator discriminator = new cn.lnd.ibatis.mapping.Discriminator();

        public Builder(Configuration configuration, ResultMapping resultMapping, Map<String, String> discriminatorMap) {
            discriminator.resultMapping = resultMapping;
            discriminator.discriminatorMap = discriminatorMap;
        }

        public cn.lnd.ibatis.mapping.Discriminator build() {
            assert discriminator.resultMapping != null;
            assert discriminator.discriminatorMap != null;
            assert !discriminator.discriminatorMap.isEmpty();
            //lock down map
            discriminator.discriminatorMap = Collections.unmodifiableMap(discriminator.discriminatorMap);
            return discriminator;
        }
    }

    public ResultMapping getResultMapping() {
        return resultMapping;
    }

    public Map<String, String> getDiscriminatorMap() {
        return discriminatorMap;
    }

    public String getMapIdFor(String s) {
        return discriminatorMap.get(s);
    }

}
