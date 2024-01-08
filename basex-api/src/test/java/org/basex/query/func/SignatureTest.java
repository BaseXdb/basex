package org.basex.query.func;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class SignatureTest extends SandboxTest {
  /**
   * Tests the validity of all function signatures.
   */
  @Test public void signatures() {
    for(final FuncDefinition fd : Functions.DEFINITIONS) check(fd);
  }

  /**
   * Checks if the specified function correctly handles its argument types,
   * and returns the function name.
   * @param fd function signature
   * types are supported.
   */
  private static void check(final FuncDefinition fd) {
    // check the general syntax of the description syntax
    final String desc = fd.toString();
    assertTrue(desc.matches("^.+\\(.*\\)$"), "Invalid syntax: " + desc);

    final String name = desc.replaceAll("\\(.*", "");
    String params = desc.replaceAll("^.*\\(|\\)$", "");
    assertTrue(name.matches("^[a-z]+:[-a-zA-Z\\d]+$"), "Invalid function name: " + desc);
    if(params.contains("[")) {
      assertTrue(params.matches("^.*\\[[^\\]]+\\]$"), "Invalid optional parameters: " + desc);
      params = params.replaceAll("\\[|\\]", "");
    }
    final String[] list = params.split(",");
    final boolean variadic = fd.variadic();
    boolean dots = false;
    if(!list[0].isEmpty()) {
      for(final String param : list) {
        assertFalse(dots, "Variadic parameter must be last one: " + desc);
        assertTrue(param.matches("^[a-z\\d][-a-z\\d]*(\\.\\.\\.)?$"),
            "Invalid parameter name: " + desc);
        dots = param.endsWith("...");
      }
    }
    assertEquals(variadic, dots, "Variadic function? " + desc);

    // check that there are enough argument names
    final QNm[] names = fd.names;
    final int min = fd.minMax[0], max = fd.minMax[1];
    assertEquals(names.length, variadic ? min + 1 : max,
      "Wrong number of argument names: " + fd + Arrays.toString(names));

    // all variable names must be distinct
    final Set<QNm> set = new HashSet<>(Arrays.asList(names));
    assertEquals(names.length, set.size(), "Duplicate argument names: " + fd);
  }
}
