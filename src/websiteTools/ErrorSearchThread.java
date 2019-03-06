package websiteTools;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;

public class ErrorSearchThread extends Thread {

	WebsiteToolsGUI mygui;
	ErrorSearchAction thisAction;

	ErrorSearchThread(WebsiteToolsGUI gui, ErrorSearchAction action) {
		this.mygui = gui;
		this.thisAction = action;
	}

	@Override
	public void run() {
		if (WebsiteTools.DEBUG) {
			System.out.println("Thread " + this.getName() + " started.");
		}
		yield();
		ErrorsAndRedirects errorsRedirects = new ErrorsAndRedirects();
		while (!thisAction.crawlPages.isEmpty()) {
			String singleURL = thisAction.crawlPages.removeFirst();
			yield();

			mygui.writeProgressRightSafely(thisAction.crawlPages.size() + " pages remaining");
			try {
				errorsRedirects = checkPages(singleURL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!mygui.stopFlag) {

				if (!((mygui.onlyErrorsBox.getState() && errorsRedirects.errorPages.isEmpty()
						&& errorsRedirects.redirectPages.isEmpty()))) {
					mygui.addResultsText(singleURL, errorsRedirects);
				}
			} else {
				mygui.addResultsText("canceled");
				break;
			}
		}
		if (WebsiteTools.DEBUG) {
			System.out.println("Thread " + this.getName() + " finished.");
		}
	}

	public ErrorsAndRedirects checkPages(String urlToCheck) throws InterruptedException {
		Hashtable<String, Integer> errorPages = new Hashtable<String, Integer>();
		Hashtable<String, Redirect> redirectPages = new Hashtable<String, Redirect>();

		try {

			// Use getLinks to return array of links from website "urlToCheck"

			LinkedList<String> urls = ErrorSearchAction.getLinks(urlToCheck);

			this.mygui.writeStatusSafely("Checking " + urls.size() + " links on: " + urlToCheck + "\n\n");

			int currentCode;

			// Iterate through the Urls
			while (!mygui.stopFlag && !urls.isEmpty()) {
				String currentURL = urls.pollFirst();
				mygui.writeProgressSafely(urls.size() + " left");

				if (WebsiteTools.DEBUG) {
					System.out.println("Thread " + this.getName() + " testing " + currentURL);
				} // DEBUG

				// check if URL is already in known redirects
				if (!mygui.redirectedLinks.isEmpty() && mygui.redirectedLinks.containsKey(currentURL)) {
					redirectPages.put(currentURL, mygui.redirectedLinks.get(currentURL));
				}

				// check if URL is already in known badLinks
				if (mygui.badLinks.contains(currentURL)) {
					errorPages.put(currentURL, 404); // badLinks should only contain pages that already returned 404
				}

				if (!mygui.goodLinks.contains(currentURL) && !ErrorSearchAction.isImage(currentURL)
						&& !ErrorSearchAction.isDocument(currentURL)) {
					try {
						// Connect to the URL and add Response Code to Codes Array
						URL currentURLurl = new URL(currentURL);
						HttpURLConnection connect = (HttpURLConnection) currentURLurl.openConnection();

						// close the connection, if there is no response for 5/8 seconds
						connect.setConnectTimeout(5000);
						connect.setReadTimeout(8000);

						currentCode = connect.getResponseCode();
						if (currentCode == 200) {
							mygui.goodLinks.add(currentURL);
						} else {
							// check for Redirects
							if (currentCode == 301 || currentCode == 302) {

								redirectPages.put(currentURL, new Redirect(
										ErrorSearchAction.getDestinationURL(currentURLurl).toString(), currentCode));

								// Everything else
							} else {
								if (currentCode == 404) {
									mygui.badLinks.add(currentURL);
								} // write Pages with 404 into badLinks, and only those with 404
								errorPages.put(currentURL, currentCode);
							}
						}
						connect.disconnect();
					} catch (SocketTimeoutException ste) {
						System.out.println("Timeout for URL: " + currentURL);
						errorPages.put(currentURL, 888);
						ste.printStackTrace();
					} catch (Exception e) {
						System.out.println("Problems with URL: " + currentURL);
						errorPages.put(currentURL, 999);
						e.printStackTrace();
					}
				}
			}
			mygui.writeProgressSafely("");

		} catch (

		Exception e) {
			e.printStackTrace();

		}
		return new ErrorsAndRedirects(errorPages, redirectPages);
	}

}
