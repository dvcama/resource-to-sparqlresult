package org.dvcama.lodview.proxy.controller;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
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
@RequestMapping(value = { "/endpoint" })
public class SimpleEndpoint implements ApplicationContextAware, ServletContextAware {
	String errorMessage = "";

	ApplicationContext applicationContext;
	ServletContext servletContext; 

	@RequestMapping(value = "/sparql")
	public String query(ModelMap model, HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException, TransformerException {
		SimpleGraph g = null;
		String query = req.getParameter("query");
		String callback = req.getParameter("callback");
		String id = StringUtils.substringBetween(query, "lodlive.it_", "?");
		//System.out.println("id---Z> " + id);
		HttpSession session = req.getSession().getSessionContext().getSession(id);

		if (session.getAttribute("currentEndpoint") != null) {
			g = (SimpleGraph) session.getAttribute("currentEndpoint");
		} else {
			errorMessage = "risorsa non disponibile";
			res.setStatus(400);
			model.addAttribute("errorMsg", errorMessage);
			return "error";
		}

		System.out.println("query " + java.net.URLDecoder.decode(query, "UTF-8"));
		OutputStream stream = new ByteArrayOutputStream();
		g.runJsonSPARQL(java.net.URLDecoder.decode(query, "UTF-8"), stream);
		String result = stream.toString();
		result = result.replaceAll("\\\\/", "/");
		//System.out.println(result);

		model.addAttribute("result", callback + "(" + result + ")");

		res.addHeader("Via", "corsResourceProxy");
		res.setHeader("Content-Type", "application/json");
		res.addHeader("Access-Control-Allow-Origin", "*");

		return "json";
	}

	@RequestMapping(value = "")
	public String rdf(ModelMap model, HttpServletRequest req) throws UnsupportedEncodingException, TransformerException {
	//	System.out.println(req.getSession().getId());
		String data = req.getParameter("data");
		String prefixes = req.getParameter("prefixes");
		String resource = req.getParameter("resource");
		if (data == null || prefixes == null || data.equals("") || prefixes.equals("")) {
			errorMessage = "you have to provide some data and one or more prefixes of your resources<br >use \"data\" and \"prefixes\" params";
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		}

		String id = req.getSession().getId();
		if (req.getParameter("clear") != null && req.getParameter("clear").equals("true")) {
			req.getSession().removeAttribute("currentEndpoint");
		}

		String[] prfixesList = prefixes.split(";");
		for (int i = 0; i < prfixesList.length; i++) {
			data = data.replaceAll("^<(" + prfixesList[i] + "[^>]*)>", "<http://lodlive.it_" + id + "?$1>");
			data = data.replaceAll("<(" + prfixesList[i] + "[^>]*)$", "<http://lodlive.it_" + id + "?$1");
			data = data.replaceAll("resource=\"(" + prfixesList[i] + "[^\"]*\")>", "resource=\"http://lodlive.it_" + id + "?$1");
			data = data.replaceAll("about=\"(" + prfixesList[i] + "[^\"]*\")>", "about=\"http://lodlive.it_" + id + "?$1");
			data = data.replaceAll("base=\"(" + prfixesList[i] + "[^\"]*\")>", "base=\"http://lodlive.it_" + id + "?$1");
		}
		//System.out.println(data);
		SimpleGraph g = null;
		if (req.getSession().getAttribute("currentEndpoint") != null) {
			g = (SimpleGraph) req.getSession().getAttribute("currentEndpoint");
		} else {
			g = new SimpleGraph(true);
		}
		try {
			g.addString(data, RDFFormat.RDFXML);
		} catch (Exception e) {
			System.out.println("NOT VALID RDF/XML");

		}
		try {
			g.addString(data, RDFFormat.NTRIPLES);
		} catch (Exception e) {
			System.out.println("NOT VALID NTRIPLES");
		}

		req.getSession().setAttribute("currentEndpoint", g);

		model.addAttribute("resource", resource);
		g.dumpRDF(System.out, RDFFormat.NTRIPLES);
		return "redirect";
	}

	public void setServletContext(ServletContext arg0) {
		// TODO Auto-generated method stub

	}

	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		// TODO Auto-generated method stub

	}

}
