package com.test.image;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class GenerateImageWithAlpha {

	private static BufferedImage resizeImage(BufferedImage source, int destWidth, int destHeight) {
		int srcWidth = source.getWidth();
		int srcHeight = source.getHeight();

		double scale = Math.max(
				(double) destWidth / srcWidth, 
				(double) destHeight / srcHeight);
		BufferedImage result = new BufferedImage(destWidth, destHeight,
				BufferedImage.TYPE_INT_ARGB);

		BufferedImage clipped = source.getSubimage(
				(int) ((srcWidth - (destWidth / scale)) / 2),
				(int) ((srcHeight - (destHeight / scale)) / 2),
				(int) (destWidth / scale),
				(int) (destHeight / scale));
		new AffineTransformOp(
				AffineTransform.getScaleInstance(scale, scale),
				AffineTransformOp.TYPE_BILINEAR).filter(clipped, result);
		clipped.flush();
		clipped = null;
		return result;
	}

	private static void maskImage(BufferedImage theImage, BufferedImage mask) {
		int destWidth = mask.getWidth();
		int destHeight = mask.getHeight();

		for (int j = destHeight - 1; j >= 0; j--)
			for (int i = destWidth - 1; i >= 0; i--) {
				int alpha = mask.getRGB(i, j);
				alpha = 255 - ((alpha & 0x000000FF)
						+ ((alpha & 0x0000FF00) >> 8) + ((alpha & 0x00FF0000) >> 16)) / 3;
				int pixel = theImage.getRGB(i, j);
				pixel = (pixel & 0x00FFFFFF) | (alpha << 24);
				theImage.setRGB(i, j, pixel);
			}
	}
	
	public static ByteArrayOutputStream processImage(InputStream is, BufferedImage mask) {
		ImageInputStream iis = new MemoryCacheImageInputStream(is);

		// Find the image reader, read the image and scale it to the
		// necessary size.
		for (Iterator<ImageReader> i = ImageIO.getImageReaders(iis); i.hasNext();) {
			ImageReader ir = i.next();
			try {
				iis.reset();
				ir.setInput(iis);
				BufferedImage srcImg = ir.read(0);
				BufferedImage destImg = resizeImage(srcImg, mask.getWidth(), mask.getHeight()); 
				maskImage(destImg, mask);

				ImageWriter iw = ImageIO.getImageWritersBySuffix("png").next();
				ByteArrayOutputStream result = new ByteArrayOutputStream();
				ImageOutputStream ios = new MemoryCacheImageOutputStream(result);
				iw.setOutput(ios);
				iw.write(destImg);
				ios.flush();
				ios.close();
				ios = null;
				return result;
			} catch (Exception e) {
				// Complain about the error and try the next ImageReader.
				e.printStackTrace();
			}
		}
		System.err.println("[processImage]: Failed processing an image.");
		return null;
	}

	public static void main1() {
		try {
			BufferedImage backGround = ImageIO.read(new File("C:/output/FIB-v-2.bmp"));
			BufferedImage out = new BufferedImage(backGround.getWidth(), backGround.getHeight(), BufferedImage.TYPE_INT_ARGB); 
			Graphics2D g = (Graphics2D) out.getGraphics();
			String text = "aslfkajshgriuo roeigtu\nhiodufhg sodiu ghsiodu fhgiosutg hsioudfh goisud hgoisud hfgiou doigu shdoiug sdiou gsoidu goisu dhgious hdgious dguios hg";
			
			g.setFont(new Font("Courier", Font.BOLD, 28));
			FontMetrics fm;
			
			// Draw department text
			Rectangle departmentRect = new Rectangle(50,50,150,500);
			
			fm = g.getFontMetrics();
			StringTokenizer st = new StringTokenizer(text, " ");
			String[] txtLines = new String[st.countTokens()];
			int counter = 0;
			while (st.hasMoreTokens())
				txtLines[counter++] = st.nextToken();
			int sumHeight = fm.getHeight() * txtLines.length;
			int offset = (departmentRect.height - sumHeight) / 2;
			for (int i = 0; i < txtLines.length; i++) {
				String s = txtLines[i];
				int tmpWidth = (int) fm.getStringBounds(s, g).getWidth();
				g.drawString(s, 
						departmentRect.x + (departmentRect.width - tmpWidth) / 2, 
						departmentRect.y + offset + (fm.getHeight() + 1) * (i+1) );
			}
			
			
			// Draw Person's name
			String firstName = "Tsfgdsfgsdgsd";
			String familyName = "GfgsdfsdfsdfS";
			String position = "Qsdfsfsdfsfs";
			
			Rectangle nameRect = new Rectangle(50,50,150,500);
			
			fm = g.getFontMetrics();
			int atY = nameRect.y;
			if (((int)fm.getStringBounds(firstName + " " + familyName, g).getWidth()) < nameRect.width) {
				atY += fm.getHeight();
				g.drawString(firstName + " " + familyName, nameRect.x, atY++);
			} else {
				atY += fm.getHeight();
				g.drawString(firstName, nameRect.x, atY++);
				atY += fm.getHeight();
				g.drawString(familyName, nameRect.x, atY++);
			}
			g.setFont(new Font("Courier", Font.ITALIC, 28));
			fm = g.getFontMetrics();
			atY += fm.getHeight();
			g.drawString(firstName + " " + familyName, nameRect.x, atY++);

			g.setFont(new Font("Courier", 0, 28));
			fm = g.getFontMetrics();
			atY += fm.getHeight();
			// empty line
			
			atY += fm.getHeight();
			g.drawString(position, nameRect.x, atY++);

			g.setFont(new Font("Courier", Font.ITALIC, 28));
			fm = g.getFontMetrics();
			atY += fm.getHeight();
			g.drawString(position, nameRect.x, atY++);
			ImageIO.write(out, "png", new File("c:/output/output.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
    public String exportAllImages() {
    	String outDir = "c:/output/";
    	
		Connection conn = null; //Db.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
	    	BufferedImage backGround = ImageIO.read(new File("c:/users/jsf/FIB-v-2.bmp"));
	    	BufferedImage output = new BufferedImage(backGround.getWidth(), backGround.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    	
	    	String aSql = "select s.name name_bg, s.family family_bg, s.name_en, s.family_en, d.name dep_bg, d.name_en dep_en, p.name pos_bg, p.name_en pos_en, s.picture " +
	    		"from fib_personnel s, fib_positions p, fib_departments d " +
	    		"where s.department_id=d.department_id and s.position_id=p.position_id and s.picture is not null";			
			ps = conn.prepareStatement(aSql);
			rs = ps.executeQuery();
			while (rs.next()) {
				String imgName = rs.getString(1);
				byte[] picture = rs.getBytes(2);
				ImageInputStream iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(picture));
				for (Iterator<ImageReader> imgReader = ImageIO.getImageReaders(iis); imgReader.hasNext();) {
					ImageReader ir = imgReader.next();
					try {
						iis.reset();
						ir.setInput(iis);
						BufferedImage pict = ir.read(0);
						
						Graphics g = output.getGraphics();
						g.drawImage(backGround, 0, 0, Color.white, null);
						g.drawImage(pict, 170, 420, null);
						
						FontMetrics fm;
						String fontName = "Arial";						
						
						// Draw department text
						Rectangle departmentRect = new Rectangle(50,240, 600, 100);
						
						g.setFont(new Font(fontName, Font.BOLD, 37));
						fm = g.getFontMetrics();

						String departmentNameBG = rs.getString(5);
						StringTokenizer st = new StringTokenizer(departmentNameBG, "|");
						String[] txtLines = new String[st.countTokens()];
						int counter = 0;
						while (st.hasMoreTokens())
							txtLines[counter++] = st.nextToken();
						int sumHeight = fm.getHeight() * txtLines.length;
						int offset = (departmentRect.height - sumHeight) / 2;
						for (int i = 0; i < txtLines.length; i++) {
							String s = txtLines[i];
							int tmpWidth = (int) fm.getStringBounds(s, g).getWidth();
							g.drawString(s, 
									departmentRect.x + (departmentRect.width - tmpWidth) / 2, 
									departmentRect.y + offset + (fm.getHeight() + 1) * (i+1) );
						}
						
						// Draw Person's name
						String firstNameBG = rs.getString(1);
						String familyNameBG = rs.getString(2);
						String firstNameEN = rs.getString(3);
						String familyNameEN = rs.getString(4);
						String positionBG = rs.getString(7);
						String positionEN = rs.getString(8);
						
						Rectangle nameRect = new Rectangle(170, 770, 600-170, 960-770);
						
						fm = g.getFontMetrics();
						int atY = nameRect.y;
						if (((int)fm.getStringBounds(firstNameBG + " " + familyNameBG, g).getWidth()) < nameRect.width) {
							atY += fm.getHeight();
							g.drawString(firstNameBG + " " + familyNameBG, nameRect.x, atY++);
						} else {
							atY += fm.getHeight();
							g.drawString(firstNameBG, nameRect.x, atY++);
							atY += fm.getHeight();
							g.drawString(familyNameBG, nameRect.x, atY++);
						}
						g.setFont(new Font(fontName, Font.ITALIC, 30));
						fm = g.getFontMetrics();
						atY += fm.getHeight();
						g.drawString(firstNameEN + " " + familyNameEN, nameRect.x, atY++);
			
						g.setFont(new Font(fontName, 0, 30));
						fm = g.getFontMetrics();
						atY += fm.getHeight();
						// empty line
						
						atY += fm.getHeight();
						g.drawString(positionBG, nameRect.x, atY++);
			
						g.setFont(new Font(fontName, Font.ITALIC, 30));
						fm = g.getFontMetrics();
						atY += fm.getHeight();
						g.drawString(positionEN, nameRect.x, atY++);

						ImageIO.write(output, "png", new File(outDir + imgName + ".png"));
						break;
					} catch (Exception e) {
						// Complain about the error and try the next ImageReader.
						e.printStackTrace();
					}
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				rs = null;
				if (ps != null)
					ps.close();
				ps = null;
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	return "";
    }
	
    public static void main(String[] args) {
    	File f = new File("c:/temp/test.txt");
    	System.out.println(f.getParentFile().getParentFile().getParent());
	}
    
//	public static void main2(String[] args) throws Exception {
//		String workDir = "C:/output/";
//		BufferedImage backGround = ImageIO.read(new File(workDir + "FIB-v-2.bmp"));
//		BufferedImage out = new BufferedImage(backGround.getWidth(), backGround.getHeight(),
//				BufferedImage.TYPE_INT_ARGB);
//		Graphics2D g = (Graphics2D)out.getGraphics();
//		String text = "Това е един много, ама наистина много, много дълъг текст, който следва да се пренесе.";
//		
//		Font f = new Font("UnvCyr", Font.BOLD, 36);
//		g.setFont(f);
//		
//		drawText(g, text, 200, 300, 250, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
//		ImageIO.write(out, "png", new File(workDir + "output.png"));
//	}
//
//	 
//	public enum HorizontalAlignment { LEFT, CENTER, RIGHT };
//	
//	public enum VerticalAlignment { TOP, CENTER, BOTTOM };
//	
//	public static void drawText(Graphics2D g, String text, 
//			int atX, int atY, int maxWidth, 
//			HorizontalAlignment hAlign, VerticalAlignment vAlign) {
//		FontRenderContext frc = g.getFontRenderContext();
//		AttributedString as = new AttributedString(text);
//		as.addAttribute(TextAttribute.FONT, g.getFont());
//		LineBreakMeasurer measurer = new LineBreakMeasurer(as.getIterator(), frc);
//		ArrayList<TextLayout> layouts = new ArrayList<TextLayout>();
//		float height = 0;
//		while (measurer.getPosition() < text.length()) {
//			TextLayout tl = measurer.nextLayout(maxWidth);
//			layouts.add(tl);
//			height += tl.getAscent();
//		}
//
//		float posY;
//		if (vAlign == VerticalAlignment.TOP)
//			posY = atY;
//		else if (vAlign == VerticalAlignment.CENTER)
//			posY = atY - height / (float)2.0;
//		else // if (vAlign == VerticalAlignment.BOTTOM)
//			posY = atY - height;
//
//		for (TextLayout tl : layouts) {
//			float posX;
//			if (hAlign == HorizontalAlignment.RIGHT)
//				posX = (float)(atX + maxWidth - tl.getBounds().getWidth());
//			else if (hAlign == HorizontalAlignment.CENTER)
//				posX = (float)(atX + (maxWidth - tl.getBounds().getWidth()) / 2.0);
//			else // if (hAlign == HorizontalAlignment.LEFT)
//				posX = atX;
//			tl.draw(g, posX, posY);
//			posY += tl.getAscent();
//		}
//	}
	
	
//	public static void main(String[] args) {
//		Asd a = new Asd();
//		a.exportAllImages();
//	}
	
	
//	public static void main(String[] args) {
//		try {
//			BufferedImage mask = ImageIO.read(new File("C:/Users/jsf/fib/WEB-INF/mask.png"));
//			String srcDir = "C:/output/snimki/";
//			File files = new File(srcDir);
//			String outDir = "c:/output/snimki.out/";
//			for (String finName : files.list()) {
//				File fin = new File(srcDir + finName);
//				System.out.println("Processing file " + fin.getName());
//				FileInputStream fis = new FileInputStream(fin);
//				FileOutputStream fos = new FileOutputStream(new File(outDir + fin.getName()));
//				fos.write(processImage(fis, mask).toByteArray());
//				fos.close();
//				fis.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
