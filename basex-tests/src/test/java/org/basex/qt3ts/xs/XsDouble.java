package org.basex.qt3ts.xs;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests features of XSD 1.1 doubles.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsDouble extends QT3TestSet {

  /**
   * Ensure +INF rejected in XSD 1.0.
   */
  @org.junit.Test
  public void xsDouble004() {
    final XQuery query = new XQuery(
      "exists(xs:double(\"+INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   * Parse a particular number known to cause trouble in Java and PHP.
   *         Some versions of Java go into an infinite loop on this one.
   *         You may be able to fix this by applying a Java patch: see
   *         http://www.oracle.com/technetwork/topics/security/alert-cve-2010-4476-305811.html.
   */
  @org.junit.Test
  public void xsDouble005() {
    final XQuery query = new XQuery(
      "xs:double(\"2.2250738585072012e-308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:double")
    );
  }
}
