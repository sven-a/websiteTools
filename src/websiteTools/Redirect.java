package websiteTools;


public class Redirect {
	private String destinationURL;
	private int code;
	
	
	Redirect(String destination, int code) {
		this.destinationURL = destination;
		this.code = code;
	}
	
	Redirect() {
		this.destinationURL = null;
		this.code = 0;
	}

	public void setDestination(String destination) {
		this.destinationURL = destination;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getDestination() {
		return destinationURL;
	}
	
	public int getCode() {
		return code;
	}
	
	
}
