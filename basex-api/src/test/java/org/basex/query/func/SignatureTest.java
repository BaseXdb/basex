package org.basex.query.func;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.basex.*;
import org.basex.query.ann.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
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
   * @return tests
   */
  @TestFactory public Stream<DynamicTest> functions() {
    return Stream.concat(
        Arrays.stream(Function.values()).map(func -> test(func, func.name())),
        Arrays.stream(ApiFunction.values()).map(func -> test(func, func.name())));
  }

  /**
   * Tests the validity of all annotation signatures.
   * @return tests
   */
  @TestFactory public Stream<DynamicTest> annotations() {
    return Arrays.stream(Annotation.values()).map(ann ->
        DynamicTest.dynamicTest(ann.toString(), () -> annotation(ann)));
  }

  /**
   * Creates a test for a single function.
   * @param func function
   * @param constant name of the enum constant
   * @return test
   */
  private static DynamicTest test(final AFunction func, final String constant) {
    return DynamicTest.dynamicTest(func.definition().toString(), () -> function(func, constant));
  }

  /**
   * Checks the signature of a single function.
   * @param func function
   * @param constant name of the enum constant
   */
  private static void function(final AFunction func, final String constant) {
    final FuncDefinition fd = func.definition();

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

    // check that there are enough parameter names and types
    final int arity = variadic ? fd.minMax[0] + 1 : fd.minMax[1];
    final QNm[] names = fd.params;
    assertEquals(arity, names.length,
        "Wrong number of parameter names: " + string + Arrays.toString(names));
    assertEquals(arity, fd.types.length, "Wrong number of parameter types: " + string);

    // all variable names must be distinct
    final Set<QNm> set = new HashSet<>(Arrays.asList(names));
    assertEquals(names.length, set.size(), "Duplicate parameter names: " + string);

    // enum constant and implementation class must be derived from the function name
    final String prefix = Token.string(fd.name.prefix()), local = Token.string(fd.name.local());
    assertEquals(constant(prefix, local, "fn"), constant, "Unexpected enum constant: " + string);

    // record constructors are backed by a single implementation
    final String clazz = func.className();
    if(!clazz.equals(Util.className(ShapeConstructor.class))) {
      // capitalization of acronyms is ignored
      assertTrue(camelCase(prefix + '-' + local).equalsIgnoreCase(clazz),
          "Unexpected implementation class: " + clazz + ", " + string);
    }
  }

  /**
   * Checks the signature of a single annotation.
   * @param ann annotation
   */
  private static void annotation(final Annotation ann) {
    final String string = ann.toString();
    final String[] params = check(string, ann.paramString, PARAM + "|'" + PARAM + "'");

    // check that there are enough parameter types
    final boolean variadic = ann.minMax[1] == Integer.MAX_VALUE;
    assertEquals(variadic ? ann.minMax[0] + 1 : ann.minMax[1], ann.params.length,
        "Wrong number of parameter types: " + string);

    // all variable names must be distinct
    final Set<String> set = new HashSet<>(Arrays.asList(params));
    assertEquals(params.length, set.size(), "Duplicate parameter names: " + string);

    // enum constant must be derived from the annotation name
    final String prefix = Token.string(ann.name.prefix()), local = Token.string(ann.name.local());
    assertEquals(constant(prefix, local, "xq"), ann.name(), "Unexpected enum constant: " + string);
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

  /**
   * Returns the name of the enum constant for a qualified name.
   * @param prefix namespace prefix
   * @param local local name
   * @param defaultPrefix prefix of the namespace that requires no qualifier
   * @return constant name
   */
  private static String constant(final String prefix, final String local,
      final String defaultPrefix) {
    final String name = local.toUpperCase(Locale.ENGLISH).replace('-', '_');
    return prefix.equals(defaultPrefix) ? name :
      '_' + prefix.toUpperCase(Locale.ENGLISH) + '_' + name;
  }

  /**
   * Converts a hyphenated name to camel case.
   * @param name hyphenated name
   * @return camel case name
   */
  private static String camelCase(final String name) {
    final StringBuilder sb = new StringBuilder();
    for(final String part : Strings.split(name, '-')) {
      sb.append(Character.toUpperCase(part.charAt(0))).append(part, 1, part.length());
    }
    return sb.toString();
  }
}
