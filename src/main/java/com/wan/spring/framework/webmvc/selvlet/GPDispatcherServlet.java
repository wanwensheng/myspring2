package com.wan.spring.framework.webmvc.selvlet;

import com.wan.spring.framework.annotation.GPController;
import com.wan.spring.framework.annotation.GPRequestMapping;
import com.wan.spring.framework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
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

    private Map<GPHandlerMapping,GPHandlerAdapter> handlerAdapters = new HashMap<GPHandlerMapping, GPHandlerAdapter>();

    private List<GPViewResoler> viewResolers = new ArrayList<GPViewResoler>();

    private GPApplicationContext context;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispath(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception ,Details\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]","").replaceAll(",\\s","\r\n"));
        }
    }

    private void doDispath(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1.从req 获取requestURL 去匹配对应的 HandlerMapping
        GPHandlerMapping handlerMapping =getHander(req);
        if(handlerMapping==null) {
            processDispachResult(req, resp, new GPModelAndView("404"));
        }
        //2.根据HandlerMapping  去获取对于的参数适配器HandlerAdapter
        GPHandlerAdapter gpHandlerAdapter = this.getHanderAdapter(handlerMapping);

        //3.真正调用方法，返回ModelAndView 存储了要传页面的上的值，和页面模板名称
        GPModelAndView gpModelAndView =gpHandlerAdapter.handle(req,resp,handlerMapping);

        //4.真正的页面输出
        processDispachResult(req,resp,gpModelAndView);


    }

    private GPHandlerAdapter getHanderAdapter(GPHandlerMapping handlerMapping) {
        if(this.handlerAdapters.isEmpty()){return null;}
        GPHandlerAdapter gpHandlerAdapter = this.handlerAdapters.get(handlerMapping);
        if(gpHandlerAdapter.supports(handlerMapping)){
            return gpHandlerAdapter;
        }
        return null;
    }

    private void processDispachResult(HttpServletRequest req, HttpServletResponse resp, GPModelAndView modelAndView) throws Exception {
        if(modelAndView==null){return;}
        if(this.viewResolers.isEmpty()){return;}
        for (GPViewResoler viewResoler : this.viewResolers) {
           GPView view =  viewResoler.resoleViewName(modelAndView.getViewName());
           view.render(req,resp,modelAndView.getModel());
        }
    }

    private GPHandlerMapping getHander(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){return null;}
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
         requestURI = requestURI.replace(contextPath, "").replaceAll("/+", "/");
        for (GPHandlerMapping handlerMapping : handlerMappings) {
            if(handlerMapping.getPattern().matcher(requestURI).matches()){
                return handlerMapping;
            }
            continue;
        }

        return null;
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
        //拿到模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        File[] templates = templateRootDir.listFiles();
        for(int i=0;i<templates.length;i++){
            //这里主要是为了兼容多模板，所有模仿Spring用List保存
            //在我写的代码中简化了，其实只有需要一个模板就可以搞定
            //只是为了仿真，所有还是搞了一个list
            this.viewResolers.add(new GPViewResoler(templateRoot));
            break;
        }
    }

    private void initHandlerAdapter(GPApplicationContext context) {
        //把一个request 请求变成一个 handler,参数都是字符串，自动配到handler中的形参
        //可想而知，他要拿到HandlerMapping才能干活
        //就意味着，有几个HandlerMapping 就有几个HanderAdapter
        for (GPHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping,new GPHandlerAdapter());
        }



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
