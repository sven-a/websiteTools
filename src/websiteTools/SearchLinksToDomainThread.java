package websiteTools;

import java.util.LinkedList;

public class SearchLinksToDomainThread extends Thread {

	WebsiteToolsGUI mygui;
	SearchLinksToDomain thisAction;
	String domainToSearchFor = "study-in.de";

	SearchLinksToDomainThread(WebsiteToolsGUI gui, SearchLinksToDomain searchLinksToDomain) {
		this.mygui = gui;
		this.thisAction = searchLinksToDomain;
	}

	@Override
	public void run() {
		if (WebsiteTools.DEBUG) {
			System.out.println("Thread " + this.getName() + " started.");
		}
		yield();

		while (!thisAction.crawlPages.isEmpty()) {
			String singleURL = thisAction.crawlPages.removeFirst();
			yield();

			mygui.writeProgressRightSafely(thisAction.crawlPages.size() + " pages remaining");
			
			if (!mygui.stopFlag) {
				LinkedList<String> hyperlinksToDomain = findLinks(singleURL, domainToSearchFor);

				if (!hyperlinksToDomain.isEmpty()) {
					mygui.addResultsText("<h2>" + singleURL +"</h2>");
					for (String singleLink : hyperlinksToDomain) {
						mygui.addResultsText(singleLink + "<br>");
					}
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

	public LinkedList<String> findLinks(String urlToCheck, String urlToFind) {
		LinkedList<String> searchResults = new LinkedList<String>();

		// Use getLinks to return array of links from website "urlToCheck"

		LinkedList<String> urls = ErrorSearchAction.getLinks(urlToCheck);

		this.mygui.writeStatusSafely("Checking " + urls.size() + " links on: " + urlToCheck + "\n\n");

		// Iterate through the Urls
		while (!mygui.stopFlag && !urls.isEmpty()) {
			String currentURL = urls.pollFirst();
			mygui.writeProgressSafely(urls.size() + " left");

			if (currentURL.contains(urlToFind)) {
				searchResults.add(currentURL);
			}

		}
		mygui.writeProgressSafely("");

		return searchResults;
	}

}
