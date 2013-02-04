package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the InstanceofExpr production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdInstanceofExpr extends QT3TestSet {

  /**
   *  A test whose essence is: `1 instance of item()`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf1() {
    final XQuery query = new XQuery(
      "1 instance of item()",
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
   *  A test whose essence is: `"a string" instance of xs:string`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf10() {
    final XQuery query = new XQuery(
      "\"a string\" instance of xs:string",
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
   *  A test whose essence is: `not("false" instance of xs:boolean)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf11() {
    final XQuery query = new XQuery(
      "not(\"false\" instance of xs:boolean)",
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
   *  A test whose essence is: `1.1 instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf12() {
    final XQuery query = new XQuery(
      "1.1 instance of xs:decimal",
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
   *  A test whose essence is: `not(1.1 instance of xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf13() {
    final XQuery query = new XQuery(
      "not(1.1 instance of xs:integer)",
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
   *  A test whose essence is: `xs:anyURI("http://www.example.com/") instance of xs:anyURI`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf14() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com/\") instance of xs:anyURI",
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
   *  A test whose essence is: `not(xs:anyURI("http://www.example.com/") instance of xs:string)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf15() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://www.example.com/\") instance of xs:string)",
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
   *  A test whose essence is: `not("a string" instance of xs:untypedAtomic)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf16() {
    final XQuery query = new XQuery(
      "not(\"a string\" instance of xs:untypedAtomic)",
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
   *  A test whose essence is: `(1, 2, 3, 4, 5) instance of xs:integer+`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf17() {
    final XQuery query = new XQuery(
      "(1, 2, 3, 4, 5) instance of xs:integer+",
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
   *  A test whose essence is: `not((1, 2, 3, 4, 5) instance of xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf18() {
    final XQuery query = new XQuery(
      "not((1, 2, 3, 4, 5) instance of xs:integer)",
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
   *  A test whose essence is: `not((1, 2, 3, 4, 5) instance of xs:integer?)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf19() {
    final XQuery query = new XQuery(
      "not((1, 2, 3, 4, 5) instance of xs:integer?)",
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
   *  item() type with comment appearing inside the paranteses. comment .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf2() {
    final XQuery query = new XQuery(
      "1 instance of item()",
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
   *  A test whose essence is: `(1, 2, 3, 4, 5) instance of xs:integer*`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf20() {
    final XQuery query = new XQuery(
      "(1, 2, 3, 4, 5) instance of xs:integer*",
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
   *  A test whose essence is: `(1, 2, 3, 4, 5) instance of item()+`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf21() {
    final XQuery query = new XQuery(
      "(1, 2, 3, 4, 5) instance of item()+",
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
   *  A test whose essence is: `not((1, 2, "a string", 4, 5) instance of xs:integer*)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf22() {
    final XQuery query = new XQuery(
      "not((1, 2, \"a string\", 4, 5) instance of xs:integer*)",
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
   *  A test whose essence is: `not((1, 2, 1.1, 4, 5) instance of xs:integer*)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf23() {
    final XQuery query = new XQuery(
      "not((1, 2, 1.1, 4, 5) instance of xs:integer*)",
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
   *  A test whose essence is: `not((1, 2, "a string", 4, 5) instance of xs:string*)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf24() {
    final XQuery query = new XQuery(
      "not((1, 2, \"a string\", 4, 5) instance of xs:string*)",
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
   *  A test whose essence is: `(1, 2, "a string", 4, 5) instance of xs:anyAtomicType*`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf25() {
    final XQuery query = new XQuery(
      "(1, 2, \"a string\", 4, 5) instance of xs:anyAtomicType*",
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
   *  A test whose essence is: `(1, 2, count("one"), 4, 5) instance of xs:integer+`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf26() {
    final XQuery query = new XQuery(
      "(1, 2, count(\"one\"), 4, 5) instance of xs:integer+",
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
   *  A test whose essence is: `not(1 instance of text())`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf27() {
    final XQuery query = new XQuery(
      "not(1 instance of text())",
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
   *  A test whose essence is: `not(1 instance of node())`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf28() {
    final XQuery query = new XQuery(
      "not(1 instance of node())",
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
   *  A test whose essence is: `not(1 instance of element())`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf29() {
    final XQuery query = new XQuery(
      "not(1 instance of element())",
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
   *  A test whose essence is: `1 instance of xs:integer`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf3() {
    final XQuery query = new XQuery(
      "1 instance of xs:integer",
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
   *  A test whose essence is: `not(1 instance of processing-instruction())`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf30() {
    final XQuery query = new XQuery(
      "not(1 instance of processing-instruction())",
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
   *  A test whose essence is: `not(1 instance of comment())`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf31() {
    final XQuery query = new XQuery(
      "not(1 instance of comment())",
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
   *  A comment appearing inside the paranteses of the sequence type text(). comment .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf32() {
    final XQuery query = new XQuery(
      "not(1 instance of text())",
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
   *  A comment appearing inside the paranteses of the sequence type node(). comment .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf33() {
    final XQuery query = new XQuery(
      "not(1 instance of node())",
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
   *  A comment appearing inside the paranteses of the sequence type element(). comment .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf34() {
    final XQuery query = new XQuery(
      "not(1 instance of element())",
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
   *  A comment appearing inside the paranteses of the sequence type processing-instruction(). comment .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf35() {
    final XQuery query = new XQuery(
      "not(1 instance of processing-instruction())",
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
   *  A comment appearing inside the paranteses of the sequence type comment(). comment .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf36() {
    final XQuery query = new XQuery(
      "not(1 instance of comment())",
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
   *  A test whose essence is: `not(1 instance of empty-sequence())`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf37() {
    final XQuery query = new XQuery(
      "not(1 instance of empty-sequence())",
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
   *  A test whose essence is: `not( (1, 2, 3) instance of empty-sequence())`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf38() {
    final XQuery query = new XQuery(
      "not( (1, 2, 3) instance of empty-sequence())",
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
   *  A test whose essence is: `() instance of empty-sequence()`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf39() {
    final XQuery query = new XQuery(
      "() instance of empty-sequence()",
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
   *  A test whose essence is: `1 instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf4() {
    final XQuery query = new XQuery(
      "1 instance of xs:decimal",
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
   *  Comment appearing inside the paranteses of empty-sequence(). comment .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf40() {
    final XQuery query = new XQuery(
      "() instance of empty-sequence()",
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
   *  A test whose essence is: `(((()))) instance of empty-sequence()`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf41() {
    final XQuery query = new XQuery(
      "(((()))) instance of empty-sequence()",
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
   *  A test whose essence is: `((), (), ()) instance of empty-sequence()`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf42() {
    final XQuery query = new XQuery(
      "((), (), ()) instance of empty-sequence()",
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
   *  A test whose essence is: `((), "xs:string") instance of xs:string`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf43() {
    final XQuery query = new XQuery(
      "((), \"xs:string\") instance of xs:string",
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
   *  A test whose essence is: `("xs:string", (), "xs:string") instance of xs:string+`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf44() {
    final XQuery query = new XQuery(
      "(\"xs:string\", (), \"xs:string\") instance of xs:string+",
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
   *  A test whose essence is: `(1.1, (), 1) instance of xs:decimal+`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf45() {
    final XQuery query = new XQuery(
      "(1.1, (), 1) instance of xs:decimal+",
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
   *  A test whose essence is: `not("a string" instance of xs:NOTATION)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf46() {
    final XQuery query = new XQuery(
      "not(\"a string\" instance of xs:NOTATION)",
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
   *  A test whose essence is: `not("a string" instance of xs:QName)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf47() {
    final XQuery query = new XQuery(
      "not(\"a string\" instance of xs:QName)",
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
   *  A test whose essence is: `1.1 instance of xs:decimal and not(1.1 instance of xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf48() {
    final XQuery query = new XQuery(
      "1.1 instance of xs:decimal and not(1.1 instance of xs:integer)",
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
   *  A type is referenced which doesn't exist. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf49() {
    final XQuery query = new XQuery(
      "3 instance of prefixDoesNotExist:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  A test whose essence is: `1 instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf5() {
    final XQuery query = new XQuery(
      "1 instance of xs:integer?",
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
   *  A type is referenced which doesn't exist. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf50() {
    final XQuery query = new XQuery(
      "3 instance of xs:doesNotExist",
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
   *  A type is referenced which doesn't exist. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf51() {
    final XQuery query = new XQuery(
      "3 instance of xs:qname",
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
   *  A type is referenced which doesn't exist. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf52() {
    final XQuery query = new XQuery(
      "3 instance of none",
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
   *  A type is referenced which doesn't exist. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf53() {
    final XQuery query = new XQuery(
      "3 instance of void",
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
   *  A type is referenced which doesn't exist. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf54() {
    final XQuery query = new XQuery(
      "3 instance of none()",
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
        error("XPST0051")
      ||
        error("XPST0003")
      )
    );
  }

  /**
   *  A type is referenced which doesn't exist. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf55() {
    final XQuery query = new XQuery(
      "3 instance of void()",
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
        error("XPST0051")
      ||
        error("XPST0003")
      )
    );
  }

  /**
   *  The fn:error() function in combination with 'instance of'. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf56() {
    final XQuery query = new XQuery(
      "error() instance of xs:integer",
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
        error("FOER0000")
      )
    );
  }

  /**
   *  The fn:error() function in combination with 'instance of'. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf57() {
    final XQuery query = new XQuery(
      "error() instance of xs:integer*",
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
        error("FOER0000")
      )
    );
  }

  /**
   *  The fn:error() function in combination with 'instance of'. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf58() {
    final XQuery query = new XQuery(
      "error() instance of empty-sequence()",
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
        error("FOER0000")
      )
    );
  }

  /**
   *  The fn:error() function in combination with 'instance of'. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf59() {
    final XQuery query = new XQuery(
      "(error(), 1) instance of empty-sequence()",
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
        assertBoolean(false)
      ||
        error("FOER0000")
      )
    );
  }

  /**
   *  A test whose essence is: `1 instance of xs:integer+`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf6() {
    final XQuery query = new XQuery(
      "1 instance of xs:integer+",
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
   *  The fn:error() function in combination with 'instance of'. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf60() {
    final XQuery query = new XQuery(
      "(1, error()) instance of xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  The fn:error() function in combination with 'instance of'. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf61() {
    final XQuery query = new XQuery(
      "(error(), 1) instance of xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  The fn:error() function in combination with 'instance of'. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf62() {
    final XQuery query = new XQuery(
      "(1, error()) instance of xs:integer*",
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
        error("FOER0000")
      )
    );
  }

  /**
   *  The fn:error() function in combination with 'instance of'. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf63() {
    final XQuery query = new XQuery(
      "(error(), 1) instance of xs:integer*",
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
        error("FOER0000")
      )
    );
  }

  /**
   *  A test whose essence is: `not(((10)div(3)) instance of xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf64() {
    final XQuery query = new XQuery(
      "not(((10)div(3)) instance of xs:integer)",
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
   *  A test whose essence is: `((10)idiv(3)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf65() {
    final XQuery query = new XQuery(
      "((10)idiv(3)) instance of xs:integer",
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
   *  A test whose essence is: `((10)mod(3)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf66() {
    final XQuery query = new XQuery(
      "((10)mod(3)) instance of xs:integer",
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
   *  A test whose essence is: `1 instance of xs:integer*`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf7() {
    final XQuery query = new XQuery(
      "1 instance of xs:integer*",
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
   *  A test whose essence is: `false() instance of xs:boolean`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf8() {
    final XQuery query = new XQuery(
      "false() instance of xs:boolean",
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
   *  A test whose essence is: `false() instance of xs:anyAtomicType`. .
   */
  @org.junit.Test
  public void kSeqExprInstanceOf9() {
    final XQuery query = new XQuery(
      "false() instance of xs:anyAtomicType",
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
   *  Test an element node against type element(). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf1() {
    final XQuery query = new XQuery(
      "<e/> instance of element()",
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
   *  Test an attribute node against type attribute(*). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf10() {
    final XQuery query = new XQuery(
      "attribute e{\"content\"} instance of attribute(*)",
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
   *  Test using element(). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf100() {
    final XQuery query = new XQuery(
      "<a><b/></a>/(b instance of element(), * instance of element())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true")
    );
  }

  /**
   *  Test an attribute node against type element(e). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf11() {
    final XQuery query = new XQuery(
      "attribute e{\"content\"} instance of element(e)",
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
   *  Test an attribute node against type element(). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf12() {
    final XQuery query = new XQuery(
      "attribute e{\"content\"} instance of element()",
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
   *  Test an attribute node against type element(*). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf13() {
    final XQuery query = new XQuery(
      "attribute e{\"content\"} instance of element(*)",
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
   *  Test an attribute node against type element(name). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf14() {
    final XQuery query = new XQuery(
      "attribute e{\"content\"} instance of attribute(name)",
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
   *  Check the return type of xs:nonPositiveInteger in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf15() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(0) instance of xs:nonPositiveInteger",
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
   *  Check the return type of xs:negativeInteger in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf16() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(-1) instance of xs:negativeInteger",
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
   *  Check the return type of xs:long in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf17() {
    final XQuery query = new XQuery(
      "xs:long(0) instance of xs:long",
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
   *  Check the return type of xs:int in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf18() {
    final XQuery query = new XQuery(
      "xs:int(0) instance of xs:int",
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
   *  Check the return type of xs:short in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf19() {
    final XQuery query = new XQuery(
      "xs:short(0) instance of xs:short",
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
   *  Test an element node against type element(*). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf2() {
    final XQuery query = new XQuery(
      "<e/> instance of element(*)",
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
   *  Check the return type of xs:byte in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf20() {
    final XQuery query = new XQuery(
      "xs:byte(0) instance of xs:byte",
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
   *  Check the return type of xs:nonNegativeInteger in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf21() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(0) instance of xs:nonNegativeInteger",
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
   *  Check the return type of xs:nonNegativeInteger in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf22() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(0) instance of xs:nonNegativeInteger",
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
   *  Check the return type of xs:unsignedLong in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf23() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(0) instance of xs:unsignedLong",
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
   *  Check the return type of xs:unsignedInt in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf24() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(0) instance of xs:unsignedInt",
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
   *  Check the return type of xs:unsignedShort in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf25() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(0) instance of xs:unsignedShort",
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
   *  Check the return type of xs:unsignedByte in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf26() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(0) instance of xs:unsignedByte",
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
   *  Check the return type of xs:positiveInteger in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf27() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(1) instance of xs:positiveInteger",
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
   *  Check the return type of xs:normalizedString in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf28() {
    final XQuery query = new XQuery(
      "xs:normalizedString(\"NCName\") instance of xs:normalizedString",
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
   *  Check the return type of xs:token in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf29() {
    final XQuery query = new XQuery(
      "xs:token(\"NCName\") instance of xs:token",
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
   *  Test an element node against type element(*). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf3() {
    final XQuery query = new XQuery(
      "<e/> instance of element(e)",
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
   *  Check the return type of xs:language in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf30() {
    final XQuery query = new XQuery(
      "xs:language(\"NCName\") instance of xs:language",
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
   *  Check the return type of xs:NMTOKEN in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf31() {
    final XQuery query = new XQuery(
      "xs:NMTOKEN(\"NCName\") instance of xs:NMTOKEN",
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
   *  Check the return type of xs:Name in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf32() {
    final XQuery query = new XQuery(
      "xs:Name(\"NCName\") instance of xs:Name",
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
   *  Check the return type of xs:NCName in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf33() {
    final XQuery query = new XQuery(
      "xs:NCName(\"NCName\") instance of xs:NCName",
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
   *  Check the return type of xs:ID in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf34() {
    final XQuery query = new XQuery(
      "xs:ID(\"NCName\") instance of xs:ID",
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
   *  Check the return type of xs:IDREF in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf35() {
    final XQuery query = new XQuery(
      "xs:IDREF(\"NCName\") instance of xs:IDREF",
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
   *  Check the return type of xs:ENTITY in a simple way. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf36() {
    final XQuery query = new XQuery(
      "xs:ENTITY(\"NCName\") instance of xs:ENTITY",
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
   *  Check that xs:normalizedString has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf37() {
    final XQuery query = new XQuery(
      "xs:normalizedString(\"ncname\") instance of xs:string",
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
   *  Check that xs:token has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf38() {
    final XQuery query = new XQuery(
      "xs:token(\"ncname\") instance of xs:normalizedString",
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
   *  Check that xs:language has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf39() {
    final XQuery query = new XQuery(
      "xs:language(\"ncname\") instance of xs:token",
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
   *  Test an element node against type element(name). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf4() {
    final XQuery query = new XQuery(
      "<e/> instance of element(name)",
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
   *  Check that xs:NMTOKEN has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf40() {
    final XQuery query = new XQuery(
      "xs:NMTOKEN(\"ncname\") instance of xs:token",
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
   *  Check that xs:Name has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf41() {
    final XQuery query = new XQuery(
      "xs:Name(\"ncname\") instance of xs:token",
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
   *  Check that xs:NCName has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf42() {
    final XQuery query = new XQuery(
      "xs:NCName(\"ncname\") instance of xs:Name",
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
   *  Check that xs:ID has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf43() {
    final XQuery query = new XQuery(
      "xs:ID(\"ncname\") instance of xs:NCName",
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
   *  Check that xs:IDREF has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf44() {
    final XQuery query = new XQuery(
      "xs:IDREF(\"ncname\") instance of xs:NCName",
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
   *  Check that xs:ENTITY has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf45() {
    final XQuery query = new XQuery(
      "xs:ENTITY(\"ncname\") instance of xs:NCName",
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
   *  Check that xs:nonPositiveInteger has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf46() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") instance of xs:integer",
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
   *  Check that xs:negativeInteger has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf47() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") instance of xs:nonPositiveInteger",
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
   *  Check that xs:long has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf48() {
    final XQuery query = new XQuery(
      "xs:long(\"0\") instance of xs:integer",
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
   *  Check that xs:int has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf49() {
    final XQuery query = new XQuery(
      "xs:int(\"0\") instance of xs:long",
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
   *  Test an element node against type attribute(). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf5() {
    final XQuery query = new XQuery(
      "<e/> instance of attribute()",
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
   *  Check that xs:short has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf50() {
    final XQuery query = new XQuery(
      "xs:short(\"0\") instance of xs:int",
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
   *  Check that xs:byte has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf51() {
    final XQuery query = new XQuery(
      "xs:byte(\"0\") instance of xs:short",
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
   *  Check that xs:nonNegativeInteger has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf52() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") instance of xs:integer",
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
   *  Check that xs:unsignedLong has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf53() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") instance of xs:nonNegativeInteger",
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
   *  Check that xs:unsignedInt has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf54() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(\"0\") instance of xs:unsignedLong",
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
   *  Check that xs:unsignedShort has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf55() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") instance of xs:unsignedInt",
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
   *  Check that xs:unsignedByte has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf56() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(\"0\") instance of xs:unsignedShort",
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
   *  Check that xs:positiveInteger has the correct parent type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf57() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") instance of xs:nonNegativeInteger",
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
   *  Check that xs:nonNegativeInteger is not a child of nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf58() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"1\") instance of xs:nonPositiveInteger",
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
   *  Check that xs:nonNegativeInteger is not a child of negativeInteger. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf59() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"1\") instance of xs:negativeInteger",
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
   *  Test an element node against type attribute(*). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf6() {
    final XQuery query = new XQuery(
      "<e/> instance of attribute(*)",
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
   *  Check that xs:positiveInteger is not a child of negativeInteger. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf60() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") instance of xs:negativeInteger",
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
   *  Check that xs:positiveInteger is not a child of nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf61() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") instance of xs:nonPositiveInteger",
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
   *  Check that xs:long is not a child of negativeInteger. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf62() {
    final XQuery query = new XQuery(
      "xs:long(\"1\") instance of xs:negativeInteger",
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
   *  Check that xs:long is not a child of nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf63() {
    final XQuery query = new XQuery(
      "xs:long(\"1\") instance of xs:nonPositiveInteger",
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
   *  Check that xs:long is not a child of nonNegativeInteger. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf64() {
    final XQuery query = new XQuery(
      "xs:long(\"1\") instance of xs:nonNegativeInteger",
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
   *  Check that xs:long is not a child of positiveInteger. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf65() {
    final XQuery query = new XQuery(
      "xs:long(\"1\") instance of xs:positiveInteger",
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
   *  Check that xs:token is not a child of xs:NCName. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf66() {
    final XQuery query = new XQuery(
      "xs:token(\"ncname\") instance of xs:NCName",
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
   *  Check that xs:ID is not a child of xs:NCName. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf67() {
    final XQuery query = new XQuery(
      "xs:ID(\"ncname\") instance of xs:IDREF",
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
   *  Check that xs:ENTITY is not a child of xs:NCName. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf68() {
    final XQuery query = new XQuery(
      "xs:ENTITY(\"ncname\") instance of xs:IDREF",
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
   *  Check that xs:language is not a child of xs:NMTOKEN. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf69() {
    final XQuery query = new XQuery(
      "xs:language(\"ncname\") instance of xs:NMTOKEN",
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
   *  Test an element node against type attribute(e). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf7() {
    final XQuery query = new XQuery(
      "<e/> instance of attribute(e)",
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
   *  Check that xs:Name is not a child of xs:language. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf70() {
    final XQuery query = new XQuery(
      "xs:Name(\"ncname\") instance of xs:language",
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
   *  Check that xs:normalizedString is not a child of xs:token. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf71() {
    final XQuery query = new XQuery(
      "xs:normalizedString(\"ncname\") instance of xs:token",
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
   *  Ensure a name test with the attribute axis gets the correct type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf72() {
    final XQuery query = new XQuery(
      "<e a=\"\"/>/attribute::a instance of attribute(a)",
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
   *  Ensure a name test with the abbreviated attribute axis gets the correct type. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf73() {
    final XQuery query = new XQuery(
      "<e a=\"\"/>/@a instance of attribute(a)",
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
   *  Ensure that a cast to xs:byte is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf74() {
    final XQuery query = new XQuery(
      "xs:byte(xs:double(\"1\")) instance of xs:byte",
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
   *  Ensure that a cast to xs:nonPositiveInteger is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf75() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(xs:double(\"0\")) instance of xs:nonPositiveInteger",
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
   *  Ensure that a cast to xs:negativeInteger is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf76() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(xs:double(\"-4\")) instance of xs:negativeInteger",
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
   *  Ensure that a cast to xs:long is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf77() {
    final XQuery query = new XQuery(
      "xs:long(xs:double(\"-4\")) instance of xs:long",
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
   *  Ensure that a cast to xs:int is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf78() {
    final XQuery query = new XQuery(
      "xs:int(xs:double(\"-4\")) instance of xs:int",
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
   *  Ensure that a cast to xs:short is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf79() {
    final XQuery query = new XQuery(
      "xs:short(xs:double(\"-4\")) instance of xs:short",
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
   *  Test an attribute node against type element(name). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf8() {
    final XQuery query = new XQuery(
      "attribute e{\"content\"} instance of attribute(e)",
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
   *  Ensure that a cast to xs:nonNegativeInteger is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf80() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(xs:double(\"4\")) instance of xs:nonNegativeInteger",
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
   *  Ensure that a cast to xs:unsignedLong is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf81() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(xs:double(\"4\")) instance of xs:unsignedLong",
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
   *  Ensure that a cast to xs:unsignedInt is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf82() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(xs:double(\"4\")) instance of xs:unsignedInt",
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
   *  Ensure that a cast to xs:unsignedShort is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf83() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(xs:double(\"4\")) instance of xs:unsignedShort",
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
   *  Ensure that a cast to xs:unsignedByte is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf84() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(xs:double(\"4\")) instance of xs:unsignedByte",
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
   *  Ensure that a cast to xs:positiveInteger is of the right type, when casting from xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf85() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(xs:double(\"4\")) instance of xs:positiveInteger",
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
   *  Ensure that a cast to xs:byte is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf86() {
    final XQuery query = new XQuery(
      "xs:byte(xs:float(\"1\")) instance of xs:byte",
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
   *  Ensure that a cast to xs:nonPositiveInteger is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf87() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(xs:float(\"0\")) instance of xs:nonPositiveInteger",
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
   *  Ensure that a cast to xs:negativeInteger is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf88() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(xs:float(\"-4\")) instance of xs:negativeInteger",
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
   *  Ensure that a cast to xs:long is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf89() {
    final XQuery query = new XQuery(
      "xs:long(xs:float(\"-4\")) instance of xs:long",
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
   *  Test an attribute node against type attribute(). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf9() {
    final XQuery query = new XQuery(
      "attribute e{\"content\"} instance of attribute()",
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
   *  Ensure that a cast to xs:int is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf90() {
    final XQuery query = new XQuery(
      "xs:int(xs:float(\"-4\")) instance of xs:int",
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
   *  Ensure that a cast to xs:short is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf91() {
    final XQuery query = new XQuery(
      "xs:short(xs:float(\"-4\")) instance of xs:short",
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
   *  Ensure that a cast to xs:nonNegativeInteger is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf92() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(xs:float(\"4\")) instance of xs:nonNegativeInteger",
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
   *  Ensure that a cast to xs:unsignedLong is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf93() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(xs:float(\"4\")) instance of xs:unsignedLong",
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
   *  Ensure that a cast to xs:unsignedInt is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf94() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(xs:float(\"4\")) instance of xs:unsignedInt",
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
   *  Ensure that a cast to xs:unsignedShort is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf95() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(xs:float(\"4\")) instance of xs:unsignedShort",
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
   *  Ensure that a cast to xs:unsignedByte is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf96() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(xs:float(\"4\")) instance of xs:unsignedByte",
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
   *  Ensure that a cast to xs:positiveInteger is of the right type, when casting from xs:float. .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf97() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(xs:float(\"4\")) instance of xs:positiveInteger",
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
   *  Instance of involving empty-sequence(). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf98() {
    final XQuery query = new XQuery(
      "node-name(text {\"\"}) instance of empty-sequence()",
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
   *  Test using attribute(). .
   */
  @org.junit.Test
  public void k2SeqExprInstanceOf99() {
    final XQuery query = new XQuery(
      "<e attr=\"\"/>/(@attr instance of attribute(), @* instance of attribute())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true")
    );
  }

  /**
   *  Test that the types of variables are correctly preserved on the stack. .
   */
  @org.junit.Test
  public void cbclInstanceOf001() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $x in (xs:integer(1), xs:decimal(1)) return (((($x + 1) * 2) - 1) instance of xs:integer)\n" +
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
      assertStringValue(false, "true false")
    );
  }

  /**
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof10() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof100() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof101() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof102() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof103() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof104() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof105() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof106() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof107() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof108() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof109() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof11() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:int",
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
   * purpose: Evaluation of "instance of" expression for pattern "integer instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof110() {
    final XQuery query = new XQuery(
      "12678967543233 instance of xs:int",
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
   * purpose: Test that a value is not testable as an instance of a list type. .
   */
  @org.junit.Test
  public void instanceof111() {
    final XQuery query = new XQuery(
      "xs:NMTOKEN('abc') instance of xs:NMTOKENS",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof12() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:dateTime",
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof121() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(*)\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof122() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(node()?) as xs:string\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof123() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(node()) as xs:string\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof124() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(element(A)?) as xs:string\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof125() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(element(A)) as xs:string\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof126() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(element(A)) as xs:anyAtomicType\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof127() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(element(A)) as xs:string+\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof128() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(element(A)) as xs:NCName\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof129() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(node()*) as xs:string\n" +
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof13() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:time",
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof130() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#1 instance of function(item()) as xs:string\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof131() {
    final XQuery query = new XQuery(
      "\n" +
      "         name#0 instance of function(item()) as xs:string\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof132() {
    final XQuery query = new XQuery(
      "\n" +
      "         filter#2 instance of function(function(item()) as xs:boolean, item()*) as item()*\n" +
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
   * Instance of with function types.
   */
  @org.junit.Test
  public void instanceof133() {
    final XQuery query = new XQuery(
      "\n" +
      "         filter#2 instance of function(function(item()*) as xs:boolean, item()*) as item()*\n" +
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof14() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof15() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof16() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof17() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof18() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof19() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof20() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof21() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "time instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof22() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:10.5Z\") instance of xs:int",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof23() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof24() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof25() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof26() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof27() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof28() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof29() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof3() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof30() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof31() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof32() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "date instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof33() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-01-01+05:00\") instance of xs:int",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof34() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof35() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof36() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof37() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof38() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof39() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof40() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof41() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof42() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof43() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "boolean instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof44() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") instance of xs:int",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof45() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof46() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof47() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof48() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof49() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof50() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof51() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof52() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof53() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof54() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "float instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof55() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:int",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof56() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof57() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof58() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof59() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof6() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof60() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof61() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof62() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof63() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof64() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof65() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "double instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof66() {
    final XQuery query = new XQuery(
      "1267.43233E12 instance of xs:int",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof67() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof68() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof69() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof7() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof70() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof71() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof72() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof73() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof74() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof75() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof76() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "anyURI instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof77() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.com\") instance of xs:int",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof78() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof79() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof8() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof80() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof81() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof82() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof83() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof84() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof85() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof86() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof87() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "string instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof88() {
    final XQuery query = new XQuery(
      "\"A String Function\" instance of xs:int",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:dateTime". .
   */
  @org.junit.Test
  public void instanceof89() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:dateTime",
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
   * purpose: Evaluation of "instance of" expression for pattern "dateTime instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof9() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-04-02T12:00:00Z\") instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:time". .
   */
  @org.junit.Test
  public void instanceof90() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:time",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:date". .
   */
  @org.junit.Test
  public void instanceof91() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:date",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:boolean". .
   */
  @org.junit.Test
  public void instanceof92() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:boolean",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:float". .
   */
  @org.junit.Test
  public void instanceof93() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:float",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:double". .
   */
  @org.junit.Test
  public void instanceof94() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:double",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:anyURI". .
   */
  @org.junit.Test
  public void instanceof95() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:anyURI",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:string". .
   */
  @org.junit.Test
  public void instanceof96() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:string",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:decimal". .
   */
  @org.junit.Test
  public void instanceof97() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:decimal",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:integer". .
   */
  @org.junit.Test
  public void instanceof98() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:integer",
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
   * purpose: Evaluation of "instance of" expression for pattern "decimal instance of xs:int". .
   */
  @org.junit.Test
  public void instanceof99() {
    final XQuery query = new XQuery(
      "12678967.543233 instance of xs:int",
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
