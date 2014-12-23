package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.junit.Assert.*;

import java.util.*;

import org.basex.build.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.type.*;
import org.junit.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class SignatureTest extends AdvancedQueryTest {
  /**
   * Tests the validity of all function signatures.
   * @throws Exception exception
   */
  @Test
  public void signatures() throws Exception {
    context.openDB(MemBuilder.build(new IOContent("<a/>")));
    for(final Function f : Function.values()) check(f);
  }

  /**
   * Checks if the specified function correctly handles its argument types,
   * and returns the function name.
   * @param def function definition
   * types are supported.
   */
  private static void check(final Function def) {
    final String desc = def.toString();
    final String name = desc.replaceAll("\\(.*", "");

    // check that there are enough argument names
    final String[] names = def.names();
    assertTrue(def + Arrays.toString(names),
        names.length == (def.max == Integer.MAX_VALUE ? def.min : def.max));
    // all variable names must be distinct
    final Set<String> set = new HashSet<>(Arrays.asList(names));
    assertEquals("Duplicate argument names: " + def, names.length, set.size());
    // var-arg functions must have a number at the end
    if(def.max == Integer.MAX_VALUE) assertTrue(names[names.length - 1].matches(".*\\d+$"));

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
            if(SeqType.STR.instanceOf(def.args[a])) any++;
          }
        } else {
          // test wrong number of arguments
          qu.append("'x'");
        }
      }
      // skip test if all types are arbitrary
      if((def.min > 0 || al != 0) && (any == 0 || any != al)) {
        final String query = qu.append(')').toString();
        // wrong types: XPTY0004, FORG0006, FODC0002, BXDB0001, BXDB0004
        if(in) error(query, INVCAST_X_X_X, NONUMBER_X_X, INVFUNCITEM_X, STRNOD_X_X,
            ELMMAP_X_X_X, BINARY_X, STRBIN_X_X, WHICHRES_X, BXDB_NODB_X_X,
            BXDB_INDEX_X);
        // wrong number of arguments: XPST0017
        else error(query, FUNCARGS_X_X_X);
      }
    }
  }
}
