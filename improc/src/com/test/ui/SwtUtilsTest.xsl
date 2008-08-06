<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
	<html>
	<body>
	<xsl:apply-templates />
	</body>
	</html>
</xsl:template>

<xsl:template name="ScalePoints" match="ScalePoints">
	<h2>Scale points</h2>
	<table border="1">
	<tr>
		<th>No</th>
		<th>imgX</th>
		<th>imgY</th>
		<th>doubleX</th>
		<th>doubleY</th>
		<th>level</th>
		<th>adjS</th>
		<th>kpScale</th>
		<th>degree</th>
	</tr>
	<xsl:for-each select="ScalePoint">
		<tr>
		<th><xsl:value-of select="position()"/></th>
		<td><xsl:value-of select="imgX/attribute::v"/></td>
		<td><xsl:value-of select="imgY/attribute::v"/></td>
		<td><xsl:value-of select="doubleX/attribute::v"/></td>
		<td><xsl:value-of select="doubleY/attribute::v"/></td>
		<td><xsl:value-of select="level/attribute::v"/></td>
		<td><xsl:value-of select="adjS/attribute::v"/></td>
		<td><xsl:value-of select="kpScale/attribute::v"/></td>
		<td><xsl:value-of select="degree/attribute::v"/></td>
		</tr>
	</xsl:for-each>
	</table>
</xsl:template>

<xsl:template match="matrix">
	<h2>Matrix</h2>
	<xsl:call-template name="matrix"/>
</xsl:template>

<xsl:template match="diagonalmatrix">
	<h2>Diagonal matrix</h2>
	<xsl:call-template name="matrix"/>
</xsl:template>

<xsl:template name="matrix">
	<table border="1">
	<xsl:for-each select="row">
		<tr>
		<xsl:for-each select="item">
			<td><xsl:value-of select="@v"/></td>
		</xsl:for-each>
		</tr>
	</xsl:for-each>
	</table>
</xsl:template>

<xsl:template name="valued-children">
	<table border="0">
	<xsl:for-each select="*[@v]">
		<tr><td align="right">
		<xsl:value-of select="local-name(.)"/>=
		</td><td>
		<xsl:value-of select="attribute::v"/>
		</td></tr>
	</xsl:for-each>
	</table>
</xsl:template>

<xsl:template name="ScalePoint">
	<h2>Scale point</h2>
	<xsl:call-template name="valued-children"/>
</xsl:template>

<xsl:template name="Statistics">
	<h2>Statistics results</h2>
	<xsl:call-template name="valued-children"/>
</xsl:template>

<xsl:template name="MatrixCompareResult">
	<h2>Matrix Compare Result</h2>
	<xsl:call-template name="valued-children"/>
</xsl:template>

<xsl:template name="LeastSquareAdjust">
	<h2>Least Square Adjustment Results</h2>
	<xsl:call-template name="valued-children"/>
	<xsl:for-each select="NormalMatrix">
		<h3>Normal matrix</h3>
		<xsl:call-template name="matrix"/>
	</xsl:for-each>
	<xsl:for-each select="APL">
		<h3>APL</h3>
		<xsl:call-template name="matrix"/>
	</xsl:for-each>
	<xsl:for-each select="Unknown">
		<h3>Unknown</h3>
		<xsl:call-template name="matrix"/>
	</xsl:for-each>
</xsl:template>

<xsl:template name="AffineTransformer">
	<h2>Affine Transformation parameters</h2>
	<xsl:for-each select="Origin">
		<h3>Origin</h3>
		<xsl:call-template name="matrix"/>
	</xsl:for-each>
	<xsl:for-each select="Coefs">
		<h3>Coefficients</h3>
		<xsl:call-template name="matrix"/>
	</xsl:for-each>
</xsl:template>

<xsl:template name="PolynomialTransformer">
	<h2>Polynomial Transformation parameters</h2>
	<xsl:call-template name="valued-children"/>
	<xsl:for-each select="Origin">
		<h3>Origin</h3>
		<xsl:call-template name="matrix"/>
	</xsl:for-each>
	<xsl:for-each select="Coefs">
		<h3>Coefficients</h3>
		<xsl:call-template name="matrix"/>
	</xsl:for-each>
	<xsl:for-each select="Powers">
		<h3>Powers</h3>
		<xsl:call-template name="matrix"/>
	</xsl:for-each>
</xsl:template>

</xsl:stylesheet>
