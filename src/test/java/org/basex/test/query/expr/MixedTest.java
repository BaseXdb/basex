package org.basex.test.query.expr;

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
      Err.WRONGMODULE);
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
}
