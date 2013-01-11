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
   *  A test whose essence is: `nilled((), "wrong param")`. .
   */
  @org.junit.Test
  public void kNilledFunc2() {
    final XQuery query = new XQuery(
      "nilled((), \"wrong param\")",
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
   *  A test whose essence is: `empty(nilled( () ))`. .
   */
  @org.junit.Test
  public void kNilledFunc3() {
    final XQuery query = new XQuery(
      "empty(nilled( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Test fn:nilled on non-element nodes. .
   */
  @org.junit.Test
  public void cbclNilled007() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tempty(nilled( <?foo ?> ))\n" +
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
   *  Evaluation of nilled function with argument set to empty sequence. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNilled1() {
    final XQuery query = new XQuery(
      "fn:count(fn:nilled(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Evaluation of nilled function used a part of boolean expression ("and" operator and fn:false() .
   */
  @org.junit.Test
  public void fnNilled13() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>) and fn:false()",
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
   *  Evaluation of nilled function used a part of boolean expression ("or" operator and fn:true() .
   */
  @org.junit.Test
  public void fnNilled14() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>) or fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Evaluation of nilled function used as argument to fn:string function. .
   */
  @org.junit.Test
  public void fnNilled16() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>) ",
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
   *  Evaluation of nilled function used as argument to xs:boolean function. Returns false .
   */
  @org.junit.Test
  public void fnNilled17() {
    final XQuery query = new XQuery(
      " xs:boolean(fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>))",
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
   *  Evaluation of nilled function used as argument to xs:boolean function. Returns false .
   */
  @org.junit.Test
  public void fnNilled18() {
    final XQuery query = new XQuery(
      " xs:boolean(fn:nilled(<shoe xsi:nil=\"{fn:false()}\"/>))",
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
   *  Evaluation of nilled function with argument set to a document node .
   */
  @org.junit.Test
  public void fnNilled19() {
    final XQuery query = new XQuery(
      " fn:nilled(document {<aList><anElement>data</anElement></aList>})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
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
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of nilled function with argument set to an attribute node .
   */
  @org.junit.Test
  public void fnNilled20() {
    final XQuery query = new XQuery(
      " fn:nilled(attribute size {1})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of nilled function with argument set to a comment node .
   */
  @org.junit.Test
  public void fnNilled21() {
    final XQuery query = new XQuery(
      " fn:nilled(<!-- This is a comment node -->)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of nilled function with argument set to a processing instruction node.
   */
  @org.junit.Test
  public void fnNilled22() {
    final XQuery query = new XQuery(
      " fn:nilled(<?format role=\"output\" ?>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  nilled() applied to document node (Allowed in 3.0). .
   */
  @org.junit.Test
  public void fnNilled23() {
    final XQuery query = new XQuery(
      "nilled()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *   nilled() applied to element node (Allowed in 3.0). .
   */
  @org.junit.Test
  public void fnNilled24() {
    final XQuery query = new XQuery(
      "/*/nilled()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *   nilled() applied to attribute node (Allowed in 3.0). .
   */
  @org.junit.Test
  public void fnNilled25() {
    final XQuery query = new XQuery(
      "/works/employee[1]/@gender/nilled()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *   nilled() applied to text node (Allowed in 3.0). .
   */
  @org.junit.Test
  public void fnNilled26() {
    final XQuery query = new XQuery(
      "/works/employee[1]/empnum/text()/nilled()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *   nilled() applied to untyped element node with xsi:nil=true (Allowed in 3.0). .
   */
  @org.junit.Test
  public void fnNilled27() {
    final XQuery query = new XQuery(
      " (<shoe xsi:nil=\"true\"/>)/fn:nilled()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *   nilled() : context item is atomic .
   */
  @org.junit.Test
  public void fnNilled28() {
    final XQuery query = new XQuery(
      "23[nilled()]",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *   nilled() : context item is a function item .
   */
  @org.junit.Test
  public void fnNilled29() {
    final XQuery query = new XQuery(
      "nilled#0[nilled()]",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *  Evaluation of nilled function with argument set to an element node. .
   */
  @org.junit.Test
  public void fnNilled3() {
    final XQuery query = new XQuery(
      " fn:nilled(/works[1]/employee[2])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *   nilled() : context item is absent .
   */
  @org.junit.Test
  public void fnNilled30() {
    final XQuery query = new XQuery(
      "current-date() gt current-date()+xs:dayTimeDuration('P1D') or nilled()",
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
   *  Evaluation of nilled function with argument set to an element node. .
   */
  @org.junit.Test
  public void fnNilled4() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe size = \"5\"/>)",
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
   *  Evaluation of nilled function with argument set to an element node with xsi:nill = true. .
   */
  @org.junit.Test
  public void fnNilled5() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"true\"/>)",
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
   *  Evaluation of nilled function with argument set to an element node with xsi:nill = false. .
   */
  @org.junit.Test
  public void fnNilled6() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"false\"/>)",
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
   *  Evaluation of nilled function with argument set to an element node with xsi:nill = fn:true(). .
   */
  @org.junit.Test
  public void fnNilled7() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>)",
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
   *  Evaluation of nilled function with argument set to an element node with xsi:nill = fn:false(). .
   */
  @org.junit.Test
  public void fnNilled8() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:false()}\"/>)",
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
   *  Evaluation of nilled function with more than one argument. .
   */
  @org.junit.Test
  public void fnNilled9() {
    final XQuery query = new XQuery(
      " fn:nilled(<shoe xsi:nil=\"{fn:true()}\"/>,\"A Second Argument\")",
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
}
