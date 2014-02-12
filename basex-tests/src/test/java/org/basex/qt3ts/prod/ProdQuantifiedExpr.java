package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the QuantifiedExpr production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdQuantifiedExpr extends QT3TestSet {

  /**
   *  Every-quantification carrying type declarations(cardinalities have no impact). .
   */
  @org.junit.Test
  public void kQuantExprWith1() {
    final XQuery query = new XQuery(
      "every $a as item()* in (1, 2), $b as item()* in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Some-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith10() {
    final XQuery query = new XQuery(
      "every $a as xs:anyURI in 1 satisfies count($a)",
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
   *  Every-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith11() {
    final XQuery query = new XQuery(
      "every $a as empty-sequence() in (1, 2), $b as xs:integer in $a satisfies $b",
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
   *  Every-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith12() {
    final XQuery query = new XQuery(
      "some $a as empty-sequence() in (1, 2), $b as xs:integer in $a satisfies $b",
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
   *  Every-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith13() {
    final XQuery query = new XQuery(
      "every $a as xs:integer+ in (1, 2), $b as xs:string* in $a satisfies $b",
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
   *  Some-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith14() {
    final XQuery query = new XQuery(
      "some $a as xs:integer+ in (1, 2), $b as xs:string* in $a satisfies $b",
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
   *  Some-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith15() {
    final XQuery query = new XQuery(
      "some $a as item()* in (1, 2), $b as xs:string in $a satisfies $b",
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
   *  Some-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith16() {
    final XQuery query = new XQuery(
      "every $a as item()* in (1, 2), $b as xs:string in $a satisfies $b",
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
   *  Every-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith17() {
    final XQuery query = new XQuery(
      "every $a as xs:integer+ in (1, 2), $b as xs:integer in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Every-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith18() {
    final XQuery query = new XQuery(
      "every $a as item()* in (1, 2), $b as xs:integer in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  No 'at' declaration is allowed in 'some'-quantification. .
   */
  @org.junit.Test
  public void kQuantExprWith19() {
    final XQuery query = new XQuery(
      "some $a as item() at $p in (1, 2) satisfies $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Every-quantification carrying type declarations(cardinalities have no impact). .
   */
  @org.junit.Test
  public void kQuantExprWith2() {
    final XQuery query = new XQuery(
      "some $a as item()* in (1, 2), $b as item()* in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  No 'at' declaration is allowed in 'every'-quantification. .
   */
  @org.junit.Test
  public void kQuantExprWith20() {
    final XQuery query = new XQuery(
      "every $a as item() at $p in (1, 2) satisfies $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Every-quantification with type-declaration. An implementation supporting the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kQuantExprWith21() {
    final XQuery query = new XQuery(
      "every $a as empty-sequence() in (), $b as xs:integer in $a satisfies $b",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Every-quantification with type-declaration. An implementation supporting the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kQuantExprWith22() {
    final XQuery query = new XQuery(
      "not(some $a as empty-sequence() in (), $b as xs:integer in $a satisfies $b)",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Every-quantification with type-declaration. .
   */
  @org.junit.Test
  public void kQuantExprWith23() {
    final XQuery query = new XQuery(
      "every $a as xs:integer in (1, 2), $b as xs:integer in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Every-quantification with type-declaration. .
   */
  @org.junit.Test
  public void kQuantExprWith24() {
    final XQuery query = new XQuery(
      "some $a as xs:integer in (1, 2), $b as xs:integer in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Every-quantification carrying type declarations(cardinalities have no impact). .
   */
  @org.junit.Test
  public void kQuantExprWith3() {
    final XQuery query = new XQuery(
      "every $a as item()? in (1, 2), $b as item()? in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Every-quantification carrying type declarations(cardinalities have no impact). .
   */
  @org.junit.Test
  public void kQuantExprWith4() {
    final XQuery query = new XQuery(
      "some $a as item()? in (1, 2), $b as item()? in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Every-quantification carrying type declarations(cardinalities have no impact). .
   */
  @org.junit.Test
  public void kQuantExprWith5() {
    final XQuery query = new XQuery(
      "every $a as item()+ in (1, 2), $b as item()+ in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Every-quantification carrying type declarations(cardinalities have no impact). .
   */
  @org.junit.Test
  public void kQuantExprWith6() {
    final XQuery query = new XQuery(
      "some $a as item()+ in (1, 2), $b as item()+ in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Every-quantification; the empty-sequence() cannot have an occurrence indicator. .
   */
  @org.junit.Test
  public void kQuantExprWith7() {
    final XQuery query = new XQuery(
      "every $a as empty-sequence()? in (1, 2) satisfies $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Every-quantification; the empty-sequence() cannot have an occurrence indicator. .
   */
  @org.junit.Test
  public void kQuantExprWith8() {
    final XQuery query = new XQuery(
      "some $a as empty-sequence()? in (1, 2) satisfies $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Some-quantification carrying invalid type declarations. .
   */
  @org.junit.Test
  public void kQuantExprWith9() {
    final XQuery query = new XQuery(
      "some $a as xs:anyURI in 1 satisfies count($a)",
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
   *  'every': EBV can't be extracted from xs:QName. .
   */
  @org.junit.Test
  public void kQuantExprWithout1() {
    final XQuery query = new XQuery(
      "some $foo in 1 satisfies QName(\"example.com/\", \"ncname\")",
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
   *  some-quantification combined with empty variable binding. .
   */
  @org.junit.Test
  public void kQuantExprWithout10() {
    final XQuery query = new XQuery(
      "not(some $i in () satisfies $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $foo in 1 satisfies true()`. .
   */
  @org.junit.Test
  public void kQuantExprWithout11() {
    final XQuery query = new XQuery(
      "some $foo in 1 satisfies true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `every $foo in 1 satisfies true()`. .
   */
  @org.junit.Test
  public void kQuantExprWithout12() {
    final XQuery query = new XQuery(
      "every $foo in 1 satisfies true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(some $foo in 1 satisfies false())`. .
   */
  @org.junit.Test
  public void kQuantExprWithout13() {
    final XQuery query = new XQuery(
      "not(some $foo in 1 satisfies false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(every $foo in 1 satisfies false())`. .
   */
  @org.junit.Test
  public void kQuantExprWithout14() {
    final XQuery query = new XQuery(
      "not(every $foo in 1 satisfies false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $foo in 1 satisfies 1`. .
   */
  @org.junit.Test
  public void kQuantExprWithout15() {
    final XQuery query = new XQuery(
      "some $foo in 1 satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `every $foo in 1 satisfies 1`. .
   */
  @org.junit.Test
  public void kQuantExprWithout16() {
    final XQuery query = new XQuery(
      "every $foo in 1 satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $foo in 1 satisfies "a string"`. .
   */
  @org.junit.Test
  public void kQuantExprWithout17() {
    final XQuery query = new XQuery(
      "some $foo in 1 satisfies \"a string\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `every $foo in 1 satisfies "a string"`. .
   */
  @org.junit.Test
  public void kQuantExprWithout18() {
    final XQuery query = new XQuery(
      "every $foo in 1 satisfies \"a string\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `every $var in (true(), true(), true()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout19() {
    final XQuery query = new XQuery(
      "every $var in (true(), true(), true()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  'some': EBV can't be extracted from xs:QName. .
   */
  @org.junit.Test
  public void kQuantExprWithout2() {
    final XQuery query = new XQuery(
      "every $foo in 1 satisfies QName(\"example.com/\", \"ncname\")",
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
   *  A test whose essence is: `not(every $var in (true(), false(), true()) satisfies $var)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout20() {
    final XQuery query = new XQuery(
      "not(every $var in (true(), false(), true()) satisfies $var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(every $var in (false(), true(), true()) satisfies $var)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout21() {
    final XQuery query = new XQuery(
      "not(every $var in (false(), true(), true()) satisfies $var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(every $var in (true(), true(), false()) satisfies $var)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout22() {
    final XQuery query = new XQuery(
      "not(every $var in (true(), true(), false()) satisfies $var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (true(), true(), true()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout23() {
    final XQuery query = new XQuery(
      "some $var in (true(), true(), true()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (true(), false(), true()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout24() {
    final XQuery query = new XQuery(
      "some $var in (true(), false(), true()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (false(), true(), true()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout25() {
    final XQuery query = new XQuery(
      "some $var in (false(), true(), true()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (true(), true(), false()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout26() {
    final XQuery query = new XQuery(
      "some $var in (true(), true(), false()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(some $var in (false(), false(), false()) satisfies $var)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout27() {
    final XQuery query = new XQuery(
      "not(some $var in (false(), false(), false()) satisfies $var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  EBV cannot be extracted fro xs:hexBinary. .
   */
  @org.junit.Test
  public void kQuantExprWithout28() {
    final XQuery query = new XQuery(
      "every $var in (xs:hexBinary(\"FF\"), true(), true()) satisfies $var",
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
   *  EBV cannot be extracted fro xs:hexBinary. .
   */
  @org.junit.Test
  public void kQuantExprWithout29() {
    final XQuery query = new XQuery(
      "every $var in (true(), xs:hexBinary(\"FF\"), true()) satisfies $var",
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
   *  $foo has static type xs:integer; which cannot be compared to xs:string. .
   */
  @org.junit.Test
  public void kQuantExprWithout3() {
    final XQuery query = new XQuery(
      "some $foo in 1 satisfies $foo eq \"1\"",
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
   *  EBV cannot be extracted fro xs:hexBinary. .
   */
  @org.junit.Test
  public void kQuantExprWithout30() {
    final XQuery query = new XQuery(
      "every $var in (true(), true(), xs:hexBinary(\"FF\")) satisfies $var",
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
   *  EBV cannot be extracted fro xs:hexBinary. .
   */
  @org.junit.Test
  public void kQuantExprWithout31() {
    final XQuery query = new XQuery(
      "some $var in (xs:hexBinary(\"FF\"), false(), true()) satisfies $var",
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
   *  EBV cannot be extracted fro xs:hexBinary. .
   */
  @org.junit.Test
  public void kQuantExprWithout32() {
    final XQuery query = new XQuery(
      "some $var in (false(), xs:hexBinary(\"FF\"), true()) satisfies $var",
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
   *  Since EBV cannot be extracted from xs:hexBinary, FORG0006 is allowed. .
   */
  @org.junit.Test
  public void kQuantExprWithout33() {
    final XQuery query = new XQuery(
      "some $var in (true(), true(), xs:hexBinary(\"FF\")) satisfies $var",
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
   *  A test whose essence is: `not(every $var in (false(), true(), true()) satisfies $var)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout34() {
    final XQuery query = new XQuery(
      "not(every $var in (false(), true(), true()) satisfies $var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(every $var in (true(), false(), true()) satisfies $var)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout35() {
    final XQuery query = new XQuery(
      "not(every $var in (true(), false(), true()) satisfies $var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(every $var in (true(), true(), false()) satisfies $var)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout36() {
    final XQuery query = new XQuery(
      "not(every $var in (true(), true(), false()) satisfies $var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (true(), true(), true()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout37() {
    final XQuery query = new XQuery(
      "some $var in (true(), true(), true()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (true(), false(), true()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout38() {
    final XQuery query = new XQuery(
      "some $var in (true(), false(), true()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (false(), true(), true()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout39() {
    final XQuery query = new XQuery(
      "some $var in (false(), true(), true()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  $foo has static type xs:integer; which cannot be compared to xs:string. .
   */
  @org.junit.Test
  public void kQuantExprWithout4() {
    final XQuery query = new XQuery(
      "every $foo in 1 satisfies $foo eq \"1\"",
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
   *  A test whose essence is: `some $var in (true(), true(), false()) satisfies $var`. .
   */
  @org.junit.Test
  public void kQuantExprWithout40() {
    final XQuery query = new XQuery(
      "some $var in (true(), true(), false()) satisfies $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(some $var in (false(), false(), false()) satisfies $var)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout41() {
    final XQuery query = new XQuery(
      "not(some $var in (false(), false(), false()) satisfies $var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A binding in a 'some' quantification shadows global variables. .
   */
  @org.junit.Test
  public void kQuantExprWithout42() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := false(); \n" +
      "        some $i in (true(), true(), true()) satisfies $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A binding in a 'some' quantification shadows global variables. .
   */
  @org.junit.Test
  public void kQuantExprWithout43() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := false(); \n" +
      "        declare variable $t := false(); \n" +
      "        some $i in (true(), true()), $t in (true(), true()) satisfies $i eq $t",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A binding in a 'every' quantification shadows global variables. .
   */
  @org.junit.Test
  public void kQuantExprWithout44() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := false(); \n" +
      "        declare variable $t := false(); \n" +
      "        some $i in (true(), true()), $t in (true(), true()) satisfies ($i eq $t)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A binding in a 'every' quantification shadows global variables. .
   */
  @org.junit.Test
  public void kQuantExprWithout45() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := false(); \n" +
      "        every $i in (true(), true()) satisfies $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (1, 2, 3) satisfies $var eq 3`. .
   */
  @org.junit.Test
  public void kQuantExprWithout46() {
    final XQuery query = new XQuery(
      "some $var in (1, 2, 3) satisfies $var eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(every $var in (1, 2, 3) satisfies $var eq 3)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout47() {
    final XQuery query = new XQuery(
      "not(every $var in (1, 2, 3) satisfies $var eq 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `every $var in (1, 2, 3) satisfies $var eq 1 or $var eq 2 or $var eq 3`. .
   */
  @org.junit.Test
  public void kQuantExprWithout48() {
    final XQuery query = new XQuery(
      "every $var in (1, 2, 3) satisfies $var eq 1 or $var eq 2 or $var eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (1, 2, 3) satisfies $var eq 1 or $var eq 2 or $var eq 3`. .
   */
  @org.junit.Test
  public void kQuantExprWithout49() {
    final XQuery query = new XQuery(
      "some $var in (1, 2, 3) satisfies $var eq 1 or $var eq 2 or $var eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A some-quantification applied on fn:count(). .
   */
  @org.junit.Test
  public void kQuantExprWithout5() {
    final XQuery query = new XQuery(
      "some $i in (0, 2, 3) satisfies count($i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $aaa in (1, 2, 3), $bbb in (3, 2, 1) satisfies $aaa + $bbb eq 4`. .
   */
  @org.junit.Test
  public void kQuantExprWithout50() {
    final XQuery query = new XQuery(
      "some $aaa in (1, 2, 3), $bbb in (3, 2, 1) satisfies $aaa + $bbb eq 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `every $aaa in (3, 3, 3), $bbb in (3, 3, 3) satisfies $aaa + $bbb eq 6`. .
   */
  @org.junit.Test
  public void kQuantExprWithout51() {
    final XQuery query = new XQuery(
      "every $aaa in (3, 3, 3), $bbb in (3, 3, 3) satisfies $aaa + $bbb eq 6",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(every $var in (1, 2, 3) satisfies $var eq 3)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout52() {
    final XQuery query = new XQuery(
      "not(every $var in (1, 2, 3) satisfies $var eq 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `some $var in (1, 2, 3) satisfies $var eq 3`. .
   */
  @org.junit.Test
  public void kQuantExprWithout53() {
    final XQuery query = new XQuery(
      "some $var in (1, 2, 3) satisfies $var eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `true() eq (some $a in 1 satisfies $a)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout54() {
    final XQuery query = new XQuery(
      "true() eq (some $a in 1 satisfies $a)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `true() eq (every $a in 1 satisfies $a)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout55() {
    final XQuery query = new XQuery(
      "true() eq (every $a in 1 satisfies $a)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `true() eq (some $fn:name in (1, 2) satisfies $fn:name)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout56() {
    final XQuery query = new XQuery(
      "true() eq (some $fn:name in (1, 2) satisfies $fn:name)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `true() eq (some $xs:name in (1, 2) satisfies $xs:name)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout57() {
    final XQuery query = new XQuery(
      "true() eq (some $xs:name in (1, 2) satisfies $xs:name)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `true() eq (every $fn:name in (1, 2) satisfies $fn:name)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout58() {
    final XQuery query = new XQuery(
      "true() eq (every $fn:name in (1, 2) satisfies $fn:name)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `true() eq (every $xs:name in (1, 2) satisfies $xs:name)`. .
   */
  @org.junit.Test
  public void kQuantExprWithout59() {
    final XQuery query = new XQuery(
      "true() eq (every $xs:name in (1, 2) satisfies $xs:name)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A some-quantification applied on fn:count(). .
   */
  @org.junit.Test
  public void kQuantExprWithout6() {
    final XQuery query = new XQuery(
      "every $i in (1, 2, 3) satisfies count($i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout60() {
    final XQuery query = new XQuery(
      "some $foo in 1 satisfies $NOTEXIST",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout61() {
    final XQuery query = new XQuery(
      "every $foo in 1 satisfies $NOTEXIST",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout62() {
    final XQuery query = new XQuery(
      "some $foo in (1, $foo) satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout63() {
    final XQuery query = new XQuery(
      "every $foo in (1, $foo) satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout64() {
    final XQuery query = new XQuery(
      "some $foo in 1 satisfies $bar + (some $bar in 2 satisfies $bar)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout65() {
    final XQuery query = new XQuery(
      "every $foo in 1 satisfies $bar + (some $bar in 2 satisfies $bar)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout66() {
    final XQuery query = new XQuery(
      "every $foo in 1 satisfies $bar + (every $bar in 2 satisfies $bar)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout67() {
    final XQuery query = new XQuery(
      "some $foo in 1 satisfies $bar + (every $bar in 2 satisfies $bar)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout68() {
    final XQuery query = new XQuery(
      "some $a in (1, 2), $b in (1, 2), $c in (1, 2) satisfies 1, $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout69() {
    final XQuery query = new XQuery(
      "some $a in (1, 2), $b in (1, 2), $c in (1, 2) satisfies 1, $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  some-quantification combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kQuantExprWithout7() {
    final XQuery query = new XQuery(
      "some $i in subsequence((0, 1, 2, current-time()), 1, 3) satisfies boolean($i treat as xs:integer)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout70() {
    final XQuery query = new XQuery(
      "some $a in (1, 2), $b in (1, 2), $c in (1, 2) satisfies 1, $c",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout71() {
    final XQuery query = new XQuery(
      "every $a in (1, 2), $b in (1, 2), $c in (1, 2) satisfies 1, $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout72() {
    final XQuery query = new XQuery(
      "every $a in (1, 2), $b in (1, 2), $c in (1, 2) satisfies 1, $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout73() {
    final XQuery query = new XQuery(
      "every $a in (1, 2), $b in (1, 2), $c in (1, 2) satisfies 1, $c",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  The 'return' keyword is not valid in a 'some' expression, it must be 'satisfies'. .
   */
  @org.junit.Test
  public void kQuantExprWithout74() {
    final XQuery query = new XQuery(
      "some $foo in (1, $2) return 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  The 'return' keyword is not valid in an 'every' expression, it must be 'satisfies'. .
   */
  @org.junit.Test
  public void kQuantExprWithout75() {
    final XQuery query = new XQuery(
      "every $foo in (1, $2) return 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout76() {
    final XQuery query = new XQuery(
      "some $foo in (1, 2, $foo) satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout77() {
    final XQuery query = new XQuery(
      "some $foo in (1, $foo, 3) satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout78() {
    final XQuery query = new XQuery(
      "some $foo in ($foo, 2, 3) satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout79() {
    final XQuery query = new XQuery(
      "some $foo in $foo satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  every-quantification combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kQuantExprWithout8() {
    final XQuery query = new XQuery(
      "every $i in subsequence((1, 2, 3, current-time()), 1, 3) satisfies boolean($i treat as xs:integer)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout80() {
    final XQuery query = new XQuery(
      "every $foo in (1, 2, $foo) satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout81() {
    final XQuery query = new XQuery(
      "every $foo in (1, $foo, 3) satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout82() {
    final XQuery query = new XQuery(
      "every $foo in ($foo, 2, 3) satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout83() {
    final XQuery query = new XQuery(
      "every $foo in $foo satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout84() {
    final XQuery query = new XQuery(
      "every $a in 1, $b in $b satisfies 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout85() {
    final XQuery query = new XQuery(
      "some $a in (1, 2, 3), $b in (1, 2, 3, $b) satisfies $a eq $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout86() {
    final XQuery query = new XQuery(
      "every $a in (1, 2, 3), $b in (1, 2, 3, $b) satisfies ($a eq $b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout87() {
    final XQuery query = new XQuery(
      "every $a in (1, 2), $b in (1, 2) satisfies 1, $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kQuantExprWithout88() {
    final XQuery query = new XQuery(
      "some $a in (1, 2), $b in (1, 2) satisfies 1, $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Nested variable bindings can reference each other. .
   */
  @org.junit.Test
  public void kQuantExprWithout89() {
    final XQuery query = new XQuery(
      "every $a in (1, 2, 3), $b in ($a, 4) satisfies $b gt 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  every-quantification combined with empty variable binding. .
   */
  @org.junit.Test
  public void kQuantExprWithout9() {
    final XQuery query = new XQuery(
      "every $i in () satisfies $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Nested variable bindings can reference each other. .
   */
  @org.junit.Test
  public void kQuantExprWithout90() {
    final XQuery query = new XQuery(
      "some $a in (1, 2, 3), $b in ($a, 4) satisfies $b gt 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Nested variable bindings can reference each other. .
   */
  @org.junit.Test
  public void kQuantExprWithout91() {
    final XQuery query = new XQuery(
      "every $a in (1, 2), $b in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Nested variable bindings can reference each other. .
   */
  @org.junit.Test
  public void kQuantExprWithout92() {
    final XQuery query = new XQuery(
      "some $a in (1, 2), $b in $a satisfies $b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Nested variable bindings can reference each other. .
   */
  @org.junit.Test
  public void kQuantExprWithout93() {
    final XQuery query = new XQuery(
      "deep-equal((for $a in 1, $b in $a, $c in $a, $d in $c return ($a, $b, $c, $d)), (1, 1, 1, 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  It is a type error to try to extract the EBV value of two integers. .
   */
  @org.junit.Test
  public void kQuantExprWithout94() {
    final XQuery query = new XQuery(
      "every $i in (1, 2, 3) satisfies ($i, $i)",
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
   *  It is a type error to try to extract the EBV value of two integers. .
   */
  @org.junit.Test
  public void kQuantExprWithout95() {
    final XQuery query = new XQuery(
      "some $i in (1, 2, 3) satisfies ($i, $i)",
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
   *  It is a type error to try to extract the EBV value of two integers. .
   */
  @org.junit.Test
  public void kQuantExprWithout96() {
    final XQuery query = new XQuery(
      "every $i in (1, 2, 3) satisfies ($i, $i)",
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
   *  It is a type error to try to extract the EBV value of two integers. .
   */
  @org.junit.Test
  public void kQuantExprWithout97() {
    final XQuery query = new XQuery(
      "some $i in (1, 2, 3) satisfies ($i, $i)",
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
   *  No 'at' declaration is allowed in 'some'-quantification. .
   */
  @org.junit.Test
  public void kQuantExprWithout98() {
    final XQuery query = new XQuery(
      "some $a at $p in (1, 2) satisfies $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  No 'at' declaration is allowed in 'every'-quantification. .
   */
  @org.junit.Test
  public void kQuantExprWithout99() {
    final XQuery query = new XQuery(
      "every $a at $p in (1, 2) satisfies $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Keywords are case sensitive. .
   */
  @org.junit.Test
  public void k2QuantExprWithout1() {
    final XQuery query = new XQuery(
      "SOME $i in (1, 2, 3) satisfies $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Keywords are case sensitive. .
   */
  @org.junit.Test
  public void k2QuantExprWithout2() {
    final XQuery query = new XQuery(
      "EVERY $i in (1, 2, 3) satisfies $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Ensure the focus is defined. .
   */
  @org.junit.Test
  public void k2QuantExprWithout3() {
    final XQuery query = new XQuery(
      "<people id=\"\"/>/(some $id in @id satisfies true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure use of the undefined focus gets flagged. .
   */
  @org.junit.Test
  public void k2QuantExprWithout4() {
    final XQuery query = new XQuery(
      "some $id in attribute::id satisfies $id",
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
   *  Use the focus from within a some-expression. .
   */
  @org.junit.Test
  public void k2QuantExprWithout5() {
    final XQuery query = new XQuery(
      "<e/>/(some $v in self::node() satisfies $v)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Use the focus from within a every-expression. .
   */
  @org.junit.Test
  public void k2QuantExprWithout6() {
    final XQuery query = new XQuery(
      "<e/>/(every $v in self::node() satisfies $v)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Use the focus from within a every-expression's tail. .
   */
  @org.junit.Test
  public void k2QuantExprWithout7() {
    final XQuery query = new XQuery(
      "<e/>/(every $b in 1, $v in self::node() satisfies $v)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Use the focus from within a some-expression's tail. .
   */
  @org.junit.Test
  public void k2QuantExprWithout8() {
    final XQuery query = new XQuery(
      "<e/>/(some $b in 1, $v in self::node() satisfies $v)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Combine some with fn:deep-equal(). .
   */
  @org.junit.Test
  public void k2QuantExprWithout9() {
    final XQuery query = new XQuery(
      "let $firstSeq := (<a/>, <b/>, <e><c/></e>) let $secondSeq := (<a attr=\"\"/>, <b>text</b>, <e><c/></e>) return some $i in $firstSeq satisfies $secondSeq[deep-equal(.,$i)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test optimization when the domain may be empty and return type is a value .
   */
  @org.junit.Test
  public void cbclEvery001() {
    final XQuery query = new XQuery(
      "every $x in (1 to 10)[. div 2 = 11] satisfies false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Tests optimization of every $x in .... satisfies true() .
   */
  @org.junit.Test
  public void cbclEvery002() {
    final XQuery query = new XQuery(
      "every $x in (1 to 10)[. mod 2 = 0] satisfies true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test optimization where the domain may be empty and the return type a value .
   */
  @org.junit.Test
  public void cbclSome001() {
    final XQuery query = new XQuery(
      "some $x in (1 to 10)[. div 2 = 11] satisfies true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and addition expression. Returns false .
   */
  @org.junit.Test
  public void quantExpr1() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x + $x = 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and usage of string-length function expression. .
   */
  @org.junit.Test
  public void quantExpr10() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies fn:string-length(xs:string($x)) = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and usage of "fn:count" function expression. .
   */
  @org.junit.Test
  public void quantExpr11() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies fn:count(($x)) = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and usage of "fn:true" function expression. .
   */
  @org.junit.Test
  public void quantExpr12() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and usage of "fn:false" function expression. .
   */
  @org.junit.Test
  public void quantExpr13() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and usage of "fn:not" function expression. .
   */
  @org.junit.Test
  public void quantExpr14() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies fn:not($x)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and use of lofical expression (or). .
   */
  @org.junit.Test
  public void quantExpr15() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x = 1 or $x = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and use of lofical expression (and). .
   */
  @org.junit.Test
  public void quantExpr16() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x = 1 and ($x +1) = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and and use of string data. .
   */
  @org.junit.Test
  public void quantExpr17() {
    final XQuery query = new XQuery(
      "some $x in (\"A\",\"B\",\"C\") satisfies $x = \"A\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword with multiple variables and addition expression .
   */
  @org.junit.Test
  public void quantExpr18() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies $x + $y = 5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and addition expression. Returns true .
   */
  @org.junit.Test
  public void quantExpr2() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x + $x = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword with multiple variables and multiplication expression .
   */
  @org.junit.Test
  public void quantExpr20() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies $x * $y = 10",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword with multiple variables and division (div) operation. .
   */
  @org.junit.Test
  public void quantExpr21() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies $x div $y = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword with multiple variables and division (idiv) operation. .
   */
  @org.junit.Test
  public void quantExpr22() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies $x idiv $y = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword with multiple variables, and string function. .
   */
  @org.junit.Test
  public void quantExpr23() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies fn:string($x) = fn:string($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword, use of multiple variable and the xs:integer type .
   */
  @org.junit.Test
  public void quantExpr24() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies xs:integer($x) = xs:integer($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword, use of multiple variable and the xs:decimal type .
   */
  @org.junit.Test
  public void quantExpr25() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies xs:decimal($x) = xs:decimal($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword, use of multiple variable and the xs:float type .
   */
  @org.junit.Test
  public void quantExpr26() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies xs:float($x) = xs:float($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword, use of multiple variable and the xs:double type .
   */
  @org.junit.Test
  public void quantExpr27() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies xs:double($x) = xs:double($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword, use of multiple variable and the xs:boolean type .
   */
  @org.junit.Test
  public void quantExpr28() {
    final XQuery query = new XQuery(
      "some $x in (\"true\", \"false\"), $y in (\"false\",\"true\") satisfies xs:boolean($x) = xs:boolean($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword, use of multiple variable and the xs:dateTime type .
   */
  @org.junit.Test
  public void quantExpr29() {
    final XQuery query = new XQuery(
      "some $x in (\"1980-05-05T13:13:13Z\", \"1980-05-05T13:13:13Z\"), $y in (\"1980-05-05T13:13:13Z\",\"1980-05-05T13:13:13Z\") satisfies xs:dateTime($x) = xs:dateTime($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and a subtraction expression. Returns false .
   */
  @org.junit.Test
  public void quantExpr3() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x - 2 = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword, use of multiple variable and the xs:date type .
   */
  @org.junit.Test
  public void quantExpr30() {
    final XQuery query = new XQuery(
      "some $x in (\"1985-07-05Z\", \"1985-07-05Z\"), $y in (\"1985-07-05Z\",\"1985-07-05Z\") satisfies xs:date($x) = xs:date($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and a subtraction expression. Returns false .
   */
  @org.junit.Test
  public void quantExpr33() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies $x - 2 = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and a subtraction expression. Returns true .
   */
  @org.junit.Test
  public void quantExpr4() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x - 2 = 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and a multiplication expression. .
   */
  @org.junit.Test
  public void quantExpr5() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x * 2 = 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and a division expression. .
   */
  @org.junit.Test
  public void quantExpr6() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x div 2 = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword, use of multiple variable and the xs:date type .
   */
  @org.junit.Test
  public void quantExpr60() {
    final XQuery query = new XQuery(
      "every $x in (\"1985-07-05Z\", \"1985-07-05Z\"), $y in (\"1985-07-05Z\",\"1985-07-05Z\") satisfies xs:date($x) = xs:date($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and a division (idiv) expression. .
   */
  @org.junit.Test
  public void quantExpr7() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies $x idiv 2 = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and usage of avg function expression. .
   */
  @org.junit.Test
  public void quantExpr8() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies fn:avg(($x, 1)) = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and usage of string function expression. .
   */
  @org.junit.Test
  public void quantExpr9() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies fn:string($x) = \"1\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword with multiple variables and subtraction expression .
   */
  @org.junit.Test
  public void quantexpr19() {
    final XQuery query = new XQuery(
      "some $x in (1,2,3), $y in (4,5,6) satisfies $x - $y = 5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and addition expression. Returns false .
   */
  @org.junit.Test
  public void quantexpr31() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies $x + $x = 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and addition expression. Returns true .
   */
  @org.junit.Test
  public void quantexpr32() {
    final XQuery query = new XQuery(
      "every $x in (1, 1) satisfies $x + $x = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and a subtraction expression. Returns true .
   */
  @org.junit.Test
  public void quantexpr34() {
    final XQuery query = new XQuery(
      "every $x in (2, 2) satisfies $x - 2 = 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and a multiplication expression. .
   */
  @org.junit.Test
  public void quantexpr35() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies $x * 2 = 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and a division expression. .
   */
  @org.junit.Test
  public void quantexpr36() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies $x div 2 = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and a division (idiv) expression. .
   */
  @org.junit.Test
  public void quantexpr37() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies $x idiv 2 = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and usage of avg function expression. .
   */
  @org.junit.Test
  public void quantexpr38() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies fn:avg(($x, 1)) = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and usage of string function expression. .
   */
  @org.junit.Test
  public void quantexpr39() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies fn:string($x) = \"1\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and usage of string-length function expression. .
   */
  @org.junit.Test
  public void quantexpr40() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies fn:string-length(xs:string($x)) = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and usage of "fn:count" function expression. .
   */
  @org.junit.Test
  public void quantexpr41() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies fn:count(($x)) = 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and usage of "fn:true" function expression. .
   */
  @org.junit.Test
  public void quantexpr42() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword and usage of "fn:false" function expression. .
   */
  @org.junit.Test
  public void quantexpr43() {
    final XQuery query = new XQuery(
      "some $x in (1, 2) satisfies fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and usage of "fn:not" function expression. .
   */
  @org.junit.Test
  public void quantexpr44() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies fn:not($x)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and use of lofical expression (or). .
   */
  @org.junit.Test
  public void quantexpr45() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies $x = 1 or $x = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and use of lofical expression (and). .
   */
  @org.junit.Test
  public void quantexpr46() {
    final XQuery query = new XQuery(
      "every $x in (1, 2) satisfies $x = 1 and ($x +1) = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword and and use of string data. .
   */
  @org.junit.Test
  public void quantexpr47() {
    final XQuery query = new XQuery(
      "every $x in (\"A\",\"B\",\"C\") satisfies $x = \"A\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword with multiple variables and addition expression .
   */
  @org.junit.Test
  public void quantexpr48() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies $x + $y = 5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword with multiple variables and subtraction expression .
   */
  @org.junit.Test
  public void quantexpr49() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies $x - $y = 5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword with multiple variables and multiplication expression .
   */
  @org.junit.Test
  public void quantexpr50() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies $x * $y = 10",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword with multiple variables and division (div) operation. .
   */
  @org.junit.Test
  public void quantexpr51() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies $x div $y = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword with multiple variables and division (idiv) operation. .
   */
  @org.junit.Test
  public void quantexpr52() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies $x idiv $y = 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword with multiple variables, and string function. .
   */
  @org.junit.Test
  public void quantexpr53() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies fn:string($x) = fn:string($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword, use of multiple variable and the xs:integer type .
   */
  @org.junit.Test
  public void quantexpr54() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies xs:integer($x) = xs:integer($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword, use of multiple variable and the xs:decimal type .
   */
  @org.junit.Test
  public void quantexpr55() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies xs:decimal($x) = xs:decimal($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword, use of multiple variable and the xs:float type .
   */
  @org.junit.Test
  public void quantexpr56() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies xs:float($x) = xs:float($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword, use of multiple variable and the xs:double type .
   */
  @org.junit.Test
  public void quantexpr57() {
    final XQuery query = new XQuery(
      "every $x in (1,2,3), $y in (4,5,6) satisfies xs:double($x) = xs:double($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword, use of multiple variable and the xs:boolean type .
   */
  @org.junit.Test
  public void quantexpr58() {
    final XQuery query = new XQuery(
      "every $x in (\"true\", \"false\"), $y in (\"false\",\"true\") satisfies xs:boolean($x) = xs:boolean($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword, use of multiple variable and the xs:dateTime type .
   */
  @org.junit.Test
  public void quantexpr59() {
    final XQuery query = new XQuery(
      "every $x in (\"1980-05-05T13:13:13Z\", \"1980-05-05T13:13:13Z\"), $y in (\"1980-05-05T13:13:13Z\",\"1980-05-05T13:13:13Z\") satisfies xs:dateTime($x) = xs:dateTime($y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword that binds the declared variables to an xs:integer type. .
   */
  @org.junit.Test
  public void quantexpr61() {
    final XQuery query = new XQuery(
      "some $x as xs:integer in (1, 2, 3) , $y as xs:integer in (2, 3, 4) satisfies $x + $y = 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword that binds the declared variable to an xs:string type. .
   */
  @org.junit.Test
  public void quantexpr62() {
    final XQuery query = new XQuery(
      "some $x as xs:string in (\"cat\",\"dog\",\"rat\") satisfies fn:string-length($x) = 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword that binds the declared variable to an xs:string type. .
   */
  @org.junit.Test
  public void quantexpr63() {
    final XQuery query = new XQuery(
      "every $x as xs:string in (\"cat\",\"dog\",\"rat\") satisfies fn:string-length($x) = 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "every" keyword that binds the declared variable to an xs:string and xs:integer type respectively. .
   */
  @org.junit.Test
  public void quantexpr64() {
    final XQuery query = new XQuery(
      "every $x as xs:string in (\"cat\",\"dog\",\"rat\"), $y as xs:integer in (3, 3, 3) satisfies fn:string-length($x) = $y",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple quantified expression using "some" keyword that binds the declared variable to an xs:integer and xs:float type respectively. .
   */
  @org.junit.Test
  public void quantexpr65() {
    final XQuery query = new XQuery(
      "some $x as xs:integer in (1, 2, 3), $y as xs:float in (xs:float(2), xs:float(3)) satisfies $x + $y = 5",
      ctx);
    try {
      result = new QT3Result(query.value());
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
