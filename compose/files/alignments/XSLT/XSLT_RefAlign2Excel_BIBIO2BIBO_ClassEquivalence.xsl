<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs owl rdf ns2"
    version="2.0"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:ns2="http://knowledgeweb.semanticweb.org/heterogeneity/alignment#"
    xmlns="urn:schemas-microsoft-com:office:spreadsheet"
    xmlns:x="urn:schemas-microsoft-com:office:excel"
    xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
    
    <!--Indents the results to get the proper xml formatting-->
    <xsl:output media-type="text/xml" version="1.0" encoding="UTF-8" indent="yes"
        use-character-maps="owl"/>
    <xsl:strip-space elements="*"/>
    
    <xsl:character-map name="owl">
        <xsl:output-character character="&amp;" string="&amp;"/>
    </xsl:character-map>
    
    <xsl:template match="/">
        
        <xsl:apply-templates select="/rdf:RDF/ns1:Alignment" xmlns:ns1="http://knowledgeweb.semanticweb.org/heterogeneity/alignment"/>
        
    </xsl:template>

    <xsl:template match="ns1:Alignment" xmlns:ns1="http://knowledgeweb.semanticweb.org/heterogeneity/alignment">
        


        <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
            xmlns:x="urn:schemas-microsoft-com:office:excel"
            xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
            <Worksheet ss:Name="Ark1">
                <Table x:FullColumns="1" x:FullRows="1" >
                    
                    <Row>
                        <Cell><Data ss:Type="String">Class 1</Data></Cell>
                        <Cell><Data ss:Type="String">Class 2</Data></Cell>
                        <Cell><Data ss:Type="String">Relation</Data></Cell>
                        <Cell><Data ss:Type="String">Measure</Data></Cell>
                    </Row>

                    <xsl:for-each select="ns1:map">

                        <xsl:variable name="class1" select="substring-after(ns1:Cell/ns1:entity1/@rdf:resource, '#')"/>
                        <xsl:variable name="class2" select="substring-after(ns1:Cell/ns1:entity2/@rdf:resource, 'bibo/')"/>
                        
                        <Row>
                            <Cell><Data ss:Type="String"><xsl:value-of select="$class1"/></Data></Cell>
                            <Cell><Data ss:Type="String"><xsl:value-of select="$class2"/></Data></Cell>
                            <Cell><Data ss:Type="String"><xsl:value-of select="ns1:Cell/ns1:relation"/></Data></Cell>
                            <Cell><Data ss:Type="String"><xsl:value-of select="ns1:Cell/ns1:measure"/></Data></Cell>
                        </Row>
                        
                    </xsl:for-each>
                    
                </Table>
            </Worksheet>
        </Workbook>
    </xsl:template>      
      
</xsl:stylesheet>