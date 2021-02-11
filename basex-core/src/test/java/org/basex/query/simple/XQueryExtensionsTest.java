package org.basex.query.simple;

import org.basex.query.*;

/**
 * XQuery extensions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQueryExtensionsTest extends QueryTest {
  static {
    queries = new Object[][] {
      { "Ternary 1", strings("ok"), "() ?? 'fail' !! 'ok'" },
      { "Ternary 2", integers(2), "0??1!!2" },
      { "Ternary 3", integers(1), "2 ?? 1 !! 0" },
      { "Ternary 4", "(1,2) ?? 'no-sequences' !! 'allowed'" },
      { "Ternary 5", integers(3), "1 ?? (2 ?? 3 !! 4) !! 5" },
      { "Ternary 6", integers(3), "1 ?? (2 ?? 3 !! 4) !! 5" },
      { "Ternary 7", integers(3), "1 ?? 2 ?? 3 !! 4 !! 5" },

      { "Elvis 1", integers(1), "() ?: 1" },
      { "Elvis 2", integers(0), "0 ?: 1" },
      { "Elvis 3", strings(""), "'' ?: '!'" },
      { "Elvis 4", integers(1), "(() ?: 1) ?: 2" },
      { "Elvis 5", integers(1), "() ?: (1 ?: 2)" },
      { "Elvis 6", integers(1), "() ?: 1 ?: 2" },

      { "IfWithoutElse 1", integers(2), "if(1) then 2" },
      { "IfWithoutElse 2", integers(3), "if(1) then if(2) then 3" },
      { "IfWithoutElse 3", integers(2), "if(1) then 2 else if(3) then 4" },

      { "IfWithoutElse 4", empty(), "if(()) then 2" },
      { "IfWithoutElse 5", empty(), "if(()) then if(2) then 3" },
      { "IfWithoutElse 6", integers(4), "if(()) then 2 else if(3) then 4" },

      {}
    };
  }
}
