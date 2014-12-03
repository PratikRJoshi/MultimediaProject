import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EntityExtractor {

	private static final String CALAIS_URL = "http://api.opencalais.com/tag/rs/enrich";

	private File input;
	private File output;
	private static String outputFile;
	private HttpClient client;
	private static Repository repository;
	static TripleObject tObj = new TripleObject();
	static List<TripleObject> repoList = new ArrayList<TripleObject>();
	
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
	public static void storeEntityList(List<TripleObject> entityList) {
		try {

			// Get the repository object
			repository = getOnMemoryRepository();

			// Get the connection object from the repository
			RepositoryConnection rdfRepositoryConnection = repository.getConnection();

			// Set the baseURI to schema.org
			String schemaURI = "http://schema.org/name";

			// Set the rdfs:type to http://www.w3.org/2000/01/rdf-schema#type
			String rdfsType = "http://www.w3.org/2000/01/rdf-schema#type";
			String dummyURI = "http://www.dummy.org/";
			
			ValueFactory factory = repository.getValueFactory();
			
			URI subject, predicate, object;
			Literal literal;

			// Iterate over all the university records and insert into the triple store
			for (int i = 0; i < entityList.size(); i++) {

//				System.out.println("Iteration "+(i+1));
//				System.out.println("Subject: "+entityList.get(i).entitySubject);
//				System.out.println("Predicate: "+entityList.get(i).entityType);
//				System.out.println("Object: "+entityList.get(i).entityName);
//				
				// <univ> rdfs:type schema:CollegeOrUniversity.
				subject = factory.createURI(entityList.get(i).entitySubject);
				predicate = factory.createURI(entityList.get(i).entityType);
				object = factory.createURI(dummyURI+entityList.get(i).entityName);
				
				rdfRepositoryConnection.add(subject, predicate, object);
			}
			
			queryStore(rdfRepositoryConnection);
			
		} catch (Exception e) {
//			System.out.println("Exception caused in storing triplets");
			e.printStackTrace();
		}
	}

	private static void queryStore(RepositoryConnection rdfRepositoryConnection) throws RepositoryException, MalformedQueryException{
		//generate the query 
		String queryString = "PREFIX dbpprop: <http://dbpedia.org/property/>\n"+
						"PREFIX dbpedia: <http://dbpedia.org/resource/>\n"+
						"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
						"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"+
						"SELECT *"+ 
						"WHERE {\n"+
							"?person <http://s.opencalais.com/1/type/em/e/Person> ?personName.\n"+
							"?city <http://s.opencalais.com/1/type/er/Geo/City> ?cityName.\n"+
							"?org <http://s.opencalais.com/1/type/em/e/Organization> ?orgName.\n"+
						"}";
		TupleQuery tQuery = rdfRepositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult tqResult = null;
		try {
			tqResult = tQuery.evaluate();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		
		try {
			while(tqResult.hasNext()){
				BindingSet bindingSet = tqResult.next();
				
				Value personSubject = bindingSet.getValue("person");
				Value personValue = bindingSet.getValue("personName");
				System.out.println("Person Subject: "+personSubject.stringValue());
				System.out.println("Person Value: "+personValue);
				
				System.out.println();
				
				Value citySubject = bindingSet.getValue("city");
				Value cityValue = bindingSet.getValue("cityName");
				System.out.println("City Subject: "+citySubject);
				System.out.println("City Name: "+cityValue);
				
				System.out.println();
				
				Value orgSubject = bindingSet.getValue("org");
				Value orgValue = bindingSet.getValue("orgName");
				System.out.println("Organization Subject: "+orgSubject);
				System.out.println("Organization Name: "+orgValue);
				
				System.out.println();
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
	}
	
	private PostMethod createPostMethod() {
		PostMethod method = new PostMethod(CALAIS_URL);

		// Set mandatory parameters
		method.setRequestHeader("x-calais-licenseID", "q9eqx5pu8gf29au9jjvdp2wj");

		// Set input content type
		//method.setRequestHeader("Content-Type", "text/xml; charset=UTF-8");
//		method.setRequestHeader("Content-Type", "text/html; charset=UTF-8");
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
			File out = new File(file.getName() + ".xml");
			outputFile = out.getAbsolutePath();
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
//			 dom = db.parse("C:\\Pratik\\548\\Calais\\output.xml");
			 dom = db.parse(outputFile);
			
//			 dom = db.parse("C:\\Pratik\\548\\Calais\\o\\article2.txt.xml");
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
//				System.out.println("Iteration "+(i+1));
				Node tempNode = nodeList.item(i);
				//get the name
//				System.out.println(nodeList.item(i).getNodeName());
//				System.out.println("Details of article "+(i+1));
//				System.out.println("==================================================");
				
				if(tempNode.getNodeType() == Node.ELEMENT_NODE){
					Element element = (Element)tempNode;
					
					getPersonDetails(element);
					
					getCityDetails(element);
					
					getOrganizationDetails(element);
					
//					System.out.println();
				}
			}
		}
	}
	
	public static void getPersonDetails(Element element){
		//check if the element has person details in it
		//if so, extract them
		if((element.getAttribute("rdf:about")).contains("pershash")){
			String personSubject = element.getAttribute("rdf:about");
//			System.out.println("Name description attribute: "+personSubject);
//			if(personName!=null)
//				entityObj = new Entity();
			Node firstChild = element.getFirstChild();
//			System.out.println("Resource id: "+ firstChild.getNodeName());
			String firstChildAttribute = ((Element) firstChild).getAttribute("rdf:resource");
//			System.out.println(firstChildAttribute);
			Node nameNode = firstChild.getNextSibling();
			String name = nameNode.getTextContent();
//			System.out.println("Name: "+name);
			
			tObj = new TripleObject(personSubject, firstChildAttribute, name);
			repoList.add(tObj);
		}
	}
	
	public static void getCityDetails(Element element){
		//check if the element has city details in it
		//if so, extract them
		if((element.getAttribute("rdf:about")).contains("city")){
			String citySubject = element.getAttribute("rdf:about");
//			System.out.println("City description attribute: "+citySubject);
			Node firstChild = element.getFirstChild();
//			System.out.println("Resource id: "+ firstChild.getNodeName());
			String firstChildAttribute = ((Element) firstChild).getAttribute("rdf:resource");
//			System.out.println(firstChildAttribute);
			String cityName = firstChild.getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getTextContent();
//			System.out.println("City name: "+cityName);
			
			tObj = new TripleObject(citySubject, firstChildAttribute, cityName);
			repoList.add(tObj);
		}
	}
	
	public static void getOrganizationDetails(Element element){
		//if the element contains organization, extract it
		if((((Element)(element.getFirstChild())).getAttribute("rdf:resource")).contains("Organization")){
			String orgSubject = element.getAttribute("rdf:about");
//			System.out.println("Organization description attribute: "+orgSubject);
			Node firstChild = element.getFirstChild();
//			System.out.println("Resource id: "+ firstChild.getNodeName());
			String firstChildAttribute = ((Element) firstChild).getAttribute("rdf:resource");
//			System.out.println(firstChildAttribute);
			String orgName = firstChild.getNextSibling().getTextContent();
//			System.out.println("Organization name: "+orgName);
			
			tObj = new TripleObject(orgSubject, firstChildAttribute, orgName);
			repoList.add(tObj);
		}
	}
	
	public static void main(String[] args) throws IOException {
//		verifyArgs(args);
		
//		String baseURL ="http://www.thehindu.com/opinion/op-ed/monitoring-the-situation-in-chhattisgarh/article6655763.ece?homepage=true";
		String baseURL = "http://www.thehindu.com/news/cities/mumbai/starting-to-bring-art-into-lives-of-mumbai-citizens/article6639928.ece";
		org.jsoup.nodes.Document homeDoc = Jsoup.connect(baseURL).get();
//		System.out.println("File from URL"+(homeDoc).html());
		
		Elements textBody = homeDoc.getElementsByClass("body");
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < textBody.size() - 1; i++){
			sb.append(textBody.get(i).text());
		}
		
//		System.out.println("URL content body :\n"+sb.toString());
		File file = new File("article1.txt");
		
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(sb.toString());
		bw.close();
		
		EntityExtractor firstHttpClientPost = new EntityExtractor();
//		firstHttpClientPost.input = new File(sb.toString());
		firstHttpClientPost.input = file;
		if((firstHttpClientPost.input).isFile())
			System.out.println("Yes");
		else
			System.out.println("No");
//		System.exit(0);

		firstHttpClientPost.output = new File("nidhi");
		firstHttpClientPost.client = new HttpClient();
		firstHttpClientPost.client.getParams().setParameter("http.useragent", "Calais Rest Client");

		firstHttpClientPost.run();
		
		extractContentFromRDFXML();
		
		storeEntityList(repoList);
	}
}