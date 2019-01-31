<#macro renderTableHead cols>
	<tr>
	<#list cols as c>
		<th>${c}</th>
	</#list>
	</tr>
</#macro>

<#macro renderTableBody rows>
<tbody>
	<#list rows as row>
	<tr>
		<#list row as d>
		<td>${d}</td>
		</#list>
	</tr>
	</#list>
</tbody>
</#macro>

<#macro renderTable rs>
<#list rs.rows>
<table class="table table-sm">
<thead>
	<@renderTableHead cols=rs.cols />
</thead>
	<tbody>
		<#items as row>
		<tr>
			<#list row as d>
			<td>${d}</td>
			</#list>
		</tr>
		</#items>
	</tbody>
	<@renderTableHead cols=rs.cols />
</table>
</#list>
</#macro>
