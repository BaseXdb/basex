package org.basex.test.w3c;

import org.basex.data.Nodes;
import org.basex.query.QueryContext;

/**
 * XQuery Test Suite wrapper.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XQTS extends W3CTS {
  /**
   * Main method of the test class.
   * @param args command-line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQTS().init(args);
  }

  /**
   * Constructor.
   */
  public XQTS() {
    super("XQTS");
  }

  @Override
  void init(final Nodes root) { }

  @Override
  void parse(final QueryContext qctx, final Nodes root) { }
}
