package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the fn:fold-left() higher-order function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFoldRight extends QT3TestSet {

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Author - Michael Kay, Saxonica 
   *         .
   */
  @org.junit.Test
  public void foldRight001() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b) { $a + $b }, 0, 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Author - Michael Kay, Saxonica 
   *         .
   */
  @org.junit.Test
  public void foldRight002() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b) { $a * $b }, 1, (2,3,5,7))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("210")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Author - Michael Kay, Saxonica 
   *         .
   */
  @org.junit.Test
  public void foldRight003() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b) { $a or $b }, false(), (true(), false(), false()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Author - Michael Kay, Saxonica 
   *         .
   */
  @org.junit.Test
  public void foldRight004() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b) { $a and $b }, false(), (true(), false(), false()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Author - Michael Kay, Saxonica 
   *         .
   */
  @org.junit.Test
  public void foldRight005() {
    final XQuery query = new XQuery(
      "let $f := function($a, $b){($b, $a)} return fold-right($f, (), 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("5,4,3,2,1")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Author - Michael Kay, Saxonica 
   *         .
   */
  @org.junit.Test
  public void foldRight006() {
    final XQuery query = new XQuery(
      "fold-right(fn:concat(?, \".\", ?), \"\", 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1.2.3.4.5.'")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Author - Michael Kay, Saxonica 
   *         .
   */
  @org.junit.Test
  public void foldRight007() {
    final XQuery query = new XQuery(
      "fold-right(fn:concat(\"$f(\", ?, \", \", ?, \")\"), \"$zero\", 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "$f(1, $f(2, $f(3, $f(4, $f(5, $zero)))))")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *         .
   */
  @org.junit.Test
  public void foldRight008() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b, $c){ $a + $b + $c }, 0, 1 to 5)",
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
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *         .
   */
  @org.junit.Test
  public void foldRight009() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b){ $a + $b }, \"\", 1 to 5)",
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
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *         .
   */
  @org.junit.Test
  public void foldRight010() {
    final XQuery query = new XQuery(
      "fold-right(function($a as xs:string, $b){ $a + $b }, 0, 1 to 5)",
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
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *         .
   */
  @org.junit.Test
  public void foldRight011() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b as xs:string){ $a + $b }, 0, 1 to 5)",
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
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *         .
   */
  @org.junit.Test
  public void foldRight012() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b) as xs:string { $a + $b }, 0, 1 to 5)",
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
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Count the number of items in a sequence
   *         .
   */
  @org.junit.Test
  public void foldRight013() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b){ $b + 1 }, 0, 1 to 1000000)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1000000")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Reverse the sequence order
   *         .
   */
  @org.junit.Test
  public void foldRight014() {
    final XQuery query = new XQuery(
      "fold-right(function($a , $b){ ($b, $a) }, (), (1 to 5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("5, 4, 3, 2, 1")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Remove duplicate values in a sequence
   *         .
   */
  @org.junit.Test
  public void foldRight015() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b){ if(exists($b[. eq $a])) then $b else ($a, $b) }, (), (1, 2, 2, 3, 3, 3, 4, 5, 5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Double each value in a sequence
   *         .
   */
  @org.junit.Test
  public void foldRight016() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b){ ($a, $a, $b) }, (), (1 to 5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 1, 2, 2, 3, 3, 4, 4, 5, 5")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Show fold-right structural transformation
   *         .
   */
  @org.junit.Test
  public void foldRight017() {
    final XQuery query = new XQuery(
      "fold-right(concat(\"(\", ?, \"+\", ?, \")\"), 0, (1 to 13))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "(1+(2+(3+(4+(5+(6+(7+(8+(9+(10+(11+(12+(13+0)))))))))))))")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Returns the accumulated length of all strings
   *         .
   */
  @org.junit.Test
  public void foldRight018() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b){ string-length($a) + $b }, 0, (\"Hello\", \"World\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Max() function
   *         .
   */
  @org.junit.Test
  public void foldRight019() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b){ if(empty($b)) then $a else if($a lt $b) then $b else $a }, (), 1 to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   * 
   *             Higher Order Functions 
   *             fold-right function 
   *             Min() function
   *         .
   */
  @org.junit.Test
  public void foldRight020() {
    final XQuery query = new XQuery(
      "fold-right(function($a, $b){ if(empty($b)) then $a else if($a gt $b) then $b else $a }, (), 1 to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }
}
