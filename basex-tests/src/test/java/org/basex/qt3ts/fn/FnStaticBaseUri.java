package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the static-base-uri() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnStaticBaseUri extends QT3TestSet {

  /**
   *  A test whose essence is: `static-base-uri(.)`. .
   */
  @org.junit.Test
  public void kStaticBaseURIFunc1() {
    final XQuery query = new XQuery(
      "static-base-uri(.)",
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
   *  A test whose essence is: `static-base-uri(1, 2)`. .
   */
  @org.junit.Test
  public void kStaticBaseURIFunc2() {
    final XQuery query = new XQuery(
      "static-base-uri(1, 2)",
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
   *  A test whose essence is: `if(static-base-uri()) then true() else true()`. .
   */
  @org.junit.Test
  public void kStaticBaseURIFunc3() {
    final XQuery query = new XQuery(
      "if(static-base-uri()) then true() else true()",
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
   *  Evaluation of fn;static-base-uri function with incorrect arity. .
   */
  @org.junit.Test
  public void fnStaticBase1() {
    final XQuery query = new XQuery(
      "fn:static-base-uri(\"A argument\")",
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
   *  Evaluation of fn:static-base-uri function using Used as argument to fn:concat function. Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase10() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:concat(fn:string(fn:static-base-uri()),\"another string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.comanother string")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using Used as argument to fn:string-join function. Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase11() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:string-join((fn:string(fn:static-base-uri()),\"another string\"),\"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.comanother string")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using Used as argument to fn:string-length function. Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase12() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:string-length(fn:string(fn:static-base-uri()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "22")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using Used as argument to fn:substring-before function. Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase13() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:substring-before(fn:string(fn:static-base-uri()),\":\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using Used as argument to fn:substring-after function. Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase14() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:substring-after(fn:string(fn:static-base-uri()),\":\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "//www.example.com")
    );
  }

  /**
   * Rely on test case environment for the base URI (this should work under XPath, but depends on the test driver) .
   */
  @org.junit.Test
  public void fnStaticBase15() {
    final XQuery query = new XQuery(
      "static-base-uri()",
      ctx);
    try {
      query.baseURI("http://www.example.com");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using "ftp". Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase2() {
    final XQuery query = new XQuery(
      "declare base-uri \"ftp://ftp.is.co.za/rfc/somefile.txt\"; fn:string(fn:static-base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ftp://ftp.is.co.za/rfc/somefile.txt")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using "mailto". Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase3() {
    final XQuery query = new XQuery(
      "declare base-uri \"mailto:John.Doe@example.com\"; fn:string(fn:static-base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "mailto:John.Doe@example.com")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using "news". Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase4() {
    final XQuery query = new XQuery(
      "declare base-uri \"news:comp.infosystems.www.servers.unix\"; fn:string(fn:static-base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "news:comp.infosystems.www.servers.unix")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using "telnet". Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase5() {
    final XQuery query = new XQuery(
      "declare base-uri \"telnet://192.0.2.16:80/\"; fn:string(fn:static-base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "telnet://192.0.2.16:80/")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using "tel". Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase6() {
    final XQuery query = new XQuery(
      "declare base-uri \"tel:+1-816-555-1212\"; fn:string(fn:static-base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "tel:+1-816-555-1212")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using "ldap" scheme. Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase7() {
    final XQuery query = new XQuery(
      "declare base-uri \"urn:oasis:names:specification:docbook:dtd:xml:4.1.2\"; fn:string(fn:static-base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "urn:oasis:names:specification:docbook:dtd:xml:4.1.2")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using Used as argument to upper-case function. Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase8() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:upper-case(fn:string(fn:static-base-uri()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "HTTP://WWW.EXAMPLE.COM")
    );
  }

  /**
   *  Evaluation of fn:static-base-uri function using Used as argument to lower-case function. Uses fn:string . .
   */
  @org.junit.Test
  public void fnStaticBase9() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:lower-case(fn:string(fn:static-base-uri()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }
}
