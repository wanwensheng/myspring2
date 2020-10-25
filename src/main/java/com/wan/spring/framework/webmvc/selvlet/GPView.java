package com.wan.spring.framework.webmvc.selvlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.RandomAccess;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPView {

    private File viewFile;

    public GPView(File templateFle) {
        this.viewFile = templateFle;
    }

    public File getViewFile() {
        return viewFile;
    }

    public void setViewFile(File viewFile) {
        this.viewFile = viewFile;
    }

    public void render(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> model) throws Exception {
        StringBuffer str = new StringBuffer();

        RandomAccessFile ra = new RandomAccessFile(this.viewFile,"r");
        String line=null;
        while(null!=(line=ra.readLine())){
            line = new String(line.getBytes("ISO-8859-1"),"utf-8");
            Pattern compile = Pattern.compile("￥\\{[^}]+\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = compile.matcher(line);
            while (matcher.find()){
                String paraName = matcher.group();
                paraName = paraName.replaceAll("￥\\{|\\}", "");
                Object o = model.get(paraName);
                if(o==null){continue;}
                line = matcher.replaceFirst(makeStringForRegExp(o.toString()));
                matcher = compile.matcher(line);
            }
            str.append(line);
        }
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(str.toString());
    }

    //处理特殊字符
    public static String makeStringForRegExp(String str){
        return str.replace("\\","\\\\").replace("*","\\*")
                .replace("+","\\+").replace("|","\\|")
                .replace("{","\\{").replace("}","\\}")
                .replace("(","\\(").replace(")","\\)")
                .replace("^","\\^").replace("$","\\$")
                .replace("[","\\[").replace("]","\\]")
                .replace("?","\\?").replace(",","\\,")
                .replace(".","\\.").replace("&","\\&");
    }
}
