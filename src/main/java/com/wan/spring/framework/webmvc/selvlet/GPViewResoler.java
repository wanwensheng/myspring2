package com.wan.spring.framework.webmvc.selvlet;

import java.io.File;
import java.util.Locale;

public class GPViewResoler {
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";
    private File tempateRootDir;
    public GPViewResoler(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        tempateRootDir = new File(templateRootPath);
    }

    public GPView resoleViewName(String viewName){
        if(null == viewName || "".equals(viewName.trim())){return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((tempateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
        return new GPView(templateFile);
    }
}
