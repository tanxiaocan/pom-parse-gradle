package com.txc.conf;

import com.txc.constrants.CacheKey;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import java.util.*;

/**
 * Created by tanxiaocan on 2016/8/21.
 */
public class ConfigBeanSupport implements InitializingBean,FactoryBean<Object> {
    Properties configBean;
    private Map<String,String> cacheMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(cacheMap==null){
            cacheMap = new HashMap<>();
        }
        String groupIds[] = configBean.getProperty(CacheKey.ALL_DEPENDENCY_GROUPID_KEY).split(",");
        String artifactIds[] = configBean.getProperty(CacheKey.ALL_DEPENDENCY_ARTIFACTID_KEY).split(",");
        String versions[] = configBean.getProperty(CacheKey.ALL_DEPENDENCY_VERSION_KEY).split(",");
//        List<String> keys = configBean.getKeys("properties");
        List<String> realVersions = new ArrayList<>();
        for(String version:versions){
            version = version.substring(2,version.length()-1);
            String realVersion = configBean.getProperty("properties." + version);
            realVersions.add(realVersion);
        }
        for(int i=0;i<groupIds.length;i++){
            cacheMap.put(groupIds[i] + ":" + artifactIds[i],realVersions.get(i));
        }
    }

    public Map<String,String> getCacheMap(){
        return this.cacheMap;
    }
    public void setCacheMap(Map<String,String> cacheMap){
        this.cacheMap = cacheMap;
    }

    public Properties getConfigBean() {
        return configBean;
    }

    public void setConfigBean(Properties configBean) {
        this.configBean = configBean;
    }

    @Override
    public Object getObject() throws Exception {
        return cacheMap!=null?cacheMap:null;
    }

    @Override
    public Class<?> getObjectType() {
        return java.util.Properties.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
