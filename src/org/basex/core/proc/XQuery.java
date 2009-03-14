package org.basex.core.proc;

import java.io.IOException;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'xquery' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XQuery extends AQuery {
  /**
   * Constructor.
   * @param query query
   */
  public XQuery(final String query) {
    super(PRINTING, query);
  }

  @Override
  protected boolean exec() {
    return query(args[0] == null ? "" : args[0]);
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    out(o, Prop.xqformat);
  }
}
