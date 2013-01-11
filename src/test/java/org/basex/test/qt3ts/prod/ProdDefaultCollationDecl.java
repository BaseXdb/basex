package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the DefaultCollationDecl production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDefaultCollationDecl extends QT3TestSet {

  /**
   *  A 'declare default collation' that uses a relative URI combined with setting the base-uri, to specify the Unicode Codepoint collation. .
   */
  @org.junit.Test
  public void kCollationProlog1() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.w3.org/2005/xpath-functions/\"; declare default collation \"collation/codepoint\"; default-collation() eq \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"",
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
   *  A 'declare default collation' that uses a relative URI combined with setting the base-uri, to specify an invalid collation. .
   */
  @org.junit.Test
  public void kCollationProlog2() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/\"; declare default collation \"collation/codepoint/DOESNOTEXIT/Testing\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0038")
    );
  }

  /**
   *  Any implementation must support setting the default collation to the Unicode Codepoint collation with 'declare default collation'. .
   */
  @org.junit.Test
  public void kCollationProlog3() {
    final XQuery query = new XQuery(
      "declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; default-collation() eq \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"",
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
   *  A 'declare default collation' expression may occur only once. .
   */
  @org.junit.Test
  public void kCollationProlog4() {
    final XQuery query = new XQuery(
      "declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; default-collation() eq \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0038")
    );
  }

  /**
   *  Two 'declare default collation' declarations where the collations differs. .
   */
  @org.junit.Test
  public void kCollationProlog5() {
    final XQuery query = new XQuery(
      "declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint2\"; default-collation() eq \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0038")
    );
  }

  /**
   *  Invalid URI literal in prolog. .
   */
  @org.junit.Test
  public void k2CollationProlog1() {
    final XQuery query = new XQuery(
      "declare default collation \"&\"; 1",
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
   *  Evaluation of a prolog with two default collation delarations. .
   */
  @org.junit.Test
  public void defaultcolldecl1() {
    final XQuery query = new XQuery(
      "declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; declare variable $input-context1 external; \"aaa\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0038")
    );
  }

  /**
   *  Evaluation of a prolog with default collation delarations that should raise an error. .
   */
  @org.junit.Test
  public void defaultcolldecl2() {
    final XQuery query = new XQuery(
      "declare default collation \"http://nonexistentcollition.org/ifsupportedwoooayouarethebestQueryimplementation/makeitharder\"; declare variable $input-context1 external; \"aaa\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0038")
    );
  }
}
