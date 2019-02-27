package websiteTools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class WebsiteTools {
	final static String version = "0.9";
	final static boolean DEBUG = true;
	static boolean ignoreQueryFragments = true;

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
		
		ActionListener clickRun = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ErrorSearchAction actionOnClick = new ErrorSearchAction(mygui);
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
		
		mygui.errorSearchButton.addActionListener(clickRun);
		mygui.metaInfoButton.addActionListener(clickMetaInfo);
		mygui.siteMapButton.addActionListener(clickSiteMap);
	}
}
