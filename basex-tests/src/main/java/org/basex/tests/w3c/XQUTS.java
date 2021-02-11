package org.basex.tests.w3c;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery Update Test Suite wrapper.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQUTS extends W3CTS {
  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    new XQUTS(args).run();
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public XQUTS(final String[] args) {
    super(args, Util.className(XQUTS.class));
    context.options.set(MainOptions.FORCECREATE, true);
  }

  @Override
  protected Value states(final Item root) throws QueryException {
    return nodes("*:state", root);
  }

  @Override
  protected boolean updating() {
    return true;
  }
}
