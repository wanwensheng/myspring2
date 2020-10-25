package com.wan.dome.action;


import com.wan.dome.service.IQueryService;
import com.wan.spring.framework.annotation.GPAutowired;
import com.wan.spring.framework.annotation.GPController;
import com.wan.spring.framework.annotation.GPRequestMapping;
import com.wan.spring.framework.annotation.GPRequestParam;
import com.wan.spring.framework.webmvc.selvlet.GPModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@GPController
@GPRequestMapping("/")
public class PageAction {

    @GPAutowired
    IQueryService queryService;

    @GPRequestMapping("/first.html")
    public GPModelAndView query(@GPRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new GPModelAndView("first.html",model);
    }

}
