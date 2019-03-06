package websiteTools;

import java.util.Hashtable;

public class ErrorsAndRedirects {
	Hashtable<String, Integer> errorPages = new Hashtable<String, Integer>();
	Hashtable<String, Redirect> redirectPages = new Hashtable<String, Redirect>();
	
	ErrorsAndRedirects() {
	}
	
	ErrorsAndRedirects(Hashtable<String, Integer> errorPages ,	Hashtable<String, Redirect> redirectPages) {
		this.errorPages = errorPages;
		this.redirectPages = redirectPages;
	}
	
	public Hashtable<String, Integer> getErrorPages() {
		return errorPages;
	}

	public Hashtable<String, Redirect> getRedirectPages() {
		return redirectPages;
	}
	
	
}