<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" encoding="utf-8"/>

<xsl:strip-space elements="*" />

<xsl:template match="/child::*">
	<xsl:for-each select="*[1]/child::*">
		<xsl:if test="position() != 1">&#x9;</xsl:if>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="name(.)" />
		<xsl:text>"</xsl:text>
	</xsl:for-each>
	<xsl:text>&#xA;</xsl:text>

	<xsl:for-each select="child::*">
		<xsl:for-each select="child::*">
			<xsl:if test="position() != 1">; </xsl:if>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="normalize-space(.)"/>
			<xsl:text>"</xsl:text>
		</xsl:for-each>
		<xsl:text>&#xA;</xsl:text>
	</xsl:for-each>
</xsl:template>

</xsl:stylesheet>