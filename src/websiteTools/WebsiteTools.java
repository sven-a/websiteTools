package websiteTools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class WebsiteTools {
	final static String version = "0.9.1";
	final static boolean DEBUG = true;
	
	// TODO: transform these into settings:
	
	static boolean ignoreQueryFragments = false;
	static boolean trailingSlash = true;
	Preferences preferences = new Preferences();

	public static void main(String[] args) {

		WebsiteToolsGUI mygui = new WebsiteToolsGUI("Website Tools v" + version);
		
		KeyListener enterPressed = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					ErrorSearchAction actionOnClick = new ErrorSearchAction(mygui);
					actionOnClick.start();
				}
			}
		};
		
		mygui.statusBar.setEditable(true);
		mygui.statusBar.addKeyListener(enterPressed);
		
		ActionListener clickMetaInfo = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MetaInfoButtonAction actionOnClick = new MetaInfoButtonAction(mygui);
				actionOnClick.start();
			}
		};
		
		ActionListener clickErrorSearch = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: revert to ErrorSearchAction
				//TODO: add Listener for url search button, when implemented
				ErrorSearchAction actionOnClick = new ErrorSearchAction(mygui);
				//SearchLinksToDomain actionOnClick = new SearchLinksToDomain(mygui);
				actionOnClick.start();
			}
		};
		
		ActionListener clickSiteMap = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SiteMapAction actionOnClick = new SiteMapAction(mygui);
				actionOnClick.start();
			}
		};
		
		mygui.controlPanel.errorSearchButton.addActionListener(clickErrorSearch);
		mygui.controlPanel.metaInfoButton.addActionListener(clickMetaInfo);
		mygui.controlPanel.siteMapButton.addActionListener(clickSiteMap);
	}
}
