<%@page import="example.webapp.logging.JUL"%>
<%@page import="example.webapp.logging.JCL"%>
<%@page import="example.webapp.logging.SystemOut"%>
<%@page import="example.webapp.logging.SLF4J"%>
<%@page import="example.webapp.logging.Log4J"%>
<%@page import="example.webapp.logging.JUL"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c"			uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn"			uri="http://java.sun.com/jsp/jstl/functions" %>
<c:url var="baseUrl" value="/" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="refresh" content="5; url=${baseUrl}" />
</head>
<body>

<%
	JUL.doLog();
	JCL.doLog();
	Log4J.doLog();
	SLF4J.doLog();
	SystemOut.doLog();
%>
	<p>All loggers used.</p>
</body>
</html>
