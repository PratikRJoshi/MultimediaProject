import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


import java.util.List;


import org.openrdf.model.Literal;
import org.openrdf.model.URI;
//import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;


public class Sesame {
	public static void main(String args[]) throws RepositoryException, FileNotFoundException, MalformedQueryException, QueryEvaluationException{
		File file = new File("/Users/student/Desktop/outputFile.txt");
		PrintWriter pw = new PrintWriter(file);
//		Repository repo = new SailRepository(new NativeStore(file));
//		repo.initialize();
		
		//generate the query 
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
		
		//get the result from dbpedia
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", q);
		
		try{
			ResultSet results = qexec.execSelect();
			int counter = 1;
			
			Repository repository = getOnMemoryRepository();
			RepositoryConnection connection = repository.getConnection();
			
			String schemaURI = "http://schema.org/";
			String dummyURI = "http://www.dummy.org/";
			ValueFactory vFactory = repository.getValueFactory();

			
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
				
				String university = (qs.get("?university")==null?"":qs.get("?university").toString());
				URI subject = vFactory.createURI(university);
				URI predicate = vFactory.createURI("http://www.w3.org/2000/01/rdf-schema#type");
				URI object = vFactory.createURI(schemaURI+"CollegeOrUniversity");
				
				//add the university type triple to the store
				connection.add(subject, predicate, object);
				
				subject = vFactory.createURI(university);
				predicate = vFactory.createURI(schemaURI+"location");
				object = vFactory.createURI(dummyURI+"loc"+counter);
				
				//add the dummy location triple to the store
				connection.add(subject, predicate, object);
				
				subject = vFactory.createURI(dummyURI+"loc"+counter);
				predicate = vFactory.createURI("http://www.w3.org/2000/01/rdf-schema#type");
				object = vFactory.createURI(schemaURI+"Place");
				
				//add the location type triple to the store
				connection.add(subject, predicate, object);
				
				subject = vFactory.createURI(dummyURI+"loc"+counter);
				predicate = vFactory.createURI(schemaURI+"geo");
				object = vFactory.createURI(dummyURI+"geo"+counter);
				
				//add the dummy geo triple to the store
				connection.add(subject, predicate, object);
				
				subject = vFactory.createURI(dummyURI+"geo"+counter);
				predicate = vFactory.createURI("http://www.w3.org/2000/01/rdf-schema#type");
				object = vFactory.createURI(schemaURI+"GeoCoordinates");
				
				//add the geo type triple to the store
				connection.add(subject, predicate, object);
				
				subject = vFactory.createURI(dummyURI+"geo"+counter);
				predicate = vFactory.createURI(schemaURI+"latitude");
				String latitude = (qs.get("?latitude")==null?"":qs.get("?latitude").toString());
				Literal lit = vFactory.createLiteral(latitude);
				
				//add the latitude triple to the store
				connection.add(subject, predicate, lit);
				
				subject = vFactory.createURI(dummyURI+"geo"+counter);
				predicate = vFactory.createURI(schemaURI+"longitude");
				String longitude = (qs.get("?longitude")==null?"":qs.get("?longitude").toString());
				lit = vFactory.createLiteral(longitude);
				
				//add the longitude triple to the store
				connection.add(subject, predicate, lit);
				
				subject = vFactory.createURI(dummyURI+"loc"+counter);
				predicate = vFactory.createURI(schemaURI+"address");
				object = vFactory.createURI(dummyURI+"address"+counter);
				
				//add the dummy address triple to the store
				connection.add(subject, predicate, object);
				
				subject = vFactory.createURI(dummyURI+"address"+counter);
				predicate = vFactory.createURI("http://www.w3.org/2000/01/rdf-schema#type");
				object = vFactory.createURI(schemaURI+"PostalAddress");
				
				//add the address type triple to the store
				connection.add(subject, predicate, object);
				
				subject = vFactory.createURI(dummyURI+"address"+counter);
				predicate = vFactory.createURI(schemaURI+"addressLocality");
				String addressLocality = (qs.get("?city")==null?"":qs.get("?city").toString());
				lit = vFactory.createLiteral(addressLocality);
				
				//add the city name triple to the store
				connection.add(subject, predicate, lit);
				
				subject = vFactory.createURI(university);
				predicate = vFactory.createURI(schemaURI+"legalName");
				String legalName = (qs.get("?name")==null?"":qs.get("?name").toString());
				lit = vFactory.createLiteral(legalName);
				
				//add the university name triple to the store
				connection.add(subject, predicate, lit);
				
				pw.write("\t"+qs.get("?name"));
				pw.write("\t"+qs.get("?city"));
				pw.write("\t"+qs.get("?latitude"));
				pw.write("\t"+qs.get("?longitude"));
				pw.write("\n");
				counter++;
			}
			
			//query the triple store to check if the added triple can be retrieved
			queryRepository(repository);
			
		}
		finally{
			qexec.close();
			pw.close();
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
	
	static void queryRepository(Repository repository) throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		RepositoryConnection rConnection = repository.getConnection();
		String qString = "SELECT ?x ?y WHERE {?x ?p ?y}";
		String queryString = "SELECT ?y "
				+ "WHERE "
				+ "{ <http://dbpedia.org/resource/Point_Loma_Nazarene_University> <http://schema.org/legalName> ?y."
				+ " } ";
		TupleQuery tQuery = rConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult tqResult = tQuery.evaluate();
		
		while(tqResult.hasNext()){
			BindingSet bindingSet = tqResult.next();
//			Value vX = bindingSet.getValue("x");
//			Value vY = bindingSet.getValue("y");
			
			List<String> names = tqResult.getBindingNames();
			for (String string : names) {
				System.out.println(string +": "+bindingSet.getValue(string));
			}
		}
	}
}