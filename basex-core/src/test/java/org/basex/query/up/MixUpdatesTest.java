package org.basex.query.up;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;

/**
 * Tests for the {@link MainOptions#MIXUPDATES} flag.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class MixUpdatesTest extends AdvancedQueryTest {
  /**
   * Prepare tests.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void beforeClass() throws BaseXException {
    new Set(MainOptions.MIXUPDATES, true).execute(context);
  }

  /**
   * Finalize tests.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void afterClass() throws BaseXException {
    new Set(MainOptions.MIXUPDATES, false).execute(context);
  }

  /** Transform expression containing a simple expression. */
  @Test
  public void transSimple() {
    error("<a/> update ('')", BASEX_MOD);
    error("copy $a := <a/> modify ('') return $a", BASEX_MOD);
  }

  /** Update test. */
  @Test
  public void list() {
    query("delete node <a/>, 1, db:output('2')", "1\n2");
  }

  /** Update test. */
  @Test
  public void update() {
    query(_XQUERY_UPDATE.args("1"), "1");
    query(_XQUERY_UPDATE.args("1") + ",2", "1\n2");
  }

  /** Test method. */
  @Test
  public void output() {
    query(_DB_OUTPUT.args("x") + ",1", "1\nx");
  }

  /** Annotations. */
  @Test
  public void annotations() {
    query("declare %updating function local:x() { 1 }; local:x()", "1");
  }

  /** Updating functions. */
  @Test
  public void updatingFunctions() {
    query("declare function local:not-used() { local:b#0 };"
        + "declare updating function local:b() { db:output('1') }; local:b()", "1");

    query("function($a) { db:output($a) }(1)", "1");
    query("db:output(?)(1)", "1");
    query("db:output#1(1)", "1");
    query("declare function local:a() { 1 }; local:a#0()", "1");
    query("declare function local:a() { local:b#0 };"
        + "declare function local:b() { db:output('1') }; local:a()()", "1");
  }

  /** Test method. */
  @Test
  public void functionLookup() {
    query("declare function local:a() { db:output(1) };"
        + "function-lookup(xs:QName('local:a'), 0)()", "1");
  }

  /** Test method. */
  @Test
  public void flwor() {
    query("<x>X</x> update (let $_ := delete node text() where 0 return $_)", "<x/>");
    query("<x>X</x> update (let $_ := delete node text() return ())", "<x/>");
  }
}
