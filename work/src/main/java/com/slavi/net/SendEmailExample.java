package com.slavi.net;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendEmailExample {
	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.load(new InputStreamReader(SendEmailExample.class.getResourceAsStream("SendEmailExample.properties")));

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						props.getProperty("mail.smtp.user"), 
						props.getProperty("mail.smtp.password"));
			}
		});
		
		String from = props.getProperty("mail.smtp.from");
		String to = from;
		
		MimeMultipart multipart = new MimeMultipart("related");
		
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent("<h1>This is actual message embedded in HTML tags</h1><p>And here goes an <img src=\"cid:image1\"> embedded image.</p>", "text/html; charset=utf-8");
		multipart.addBodyPart(messageBodyPart);
		
		messageBodyPart = new MimeBodyPart(SendEmailExample.class.getResourceAsStream("SendEmailExample.properties"));
		messageBodyPart.setFileName("attachment1.properties");
		multipart.addBodyPart(messageBodyPart);
		
		BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.setColor(Color.RED);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		g.drawString(df.format(new Date()), 5, 40);
		ByteArrayOutputStream fou = new ByteArrayOutputStream();
		ImageIO.write(image, "png", fou);
		fou.close();
		
		messageBodyPart = new MimeBodyPart();
		messageBodyPart.setHeader("Content-ID", "<image1>");
		messageBodyPart.setHeader("Content-Disposition", "inline; filename=\"image1.png\"");
		messageBodyPart.setContent(fou.toByteArray(), "image/png");
		multipart.addBodyPart(messageBodyPart);
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject("Testing Subject");
		//message.setContent("<h1>This is actual message embedded in HTML tags</h1>", "text/html");
		message.setContent(multipart);
		
		Transport.send(message);

		System.out.println("Done.");
	}
}
