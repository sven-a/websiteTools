package websiteTools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MetaInfoButtonAction extends Thread {
	WebsiteToolsGUI mygui;

	MetaInfoButtonAction(WebsiteToolsGUI newGUI) {
		this.mygui = newGUI;
	}

	@Override
	public void run() {

		// deactivate input line and runButton, enable stopButton:
		mygui.buttonConfigurationWorking();

		// Clear Results
		mygui.clearResults();

		ActionListener stopListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mygui.stopFlag = true;
			}

		};

		mygui.stopButton.addActionListener(stopListener);

		// read URL from statusBar

		String urlFromInput = HyperLinkFormatter.cleanURL(mygui.statusBar.getText());

		LinkedList<String> crawlPages = new LinkedList<String>();
		try {
			// When "recursive" is selected, identify all subpages, add them to the
			// LinkedList and display results of subpage crawl
			if (mygui.recursiveBox.getState()) {
				// Search for all subpages and collect them in a LinkedList
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

			crawlPages.addFirst(urlFromInput); // add the input URL at the start

			mygui.initialiseResults(); // initialise results window

			// check all links in the list and show the results immediately
			while (!crawlPages.isEmpty()) {
				String singleURL = crawlPages.removeFirst();

				mygui.writeProgressRightSafely(crawlPages.size() + " pages remaining");
				// errorsRedirects = checkPages(singleURL);
				if (!mygui.stopFlag) {
					Document doc = Jsoup.connect(singleURL).get();
					String title = doc.title();
					String description = doc.select("meta[name=description]").get(0).attr("content");
					String keywords = doc.select("meta[name=keywords]").first().attr("content");

					MetaInfo currentMetaInfo = new MetaInfo(singleURL, title, description, keywords);
					// Show results in window

					mygui.addMetaInfoText(currentMetaInfo);

				} else {
					mygui.addResultsText("canceled");
					break;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Write the initially entered URL back into the statusBar, clean up progress,
		// set buttons to idle configuration
		mygui.writeStatusSafely(urlFromInput);
		mygui.writeProgressRightSafely("");

		if (mygui.stopFlag) {
			mygui.writeProgressSafely("canceled");
		} else {
			mygui.writeProgressSafely("finished");
		}
		mygui.buttonConfigurationIdle();
	}

	public LinkedList<String> getAllSubPages(String urlToCrawl) {
		LinkedList<String> subPages = new LinkedList<String>();
		LinkedList<String> unCrawledPages = new LinkedList<String>();

		String currentURL;
		unCrawledPages.add(urlToCrawl);
		mygui.writeStatusSafely("Collecting subpages of " + urlToCrawl);
		if (WebsiteTools.DEBUG) {
			System.out.println("ErrorSearchAction.getAllSubPages: Collecting subpages of " + urlToCrawl);
		}

		while (!unCrawledPages.isEmpty()) {
			currentURL = unCrawledPages.remove();

			if (!mygui.stopFlag) {

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
				return subPages;
			}
		}

		if (WebsiteTools.DEBUG) {
			System.out.println("ErrorSearchAction.getAllSubPages found " + subPages.size() + " subpages:");
			for (String singleURL : subPages) {
				System.out.println(singleURL);
			}
		}
		return subPages;
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

	public static String suffix(URL url) {
		int length = url.toString().trim().length();
		return url.toString().trim().substring(length - 4, length).toLowerCase();
	}

	public static boolean isImage(URL url) {
		String suffix = suffix(url);
		return (suffix.equals(".jpg") || suffix.equals(".gif") || suffix.equals(".png"));
	}
}
