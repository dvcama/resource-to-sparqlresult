package org.dvcama.lodview.proxy.controller;

import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.xml.transform.TransformerException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

@Controller
@RequestMapping(value = { "" })
public class Common implements ApplicationContextAware, ServletContextAware {
	String errorMessage = "";

	ApplicationContext applicationContext;
	ServletContext servletContext;
 

	@RequestMapping(value = "error.html")
	public String rdf(ModelMap model) throws UnsupportedEncodingException, TransformerException {
		String errorMsg = "sorry, an error occured";
		model.addAttribute("errorMsg", errorMsg);
		return "error";
	}

 

	public void setServletContext(ServletContext arg0) {
		// TODO Auto-generated method stub

	}

	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		// TODO Auto-generated method stub

	}

}
