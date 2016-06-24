package org.dvcama.lodview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class SimpleGraph {

	Repository therepository = null;

	// useful -local- constants
	static RDFFormat NTRIPLES = RDFFormat.NTRIPLES;
	static RDFFormat N3 = RDFFormat.N3;
	static RDFFormat RDFXML = RDFFormat.RDFXML;
	static String RDFTYPE = RDF.TYPE.toString();

	/**
	 * In memory Sesame repository without type inferencing
	 */
	public SimpleGraph() {
		this(false);
	}

	/**
	 * In memory Sesame Repository with optional inferencing
	 * 
	 * @param inferencing
	 */
	public SimpleGraph(boolean inferencing) {
		try {
			if (inferencing) {
				therepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));

			} else {
				therepository = new SailRepository(new MemoryStore());
			}
			therepository.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Literal factory
	 * 
	 * @param s
	 *            the literal value
	 * @param typeuri
	 *            uri representing the type (generally xsd)
	 * @return
	 */
	public org.openrdf.model.Literal Literal(String s, URI typeuri) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				if (typeuri == null) {
					return vf.createLiteral(s);
				} else {
					return vf.createLiteral(s, typeuri);
				}
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Untyped Literal factory
	 * 
	 * @param s
	 *            the literal
	 * @return
	 */
	public org.openrdf.model.Literal Literal(String s) {
		return Literal(s, null);
	}

	/**
	 * URIref factory
	 * 
	 * @param uri
	 * @return
	 */
	public URI URIref(String uri) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				return vf.createURI(uri);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * BNode factory
	 * 
	 * @return
	 */
	public BNode bnode() {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				return vf.createBNode();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * dump RDF graph
	 * 
	 * @param out
	 *            output stream for the serialization
	 * @param outform
	 *            the RDF serialization format for the dump
	 * @return
	 */
	public void dumpRDF(OutputStream out, RDFFormat outform) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				RDFWriter w = Rio.createWriter(outform, out);
				con.export(w, new Resource[0]);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convenience URI import for RDF/XML sources
	 * 
	 * @param urlstring
	 *            absolute URI of the data source
	 */
	public void addURI(String urlstring) {
		addURI(urlstring, RDFFormat.RDFXML);
	}

	/**
	 * Import data from URI source Request is made with proper HTTP ACCEPT
	 * header and will follow redirects for proper LOD source negotiation
	 * 
	 * @param urlstring
	 *            absolute URI of the data source
	 * @param format
	 *            RDF format to request/parse from data source
	 */
	public void addURI(String urlstring, RDFFormat format) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				con.add(new URL(urlstring), null, format);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Import RDF data from a string
	 * 
	 * @param rdfstring
	 *            string with RDF data
	 * @param format
	 *            RDF format of the string (used to select parser)
	 * @throws Exception
	 */
	public void addString(String rdfstring, RDFFormat format) throws Exception {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				StringReader sr = new StringReader(rdfstring);
				con.add(sr, "", format, new Resource[0]);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Import RDF data from a file
	 * 
	 * @param location
	 *            of file (/path/file) with RDF data
	 * @param format
	 *            RDF format of the string (used to select parser)
	 */
	public void addFile(String filepath, RDFFormat format) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				con.add(new File(filepath), "", format, new Resource[0]);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Insert Triple/Statement into graph
	 * 
	 * @param s
	 *            subject uriref
	 * @param p
	 *            predicate uriref
	 * @param o
	 *            value object (URIref or Literal)
	 */
	public void add(URI s, URI p, Value o) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				ValueFactory myFactory = con.getValueFactory();
				Statement st = myFactory.createStatement((Resource) s, p, (Value) o);
				con.add(st, new Resource[0]);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			// handle exception
		}
	}

	/**
	 * Tuple pattern query - find all statements with the pattern, where null is
	 * a wild card
	 * 
	 * @param s
	 *            subject (null for wildcard)
	 * @param p
	 *            predicate (null for wildcard)
	 * @param o
	 *            object (null for wildcard)
	 * @return serialized graph of results
	 */
	public List tuplePattern(URI s, URI p, Value o) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				RepositoryResult repres = con.getStatements(s, p, o, true, new Resource[0]);
				ArrayList reslist = new ArrayList();
				while (repres.hasNext()) {
					reslist.add(repres.next());
				}
				return reslist;
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Execute a CONSTRUCT/DESCRIBE SPARQL query against the graph
	 * 
	 * @param qs
	 *            CONSTRUCT or DESCRIBE SPARQL query
	 * @param format
	 *            the serialization format for the returned graph
	 * @return serialized graph of results
	 */
	public String runSPARQL(String qs, RDFFormat format) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				GraphQuery query = con.prepareGraphQuery(org.openrdf.query.QueryLanguage.SPARQL, qs);
				StringWriter stringout = new StringWriter();
				RDFWriter w = Rio.createWriter(format, stringout);
				query.evaluate(w);
				return stringout.toString();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void runJsonSPARQL(String qs, OutputStream servletOutputStream) {
		try {
			/*
			 * fix for Exception in thread "main"
			 * javax.net.ssl.SSLHandshakeException:
			 * sun.security.validator.ValidatorException: PKIX path building
			 * failed:
			 * sun.security.provider.certpath.SunCertPathBuilderException:
			 * unable to find valid certification path to requested target
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

			RepositoryConnection con = therepository.getConnection();
			try {
				TupleQuery query = con.prepareTupleQuery(org.openrdf.query.QueryLanguage.SPARQL, qs);
				TupleQueryResult qres = query.evaluate();

				org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter jsonWriter = new SPARQLResultsJSONWriter(
						servletOutputStream);
				jsonWriter.startQueryResult(qres.getBindingNames());
				while (qres.hasNext()) {
					jsonWriter.handleSolution(qres.next());
				}
				jsonWriter.endQueryResult();

			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Execute a SELECT SPARQL query against the graph
	 * 
	 * @param qs
	 *            SELECT SPARQL query
	 * @return list of solutions, each containing a hashmap of bindings
	 */
	public List runSPARQL(String qs) {
		try {
			RepositoryConnection con = therepository.getConnection();
			try {
				TupleQuery query = con.prepareTupleQuery(org.openrdf.query.QueryLanguage.SPARQL, qs);
				TupleQueryResult qres = query.evaluate();
				ArrayList reslist = new ArrayList();
				while (qres.hasNext()) {
					BindingSet b = qres.next();
					Set names = b.getBindingNames();
					HashMap hm = new HashMap();
					for (Object n : names) {
						hm.put((String) n, b.getValue((String) n));
					}
					reslist.add(hm);
				}
				return reslist;
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	public static void main(String[] args) throws MalformedURLException, IOException {
//
//		// create a graph with type inferencing
//		SimpleGraph g = new SimpleGraph(false);
//
//		// load the film schema and the example data
//		// g.addFile("film-ontology.owl", SimpleGraph.RDFXML);
//		System.out.println("adding");
//		g.addURI("http://dbpedia.org/resource/Roma", SimpleGraph.RDFXML);
//		// g.addString(s, RDFFormat.RDFXML);
//		System.out.println("added");
//		g.runJsonSPARQL("SELECT DISTINCT * WHERE {?s ?property ?object}  ORDER BY ?property", System.out);
//
//	}
}
