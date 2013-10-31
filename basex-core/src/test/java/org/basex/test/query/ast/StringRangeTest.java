package org.basex.test.query.ast;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.query.expr.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests if string range queries are correctly evaluated with(out) the index.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class StringRangeTest extends QueryPlanTest {
  /**
   * Initializes the tests.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void start() throws BaseXException {
    final Random rnd = new Random();

    // create initial document
    final TokenBuilder tb = new TokenBuilder();
    final TokenBuilder r = new TokenBuilder();
    tb.add("<xml>");
    for(int i = 100; i < 1000; i++) {
      // add numeric value
      tb.add("<n>").addInt(i).add("</n>");
      // add random value
      final int s = rnd.nextInt(8);
      for(int j = 0; j <= s; j++) r.add('A' + rnd.nextInt(26));
      tb.add("<x>").add(r.finish()).add("</x>");
      r.reset();
    }
    tb.add("</xml>");
    new CreateDB(NAME, tb.toString()).execute(context);
  }

  /**
   * Finishes the tests.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Testing greater-equal and less-equal.
   * @throws BaseXException database exception
   */
  @Test
  public void geLe() throws BaseXException {
    test("exists(//*[text() >= '999' and text() <= '999'])", "true", ValueAccess.class);
    final Class<? extends Expr> clz = StringRangeAccess.class;
    test("count(//*[text() >= '990' and text() <= '999'])", "10", clz);
    test("count(//*[text() >= '900' and text() <= '999'])", "100", clz);
    test("count(//*[text() >= '100' and text() <= '999'])", "900", clz);
    test("count(//*[text() >= ' '   and text() <= 'a'  ])", "1800", clz);
    test("count(//*[text() >= '@'   and text() <= 'a'  ])", "900", clz);
    test("count(//*[text() >= '@'])", "900");
  }

  /**
   * Testing less-equal and greater-equal.
   * @throws BaseXException database exception
   */
  @Test
  public void leGe() throws BaseXException {
    test("exists(//*[text() <= '999' and text() >= '999'])", "true", ValueAccess.class);
    final Class<? extends Expr> clz = StringRangeAccess.class;
    test("count(//*[text() <= '999' and text() >= '990'])", "10", clz);
    test("count(//*[text() <= '999' and text() >= '900'])", "100", clz);
    test("count(//*[text() <= '999' and text() >= '100'])", "900", clz);
    test("count(//*[text() <= 'zzz' and text() >= ' '  ])", "1800", clz);
    test("count(//*[text() <= 'a'   and text() >= '@'  ])", "900", clz);
  }

  /**
   * Testing greater-than and less-than.
   * @throws BaseXException database exception
   */
  @Test
  public void gtLt() throws BaseXException {
    test("exists(//*[text() > '999' and text() < '999'])", "false");
    final Class<? extends Expr> clz = StringRangeAccess.class;
    test("count(//*[text() > '990' and text() < '999'])", "8", clz);
    test("count(//*[text() > '900' and text() < '999'])", "98", clz);
    test("count(//*[text() > '100' and text() < '999'])", "898", clz);
    test("count(//*[text() > ' '   and text() < 'a'  ])", "1800", clz);
    test("count(//*[text() > '@'   and text() < 'a'  ])", "900", clz);
    test("count(//*[text() > '@'])", "900");
  }

  /**
   * Tests a query with and without index.
   * @param query query
   * @param result expected result
   * @param expr class expected in query plan
   * @throws BaseXException database exception
   */
  private static void test(final String query, final String result,
                           final Class<? extends Expr> expr) throws BaseXException {

    new CreateIndex(CmdIndex.TEXT).execute(context);
    check(query, result, "exists(//" + Util.className(expr) + ')');
    new DropIndex(CmdIndex.TEXT).execute(context);
    check(query, result, "not(//" + Util.className(expr) + ')');
  }

  /**
   * Tests a query with and without index.
   * @param query query
   * @param result expected result
   * @throws BaseXException database exception
   */
  private static void test(final String query, final String result) throws BaseXException {
    new CreateIndex(CmdIndex.TEXT).execute(context);
    check(query, result);
    new DropIndex(CmdIndex.TEXT).execute(context);
    check(query, result);
  }
}
