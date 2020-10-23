package com.wan.spring.framework.webmvc.selvlet;

import com.wan.spring.framework.annotation.GPController;
import com.wan.spring.framework.annotation.GPRequestMapping;
import com.wan.spring.framework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: WanWenSheng
 * @Description:
 * @Dete: Created in 16:06 2020/10/23
 * @Modified By:
 */
public class GPDispatcherServlet extends HttpServlet {

    private final String CONTEXT_CONFIG_LOCATION="contextConfigLocation";

    private List<GPHandlerMapping> handlerMappings = new ArrayList<GPHandlerMapping>();

    private GPApplicationContext context;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、初始化 ApplicationContext
        context = new GPApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));
        //2、初始化九大组件
        initStrategies(context);

    }

    private void initStrategies(GPApplicationContext context) {
        //1、HandlerMapping必须初始化
        initHandlerMapping(context);
        //2、初始化参数适配器必须实现
        initHandlerAdapter(context);
        //3.初始化视图转换器，必须实现
        initViewResolvers(context);
    }

    private void initViewResolvers(GPApplicationContext context) {
    }

    private void initHandlerAdapter(GPApplicationContext context) {
    }

    private void initHandlerMapping(GPApplicationContext context) {

        String[] beanNames = context.getBeanDefinitionNames();
        try {
                for (String beanName:beanNames){
                    Object controller = context.getBean(beanName);
                    Class<?> aClass = controller.getClass();
                    if(!aClass.isAnnotationPresent(GPController.class)){continue;}

                    String beseUrl="";
                    //获取Controller的Url位置
                    if(aClass.isAnnotationPresent(GPRequestMapping.class)){
                        GPRequestMapping annotation = aClass.getAnnotation(GPRequestMapping.class);
                        beseUrl = annotation.value();
                    }

                    //获取Method URl
                    Method[] methods = aClass.getMethods();
                    for (Method method : methods){
                        if(!method.isAnnotationPresent(GPRequestMapping.class)){continue;}
                        GPRequestMapping annotation = method.getAnnotation(GPRequestMapping.class);
                        String value = annotation.value();
                        String methodUrl = beseUrl+value;
                        String regex = ("/" + beseUrl + "/" + value.replaceAll("\\*", ".*")).replaceAll("/+", "/");
                        Pattern compile = Pattern.compile(regex);
                        this.handlerMappings.add(new GPHandlerMapping(controller,method,compile));
                    }

                }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
