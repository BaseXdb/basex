package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the boolean() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnBoolean extends QT3TestSet {

  /**
   *  Test: K-SeqBooleanFunc-1 Purpose: A test whose essence is: `boolean()`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc1() {
    final XQuery query = new XQuery(
      "boolean()",
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
   *  Test: K-SeqBooleanFunc-10 Purpose: fn:boolean() invoked on an non-empty
   *          xs:untypedAtomic should return true. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc10() {
    final XQuery query = new XQuery(
      "boolean(xs:untypedAtomic(\"string\"))",
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
   *  Test: K-SeqBooleanFunc-11 Purpose: fn:boolean() invoked on an non-empty
   *          xs:untypedAtomic should return false. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc11() {
    final XQuery query = new XQuery(
      "not(boolean(xs:untypedAtomic(\"\")))",
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
   *  Test: K-SeqBooleanFunc-12 Purpose: A test whose essence is: `boolean(1)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc12() {
    final XQuery query = new XQuery(
      "boolean(1)",
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
   *  Test: K-SeqBooleanFunc-13 Purpose: A test whose essence is: `not(boolean(""))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc13() {
    final XQuery query = new XQuery(
      "not(boolean(\"\"))",
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
   *  Test: K-SeqBooleanFunc-14 Purpose: A test whose essence is:
   *          `not(boolean(false()))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc14() {
    final XQuery query = new XQuery(
      "not(boolean(false()))",
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
   *  Test: K-SeqBooleanFunc-15 Purpose: A test whose essence is: `boolean(true())`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc15() {
    final XQuery query = new XQuery(
      "boolean(true())",
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
   *  Test: K-SeqBooleanFunc-16 Purpose: A test whose essence is: `true() eq
   *          boolean(remove((xs:hexBinary("FF"), 1), 1) treat as xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc16() {
    final XQuery query = new XQuery(
      "true() eq boolean(remove((xs:hexBinary(\"FF\"), 1), 1) treat as xs:integer)",
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
   *  Test: K-SeqBooleanFunc-17 Purpose: A test whose essence is: `not(0)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc17() {
    final XQuery query = new XQuery(
      "not(0)",
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
   * zzzA test whose essence is: `boolean(1)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc18() {
    final XQuery query = new XQuery(
      "boolean(1)",
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
   * zzzA test whose essence is: `boolean(1.1)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc19() {
    final XQuery query = new XQuery(
      "boolean(1.1)",
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
   *  Test: K-SeqBooleanFunc-2 Purpose: A test whose essence is: `boolean(1, "wrong
   *          param")`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc2() {
    final XQuery query = new XQuery(
      "boolean(1, \"wrong param\")",
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
   * sA test whose essence is: `boolean(1.1e1)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc20() {
    final XQuery query = new XQuery(
      "boolean(1.1e1)",
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
   * sA test whose essence is: `not(0.0e0)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc21() {
    final XQuery query = new XQuery(
      "not(0.0e0)",
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
   * sA test whose essence is: `not(0.0)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc22() {
    final XQuery query = new XQuery(
      "not(0.0)",
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
   * sA test whose essence is: `boolean(-1)`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc23() {
    final XQuery query = new XQuery(
      "boolean(-1)",
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
   * A test whose essence is: `not(())`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc24() {
    final XQuery query = new XQuery(
      "not(())",
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
   * A test whose essence is: `not(boolean(()))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc25() {
    final XQuery query = new XQuery(
      "not(boolean(()))",
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
   * A test whose essence is: `not(())`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc26() {
    final XQuery query = new XQuery(
      "not(())",
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
   *  A test whose essence is: `not(empty((1, 2)))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc27() {
    final XQuery query = new XQuery(
      "not(empty((1, 2)))",
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
   * A test whose essence is: `not(empty(((), 1, 2)))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc28() {
    final XQuery query = new XQuery(
      "not(empty(((), 1, 2)))",
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
   * A test whose essence is: `boolean('nada')`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc29() {
    final XQuery query = new XQuery(
      "boolean('nada')",
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
   *  Test: K-SeqBooleanFunc-3 Purpose: A test whose essence is: `boolean((1, 2))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc3() {
    final XQuery query = new XQuery(
      "boolean((1, 2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   * A test whose essence is: `boolean('""')`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc30() {
    final XQuery query = new XQuery(
      "boolean('\"\"')",
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
   * A test whose essence is: `not('')`..
   */
  @org.junit.Test
  public void kSeqBooleanFunc31() {
    final XQuery query = new XQuery(
      "not('')",
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
   * Apply fn:boolean() on fn:count(). .
   */
  @org.junit.Test
  public void kSeqBooleanFunc32() {
    final XQuery query = new XQuery(
      "fn:boolean(count((1, 2, 3, timezone-from-time(current-time()), 4)))",
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
   *  Test: K-SeqBooleanFunc-4 Purpose: A test whose essence is:
   *          `boolean(xs:QName("valid-local-name"))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc4() {
    final XQuery query = new XQuery(
      "boolean(xs:QName(\"valid-local-name\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Test: K-SeqBooleanFunc-5 Purpose: A test whose essence is:
   *          `boolean(xs:hexBinary("03"))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc5() {
    final XQuery query = new XQuery(
      "boolean(xs:hexBinary(\"03\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Test: K-SeqBooleanFunc-6 Purpose: A test whose essence is:
   *          `boolean(xs:base64Binary("aaaa"))`. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc6() {
    final XQuery query = new XQuery(
      "boolean(xs:base64Binary(\"aaaa\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Test: K-SeqBooleanFunc-7 Purpose: Extracting EBV from xs:anyURI is allowed. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc7() {
    final XQuery query = new XQuery(
      "boolean(xs:anyURI(\"example.com/\"))",
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
   *  Test: K-SeqBooleanFunc-8 Purpose: Extracting EBV from xs:anyURI is allowed. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc8() {
    final XQuery query = new XQuery(
      "not(boolean(xs:anyURI(\"\")))",
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
   *  Test: K-SeqBooleanFunc-9 Purpose: fn:boolean() invoked on an non-empty xs:string
   *          should return true. .
   */
  @org.junit.Test
  public void kSeqBooleanFunc9() {
    final XQuery query = new XQuery(
      "boolean(\"string\")",
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
   * boolean() applied to a node sequence.
   */
  @org.junit.Test
  public void boolean001() {
    final XQuery query = new XQuery(
      "boolean(//*:Open)",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
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
   * boolean() applied to an empty node sequence.
   */
  @org.junit.Test
  public void boolean002() {
    final XQuery query = new XQuery(
      "boolean(//*:NotAtAllOpen)",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
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
   * boolean() applied to a sequence whose first item is a node.
   */
  @org.junit.Test
  public void boolean003() {
    final XQuery query = new XQuery(
      "boolean((/, 93.7))",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
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
   * boolean() applied to a sequence whose second item is a node.
   */
  @org.junit.Test
  public void boolean004() {
    final XQuery query = new XQuery(
      "boolean((93.7, /))",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   * boolean() applied to a sequence of two booleans.
   */
  @org.junit.Test
  public void boolean005() {
    final XQuery query = new XQuery(
      "boolean((true(), false()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   * boolean() applied to a sequence of two integers.
   */
  @org.junit.Test
  public void boolean006() {
    final XQuery query = new XQuery(
      "boolean((1, 0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   * boolean() applied to a sequence of two strings.
   */
  @org.junit.Test
  public void boolean007() {
    final XQuery query = new XQuery(
      "boolean((\"\", \"a\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   * boolean() applied to a sequence of of a boolean and then an empty
   *          node-set.
   */
  @org.junit.Test
  public void boolean008() {
    final XQuery query = new XQuery(
      "boolean((true(), //aspidistra))",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
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
   * boolean() applied to the string "false".
   */
  @org.junit.Test
  public void boolean009() {
    final XQuery query = new XQuery(
      "boolean(string(false()))",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
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
   * test fn:boolean on a count-preserving function .
   */
  @org.junit.Test
  public void cbclBoolean001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:generate($count as xs:integer) {\n" +
      "      \t\tif ($count < 0) then \"string\" \n" +
      "      \t\telse for $x in 1 to $count \n" +
      "      \t\t\t return \n" +
      "      \t\t\t \tif ($x mod 3 = 0) then <a /> \n" +
      "      \t\t\t \telse if ($x mod 3 = 1) then <b /> \n" +
      "      \t\t\t \telse <c /> \n" +
      "      \t}; \n" +
      "      \tfn:boolean(fn:reverse( local:generate(5) ))\n" +
      "      ",
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
   *  test fn:boolean on fn:count function.
   */
  @org.junit.Test
  public void cbclBoolean002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:generate($count as xs:integer) {\n" +
      "      \t\tfor $x in 1 to $count \n" +
      "      \t\treturn \n" +
      "      \t\t\tif ($x mod 3 = 0) then <a /> \n" +
      "      \t\t\telse if ($x mod 3 = 1) then <b /> \n" +
      "      \t\t\telse <c /> \n" +
      "      \t}; \n" +
      "      \tfn:boolean(fn:count( local:generate(5) ))\n" +
      "      ",
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
   * Test fn:boolean on sequence of node and string.
   */
  @org.junit.Test
  public void cbclBoolean003() {
    final XQuery query = new XQuery(
      "declare function local:f() { (<a/>, \"a\") }; boolean(local:f())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        error("FORG0006")
      )
    );
  }

  /**
   * Tests negation of fn:boolean .
   */
  @org.junit.Test
  public void cbclBoolean004() {
    final XQuery query = new XQuery(
      "not(boolean(for $x in 1 to 10 return $x * $x))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   * test fn:boolean on a text node.
   */
  @org.junit.Test
  public void cbclBoolean005() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:repeat($count as xs:integer, $arg as xs:string) { for $x in 1 to $count return $arg }; \n" +
      "      fn:boolean( text { local:repeat(0, \"string\") } )\n" +
      "      ",
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
   * test fn:boolean on a text node .
   */
  @org.junit.Test
  public void cbclBoolean006() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:repeat($count as xs:integer, $arg as xs:string) as item()* { \n" +
      "      \tif ($count lt 0) then '$count must not be negative' \n" +
      "      \telse text { for $x in 1 to $count return $arg }\n" +
      "      }; \n" +
      "      fn:boolean( local:repeat(0, \"string\") )",
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
   *  Name: context-item-1 Description: Evaluation of contect item expression for
   *          which the context item is not defined. .
   */
  @org.junit.Test
  public void contextItem1() {
    final XQuery query = new XQuery(
      "let $f := function() { fn:boolean(.) } return $f()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Test: fn-boolean-050.xq Written By: Carmelo Montanez Date: February 6, 2006
   *          Purpose: Raise error condition FORG0006 for fn:boolean. .
   */
  @org.junit.Test
  public void fnBoolean050() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:dateTime(\"1999-12-31T00:00:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Test: fn-boolean-mixed-args-001.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: empty sequence .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs001() {
    final XQuery query = new XQuery(
      "fn:boolean(())",
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
   *  Test: fn-boolean-mixed-args-002.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: string .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs002() {
    final XQuery query = new XQuery(
      "fn:boolean(\"\")",
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
   *  Test: fn-boolean-mixed-args-003.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: boolean function .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs003() {
    final XQuery query = new XQuery(
      "fn:boolean(false())",
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
   *  Test: fn-boolean-mixed-args-004.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: boolean function .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs004() {
    final XQuery query = new XQuery(
      "fn:boolean(true())",
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
   *  Test: fn-boolean-mixed-args-005.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: string .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs005() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:string(\"\"))",
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
   *  Test: fn-boolean-mixed-args-006.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: string .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs006() {
    final XQuery query = new XQuery(
      "fn:boolean(('a'))",
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
   *  Test: fn-boolean-mixed-args-007.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: string .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs007() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:string('abc'))",
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
   *  Test: fn-boolean-mixed-args-008.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: integer .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs008() {
    final XQuery query = new XQuery(
      "fn:boolean(0)",
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
   *  Test: fn-boolean-mixed-args-009.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: integer .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs009() {
    final XQuery query = new XQuery(
      "fn:boolean(1)",
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
   *  Test: fn-boolean-mixed-args-010.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: integer .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs010() {
    final XQuery query = new XQuery(
      "fn:boolean(-1)",
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
   *  Test: fn-boolean-mixed-args-011.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs011() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float('NaN'))",
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
   *  Test: fn-boolean-mixed-args-012.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs012() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float('-INF'))",
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
   *  Test: fn-boolean-mixed-args-013.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs013() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float('INF'))",
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
   *  Test: fn-boolean-mixed-args-014.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs014() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float(0))",
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
   *  Test: fn-boolean-mixed-args-015.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs015() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float(1))",
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
   *  Test: fn-boolean-mixed-args-016.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs016() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float(-1))",
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
   *  Test: fn-boolean-mixed-args-017.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs017() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double('NaN'))",
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
   *  Test: fn-boolean-mixed-args-018.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs018() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double('-INF'))",
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
   *  Test: fn-boolean-mixed-args-019.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs019() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double('INF'))",
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
   *  Test: fn-boolean-mixed-args-020.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs020() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double(0))",
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
   *  Test: fn-boolean-mixed-args-021.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs021() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double(1))",
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
   *  Test: fn-boolean-mixed-args-022.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs022() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double('1'))",
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
   *  Test: fn-boolean-mixed-args-023.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs023() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double('NaN'))",
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
   *  Test: fn-boolean-mixed-args-024.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: decimal .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs024() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:decimal('9.99999999999999999999999999'))",
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
   *  Test: fn-boolean-mixed-args-025.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: decimal .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs025() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:decimal('-123456789.123456789123456789'))",
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
   *  Test: fn-boolean-mixed-args-026.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: decimal .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs026() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:decimal('0'))",
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
   *  Test: fn-boolean-mixed-args-027.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: decimal .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs027() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:decimal('1'))",
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
   *  Test: fn-boolean-mixed-args-028.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: decimal .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs028() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:decimal('-1'))",
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
   *  Test: fn-boolean-mixed-args-029.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: integer .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs029() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:integer('0'))",
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
   *  Test: fn-boolean-mixed-args-030.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: integer .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs030() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:integer('1'))",
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
   *  Test: fn-boolean-mixed-args-031.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: integer .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs031() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:integer('-1'))",
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
   *  Test: fn-boolean-mixed-args-032.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: nonPositiveInteger .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs032() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonPositiveInteger('-99999999999999999'))",
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
   *  Test: fn-boolean-mixed-args-033.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: nonPositiveInteger .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs033() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonPositiveInteger('0'))",
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
   *  Test: fn-boolean-mixed-args-034.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: nonPositiveInteger .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs034() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonPositiveInteger('-1'))",
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
   *  Test: fn-boolean-mixed-args-035.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: negativeInteger .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs035() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:negativeInteger('-99999999999999999'))",
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
   *  Test: fn-boolean-mixed-args-036.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: negativeInteger .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs036() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:negativeInteger('-1'))",
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
   *  Test: fn-boolean-mixed-args-037.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: positiveInteger .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs037() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:positiveInteger('99999999999999999'))",
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
   *  Test: fn-boolean-mixed-args-038.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: positiveInteger .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs038() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:positiveInteger('1'))",
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
   *  Test: fn-boolean-mixed-args-039.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: long .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs039() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:long('9223372036854775807'))",
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
   *  Test: fn-boolean-mixed-args-040.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: long .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs040() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:long('-9223372036854775808'))",
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
   *  Test: fn-boolean-mixed-args-041.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: long .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs041() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:long('0'))",
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
   *  Test: fn-boolean-mixed-args-042.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: long .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs042() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:long('1'))",
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
   *  Test: fn-boolean-mixed-args-043.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: long .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs043() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:long('-1'))",
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
   *  Test: fn-boolean-mixed-args-044.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: int .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs044() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:int('2147483647'))",
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
   *  Test: fn-boolean-mixed-args-045.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: int .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs045() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:int('-2147483648'))",
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
   *  Test: fn-boolean-mixed-args-046.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: int .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs046() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:int('0'))",
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
   *  Test: fn-boolean-mixed-args-047.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: int .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs047() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:int('1'))",
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
   *  Test: fn-boolean-mixed-args-048.xq Written By: Pulkita Tyagi Date: Mon May 23
   *          04:50:42 2005 Purpose: arg: int .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs048() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:int('-1'))",
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
   *  Test: fn-boolean-mixed-args-049.xq Written By: Carmelo Montanez Date: January,
   *          19, 2006 Purpose: arg: anyURI .
   */
  @org.junit.Test
  public void fnBooleanMixedArgs049() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:anyURI(\"http://www.example.org/examples\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnBooleandbl1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double(\"-1.7976931348623157E308\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:double(mid range) .
   */
  @org.junit.Test
  public void fnBooleandbl1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double(\"0\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnBooleandbl1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:double(\"1.7976931348623157E308\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnBooleandec1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:decimal(\"-999999999999999999\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnBooleandec1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:decimal(\"617375191608514839\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnBooleandec1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:decimal(\"999999999999999999\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnBooleanflt1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float(\"-3.4028235E38\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:float(mid range) .
   */
  @org.junit.Test
  public void fnBooleanflt1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float(\"0\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnBooleanflt1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:float(\"3.4028235E38\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnBooleanint1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:int(\"-2147483648\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:int(mid range) .
   */
  @org.junit.Test
  public void fnBooleanint1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:int(\"-1873914410\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnBooleanint1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:int(\"2147483647\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnBooleanintg1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:integer(\"-999999999999999999\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnBooleanintg1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:integer(\"830993497117024304\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnBooleanintg1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:integer(\"999999999999999999\"))",
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
   * Evaluates The "boolean" function with the arguments set as follows: $arg =
   *          xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnBooleanlng1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:long(\"-92233720368547758\"))",
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
   *  Test: booleanlng1args-2 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnBooleanlng1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:long(\"-47175562203048468\"))",
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
   *  Test: booleanlng1args-3 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnBooleanlng1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:long(\"92233720368547758\"))",
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
   *  Test: booleannint1args-1 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnBooleannint1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:negativeInteger(\"-999999999999999999\"))",
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
   *  Test: booleannint1args-2 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnBooleannint1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:negativeInteger(\"-297014075999096793\"))",
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
   *  Test: booleannint1args-3 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnBooleannint1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:negativeInteger(\"-1\"))",
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
   *  Test: booleannni1args-1 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnBooleannni1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonNegativeInteger(\"0\"))",
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
   *  Test: booleannni1args-2 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnBooleannni1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonNegativeInteger(\"303884545991464527\"))",
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
   *  Test: booleannni1args-3 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnBooleannni1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonNegativeInteger(\"999999999999999999\"))",
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
   *  Test: booleannpi1args-1 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnBooleannpi1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonPositiveInteger(\"-999999999999999999\"))",
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
   *  Test: booleannpi1args-2 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnBooleannpi1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonPositiveInteger(\"-475688437271870490\"))",
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
   *  Test: booleannpi1args-3 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnBooleannpi1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:nonPositiveInteger(\"0\"))",
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
   *  Test: booleanpint1args-1 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnBooleanpint1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:positiveInteger(\"1\"))",
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
   *  Test: booleanpint1args-2 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnBooleanpint1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:positiveInteger(\"52704602390610033\"))",
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
   *  Test: booleanpint1args-3 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnBooleanpint1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:positiveInteger(\"999999999999999999\"))",
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
   *  Test: booleansht1args-1 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnBooleansht1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:short(\"-32768\"))",
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
   *  Test: booleansht1args-2 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnBooleansht1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:short(\"-5324\"))",
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
   *  Test: booleansht1args-3 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnBooleansht1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:short(\"32767\"))",
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
   *  Test: booleanulng1args-1 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnBooleanulng1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:unsignedLong(\"0\"))",
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
   *  Test: booleanulng1args-2 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnBooleanulng1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:unsignedLong(\"130747108607674654\"))",
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
   *  Test: booleanulng1args-3 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnBooleanulng1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:unsignedLong(\"184467440737095516\"))",
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
   *  Test: booleanusht1args-1 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnBooleanusht1args1() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:unsignedShort(\"0\"))",
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
   *  Test: booleanusht1args-2 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnBooleanusht1args2() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:unsignedShort(\"44633\"))",
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
   *  Test: booleanusht1args-3 Written By: Carmelo Montanez Date: Fri Dec 10 10:15:47
   *          GMT-05:00 2004 Purpose: Evaluates The "boolean" function with the arguments set as follows:
   *          $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnBooleanusht1args3() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:unsignedShort(\"65535\"))",
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
}
