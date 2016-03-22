package javax.persistence;

public class NonUniqueResultException extends RuntimeException {
	
	public NonUniqueResultException(){
		super();
	}

	public NonUniqueResultException(String message){
		super(message);
	}
};