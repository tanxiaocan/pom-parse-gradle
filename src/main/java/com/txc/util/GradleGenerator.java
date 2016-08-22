package com.txc.util;

import com.sun.xml.internal.fastinfoset.tools.FI_DOM_Or_XML_DOM_SAX_SAXEvent;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.security.auth.login.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by tanxiaocan on 2016/8/21.
 */
public class GradleGenerator {
    Map<String,String> cacheMap;
    Map<String,String> dependencyMap = new HashMap<>();
    Map<String,List<String>> exclusionMap = new HashMap<>();//groupId和artifactId为键，值为group:'groupId',module:'artifactId'
    public Map<String, String> getCacheMap() {
        return cacheMap;
    }

    public void setCacheMap(Map<String, String> cacheMap) {
        this.cacheMap = cacheMap;
    }

    public String generatorGradleBuildFile(String filePath){
        try {
            XMLConfiguration xmlConfiguration = new XMLConfiguration(filePath);
            Document document = xmlConfiguration.getDocument();
            Element root = document.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("dependencies");
            for(int i=0;i<nodeList.getLength();i++){
                Node node = nodeList.item(i);
                if("dependencies".equals(node.getNodeName())){
                    NodeList childNodeList = node.getChildNodes();
                    for(int j=0;j<childNodeList.getLength();j++){
                        Node childNode = childNodeList.item(j);
                        if("dependency".equals(childNode.getNodeName()))
                        parseDependency(childNode);
                    }

                }
            }
            return outPutBuildGradle(filePath);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return "文件不存在:" + filePath;
        }
    }

    private String outPutBuildGradle(String filePath) {
        String dirName = filePath.substring(0,filePath.lastIndexOf("/")+1);
        String gradleBuildName = dirName + "build.gradle";
        StringBuffer text = new StringBuffer();
        text.append(getPrefix());
        text.append(getDependencyList());
        text.append(getLastTex());
        return text.toString();
    }

    private String getDependencyList(){
        StringBuffer dependencies = new StringBuffer("\ndependencies {\n");
        int count = 0;
        for(String key:dependencyMap.keySet()){
            String dependency = getDependency(key);
            dependencies.append(dependency);
            count++;
            if(count!=dependencyMap.size()){
                dependencies.append("\n");
            }
        }
        dependencies.append("\n}");
        return dependencies.toString();
    }

    private String getDependency(String key){
        StringBuffer dependency = new StringBuffer("compile ");
        String dependencyText = dependencyMap.get(key);
        List<String> exclusionList = exclusionMap.get(key);
        if(CollectionUtils.isNotEmpty(exclusionList)){
            dependency.append("('" + dependencyText + "')");
            dependency.append("{\n");
            for (int i = 0; i < exclusionList.size(); i++) {
                dependency.append(exclusionList.get(i));
                if(i!=exclusionList.size()-1){
                    dependency.append("\n");
                }
            }
            dependency.append("\n}");
        }else {
            dependency.append("'" + dependencyText + "'");
        }
        return dependency.toString();
    }

    private String getPrefix(){
        String prefix = "import cn.focus.gradle.plugins.VersionedPlugin\n" +
                "import cn.focus.gradle.plugins.ScePlugin\n" +
                "\n" +
                "group 'cn.focus'\n" +
                "\n" +
                "apply plugin: 'war'\n" +
                "apply plugin: VersionedPlugin\n" +
                "apply plugin: ScePlugin\n" +
                "apply plugin: 'java'\n" +
                "apply plugin: 'groovy'\n" +
                "\n" +
                "compileJava {\n" +
                "    options.encoding = \"UTF-8\"\n" +
                "}";
        prefix += "\nrepositories {\n" +
                "    maven {\n" +
                "        url \"http://nexus.inner.focus.cn/nexus/content/groups/public/\"\n" +
                "    }\n" +
                "    mavenLocal()\n" +
                "    mavenCentral()\n" +
                "}";
        return prefix;
    }

    private String getLastTex(){
        return "\nconfigurations.all {\n" +
                "    resolutionStrategy.cacheDynamicVersionsFor 1, 'minutes'\n" +
                "    resolutionStrategy.cacheChangingModulesFor 0, 'seconds'\n" +
                "}\n" +
                "\n" +
                "staticConfigs {\n" +
                "    configs {\n" +
                "        first {\n" +
                "            testPrefix = 'http://s.m.focus.cn/usercenter'\n" +
                "            subTopDir = '/uc'\n" +
                "            assetsRelative = ''\n" +
                "            gitUrl = 'git@code.sohuno.com:focus-fe/focus-wap-usercenter-static.git'\n" +
                "            gitBranch = 'online'\n" +
                "            trimStart = '/'\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "buildscript {\n" +
                "    repositories {\n" +
                "        maven {\n" +
                "            url \"http://nexus.inner.focus.cn/nexus/content/groups/public/\"\n" +
                "        }\n" +
                "        mavenLocal()\n" +
                "        mavenCentral()\n" +
                "    }\n" +
                "    dependencies {\n" +
                "        classpath(group: 'cn.focus', name: 'gradle-plugin', version: '1.0-domeos-static-SNAPSHOT') {\n" +
                "            changing: true\n" +
                "        }\n" +
                "    }\n" +
                "    configurations.all {\n" +
                "        resolutionStrategy.cacheDynamicVersionsFor 1, 'minutes'\n" +
                "        resolutionStrategy.cacheChangingModulesFor 0, 'seconds'\n" +
                "    }\n" +
                "}";
    }

    private void parseDependency(Node node){
        String groupId = "";
        String artifactId = "";
        String version = "";
        NodeList nodeList = node.getChildNodes();
        for(int i=0;i<nodeList.getLength();i++){
            Node childNode = nodeList.item(i);
            if("groupId".equals(childNode.getNodeName())){
                groupId = childNode.getTextContent();
            }
            if("artifactId".equals(childNode.getNodeName())){
                artifactId = childNode.getTextContent();
            }
            if("version".equals(childNode.getNodeName())){
                version = childNode.getTextContent();
            }
            if("exclusions".equals(childNode.getNodeName())){
                parseExclusions(childNode,groupId,artifactId);
            }
        }
        if(!StringUtils.hasText(version)){
            version = cacheMap.get(groupId + ":" + artifactId);
        }
        if(StringUtils.hasText(groupId)&&StringUtils.hasText(artifactId))
        dependencyMap.put(groupId + ":" + artifactId,groupId + ":" + artifactId + ":" + version);
    }

    private void parseExclusions(Node node,String groupId,String artifactId){
        NodeList nodeList = node.getChildNodes();
        List<String> exclusions = new ArrayList<>();
        for(int i=0;i<nodeList.getLength();i++){
            Node childNode = nodeList.item(i);
            if("exclusion".equals(childNode.getNodeName())){
                String exclusionGroupId = "";
                String exclusionArtifactId = "";
                NodeList childNodeList = childNode.getChildNodes();
                for(int j=0;j<childNodeList.getLength();j++){
                    Node parentChildNode = childNodeList.item(j);
                    if("groupId".equals(parentChildNode.getNodeName())){
                        exclusionGroupId = parentChildNode.getTextContent();
                    }
                    if("artifactId".equals(parentChildNode.getNodeName())){
                        exclusionArtifactId = parentChildNode.getTextContent();
                    }
                }
                String exclusionValue = buildExclusionValue(exclusionGroupId,exclusionArtifactId);
                if(StringUtils.hasText(exclusionValue))
                    exclusions.add(exclusionValue);
            }
        }exclusionMap.put(groupId + ":" + artifactId,exclusions);
    }

    private String buildExclusionValue(String exclusionGroupId,String exclusionArtifactId){
        StringBuffer exclusionValue = new StringBuffer();
        exclusionValue.append("exclude ");
        if(StringUtils.hasText(exclusionGroupId)){
            exclusionValue.append("group:").append("'" + exclusionGroupId + "'");
        }
        if(StringUtils.hasText(exclusionArtifactId)){
            if(StringUtils.hasText(exclusionGroupId)){
                exclusionValue.append(",");
            }
            exclusionValue.append("module:").append("'" + exclusionArtifactId + "'");
        }
        return exclusionValue.toString();
    }
}
