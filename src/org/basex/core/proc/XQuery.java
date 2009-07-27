package org.basex.core.proc;

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
    return query(args[0]);
  }
}
