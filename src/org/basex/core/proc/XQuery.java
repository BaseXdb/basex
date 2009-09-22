package org.basex.core.proc;

import org.basex.core.Commands.Cmd;

/**
 * Evaluates the 'xquery' command and processes an XQuery request.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XQuery extends AQuery {
  /**
   * Default constructor.
   * @param query query to process
   */
  public XQuery(final String query) {
    super(PRINTING, query);
  }

  @Override
  protected boolean exec() {
    return query(args[0]);
  }

  // [LK] this method could be overwritten to check if the process
  //   performs updates
  @Override
  public boolean updating() {
    return super.updating();
  }

  @Override
  public String toString() {
    return Cmd.XQUERY + " " + args[0];
  }
}
