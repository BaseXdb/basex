package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the XSLT Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XsltModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void init() {
    final Function func = _XSLT_INIT;
    assertTrue(query(func.args()).isEmpty());
  }

  /** Test method. */
  @Test public void processor() {
    final Function func = _XSLT_PROCESSOR;
    assertFalse(query(func.args()).isEmpty());
  }

  /** Test method. */
  @Test public void transform() {
    final Function func = _XSLT_TRANSFORM;
    final String doc = " <a/>";
    String style = wrap("<xsl:template match='/'><X/></xsl:template>");
    query(func.args(doc, ' ' + style), "<X/>");
    query(func.args(doc, style), "<X/>");

    style = wrap("<xsl:param name='t'/><xsl:template match='/'>" +
        "<X><xsl:value-of select='$t'/></X></xsl:template>");
    query(func.args(doc, ' ' + style, " { 't': '1' }"), "<X>1</X>");
    query(func.args(doc, ' ' + style, " { 't' : text { '1' } }"), "<X>1</X>");
  }

  /** Test method. */
  @Test public void transformText() {
    final Function func = _XSLT_TRANSFORM_TEXT;
    final String doc = " <a/>";
    String style = wrap("<xsl:template match='/'>" +
        "<xsl:output omit-xml-declaration='yes'/>1</xsl:template>");
    query(func.args(doc, ' ' + style), 1);
    query(func.args(doc, style), 1);

    style = wrap("<xsl:param name='t'/><xsl:output omit-xml-declaration='yes'/>" +
      "<xsl:template match='/'><xsl:value-of select='$t'/></xsl:template>");
    query(func.args(doc, ' ' + style, " { 't': '1' }"), 1);
  }

  /** Test method. */
  @Test public void transformReport() {
    final Function func = _XSLT_TRANSFORM_REPORT;
    final String doc = " <a/>";
    final String style = wrap("<xsl:template match='/'>" +
        "<xsl:output omit-xml-declaration='yes'/>1</xsl:template>");
    query(func.args(doc, ' ' + style) + "?result", 1);
    query(func.args(doc, ' ' + style) + "?error => exists()", false);
    query(func.args(doc, ' ' + wrap("")) + "?error => exists()", false);
    query(func.args(doc, ' ' + wrap("<xsl:x/>")) + "?error => exists()", true);
  }

  /** Test method. */
  @Test public void version() {
    final Function func = _XSLT_VERSION;
    assertFalse(query(func.args()).isEmpty());
  }

  /**
   * Wraps the specified string with an XSLT header and footer.
   * @param content content string
   * @return wrapped string
   */
  private static String wrap(final String content) {
    return "<xsl:stylesheet version='1.0'" +
      " xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>" + content +
      "</xsl:stylesheet>";
  }
}
