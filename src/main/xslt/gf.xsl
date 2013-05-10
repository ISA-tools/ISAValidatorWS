<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pom="http://maven.apache.org/POM/4.0.0" version="1.0" >

          <xsl:output method="xml" indent="yes" />

			<xsl:template match="/">
<xsl:apply-templates/>
</xsl:template>			
			
          <xsl:template match="pom:dependency[pom:groupId='com.sun.jersey' or pom:groupId='com.sun.xml.bind' or pom:groupId='javax.servlet']/pom:scope[text()!=test]">
          		<scope>provided</scope>
          </xsl:template>

          <xsl:template match="pom:dependency[pom:groupId='com.sun.jersey'  or pom:groupId='com.sun.xml.bind' or pom:groupId='javax.servlet']">
            	<xsl:copy>
              	<xsl:apply-templates/>
              	<xsl:if test="count(pom:scope)=0">
              		<scope>provided</scope>
              	</xsl:if>
            	</xsl:copy>
          </xsl:template>

          <xsl:template match="comment()">
            <xsl:comment><xsl:value-of select="."/></xsl:comment>
          </xsl:template>

          <xsl:template match="*">
            	<xsl:copy>
              	<xsl:apply-templates/>
            	</xsl:copy>
          </xsl:template>

     </xsl:stylesheet> 
