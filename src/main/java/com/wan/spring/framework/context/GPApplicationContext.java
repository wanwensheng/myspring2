package com.wan.spring.framework.context;

import com.wan.spring.framework.annotation.GPAutowired;
import com.wan.spring.framework.annotation.GPController;
import com.wan.spring.framework.annotation.GPService;
import com.wan.spring.framework.beans.GPBeanWrapper;
import com.wan.spring.framework.beans.config.GPBeanDefinition;
import com.wan.spring.framework.beans.support.GPBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPApplicationContext {


    private GPBeanDefinitionReader reader;

    private Map<String ,GPBeanDefinition> beanDefinitionMap = new HashMap<String,GPBeanDefinition>();

    private Map<String,Object> factoryBeanObjectCache = new HashMap<String,Object>();

    private Map<String,GPBeanWrapper> factoryBeanInstanceCache = new HashMap<String,GPBeanWrapper>();


    public GPApplicationContext(String ... configLocations) {

        try {
            //1、加载配置文件
            reader = new GPBeanDefinitionReader(configLocations);

            //2、由GPBeanDefinitionReader 生成GBBeanDefinition
            List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinition();

            //3、缓存GBBeanDefinition
            doRegistBeanDefinition(beanDefinitions);
            
            //4、Autowired
            doAutowired();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void doAutowired() {
        for (Map.Entry<String, GPBeanDefinition> stringGPBeanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName=stringGPBeanDefinitionEntry.getKey();
            getBean(beanName);
        }
    }
    //Bean的实例化，DI是从而这个方法开始的
    private Object getBean(String beanName) {
        //1.先拿到GPBeanDefinition
        GPBeanDefinition gpBeanDefinition = beanDefinitionMap.get(beanName);
        //2.反射实例化对象
        Object instance = instanceBean(gpBeanDefinition.getBenaClassName(),gpBeanDefinition);
        //3.封装成GPBeanWrapper对象
        GPBeanWrapper gpBeanWrapper = new GPBeanWrapper(instance);
        //4、保存到IoC容器
        this.factoryBeanInstanceCache.put(beanName,gpBeanWrapper);
        //5、执行依赖注入
        populateBean(beanName,gpBeanDefinition,gpBeanWrapper);

        return gpBeanWrapper.getWrapperInstance();
    }

    private void populateBean(String beanName, GPBeanDefinition gpBeanDefinition, GPBeanWrapper gpBeanWrapper) {
        //可能涉及到循环依赖？
        //A{ B b}
        //B{ A b}
        //用两个缓存，循环两次
        //1、把第一次读取结果为空的BeanDefinition存到第一个缓存
        //2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值
        Object wrapperInstance = gpBeanWrapper.getWrapperInstance();
        Class<?> wrapperClass = gpBeanWrapper.getWrapperClass();

        if(!(wrapperClass.isAnnotationPresent(GPController.class) || wrapperClass.isAnnotationPresent(GPService.class))){
            return;
        }
        //把所有的包括private/public/protected/default字段全部找出来
        for (Field field : wrapperClass.getFields()) {
            if(!field.isAnnotationPresent(GPAutowired.class))continue;
            GPAutowired annotation = field.getAnnotation(GPAutowired.class);

            //如果用户没有自定义的beanName，就默认根据类型注入
            String autowiredBeanName = annotation.value().trim();
            if("".equals(autowiredBeanName)){
                //field.getType().getName() 获取字段的类型
                autowiredBeanName = field.getType().getName();
            }
            //暴力访问
            field.setAccessible(true);

            try {
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){
                    continue;
                }
                field.set(wrapperInstance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }

    }


    private Object instanceBean(String benaClassName, GPBeanDefinition gpBeanDefinition) {
        Object o = null;
        try {
            if(this.factoryBeanObjectCache.containsKey(benaClassName)){
                o=factoryBeanObjectCache.get(benaClassName);
            }else{
                Class<?> aClass = Class.forName(benaClassName);
                o = aClass.newInstance();
                factoryBeanObjectCache.put(benaClassName,o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }


    private void doRegistBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception {
        for (GPBeanDefinition beanDefinition : beanDefinitions) {
            String factoryBeanName = beanDefinition.getFactoryBeanName();
            if(!this.beanDefinitionMap.containsKey(factoryBeanName)){
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + "is exists");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBenaClassName(),beanDefinition);
        }
    }
}
