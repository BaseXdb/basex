package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the OptionDecl production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdOptionDecl extends QT3TestSet {

  /**
   *  The name of an option must be a full QName. .
   */
  @org.junit.Test
  public void kOptionDeclarationProlog1() {
    final XQuery query = new XQuery(
      "declare option myopt \"option value\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Five identical options appearing after each other. .
   */
  @org.junit.Test
  public void kOptionDeclarationProlog2() {
    final XQuery query = new XQuery(
      "declare(::)option(::)local:opt(::)\"option value\"(::); \n" +
      "        declare(::)option(::)local:opt(::)\"option value\"(::); \n" +
      "        declare(::)option(::)local:opt(::)\"option value\"(::); \n" +
      "        declare(::)option(::)local:opt(::)\"option value\"(::); \n" +
      "        declare(::)option(::)local:opt(::)\"option value\";1(::)eq(::)1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A simple option using double quotes for the value. .
   */
  @org.junit.Test
  public void kOptionDeclarationProlog3() {
    final XQuery query = new XQuery(
      "declare(::)option(::)local:opt\"option value\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A simple option using single quotes for the value. .
   */
  @org.junit.Test
  public void kOptionDeclarationProlog4() {
    final XQuery query = new XQuery(
      "declare(::)option(::)local:opt'option value'; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An undeclared prefix in a option declaration is an error regardless of any option support in the implementation. .
   */
  @org.junit.Test
  public void kOptionDeclarationProlog5() {
    final XQuery query = new XQuery(
      "declare option prefixnotdeclared:opt 'option value'; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  A syntactically invalid option declaration. .
   */
  @org.junit.Test
  public void kOptionDeclarationProlog6() {
    final XQuery query = new XQuery(
      "declare option localpartmissing: 'option value'; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Evaluation of a simple prolog option declaration. .
   */
  @org.junit.Test
  public void optiondeclprolog1() {
    final XQuery query = new XQuery(
      "declare namespace exq = \"http://example.org/XQueryImplementation\"; declare option exq:java-class \"math = java.lang.Math\"; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "aaa")
    );
  }

  /**
   *  Evaluation of prolog option declaration for which namespace prefix is not defined. .
   */
  @org.junit.Test
  public void optiondeclprolog2() {
    final XQuery query = new XQuery(
      "declare option exq:java-class \"math = java.lang.Math\"; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }
}
