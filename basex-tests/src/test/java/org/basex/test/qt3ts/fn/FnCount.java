package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the count() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCount extends QT3TestSet {

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count001() {
    final XQuery query = new XQuery(
      "count(//employee[@name='John Doe 4']) = 1",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count002() {
    final XQuery query = new XQuery(
      "count(//employee[@name='John Doe 4']) < 2",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count003() {
    final XQuery query = new XQuery(
      "count(//employee[@name='John Doe 4']) > 0",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count004() {
    final XQuery query = new XQuery(
      "count(//employee[@name='John Doe 4']/@name) > 0.5",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count005() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) lt 1.5",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count006() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) eq 0",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count007() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 498']) eq 0",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count008() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) lt 1000000000000",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count009() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) gt -5",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count010() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) eq 0.3",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count011() {
    final XQuery query = new XQuery(
      "count(//node()) gt 40",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count012() {
    final XQuery query = new XQuery(
      "count(//node()) ne -1",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count013() {
    final XQuery query = new XQuery(
      "0 = count(//node())",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count014() {
    final XQuery query = new XQuery(
      "40 gt count(//node())",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  A test whose essence is: `count()`. .
   */
  @org.junit.Test
  public void kSeqCountFunc1() {
    final XQuery query = new XQuery(
      "count()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `count( ((), "one", 2, "three")) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc10() {
    final XQuery query = new XQuery(
      "count( ((), \"one\", 2, \"three\")) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( ("one", (2, "three")) ) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc11() {
    final XQuery query = new XQuery(
      "count( (\"one\", (2, \"three\")) ) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count((1, 2)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqCountFunc12() {
    final XQuery query = new XQuery(
      "count((1, 2)) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count((1, 2, 3, "four")) eq 4`. .
   */
  @org.junit.Test
  public void kSeqCountFunc13() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, \"four\")) eq 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count((1, 2, 3, "four")) eq 4`. .
   */
  @org.junit.Test
  public void kSeqCountFunc14() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, \"four\")) eq 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(1 to 3) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc15() {
    final XQuery query = new XQuery(
      "count(1 to 3) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(reverse((1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc16() {
    final XQuery query = new XQuery(
      "count(reverse((1, 2, 3))) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(reverse((1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc17() {
    final XQuery query = new XQuery(
      "count(reverse((1, 2, 3))) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(count((1, 2, current-time(), 4))) eq false()`. .
   */
  @org.junit.Test
  public void kSeqCountFunc18() {
    final XQuery query = new XQuery(
      "not(count((1, 2, current-time(), 4))) eq false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(1, ())`. .
   */
  @org.junit.Test
  public void kSeqCountFunc2() {
    final XQuery query = new XQuery(
      "count(1, ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `count( () ) eq 0`. .
   */
  @org.junit.Test
  public void kSeqCountFunc3() {
    final XQuery query = new XQuery(
      "count( () ) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( (1, 2, 3) ) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc4() {
    final XQuery query = new XQuery(
      "count( (1, 2, 3) ) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( (1, 2, ()) ) eq 2`. .
   */
  @org.junit.Test
  public void kSeqCountFunc5() {
    final XQuery query = new XQuery(
      "count( (1, 2, ()) ) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(()) eq 0`. .
   */
  @org.junit.Test
  public void kSeqCountFunc6() {
    final XQuery query = new XQuery(
      "count(()) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(((((()))))) eq 0`. .
   */
  @org.junit.Test
  public void kSeqCountFunc7() {
    final XQuery query = new XQuery(
      "count(((((()))))) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( (((), (), ()), (), (), (), ()) ) eq 0`. .
   */
  @org.junit.Test
  public void kSeqCountFunc8() {
    final XQuery query = new XQuery(
      "count( (((), (), ()), (), (), (), ()) ) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( (1, 2, 3) ) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc9() {
    final XQuery query = new XQuery(
      "count( (1, 2, 3) ) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:count() doesn't implicitly get the context node. .
   */
  @org.junit.Test
  public void k2SeqCountFunc1() {
    final XQuery query = new XQuery(
      "(1 to 10)/count()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  test fn:count on a range .
   */
  @org.junit.Test
  public void cbclCount001() {
    final XQuery query = new XQuery(
      "fn:count(1 to 10000000)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10000000")
    );
  }

  /**
   *  test fn:count on a mix of values and expressions .
   */
  @org.junit.Test
  public void cbclCount002() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) { if ($arg = 0) then (1, 2, 3) else $arg }; fn:count( ( (), local:generate( () ), local:generate( 0 ), (1 to 10000000), local:generate( () ), local:generate(1)) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10000004")
    );
  }

  /**
   *  Test fn:count on a range .
   */
  @org.junit.Test
  public void cbclCount003() {
    final XQuery query = new XQuery(
      "let $x := year-from-date(current-date()) return count( 1 to $x ) = $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount005() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt 25",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount006() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le 25",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount007() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq 25",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount008() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge 25",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount009() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt 25",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount010() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne 25",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount011() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) lt 25)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount012() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) le 25)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount013() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) eq 25)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount014() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) ge 25)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount015() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) gt 25)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount016() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) ne 25)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount017() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 25 lt count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount018() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 25 le count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount019() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 25 eq count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount020() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 25 ge count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount021() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 25 gt count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount022() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 25 ne count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount023() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(25 lt count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount024() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(25 le count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount025() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(25 eq count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount026() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(25 ge count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount027() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(25 gt count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount028() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(25 ne count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount029() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) lt local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount030() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) le local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount031() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) eq local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount032() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) ge local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount033() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) gt local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount034() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) ne local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount035() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(2, 100)) lt count(local:primes(100, 200))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount036() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(2, 100)) le count(local:primes(100, 200))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount037() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(2, 100)) eq count(local:primes(100, 200))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount038() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(2, 100)) ge count(local:primes(100, 200))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount039() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(2, 100)) gt count(local:primes(100, 200))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount040() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(2, 100)) ne count(local:primes(100, 200))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount041() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount042() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount043() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount044() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount045() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount046() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount047() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) lt -local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount048() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) le -local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount049() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) eq -local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount050() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) ge -local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount051() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) gt -local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount052() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:integer) { $n * $n }; count(local:primes(100)) ne -local:square(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount053() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(2, 100)) lt count(local:primes(100, 200)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount054() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(2, 100)) le count(local:primes(100, 200)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount055() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(2, 100)) eq count(local:primes(100, 200)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount056() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(2, 100)) ge count(local:primes(100, 200)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount057() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(2, 100)) gt count(local:primes(100, 200)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount058() {
    final XQuery query = new XQuery(
      "declare function local:primes($s as xs:integer, $n as xs:integer) { let $start := if ($s lt 2) then 2 else $s return for $i in $s to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(2, 100)) ne count(local:primes(100, 200)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount059() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt 25.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount060() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le 25.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount061() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq 25.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount062() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge 25.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount063() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt 25.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount064() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne 25.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount065() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) lt 25.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount066() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) le 25.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount067() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) eq 25.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount068() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) ge 25.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount069() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) gt 25.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount070() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) ne 25.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount071() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 24.5 lt count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount072() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 24.5 le count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount073() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 24.5 eq count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount074() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 24.5 ge count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount075() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 24.5 gt count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount076() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; 24.5 ne count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount077() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(24.5 lt count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount078() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(24.5 le count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount079() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(24.5 eq count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount080() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(24.5 ge count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount081() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(24.5 gt count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount082() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(24.5 ne count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount083() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) lt local:square(5.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount084() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) le local:square(5.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount085() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) eq local:square(5.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount086() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) ge local:square(5.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount087() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) gt local:square(5.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount088() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) ne local:square(5.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount089() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt -1.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount090() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le -1.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount091() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq -1.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount092() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge -1.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount093() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt -1.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount094() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne -1.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount095() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) lt -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount096() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) le -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount097() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) eq -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount098() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) ge -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount099() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) gt -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount100() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) ne -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount101() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:decimal) { $n * $n }; count(local:primes(100)) ge 1.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount102() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt xs:float(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount103() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le xs:float(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount104() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq xs:float(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount105() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:float(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount106() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt xs:float(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount107() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne xs:float(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount108() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) lt xs:float(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount109() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) le xs:float(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount110() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) eq xs:float(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount111() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) ge xs:float(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount112() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) gt xs:float(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount113() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) ne xs:float(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount114() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:float(\"24.5\") lt count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount115() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:float(\"24.5\") le count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount116() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:float(\"24.5\") eq count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount117() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:float(\"24.5\") ge count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount118() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:float(\"24.5\") gt count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount119() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:float(\"24.5\") ne count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount120() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:float(\"24.5\") lt count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount121() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:float(\"24.5\") le count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount122() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:float(\"24.5\") eq count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount123() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:float(\"24.5\") ge count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount124() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:float(\"24.5\") gt count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount125() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:float(\"24.5\") ne count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount126() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) lt local:square(xs:float(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount127() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) le local:square(xs:float(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount128() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) eq local:square(xs:float(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount129() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) ge local:square(xs:float(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount130() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) gt local:square(xs:float(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount131() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) ne local:square(xs:float(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount132() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt xs:float(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount133() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le xs:float(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount134() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq xs:float(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount135() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:float(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount136() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt xs:float(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount137() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne xs:float(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount138() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) lt -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount139() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) le -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount140() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) eq -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount141() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) ge -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount142() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) gt -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount143() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:float) { $n * $n }; count(local:primes(100)) ne -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount144() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:float(\"1.0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount145() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt xs:double(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount146() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le xs:double(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount147() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq xs:double(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount148() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:double(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount149() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt xs:double(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount150() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne xs:double(\"25.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount151() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) lt xs:double(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount152() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) le xs:double(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount153() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) eq xs:double(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount154() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) ge xs:double(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount155() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) gt xs:double(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount156() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(count(local:primes(100)) ne xs:double(\"25.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount157() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:double(\"24.5\") lt count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount158() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:double(\"24.5\") le count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount159() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:double(\"24.5\") eq count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount160() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:double(\"24.5\") ge count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount161() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:double(\"24.5\") gt count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount162() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; xs:double(\"24.5\") ne count(local:primes(100))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount163() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:double(\"24.5\") lt count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount164() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:double(\"24.5\") le count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount165() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:double(\"24.5\") eq count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount166() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:double(\"24.5\") ge count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount167() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:double(\"24.5\") gt count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount168() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; not(xs:double(\"24.5\") ne count(local:primes(100)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount169() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) lt local:square(xs:double(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount170() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) le local:square(xs:double(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount171() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) eq local:square(xs:double(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount172() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) ge local:square(xs:double(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount173() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) gt local:square(xs:double(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount174() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) ne local:square(xs:double(\"4.9\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount175() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt xs:double(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount176() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le xs:double(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount177() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq xs:double(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount178() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:double(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount179() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt xs:double(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount180() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne xs:double(\"-1.5\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount181() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) lt -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount182() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) le -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount183() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) eq -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount184() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) ge -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount185() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) gt -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount186() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:square($n as xs:double) { $n * $n }; count(local:primes(100)) ne -local:square(5.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount187() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:double(\"1.0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount188() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount189() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount190() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount191() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount192() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount193() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount194() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount195() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount196() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount197() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount198() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount199() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount200() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount201() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount202() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount203() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount204() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount205() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount206() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount207() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount208() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount209() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount210() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount211() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount212() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount213() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) lt local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount214() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) le local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount215() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) eq local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount216() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) ge local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount217() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) gt local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount218() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) ne local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount219() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) lt local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount220() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) le local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount221() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) eq local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount222() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) ge local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount223() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) gt local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount224() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:double { if ($n) then xs:double('NaN') else xs:double('INF') }; count(local:primes(100)) ne local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount225() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) lt local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount226() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) le local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount227() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) eq local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount228() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) ge local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount229() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) gt local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount230() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) ne local:strange(true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount231() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) lt local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount232() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) le local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount233() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) eq local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount234() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) ge local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount235() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) gt local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount236() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; declare function local:strange($n as xs:boolean) as xs:float { if ($n) then xs:float('NaN') else xs:float('INF') }; count(local:primes(100)) ne local:strange(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Tests a count of initial subsequence .
   */
  @org.junit.Test
  public void cbclCount237() {
    final XQuery query = new XQuery(
      "count((for $x in 1 to 10 return $x * $x)[position() < 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount238() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) lt -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount239() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) le -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount240() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) eq -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount241() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount242() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) gt -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount243() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ne -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:count .
   */
  @org.junit.Test
  public void cbclCount244() {
    final XQuery query = new XQuery(
      "declare function local:primes($n as xs:integer) { if ($n lt 2) then 1 else for $i in 2 to $n return if (every $x in 2 to ($i - 1) satisfies ($i mod $x ne 0)) then $i else () }; count(local:primes(100)) ge 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnCountdbl1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnCountdbl1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:double(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnCountdbl1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:double(\"1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnCountdec1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnCountdec1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnCountdec1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:decimal(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnCountflt1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnCountflt1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:float(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnCountflt1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:float(\"3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnCountint1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnCountint1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnCountint1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnCountintg1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnCountintg1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnCountintg1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:integer(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnCountlng1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnCountlng1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnCountlng1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:long(\"92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCountnint1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnCountnint1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCountnint1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:negativeInteger(\"-1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCountnni1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnCountnni1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCountnni1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCountnpi1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnCountnpi1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCountnpi1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCountpint1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnCountpint1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCountpint1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:positiveInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnCountsht1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnCountsht1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnCountsht1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnCountulng1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnCountulng1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnCountulng1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedLong(\"184467440737095516\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnCountusht1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnCountusht1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnCountusht1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }
}
