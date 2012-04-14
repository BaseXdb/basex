package org.basex.test.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the ErrorsAndOptimization.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscErrorsAndOptimization extends QT3TestSet {

  /**
   *  Conditional and typeswitch expressions must not raise a dynamic error in respect of subexpressions occurring in a branch that is not selected .
   */
  @org.junit.Test
  public void errorsAndOptimization1() {
    final XQuery query = new XQuery(
      "if (true()) then 1 else error(QName('http://www.example.com/errors', 'err:oops'), \"Oops, this error should not be raised!\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Conditional expressions must not return the value delivered by a branch unless that branch is selected. .
   */
  @org.junit.Test
  public void errorsAndOptimization2() {
    final XQuery query = new XQuery(
      "if (true()) then 1 div 0 else 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   *  To avoid unexpected errors caused by expression rewrite, tests that are designed to prevent dynamic errors should be expressed using conditional or typeswitch expressions. .
   */
  @org.junit.Test
  public void errorsAndOptimization3() {
    final XQuery query = new XQuery(
      "count( let $N := <n x=\"this ain't no date\"/> return $N[if (@x castable as xs:date) then xs:date(@x) gt xs:date(\"2000-01-01\") else false()] )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  If a processor evaluates an operand E (wholly or in part), then it is required to establish that the actual value of the operand E does not violate any constraints on its cardinality. .
   */
  @org.junit.Test
  public void errorsAndOptimization4() {
    final XQuery query = new XQuery(
      "string-length((\"one\", \"two\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  If a processor evaluates an operand E (wholly or in part), then it is required to establish that the actual value of the operand E does not violate any constraints on its cardinality. .
   */
  @org.junit.Test
  public void errorsAndOptimization5() {
    final XQuery query = new XQuery(
      "let $e := (1,2) return $e eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Expressions must not be rewritten in such a way as to create or remove static errors. .
   */
  @org.junit.Test
  public void errorsAndOptimization6() {
    final XQuery query = new XQuery(
      "for $s in \"var:QName\" return QName($s)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  Expressions must not be rewritten in such a way as to create or remove static errors. .
   */
  @org.junit.Test
  public void errorsAndOptimization7() {
    final XQuery query = new XQuery(
      "if (true()) then 1 else let $unbound:var := 2 return $unbound:var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }
}
