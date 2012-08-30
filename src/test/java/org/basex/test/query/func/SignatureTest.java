package org.basex.test.query.func;

import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class SignatureTest extends AdvancedQueryTest {
  /**
   * Tests the validity of all function signatures.
   * @throws Exception exception
   */
  @Test
  public void signatures() throws Exception {
    context.openDB(CreateDB.mainMem(new IOContent("<a/>"), context));
    context.data().meta.name = "X";
    for(final Function f : Function.values()) check(f);
  }

  /**
   * Checks if the specified function correctly handles its argument types,
   * and returns the function name.
   * @param def function definition
   * types are supported.
   */
  protected static void check(final Function def) {
    final String desc = def.toString();
    final String name = desc.replaceAll("\\(.*", "");

    // test too few, too many, and wrong argument types
    for(int al = Math.max(def.min - 1, 0); al <= def.max + 1; al++) {
      final boolean in = al >= def.min && al <= def.max;
      final StringBuilder qu = new StringBuilder(name + '(');
      int any = 0;
      for(int a = 0; a < al; a++) {
        if(a != 0) qu.append(", ");
        if(in) {
          // test arguments
          if(def.args[a].type == AtomType.STR) {
            qu.append('1');
          } else { // any type (skip test)
            qu.append("'X'");
            if(SeqType.STR.instance(def.args[a])) any++;
          }
        } else {
          // test wrong number of arguments
          qu.append("'x'");
        }
      }
      // skip test if all types are arbitrary
      if((def.min > 0 || al != 0) && (any == 0 || any != al)) {
        final String query = qu.append(')').toString();
        if(in) error(query, Err.XPTYPE, Err.BXDB_NODB, Err.BXDB_OPEN, Err.FUNCMP,
            Err.BXDB_INDEX, Err.NODEERR);
        else error(query, Err.XPARGS);
      }
    }
  }
}
