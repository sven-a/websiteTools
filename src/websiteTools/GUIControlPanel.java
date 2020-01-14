package websiteTools;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class GUIControlPanel extends JPanel {
	final static long serialVersionUID = 655459731833313039L;
	JButton siteMapButton;
	JButton metaInfoButton;
	JButton errorSearchButton;
	JButton stopButton;

	JCheckBox recursiveBox;
	JCheckBox onlyErrorsBox;
	JCheckBox noRedirectsBox;

	GUIControlPanel() {
		try {
		    UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		 } catch (Exception e) {
		            e.printStackTrace();
		 }
		// Control panel
		siteMapButton = new JButton("Sitemap");
		metaInfoButton = new JButton("Meta-Info");
		stopButton = new JButton("stop");
		stopButton.setEnabled(false);
		// Stop button will only be enabled, when crawler is running

		
		//TODO: add Button for link search
		JPanel errorSearchPanel = new JPanel();
		errorSearchPanel.setLayout(new BoxLayout(errorSearchPanel, BoxLayout.PAGE_AXIS));
		errorSearchButton = new JButton("search Errors");
		onlyErrorsBox = new JCheckBox("show only pages with errors");
		noRedirectsBox = new JCheckBox("don't list redirects");
		errorSearchButton.setAlignmentX(LEFT_ALIGNMENT);
		onlyErrorsBox.setAlignmentX(LEFT_ALIGNMENT);
		noRedirectsBox.setAlignmentX(LEFT_ALIGNMENT);
		
		errorSearchPanel.add(errorSearchButton);
		errorSearchPanel.add(onlyErrorsBox);
		errorSearchPanel.add(noRedirectsBox);

		JPanel checkBoxesPanel = new JPanel();
		checkBoxesPanel.setLayout(new BoxLayout(checkBoxesPanel, BoxLayout.Y_AXIS));
		recursiveBox = new JCheckBox("recursive Search");
		checkBoxesPanel.add(new JLabel("Crawl subpages"));
		checkBoxesPanel.add(recursiveBox);
	
		this.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.add(siteMapButton);
		this.add(metaInfoButton);
		this.add(errorSearchPanel);
		this.add(stopButton);

		this.add(checkBoxesPanel);
	}

	public void setControlsReady() {
		// deactivate input line, meta-button and runButton, enable stopButton:
		siteMapButton.setEnabled(false);
		metaInfoButton.setEnabled(false);
		errorSearchButton.setEnabled(false);
		stopButton.setEnabled(true);
		recursiveBox.setEnabled(false);
		onlyErrorsBox.setEnabled(false);
		noRedirectsBox.setEnabled(false);
	}

	public void setControlsIdle() {
		// enable input line, meta-button and runButton, enable stopButton:
		siteMapButton.setEnabled(true);
		metaInfoButton.setEnabled(true);
		errorSearchButton.setEnabled(true);
		stopButton.setEnabled(false);

		recursiveBox.setEnabled(true);
		onlyErrorsBox.setEnabled(true);
		noRedirectsBox.setEnabled(true);
	}
}
