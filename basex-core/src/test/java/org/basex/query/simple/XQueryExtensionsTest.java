package org.basex.query.simple;

import org.basex.query.*;

/**
 * XQuery extensions.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class XQueryExtensionsTest extends QueryTest {
  static {
    queries = new Object[][] {
      { "IfWithoutElse 1", integers(2), "if(1) then 2" },
      { "IfWithoutElse 2", integers(3), "if(1) then if(2) then 3" },
      { "IfWithoutElse 3", integers(2), "if(1) then 2 else if(3) then 4" },

      { "IfWithoutElse 4", emptySequence(), "if(()) then 2" },
      { "IfWithoutElse 5", emptySequence(), "if(()) then if(2) then 3" },
      { "IfWithoutElse 6", integers(4), "if(()) then 2 else if(3) then 4" },

      {}
    };
  }
}
