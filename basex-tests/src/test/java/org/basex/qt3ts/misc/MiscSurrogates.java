package org.basex.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the surrogates operator.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscSurrogates extends QT3TestSet {

  /**
   *  string-length() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates01() {
    final XQuery query = new XQuery(
      "string-length(\"abc&#x1D156;def\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("7")
    );
  }

  /**
   *  substring() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates02() {
    final XQuery query = new XQuery(
      "substring(\"abc&#x1D156;def\", 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "def")
    );
  }

  /**
   *  substring() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates03() {
    final XQuery query = new XQuery(
      "substring(\"abc&#x1D156;def\", 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "𝅖def")
    );
  }

  /**
   *  translate() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates04() {
    final XQuery query = new XQuery(
      "translate(\"abc&#x1D156;def\", \"&#x1D156;\", \"#\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc#def")
    );
  }

  /**
   *  translate() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates05() {
    final XQuery query = new XQuery(
      "translate(\"abc&#x1D156;def\", \"&#x1D156;de\", \"#DE\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc#DEf")
    );
  }

  /**
   *  translate() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates06() {
    final XQuery query = new XQuery(
      "translate(\"abc&#x1D156;def\", \"def\", \"&#x1D156;EF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc𝅖𝅖EF")
    );
  }

  /**
   *  string-to-codepoints() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates07() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"abc&#x1D156;def\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "97 98 99 119126 100 101 102")
    );
  }

  /**
   *  codepoints-to-string() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates08() {
    final XQuery query = new XQuery(
      "codepoints-to-string((97, 98, 99, 119126, 100, 101, 102))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc𝅖def")
    );
  }

  /**
   *  codepoints-to-string() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates08a() {
    final XQuery query = new XQuery(
      "codepoints-to-string((97, 98, 99, 119126, 100, 101, 102))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc𝅖def")
    );
  }

  /**
   *  substring-before() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates09() {
    final XQuery query = new XQuery(
      "substring-before(\"abc&#x1D156;def\", \"&#x1D156;\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
   *  substring-before() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates10() {
    final XQuery query = new XQuery(
      "substring-before(\"abc&#x1D156;def\", \"f\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc𝅖de")
    );
  }

  /**
   *  substring-after() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates11() {
    final XQuery query = new XQuery(
      "substring-after(\"abc&#x1D156;def\", \"&#x1D156;\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "def")
    );
  }

  /**
   *  matches() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates12() {
    final XQuery query = new XQuery(
      "matches(\"abc&#x1D157;def\", \"abc[&#x1D156;-&#x1D158;]def\")",
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
   *  matches() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates13() {
    final XQuery query = new XQuery(
      "matches(\"abc&#x1D157;def\", \"abc.def\")",
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
   *  replace() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates14() {
    final XQuery query = new XQuery(
      "replace(\"abc&#119130;def\", \"[&#119120;-&#119135;]\", \"&#119135;\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc𝅘𝅥def")
    );
  }

  /**
   *  replace() when non-BMP characters are present .
   */
  @org.junit.Test
  public void surrogates15() {
    final XQuery query = new XQuery(
      "replace(\"abc&#x1D157;def\", \"[^a-f]\", \"###\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc###def")
    );
  }

  /**
   * Leading surrogate = edge value of 56319. Test case from Gunther Rademacher.
   */
  @org.junit.Test
  public void surrogates16() {
    final XQuery query = new XQuery(
      "string-to-codepoints(substring(\"&#x10FC00;A\", 2, 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65")
    );
  }
}
