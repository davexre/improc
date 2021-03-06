package com.slavi.various;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ListFilesServlet extends HttpServlet {
	private static final long serialVersionUID = -4207621041307093318L;

	private Logger logger = Logger.getLogger(ListFilesServlet.class);

	private String rootStr = null;
	private File rootFile = null;
	private URI rootURI = null;
	private HashSet<String> usersAllowed = new HashSet<String>();

	public void init(ServletConfig config) throws ServletException {
		// TODO: The parameres bellow should be specified somewhere else, not in the web.xml
		String rootDirStr = config.getInitParameter("rootDir");
		String usersAllowedStr = config.getInitParameter("usersAllowed");
		doInitialize(rootDirStr, usersAllowedStr);
	}
  
	void doInitialize(String rootDirStr, String usersAllowedStr) throws ServletException {
		try {
			rootStr = rootDirStr;
			rootFile = new File(rootStr).getCanonicalFile();
			rootStr = rootFile.getPath();
			rootURI = rootFile.toURI();
		} catch (IOException e) {
			logger.error("Could not initialize servlet: Init-param rootDir=" + rootStr, e);
			throw new ServletException("Could not initialize servlet: Init-param rootDir=" + rootStr, e);
		}
		if (usersAllowedStr == null)
			usersAllowedStr = "";
		String users[] = usersAllowedStr.split(",");
		usersAllowed.clear();
		for (String user : users) {
			String userStr = user.trim();
			if (!"".equals(userStr))
				usersAllowed.add(userStr);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Initialized with root dir:" + rootStr + ", allowed users:" + usersAllowedStr);
		}
	}  
  
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Principal principal = req.getUserPrincipal();
		String userName = null;
		if (principal != null) {
			userName = principal.getName();
		}
		if ((userName == null) || (!usersAllowed.contains(userName))) {
			if (!usersAllowed.contains("Guest")) {
				if (logger.isDebugEnabled()) {
					logger.debug("Denied access to user " + userName);
				}
				res.sendError(403); // Forbidden
				return;
			}
		}

		File reqFile;
		String getStr = req.getParameter("get");
		if (getStr != null) {
			reqFile = new File(rootFile, getStr);
		} else {
			reqFile = rootFile;
		}
		if (!reqFile.exists()) {
			logger.warn("An attempt to get a non existing file. Returning to root. The requested file is "
					+ reqFile.getCanonicalPath());
			reqFile = rootFile;
		}
		if (!reqFile.getCanonicalPath().startsWith(rootStr)) {
			logger.warn("An attempt to get a file outside specified root. Returning to root. The requested file is "
					+ reqFile.getCanonicalPath());
			reqFile = rootFile;
		}
		reqFile = reqFile.getCanonicalFile();

		if (logger.isInfoEnabled()) {
			logger.info("User " + userName + " requested file/folder " + reqFile);
		}

		if (reqFile.isDirectory()) {
			SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			File[] files = reqFile.listFiles();
			Arrays.sort(files, new Comparator() {
				public int compare(Object o1, Object o2) {
					File f1 = (File) o1;
					File f2 = (File) o2;
					if (f1.isDirectory() && !f2.isDirectory()) {
						return -1;
					} else if (!f1.isDirectory() && f2.isDirectory()) {
						return 1;
					} else {
						try {
							return f1.getCanonicalPath().compareTo(f2.getCanonicalPath());
						} catch (IOException e) {
							return 0;
						}
					}
				}
			});

			res.setContentType("text/html");
			PrintWriter out = res.getWriter();
			int rowsCount = 0;
			out.println("<html>");
			out.println("<head>");
			URI uri = rootURI.relativize(reqFile.toURI());
			String str = uri.getPath();
			out.println("<title>Directory Listing For /" + str + "</title>");
			out.println("<body>");
			out.println("<style><!-- TH {text-align:left;} .R{text-align:right;} .TR_EVEN {background-color:#ffffff;} .TR_ODD {background-color:#eeeeee;} A {color:black;} A.name {color:black;} --></style>");
			out.println("<h1>Directory Listing For /" + str + "</h1>");
			out.println("<hr/>");
			out.println("<table width='100%'>");
			out.println("<tr>");
			out.println("<th>Filename</th>");
			out.println("<th class='R'>Size</th>");
			out.println("<th>Last Modified</th>");
			out.println("</tr>");
			uri = reqFile.toURI();
			if (rootURI.compareTo(uri) != 0) {
				rowsCount++;
				uri = rootURI.relativize(reqFile.getParentFile().toURI());
				str = uri.getPath();
				out.println("<tr class='" + (rowsCount % 2 == 0 ? "TR_EVEN" : "TR_ODD") + "'>");
				out.println("<td><tt><a href='?get=" + str + "' target='_top'>[&nbsp;..&nbsp;]</a></tt></td>");
				out.println("<td></td>");
				out.println("<td></td>");
				out.println("</tr>");
			}
			for (File f : files) {
				rowsCount++;
				uri = rootURI.relativize(f.toURI());
				String href = uri.getPath();
				String displayName = f.isDirectory() ? "[&nbsp;" + f.getName() + "&nbsp]" : f.getName();
				out.println("<tr class='" + (rowsCount % 2 == 0 ? "TR_EVEN" : "TR_ODD") + "'>");
				out.println("<td><tt><a href='?get=" + href + "' target='_top'>" + displayName + "</a></tt></td>");
				out.print("<td class='R'><tt>");
				out.print(f.isDirectory() ? "&lt;Folder&gt;" : String.format(Locale.US, "%1$,d", f.length()));
				out.println("</tt></td>");
				out.println("<td><tt>" + df.format(new Date(f.lastModified())) + "</tt></td>");
				out.println("</tr>");
			}
			out.println("</table>");
			out.println("</body>");
			out.println("</head>");
			out.println("</html>");
		} else {
			res.setContentType("application/x-download");
			res.setHeader("Content-Disposition", "attachment; filename=" + reqFile.getName());
			OutputStream out = res.getOutputStream();
			FileInputStream fin = new FileInputStream(reqFile);
			byte buf[] = new byte[256];
			int len;
			while ((len = fin.read(buf)) >= 0) {
				out.write(buf, 0, len);
			}
			fin.close();
			out.flush();
		}
	}
}
