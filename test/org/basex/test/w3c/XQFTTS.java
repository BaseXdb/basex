package org.basex.test.w3c;

import org.basex.core.Prop;

/**
 * XQuery Full-text Test Suite Wrapper.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XQFTTS extends W3CTS {
   /**
   * Main method of the test class.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQFTTS().init(args);
  }

  /**
   * Constructor.
   */
  public XQFTTS() {
    super("XQFTTS");
    context.prop.set(Prop.FTINDEX, true);
  }
}
