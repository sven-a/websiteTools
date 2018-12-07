package websiteTools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import org.htmlparser.beans.LinkBean;

public class ErrorSearchAction extends Thread {

	WebsiteToolsGUI mygui;
	boolean stopFlag = false; // The stopFlag will be used to quit several processes when set to true

	ErrorSearchAction(WebsiteToolsGUI newGUI) {
		this.mygui = newGUI;
	}

	@Override
	public void run() {

		mygui.buttonConfigurationWorking();

		// Clear Results
		mygui.clearResults();

		ActionListener stopListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopFlag = true;
			}

		};

		mygui.stopButton.addActionListener(stopListener);

		// read URL from statusBar

		String urlFromInput = StringFormatter.cleanURL(mygui.statusBar.getText());

		LinkedList<String> crawlPages = new LinkedList<String>();
		// When "recursive" is selected, identify all subpages

		try {
			// Search for all subpages, collect them in a LinkedList and display the results
			if (mygui.recursiveBox.getState()) {

				crawlPages = getAllSubPages(urlFromInput);

				if (crawlPages.isEmpty()) {
					mygui.displayText("no subpages on " + urlFromInput);
				} else {

					mygui.displayText(crawlPages.size() + " subpages found on " + urlFromInput + "<br>");

					for (String singleURL : crawlPages) {
						mygui.addResultsText(singleURL + "<br>");
					}
				}
			}
			// add the initial URL to the list
			crawlPages.addFirst(urlFromInput);

			// clear results window
			mygui.initialiseResults();

			// check all links in the LinkedList and show the results immediately
			while (!crawlPages.isEmpty()) {
				String singleURL = crawlPages.removeFirst();

				mygui.writeProgressRightSafely(crawlPages.size() + " pages remaining");
				ErrorsAndRedirects errorsRedirects = checkPages(singleURL);
				if (!stopFlag) {

					if (!((mygui.onlyErrorsBox.getState() && errorsRedirects.errorPages.isEmpty()
							&& errorsRedirects.redirectPages.isEmpty()))) {
						mygui.addResultsText(singleURL, errorsRedirects);
					}
				} else {
					mygui.addResultsText("canceled");
					break;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Write the initially entered URL back into the statusBar
		mygui.writeStatusSafely(urlFromInput);
		mygui.writeProgressRightSafely("");
		mygui.buttonConfigurationIdle();

		// update the progress bar

		if (stopFlag) {
			mygui.writeProgressSafely("canceled");
		} else {
			mygui.writeProgressSafely("finished");
		}

	}

	public LinkedList<String> getAllSubPages(String urlToCrawl) {
		LinkedList<String> subPages = new LinkedList<String>();
		LinkedList<String> unCrawledPages = new LinkedList<String>();

		String currentURL;
		unCrawledPages.add(urlToCrawl);
		subPages.add(urlToCrawl);
		mygui.writeStatusSafely("Collecting subpages of " + urlToCrawl);

		while (unCrawledPages.size() != 0) {
			currentURL = unCrawledPages.poll();

			if (!stopFlag) {

				try {
					URL[] urls = getLinks(currentURL);
					String[] urlStrings = StringFormatter.cleanURLArray(urls);
					for (String singleURL : urlStrings) {
						String URLString = singleURL;
						if (URLString.startsWith(currentURL) && (!subPages.contains(URLString))) {
							subPages.add(URLString);
							unCrawledPages.add(URLString);
							mygui.writeProgressSafely((subPages.size() - 1) + " Subpages found: ");
						}
					}
				} catch (Exception e) {
					System.out.println("Error with URL: " + currentURL);
					e.printStackTrace();
				}
			} else {
				subPages = new LinkedList<String>();
				return subPages;
			}
		}

		subPages.remove(urlToCrawl);

		return subPages;
	}

	public ErrorsAndRedirects checkPages(String urlToCheck) throws InterruptedException {
		Hashtable<String, Integer> errorPages = new Hashtable<String, Integer>();
		Hashtable<String, Redirect> redirectPages = new Hashtable<String, Redirect>();

		try {

			// Use getLinks to return array of links from website "urlToCheck"

			URL[] urls = getLinks(urlToCheck);

			this.mygui.writeStatusSafely("Checking " + urls.length + " links on: " + urlToCheck + "\n\n");

			int currentCode;

			// Iterate through the Urls
			for (int i = 0; i < urls.length && !stopFlag; i++) {
				try {
					mygui.writeProgressSafely(i + " of " + urls.length);
					// DEBUG System.out.println("testing " + urls[i]);

					// see if URL was already checked and is not an image
					if (!mygui.goodLinks.contains(urls[i].toString()) && !isImage(urls[i])) {

						// Connect to the URL and add Response Code to Codes Array
						HttpURLConnection connect = (HttpURLConnection) urls[i].openConnection();

						// close the connection, if there is no response for 5/8 seconds
						connect.setConnectTimeout(5000);
						connect.setReadTimeout(8000);

						currentCode = connect.getResponseCode();
						if (currentCode == 200) {
							mygui.goodLinks.add(urls[i].toString());
						} else {
							// check for Redirects
							if (currentCode == 301 || currentCode == 302) {

								redirectPages.put(urls[i].toString(),
										new Redirect(getDestinationURL(urls[i]).toString(), currentCode));

								// Everything else
							} else {
								errorPages.put(urls[i].toString(), currentCode);
							}
						}
						connect.disconnect();
						mygui.writeProgressSafely("");
					}
				} catch (SocketTimeoutException ste) {
					System.out.println("Timeout for URL: " + urls[i]);
					errorPages.put(urls[i].toString(), 888);
					ste.printStackTrace();
				} catch (Exception e) {
					System.out.println("Problems with URL: " + urls[i]);
					errorPages.put(urls[i].toString(), 999);
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ErrorsAndRedirects(errorPages, redirectPages);
	}

	public static URL[] getLinks(String url) {
		// Use LinkBean class from HTMLParser to get array of links from a page
		LinkBean lb = new LinkBean();
		lb.setURL(url);
		URL[] urls = lb.getLinks();
		return urls;
	}

	public static URL getDestinationURL(URL url) {
		// returns the destination URL in case of a redirect
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);
			con.connect();

			String Location = con.getHeaderField("Location");
			if (Location.startsWith("/")) {
				Location = url.getProtocol() + "://" + url.getHost() + Location;
			}
			return new URL(Location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String suffix(URL url) {
		int length = url.toString().trim().length();
		return url.toString().trim().substring(length - 4, length).toLowerCase();
	}

	public static boolean isImage(URL url) {
		String suffix = suffix(url);
		return (suffix.equals(".jpg") || suffix.equals(".gif") || suffix.equals(".png"));
	}

}