package org.basex.tests.w3c;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * XQuery Update Test Suite wrapper.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XQUTS extends W3CTS {
  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQUTS().run(args);
  }

  /**
   * Constructor.
   */
  public XQUTS() {
    super(Util.className(XQUTS.class));
    context.options.set(MainOptions.FORCECREATE, true);
    context.options.set(MainOptions.ONLYUPDATES, true);
  }

  @Override
  protected Nodes states(final Nodes root) throws QueryException {
    return nodes("*:state", root);
  }

  @Override
  protected boolean updating() {
    return true;
  }
}
