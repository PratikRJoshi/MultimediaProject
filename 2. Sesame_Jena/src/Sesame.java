import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;



public class Sesame {
	public static void main(String args[]) throws RepositoryException, FileNotFoundException{
		File file = new File("/Users/student/Desktop/outputFile.txt");
		PrintWriter pw = new PrintWriter(file);
//		Repository repo = new SailRepository(new NativeStore(file));
//		repo.initialize();
		
		String query = "PREFIX dbpprop: <http://dbpedia.org/property/>\n"+
						"PREFIX dbpedia: <http://dbpedia.org/resource/>\n"+
						"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
						"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"+
						"SELECT DISTINCT ?university ?name ?city ?latitude ?longitude\n"+ 
						"WHERE {\n"+
							"?university dbpprop:type dbpedia:Private_university.\n"+
							"?university dbpprop:state dbpedia:California.\n"+ 

        					"OPTIONAL {?university dbpprop:name ?name.}\n"+
        					"OPTIONAL {?university dbpprop:city ?city.}\n"+
        					"#OPTIONAL {?university dbpedia-owl:city ?city.}\n"+
        					"OPTIONAL {?university dbpprop:label ?name.}\n"+
        					"OPTIONAL {?university rdfs:label ?name.}\n"+
        					"OPTIONAL {?university geo:lat ?latitude.}\n"+
        					"OPTIONAL {?university geo:long ?longitude.}\n"+
        					"OPTIONAL {?university dbpprop:lat ?latitude.}\n"+
        					"OPTIONAL {?university dbpprop:long ?longitude.}\n"+
        					"OPTIONAL {?university dbpprop:location ?city.}\n"+  
						"}";


		Query q = QueryFactory.create(query);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", q);
		try{
			ResultSet results = qexec.execSelect();
			int counter = 1;
			while(results.hasNext()){
				QuerySolution qs = results.nextSolution();
				System.out.print(counter+". ");
				System.out.print("\t"+qs.get("?university"));
				System.out.print("\t"+qs.get("?name"));
				System.out.print("\t"+qs.get("?city"));
				System.out.print("\t"+qs.get("?latitude"));
				System.out.print("\t"+qs.get("?longitude"));
				System.out.println();
				pw.write(counter+". ");
				pw.write("\t"+qs.get("?university"));
				pw.write("\t"+qs.get("?name"));
				pw.write("\t"+qs.get("?city"));
				pw.write("\t"+qs.get("?latitude"));
				pw.write("\t"+qs.get("?longitude"));
				pw.write("\n");
				counter++;
			}
		}
		finally{
			qexec.close();
		}
	}
}