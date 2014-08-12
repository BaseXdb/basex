package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCodepointEqual extends QT3TestSet {

  /**
   *  Compare two values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2CodepointEqual1() {
    final XQuery query = new XQuery(
      "codepoint-equal(lower-case(concat(\"B STRING\", current-time())), \n" +
      "                            lower-case(concat(\"b string\", current-time())))\n" +
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
   *  Compare two values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2CodepointEqual2() {
    final XQuery query = new XQuery(
      "codepoint-equal(upper-case(concat(\"B STRING\", current-time())), \n" +
      "                            upper-case(concat(\"b string\", current-time())))\n" +
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
   *  Compare two non-matching values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2CodepointEqual3() {
    final XQuery query = new XQuery(
      "codepoint-equal(lower-case(concat(\"B STRING\", current-time())), \n" +
      "                            lower-case(concat(\"no match\", current-time())))",
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
   *  Compare two non-matching values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2CodepointEqual4() {
    final XQuery query = new XQuery(
      "codepoint-equal(upper-case(concat(\"B STRING\", current-time())), \n" +
      "                            upper-case(concat(\"no match\", current-time())))\n" +
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
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2CodepointEqual5() {
    final XQuery query = new XQuery(
      "codepoint-equal(upper-case(concat(\"B STRING\", current-time())), \n" +
      "                            lower-case(concat(\"no match\", current-time())))\n" +
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
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2CodepointEqual6() {
    final XQuery query = new XQuery(
      "codepoint-equal(lower-case(concat(\"B STRING\", current-time())), \n" +
      "                            upper-case(concat(\"no match\", current-time())))\n" +
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
   *  test fn:codepoint-equal with empty $arg1 .
   */
  @org.junit.Test
  public void cbclCodepointEqual001() {
    final XQuery query = new XQuery(
      "declare function local:yes-empty($arg as xs:boolean) as xs:string? { if ($arg) then 'yes' else () }; \n" +
      "      empty(fn:codepoint-equal( local:yes-empty(fn:false()), local:yes-empty(fn:true()) ) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:codepoint-equal with empty $arg2 .
   */
  @org.junit.Test
  public void cbclCodepointEqual002() {
    final XQuery query = new XQuery(
      "declare function local:yes-empty($arg as xs:boolean) as xs:string? { if ($arg) then 'yes' else () }; \n" +
      "      empty(fn:codepoint-equal( local:yes-empty(fn:true()), local:yes-empty(fn:false()) ) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:codepoint-equal with non-empty arguments .
   */
  @org.junit.Test
  public void cbclCodepointEqual003() {
    final XQuery query = new XQuery(
      "declare function local:yes-no($arg as xs:boolean) as xs:string { if ($arg) then 'yes' else 'no' }; \n" +
      "      fn:codepoint-equal( local:yes-no(fn:true()), local:yes-no(fn:false()) ) = false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:boolean on fn:codepoint-equal with empty $arg2 .
   */
  @org.junit.Test
  public void cbclCodepointEqual004() {
    final XQuery query = new XQuery(
      "declare function local:yes-empty($arg as xs:boolean) as xs:string? { if ($arg) then 'yes' else () }; \n" +
      "      boolean(fn:codepoint-equal( local:yes-empty(fn:true()), local:yes-empty(fn:false()) ) )",
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
   *  test fn:boolean on fn:codepoint-equal with empty $arg1 .
   */
  @org.junit.Test
  public void cbclCodepointEqual005() {
    final XQuery query = new XQuery(
      "declare function local:yes-empty($arg as xs:boolean) as xs:string? { if ($arg) then 'yes' else () }; \n" +
      "      boolean(fn:codepoint-equal( local:yes-empty(fn:false()), local:yes-empty(fn:true()) ) )",
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
   *  test fn:codepoint-equal evaluate to item .
   */
  @org.junit.Test
  public void cbclCodepointEqual006() {
    final XQuery query = new XQuery(
      "declare function local:yes-no($arg as xs:boolean) as xs:string { if ($arg) then 'yes' else 'no' }; \n" +
      "      fn:index-of( (fn:true(), fn:false()), fn:codepoint-equal( local:yes-no(fn:false()), local:yes-no(fn:true()) ) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" with wrong arity. .
   */
  @org.junit.Test
  public void fnCodepointEqual1() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"a\")",
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
   *  Evaluation of an "fn:codepoint-equal" with wrong argument type. .
   */
  @org.junit.Test
  public void fnCodepointEqual10() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(xs:integer(1),xs:integer(1))",
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
   *  Evaluation of an "fn:codepoint-equal" with wrong argument type (only second argument). .
   */
  @org.junit.Test
  public void fnCodepointEqual11() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",xs:integer(1))",
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
   *  Evaluation of an "fn:codepoint-equal" with arguments set to "aa" and "AA" respectively. .
   */
  @org.junit.Test
  public void fnCodepointEqual12() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",\"AA\")",
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
   *  Evaluation of an "fn:codepoint-equal" with arguments set to "aa" and lower-case("AA") respectively. .
   */
  @org.junit.Test
  public void fnCodepointEqual13() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",fn:lower-case(\"AA\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" with arguments set to "aa" and upper-case("aa") respectively. .
   */
  @org.junit.Test
  public void fnCodepointEqual14() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",fn:upper-case(\"aa\"))",
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
   *  Evaluation of an "fn:codepoint-equal" as an argument to xs:boolean. .
   */
  @org.junit.Test
  public void fnCodepointEqual15() {
    final XQuery query = new XQuery(
      "xs:boolean(fn:codepoint-equal(\"aa\",\"aa\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" as part of boolean expression ("and" and fn:true()). .
   */
  @org.junit.Test
  public void fnCodepointEqual16() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",\"aa\") and fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" as part of boolean expression ("and" and fn:false()). .
   */
  @org.junit.Test
  public void fnCodepointEqual17() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",\"aa\") and fn:false()",
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
   *  Evaluation of an "fn:codepoint-equal" as part of boolean expression ("or" and fn:true()). .
   */
  @org.junit.Test
  public void fnCodepointEqual18() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",\"aa\") or fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" as part of boolean expression ("or" and fn:false()). .
   */
  @org.junit.Test
  public void fnCodepointEqual19() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",\"aa\") or fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" with arguments set to empty sequence .
   */
  @org.junit.Test
  public void fnCodepointEqual2() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal((),())",
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
   *  Evaluation of an "fn:codepoint-equal" as part of boolean expression involving two fn:codepoint-equal ("and" operator). .
   */
  @org.junit.Test
  public void fnCodepointEqual20() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",\"aa\") and fn:codepoint-equal(\"aa\",\"aa\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" as part of boolean expression involving two fn:codepoint-equal ("or" operator). .
   */
  @org.junit.Test
  public void fnCodepointEqual21() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"aa\",\"aa\") or fn:codepoint-equal(\"aa\",\"aa\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Different normalization forms of the same string are not codepoint-equal .
   */
  @org.junit.Test
  public void fnCodepointEqual22() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(normalize-unicode(\"garçon\", \"NFC\"), normalize-unicode(\"garçon\", \"NFD\"))",
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
   *  Evaluation of an "fn:codepoint-equal" with one argument set to empty sequence Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnCodepointEqual2a() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"abc\",())",
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
   *  Evaluation of an "fn:codepoint-equal" with one argument set to empty sequence Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnCodepointEqual2b() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal((), \"\")",
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
   *  Evaluation of an "fn:codepoint-equal" with arguments set to zero length string. .
   */
  @org.junit.Test
  public void fnCodepointEqual3() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"\",\"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" with arguments set to same value "a". .
   */
  @org.junit.Test
  public void fnCodepointEqual4() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"a\",\"a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" with arguments set to different values "a", "b" respectively. .
   */
  @org.junit.Test
  public void fnCodepointEqual5() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(\"a\",\"b\")",
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
   *  Evaluation of an "fn:codepoint-equal" as argument to fn:not. Returns true .
   */
  @org.junit.Test
  public void fnCodepointEqual6() {
    final XQuery query = new XQuery(
      "fn:not(fn:codepoint-equal(\"a\",\"b\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" as argument to fn:not. Returns false .
   */
  @org.junit.Test
  public void fnCodepointEqual7() {
    final XQuery query = new XQuery(
      "fn:not(fn:codepoint-equal(\"a\",\"a\"))",
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
   *  Evaluation of an "fn:codepoint-equal" whose arguments use fn:string() for a number .
   */
  @org.junit.Test
  public void fnCodepointEqual8() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(fn:string(1),fn:string(1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:codepoint-equal" whose arguments use fn:string() for a string. .
   */
  @org.junit.Test
  public void fnCodepointEqual9() {
    final XQuery query = new XQuery(
      "fn:codepoint-equal(fn:string(\"aa\"),fn:string(\"aa\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
