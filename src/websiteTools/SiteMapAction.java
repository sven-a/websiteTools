package websiteTools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class SiteMapAction extends Thread {
	WebsiteToolsGUI mygui;
	boolean stopFlag = false; // The stopFlag will be used to quit several processes when set to true

	SiteMapAction(WebsiteToolsGUI newGUI) {
		this.mygui = newGUI;
	}

	@Override
	public void run() {
		mygui.buttonConfigurationWorking();

		// Clear Results window
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
		if (WebsiteTools.DEBUG) {
			System.out.println("SiteMapAction URL from input: " + urlFromInput );
		}
		
		
		// create a TreeView JTree inside 
		TreeNode root = createNode(urlFromInput);
		
		JTree tree = new JTree(root);
		mygui.scrolling.add(new JScrollPane(tree));
		
		// Finishing touches...
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
	
	private DefaultMutableTreeNode createNode(String urlToCrawl) {
		LinkedList<String> subPages = getDirectSubPages(urlToCrawl);
		DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(urlToCrawl);
		
		for (String singleURL : subPages) {
        	if (stopFlag) {break;} // Stop the creation
			DefaultMutableTreeNode subNode = createNode(singleURL);
        	thisNode.add(subNode);
        }
		
		return thisNode;
	}
	
	
	public LinkedList<String> getDirectSubPages(String urlToCrawl) {
		LinkedList<String> subPages = new LinkedList<String>();
	
		mygui.writeStatusSafely("getDirectSubPages: Collecting subpages of " + urlToCrawl);

				try {
					LinkedList<String> urls = ErrorSearchAction.getLinks(urlToCrawl);
					LinkedList<String> urlStrings = HyperLinkFormatter.cleanURLLinkedList(urls);
					for (String singleURL : urlStrings) {
						if (!singleURL.equals(urlToCrawl) && singleURL.startsWith(urlToCrawl) && (!subPages.contains(singleURL)) && (!singleURL.substring(urlToCrawl.length() + 1).contains("/") )) {
							subPages.add(singleURL);
							mygui.writeProgressSafely((subPages.size()) + " Subpages found");
						}
					}
				} catch (Exception e) {
					if (WebsiteTools.DEBUG) {System.out.println("Error with URL: " + urlToCrawl);}
					e.printStackTrace();
					subPages = new LinkedList<String>();
				}
				if (WebsiteTools.DEBUG) {
					System.out.println("getDirectSubPages: ");
					for (String singleURL : subPages) {
						System.out.println(singleURL);
					}
				}
		return subPages;
	}

}
