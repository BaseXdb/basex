<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" />
  <xsl:template match="/">
    <x><xsl:value-of select="document('document.xml')"/></x>
  </xsl:template>
</xsl:stylesheet>
