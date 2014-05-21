package org.basex.tests.w3c;

import org.basex.core.*;
import org.basex.util.*;

/**
 * XQuery Test Suite wrapper.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XQTS extends W3CTS {
  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQTS(args).run();
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public XQTS(final String[] args) {
    super(args, Util.className(XQTS.class));
    context.options.set(MainOptions.XQUERY3, false);
  }
}
