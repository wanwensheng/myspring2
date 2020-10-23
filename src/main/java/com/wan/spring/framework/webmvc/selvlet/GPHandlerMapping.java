package com.wan.spring.framework.webmvc.selvlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @Author: WanWenSheng
 * @Description:
 * @Dete: Created in 16:01 2020/10/23
 * @Modified By:
 */
public class GPHandlerMapping {

    private Object controller; //保存对应方法的实例

    private Method method;//保存映射的方法

    private Pattern pattern;//URL 正则表达式匹配

    public GPHandlerMapping(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
