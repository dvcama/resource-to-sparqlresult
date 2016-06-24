package org.dvcama.lodview.proxy.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;

@Controller
@RequestMapping(value = { "sparqlProxy" })
public class SparqlProxy implements ApplicationContextAware, ServletContextAware {
	String errorMessage = "";

	ApplicationContext applicationContext;
	ServletContext servletContext;
 

	@RequestMapping(value = "error.html")
	public String rdf(ModelMap model) throws UnsupportedEncodingException, TransformerException {
		model.addAttribute("errorMsg", errorMessage);
		return "error";
	}

	@RequestMapping(value = "")
	public String proxy(ModelMap model, HttpServletRequest req, HttpServletResponse res, @RequestParam String query, @RequestParam String endpoint, @RequestParam String callback) throws UnsupportedEncodingException, TransformerException {
		try {
			Map<String, Object> resultMap = org.dvcama.lodview.utils.PostForRDF(endpoint+"?query="+java.net.URLEncoder.encode(query,"UTF-8"),"application/sparql-results+json");
			@SuppressWarnings("unchecked")
			Map<String, List<String>> hf = (Map<String, List<String>>) resultMap.get("headerFields");

			for (String field : hf.keySet()) {
				if (field != null) {
					try {
						for (int i = 0; i < hf.get(field).size(); i++) {
							// 	do we want the original headers?
							//	res.addHeader(field, hf.get(field).get(i));
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
			res.addHeader("Via", "rdfSparqlProxy");
			model.addAttribute("result", callback + "(" + resultMap.get("result") + ")");
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(400);
			model.addAttribute("errorMsg", e.getMessage());
			return "error";
		}
		return "json";
	}

	public void setServletContext(ServletContext arg0) {
		// TODO Auto-generated method stub

	}

	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		// TODO Auto-generated method stub

	}

}
