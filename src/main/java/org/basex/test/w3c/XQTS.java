package org.basex.test.w3c;

import org.basex.core.Prop;

/**
 * XQuery Test Suite wrapper.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class XQTS extends W3CTS {
  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQTS().run(args);
  }

  /**
   * Constructor.
   */
  public XQTS() {
    super("XQTS");
    context.prop.set(Prop.XQUERY11, false);
  }
}
