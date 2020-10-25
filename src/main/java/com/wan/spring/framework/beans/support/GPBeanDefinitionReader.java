package com.wan.spring.framework.beans.support;

import com.wan.spring.framework.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GPBeanDefinitionReader {

    private Properties properties = new Properties();
    //保存所有扫描到的类
    private List<String> regitryBeanClasses = new ArrayList<>();


    public GPBeanDefinitionReader(String ... configLocations) {
        doLoadConfig(configLocations[0]);
        
        //扫描配置下所有的类
        doSnner(properties.getProperty("scanPackage"));

    }


    public List<GPBeanDefinition> loadBeanDefinition(){
        if(regitryBeanClasses.isEmpty())return new ArrayList<GPBeanDefinition>();
        List<GPBeanDefinition> beanDefinitions = new ArrayList<GPBeanDefinition>();
        for(String className:regitryBeanClasses){
            try {
                Class<?> aClass = this.getClass().getClassLoader().loadClass(className);
                if(aClass.isInterface()){continue;}
                String name = aClass.getName();
                String simpleName = aClass.getSimpleName();
                //保存类对应的ClassName（全类名）
                //还有beanName
                //1、默认是类名首字母小写
                beanDefinitions.add(doCreateBeanDefinition(toLowerFirstCase(simpleName),name));
                //2、自定义
                //3、接口注入
                for (Class<?> i : aClass.getInterfaces()) {
                    beanDefinitions.add(doCreateBeanDefinition(i.getName(),aClass.getName()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return beanDefinitions;
    }

    private GPBeanDefinition doCreateBeanDefinition(String simpleName, String className) {
        GPBeanDefinition gpBeanDefinition = new GPBeanDefinition();
        gpBeanDefinition.setFactoryBeanName(simpleName);
        gpBeanDefinition.setBenaClassName(className);
        return gpBeanDefinition;
    }


    private void doSnner(String scanPackage) {
        //jar 、war、zip、rar
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File file = new File(url.getFile());
        for (File f : file.listFiles()){
            if(f.isDirectory()){
                doSnner(scanPackage+"."+f.getName());
            }else{
                if(!f.getName().endsWith(".class"))continue;
                String className = scanPackage+"."+f.getName().replaceAll(".class","");
                regitryBeanClasses.add(className);
            }
        }
    }

    private void doLoadConfig(String configLocation) {
        System.out.println(this.getClass().getClassLoader().getResource(""));
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(configLocation.replaceAll("classpath:",""));
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //自己写，自己用
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
//        if(chars[0] > )
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return properties;
    }
}
