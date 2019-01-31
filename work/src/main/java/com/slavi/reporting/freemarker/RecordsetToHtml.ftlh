<#import "lib.Table.ftlh" as tbl>
<#-- #<!doctype html> ## Not supported by flying sauser (html-to-pdf) -->
<html>
<head>
<style>
<#include cssFile>
<#include "//com/slavi/reporting/velocity/html2pdf.css">
</style>
</head>
<body>
	<div class="pageheader">
		<table class="table table-borderless"><tr>
			<td class="text-center">--- ${utl.esc(title)} ---</td>
		</tr></table>
	</div>
	<div class="pagefooter">
		<table class="table table-borderless"><tr>
			<td class="text-left">${utl.esc(date)}</td>
			<td class="text-right">Page <span class="pagenumber" /> of <span class="pagecount" /></td>
		</tr></table>
	</div>

	<div class="content container-fluid">
	<p>No data in the first table</p>
	<@tbl.renderTable rs=utl.sqlAsList("select * from countries where REGION_ID = 123") />
	<hr/>
	<@tbl.renderTable rs=utl.sqlAsList("select * from countries") />
	<hr/>
	<@tbl.renderTable rs=utl.sqlAsList("select * from countries") />
	<hr/>
	<@tbl.renderTable rs=utl.sqlAsList("select * from countries") />
	</div>

	<#list items>
		<p>Our latest products:</p>
		<#items as i>${i?index}, ${i?item_parity}, ${i?counter}  -> ${i}<#sep>, </#sep></#items>
	</#list>
</body>
</html>
