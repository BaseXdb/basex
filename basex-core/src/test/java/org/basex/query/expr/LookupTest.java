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
    query("{ 'a': 'b' } ? c", "");
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

  /** Lookup directly following an axis step (#2591). */
  @Test public void step() {
    query("{ 'a': 1, 'b': 2 } ! self::jnode()?b", 2);
    query("{ 'a': 1, 'b': 2 } ! self::jnode()?*", "1\n2");
    query("{ 'a': { 'b': 5 } } ! self::jnode()?a?b", 5);
    // predicate before and after the lookup
    query("{ 'a': 1 } ! self::jnode()[1]?a", 1);
    query("{ 'a': (1, 2, 3) } ! self::jnode()?a[. > 1]", "2\n3");
    // one lookup per context item
    query("({ 'k': 7 }, { 'k': 8 }) ! self::jnode()?k", "7\n8");
  }

  /** Rewrite lookups to map:get/array:get unless this could suppress a strict-record error. */
  @Test public void rewrite() {
    check("({ 'a': <x/> }, { 'b': <y/> })?a", "<x/>", exists(Lookup.class), empty(_MAP_GET));
    check("declare record local:r(a, b); local:r(<x/>, <y/>)?('a', 'b')", "<x/>\n<y/>",
        exists(Lookup.class));
    check("[ <x/>, <y/> ]?(1, 2)", "<x/>\n<y/>", exists(Lookup.class));

    check("({ 'a': <x/> })?a", "<x/>", exists(RecordGet.class), empty(Lookup.class));
    check("({ 'a': <x/> })?b", "", empty(Lookup.class));
    check("({ 1: <x/> })?1", "<x/>", exists(_MAP_GET), empty(Lookup.class));
    check("({ 'a': <x/> })?*", "<x/>", exists(_MAP_ITEMS), empty(Lookup.class));
    check("[ <x/> ]?1", "<x/>", exists(_ARRAY_GET), empty(Lookup.class));
    check("[ <x/> ]?*", "<x/>", exists(_ARRAY_ITEMS), empty(Lookup.class));
  }

  /** Test. */
  @Test public void error() {
    error("1?a", LOOKUP_X);
    error("declare record local:r(a, b); local:r(1, 2)?c", RECORDFIELD_X_X);
    error("declare record local:r(a, b); (local:r(1, 2), { 'c': 3 })?c", RECORDFIELD_X_X);
    error("declare record local:r(a, b); local:r(<x/>, <y/>)?1", RECORDFIELD_X_X);
  }
}
