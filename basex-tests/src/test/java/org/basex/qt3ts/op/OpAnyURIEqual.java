package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the anyURI-equal operator (not actually defined as such in F+O).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAnyURIEqual extends QT3TestSet {

  /**
   *  A test whose essence is: `xs:anyURI("example.com/") eq xs:anyURI("example.com/")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual1() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") eq xs:anyURI(\"example.com/\")",
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
   *  A test whose essence is: `xs:untypedAtomic("example.com/") ne xs:anyURI("example.com/No")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual10() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"example.com/\") ne xs:anyURI(\"example.com/No\")",
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
   *  A test whose essence is: `xs:anyURI("example.com/") ne xs:anyURI("example.com/No")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual2() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") ne xs:anyURI(\"example.com/No\")",
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
   *  A test whose essence is: `xs:anyURI("example.com/") eq xs:string("example.com/")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual3() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") eq xs:string(\"example.com/\")",
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
   *  A test whose essence is: `xs:string("example.com/") eq xs:anyURI("example.com/")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual4() {
    final XQuery query = new XQuery(
      "xs:string(\"example.com/\") eq xs:anyURI(\"example.com/\")",
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
   *  A test whose essence is: `xs:anyURI("example.com/") ne xs:string("example.com/No")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual5() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") ne xs:string(\"example.com/No\")",
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
   *  A test whose essence is: `xs:string("example.com/") ne xs:anyURI("example.com/No")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual6() {
    final XQuery query = new XQuery(
      "xs:string(\"example.com/\") ne xs:anyURI(\"example.com/No\")",
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
   *  A test whose essence is: `xs:anyURI("example.com/") eq xs:untypedAtomic("example.com/")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual7() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") eq xs:untypedAtomic(\"example.com/\")",
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
   *  A test whose essence is: `xs:untypedAtomic("example.com/") eq xs:anyURI("example.com/")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual8() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"example.com/\") eq xs:anyURI(\"example.com/\")",
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
   *  A test whose essence is: `xs:anyURI("example.com/") ne xs:untypedAtomic("example.com/No")`. .
   */
  @org.junit.Test
  public void kAnyURIEqual9() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") ne xs:untypedAtomic(\"example.com/No\")",
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
   *  Invoked 'eq' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual1() {
    final XQuery query = new XQuery(
      "xs:string(\"http://example.com/A\") eq xs:anyURI(\"http://example.com/A\")",
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
   *  Invoked 'ne' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual10() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/A\") ne xs:string(\"http://example.com/B\")",
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
   *  Invoked 'ne' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual11() {
    final XQuery query = new XQuery(
      "xs:string(\"http://example.com/A\") ne xs:anyURI(\"http://example.com/B\")",
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
   *  Invoked 'ne' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual12() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/A\") ne xs:anyURI(\"http://example.com/B\")",
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
   *  Invoked 'eq' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual2() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/A\") eq xs:string(\"http://example.com/A\")",
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
   *  Invoked 'eq' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual3() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/A\") eq xs:anyURI(\"http://example.com/A\")",
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
   *  Invoked 'eq' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual4() {
    final XQuery query = new XQuery(
      "not(xs:string(\"http://example.com/A\") eq xs:anyURI(\"http://example.com/B\"))",
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
   *  Invoked 'eq' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual5() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/A\") eq xs:string(\"http://example.com/B\"))",
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
   *  Invoked 'eq' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual6() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/A\") eq xs:anyURI(\"http://example.com/B\"))",
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
   *  Invoked 'ne' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual7() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/A\") ne xs:string(\"http://example.com/A\"))",
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
   *  Invoked 'ne' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual8() {
    final XQuery query = new XQuery(
      "not(xs:string(\"http://example.com/A\") ne xs:anyURI(\"http://example.com/A\"))",
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
   *  Invoked 'ne' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURIEqual9() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/A\") ne xs:anyURI(\"http://example.com/A\"))",
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
