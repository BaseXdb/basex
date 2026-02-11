package org.basex.query.util;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * XQuery shared data tests.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class SharedDataTest extends SandboxTest {
  /** Number of test iterations. */
  private static final int N = 100;

  /** Concurrent usage of SharedData#qnames. */
  @Test public void qname() {
    query("xquery:fork-join(\n"
        + "  for $i in 1 to " + N + "\n"
        + "  return fn() { element { 'x' || $i } {} }\n"
        + ")\n"
        + "=> count()", N);
  }

  /** Concurrent usage of SharedData#tokens. */
  @Test public void token() {
    query("xquery:fork-join(\n"
        + "  for $i in 1 to " + N + "\n"
        + "  return fn() {\n"
        + "    element { `e{$i}` } {\n"
        + "      (1 to $i) ! attribute { `a{.}` } { `{$i}-{.}` }\n"
        + "    }\n"
        + "  }\n"
        + ")\n"
        + "=> count()", N);
  }

  /** Concurrent usage of SharedData#recordTypes. */
  @Test public void recordType() {
    query("xquery:fork-join(\n"
        + "  for $i in 1 to " + N + "\n"
        + "  return function() { \n"
        + "    let $type := 'record(' || string-join((1 to $i) ! ('a' || .), ',') || ')'\n"
        + "    let $rec := `{{ {string-join((1 to $i) ! `'a{.}':{.}`, ',')} }}`\n"
        + "    return xquery:eval(`fn() as {$type} {{ {$rec} }} ()`)\n"
        + "  }\n"
        + ")\n"
        + "=> count()", N);
  }
}
