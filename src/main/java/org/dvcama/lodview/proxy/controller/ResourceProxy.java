package org.dvcama.lodview.proxy.controller;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.dvcama.lodview.SimpleGraph;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

@Controller
@RequestMapping(value = { "resource" })
public class ResourceProxy implements ApplicationContextAware, ServletContextAware {
	String errorMessage = "";

	ApplicationContext applicationContext;
	ServletContext servletContext;

	@RequestMapping(value = "")
	public String proxy(ModelMap model, HttpServletRequest req, HttpServletResponse res)
			throws UnsupportedEncodingException, TransformerException {
		String uri = req.getParameter("uri");
		String query = req.getParameter("query");
		String callback = req.getParameter("callback");

		try {

			SimpleGraph g = new SimpleGraph(true);
			try {
				g.addURI(uri);
			} catch (Exception e) {
				g.addURI(uri, RDFFormat.NTRIPLES);
			}

			OutputStream stream = new ByteArrayOutputStream();
			g.runJsonSPARQL(java.net.URLDecoder.decode(query, "UTF-8"), stream);
			String result = stream.toString();
			result = result.replaceAll("\\\\/", "/");
			// System.out.println(result);
			model.addAttribute("result", callback + "(" + result + ")");

			res.addHeader("Via", "corsResourceProxy");
			res.setHeader("Content-Type", "application/json");
			res.addHeader("Access-Control-Allow-Origin", "*");
			// model.addAttribute("result", resultMap.get("result"));
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = "risorsa non disponibile";
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
