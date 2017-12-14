<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="fragments">
    <FindBugsFilter>
      <xsl:apply-templates/>
    </FindBugsFilter>
  </xsl:template>

  <xsl:template match="FindBugsFilter">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*">
    <xsl:copy-of select="."/>
  </xsl:template>
</xsl:stylesheet>
