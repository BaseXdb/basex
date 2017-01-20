package org.basex.query.up;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.query.*;
import org.junit.*;

/**
 * Tests for the {@link MainOptions#MIXUPDATES} flag.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class MixUpdatesTest extends AdvancedQueryTest {
  /**
   * Prepare tests.
   */
  @BeforeClass
  public static void beforeClass() {
    set(MainOptions.MIXUPDATES, true);
  }

  /** Transform expression containing a simple expression. */
  @Test
  public void transSimple() {
    error("<a/> update ('')", BASX_UPMODIFY);
    error("copy $a := <a/> modify ('') return $a", BASX_UPMODIFY);
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
    query("declare %updating function local:b() { db:output('1') }; local:b()", "1");

    query("declare function local:not-used() { local:b#0 };"
        + "declare %updating function local:b() { db:output('1') }; local:b()", "1");

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

  /** Test method. */
  @Test
  public void functionItem() {
    error("let $x := <a>a</a> update () return (delete node $x/text(), [$x])", BASX_FITEM_X);
  }

  /** Test method. */
  @Test
  public void xqueryEval() {
    query(_XQUERY_EVAL.args(" \"function($x) { function() { $x }  }(4)\"") + "()", "4");
  }


  /** Test method (GH-1281). */
  @Test
  public void inlineFunction() {
    query("declare function local:f() { db:output('1') }; local:f()", "1");
  }

  /**
   * Reject updating functions in built-in higher-order function.
   */
  @Test
  public void updatingHof() {
    query(FOR_EACH.args("1", " db:output#1"), "1");
    query(APPLY.args(" db:output#1", " [1]"), "1");
  }
}
