package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the function true().
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnTrue extends QT3TestSet {

  /**
   *  A test whose essence is: `true(1)`. .
   */
  @org.junit.Test
  public void kTrueFunc1() {
    final XQuery query = new XQuery(
      "true(1)",
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
   *  A test whose essence is: `true() eq true()`. .
   */
  @org.junit.Test
  public void kTrueFunc2() {
    final XQuery query = new XQuery(
      "true() eq true()",
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
   *  A test whose essence is: `true()`. .
   */
  @org.junit.Test
  public void kTrueFunc3() {
    final XQuery query = new XQuery(
      "true()",
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
   *  Simple call to an "fn:true" function. .
   */
  @org.junit.Test
  public void fnTrue1() {
    final XQuery query = new XQuery(
      "fn:true()",
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
      &&
        assertType("xs:boolean")
      )
    );
  }

  /**
   *  Evaluation of an "fn:true" function with a comparison expression involving the "ge" operator. .
   */
  @org.junit.Test
  public void fnTrue10() {
    final XQuery query = new XQuery(
      "fn:true() ge fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "=" operator. .
   */
  @org.junit.Test
  public void fnTrue11() {
    final XQuery query = new XQuery(
      "fn:true() = fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "!=" operator. .
   */
  @org.junit.Test
  public void fnTrue12() {
    final XQuery query = new XQuery(
      "fn:true() != fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "<" operator. .
   */
  @org.junit.Test
  public void fnTrue13() {
    final XQuery query = new XQuery(
      "fn:true() < fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "<=" operator. .
   */
  @org.junit.Test
  public void fnTrue14() {
    final XQuery query = new XQuery(
      "fn:true() <= fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the ">" operator. .
   */
  @org.junit.Test
  public void fnTrue15() {
    final XQuery query = new XQuery(
      "fn:true() > fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the ">=" operator. .
   */
  @org.junit.Test
  public void fnTrue16() {
    final XQuery query = new XQuery(
      "fn:true() >= fn:true()",
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
   *  Evaluation of an "fn:true" function as an argument to an "xs:boolean" function. .
   */
  @org.junit.Test
  public void fnTrue17() {
    final XQuery query = new XQuery(
      "xs:boolean(fn:true())",
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
   *  Evaluation of an "fn:true" function as an argument to an "fn:string" function. .
   */
  @org.junit.Test
  public void fnTrue18() {
    final XQuery query = new XQuery(
      "fn:string(fn:true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"true\"")
    );
  }

  /**
   *  Evaluation of an "fn:true" function as arguments to an "fn:concat" function. .
   */
  @org.junit.Test
  public void fnTrue19() {
    final XQuery query = new XQuery(
      "fn:concat(xs:string(fn:true()),xs:string(fn:true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "truetrue")
    );
  }

  /**
   *  Evaluation of "fn:true" function as an argument to "fn:not" function. .
   */
  @org.junit.Test
  public void fnTrue2() {
    final XQuery query = new XQuery(
      "fn:not(fn:true())",
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
   *  Evaluation of an "fn:true" function as arguments to an "fn:contains" function. .
   */
  @org.junit.Test
  public void fnTrue20() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(fn:true()),xs:string(fn:true()))",
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
   *  Evaluation of an "fn:true" function as an argument to an "fn:string-length" function. .
   */
  @org.junit.Test
  public void fnTrue21() {
    final XQuery query = new XQuery(
      "fn:string-length(xs:string(fn:true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * true() as a function item.
   */
  @org.junit.Test
  public void fnTrue22() {
    final XQuery query = new XQuery(
      "let $t := true#0 return 3[$t()]",
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
   *  Evaluation of a "true" function with a logical expression involving the "and" operator. .
   */
  @org.junit.Test
  public void fnTrue3() {
    final XQuery query = new XQuery(
      "fn:true() and fn:true()",
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
   *  Evaluation of a "fn:true" function with a logical expression involving the "or" operator. .
   */
  @org.junit.Test
  public void fnTrue4() {
    final XQuery query = new XQuery(
      "fn:true() or fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "eq" operator. .
   */
  @org.junit.Test
  public void fnTrue5() {
    final XQuery query = new XQuery(
      "fn:true() eq fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "ne" operator. .
   */
  @org.junit.Test
  public void fnTrue6() {
    final XQuery query = new XQuery(
      "fn:true() ne fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "lt" operator. .
   */
  @org.junit.Test
  public void fnTrue7() {
    final XQuery query = new XQuery(
      "fn:true() lt fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "le" operator. .
   */
  @org.junit.Test
  public void fnTrue8() {
    final XQuery query = new XQuery(
      "fn:true() le fn:true()",
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
   *  Evaluation of an "fn:true" function with a comparison expression involving the "gt" operator. .
   */
  @org.junit.Test
  public void fnTrue9() {
    final XQuery query = new XQuery(
      "fn:true() gt fn:true()",
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
}
