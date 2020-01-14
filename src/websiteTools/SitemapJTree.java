package websiteTools;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class SitemapJTree {

	JTree sitemapTree;
		
	SitemapJTree(JTree inputTree) {
		sitemapTree = inputTree;
	}
	SitemapJTree(DefaultMutableTreeNode root) {
		sitemapTree = new JTree(root);
	}
	
	
	public void insertStringAsNode(String urlToInsert) {
		// check if the URL to insert and the root of the tree are equal
		// split off the root String of the URL to insert
		// split up the rest of the String to chunks that end with /
		// for each chunk, check if a corresponding node exists and if not, create it (either with the full URL or just the chunk, the user should be able to select this behavior later on.
		
	}
	
	public boolean contains(String urlToCheck) {
		boolean contains = true;
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) sitemapTree.getModel().getRoot();
		
		// check if the URL to insert and the root of the tree are equal
		if (!urlToCheck.startsWith(currentNode.toString())) {
			contains = false;
		}
		else {
			// split off the root String of the URL to insert
			String restString = urlToCheck;
			restString.replace(currentNode.toString(),"");
			
			// split up the rest of the String to chunks that end with /
			String[] chunks = restString.split("/");
			
			// for each chunk, check if a corresponding node exists
			for (String singleChunk : chunks) {
				if (currentNode.getChildCount() != 0) {
					Enumeration<DefaultMutableTreeNode> children = currentNode.children();
					while (children.hasMoreElements()) {
						if (children.nextElement().toString().equals(singleChunk) ) {
							
							break;
						}
						
					}
 
						
					
					 
				}
			}
			
			
		} 
		
				
				
				
		return contains;
	}
	
	
	
}
