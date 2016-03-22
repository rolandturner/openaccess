<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:import href="/usr/share/sgml/docbook/xsl-stylesheets/html/chunk.xsl"/>

    <xsl:param name="html.stylesheet" select="'manual.css'"/>
    <xsl:param name="use.id.as.filename" select="'1'"/>
    <xsl:param name="root.filename" select="'contents'"/>
    <xsl:param name="chunk.section.depth" select="'0'"/>
    <xsl:param name="section.autolabel" select="'1'"/>
    <xsl:param name="section.label.includes.component.label" select="'1'"/>
    <xsl:param name="generate.toc" select="'book toc'"/>
    <xsl:param name="toc.section.depth" select="'2'"/>

    <xsl:template match="/">

        <!--  Chunk the document : Note root chunk will be called index -->
        <xsl:apply-imports/>

        <!--  Create the frame master for this document -->
        <xsl:document href="index.html" method="html">
            <html>
                <frameset cols="30%,*">
                    <frame src="contents.html" name="contents" scrolling="auto"
                        />
                    <frame src="intro.html" name="body" scrolling="auto"/>
                </frameset>
            </html>
        </xsl:document>

    </xsl:template>

    <!-- Make sure all links target the body frame -->
    <xsl:template name="user.head.content">
        <base target="body"/>
    </xsl:template>

    <xsl:template match="bookinfo/title/inlinegraphic">
    </xsl:template>

    <xsl:template name="header.navigation">
        <xsl:param name="prev" select="/foo"/>
        <xsl:param name="next" select="/foo"/>
        <xsl:variable name="home" select="/*[1]"/>
        <xsl:variable name="up" select="parent::*"/>

        <xsl:if test="count($up)>0">
            <div class="navheader">
                <table width="100%" summary="Navigation header">
                    <tr>
                        <th colspan="3" align="center">
                            <xsl:apply-templates select="." mode="object.title.markup"/>
                        </th>
                    </tr>
                    <tr>
                        <td width="40%" align="left">
                            <xsl:if test="count($prev)>0 and $prev != $home">
                                <a accesskey="p">
                                    <xsl:attribute name="href">
                                        <xsl:call-template name="href.target">
                                            <xsl:with-param name="object" select="$prev"/>
                                        </xsl:call-template>
                                    </xsl:attribute>
                                    <xsl:call-template name="navig.content">
                                        <xsl:with-param name="direction" select="'prev'"/>
                                    </xsl:call-template>
                                </a>
                            </xsl:if>
                            <xsl:text>&#160;</xsl:text>
                        </td>
                        <td width="20%" align="center">
                            <xsl:choose>
                                <xsl:when test="$home != .">
                                    <a accesskey="h">
                                        <xsl:attribute name="href">index.html</xsl:attribute>
                                        <xsl:attribute name="target">_top</xsl:attribute>
                                        <xsl:call-template name="navig.content">
                                            <xsl:with-param name="direction" select="'home'"/>
                                        </xsl:call-template>
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>&#160;</xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td width="40%" align="right">
                            <xsl:text>&#160;</xsl:text>
                            <xsl:if test="count($next)>0">
                                <a accesskey="n">
                                    <xsl:attribute name="href">
                                        <xsl:call-template name="href.target">
                                            <xsl:with-param name="object" select="$next"/>
                                        </xsl:call-template>
                                    </xsl:attribute>
                                    <xsl:call-template name="navig.content">
                                        <xsl:with-param name="direction" select="'next'"/>
                                    </xsl:call-template>
                                </a>
                            </xsl:if>
                        </td>
                    </tr>
                </table>
                <hr/>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="footer.navigation">
        <xsl:param name="prev" select="/foo"/>
        <xsl:param name="next" select="/foo"/>
        <xsl:variable name="home" select="/*[1]"/>
        <xsl:variable name="up" select="parent::*"/>

        <xsl:if test="count($up)>0">
            <div class="navfooter">
                <hr/>
                <table width="100%" summary="Navigation footer">
                    <tr>
                        <td width="40%" align="left">
                            <xsl:if test="count($prev)>0 and $prev != $home">
                                <a accesskey="p">
                                    <xsl:attribute name="href">
                                        <xsl:call-template name="href.target">
                                            <xsl:with-param name="object" select="$prev"/>
                                        </xsl:call-template>
                                    </xsl:attribute>
                                    <xsl:call-template name="navig.content">
                                        <xsl:with-param name="direction" select="'prev'"/>
                                    </xsl:call-template>
                                </a>
                            </xsl:if>
                            <xsl:text>&#160;</xsl:text>
                        </td>
                        <td width="20%" align="center">
                            &#160;
                        </td>
                        <td width="40%" align="right">
                            <xsl:text>&#160;</xsl:text>
                            <xsl:if test="count($next)>0">
                                <a accesskey="n">
                                    <xsl:attribute name="href">
                                        <xsl:call-template name="href.target">
                                            <xsl:with-param name="object" select="$next"/>
                                        </xsl:call-template>
                                    </xsl:attribute>
                                    <xsl:call-template name="navig.content">
                                        <xsl:with-param name="direction" select="'next'"/>
                                    </xsl:call-template>
                                </a>
                            </xsl:if>
                        </td>
                    </tr>

                    <tr>
                        <td width="40%" align="left" valign="top">
                            <xsl:if test="$navig.showtitles != 0">
                                <xsl:apply-templates select="$prev" mode="object.title.markup"/>
                            </xsl:if>
                            <xsl:text>&#160;</xsl:text>
                        </td>
                        <td width="20%" align="center">
                            <xsl:choose>
                                <xsl:when test="$home != .">
                                    <a accesskey="h">
                                        <xsl:attribute name="href">index.html</xsl:attribute>
                                        <xsl:attribute name="target">_top</xsl:attribute>
                                        <xsl:call-template name="navig.content">
                                            <xsl:with-param name="direction" select="'home'"/>
                                        </xsl:call-template>
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>&#160;</xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td width="40%" align="right" valign="top">
                            <xsl:text>&#160;</xsl:text>
                            <xsl:if test="$navig.showtitles != 0">
                                <xsl:apply-templates select="$next" mode="object.title.markup"/>
                            </xsl:if>
                        </td>
                    </tr>
                </table>
            </div>
            <!--xsl:apply-templates select="copyright" mode="titlepage.mode"/-->
            <p class="copyright" align="right">
                Versant Open Access @JDO.VERSION@ (@JDO.VERSION.DATE@) - Copyright(c) 2004
                <a href="http://www.versant.com">Versant Corporation</a>
            </p>
        </xsl:if>
    </xsl:template>


</xsl:stylesheet>

