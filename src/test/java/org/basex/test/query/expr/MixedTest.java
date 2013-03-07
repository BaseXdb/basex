package org.basex.test.query.expr;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * Mixed XQuery tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class MixedTest extends AdvancedQueryTest {
  /** Test XQuery module file. */
  private static final String XQMFILE = "src/test/resources/hello.xqm";

  /**
   * Drops the collection.
   * @throws BaseXException exception
   */
  @AfterClass
  public static void after() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /** Catches duplicate module import. */
  @Test
  public void duplImport() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace a='world' at '" + XQMFILE + "'; 1",
      Err.DUPLMODULE);
  }

  /** Catches duplicate module import with different module uri. */
  @Test
  public void duplImportDiffUri() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace a='galaxy' at '" + XQMFILE + "'; 1",
      Err.DUPLNSDECL);
  }

  /** Catches duplicate module import. */
  @Test
  public void duplLocation() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace b='galaxy' at '" + XQMFILE + "'; 1",
      Err.WRONGMODULE);
  }

  /** Checks static context scoping in variables. */
  @Test
  public void varsInModules() {
    contains("import module namespace a='world' at '" + XQMFILE + "';" +
        "$a:eager", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "';" +
        "$a:lazy", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "';" +
        "$a:func()", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "';" +
        "a:inlined()", "hello:foo");
  }

  /**
   * Overwrites an empty attribute value.
   * @throws BaseXException database exception
   */
  @Test
  public void emptyAttValues() throws BaseXException {
    new CreateDB(NAME, "<A a='' b=''/>").execute(context);
    query("replace value of node /A/@a with 'A'");
    query("/", "<A a=\"A\" b=\"\"/>");
  }

  /**
   * Parse recursive queries.
   */
  @Test
  public void parseRec() {
    // simple call
    query("declare function local:x() { if(<a/>) then 1 else local:x() }; local:x()");
    // call from FLWOR expression
    query("declare function local:x() { if(<a/>) then 1 else local:x() }; " +
        "let $x := local:x() return $x", "1");
  }

  /**
   * Performs count() on parts of a collection.
   * @throws BaseXException database exception
   */
  @Test
  public void countCollection() throws BaseXException {
    new CreateDB(NAME).execute(context);
    new Add("a", "<a/>").execute(context);
    new Add("b", "<a/>").execute(context);
    new Optimize().execute(context);
    query(COUNT.args(_DB_OPEN.args(NAME, "a") + "/a"), "1");
    query(COUNT.args(_DB_OPEN.args(NAME) + "/a"), "2");
  }
}
