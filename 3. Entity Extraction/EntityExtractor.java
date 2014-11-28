import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.xerces.impl.XMLEntityManager.Entity;

import java.io.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.jsoup.Jsoup;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
//import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public class EntityExtractor {

	private static final String CALAIS_URL = "http://api.opencalais.com/tag/rs/enrich";

	private File input;
	private File output;
	private HttpClient client;
	private static Repository repository;
	
	// Get in memory repo
	private static Repository getOnMemoryRepository() throws RepositoryException {

		MemoryStore memory = new MemoryStore();
		Sail sail = new ForwardChainingRDFSInferencer(memory);

		// This class allows inference
		Repository repo = new SailRepository(sail);

		// SAIL stands for Storage And Inference Layer
		repo.initialize();

		return repo;
	}
	
	// Store to triple store
	/*public static void storeEntityList(ArrayList<Entity> entityList) {
		try {

			// Get the repository object
			repository = getOnMemoryRepository();

			// Get the connection object from the repository
			RepositoryConnection rdfRepositoryConnection = repository.getConnection();

			// Set the baseURI to schema.org
			String schemaURI = "http://schema.org/name";

			// Set the rdfs:type to http://www.w3.org/2000/01/rdf-schema#type
			String rdfsType = "http://www.w3.org/2000/01/rdf-schema#type";

			ValueFactory factory = repository.getValueFactory();
			
			URI subject, predicate, object;
			Literal literal;

			// Iterate over all the university records and insert into the triple store
			for (Entity entityObj : entityList) {

				// <univ> rdfs:type schema:CollegeOrUniversity.
				subject = factory.createURI(entityObj.);
				predicate = factory.createURI(rdfsType);
				object = factory.createURI(entityObj.type);

				rdfRepositoryConnection.add(subject, predicate, object);

				// <univ> schema:legalName "name".
				subject = factory.createURI(entityObj.resUrl);
				predicate = factory.createURI(schemaURI);
				literal = factory.createLiteral(entityObj.name);

				rdfRepositoryConnection.add(subject, predicate, literal);
			}
		} catch (Exception e) {
			System.out.println("Exception caused in storing triplets");
		}
	}*/

	private PostMethod createPostMethod() {
		PostMethod method = new PostMethod(CALAIS_URL);

		// Set mandatory parameters
		method.setRequestHeader("x-calais-licenseID", "q9eqx5pu8gf29au9jjvdp2wj");

		// Set input content type
		//method.setRequestHeader("Content-Type", "text/xml; charset=UTF-8");
		//method.setRequestHeader("Content-Type", "text/html; charset=UTF-8");
		method.setRequestHeader("Content-Type", "text/raw; charset=UTF-8");

		// Set response/output format
		method.setRequestHeader("Accept", "xml/rdf");
		//method.setRequestHeader("Accept", "application/json");

		// Enable Social Tags processing
		//method.setRequestHeader("enableMetadataType", "SocialTags");

		return method;
	}

	private void run() {
		try {
			if (input.isFile()) {
				postFile(input, createPostMethod());
			} else if (input.isDirectory()) {
				System.out.println("working on all files in " + input.getAbsolutePath());
				for (File file : input.listFiles()) {
					if (file.isFile())
						postFile(file, createPostMethod());
					else
						System.out.println("skipping "+file.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doRequest(File file, PostMethod method) {
		try {
			int returnCode = client.executeMethod(method);
			if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
				System.err.println("The Post method is not implemented by this URI");
				// still consume the response body
				method.getResponseBodyAsString();
			} else if (returnCode == HttpStatus.SC_OK) {
				System.out.println("File post succeeded: " + file);
				saveResponse(file, method);
			} else {
				System.err.println("File post failed: " + file);
				System.err.println("Got code: " + returnCode);
				System.err.println("response: "+method.getResponseBodyAsString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
	}

	private void saveResponse(File file, PostMethod method) throws IOException {
		PrintWriter writer = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					method.getResponseBodyAsStream(), "UTF-8"));
			File out = new File("output.xml");
			writer = new PrintWriter(new BufferedWriter(new FileWriter(out)));
			String line;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) try {writer.close();} catch (Exception ignored) {}
		}
	}

	private void postFile(File file, PostMethod method) throws IOException {
		method.setRequestEntity(new FileRequestEntity(file, null));
		doRequest(file, method);
	}

	private static void verifyArgs(String[] args) {
		if (args.length==0) {
			usageError("no params supplied");
		} else if (args.length < 2) {
			usageError("2 params are required");
		} else {
			if (!new File(args[0]).exists())
				usageError("file " + args[0] + " doesn't exist");
			File outdir = new File(args[1]);
			if (!outdir.exists() && !outdir.mkdirs())
				usageError("couldn't create output dir");
		}
	}

	private static void usageError(String s) {
		System.err.println(s);
		System.err.println("Usage: java " + (new Object() { }.getClass().getEnclosingClass()).getName() + " input_dir output_dir");
		System.exit(-1);
	}

	public static void extractContentFromRDFXML(){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom;
		
		try {
			//gets an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			//parse using builder to get DOM representation of the XML file
			 dom = db.parse("C:\\Pratik\\548\\output.xml");
			 dom.getDocumentElement().normalize();
			 System.out.println("Root element = "+dom.getDocumentElement().getNodeName());
			 
			 parseDocument(dom);
			
		}
		catch(Exception e){
			
		}
	}
	
	public static void parseDocument(Document dom){
		Element root = dom.getDocumentElement();
		NodeList nodeList = root.getElementsByTagName("rdf:Description");
		if (nodeList!=null && nodeList.getLength() > 0) {
			for(int i=0; i<nodeList.getLength(); i++){

				Node tempNode = nodeList.item(i);
				//get the name
//				System.out.println(nodeList.item(i).getNodeName());
				
				if(tempNode.getNodeType() == Node.ELEMENT_NODE){
					Element element = (Element)tempNode;
					//check if the element has person details in it
					//if so, extract them
					if((element.getAttribute("rdf:about")).contains("pershash")){
						System.out.println("Name description attribute: "+element.getAttribute("rdf:about"));
						Node firstChild = element.getFirstChild();
						System.out.println("Resource id: "+ firstChild.getNodeName());
						String firstChildAttribute = ((Element) firstChild).getAttribute("rdf:resource");
						System.out.println(firstChildAttribute);
						Node nameNode = firstChild.getNextSibling();
						System.out.println("Name: "+nameNode.getTextContent());
						
					}
					
					//check if the element has city details in it
					//if so, extract them
					if((element.getAttribute("rdf:about")).contains("city")){
						
						System.out.println("City description attribute: "+element.getAttribute("rdf:about"));
						Node firstChild = element.getFirstChild();
						System.out.println("Resource id: "+ firstChild.getNodeName());
						String firstChildAttribute = ((Element) firstChild).getAttribute("rdf:resource");
						System.out.println(firstChildAttribute);
						System.out.println("City name: "+firstChild.getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getTextContent());
					}
					
					//if the element contains organization, extract it
					if((((Element)(element.getFirstChild())).getAttribute("rdf:resource")).contains("Organization")){
						System.out.println("Organization description attribute: "+element.getAttribute("rdf:about"));
						Node firstChild = element.getFirstChild();
						System.out.println("Resource id: "+ firstChild.getNodeName());
						String firstChildAttribute = ((Element) firstChild).getAttribute("rdf:resource");
						System.out.println(firstChildAttribute);
						System.out.println("Organization name: "+firstChild.getNextSibling().getTextContent());
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		verifyArgs(args);
		EntityExtractor httpClientPost = new EntityExtractor();
		httpClientPost.input = new File(args[0]);
		httpClientPost.output = new File(args[1]);
		httpClientPost.client = new HttpClient();
		httpClientPost.client.getParams().setParameter("http.useragent", "Calais Rest Client");

		httpClientPost.run();
		
		extractContentFromRDFXML();
	}
}