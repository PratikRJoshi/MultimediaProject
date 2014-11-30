
public class TripleObject {
	public String entitySubject;
	public String entityName;
	public String entityType;
	
	public TripleObject(){
		
	}
	
	public TripleObject(String subject, String type, String name){
		this.entitySubject = subject;
		this.entityName = name;
		this.entityType = type;
	}
}
