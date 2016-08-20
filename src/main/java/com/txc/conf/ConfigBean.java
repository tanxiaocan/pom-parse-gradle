package com.txc.conf;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * 根据JVM环境变量，读取不同的配置文件，创建相应实例。
 * </p>
 * 
 * @author <a href="mailto:shanyongwang@sohu-inc.com">Shanyong Wang</a>
 * @date 2013-9-11 下午2:56:16
 */
public class ConfigBean implements InitializingBean, FactoryBean<Object> {

    public static final String APP_ENV_KEY = "spring.profiles.active";

    private CompositeConfiguration configuration;

    private Configuration[] configurations;

    public ConfigBean() {
    }

    public ConfigBean(Configuration configuration) {
        this.configuration = new CompositeConfiguration(configuration);
    }

    public void afterPropertiesSet() {
        if (configurations != null && configurations.length >0) {
            if (configuration == null) {
                configuration = new CompositeConfiguration();
            }
            for (int i = 0; i < configurations.length; i++) {
                configuration.addConfiguration(configurations[i]);
            }
        } else {
            throw new IllegalArgumentException("no configuration object or location specified");
        }
    }

    public void setConfigurations(Configuration[] cfgs) {
        if (cfgs == null) {
            this.configurations = new Configuration[0];
        } else {
            this.configurations = Arrays.copyOf(cfgs, cfgs.length);
        }
    }

    public Object getObject() {
        return (configuration != null) ? ConfigurationConverter.getProperties(configuration) : null;
    }

    public Class<?> getObjectType() {
        return java.util.Properties.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public String getProperty(String key) {
        // TODO List属性的获取可能会有问题，有待改进
        return configuration.getString(key);
    }

    /**
     * 获取指定前缀的key集合
     * @param key
     * @return
     */
    public List<String> getKeys(String key){
        List<String> keys = new ArrayList<>();
        if(StringUtils.isEmpty(key)){
            return keys;
        }
        Iterator iterable = this.configuration.getKeys(key);
        if(iterable!=null){
            while (iterable.hasNext()){
                Object object = iterable.next();
                if(object instanceof String){
                    keys.add(String.valueOf(object));
                }
            }
        }
        return keys;
    }
}
