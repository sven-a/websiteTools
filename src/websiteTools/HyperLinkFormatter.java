package websiteTools;

import java.util.LinkedList;

public class HyperLinkFormatter {

	public static String noSuffixHTML(String inputURL) {
		String resultURL = inputURL.trim();
		if (resultURL.endsWith("#")) {
			resultURL = resultURL.substring(0, resultURL.length() - 1);
		}
		/*if (resultURL.endsWith("/")) {
			resultURL = resultURL.substring(0, resultURL.length() - 1);
		}*/
		if (resultURL.endsWith(".html")) {
			resultURL = resultURL.substring(0, resultURL.length() - 5);
		}
		if (resultURL.endsWith(".htm")) {
			resultURL = resultURL.substring(0, resultURL.length() - 4);
		}

		return resultURL;
	}

	public static String cleanURL2(String inputURL) {
		String resultURL = inputURL;
		if (resultURL.startsWith("www")) {
			resultURL = "https://" + resultURL;
		}
		if (resultURL.endsWith("//")) {
			resultURL.substring(0, resultURL.length() - 2);
		}

		if (resultURL.endsWith("/")) {
			resultURL.substring(0, resultURL.length() - 1);
		}
		if (WebsiteTools.ignoreQueryFragments) {
			resultURL = noQueryFragment(resultURL);
		}
		resultURL = noSuffixHTML(resultURL);

		return resultURL;
	}

	public static String cleanURL(String inputURL) {
		String resultURL = inputURL;
		if (resultURL.startsWith("www")) {
			resultURL = "https://" + resultURL;
		}

		if (WebsiteTools.ignoreQueryFragments) {
			resultURL = noQueryFragment(resultURL);
		}
		resultURL = noSuffixHTML(resultURL);
		if (resultURL.endsWith("//")) {
			resultURL.substring(0, resultURL.length() - 1);
		}
		if (!resultURL.endsWith("/")) {
			resultURL += "/";
		}
		return resultURL;
	}
	
	public static String cleanHyperLink(String inputURL) {
		String resultURL = inputURL;
		if (resultURL.startsWith("www")) {
			resultURL = "https://" + resultURL;
		}

		if (WebsiteTools.ignoreQueryFragments) {
			resultURL = noQueryFragment(resultURL);
		}
		
		if (resultURL.endsWith("//")) {
			resultURL.substring(0, resultURL.length() - 1);
		}
	
		return resultURL;
	}

	public static boolean isValidHyperlink(String inputURL) {
		return (!inputURL.startsWith("#")  
				&& !inputURL.equals("/")
				&& !inputURL.isEmpty()) 
				&& !inputURL.startsWith("mail") 
				&& !ErrorSearchAction.isImage(inputURL) 
				&& !ErrorSearchAction.isDocument(inputURL);
	}
	
	public static String cleanDomain(String inputURL) {
		String resultURL = cleanURL(inputURL);

		if (resultURL.substring(8).contains("/")) { // https:// is 8 characters
			resultURL = resultURL.substring(0, resultURL.substring(8).indexOf("/") + 8);
		}
		return resultURL;
	}

	public static LinkedList<String> cleanURLLinkedList(LinkedList<String> urlList) {
		LinkedList<String> results = new LinkedList<String>();
		for (String singleURL : urlList) {
			results.add(cleanURL(singleURL));
		}
		return results;
	}

	public static String[] cleanURLArray(String[] urls) {
		String[] results = new String[urls.length];
		for (int i = 0; i < urls.length; i++) {
			results[i] = cleanURL(urls[i]);
		}
		return results;
	}

	public static String noQueryFragment(String inputURL) {
		String resultURL = inputURL.trim();
		if (resultURL.contains("?")) {
			resultURL = resultURL.substring(0, resultURL.indexOf("?"));
		}
		if (resultURL.contains("#")) {
			resultURL = resultURL.substring(0, resultURL.indexOf("#"));
		}
		return resultURL;
	}

	public static String notADocument(String inputURL) {
		String resultURL = inputURL.trim();
		if (resultURL.endsWith(".doc") || resultURL.endsWith(".pdf") || resultURL.endsWith(".xls")) {
			resultURL = resultURL.substring(0, resultURL.length() - 4);
		}
		return resultURL;
	}
	
	
}