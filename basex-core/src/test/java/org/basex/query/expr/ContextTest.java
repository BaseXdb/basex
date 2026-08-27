package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the context value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ContextTest extends SandboxTest {
  /** Creates the test database (context item: {@code <x>X</x>}). */
  @BeforeAll public static void beforeClass() {
    execute(new CreateDB(NAME, "<x>X</x>"));
  }

  /** Drops the test database. */
  @AfterAll public static void afterClass() {
    execute(new DropDB(NAME));
  }

  /** Checks that the outer context value is restored after errors. */
  @Test public void contextItem() {
    query(".", "<x>X</x>");
    query("42[not(.)], .", "<x>X</x>");
    query("try { 1[error()] } catch * {.}", "<x>X</x>");
    query("try { 1[error()][1] } catch * {.}", "<x>X</x>");
    query("try { 1[1][error()] } catch * {.}", "<x>X</x>");
    query("try { let $a := <a><b/></a> return $a/b[error()] } catch * {.}", "<x>X</x>");
    query("declare function local:x() {1+<x/>};1[try { local:x() } catch *{.}]", 1);
    query("try { <a/>/(1+'') } catch * {.}", "<x>X</x>");
    query("('a', 'b') ! count(.)", "1\n1");
  }

  /** Checks that function bodies have no context value. */
  @Test public void functionContext() {
    error("declare function local:x() { /x }; local:x()", NOCTX_X);
  }
}
