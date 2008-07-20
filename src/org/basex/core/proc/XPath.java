package org.basex.core.proc;

import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.query.xpath.XPathProcessor;

/**
 * Evaluates the 'xpath' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XPath extends AQuery {
  /**
   * Constructor.
   * @param q query
   */
  public XPath(final String q) {
    super(DATAREF | PRINTING, q);
  }
  
  @Override
  protected boolean exec() {
    return query(XPathProcessor.class, args[0]);
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    out(o, context.data().meta.chop);
  }
}
