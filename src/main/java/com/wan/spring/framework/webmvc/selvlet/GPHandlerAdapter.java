package com.wan.spring.framework.webmvc.selvlet;

import com.wan.spring.framework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: WanWenSheng
 * @Description:
 * @Dete: Created in 16:04 2020/10/23
 * @Modified By: 请求方法的适配器
 */
public class GPHandlerAdapter {
    public boolean supports(Object handlerMapping) {
        return handlerMapping instanceof GPHandlerMapping;
    }

    public GPModelAndView handle(HttpServletRequest req, HttpServletResponse resp, GPHandlerMapping handlerMapping) throws Exception {


        //把方法的形参列表和requets 参数列表 按照顺序一一对应
        Map<String,Integer> paramIndexMapping = new HashMap<String,Integer>();
        //提取方法上加了注解的参数
        //把方法上的注解拿到，得到一个二维数组
        //因为一个方法有多个参数，一个参数可以多个注解
        Annotation[][] parameterAnnotations = handlerMapping.getMethod().getParameterAnnotations();
        for (int i=0;i<parameterAnnotations.length;i++){
            for (Annotation annotation : parameterAnnotations[i]) {
                if(annotation instanceof GPRequestParam){
                    String value = ((GPRequestParam) annotation).value();
                    if(!"".equals(value.trim())){
                        paramIndexMapping.put(value,i);
                    }
                }

            }
        }
        //获取request请求参数和response参数
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
        for(int j=0;j<parameterTypes.length;j++){
            Class<?> parameterType = parameterTypes[j];
            if(parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class){
                paramIndexMapping.put(parameterTypes[j].getName(),j);
            }
        }


        //获取request请求参数
        Map<String,String[]> parameterMap = req.getParameterMap();

        Object[] paramValue = new Object[parameterAnnotations.length];
        for (Map.Entry<String, String[]> s : parameterMap.entrySet()) {
            String value = Arrays.toString(parameterMap.get(s.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");
            if(!paramIndexMapping.containsKey(s.getKey())){continue;}
            Integer integer = paramIndexMapping.get(s.getKey());
            paramValue[integer]=caseStringValue(value,parameterTypes[integer]);
        }
        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            Integer integer = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValue[integer]=req;
        }
        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            Integer integer = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValue[integer]=resp;
        }
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValue);
        if(result==null||result instanceof Void){return null;}
        Boolean isModelAndView =handlerMapping.getMethod().getReturnType()==GPModelAndView.class;

        //这里只对一种返回类型进行处理
        if(isModelAndView){
            return (GPModelAndView)result;
        }
        return null;
    }

    private Object caseStringValue(String value,Class<?> classType){
        if(String.class==classType){
            return value;
        }else if(Integer.class==classType){
            return Integer.valueOf(value);
        }else {
            return null;
            //如果还有double或者其他类型，继续加if
            //这时候，我们应该想到策略模
        }
    }
}
