import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;



public class Sesame {
	public static void main(String args[]) throws RepositoryException{
		File file = new File("/Users/student/Desktop/file.txt");
		Repository repo = new SailRepository(new NativeStore(file));
		repo.initialize();
		
		String ontology_service = "http://ambit.uni-plovdiv.bg:8080/ontology";
		String endpoint = "otee:Endpoints";
		String endpointsSparql = 
		"PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
		"PREFIX ota:<http://www.opentox.org/algorithms.owl#>\n"+
		"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
		"PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"+
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
		"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
		"select ?url ?title\n"+
		"where {\n"+
		"?url rdfs:subClassOf %s.\n"+
		"?url dc:title ?title.\n"+
		"}\n";
		
		QueryExecution x = QueryExecutionFactory.sparqlService(ontology_service, String.format(endpointsSparql,endpoint));
		ResultSet results = x.execSelect();
		ResultSetFormatter.out(System.out, results);
	}
}
