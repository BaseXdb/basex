package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;
import org.basex.query.func.Variable;
import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * This class tests the XQuery utility functions prefixed with "xslt".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNXsltTest extends AdvancedQueryTest {
  /**
   * Test method for the xslt:processor() function.
   */
  @Test
  public void xsltProcessor() {
    assertTrue(!query(Variable.XSLTPROC.toString()).isEmpty());
  }

  /**
   * Test method for the xslt:version() function.
   */
  @Test
  public void xsltVersion() {
    assertTrue(!query(Variable.XSLTVERSION.toString()).isEmpty());
  }

  /**
   * Test method for the xslt:transform() function.
   */
  @Test
  public void xsltTransform() {
    check(_UTIL_TRANSFORM);

    final String doc = "<a/>";
    String style = wrap("<xsl:template match='/'><X/></xsl:template>");
    query(_UTIL_TRANSFORM.args(doc, style), "<X/>");
    query(_UTIL_TRANSFORM.args(doc, "\"" + style + "\""), "<X/>");

    style = wrap("<xsl:param name='t'/><xsl:template match='/'>" +
      "<X><xsl:value-of select='$t'/></X></xsl:template>");
    final String param =
      "<xslt:parameters><xslt:t>1</xslt:t></xslt:parameters>";

    query(_UTIL_TRANSFORM.args(doc, style, param), "<X>1</X>");
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
