package org.basex.query.func;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.query.ann.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SignatureTest extends SandboxTest {
  /** Syntax of parameter names. */
  private static final String PARAM = "[a-z\\d][-a-z\\d]*";

  /**
   * Tests the validity of all function signatures.
   */
  @Test public void functions() {
    for(final FuncDefinition fd : Functions.BUILT_IN.values()) {
      // check the general syntax of the description string
      final String string = fd.toString();
      assertTrue(string.matches("^.+\\(.*\\)$"), "Invalid syntax: " + string);
      assertTrue(string.replaceAll("\\(.*", "").matches("^[a-z]+:[-a-zA-Z\\d]+$"),
          "Invalid function name: " + string);

      final String[] params = check(string, fd.paramString(), PARAM);
      final int pl = params.length;
      final boolean variadic = fd.variadic();
      assertEquals(pl > 0 && params[pl - 1].endsWith("..."), variadic,
          "Variadic function? " + string);

      // check that there are enough argument names
      final QNm[] names = fd.params;
      assertEquals(names.length, variadic ? fd.minMax[0] + 1 : fd.minMax[1],
          "Wrong number of argument names: " + string + Arrays.toString(names));

      // all variable names must be distinct
      final Set<QNm> set = new HashSet<>(Arrays.asList(names));
      assertEquals(names.length, set.size(), "Duplicate argument names: " + string);
    }
  }

  /**
   * Tests the validity of all annotation signatures.
   */
  @Test public void annotations() {
    for(final Annotation ann : Annotation.values()) {
      check(ann.toString(), ann.paramString, PARAM + "|'" + PARAM + "'");
    }
  }

  /**
   * Checks the syntax of a parameter string.
   * @param string descriptive string
   * @param paramString parameter string
   * @param name syntax of parameter names
   * @return parameters, including their optional and variadic markers
   */
  private static String[] check(final String string, final String paramString, final String name) {
    assertFalse(paramString.contains("[") || paramString.contains("]"),
        "Obsolete syntax for optional parameters: " + string);
    if(paramString.isEmpty()) return new String[0];

    final String[] params = paramString.split(",", -1);
    final int pl = params.length;
    boolean optional = false, variadic = false;
    for(int p = 0; p < pl; p++) {
      String param = params[p];
      if(p > 0) {
        assertTrue(param.startsWith(" "), "Missing space after comma: " + string);
        param = param.substring(1);
      }
      assertFalse(variadic, "Variadic parameter must be last one: " + string);
      assertTrue(param.matches("^(" + name + ")(\\?|\\.\\.\\.)?$"),
          "Invalid parameter name: " + string);
      variadic = param.endsWith("...");
      final boolean opt = variadic || param.endsWith("?");
      assertFalse(optional && !opt, "Parameter after optional one must be optional: " + string);
      optional = opt;
      params[p] = param;
    }
    return params;
  }
}
