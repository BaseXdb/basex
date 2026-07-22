package org.basex.query.func.xslt;

import javax.xml.transform.sax.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.query.value.node.*;

/**
 * Assembles a document node from the events of an XSL transformation. Compared to the
 * serialized result, the document may also have text children, and it is not affected
 * by the serialization method of the stylesheet.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XsltResult extends SAXResult {
  /** Database builder. */
  private final MemBuilder builder;

  /**
   * Constructor.
   * @param uri document URI
   * @param options main options
   */
  public XsltResult(final byte[] uri, final MainOptions options) {
    builder = new MemBuilder(org.basex.build.Parser.emptyParser(options)).init();
    final SAXHandler handler = new SAXHandler(builder, uri);
    setHandler(handler);
    setLexicalHandler(handler);
  }

  /**
   * Returns the assembled document node.
   * @return document node
   */
  public DBNode node() {
    return new DBNode(builder.finish());
  }
}
