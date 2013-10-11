package org.velobg.imageStitch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class JoinSofiaOupImage {

	int gTierCount;

	int gImageWidth;
	int gImageHeight;
	int gTileSize;
	
	int gTierTileCount[];
	int gTileCountWidth[];
	int gTileCountHeight[];
	int gTierWidth[];
	int gTierHeight[];
		
	URL baseUrl;
	
	URL getImageUrl(int tier, int x, int y) throws MalformedURLException {
		int theOffset = (y * gTileCountWidth[tier]) + x;
		int theTier = 0;
		while (theTier < tier) {
			theOffset = theOffset + gTierTileCount[theTier];
			theTier++;
		}
		int theCurrentOffsetChunk = theOffset / 256;
		return new URL(baseUrl, "TileGroup" + theCurrentOffsetChunk + "/" + tier + "-" + x + "-" + y + ".jpg");
	}
	
	void doIt(String urlStr) throws Exception {
		urlStr = urlStr.trim();
		String urlPrefix = "http://sofia-agk.com/esoft/planove/displayImage.php?folder=";
		if (!urlStr.startsWith(urlPrefix))
			throw new Exception("URL should start with '" + urlPrefix + "'");
		baseUrl = new URL("http://sofia-agk.com/esoft/planove/" + urlStr.substring(urlPrefix.length()));
		URL xmlUrl = new URL(baseUrl, "ImageProperties.xml");
		
		InputStream is = xmlUrl.openStream();
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = builder.build(is);
		is.close();
		Element xmlRoot = doc.getRootElement();
		
		gImageWidth = Integer.parseInt(xmlRoot.getAttributeValue("WIDTH"));
		gImageHeight = Integer.parseInt(xmlRoot.getAttributeValue("HEIGHT"));
		gTileSize = Integer.parseInt(xmlRoot.getAttributeValue("TILESIZE"));

		int tempWidth = gImageWidth;
		int tempHeight = gImageHeight;

		// the algorythm in buildPyramid
		gTierCount = 1;
		while ((tempWidth > gTileSize) || (tempHeight > gTileSize)) {
			// if (pyramidType == "Div2") {
			tempWidth = tempWidth / 2;
			tempHeight = tempHeight / 2;
			gTierCount++;
		}
		
		gTierTileCount = new int[gTierCount];
		gTileCountWidth = new int[gTierCount];
		gTileCountHeight = new int[gTierCount];
		gTierWidth = new int[gTierCount];
		gTierHeight = new int[gTierCount];

		tempWidth = gImageWidth;
		tempHeight = gImageHeight;

		int j = gTierCount - 1;
		while (j >= 0) {
			gTileCountWidth[j] = (int) Math.ceil((double) tempWidth / gTileSize);
			gTileCountHeight[j] = (int) Math.ceil((double) tempHeight / gTileSize);
			gTierTileCount[j] = gTileCountWidth[j] * gTileCountHeight[j];
			gTierWidth[j] = tempWidth;
			gTierHeight[j] = tempHeight;
			// if (pyramidType == "Div2") {
			tempWidth = tempWidth / 2;
			tempHeight = tempHeight / 2;
			j--;
		}
		
		for (int tier = 0; tier < gTierCount; tier++) {
			BufferedImage img = new BufferedImage(gTierWidth[tier], gTierHeight[tier], BufferedImage.TYPE_BYTE_INDEXED);
			Graphics g = img.getGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, gTierWidth[tier], gTierHeight[tier]);
			for (int x = 0; x < gTileCountWidth[tier]; x++) {
				for (int y = 0; y < gTileCountHeight[tier]; y++) {
					URL url = getImageUrl(tier, x, y);
					try {
						BufferedImage bi = ImageIO.read(url);
						g.drawImage(bi, x * gTileSize, y * gTileSize, null);
					} catch (Exception e) {
						System.err.println("Error reading image " + url + ". Image skipped");
					}
				}
			}
			File fou = new File("tier" + tier + ".jpg");
			ImageIO.write(img, "jpg", fou);
			System.out.println("Written image " + fou.getAbsolutePath());
		}
		System.out.println("Done.");
	}

	String exampleURL = "http://sofia-agk.com/esoft/planove/displayImage.php?folder=podrobni/2013/IPUR_r-n_Triadica_r-n_Krasno_selo/";
	
	void ui() throws Exception {
		JFrame frame = new JFrame("Сваляне на чертежи от сайта на sofia-agk.com");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		ImageIcon icon = new ImageIcon(getClass().getResource("velobg_org_logo.png"));
		JLabel l = new JLabel("<html>Програмата за сваляне на чертежи-мозайка от сайта на sofia-agk.com и подреждането им в един цял чертеж.</html>", icon, SwingConstants.TRAILING);
		p.add(l, c);
		l = new JLabel("<html>URL адрес, от който да се свали чертежа</html>");
		c.gridwidth = 1;
		c.gridy = 1;
		p.add(l, c);
		
		JTextArea urlText = new JTextArea(exampleURL);
		urlText.setPreferredSize(new Dimension(50, 20));
		c.gridx = 1;
		p.add(urlText, c);
		
		
		
		frame.add(p);
		frame.pack();
		frame.setSize(600,400);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) throws Exception {
		//new JoinSofiaOupImage().doIt("http://sofia-agk.com/esoft/planove/displayImage.php?folder=podrobni/2013/IPUR_r-n_Triadica_r-n_Krasno_selo/");
		new JoinSofiaOupImage().ui();
	}
}
