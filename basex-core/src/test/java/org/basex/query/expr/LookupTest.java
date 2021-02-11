package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Lookup operator tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class LookupTest extends SandboxTest {
  /** Test. */
  @Test public void map() {
    query("map { 'a':'b' } ? a", "b");
    query("(map { 'a':'b' }, map { 'c':'d' }) ? a", "b");
    query("map { 'a':'b', 'c':'d' } ? ('a','c')", "b\nd");
    query("(map { 'a':'b' }, map { 'c':'d' }) ? ('a','c')", "b\nd");
    query("map:merge(for $i in 1 to 5 return map { $i: $i+1 })? 2", 3);

    query("map { 'first' : 'Jenna', 'last' : 'Scott' } ? first", "Jenna");
    query("(map { 'first': 'Tom' }, map { 'first': 'Dick' }, map { 'first': 'Harry' }) ? first",
        "Tom\nDick\nHarry");

    query("<_>X</_>[map { 'Y': <_/> }?(text())]", "");
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
    query("([1,2,3], [4,5,6])?2", "2\n5");
  }

  /** Test. */
  @Test public void mixed() {
    query("(map { 1: 'm' }, array { 'a' }) ? 1", "m\na");
  }

  /** Test. */
  @Test public void wildcard() {
    query("(map { 1: 'm' }, array { 'a' }) ? *", "m\na");
  }

  /** Test. */
  @Test public void unary() {
    query("(map { 1: 'm' }, array { 'a' }) ! ?*", "m\na");
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

  /** Test. */
  @Test public void error() {
    error("1?a", LOOKUP_X);
  }
}
