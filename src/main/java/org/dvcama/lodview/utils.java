package org.dvcama.lodview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.openrdf.rio.RDFFormat;

public class utils {
	public static void main(String[] args) throws MalformedURLException, IOException, NoSuchAlgorithmException, KeyManagementException {
		System.out.println(PostForJson("select * where {?f ?H ?J} LIMIT 10", "http://worldbank.270a.info/sparql").get("result"));
		// System.out.println(RDFFormat.RDFXML.getDefaultMIMEType());
		// System.out.println(PostForRDF("http://rdf.freebase.com/ns/en.parliament_of_italy"));

	}

	public static Map<String, Object> PostForJson(String query, String endpoint) throws MalformedURLException, IOException, NoSuchAlgorithmException, KeyManagementException {

		/*
		 * fix for Exception in thread "main"
		 * javax.net.ssl.SSLHandshakeException:
		 * sun.security.validator.ValidatorException: PKIX path building failed:
		 * sun.security.provider.certpath.SunCertPathBuilderException: unable to
		 * find valid certification path to requested target
		 */
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		/*
		 * end of the fix
		 */

		HttpGet httppost = new HttpGet(endpoint + "?query=" + URLEncoder.encode(URLDecoder.decode(query, "UTF-8"), "UTF-8"));
		HttpClient httpclient = new DefaultHttpClient();
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("query", URLDecoder.decode(query, "UTF-8")));
		nameValuePairs.add(new BasicNameValuePair("output", "json"));
		httppost.setHeader("Accept-Charset", "utf-8");
		httppost.setHeader("Accept", "application/sparql-results+json");
		// Execute HTTP Post Request URLDecoder.decode(query, "UTF-8")
		// System.out.println(httppost.getRequestLine());
		HttpResponse response = httpclient.execute(httppost);
		String httpResult = EntityUtils.toString(response.getEntity());
		// System.out.println(httpResult);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", httpResult);
		Map<String, List<String>> hf = new HashMap<String, List<String>>();

		for (Header nameValuePair : response.getAllHeaders()) {
			String name = nameValuePair.getName();
			List<String> aH = new ArrayList<String>();
			for (HeaderElement a : nameValuePair.getElements()) {
				aH.add(a.getValue());
			}
			hf.put(name, aH);
		}

		resultMap.put("headerFields", hf);
		return resultMap;
	}

	public static Map<String, Object> PostForJsonOld(String query, String endpoint) throws MalformedURLException, IOException {
		String data = "";
		if (query != null) {
			query = URLDecoder.decode(query, "UTF-8");
			data = "query=" + URLEncoder.encode(query, "UTF-8");
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String result = "";
		URLConnection conn = new URL(endpoint).openConnection();
		conn.setRequestProperty("Accept-Charset", "utf-8");
		System.out.println("conn " + conn.getURL());

		conn.setRequestProperty("Accept", "application/sparql-results+json");
		conn.setDoOutput(true);

		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		// Get the response
		System.out.println("reading output ");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		Map<String, List<String>> hf = conn.getHeaderFields();
		String line;
		while ((line = rd.readLine()) != null) {
			result += line + "\n";
		}
		System.err.println(result);
		System.out.println("ended ");
		wr.close();
		rd.close();

		resultMap.put("result", result);
		resultMap.put("headerFields", hf);
		return resultMap;
	}

	public static Map<String, Object> getRDF(String endpoint) throws MalformedURLException, IOException, NoSuchAlgorithmException, KeyManagementException {

		/*
		 * fix for Exception in thread "main"
		 * javax.net.ssl.SSLHandshakeException:
		 * sun.security.validator.ValidatorException: PKIX path building failed:
		 * sun.security.provider.certpath.SunCertPathBuilderException: unable to
		 * find valid certification path to requested target
		 */
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		/*
		 * end of the fix
		 */

		Map<String, Object> resultMap = new HashMap<String, Object>();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		HttpProtocolParams.setHttpElementCharset(params, "utf-8");
		params.setBooleanParameter("http.protocol.expect-continue", false);
		HttpClient httpclient = new DefaultHttpClient(params);

		HttpGet httpGet = new HttpGet(endpoint);
		httpGet.addHeader("Accept", RDFFormat.RDFXML.getDefaultMIMEType());
		httpGet.addHeader("Accept-Charset", "utf-8");

		System.out.println("connected to " + endpoint);
		HttpResponse httpResponse = httpclient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();
		// System.out.println(httpResponse.getLocale());
		// System.out.println(EntityUtils.getContentCharSet(httpEntity));
		String httpResult = EntityUtils.toString(httpEntity, HTTP.UTF_8);
		// System.out.println("ended ");
		// System.out.println(httpResult);

		resultMap.put("result", httpResult);
		resultMap.put("headerFields", null);
		return resultMap;
	}

	public static Map<String, Object> PostForRDF(String endpoint, String format) throws MalformedURLException, IOException, NoSuchAlgorithmException, KeyManagementException {

		/*
		 * fix for Exception in thread "main"
		 * javax.net.ssl.SSLHandshakeException:
		 * sun.security.validator.ValidatorException: PKIX path building failed:
		 * sun.security.provider.certpath.SunCertPathBuilderException: unable to
		 * find valid certification path to requested target
		 */
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		/*
		 * end of the fix
		 */

		Map<String, Object> resultMap = new HashMap<String, Object>();
		String result = "";
		URLConnection conn = new URL(endpoint).openConnection();

		conn.setRequestProperty("Accept", format);
		conn.setRequestProperty("Accept-Charset", "utf-8");

		System.out.println("connected to " + endpoint);
		conn.setDoOutput(true);

		// Get the response
		// System.out.println("reading output ");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		Map<String, List<String>> hf = conn.getHeaderFields();
		String line = new String(new byte[0], Charset.forName("UTF-8"));
		while ((line = rd.readLine()) != null) {
			result += line + "\n";
		}
		System.out.println("ended ");
		// System.out.println(result);

		rd.close();
		resultMap.put("result", result);
		resultMap.put("headerFields", hf);
		return resultMap;
	}

}
