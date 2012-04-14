package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the nilled() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNilled extends QT3TestSet {

  /**
   *  A test whose essence is: `nilled()`. .
   */
  @org.junit.Test
  public void kNilledFunc1() {
    final XQuery query = new XQuery(
      "nilled()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `nilled((), "wrong param")`. .
   */
  @org.junit.Test
  public void kNilledFunc2() {
    final XQuery query = new XQuery(
      "nilled((), \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(nilled( () ))`. .
   */
  @org.junit.Test
  public void kNilledFunc3() {
    final XQuery query = new XQuery(
      "empty(nilled( () ))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `nilled(1)`. .
   */
  @org.junit.Test
  public void kNilledFunc4() {
    final XQuery query = new XQuery(
      "nilled(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of nilled function with argument set to empty sequence. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNilled1() {
    final XQuery query = new XQuery(
      "fn:count(fn:nilled(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of nilled function as argument to fn:not function. returns true. .
   */
  @org.junit.Test
  public void fnNilled10() {
    final XQuery query = new XQuery(
      " fn:not(fn:nilled(<shoe xsi:nil=\"{fn:false()}\"/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of nilled function as argument to fn:not function. returns false. .
   */
  @org.junit.Test
  public void fnNilled11() {
    final XQuery query = new XQuery(
      " fn:not(fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of nilled function used a part of boolean expression ("and" operator and fn:true() .
   */
  @org.junit.Test
  public void fnNilled12() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>) and fn:true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function used a part of boolean expression ("and" operator and fn:false() .
   */
  @org.junit.Test
  public void fnNilled13() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>) and fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function used a part of boolean expression ("or" operator and fn:true() .
   */
  @org.junit.Test
  public void fnNilled14() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>) or fn:true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of nilled function used a part of boolean expression ("or" operator and fn:false() .
   */
  @org.junit.Test
  public void fnNilled15() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>) or fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function used as argument to fn:string function. .
   */
  @org.junit.Test
  public void fnNilled16() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>) ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function used as argument to xs:boolean function. Returns true .
   */
  @org.junit.Test
  public void fnNilled17() {
    final XQuery query = new XQuery(
      " xs:boolean(fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function used as argument to xs:boolean function. Returns false .
   */
  @org.junit.Test
  public void fnNilled18() {
    final XQuery query = new XQuery(
      " xs:boolean(fn:nilled(<shoe xsi:nil=\"{fn:false()}\"/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function with argument set to a document node Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNilled19() {
    final XQuery query = new XQuery(
      " fn:count(fn:nilled(document {<aList><anElement>data</anElement></aList>}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of nilled function with argument set to comment node. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNilled2() {
    final XQuery query = new XQuery(
      " fn:count(fn:nilled(/works[1]/employee[2]/child::text()[last()]))",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of nilled function with argument set to an attribute node Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNilled20() {
    final XQuery query = new XQuery(
      " fn:count(fn:nilled(attribute size {1}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of nilled function with argument set to a comment node Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNilled21() {
    final XQuery query = new XQuery(
      " fn:count(fn:nilled(<!-- This is a comment node -->))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of nilled function with argument set to a processing instruction node Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNilled22() {
    final XQuery query = new XQuery(
      " fn:count(fn:nilled(<?format role=\"output\" ?>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of nilled function with argument set to an element node. .
   */
  @org.junit.Test
  public void fnNilled3() {
    final XQuery query = new XQuery(
      " fn:nilled(/works[1]/employee[2])",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function with argument set to an element node. .
   */
  @org.junit.Test
  public void fnNilled4() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe size = \"5\"/>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function with argument set to an element node with xsi:nill = true. .
   */
  @org.junit.Test
  public void fnNilled5() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"true\"/>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function with argument set to an element node with xsi:nill = false. .
   */
  @org.junit.Test
  public void fnNilled6() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"false\"/>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function with argument set to an element node with xsi:nill = fn:true(). .
   */
  @org.junit.Test
  public void fnNilled7() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function with argument set to an element node with xsi:nill = fn:false(). .
   */
  @org.junit.Test
  public void fnNilled8() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:false()}\"/>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of nilled function with more than one argument. .
   */
  @org.junit.Test
  public void fnNilled9() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>,\"A Second Argument\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }
}
