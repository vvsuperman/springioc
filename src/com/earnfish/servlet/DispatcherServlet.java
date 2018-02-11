package com.earnfish.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.earnfish.annotion.Controller;
import com.earnfish.annotion.Qualifier;
import com.earnfish.annotion.RequestMapping;
import com.earnfish.annotion.Service;
import com.earnfish.controller.FishController;

/**
 * Servlet implementation class DispatcherServlet
 */
@WebServlet("/")
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private List<String> classNames = new ArrayList<String>();
	
	private Map<String, Object> instanceMap = new HashMap<String,Object>();
	
	private Map<String, Method> methodMap = new HashMap<String,Method>();

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DispatcherServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	   /**找到bean*/
    	   scanBase("com.earnfish");	
    	   try {
    		    /**生成并注册bean*/
			filterAndInstance();
	    	    /**注入bean*/
			springDi();
			/**获取url对应的method*/
			mvc();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void mvc() {
	    	if(instanceMap.size() == 0) {
	    	    return;
	      }
	    	 /** 循环获取实例*/
	      for(Map.Entry<String, Object> entry: instanceMap.entrySet()) {
	    	       /** 含有Controller注解*/
	    	      if(entry.getValue().getClass().isAnnotationPresent(Controller.class)) {
	    	    	     String ctlUrl = ((Controller)entry.getValue().getClass().getAnnotation(Controller.class)).value();
	    	    	     Method[] methods = entry.getValue().getClass().getMethods();
	    	    	     for(Method method :methods) {
	    	    	    	    /** 含有RequestMapping注解*/
	    	    	    	 	if(method.isAnnotationPresent(RequestMapping.class)) {
	    	    	    	 		String reqUrl = ((RequestMapping)method.getAnnotation(RequestMapping.class)).value();
	    	    	    	 		String dispatchUrl = "/"+ ctlUrl +"/" + reqUrl;
	    	    	    	 		methodMap.put(dispatchUrl, method);
	    	    	    	 	}
	    	    	     }
	    	      }else {
	    	    	  	continue;
	    	      }
	      }
	}
    /*
     * 注入bean
     * */
	private void springDi() throws IllegalArgumentException, IllegalAccessException {
    	
      if(instanceMap.size() == 0) {
    	    return;
      }
      /**
       * 循环获取实例
       * */
      for(Map.Entry<String, Object> entry: instanceMap.entrySet()) {
    	  	  /**获取所有的类变量*/
    	      Field[] fields = entry.getValue().getClass().getDeclaredFields();
    	      for(Field  field: fields) {
    	    	    /**包含Qualifer注解*/
    	    	  	if(field.isAnnotationPresent(Qualifier.class)){
    	    	  		String key = ((Qualifier)field.getAnnotation(Qualifier.class)).value();
    	    	  		field.setAccessible(true);
    	    	  		field.set(entry.getValue(), instanceMap.get(key));
    	    	  	}//autowired
    	      }
      }
		
		
	}

	/**
	 * 生成并注册bean
	 * */
    private void filterAndInstance() throws Exception {
    	    if( classNames.size() == 0) {
    	    	   return;
    	    }
    	    /**循环获取类名*/
    	    for(String className : classNames) {
    	    	   Class clazz = Class.forName(className.replace(".class", ""));
    	    	   if(clazz.isAnnotationPresent(Controller.class)) {
    	    		   /**获取bean实例*/
    	    		   Object instance = clazz.newInstance();
    	    		   /**获取注解的value*/
    	    		   String key = ((Controller)clazz.getAnnotation(Controller.class)).value();
    	    		   /**将bean交付给IOC*/
    	    		   instanceMap.put(key, instance);
    	    	   }else if(clazz.isAnnotationPresent(Service.class)) {
    	    		   /**获取bean实例*/
    	    		   Object instance = clazz.newInstance();
    	    		   /**获取注解的value*/
    	    		   String key = ((Service)clazz.getAnnotation(Service.class)).value();
    	    		   /**将bean交付给IOC*/
    	    		   instanceMap.put(key, instance);
    	    	   }else {
    	    		   continue;
    	    	   }
    	    }
    }
    
    
    /**
     * 找到类
     * */
	private void scanBase(String basePackages) {
		URL url = this.getClass().getClassLoader().getResource("/" + replacePath(basePackages));
		String path = url.getFile();
		File file = new File(path);
		String [] strFiles = file.list();
		for(String strFile: strFiles) {
			File eachFile = new File(path + strFile);
			if(eachFile.isDirectory()) {
				scanBase(basePackages +"."+eachFile.getName());
			}else {
				System.out.println("class name" + eachFile.getName());
				classNames.add(basePackages +"." + eachFile.getName());
			}
		}
	}
	
	
	String replacePath(String path) {
		return path.replaceAll("\\.","/");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/**  springioc/fish/get  */
		String uri = request.getRequestURI();
		String projetname = request.getContextPath();
		String path = uri.replaceAll(projetname, "");
		
		Method method = methodMap.get(path);
		
		String className = uri.split("/")[2];
		FishController fishController = (FishController)instanceMap.get(className);
		
		try {
			method.invoke(fishController, new Object[] {request,response,null});
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
