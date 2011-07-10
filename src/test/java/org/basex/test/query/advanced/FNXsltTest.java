package org.basex.test.query.advanced;

import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.junit.Test;

/**
 * This class tests the XQuery utility functions prefixed with "xslt".
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNXsltTest extends AdvancedQueryTest {
  /**
   * Test method for the xslt:transform() function.
   * @throws QueryException database exception
   */
  @Test
  public void testXSLT() throws QueryException {
    final String fun = check(Function.TRANSFORM);

    final String doc = "<a/>";
    String style = wrap("<xsl:template match='/'><X/></xsl:template>");
    query(fun + "(" + doc + ", " + style + ")", "<X/>");
    query(fun + "('" + doc + "', \"" + style + "\")", "<X/>");

    style = wrap("<xsl:param name='t'/><xsl:template match='/'>" +
      "<X><xsl:value-of select='$t'/></X></xsl:template>");
    final String param =
      "<xslt:parameters><xslt:t>1</xslt:t></xslt:parameters>";

    query(fun + "(" + doc + ", " + style + ", " + param + ")", "<X>1</X>");
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
