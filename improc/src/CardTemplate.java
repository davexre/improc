import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

public class CardTemplate {

	// HorizontalAlignment
	public static final int HALEFT = 0;
	public static final int HACENTER = 1;
	public static final int HARIGHT = 2;

	// VerticalAlignment
	public static final int VATOP = 3;
	public static final int VACENTER = 1;
	public static final int VABOTTOM = 4;

	BufferedImage background;

	String backgroundFile;

	TextBox title;

	TextBox text;

	Font titleFont;

	Font bgTextFont;

	Font enTextFont;

	int imgX;

	int imgY;

	public class TextBox {
		Color color;

		int x;

		int y;

		int width;

		int vAlign; // VerticalAlignment

		int hAlign; // HorizontalAlignment 

		public TextBox() {
			this(Color.WHITE, 0, 0, 100, VATOP,
					HALEFT);
		}

		public TextBox(Color color, int x, int y, int width,
				int vAlign, int hAlign) {
			super();
			this.color = color;
			this.x = x;
			this.y = y;
			this.width = width;
			this.vAlign = vAlign;
			this.hAlign = hAlign;
		}

		public final String VerticalAlignmentToString(int align) {
			switch (align) {
			case CardTemplate.VABOTTOM: return "BOTTOM";
			case CardTemplate.VACENTER: return "CENTER";
			}
			return "TOP";
		}
		
		public final int VerticalAlignmentFromString(String align) {
			if (align.equalsIgnoreCase("BOTTOM")) return CardTemplate.VABOTTOM;
			if (align.equalsIgnoreCase("CENTER")) return CardTemplate.VACENTER;
			return CardTemplate.VATOP;
		}
		
		public final String HorizontalAlignmentToString(int align) {
			switch (align) {
			case CardTemplate.HARIGHT: return "RIGHT";
			case CardTemplate.HACENTER: return "CENTER";
			}
			return "LEFT";
		}
		
		public final int HorizontalAlignmentFromString(String align) {
			if (align.equalsIgnoreCase("RIGHT")) return CardTemplate.HARIGHT;
			if (align.equalsIgnoreCase("CENTER")) return CardTemplate.HACENTER;
			return CardTemplate.HALEFT;
		}
		
		/**
		 * Converts the object to a formatted string and can later be restored
		 * with a call to {@link #fromString(String)}
		 * 
		 * @see #fromString(String)
		 */
		public String toString() {
			return Integer.toHexString(color.getRGB()) + "," + x + "," + y
					+ "," + width + "," + VerticalAlignmentToString(vAlign) + ","
					+ HorizontalAlignmentToString(hAlign);
		}

		/**
		 * Loads the object fields with the values specified in the formatted
		 * string. The format of the string is: <br>
		 * <tt>
		 * [TEXTBOX] = [COLOR text color],[LOCATION origin],[INT box width],[VERTICAL_ALIGNMENT],[HORIZONTAL_ALIGNMENT]
		 *
		 * where:
		 * [COLOR] = [STRING hex color integer] 
		 * [LOCATION] = [INT x],[INT y] 
		 * [VERTICAL_ALIGNMENT] = [STRING Vertical alignment "TOP" | "CENTER" | "BOTTOM"] 
		 * [HORIZONTAL_ALIGNMENT] = [STRING Horizontal alignment "LEFT" | "CENTER" | "RIGHT"]
		 * 
		 * Example:
		 * ffffff,350,290,600,CENTER,CENTER
		 * </tt>
		 */
		public void fromString(String str) {
			StringTokenizer st = new StringTokenizer(str, ",");
			try {
				int aColor = Integer.parseInt(st.nextToken(), 16);
				int aX = Integer.parseInt(st.nextToken());
				int aY = Integer.parseInt(st.nextToken());
				int aWidth = Integer.parseInt(st.nextToken());
				int aVAlign = VerticalAlignmentFromString(st.nextToken());
				int aHAlign = HorizontalAlignmentFromString(st.nextToken());
				color = new Color(aColor);
				x = aX;
				y = aY;
				width = aWidth;
				vAlign = aVAlign;
				hAlign = aHAlign;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Draws sequence of paragraphs (each paragraph in its own font) on a
	 * Graphics2D object
	 * 
	 * @param g
	 *            Writable Graphics2D object
	 * @param tb
	 *            TextBox definition
	 * @param params
	 *            Sequence of (String, Font) objects
	 */
	private static void drawText(Graphics2D g, TextBox tb, Object[] params) {
		if (params == null)
			return;
		if (params.length % 2 != 0) {
			throw new Error("Incorrect number of parameters");
		}

		float height = 0;
		ArrayList<TextLayout> layouts = new ArrayList<TextLayout>();

		for (int i = 0; i < (params.length >> 1); i++) {
			int index = i << 1;
			String text = (String) (params[index]);
			Font font = (Font) (params[index + 1]);

			AttributedString as = new AttributedString(text);
			as.addAttribute(TextAttribute.FONT, font);

			FontRenderContext frc = g.getFontRenderContext();
			LineBreakMeasurer measurer = new LineBreakMeasurer(
					as.getIterator(), frc);
			while (measurer.getPosition() < text.length()) {
				TextLayout tl = measurer.nextLayout(tb.width);
				layouts.add(tl);
				height += tl.getAscent();
			}
		}

		float posY;
		if (tb.vAlign == VATOP)
			posY = tb.y - height;
		else if (tb.vAlign == VACENTER)
			posY = tb.y + height / (float) 2.0;
		else
			// if (vAlign == VerticalAlignment.BOTTOM)
			posY = tb.y;

		// posX, posY = bottom left corner of the text rectangle
		g.setColor(tb.color);
		for (int i = layouts.size() - 1; i >= 0; i--) {
			TextLayout tl = (TextLayout) layouts.get(i);
			float posX;
			if (tb.hAlign == HARIGHT)
				posX = (float) (tb.x + tb.width - tl.getBounds().getWidth());
			else if (tb.hAlign == HACENTER)
				posX = (float) (tb.x - tl.getBounds().getWidth() / 2.0);
			else
				// if (hAlign == HorizontalAlignment.LEFT)
				posX = tb.x;
			tl.draw(g, posX, posY - tl.getDescent());
			posY -= tl.getAscent();
		}
	}

	/**
	 * Produces an image for printing of a personal card.
	 * 
	 * @return Returns the image of the personal card.
	 */
	public BufferedImage makeCardImage(BufferedImage picture, String name_bg,
			String name_en, String department_bg,
			String position_bg, String position_en) {

		BufferedImage output = new BufferedImage(background.getWidth(),
				background.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) output.getGraphics();
		g.drawImage(background, 0, 0, Color.white, null);

		if (picture != null)
			g.drawImage(picture, imgX, imgY, null);

		drawText(g, title, new Object[] { department_bg, titleFont });
		drawText(g, text, new Object[] { name_bg, bgTextFont, name_en, enTextFont, " ",
				enTextFont, position_bg, bgTextFont, position_en,
				enTextFont});
		return output;
	}

	/**
	 * Converts the object to a formatted string and can later be restored
	 * with a call to {@link #FontToString(Font)}
	 * 
	 * @see #FontToString(Font)
	 */
	public String FontToString(Font font) {
		String style;
		switch (font.getStyle()) {
		case Font.BOLD: {
			style = "BOLD";
			break;
		}
		case Font.ITALIC: {
			style = "ITALIC";
			break;
		}
		case Font.BOLD + Font.ITALIC: {
			style = "BOLDITALIC";
			break;
		}
		default: {
			style = "PLAIN";
			break;
		} // Font.PLAIN:
		}
		return font.getFontName() + "," + style + "," + font.getSize();
	}

	/**
	 * Creates a new Font object from the values specified in the formatted
	 * string. The format of the string is: <br>
	 * <tt>
	 * [FONT] = [STRING font name],[FONTSTYLE],[INT font size]
	 * 
	 * where:  
	 * [FONTSTYLE] = [STRING font style "PLAIN" | "BOLD" | "ITALIC" | "BOLDITALIC"]
	 * 
	 * Example:
	 * UnvCyr,BOLD,36
	 * </tt> 
	 * 
	 * @param str
	 */
	public Font FontFromString(String str) {
		StringTokenizer st = new StringTokenizer(str, ",");
		try {
			String fontName = st.nextToken();
			String style = st.nextToken();
			int fontStyle;
			if (style.equalsIgnoreCase("BOLD"))
				fontStyle = Font.BOLD;
			else if (style.equalsIgnoreCase("ITALIC"))
				fontStyle = Font.ITALIC;
			else if (style.equalsIgnoreCase("BOLDITALIC"))
				fontStyle = Font.BOLD + Font.ITALIC;
			else
				// if (style.equalsIgnoreCase("PLAIN"))
				fontStyle = Font.PLAIN;
			int fontSize = Integer.parseInt(st.nextToken());
			return new Font(fontName, fontStyle, fontSize);
		} catch (Exception e) {
			e.printStackTrace();
			return new Font("Courier", Font.PLAIN, 12);
		}
	}

	/**
	 * Converts the object to a formatted string and can later be restored
	 * with a call to {@link #fromString(String, String)}
	 * 
	 * @see #fromString(String, String)
	 */
	public String toString() {
		return backgroundFile + ":" + imgX + "," + imgY + ":"
				+ title.toString() + ":" + text.toString() + ":"
				+ FontToString(titleFont) + ":" + FontToString(bgTextFont)
				+ ":" + FontToString(enTextFont);
	}

	/**
	 * Loads the object fields with the values specified in the formatted
	 * string. The format of the string is: <br>
	 * <tt> 
	 * [CARDTEMPLATE] = [IMAGE background]:[LOCATION picture]:[TEXTBOX title]:
	 * 				[TEXTBOX text]:[FONT title]:[FONT bg text]:[FONT en text]
	 * 
	 * [IMAGE] = [STRING relative path to image file]
	 * 
	 * Example:
	 * WEB-INF/FIB_blue.bmp:170,380:ffffff,350,290,600,CENTER,CENTER:ffffff,170,985,400,BOTTOM,LEFT:UnvCyr,BOLD,36:UnvCyr,BOLD,40:UnvCyr,ITALIC,33
	 * WEB-INF/FIB_orange.bmp:170,380:ffffff,350,290,600,CENTER,CENTER:ffffff,170,985,400,BOTTOM,LEFT:UnvCyr,BOLD,36:UnvCyr,BOLD,40:UnvCyr,ITALIC,33
	 * </tt>
	 * 
	 * @param pathPrefix
	 *            Specifies the root folder to use when searching for the
	 *            background image
	 * @see TextBox#fromString(String)
	 * @see #FontFromString(String)
	 */
	public void fromString(String str, String pathPrefix) {
		StringTokenizer st = new StringTokenizer(str, ":");
		try {
			backgroundFile = st.nextToken();
			background = ImageIO.read(new File(pathPrefix + backgroundFile));
			StringTokenizer location = new StringTokenizer(st.nextToken(), ",");
			imgX = Integer.parseInt(location.nextToken());
			imgY = Integer.parseInt(location.nextToken());
			title = new TextBox();
			title.fromString(st.nextToken());
			text = new TextBox();
			text.fromString(st.nextToken());
			titleFont = FontFromString(st.nextToken());
			bgTextFont = FontFromString(st.nextToken());
			enTextFont = FontFromString(st.nextToken());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
