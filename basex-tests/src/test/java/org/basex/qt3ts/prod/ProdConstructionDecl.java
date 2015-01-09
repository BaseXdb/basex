package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the ConstructionDecl production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdConstructionDecl extends QT3TestSet {

  /**
   *  Test that 'declare construction' with the preserve keyword is parsed properly. .
   */
  @org.junit.Test
  public void kConstructionProlog1() {
    final XQuery query = new XQuery(
      "(::)declare(::)construction(::)preserve(::);(::)1(::)eq(::)1(::)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  'declare construction Preserve' is syntactically invalid. .
   */
  @org.junit.Test
  public void kConstructionProlog2() {
    final XQuery query = new XQuery(
      "declare(::)construction(::)Preserve;(::)1(::)eq(::)1",
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
   *  Test that 'declare construction' with the strip keyword is parsed properly. .
   */
  @org.junit.Test
  public void kConstructionProlog3() {
    final XQuery query = new XQuery(
      "declare(::)construction(::)strip;(::)1(::)eq(::)1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Two 'declare construction' expressions is invalid. .
   */
  @org.junit.Test
  public void kConstructionProlog4() {
    final XQuery query = new XQuery(
      "declare(::)construction(::)strip; declare(::)construction(::)strip;1(::)eq(::)1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0067")
    );
  }

  /**
   *  Ensure the 'construction' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2ConstructionProlog1() {
    final XQuery query = new XQuery(
      "construction gt construction",
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
   *  Evaluation of a prolog with two construction declarations. .
   */
  @org.junit.Test
  public void constprolog1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        declare construction strip; \n" +
      "        \"abc\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0067")
    );
  }

  /**
   *  Evaluation of a prolog with construction declaration set to "preserve" for a directly constructed element and used as argument to fn:not. .
   */
  @org.junit.Test
  public void constprolog10() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        let $anElement := <someElement>some content</someElement> \n" +
      "        return fn:not($anElement instance of element(*,xs:anyType))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of a prolog with construction declaration set to "preserve" for a computed element and used as argument to fn:not. .
   */
  @org.junit.Test
  public void constprolog11() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        let $anElement := element someElement{\"some content\"} \n" +
      "        return fn:not($anElement instance of element(*,xs:anyType))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of a prolog with construction declaration set to "strip" for a directly constructed element (inside of instance expression) and used as argument to fn:not() . .
   */
  @org.junit.Test
  public void constprolog12() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        fn:not(<someElement>some content</someElement> instance of element(*,xs:untyped))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of a prolog with construction declaration set to "preserve" for a computed element (inside of instance of expression) and used as argument to fn:not. .
   */
  @org.junit.Test
  public void constprolog13() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        fn:not(element someElement{\"some content\"} instance of element(*,xs:anyType))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of a prolog with construction declaration set to "strip" for direct and used in boolean expression ("and" and "fn:true()") .
   */
  @org.junit.Test
  public void constprolog14() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $var := <anElement>Some content</anElement> \n" +
      "        return ($var instance of element(*,xs:untyped)) and fn:true()\n" +
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
   *  Evaluation of a prolog with construction declaration set to "strip" for computed element and used in boolean expression ("and" and "fn:true()") .
   */
  @org.junit.Test
  public void constprolog15() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := element someElement{\"some content\"} \n" +
      "        return ($anElement instance of element(*,xs:untyped)) and fn:true()\n" +
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
   *  Evaluation of a prolog with construction declaration set to "preserve" for direct element and used in boolean expression ("and" and "fn:true()") .
   */
  @org.junit.Test
  public void constprolog16() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        let $anElement := <someElement>content</someElement> \n" +
      "        return ($anElement instance of element(*,xs:anyType)) and fn:true()\n" +
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
   *  Evaluation of a prolog with construction declaration set to "preserve" for computed element and used in boolean expression ("and" and "fn:true()") .
   */
  @org.junit.Test
  public void constprolog17() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        let $anElement := element someElement{\"some content\"} \n" +
      "        return ($anElement instance of element(*,xs:anyType)) and fn:true()\n" +
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
   *  Evaluation of a prolog with construction declaration set to "strip" for direct and used in boolean expression ("or" and "fn:false()") .
   */
  @org.junit.Test
  public void constprolog18() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $var := <anElement>Some content</anElement> \n" +
      "        return ($var instance of element(*,xs:untyped)) or fn:false()\n" +
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
   *  Evaluation of a prolog with construction declaration set to "strip" for computed element and used in boolean expression ("or" and "fn:false()") .
   */
  @org.junit.Test
  public void constprolog19() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := element someElement{\"some content\"} \n" +
      "        return ($anElement instance of element(*,xs:untyped)) or fn:false()\n" +
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
   *  Evaluation of a prolog with construction declaration set to "strip" and used with directly construted element. .
   */
  @org.junit.Test
  public void constprolog2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := <anElement>some content</anElement> \n" +
      "        return $anElement instance of element(*,xs:untyped)\n" +
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
   *  Evaluation of a prolog with construction declaration set to "preserve" for direct element and used in boolean expression ("or" and "fn:false()") .
   */
  @org.junit.Test
  public void constprolog20() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        let $anElement := <someElement>content</someElement> \n" +
      "        return ($anElement instance of element(*,xs:anyType)) or fn:false()\n" +
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
   *  Evaluation of a prolog with construction declaration set to "preserve" for computed element and used in boolean expression ("or" and "fn:false()") .
   */
  @org.junit.Test
  public void constprolog21() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        let $anElement := element someElement{\"some content\"} \n" +
      "        return ($anElement instance of element(*,xs:anyType)) or fn:false()\n" +
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
   *  Evaluation of a prolog with construction declaration set to "strip" and used with computed delement. .
   */
  @org.junit.Test
  public void constprolog3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := element anElement {\"someContent\"} \n" +
      "        return $anElement instance of element(*,xs:untyped)\n" +
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
   *  Evaluation of a prolog with construction declaration set to "preserve" and used with directly construted element. .
   */
  @org.junit.Test
  public void constprolog4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        let $anElement := <anElement>some content</anElement> \n" +
      "        return $anElement instance of element(*,xs:anyType)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of a prolog with construction declaration set to "preserve" and used with computed delement. .
   */
  @org.junit.Test
  public void constprolog5() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := element anElement {\"someContent\"} \n" +
      "        return $anElement instance of element(*,xs:anyType)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of a prolog with construction declaration set to "strip" and used with directly construted element. Compare against wrong type .
   */
  @org.junit.Test
  public void constprolog6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := <anElement>some content</anElement> \n" +
      "        return $anElement instance of element(*,xs:anyType)\n" +
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
   *  Evaluation of a prolog with construction declaration set to "strip" and used with computed element, it compares agaisnt "xs:anyType". .
   */
  @org.junit.Test
  public void constprolog7() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := element anElement {\"someContent\"} \n" +
      "        return $anElement instance of element(*,xs:anyType)\n" +
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
   *  Evaluation of a prolog with construction declaration set to "strip" for a directly constructed element and used as argument to fn:not. .
   */
  @org.junit.Test
  public void constprolog8() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := <someElement>some content</someElement> \n" +
      "        return fn:not($anElement instance of element(*,xs:untyped))",
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
   *  Evaluation of a prolog with construction declaration set to "strip" for a computed element and used as argument to fn:not. .
   */
  @org.junit.Test
  public void constprolog9() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction strip; \n" +
      "        let $anElement := element someElement{\"some content\"} \n" +
      "        return fn:not($anElement instance of element(*,xs:untyped))",
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
