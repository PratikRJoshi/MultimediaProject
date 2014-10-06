package cs548.hw6;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
public class RDFUniversity {

	private final static String NAMESPACE = "http://seit1.lti.cs.cmu.edu/ontology#";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//File file = new File("/Users/shreenidhibhat/cs548/hw6/a.rdf");
		String baseURI = "http://schema.org/";

		try {
			
			Repository repo = getOnMemoryRepository();
		    RepositoryConnection con = repo.getConnection();
		   try {
			   ValueFactory factory = repo.getValueFactory();
			   
			   URI usc   = factory.createURI(baseURI+"USC");
			   URI type   = factory.createURI("http://www.w3.org/2000/01/rdf-schema#type");
			   URI colg   = factory.createURI(baseURI+"CollegeOrUniversity");
			   
		      con.add(usc,type,colg);
		      
		     // con.add(url, url.toString(), RDFFormat.RDFXML);
		      
		      queryRepo(repo);
		   }catch(Exception e)
		   {
			   e.printStackTrace();
		   }
		}catch(Exception e)
		   {
			   e.printStackTrace();
		   }
		   
		}
		
		
	
	
	
	public static void queryRepo(Repository repo)
	{

try {
   RepositoryConnection con = repo.getConnection();
   try {
	  String queryString = "SELECT ?x ?y WHERE { ?x ?p ?y } ";
	  TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

	  TupleQueryResult result = tupleQuery.evaluate();
	  try {
              while (result.hasNext()) {  // iterate over the result
			BindingSet bindingSet = result.next();
			Value valueOfX = bindingSet.getValue("x");
			Value valueOfY = bindingSet.getValue("y");

			List<String> names = result.getBindingNames();
	    	for ( String name : names ) {
		    	System.out.println( name+": "+bindingSet.getValue(name) );
	    	}
	    	
			
			
              }
	  }
	  finally {
	      result.close();
	  }
   }
   finally {
      con.close();
   }
}
catch (OpenRDFException e) {
   // handle exception
}
	}

	
	
	
	
	
	/**
	 * Creates an on-memory Repository object.
	 * See more details at http://www.openrdf.org/doc/sesame2/users/ch07.html#section-native-store-config
	 * 
	 */
	private static Repository getOnMemoryRepository() throws RepositoryException {
		MemoryStore memory = new MemoryStore(); 
		Sail sail = new ForwardChainingRDFSInferencer( memory ); // This class allows inference
		Repository repo = new SailRepository( sail ); // SAIL stands for Storage And Inference Layer
		repo.initialize();
		return repo;
	}
	
	
	
}
