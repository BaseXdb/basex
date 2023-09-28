package org.basex.query.func;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.build.*;
import org.basex.io.*;
import org.basex.query.value.type.*;
import org.junit.jupiter.api.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FunctionArgsTest extends SandboxTest {
  /**
   * Tests the validity of all function signatures.
   * @throws Exception exception
   */
  @Test public void signatures() throws Exception {
    context.openDB(MemBuilder.build(new IOContent("<a/>")));
    for(final FuncDefinition fd : Functions.DEFINITIONS) run(fd);
  }

  /**
   * Runs the specified functions with wrong arguments.
   * @param fd function signature
   * types are supported.
   */
  private static void run(final FuncDefinition fd) {
    final String desc = fd.toString(), name = desc.replaceAll("\\(.*", "");
    final int min = fd.minMax[0], max = fd.variadic() ? min - 2 : fd.minMax[1];

    // test too few, too many, and wrong argument types
    for(int al = Math.max(min - 1, 0); al <= max + 1; al++) {
      final boolean in = al >= min && al <= max;
      final StringBuilder qu = new StringBuilder(name + '(');
      int any = 0;
      for(int t = 0; t < al; t++) {
        if(t != 0) qu.append(", ");
        if(in) {
          // test arguments
          if(fd.types[t].type == AtomType.STRING) {
            qu.append((char) (48 + t));
          } else { // any type (skip test)
            qu.append("'").append((char) (65 + t)).append("'");
            if(SeqType.STRING_O.instanceOf(fd.types[t])) any++;
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
        if(in) error(query, INVCONVERT_X_X_X, NONUMBER_X_X, INVFUNCITEM_X_X, NODOC_X,
            BINARY_X, STRBIN_X_X, WHICHRES_X, DB_NODE_X, MAP_X_X, FUNCCAST_X_X);
        // wrong number of arguments: XPST0017
        else error(query, INVNARGS_X_X_X);
      }
    }
  }
}
