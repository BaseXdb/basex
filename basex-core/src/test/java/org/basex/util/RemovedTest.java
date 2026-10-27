package org.basex.util;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for removed features.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RemovedTest extends SandboxTest {
  /** Removed functions. */
  @Test public void functions() {
    error("convert:binary-to-string(xs:base64Binary(''))", WHICHFUNC_X);
    description("convert:binary-to-string(xs:base64Binary(''))",
        "Unknown function: convert:binary-to-string (removed; use bin:decode-string).");
    description("fn:function-identity(true#0)",
        "Unknown function: function-identity (removed).");
    description("hof:scan-left(1, 2, 3)",
        "Namespace prefix not declared: hof (removed; use the standard functions).");
    // other unknown functions are still reported with similar names
    description("convert:binary-to-strin(())",
        "Unknown function: convert:binary-to-strin.");
  }

  /** Removed options. */
  @Test public void options() {
    description("csv:parse('a', { 'field-delimiter': ';' })",
        "Unknown option 'field-delimiter'. Removed; use 'separator'.");
    description("declare option db:mixupdates 'true'; 1",
        "Unknown option 'mixupdates'. Removed; use job:execute.");
    description("csv:parse('a', { 'separatr': ';' })",
        "Unknown option 'separatr'. Did you mean 'separator'?");
    description("serialize(<a/>, { 'indents': 4 })",
        "Unknown option 'indents'. Removed; use 'indent-unit'.");
    description("declare %output:newline('\\n') function f() { 1 }; f()",
        "Unknown annotation: %output:newline (removed; use 'line-ending').");
  }

  /** Removed option values. */
  @Test public void values() {
    description("json:parse('1', { 'format': 'basic' })",
        "Invalid 'format' value 'basic'; expected: one of direct, attributes, jsonml, w3, "
        + "w3-xml, w3-mapping. Removed; use 'w3-xml'.");
  }

  /**
   * Checks the error description of a query.
   * @param query query
   * @param expected expected description
   */
  private static void description(final String query, final String expected) {
    query("try { xquery:eval(\"" + query.replace("\"", "\"\"") + "\") } "
        + "catch * { $err:description }", expected);
  }
}
