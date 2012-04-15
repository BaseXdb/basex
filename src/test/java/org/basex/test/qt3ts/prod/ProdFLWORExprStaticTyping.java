package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the FLWORExpr.static-typing production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdFLWORExprStaticTyping extends QT3TestSet {

  /**
   *  In LetClause, binding expr's ST must be subtype of variable's ST. Under REC FS, both are 'processing-instruction?', so STA succeeds. Under PER FS, former is 'processing-instruction filesystem?', latter is 'processing-instruction filesytem', so STA fails, raises error. (Note that an implementation that doesn't do STA will not raise an error.) .
   */
  @org.junit.Test
  public void sTPITest01() {
    final XQuery query = new XQuery(
      "(: Description: In LetClause, binding expr's ST must be subtype of variable's ST. Under REC FS, both are 'processing-instruction?', so STA succeeds. Under PER FS, former is 'processing-instruction filesystem?', latter is 'processing-instruction filesytem', so STA fails, raises error. (Note that an implementation that doesn't do STA will not raise an error.) :) let $pi as processing-instruction(filesystem) := (//processing-instruction(filesystem))[1] return $pi",
      ctx);
    query.context(node(file("prod/ForClause/fsx_NS.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  
   *           In LetClause, binding expr's ST must be subtype of variable's ST. 
   *           Under REC FS, both are 'processing-instruction?', so STA succeeds. 
   *           Under PER FS, former is 'processing-instruction nonexistent', latter is 'processing-instruction filesytem', so STA fails, raises error. 
   *           (Note that an implementation that doesn't do STA will not raise an error, because the LetClause isn't evaluated.) 
   *       .
   */
  @org.junit.Test
  public void sTPITest02() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $pi in //processing-instruction(nonexistent) \n" +
      "        let $pi2 as processing-instruction(filesystem) := $pi \n" +
      "        return $pi2",
      ctx);
    query.context(node(file("prod/ForClause/fsx_NS.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Test 'where' clause with heterogenous sequences. First item is a node .
   */
  @org.junit.Test
  public void sTWhereExpr001() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where ($file, 1) return $file/FileName",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPTY0004")
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  Test 'where' clause with heterogenous sequences. First item is a value. .
   */
  @org.junit.Test
  public void sTWhereExpr002() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where (1, $file) return $file/FileName",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPTY0004")
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression. .
   */
  @org.junit.Test
  public void statictyping1() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where $var + 1 = 3 return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses "div" operator). .
   */
  @org.junit.Test
  public void statictyping10() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where ($var div 2) = 3 return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses "idiv" operator). .
   */
  @org.junit.Test
  public void statictyping11() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where ($var idiv 2) = 3 return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses "mod" operator). .
   */
  @org.junit.Test
  public void statictyping12() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where ($var mod 2) = 3 return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong cardinality for operator (uses eq operator). .
   */
  @org.junit.Test
  public void statictyping13() {
    final XQuery query = new XQuery(
      "let $var := (\"a\",\"b\",\"c\") where ($var eq 1) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong cardinality for operator (uses le operator). .
   */
  @org.junit.Test
  public void statictyping14() {
    final XQuery query = new XQuery(
      "let $var := (\"a\",\"b\",\"c\") where ($var le 1) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong cardinality for operator (uses lt operator). .
   */
  @org.junit.Test
  public void statictyping15() {
    final XQuery query = new XQuery(
      "let $var := (\"a\",\"b\",\"c\") where ($var lt 1) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong cardinality for operator (uses gt operator). .
   */
  @org.junit.Test
  public void statictyping16() {
    final XQuery query = new XQuery(
      "let $var := (\"a\",\"b\",\"c\") where ($var gt 1) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong cardinality for operator (uses ne operator). .
   */
  @org.junit.Test
  public void statictyping17() {
    final XQuery query = new XQuery(
      "let $var := (\"a\",\"b\",\"c\") where ($var ne 1) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong cardinality for operator (uses "+" operator). .
   */
  @org.junit.Test
  public void statictyping18() {
    final XQuery query = new XQuery(
      "let $var := (1,2,3) where ($var + 1) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong cardinality for operator (uses "-" operator). .
   */
  @org.junit.Test
  public void statictyping19() {
    final XQuery query = new XQuery(
      "let $var := (1,2,3) where ($var - 1) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression. .
   */
  @org.junit.Test
  public void statictyping2() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where fn:abs(($var)) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong cardinality for operator (uses "*" operator). .
   */
  @org.junit.Test
  public void statictyping20() {
    final XQuery query = new XQuery(
      "let $var := (1,2,3) where ($var * 1) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing where value of a variable is not set. .
   */
  @org.junit.Test
  public void statictyping21() {
    final XQuery query = new XQuery(
      "let $x := 1 let $z := $x + $y return $x",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  Evaluation of static typing where value of a variable is not set. .
   */
  @org.junit.Test
  public void statictyping22() {
    final XQuery query = new XQuery(
      "declare variable $x := $y + 1; \"abc\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  Evaluation of static typing where value of a variable is not set. Used in a for clause of a FLOWR expression. .
   */
  @org.junit.Test
  public void statictyping23() {
    final XQuery query = new XQuery(
      "for $x in (1, 2, 3) for $z in ($x, $y) return $x",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses fn:not) .
   */
  @org.junit.Test
  public void statictyping24() {
    final XQuery query = new XQuery(
      "let $var := (\"a\",\"b\",\"c\") where fn:not($var) eq fn:true() return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPTY0004")
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses fn:avg). .
   */
  @org.junit.Test
  public void statictyping3() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where fn:avg(($var,1)) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses fn:max). .
   */
  @org.junit.Test
  public void statictyping4() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where fn:max(($var,1)) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses fn:min). .
   */
  @org.junit.Test
  public void statictyping5() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where fn:min(($var,1)) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses fn:sum). .
   */
  @org.junit.Test
  public void statictyping6() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where fn:sum(($var,1)) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses fn:boolean). .
   */
  @org.junit.Test
  public void statictyping7() {
    final XQuery query = new XQuery(
      "let $var := (\"a\",\"b\",\"c\") where fn:boolean($var) = fn:true() return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPTY0004")
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses subtraction expression). .
   */
  @org.junit.Test
  public void statictyping8() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where ($var - 1) = 3 return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of static typing feature within a "where" clause. Wrong operand for expression (uses multiplication operation). .
   */
  @org.junit.Test
  public void statictyping9() {
    final XQuery query = new XQuery(
      "for $var in (\"a\",\"b\",\"c\") where ($var * 1) = 3 return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }
}
