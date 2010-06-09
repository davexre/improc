package com.test;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class TestGetINetFile {
	public static void main(String[] args) throws Exception {
		String proxySettings = "HTTP:proxy:8080:::localhost";
		String settings[] = proxySettings == null ? null : proxySettings.split(":");
		int count = settings == null ? 0 : settings.length;
		String type = 0 < count ? settings[0] : "";
		String host = 1 < count ? settings[1] : "";
		String port = 2 < count ? settings[2] : "";
		String user = 3 < count ? settings[3] : "";
		String pass = 4 < count ? settings[4] : "";
		String noProxyList = 5 < count ? settings[5] : "";
		
		Properties props = System.getProperties();
		
		props.remove("http.proxyHost");
		props.remove("http.proxyPort");
		props.remove("http.proxyUser");
		props.remove("http.proxyPassword");
		props.remove("http.http.nonProxyHosts");
		
		props.remove("https.proxyHost");
		props.remove("https.proxyPort");
		props.remove("https.http.nonProxyHosts");
		
		props.remove("ftp.proxyHost");
		props.remove("ftp.proxyPort");
		props.remove("ftp.nonProxyHosts");
		
		props.remove("socksProxyHost");
		props.remove("socksProxyPort");
		props.remove("java.net.socks.username");
		props.remove("java.net.socks.password");

		if ("HTTP".equalsIgnoreCase(type)) {
			if (!"".equals(host)) {
				props.put("http.proxyHost", host);
				props.put("https.proxyHost", host);
				props.put("ftp.proxyHost", host);
			}
			if (!"".equals(port)) {
				props.put("http.proxyPort", port);
				props.put("https.proxyPort", port);
				props.put("ftp.proxyPort", port);
			}
			if (!"".equals(user)) {
				props.put("http.proxyUser", user);
			}
			if (!"".equals(pass)) {
				props.put("http.proxyPassword", pass);
			}
			if (!"".equals(noProxyList)) {
				props.put("http.nonProxyHosts", noProxyList);
				props.put("https.nonProxyHosts", noProxyList);
				props.put("ftp.nonProxyHosts", noProxyList);
			}
		} else if ("SOCKS".equalsIgnoreCase(type)) {
			if (!"".equals(host)) {
				props.put("socksProxyHost", host);
			}
			if (!"".equals(port)) {
				props.put("socksProxyPort", port);
			}
			if (!"".equals(user)) {
				props.put("java.net.socks.username", user);
			}
			if (!"".equals(pass)) {
				props.put("java.net.socks.password", pass);
			}
		}
		
		URL url = new URL("http://buttons.googlesyndication.com/fusion/add.gif");
		InputStream is = url.openStream();
		System.out.println(is.available());
		is.close();
	}
}
