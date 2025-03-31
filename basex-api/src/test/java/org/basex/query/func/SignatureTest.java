package org.basex.query.func;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SignatureTest extends SandboxTest {
  /**
   * Tests the validity of all function signatures.
   */
  @Test public void signatures() {
    for(final FuncDefinition fd : Functions.BUILT_IN.values()) check(fd);
  }

  /**
   * Checks if the specified function correctly handles its argument types,
   * and returns the function name.
   * @param fd function signature
   * types are supported.
   */
  private static void check(final FuncDefinition fd) {
    // check the general syntax of the description syntax
    final String string = fd.toString();
    assertTrue(string.matches("^.+\\(.*\\)$"), "Invalid syntax: " + string);

    final String name = string.replaceAll("\\(.*", "");
    String params = string.replaceAll("^.*\\(|\\)$", "");
    assertTrue(name.matches("^[a-z]+:[-a-zA-Z\\d]+$"), "Invalid function name: " + string);
    if(params.contains("[")) {
      assertTrue(params.matches("^.*\\[[^\\]]+\\]$"), "Invalid optional parameters: " + string);
      params = params.replaceAll("\\[|\\]", "");
    }
    final String[] list = params.split(",");
    final boolean variadic = fd.variadic();
    boolean dots = false;
    if(!list[0].isEmpty()) {
      for(final String param : list) {
        assertFalse(dots, "Variadic parameter must be last one: " + string);
        assertTrue(param.matches("^[a-z\\d][-a-z\\d]*(\\.\\.\\.)?$"),
            "Invalid parameter name: " + string);
        dots = param.endsWith("...");
      }
    }
    assertEquals(variadic, dots, "Variadic function? " + string);

    // check that there are enough argument names
    final QNm[] names = fd.params;
    final int min = fd.minMax[0], max = fd.minMax[1];
    assertEquals(names.length, variadic ? min + 1 : max,
      "Wrong number of argument names: " + fd + Arrays.toString(names));

    // all variable names must be distinct
    final Set<QNm> set = new HashSet<>(Arrays.asList(names));
    assertEquals(names.length, set.size(), "Duplicate argument names: " + fd);

    /* higher-order functions must have HOF flag
    final Checks<SeqType> hof = arg -> arg.type.instanceOf(SeqType.FUNCTION) &&
        !arg.type.instanceOf(SeqType.MAP) && !arg.type.instanceOf(SeqType.ARRAY);
    if(hof.any(fd.types) && !fd.has(Flag.HOF)) System.err.println(fd);
    */
  }
}
