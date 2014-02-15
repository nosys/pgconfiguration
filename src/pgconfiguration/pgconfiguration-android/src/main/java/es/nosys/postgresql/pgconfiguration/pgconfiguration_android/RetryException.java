package es.nosys.postgresql.pgconfiguration.pgconfiguration_android;

public class RetryException extends Exception {

	private static final long serialVersionUID = 6488381361979974478L;

	public RetryException(Throwable throwable) {
		super(throwable);
	}
	
}
