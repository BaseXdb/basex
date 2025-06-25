package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Lookup operator tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LookupTest extends SandboxTest {
  /** Test. */
  @Test public void map() {
    query("{ 'a': 'b' } ? a", "b");
    query("({ 'a': 'b' }, { 'c': 'd' }) ? a", "b");
    query("{ 'a': 'b', 'c': 'd' } ? ('a', 'c')", "b\nd");
    query("({ 'a': 'b' }, { 'c': 'd' }) ? ('a', 'c')", "b\nd");
    query("map:merge(for $i in 1 to 5 return { $i: $i+1 })? 2", 3);

    query("{ 'first' : 'Jenna', 'last' : 'Scott' } ? first", "Jenna");
    query("({ 'first': 'Tom' }, { 'first': 'Dick' }, { 'first': 'Harry' }) ? first",
        "Tom\nDick\nHarry");

    query("<_>X</_>[{ 'Y': <_/> }?(text())]", "");
  }

  /** Test. */
  @Test public void array() {
    query("array { 'a', 'b' } ? 1", "a");
    query("(array { 'a', 'b' }, array { 'c', 'd' }) ? 1", "a\nc");
    query("(array { 'a', 'b', 'c' }) ? (1, 2)", "a\nb");
    query("(array { 'a', 'b', 'c' }) ? (1 to 2)", "a\nb");
    query(_ARRAY_JOIN.args(" for $i in 1 to 5 return array { $i+1 }") + " ? 2", 3);

    query("[1, 2, 5, 7] ?*", "1\n2\n5\n7");
    query("[[1, 2, 3], [4, 5, 6]] ?* ?*", "1\n2\n3\n4\n5\n6");
    query("[4, 5, 6]?2", 5);
    query("([1, 2, 3], [4, 5, 6])?2", "2\n5");
  }

  /** Test. */
  @Test public void mixed() {
    query("({ 1: 'm' }, array { 'a' }) ? 1", "m\na");
  }

  /** Test. */
  @Test public void wildcard() {
    query("({ 1: 'm' }, array { 'a' }) ? *", "m\na");
  }

  /** Test. */
  @Test public void unary() {
    query("({ 1: 'm' }, array { 'a' }) ! ?*", "m\na");
    query("array { 1 }[?1] ! ?1", 1);
  }

  /** Test. */
  @Test public void typing() {
    query("empty(array:for-each([], function($i) { string($i) })?*)", true);
    query("empty(array:for-each([], function($i) { string($i) })!?*)", true);
    query("empty(array:for-each([], function($i) { string($i) })?())", true);
    query("empty(array:for-each([], function($i) { string($i) })!?())", true);
  }

  /** Test. */
  @Test public void emptyKey() {
    query("array:for-each([], function($i) { string($i) })?([])", "");
    query("array:for-each([], function($i) { string($i) })!?([])", "");
  }

  /**Test. */
  @Test public void modifiers() {
    query("{ 'a': 'b' } ? pairs::a", "{\"key\":\"a\",\"value\":\"b\"}");
    query("{ 'a': 'b' } ? keys::a", "a");
    query("{ 'a': 'b' } ? values::a", "[\"b\"]");
    query("{ 'a': 'b' } ? items::a", "b");
    query("{ 'a': 'b' } ? a", "b");

    query("([ 1, [ \"a\", \"b\" ], 4, 5, [ \"c\", \"d\"] ])?values::*[. instance of "
        + "array(array(xs:string))]",
          "[[\"a\",\"b\"]]\n"
        + "[[\"c\",\"d\"]]");

    final String a = "let $A := [ (\"a\", \"b\"), (\"c\", \"d\"), (\"e\", \"f\"), 42 ] return ";
    query(a + "$A?items::*", "a\n"
        + "b\n"
        + "c\n"
        + "d\n"
        + "e\n"
        + "f\n"
        + "42");
    query(a + "$A?pairs::*", "{\"key\":1,\"value\":(\"a\",\"b\")}\n"
        + "{\"key\":2,\"value\":(\"c\",\"d\")}\n"
        + "{\"key\":3,\"value\":(\"e\",\"f\")}\n"
        + "{\"key\":4,\"value\":42}");
    query(a + "$A?values::*", "[\"a\",\"b\"]\n"
        + "[\"c\",\"d\"]\n"
        + "[\"e\",\"f\"]\n"
        + "[42]");
    query(a + "$A?keys::*", "1\n"
        + "2\n"
        + "3\n"
        + "4");
    query(a + "$A?items::2", "c\n"
        + "d");
    query(a + "$A?pairs::2", "{\"key\":2,\"value\":(\"c\",\"d\")}");
    query(a + "$A?values::2", "[\"c\",\"d\"]");
    query(a + "$A?keys::2", "2");
    query(a + "$A?items::(3, 1)", "e\n"
        + "f\n"
        + "a\n"
        + "b");
    query(a + "$A?pairs::(3, 1)", "{\"key\":3,\"value\":(\"e\",\"f\")}\n"
        + "{\"key\":1,\"value\":(\"a\",\"b\")}");
    query(a + "$A?values::(3, 1)", "[\"e\",\"f\"]\n"
        + "[\"a\",\"b\"]");
    query(a + "$A?keys::(3, 1)", "3\n"
        + "1");

    final String m = "let $M := { \"X\": (\"a\", \"b\"), \"Y\": (\"c\", \"d\"), \"Z\": (\"e\", "
        + "\"f\"), \"N\": 42 } return ";
    query(m + "$M?items::*", "a\n"
        + "b\n"
        + "c\n"
        + "d\n"
        + "e\n"
        + "f\n"
        + "42");
    query(m + "$M?pairs::*", "{\"key\":\"X\",\"value\":(\"a\",\"b\")}\n"
        + "{\"key\":\"Y\",\"value\":(\"c\",\"d\")}\n"
        + "{\"key\":\"Z\",\"value\":(\"e\",\"f\")}\n"
        + "{\"key\":\"N\",\"value\":42}");
    query(m + "$M?values::*", "[\"a\",\"b\"]\n"
        + "[\"c\",\"d\"]\n"
        + "[\"e\",\"f\"]\n"
        + "[42]");
    query(m + "$M?keys::*", "X\n"
        + "Y\n"
        + "Z\n"
        + "N");
    query(m + "$M?items::Y", "c\n"
        + "d");
    query(m + "$M?pairs::Y", "{\"key\":\"Y\",\"value\":(\"c\",\"d\")}");
    query(m + "$M?values::Y", "[\"c\",\"d\"]");
    query(m + "$M?keys::Y", "Y");
    query(m + "$M?items::(\"Z\", \"X\")", "e\n"
        + "f\n"
        + "a\n"
        + "b");
    query(m + "$M?pairs::(\"Z\", \"X\")", "{\"key\":\"Z\",\"value\":(\"e\",\"f\")}\n"
        + "{\"key\":\"X\",\"value\":(\"a\",\"b\")}");
    query(m + "$M?values::(\"Z\", \"X\")", "[\"e\",\"f\"]\n"
        + "[\"a\",\"b\"]");
    query(m + "$M?keys::(\"Z\", \"X\")", "Z\n"
        + "X");
  }

  /** Test. */
  @Test public void error() {
    error("1?a", LOOKUP_X);
  }

  /** Test. */
  @Test public void deep() {
    query("[ 'a' ]??(1.0)", "a");
    query("[ 'a' ]??(1.1)", "");
  }
}
