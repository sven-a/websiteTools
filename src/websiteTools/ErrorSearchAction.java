package websiteTools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

		String urlFromInput = HyperLinkFormatter.cleanURL(mygui.statusBar.getText());

		// LinkedList<String> crawlPages = new LinkedList<String>();
		ConcurrentLinkedQueue<String> crawlPages = new ConcurrentLinkedQueue<String>();
		// When "recursive" is selected, identify all subpages

		try {
			// Search for all subpages, collect them in a LinkedList and display the results
			if (WebsiteTools.DEBUG) {
				System.out.println("ErrorSearchAction: recursive is set to " + mygui.recursiveBox.getState());
			}
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

			// clear results window
			mygui.initialiseResults();

			// check the links of the initial URL and show the results
			// crawlPages.addFirst(urlFromInput);
			ErrorsAndRedirects errorsRedirects = checkPages(urlFromInput);
			if (!stopFlag) {

				if (!((mygui.onlyErrorsBox.getState() && errorsRedirects.errorPages.isEmpty()
						&& errorsRedirects.redirectPages.isEmpty()))) {
					mygui.addResultsText(urlFromInput, errorsRedirects);
				}
			} else {
				mygui.addResultsText("canceled");
			}

			// if recursive box is checked: check all links in the LinkedList and show the results immediately
			if (mygui.recursiveBox.getState()) {

				// TODO: multithreading!
				int cores = Runtime.getRuntime().availableProcessors();
				if (WebsiteTools.DEBUG) {
					System.out.println("number of available cores: " + cores);
				}

				for (int i = 1; i<= cores /2 ; i++) {
					if (WebsiteTools.DEBUG) {
						System.out.println("starting thread number " + i);
					}
				}
				
				while (!crawlPages.isEmpty()) {
					String singleURL = crawlPages.poll();

					mygui.writeProgressRightSafely(crawlPages.size() + " pages remaining");
					errorsRedirects = checkPages(singleURL);
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

	public ConcurrentLinkedQueue<String> getAllSubPages(String urlToCrawl) {
		ConcurrentLinkedQueue<String> subPages = new ConcurrentLinkedQueue<String>();
		LinkedList<String> unCrawledPages = new LinkedList<String>();

		String currentURL;
		unCrawledPages.add(urlToCrawl);
		// subPages.add(urlToCrawl);
		mygui.writeStatusSafely("Collecting subpages of " + urlToCrawl);
		if (WebsiteTools.DEBUG) {
			System.out.println("ErrorSearchAction.getAllSubPages: Collecting subpages of " + urlToCrawl);
		}

		while (!unCrawledPages.isEmpty()) {
			currentURL = unCrawledPages.remove();

			if (!stopFlag) {

				try {
					LinkedList<String> urls = getLinks(currentURL);
					urls = HyperLinkFormatter.cleanURLLinkedList(urls);
					for (String singleURL : urls) {
						if (singleURL.startsWith(currentURL) && (!subPages.contains(singleURL))) {
							subPages.add(singleURL);
							unCrawledPages.add(singleURL);
							mygui.writeProgressSafely((subPages.size()) + " Subpages found: ");
						}
					}
				} catch (Exception e) {
					if (WebsiteTools.DEBUG) {
						System.out.println("Error with URL: " + currentURL);
					}
					e.printStackTrace();
				}
			} else {
				subPages = new ConcurrentLinkedQueue<String>();
				return subPages;
			}
		}

		// subPages.remove(urlToCrawl);
		if (WebsiteTools.DEBUG) {
			System.out.println("ErrorSearchAction.getAllSubPages found " + subPages.size() + " subpages:");
			for (String singleURL : subPages) {
				System.out.println(singleURL);
			}
		}
		return subPages;
	}

	public ErrorsAndRedirects checkPages(String urlToCheck) throws InterruptedException {
		Hashtable<String, Integer> errorPages = new Hashtable<String, Integer>();
		Hashtable<String, Redirect> redirectPages = new Hashtable<String, Redirect>();

		try {

			// Use getLinks to return array of links from website "urlToCheck"

			LinkedList<String> urls = getLinks(urlToCheck);

			this.mygui.writeStatusSafely("Checking " + urls.size() + " links on: " + urlToCheck + "\n\n");

			int currentCode;

			// Iterate through the Urls
			while (!stopFlag && !urls.isEmpty()) {
				String currentURL = urls.pollFirst();
				mygui.writeProgressSafely(urls.size() + " left");

				if (WebsiteTools.DEBUG) {
					System.out.println("testing " + currentURL);
				} // DEBUG

				// check if URL is already in known redirects
				if (!mygui.redirectedLinks.isEmpty() && mygui.redirectedLinks.containsKey(currentURL)) {
					redirectPages.put(currentURL, mygui.redirectedLinks.get(currentURL));
				}

				// check if URL is already in known badLinks
				if (mygui.badLinks.contains(currentURL)) {
					errorPages.put(currentURL, 404); // badLinks should only contain pages that already returned 404
				}

				if (!mygui.goodLinks.contains(currentURL) && !isImage(currentURL) && !isDocument(currentURL)) {
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

								redirectPages.put(currentURL,
										new Redirect(getDestinationURL(currentURLurl).toString(), currentCode));

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

	public static LinkedList<String> getLinks(String url) {
		// Use Jsoup to get a List of links from a page
		if (WebsiteTools.DEBUG) {
			System.out.println("ErrorSearchAction.getLinks getting links from " + url);
		}
		LinkedList<String> urlList = new LinkedList<String>();

		Document doc = new Document(url);
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (WebsiteTools.DEBUG) {
				System.out.println("getLinks connection Error");
			} // DEBUG

		}
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			String currentLink = HyperLinkFormatter.cleanURL(link.attr("href"));

			if (!currentLink.equals("#") && !currentLink.equals("#top") && !currentLink.equals("/")
					&& !currentLink.isEmpty()) {
				if (currentLink.startsWith("/")) {
					currentLink = HyperLinkFormatter.cleanDomain(url) + currentLink;
				}
				urlList.add(currentLink);

			}
		}

		if (WebsiteTools.DEBUG) {
			System.out.println("\nErrorSearchAction.getLinks found: ");
			for (String singleURL : urlList) {
				System.out.println(singleURL);
			}
		}
		return urlList;
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

	public static String suffix(String url) {
		int length = url.trim().length();
		return url.trim().substring(length - 4, length).toLowerCase();
	}

	public static boolean isImage(URL url) {
		String suffix = suffix(url);
		return (suffix.equals(".jpg") || suffix.equals(".gif") || suffix.equals(".png"));
	}

	public static boolean isImage(String url) {
		String suffix = suffix(url);
		return (suffix.equals(".jpg") || suffix.equals(".gif") || suffix.equals(".png"));
	}

	public static boolean isDocument(String url) {
		return (url.endsWith(".doc") || url.endsWith(".docx") || url.endsWith(".pdf") || url.endsWith(".xls")
				|| url.endsWith(".xlsx"));
	}
	
	

}