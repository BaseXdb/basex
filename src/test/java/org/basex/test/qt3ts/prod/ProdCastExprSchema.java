package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CastExpr production with user-defined types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCastExprSchema extends QT3TestSet {

  /**
   * Cast to a built-in list type IDREFS: using function lookup.
   */
  @org.junit.Test
  public void castAsListType10() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := function-lookup(QName('http://www.w3.org/2001/XMLSchema', 'IDREFS'), 1)\n" +
      "         return $f(\"a b c\")\n" +
      "       ",
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
        assertDeepEq("'a', 'b', 'c'")
      &&
        assertType("xs:IDREF*")
      )
    );
  }

  /**
   * Cast to a built-in list type IDREFS: using partial apply.
   */
  @org.junit.Test
  public void castAsListType11() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := xs:IDREFS(?)\n" +
      "         return $f(\"a b c\")\n" +
      "       ",
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
        assertDeepEq("'a', 'b', 'c'")
      &&
        assertType("xs:IDREF*")
      )
    );
  }

  /**
   * Cast to a built-in list type NMTOKENS..
   */
  @org.junit.Test
  public void castAsListType12() {
    final XQuery query = new XQuery(
      "\n" +
      "         \"a b c\" cast as xs:NMTOKENS\n" +
      "       ",
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
        assertDeepEq("'a', 'b', 'c'")
      &&
        assertType("xs:NMTOKEN*")
      )
    );
  }

  /**
   * Cast to a built-in list type NMTOKENS: fails because minLength=1.
   */
  @org.junit.Test
  public void castAsListType13() {
    final XQuery query = new XQuery(
      "\n" +
      "         xs:NMTOKENS(\"\")\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   * Cast to a built-in list type ENTITIES..
   */
  @org.junit.Test
  public void castAsListType14() {
    final XQuery query = new XQuery(
      "\n" +
      "         \"a b c\" cast as xs:ENTITIES\n" +
      "       ",
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
        assertDeepEq("'a', 'b', 'c'")
      &&
        assertType("xs:ENTITY*")
      )
    );
  }

  /**
   * Cast to a built-in list type ENTITIES: fails because minLength=1.
   */
  @org.junit.Test
  public void castAsListType15() {
    final XQuery query = new XQuery(
      "\n" +
      "         xs:ENTITIES(\"  \")\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   * Cast to a built-in list type ENTITIES: fails because an item is numeric.
   */
  @org.junit.Test
  public void castAsListType16() {
    final XQuery query = new XQuery(
      "\n" +
      "         xs:ENTITIES(\" a b c 12 \")\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   * Cast to a built-in list type ENTITIES: fails because input is not a string.
   */
  @org.junit.Test
  public void castAsListType17() {
    final XQuery query = new XQuery(
      "\n" +
      "         xs:ENTITIES(xs:anyURI(\"abcd\"))\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Dynamic constructor to a built-in list type ENTITIES: fails because input is not a string.
   */
  @org.junit.Test
  public void castAsListType18() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := xs:ENTITIES#1\n" +
      "         return $f(xs:anyURI(\"abcd\"))\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Cannot cast a sequence of xs:ENTITY values to xs:ENTITIES.
   */
  @org.junit.Test
  public void castAsListType19() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := xs:ENTITIES#1\n" +
      "         return $f(($f(\"abcd\"), $f(\"defg\")))\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Cannot use a list type name in a sequence type.
   */
  @org.junit.Test
  public void castAsListType20() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $v as xs:NMTOKENS := xs:NMTOKENS(\"a b c\")\n" +
      "         return count($v)\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0051")
    );
  }

  /**
   * Type of constructor function for casting to list.
   */
  @org.junit.Test
  public void castAsListType27() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f as function(xs:anyAtomicType) as xs:NMTOKEN* := xs:NMTOKENS#1\n" +
      "         let $v as xs:NMTOKEN* := $f(\"a b c\")\n" +
      "         return count($v)\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   * Castable to list: true.
   */
  @org.junit.Test
  public void castAsListType28() {
    final XQuery query = new XQuery(
      "\n" +
      "         \"a b c\" castable as xs:NMTOKENS\n" +
      "       ",
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
   * Castable to list: false.
   */
  @org.junit.Test
  public void castAsListType29() {
    final XQuery query = new XQuery(
      "\n" +
      "         \"a b 12\" castable as xs:IDREFS\n" +
      "       ",
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
   * Castable to list: false (violates minLength).
   */
  @org.junit.Test
  public void castAsListType30() {
    final XQuery query = new XQuery(
      "\n" +
      "         \" \" castable as xs:NMTOKENS\n" +
      "       ",
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
   * Cast to a built-in list type IDREFS..
   */
  @org.junit.Test
  public void castAsListType7() {
    final XQuery query = new XQuery(
      "\n" +
      "         \"a b c\" cast as xs:IDREFS\n" +
      "       ",
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
        assertDeepEq("'a', 'b', 'c'")
      &&
        assertType("xs:IDREF*")
      )
    );
  }

  /**
   * Cast to a built-in list type IDREFS: fails because minLength=1.
   */
  @org.junit.Test
  public void castAsListType8() {
    final XQuery query = new XQuery(
      "\n" +
      "         xs:IDREFS(\"\")\n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   * Cast to a built-in list type IDREFS: using function literal.
   */
  @org.junit.Test
  public void castAsListType9() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := xs:IDREFS#1 return $f(\"a b c\")\n" +
      "       ",
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
        assertDeepEq("'a', 'b', 'c'")
      &&
        assertType("xs:IDREF*")
      )
    );
  }

  /**
   *  Evaluates casting an xs:QName type to another xs:QName type. .
   */
  @org.junit.Test
  public void qnameCast1() {
    final XQuery query = new XQuery(
      "xs:QName(\"value1\") cast as xs:QName",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "value1")
    );
  }
}
