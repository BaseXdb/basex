package org.basex.query.util;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the transferability of values to another query context.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TransferVisitorTest extends SandboxTest {
  /** Constant function body. */
  @Test public void constant() {
    assertNull(dependency("fn() { 1 }"));
  }

  /** Node constructor: the static context is shared, not copied. */
  @Test public void constructor() {
    assertNull(dependency("fn() { <a/> }"));
  }

  /** Closure with a materialized capture. */
  @Test public void closure() {
    assertNull(dependency("let $a := 123 return fn() { $a }"));
    assertNull(dependency("let $a := <a/> return fn() { $a }"));
  }

  /** Partial application. */
  @Test public void partial() {
    assertNull(dependency("string-join(?, '-')"));
  }

  /** Call of a user-defined function. */
  @Test public void staticFunction() {
    assertNull(dependency("declare function local:f() { 1 }; fn() { local:f() }"));
  }

  /** Recursive user-defined function. */
  @Test public void recursion() {
    assertNull(dependency("declare function local:f($n) { if($n) then local:f($n - 1) else 0 }; "
        + "fn() { local:f(2) }"));
  }

  /** Function items in maps and arrays. */
  @Test public void structures() {
    assertNull(dependency("{ 'f': fn() { 1 } }"));
    assertNull(dependency("[ fn() { 1 } ]"));
    assertEquals("context value", dependency("{ 'f': fn() { . } }"));
    assertEquals("context value", dependency("[ fn() { . } ]"));
  }

  /** Reference to a static variable: the declaration is visited. */
  @Test public void staticVariable() {
    assertNull(dependency("declare variable $v := random:integer(); fn() { $v }"));
    assertEquals("Java code in static variable $v",
        dependency("declare variable $v := Q{java:java.lang.Math}abs(-1); fn() { $v }"));
  }

  /** Reference to a static variable that has already been evaluated. */
  @Test public void evaluatedStaticVariable() {
    execute(new CreateDB(NAME, "<a/>"));
    try {
      assertEquals("persistent data in static variable $v", dependency(
          "declare variable $v := db:get('" + NAME + "'); let $c := count($v) return fn() { $v }"));
    } finally {
      execute(new DropDB(NAME));
    }
  }

  /** Access to the query focus. */
  @Test public void contextValue() {
    assertEquals("context value", dependency("fn() { . }"));
  }

  /** Call of Java code. */
  @Test public void javaCall() {
    assertEquals("Java code", dependency("fn() { Q{java:java.lang.Math}abs(-1) }"));
  }

  /** Database access is resolved by the receiving context. */
  @Test public void databaseAccess() {
    assertNull(dependency("fn() { db:get('" + NAME + "') }"));
  }

  /** Captured node of a persistent database. */
  @Test public void persistentNode() {
    execute(new CreateDB(NAME, "<a/>"));
    try {
      assertEquals("persistent data",
          dependency("let $n := db:get('" + NAME + "') return fn() { $n }"));
    } finally {
      execute(new DropDB(NAME));
    }
  }

  /**
   * Returns the dependency of the value of a query.
   * @param query query
   * @return dependency, or {@code null} if the value can be passed on
   */
  private static String dependency(final String query) {
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      return TransferVisitor.dependency(qp.value());
    } catch(final QueryException ex) {
      Util.stack(ex);
      throw new AssertionError(ex);
    }
  }
}
