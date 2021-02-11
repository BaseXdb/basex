package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.build.*;
import org.basex.io.*;
import org.basex.query.value.type.*;
import org.junit.jupiter.api.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SignatureTest extends SandboxTest {
  /**
   * Tests the validity of all function signatures.
   * @throws Exception exception
   */
  @Test public void signatures() throws Exception {
    context.openDB(MemBuilder.build(new IOContent("<a/>")));
    for(final FuncDefinition fd : Functions.DEFINITIONS) check(fd);
  }

  /**
   * Checks if the specified function correctly handles its argument types,
   * and returns the function name.
   * @param fd function signature
   * types are supported.
   */
  private static void check(final FuncDefinition fd) {
    final String desc = fd.toString(), name = desc.replaceAll("\\(.*", "");

    // check that there are enough argument names
    final String[] names = fd.names();
    final int min = fd.minMax[0], max = fd.minMax[1];
    assertEquals(names.length, max == Integer.MAX_VALUE ? min : max, fd + Arrays.toString(names));
    // all variable names must be distinct
    final Set<String> set = new HashSet<>(Arrays.asList(names));
    assertEquals(names.length, set.size(), "Duplicate argument names: " + fd);
    // var-arg functions must have a number at the end
    if(max == Integer.MAX_VALUE) assertTrue(names[names.length - 1].matches(".*\\d+$"));

    // test too few, too many, and wrong argument types
    for(int al = Math.max(min - 1, 0); al <= max + 1; al++) {
      final boolean in = al >= min && al <= max;
      final StringBuilder qu = new StringBuilder(name + '(');
      int any = 0;
      for(int p = 0; p < al; p++) {
        if(p != 0) qu.append(", ");
        if(in) {
          // test arguments
          if(fd.params[p].type == AtomType.STRING) {
            qu.append((char) (48 + p));
          } else { // any type (skip test)
            qu.append("'").append((char) (65 + p)).append("'");
            if(SeqType.STRING_O.instanceOf(fd.params[p])) any++;
          }
        } else {
          // test wrong number of arguments
          qu.append("'x'");
        }
      }
      // skip test if all types are arbitrary
      if((min > 0 || al != 0) && (any == 0 || any != al)) {
        final String query = qu.append(')').toString();
        // wrong types: XPTY0004, FORG0006, FODC0002, BXDB0001, BXDB0004, FORG0001
        if(in) error(query, INVTYPE_X_X_X, NONUMBER_X_X, INVFUNCITEM_X_X, ZEROFUNCS_X_X, NODOC_X,
            BINARY_X, STRBIN_X_X, WHICHRES_X, DB_NODE_X, MAP_X_X, FUNCARITY_X_X, FUNCCAST_X_X);
        // wrong number of arguments: XPST0017
        else error(query, FUNCARITY_X_X_X);
      }
    }
  }
}
