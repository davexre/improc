package com.slavi.net;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class TestApacheHttpClient {

	void doIt1() throws Exception {
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier());
		ssh.connect("localhost");
		ssh.authPublickey(System.getProperty("user.name"));
		SFTPClient sftp = ssh.newSFTPClient();
		List<RemoteResourceInfo> ls = sftp.ls(".");
		sftp.close();
		
		ssh.disconnect();
		for (RemoteResourceInfo i : ls)
			System.out.println(i);
		
	}
	
	void doIt3() throws Exception {
		Document jsoupDoc;
		jsoupDoc = Jsoup.parse(getClass().getResourceAsStream("TestApacheHttpClient.xml"), null, "", Parser.xmlParser());
		System.out.println(jsoupDoc);
		System.out.println("--------------");
		System.out.println(jsoupDoc.select("configuration badtag"));
	}
	
	void doIt() throws Exception {
		String url = "https://www.google.com";
		url = "http://localhost:8080/manager/html";
		Document jsoupDoc;
		
		if (url.startsWith("file://")) {
			jsoupDoc = Jsoup.parse(new URL(url).openStream(), null, "");
		} else {
			BasicCookieStore cookieStore = new BasicCookieStore();
			HttpHost target = new HttpHost("localhost", 8080, "http");
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials("asd", "dsa"));
			try (CloseableHttpClient httpclient = HttpClients.custom()
					.setDefaultCookieStore(cookieStore)
					.setDefaultCredentialsProvider(credsProvider)
					.build();
				) {
				
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(target, basicAuth);
				HttpContext ctx = new BasicHttpContext();
				HttpClientContext clictx = HttpClientContext.adapt(ctx);

				HttpGet httpget = new HttpGet(new URI(url));
				CloseableHttpResponse response1 = httpclient.execute(httpget);
				System.out.println("Status line: " + response1.getStatusLine());
				HttpEntity entity = response1.getEntity();
				
				ContentType contentType = ContentType.get(entity);
				Charset charset = contentType == null ? null : contentType.getCharset();
				String charsetName = charset == null ? null : charset.name();
	
				InputStream is = entity.getContent();
				jsoupDoc = Jsoup.parse(is, charsetName, url);
				IOUtils.closeQuietly(is);
				
				//EntityUtils.consume(entity);
				//IOUtils.copy(entity.getContent(), System.out);
	/*			System.out.println("Initial set of cookies:");
				List<Cookie> cookies = cookieStore.getCookies();
				for (int i = 0; i < cookies.size(); i++) {
					System.out.println("- " + cookies.get(i).toString());
				}*/
				response1.close();
			}
		}
		System.out.println(jsoupDoc);
	}

	public static void main(String[] args) throws Exception {
		new TestApacheHttpClient().doIt();
		System.out.println("Done.");
	}
}
