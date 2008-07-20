package org.basex.core.proc;

import java.io.IOException;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;
import org.basex.query.xquery.XQueryProcessor;

/**
 * Evaluates the 'xquery' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQuery extends AQuery {
  /**
   * Constructor.
   * @param q query
   */
  public XQuery(final String q) {
    super(PRINTING, q);
  }

  @Override
  protected boolean exec() {
    return query(XQueryProcessor.class, args[0] == null ? "" : args[0]);
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    out(o, Prop.xqformat);
  }
}
