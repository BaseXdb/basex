package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.*;
import org.basex.build.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.junit.jupiter.api.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FunctionArgsTest extends SandboxTest {
  /**
   * Tests the validity of all function signatures.
   * @throws Exception exception
   */
  @Test public void signatures() throws Exception {
    context.openDB(MemBuilder.build(new IOContent("<a/>")));
    for(final FuncDefinition fd : Functions.BUILT_IN.values()) run(fd);
  }

  /** Checks if functions with mutable fields override the copy method. */
  @Test public void copy() {
    // runtime caches that are rebuilt on demand
    final Set<String> caches = Set.of("Docs.queryInput", "FnInvisibleXml.generator",
        "FtThesaurus.recent", "ParseFn.input");
    final TreeSet<String> missing = new TreeSet<>();
    for(final FuncDefinition fd : Functions.BUILT_IN.values()) {
      for(Class<?> c = fd.get(null).getClass(); c != StandardFunc.class; c = c.getSuperclass()) {
        try {
          c.getDeclaredMethod("copy", CompileContext.class, IntObjectMap.class);
          continue;
        } catch(final NoSuchMethodException ex) {
          // check fields
        }
        for(final Field field : c.getDeclaredFields()) {
          final int mod = field.getModifiers();
          final String name = c.getSimpleName() + '.' + field.getName();
          if(!Modifier.isStatic(mod) && !Modifier.isFinal(mod) && !field.isSynthetic() &&
              !caches.contains(name)) missing.add(name);
        }
      }
    }
    assertTrue(missing.isEmpty(), "State is not copied: " + missing);
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
          if(fd.types[t].type == BasicType.STRING && t < 10) {
            qu.append((char) (48 + t));
          } else { // any type (skip test)
            qu.append("'").append((char) (65 + t)).append("'");
            // strings are implicitly cast to xs:anyURI
            if(Types.STRING_O.instanceOf(fd.types[t]) ||
               Types.ANY_URI_O.instanceOf(fd.types[t])) any++;
          }
        } else {
          // test wrong number of arguments
          qu.append("'x'");
        }
      }
      // skip test if all types are arbitrary
      if((min > 0 || al != 0) && (any == 0 || any != al)) {
        final String query = qu.append(')').toString();
        if(in) error(query, EXP_FOUND_X_X, INVTYPE_X, NONUMBER_X_X, BINARY_X, STRBIN_X_X,
            FUNCCAST_X_X, RESWHICH_X, DB_COMPACT_X, DB_NODE_X, NODOC_X, PATHNODE_X_X_X, CLIENT_ID_X,
            SQL_ID1_X, SQL_ID2_X);
        // wrong number of arguments: XPST0017
        else error(query, al < min ? PARAMMISSING_X_X : INVNARGS_X_X);
      }
    }
  }
}
