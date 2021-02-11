package org.basex.query.index;

import java.util.*;

import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.CmdIndex;
import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests if string range queries are correctly evaluated with(out) the index.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StringRangeTest extends QueryPlanTest {
  /**
   * Initializes the tests.
   */
  @BeforeAll public static void start() {
    final Random rnd = new Random();

    // create initial document
    final TokenBuilder tb = new TokenBuilder(), r = new TokenBuilder();
    tb.add("<xml>");
    for(int i = 100; i < 1000; i++) {
      // add numeric value
      tb.add("<n>").addInt(i).add("</n>");
      // add random value
      final int s = rnd.nextInt(8);
      for(int j = 0; j <= s; j++) r.add('A' + rnd.nextInt(26));
      tb.add("<x>").add(r.next()).add("</x>");
    }
    tb.add("</xml>");
    execute(new CreateDB(NAME, tb.toString()));
  }

  /**
   * Finishes the tests.
   */
  @AfterAll public static void finish() {
    execute(new DropDB(NAME));
  }

  /**
   * Testing greater-equal and less-equal.
   */
  @Test public void geLe() {
    test("exists(//*[text() >= '999' and text() <= '999'])", true, ValueAccess.class);
    final Class<? extends Expr> clz = StringRangeAccess.class;
    test("count(//*[text() >= '990' and text() <= '999'])", 10, clz);
    test("count(//*[text() >= '900' and text() <= '999'])", 100, clz);
    test("count(//*[text() >= '100' and text() <= '999'])", 900, clz);
    test("count(//*[text() >= ' '   and text() <= 'a'  ])", 1800, clz);
    test("count(//*[text() >= '@'   and text() <= 'a'  ])", 900, clz);
    test("count(//*[text() >= '@'])", 900);
  }

  /**
   * Testing less-equal and greater-equal.
   */
  @Test public void leGe() {
    test("exists(//*[text() <= '999' and text() >= '999'])", true, ValueAccess.class);
    final Class<? extends Expr> clz = StringRangeAccess.class;
    test("count(//*[text() <= '999' and text() >= '990'])", 10, clz);
    test("count(//*[text() <= '999' and text() >= '900'])", 100, clz);
    test("count(//*[text() <= '999' and text() >= '100'])", 900, clz);
    test("count(//*[text() <= 'zzz' and text() >= ' '  ])", 1800, clz);
    test("count(//*[text() <= 'a'   and text() >= '@'  ])", 900, clz);
  }

  /**
   * Testing greater-than and less-than.
   */
  @Test public void gtLt() {
    test("exists(//*[text() > '999' and text() < '999'])", false);
    final Class<? extends Expr> clz = StringRangeAccess.class;
    test("count(//*[text() > '990' and text() < '999'])", 8, clz);
    test("count(//*[text() > '900' and text() < '999'])", 98, clz);
    test("count(//*[text() > '100' and text() < '999'])", 898, clz);
    test("count(//*[text() > ' '   and text() < 'a'  ])", 1800, clz);
    test("count(//*[text() > '@'   and text() < 'a'  ])", 900, clz);
    test("count(//*[text() > '@'])", 900);
  }

  /**
   * Tests a query with and without index.
   * @param query query
   * @param result expected result
   * @param expr class expected in query plan
   */
  private static void test(final String query, final Object result,
      final Class<? extends Expr> expr) {

    execute(new CreateIndex(CmdIndex.TEXT));
    check(query, result, exists(Util.className(expr)));
    execute(new DropIndex(CmdIndex.TEXT));
    check(query, result, empty(Util.className(expr)));
  }

  /**
   * Tests a query with and without index.
   * @param query query
   * @param result expected result
   */
  private static void test(final String query, final Object result) {
    execute(new CreateIndex(CmdIndex.TEXT));
    check(query, result);
    execute(new DropIndex(CmdIndex.TEXT));
    check(query, result);
  }
}
