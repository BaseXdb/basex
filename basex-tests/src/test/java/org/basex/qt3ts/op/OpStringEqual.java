package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the string-equal operation (not actually defined as such in F+O).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpStringEqual extends QT3TestSet {

  /**
   *  A test whose essence is: `'equals' eq 'equals'`. .
   */
  @org.junit.Test
  public void kStringEqual1() {
    final XQuery query = new XQuery(
      "'equals' eq 'equals'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `'' eq ''`. .
   */
  @org.junit.Test
  public void kStringEqual2() {
    final XQuery query = new XQuery(
      "'' eq ''",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not('nada' eq 'equalness')`. .
   */
  @org.junit.Test
  public void kStringEqual3() {
    final XQuery query = new XQuery(
      "not('nada' eq 'equalness')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `'not' ne 'equal'`. .
   */
  @org.junit.Test
  public void kStringEqual4() {
    final XQuery query = new XQuery(
      "'not' ne 'equal'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not('equal' ne 'equal')`. .
   */
  @org.junit.Test
  public void kStringEqual5() {
    final XQuery query = new XQuery(
      "not('equal' ne 'equal')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Compare two values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2StringEqual1() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"b string\", current-time(), string(<e>content</e>))[1] treat as xs:string; (lower-case($vA) eq lower-case($vB))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void k2StringEqual2() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"b string\", current-time(), string(<e>content</e>))[1] treat as xs:string; (upper-case($vA) eq upper-case($vB))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void k2StringEqual3() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"no match\", current-time(), string(<e>content</e>))[1] treat as xs:string; (lower-case($vA) eq lower-case($vB))",
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
  public void k2StringEqual4() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"no match\", current-time(), string(<e>content</e>))[1] treat as xs:string; (upper-case($vA) eq upper-case($vB))",
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
  public void k2StringEqual5() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"b string\", current-time(), string(<e>content</e>))[1] treat as xs:string; (upper-case($vA) eq lower-case($vB))",
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
  public void k2StringEqual6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; \n" +
      "        declare variable $vB as xs:string := (\"b string\", current-time(), string(<e>content</e>))[1] treat as xs:string; \n" +
      "        (lower-case($vA) eq upper-case($vB))\n" +
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
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringEqual001() {
    final XQuery query = new XQuery(
      "not(string(current-time()) eq \"now\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringEqual002() {
    final XQuery query = new XQuery(
      "not(string(current-time()) ne \"now\")",
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
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringEqual003() {
    final XQuery query = new XQuery(
      "\n" +
      "        not(xs:untypedAtomic(current-time()) eq xs:untypedAtomic(\"now\"))\n" +
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
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringEqual004() {
    final XQuery query = new XQuery(
      "\n" +
      "        not(xs:untypedAtomic(current-time()) ne xs:untypedAtomic(\"now\"))\n" +
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
}
