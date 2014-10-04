import java.io.File;






//import org.openrdf.query.Query;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import com.github.jsonldjava.core.RDFDataset.Literal;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;



public class Sesame {
	public static void main(String args[]) throws RepositoryException{
		/*File file = new File("/Users/student/Desktop/file.txt");
		Repository repo = new SailRepository(new NativeStore(file));
		repo.initialize();*/
		
		String query =  "PREFIX dbpprop: <http://dbpedia.org/property/>"+
						"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
						"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
						"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>"+
						"SELECT DISTINCT ?university ?name ?city ?latitude ?longitude "+
						"WHERE{ {"+
							"?university dbpprop:type dbpedia:Private_university."+
							"?university dbpprop:state dbpedia:California."+ 

							"OPTIONAL {?university dbpprop:name ?name.}"+
							"OPTIONAL {?university dbpprop:city ?city.}"+
							//"OPTIONAL {?university dbpedia-owl:city ?city.}"+
							"OPTIONAL {?university dbpprop:label ?name.}"+
							"OPTIONAL {?university rdfs:label ?name.}"+
							"OPTIONAL {?university geo:lat ?latitude.}"+
							"OPTIONAL {?university geo:long ?longitude.}"+
							"OPTIONAL {?university dbpprop:lat ?latitude.}"+
							"OPTIONAL {?university dbpprop:long ?longitude.}"+
							"OPTIONAL {?university dbpprop:location ?city.}"+
							"}}";
		
		Query q =  QueryFactory.create(query);
		ARQ.getContext().setTrue(ARQ.useSAX);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", q);
		
		try{
			ResultSet result = qexec.execSelect();
//			ResultSetFormatter.out(System.out, result, q);
		
			if(qexec.execSelect()!=null)
				System.out.println("Present");
			else
				System.out.println("Absent");
		
			while(result.hasNext()) {
				QuerySolution qs = result.nextSolution();
				System.out.println(qs.get("?university"));
			}
		}
		finally{
			qexec.close();
		}
//		QueryExecution x = QueryExecutionFactory.sparqlService(ontology_service, String.format(endpointsSparql,endpoint));
//		ResultSet results = x.execSelect();
//		ResultSetFormatter.out(System.out, results);
	}
}
