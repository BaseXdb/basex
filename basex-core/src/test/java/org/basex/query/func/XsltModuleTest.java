package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the XSLT Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XsltModuleTest extends SandboxTest {
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
    query(func.args(doc, ' ' + style, " map { 't': '1' }"), "<X>1</X>");
    query(func.args(doc, ' ' + style, " map { 't' : text { '1' } }"), "<X>1</X>");

    // catalog manager (via option declaration); requires resolver in lib/ directory
    final String dir = "src/test/resources/catalog/";
    query("declare option db:catfile '" + dir + "catalog.xml';" +
        func.args(" <dummy/>", dir + "document.xsl"),
        "<x>X</x>");
    query("declare option db:catfile '" + dir + "catalog.xml';" +
        func.args(" <dummy/>", " doc('" + dir + "document.xsl')"),
        "<x>X</x>");

    // catalog manager (via pragma); requires resolver in lib/ directory
    query("(# db:catfile " + dir + "catalog.xml #) { " +
        func.args(" <dummy/>", dir + "document.xsl") + " }",
        "<x>X</x>");

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
    query(func.args(doc, ' ' + style, " map { 't': '1' }"), 1);
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
