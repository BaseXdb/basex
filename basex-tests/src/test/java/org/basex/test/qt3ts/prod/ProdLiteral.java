package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the Literal production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdLiteral extends QT3TestSet {

  /**
   *  A test whose essence is: `'fo''o' eq 'fo''o'`. .
   */
  @org.junit.Test
  public void kLiterals1() {
    final XQuery query = new XQuery(
      "'fo''o' eq 'fo''o'",
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
   *  A test whose essence is: `1.3e-3 instance of xs:double`. .
   */
  @org.junit.Test
  public void kLiterals10() {
    final XQuery query = new XQuery(
      "1.3e-3 instance of xs:double",
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
   *  A test whose essence is: `1.e+3 instance of xs:double`. .
   */
  @org.junit.Test
  public void kLiterals11() {
    final XQuery query = new XQuery(
      "1.e+3 instance of xs:double",
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
   *  A test whose essence is: `1.e3 instance of xs:double`. .
   */
  @org.junit.Test
  public void kLiterals12() {
    final XQuery query = new XQuery(
      "1.e3 instance of xs:double",
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
   *  A test whose essence is: `1231.123e3 instance of xs:double`. .
   */
  @org.junit.Test
  public void kLiterals13() {
    final XQuery query = new XQuery(
      "1231.123e3 instance of xs:double",
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
   *  A test whose essence is: `1.E3 instance of xs:double`. .
   */
  @org.junit.Test
  public void kLiterals14() {
    final XQuery query = new XQuery(
      "1.E3 instance of xs:double",
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
   *  A test whose essence is: `-1231.123e3 instance of xs:double`. .
   */
  @org.junit.Test
  public void kLiterals15() {
    final XQuery query = new XQuery(
      "-1231.123e3 instance of xs:double",
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
   *  A test whose essence is: `3 instance of xs:integer`. .
   */
  @org.junit.Test
  public void kLiterals16() {
    final XQuery query = new XQuery(
      "3 instance of xs:integer",
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
   *  A test whose essence is: `3.3 instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kLiterals17() {
    final XQuery query = new XQuery(
      "3.3 instance of xs:decimal",
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
   *  A test whose essence is: `3.3e1 instance of xs:double`. .
   */
  @org.junit.Test
  public void kLiterals18() {
    final XQuery query = new XQuery(
      "3.3e1 instance of xs:double",
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
   *  A test whose essence is: `"a xs:string" instance of xs:string`. .
   */
  @org.junit.Test
  public void kLiterals19() {
    final XQuery query = new XQuery(
      "\"a xs:string\" instance of xs:string",
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
   *  A test whose essence is: `'foo' eq "foo"`. .
   */
  @org.junit.Test
  public void kLiterals2() {
    final XQuery query = new XQuery(
      "'foo' eq \"foo\"",
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
   *  A test whose essence is: `not(xs:double("NaN"))`. .
   */
  @org.junit.Test
  public void kLiterals20() {
    final XQuery query = new XQuery(
      "not(xs:double(\"NaN\"))",
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
   *  A test whose essence is: `not(xs:float("NaN"))`. .
   */
  @org.junit.Test
  public void kLiterals21() {
    final XQuery query = new XQuery(
      "not(xs:float(\"NaN\"))",
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
   *  A test whose essence is: `xs:float("NaN") instance of xs:float`. .
   */
  @org.junit.Test
  public void kLiterals22() {
    final XQuery query = new XQuery(
      "xs:float(\"NaN\") instance of xs:float",
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
   *  '3 >E 2' is a syntatically invalid expression. .
   */
  @org.junit.Test
  public void kLiterals23() {
    final XQuery query = new XQuery(
      "3 >E 2",
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
   *  A syntactically invalid query, reminding of somekind of literal. .
   */
  @org.junit.Test
  public void kLiterals24() {
    final XQuery query = new XQuery(
      "33\"",
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
   *  A syntactically invalid query. .
   */
  @org.junit.Test
  public void kLiterals25() {
    final XQuery query = new XQuery(
      "2 + 3!#",
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
   *  '432f542' is a syntactically invalid expression. .
   */
  @org.junit.Test
  public void kLiterals26() {
    final XQuery query = new XQuery(
      "432f542",
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
   *  '.54.45' is a syntactically invalid expression. .
   */
  @org.junit.Test
  public void kLiterals27() {
    final XQuery query = new XQuery(
      ".54.45",
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
   *  '.3' is a valid literal. .
   */
  @org.junit.Test
  public void kLiterals28() {
    final XQuery query = new XQuery(
      "0.3 eq .3",
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
   *  An empty string is not a valid XPath/XQuery expression. .
   */
  @org.junit.Test
  public void kLiterals29() {
    final XQuery query = new XQuery(
      "",
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
   *  A test whose essence is: `'f'oo'`. .
   */
  @org.junit.Test
  public void kLiterals3() {
    final XQuery query = new XQuery(
      "'f'oo'",
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
   *  '{1}' is a syntactically invalid XQuery expression. .
   */
  @org.junit.Test
  public void kLiterals30() {
    final XQuery query = new XQuery(
      "{1}",
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
   *  An invalid character reference. .
   */
  @org.junit.Test
  public void kLiterals31() {
    final XQuery query = new XQuery(
      "\"a string &;\"",
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
   *  An invalid decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals32() {
    final XQuery query = new XQuery(
      "\"a string &#;\"",
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
   *  An invalid decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals33() {
    final XQuery query = new XQuery(
      "\"a string &#;\"",
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
   *  An invalid decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals34() {
    final XQuery query = new XQuery(
      "\"a string &#1233a98;\"",
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
   *  An invalid decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals35() {
    final XQuery query = new XQuery(
      "\"a string &#1233.98;\"",
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
   *  An invalid decimal character reference, they cannot contain whitespace. .
   */
  @org.junit.Test
  public void kLiterals36() {
    final XQuery query = new XQuery(
      "\"a string &#1233 98;\"",
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
   *  Simple test of a decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals37() {
    final XQuery query = new XQuery(
      "\"t\" eq \"t\"",
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
   *  An invalid hexa-decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals38() {
    final XQuery query = new XQuery(
      "\"a string &#x;\"",
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
   *  An invalid hexa-decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals39() {
    final XQuery query = new XQuery(
      "\"a string &#x543.3;\"",
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
   *  '""""' is a valid string literal. .
   */
  @org.junit.Test
  public void kLiterals4() {
    final XQuery query = new XQuery(
      "\"\"\"\" eq '\"'",
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
   *  An invalid hexa-decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals40() {
    final XQuery query = new XQuery(
      "\"a string &#x543g3;\"",
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
   *  An invalid hexa-decimal character reference, they cannot contain whitespace. .
   */
  @org.junit.Test
  public void kLiterals41() {
    final XQuery query = new XQuery(
      "\"a string &#x543 3;\"",
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
   *  'LT' is not a predefined character reference. .
   */
  @org.junit.Test
  public void kLiterals42() {
    final XQuery query = new XQuery(
      "\"a string &LT;\"",
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
   *  'lte' is not a predefined character reference. .
   */
  @org.junit.Test
  public void kLiterals43() {
    final XQuery query = new XQuery(
      "\"a string &lte;\"",
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
   *  '&' must be escaped. .
   */
  @org.junit.Test
  public void kLiterals44() {
    final XQuery query = new XQuery(
      "\"a string &\"",
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
   *  An invalid hexa-decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals45() {
    final XQuery query = new XQuery(
      "\"a string &#x;\"",
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
   *  Test containing all predefined character references and one hexa and decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals46() {
    final XQuery query = new XQuery(
      "\"&lt; &gt; &amp; &quot; &apos; &#x48; &#48;\" eq \"< > &amp; \"\" ' &#x48; &#48;\"",
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
   *  Simple test of a hexa decimal character reference. .
   */
  @org.junit.Test
  public void kLiterals47() {
    final XQuery query = new XQuery(
      "\"t\" eq \"&#x74;\"",
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
   *  The 'x' in a hexa decimal character reference must be lower case. .
   */
  @org.junit.Test
  public void kLiterals48() {
    final XQuery query = new XQuery(
      "\"&#X4A;\"",
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
   *  An '&' character reference embedded in other text. .
   */
  @org.junit.Test
  public void kLiterals49() {
    final XQuery query = new XQuery(
      "\"I love brownies&amp;cookies.\" eq \"I love brownies&amp;cookies.\"",
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
   *  '''''' is a valid string literal. .
   */
  @org.junit.Test
  public void kLiterals5() {
    final XQuery query = new XQuery(
      "'''' eq \"'\"",
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
   *  Character references are only allowed inside string literals. .
   */
  @org.junit.Test
  public void kLiterals50() {
    final XQuery query = new XQuery(
      "1 &lt;= 3",
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
   *  '"fo""""' is a valid string literal. .
   */
  @org.junit.Test
  public void kLiterals6() {
    final XQuery query = new XQuery(
      "\"fo\"\"o\" eq concat(\"fo\", \"\"\"\", \"o\")",
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
   *  '"f"oo"' is an invalid string literal. .
   */
  @org.junit.Test
  public void kLiterals7() {
    final XQuery query = new XQuery(
      "\"f\"oo\"",
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
   *  '3.' is a valid number literal. .
   */
  @org.junit.Test
  public void kLiterals8() {
    final XQuery query = new XQuery(
      "3. eq 3.",
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
   *  '.3' is a valid number literal. .
   */
  @org.junit.Test
  public void kLiterals9() {
    final XQuery query = new XQuery(
      ".3 eq .3",
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
   *  An invalid character reference. .
   */
  @org.junit.Test
  public void k2Literals1() {
    final XQuery query = new XQuery(
      "\"&#x00;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }

  /**
   *  Attempt to trigger underflow in an xs:double literal. .
   */
  @org.junit.Test
  public void k2Literals10() {
    final XQuery query = new XQuery(
      "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001e10",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "0")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Negative zero, as a double. .
   */
  @org.junit.Test
  public void k2Literals11() {
    final XQuery query = new XQuery(
      "-0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-0")
      &&
        assertStringValue(false, "-0")
      )
    );
  }

  /**
   *  Zero, as a double. .
   */
  @org.junit.Test
  public void k2Literals12() {
    final XQuery query = new XQuery(
      "0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      &&
        assertStringValue(false, "0")
      )
    );
  }

  /**
   *  Negative zero, as a decimal. .
   */
  @org.junit.Test
  public void k2Literals13() {
    final XQuery query = new XQuery(
      "-0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      &&
        assertStringValue(false, "0")
      )
    );
  }

  /**
   *  Zero, as a decimal. .
   */
  @org.junit.Test
  public void k2Literals14() {
    final XQuery query = new XQuery(
      "0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      &&
        assertStringValue(false, "0")
      )
    );
  }

  /**
   *  A literal looking like a negative zero integer. .
   */
  @org.junit.Test
  public void k2Literals15() {
    final XQuery query = new XQuery(
      "-0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      &&
        assertStringValue(false, "0")
      )
    );
  }

  /**
   *  Use a character reference that is a decimal 32 bit overflow. .
   */
  @org.junit.Test
  public void k2Literals16() {
    final XQuery query = new XQuery(
      "<p>FA&#xFF000000F6;IL</p>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }

  /**
   *  Use a character reference that is a hexa decimal 32 bit overflow. .
   */
  @org.junit.Test
  public void k2Literals17() {
    final XQuery query = new XQuery(
      "<p>FA&#4294967542;IL</p>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }

  /**
   *  Use a character reference that is a decimal 64 bit overflow. .
   */
  @org.junit.Test
  public void k2Literals18() {
    final XQuery query = new XQuery(
      "<p>FA&#xFFFFFFFF000000F6;IL</p>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }

  /**
   *  Use a character reference that is a hexa decimal 64 bit overflow. .
   */
  @org.junit.Test
  public void k2Literals19() {
    final XQuery query = new XQuery(
      "<p>FA&#18446744073709551862;IL</p>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }

  /**
   *  An invalid character reference. .
   */
  @org.junit.Test
  public void k2Literals2() {
    final XQuery query = new XQuery(
      "\"&\"",
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
   *  Expressions can't be separated by semi-colons. .
   */
  @org.junit.Test
  public void k2Literals20() {
    final XQuery query = new XQuery(
      "1 ; 1",
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
   *  XQuery doesn't have a syntax for expressing hexa decimals. .
   */
  @org.junit.Test
  public void k2Literals21() {
    final XQuery query = new XQuery(
      "0x20",
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
   *  A minus sign is not allowed in hexa character references. .
   */
  @org.junit.Test
  public void k2Literals22() {
    final XQuery query = new XQuery(
      "\"&#x-20;\"",
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
   *  A minus sign is not allowed in decimal character references. .
   */
  @org.junit.Test
  public void k2Literals23() {
    final XQuery query = new XQuery(
      "\"&#-20;\"",
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
   *  A plus sign is not allowed in hexa character references. .
   */
  @org.junit.Test
  public void k2Literals24() {
    final XQuery query = new XQuery(
      "\"&#x+20;\"",
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
   *  A plus sign is not allowed in decimal character references. .
   */
  @org.junit.Test
  public void k2Literals25() {
    final XQuery query = new XQuery(
      "\"&#+20;\"",
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
   *  Only digits are allowed in decimal character references. .
   */
  @org.junit.Test
  public void k2Literals26() {
    final XQuery query = new XQuery(
      "\"&#2A0;\"",
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
   *  'W' is an invalid character in a hexa character reference. .
   */
  @org.junit.Test
  public void k2Literals27() {
    final XQuery query = new XQuery(
      "\"&#xW20;\"",
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
   *  Ensure that EOLs are normalized in string literals. .
   */
  @org.junit.Test
  public void k2Literals28() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/Literal/K2-Literals-28.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("deep-equal(string-to-codepoints($result), \n            (97, 10, 10, 10, 32, 10, 115, 116, 114, 105, 110, 103, 32, 108, 105, 116, 101, 114, 97, 108, 32, 10))")
    );
  }

  /**
   *  A syntactically invalid character reference. .
   */
  @org.junit.Test
  public void k2Literals29() {
    final XQuery query = new XQuery(
      "\"&#0xA;\"",
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
   *  Preceeding zeros are allowed in character references. .
   */
  @org.junit.Test
  public void k2Literals3() {
    final XQuery query = new XQuery(
      "\"&#0000045;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-")
    );
  }

  /**
   *  Check EBNF 142. .
   */
  @org.junit.Test
  public void k2Literals30() {
    final XQuery query = new XQuery(
      ".5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.5")
    );
  }

  /**
   *  Check EBNF 142(#2). .
   */
  @org.junit.Test
  public void k2Literals31() {
    final XQuery query = new XQuery(
      ". 5",
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
   *  Numeric literals can end with a dot. .
   */
  @org.junit.Test
  public void k2Literals32() {
    final XQuery query = new XQuery(
      "465.",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("465")
    );
  }

  /**
   *  Two subsequent right curlies. .
   */
  @org.junit.Test
  public void k2Literals33() {
    final XQuery query = new XQuery(
      "} }",
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
   *  A main module without query body. .
   */
  @org.junit.Test
  public void k2Literals34() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://example.com/\";",
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
   *  Attempt to evaluate a library module. The specification does not specify what an implementation 
   *         should do, so we allow any output and any error code. .
   */
  @org.junit.Test
  public void k2Literals35() {
    final XQuery query = new XQuery(
      "module namespace prefix = \"http://example.com\"; declare function prefix:myFunction() { 1 };",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertQuery("true()")
      ||
        error("*")
      )
    );
  }

  /**
   *  Attempt to evaluate a library module. The specification does not specify what an implementation 
   *         should do, so we allow any output and any error code. This is a simpler version of the previous query. .
   */
  @org.junit.Test
  public void k2Literals36() {
    final XQuery query = new XQuery(
      "module namespace prefix = \"http://www.example.com/\";",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertQuery("true()")
      ||
        error("*")
      )
    );
  }

  /**
   *  Ensure 'import' is properly parsed as a keyword. .
   */
  @org.junit.Test
  public void k2Literals37() {
    final XQuery query = new XQuery(
      "import gt import",
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
   *  Ensure 'schema' is properly parsed as a keyword. .
   */
  @org.junit.Test
  public void k2Literals38() {
    final XQuery query = new XQuery(
      "schema gt schema",
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
   *  Ensure that EOLs are normalized in string literals, using single quotes. .
   */
  @org.junit.Test
  public void k2Literals39() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/Literal/K2-Literals-39.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a \n\n\n \nstring literal \n")
    );
  }

  /**
   *  Use a relatively large xs:integer literal. .
   */
  @org.junit.Test
  public void k2Literals4() {
    final XQuery query = new XQuery(
      "999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Use a relatively small xs:integer literal. .
   */
  @org.junit.Test
  public void k2Literals5() {
    final XQuery query = new XQuery(
      "-999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Use a relatively large xs:decimal literal. .
   */
  @org.junit.Test
  public void k2Literals6() {
    final XQuery query = new XQuery(
      "999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999.1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999.1")
      ||
        assertEq("1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Use a relatively small xs:decimal literal, that might result in an underflow. .
   */
  @org.junit.Test
  public void k2Literals7() {
    final XQuery query = new XQuery(
      "0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        assertStringValue(false, "0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001")
      ||
        error("FOCA0006")
      )
    );
  }

  /**
   *  An xs:double literal with a large significand. .
   */
  @org.junit.Test
  public void k2Literals8() {
    final XQuery query = new XQuery(
      "999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999E100000000000000000000000000000000000000000000000000000000",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "INF")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  An xs:double literal with a small significand. .
   */
  @org.junit.Test
  public void k2Literals9() {
    final XQuery query = new XQuery(
      "-999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999E100000000000000000000000000000000000000000000000000000000",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "-INF")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Simple use case for string literals .
   */
  @org.junit.Test
  public void literals001() {
    final XQuery query = new XQuery(
      "\"test\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "test")
    );
  }

  /**
   *  Simple use case for string literals .
   */
  @org.junit.Test
  public void literals002() {
    final XQuery query = new XQuery(
      "'test'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "test")
    );
  }

  /**
   *  Test case where string literal contains a new line .
   */
  @org.junit.Test
  public void literals003() {
    final XQuery query = new XQuery(
      "\"line1\n" +
      "line2\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "line1\nline2")
    );
  }

  /**
   *  Test case where string literal contains a new line .
   */
  @org.junit.Test
  public void literals004() {
    final XQuery query = new XQuery(
      "'line1\n" +
      "line2'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "line1\nline2")
    );
  }

  /**
   *  Test case for a sequence of string literals .
   */
  @org.junit.Test
  public void literals005() {
    final XQuery query = new XQuery(
      "<result>{ (\"test1\", \"test2\") }</result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result>test1 test2</result>", false)
    );
  }

  /**
   *  Unterminated string literal .
   */
  @org.junit.Test
  public void literals006() {
    final XQuery query = new XQuery(
      "\"test",
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
   *  Unterminated string literal .
   */
  @org.junit.Test
  public void literals007() {
    final XQuery query = new XQuery(
      "'test",
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
   *  Incorrectly terminated string literal .
   */
  @org.junit.Test
  public void literals008() {
    final XQuery query = new XQuery(
      "'test\"",
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
   *  Incorrectly terminated string literal .
   */
  @org.junit.Test
  public void literals009() {
    final XQuery query = new XQuery(
      "\"test'",
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
   *  Test for valid integer literal .
   */
  @org.junit.Test
  public void literals010() {
    final XQuery query = new XQuery(
      "65535032",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535032")
    );
  }

  /**
   *  Test for valid integer literal .
   */
  @org.junit.Test
  public void literals011() {
    final XQuery query = new XQuery(
      "-65535032",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-65535032")
    );
  }

  /**
   *  Test for valid decimal literal .
   */
  @org.junit.Test
  public void literals012() {
    final XQuery query = new XQuery(
      "65535032.0023",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "65535032.0023")
    );
  }

  /**
   *  Test for valid decimal literal .
   */
  @org.junit.Test
  public void literals013() {
    final XQuery query = new XQuery(
      ".65535032",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.65535032")
    );
  }

  /**
   *  Test for valid decimal literal .
   */
  @org.junit.Test
  public void literals014() {
    final XQuery query = new XQuery(
      "-.65535032",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0.65535032")
    );
  }

  /**
   *  Test for valid decimal literal .
   */
  @org.junit.Test
  public void literals015() {
    final XQuery query = new XQuery(
      "+.65535032",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.65535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals016() {
    final XQuery query = new XQuery(
      "65535032e2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6.5535032E9")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals017() {
    final XQuery query = new XQuery(
      "65535.032e2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6.5535032E6")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals018() {
    final XQuery query = new XQuery(
      ".65535032e2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "65.535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals019() {
    final XQuery query = new XQuery(
      "-.65535032e2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-65.535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals020() {
    final XQuery query = new XQuery(
      "+.65535032e2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "65.535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals021() {
    final XQuery query = new XQuery(
      "65535.032e-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "655.35032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals022() {
    final XQuery query = new XQuery(
      ".65535032e-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.0065535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals023() {
    final XQuery query = new XQuery(
      "-.65535032e-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0.0065535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals024() {
    final XQuery query = new XQuery(
      "+.65535032e-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.0065535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals025() {
    final XQuery query = new XQuery(
      "-65535.032e2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-6.5535032E6")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals026() {
    final XQuery query = new XQuery(
      "-65535.032e-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-655.35032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals027() {
    final XQuery query = new XQuery(
      "65535032E2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6.5535032E9")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals028() {
    final XQuery query = new XQuery(
      "65535.032E2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6.5535032E6")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals029() {
    final XQuery query = new XQuery(
      ".65535032E2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "65.535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals030() {
    final XQuery query = new XQuery(
      "-.65535032E2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-65.535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals031() {
    final XQuery query = new XQuery(
      "+.65535032E2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "65.535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals032() {
    final XQuery query = new XQuery(
      "65535.032E-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "655.35032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals033() {
    final XQuery query = new XQuery(
      ".65535032E-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.0065535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals034() {
    final XQuery query = new XQuery(
      "-.65535032E-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0.0065535032")
    );
  }

  /**
   *  Test for valid double literal .
   */
  @org.junit.Test
  public void literals035() {
    final XQuery query = new XQuery(
      "+.65535032E-2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.0065535032")
    );
  }

  /**
   *  Test for invalid decimal literal .
   */
  @org.junit.Test
  public void literals036() {
    final XQuery query = new XQuery(
      "65535032.001.01",
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
   *  Test for invalid decimal literal .
   */
  @org.junit.Test
  public void literals037() {
    final XQuery query = new XQuery(
      "..01",
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
   *  Test for invalid decimal literal .
   */
  @org.junit.Test
  public void literals038() {
    final XQuery query = new XQuery(
      ".0.1",
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
   *  Test for invalid decimal literal .
   */
  @org.junit.Test
  public void literals039() {
    final XQuery query = new XQuery(
      "-.0.1",
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
   *  Test for invalid decimal literal .
   */
  @org.junit.Test
  public void literals040() {
    final XQuery query = new XQuery(
      "+.0.1",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals041() {
    final XQuery query = new XQuery(
      "1e 2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals042() {
    final XQuery query = new XQuery(
      "1E 2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals043() {
    final XQuery query = new XQuery(
      "1 e2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals044() {
    final XQuery query = new XQuery(
      "1 E2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals045() {
    final XQuery query = new XQuery(
      "1 e 2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals046() {
    final XQuery query = new XQuery(
      "1 E 2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals047() {
    final XQuery query = new XQuery(
      "1e2.1.1",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals048() {
    final XQuery query = new XQuery(
      "1E2.1.1",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals049() {
    final XQuery query = new XQuery(
      "1.1.1e2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals050() {
    final XQuery query = new XQuery(
      "1.1.1.E2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals051() {
    final XQuery query = new XQuery(
      "1ee2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals052() {
    final XQuery query = new XQuery(
      "1EE2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals053() {
    final XQuery query = new XQuery(
      "1eE2",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals054() {
    final XQuery query = new XQuery(
      "1e2e3",
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
   *  Test for invalid double literal .
   */
  @org.junit.Test
  public void literals055() {
    final XQuery query = new XQuery(
      "1e-2.1.1",
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
   *  Test for string literal containing the predefined entity reference '&amp;' (XQuery-only).
   */
  @org.junit.Test
  public void literals056() {
    final XQuery query = new XQuery(
      "\"&amp;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "&")
    );
  }

  /**
   *  Test for string literal containing the predefined entity reference '&quot;' (XQuery-only).
   */
  @org.junit.Test
  public void literals057() {
    final XQuery query = new XQuery(
      "\"&quot;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "\"")
    );
  }

  /**
   *  Test for string literal containing the predefined entity reference '&apos;' (XQuery-only).
   */
  @org.junit.Test
  public void literals058() {
    final XQuery query = new XQuery(
      "\"&apos;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "'")
    );
  }

  /**
   *  Test for string literal containing the predefined entity reference '&lt;' (XQuery-only).
   */
  @org.junit.Test
  public void literals059() {
    final XQuery query = new XQuery(
      "\"&lt;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "<")
    );
  }

  /**
   *  Test for string literal containing the predefined entity reference '&gt;' (XQuery-only).
   */
  @org.junit.Test
  public void literals060() {
    final XQuery query = new XQuery(
      "\"&gt;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, ">")
    );
  }

  /**
   *  Test for string literal containing the character reference '&#8364;' which transaltes into the 'Euro' currency symbol (XQuery-only).
   */
  @org.junit.Test
  public void literals061() {
    final XQuery query = new XQuery(
      "\"&#8364;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "€")
    );
  }

  /**
   *  Test the escaping of the " (quotation) character in XPath/XQuery. .
   */
  @org.junit.Test
  public void literals062() {
    final XQuery query = new XQuery(
      "\"\"\"\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "\"")
    );
  }

  /**
   *  Test the escaping of the ' (quotation) character in XPath/XQuery. .
   */
  @org.junit.Test
  public void literals063() {
    final XQuery query = new XQuery(
      "''''",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "'")
    );
  }

  /**
   *  Test the escaping of the ' (apostrophe) and " (quotation) characters in XPath/XQuery .
   */
  @org.junit.Test
  public void literals064() {
    final XQuery query = new XQuery(
      "\"He said, \"\"I don't like it.\"\"\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "He said, \"I don't like it.\"")
    );
  }

  /**
   *  Test the escaping of the ' (apostrophe) and " (quotation) characters in XPath/XQuery .
   */
  @org.junit.Test
  public void literals065() {
    final XQuery query = new XQuery(
      "'He said, \"I don''t like it.\"'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "He said, \"I don't like it.\"")
    );
  }

  /**
   *  Test the escaping of the ' (apostrophe) and " (quotation) characters as part of an XML element constructor .
   */
  @org.junit.Test
  public void literals066() {
    final XQuery query = new XQuery(
      "<test>{ 'He said, \"I don''t like it.\"' }</test>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<test>He said, \"I don't like it.\"</test>", false)
    );
  }

  /**
   *  Test the escaping of the ' (apostrophe) and " (quotation) characters as part of an XML attribute constructor. 
   *         Notice that the &quot; (quote) characters need to be entitized in the attribute content for XML validity .
   */
  @org.junit.Test
  public void literals067() {
    final XQuery query = new XQuery(
      "<test check='He said, \"I don''t like it.\"' />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<test check=\"He said, &quot;I don't like it.&quot;\"/>", false)
    );
  }

  /**
   *  Test the escaping of the &apos; (apostrophe) and &quot; (quotation) characters as part of an XML text node constructor .
   */
  @org.junit.Test
  public void literals068() {
    final XQuery query = new XQuery(
      "text{ 'He said, \"I don''t like it.\"' }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "He said, \"I don't like it.\"")
    );
  }

  /**
   *  Test the escaping of the &apos; (apostrophe) and &quot; (quotation) characters as part of an XML text node constructor .
   */
  @org.junit.Test
  public void literals069() {
    final XQuery query = new XQuery(
      "text{ \"He said, \"\"I don't like it.\"\"\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "He said, \"I don't like it.\"")
    );
  }

  /**
   *  test invalid character in hex character reference .
   */
  @org.junit.Test
  public void cbclLiterals001() {
    final XQuery query = new XQuery(
      "'&#x100000000x'",
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
   *  test missing ; in hex character reference .
   */
  @org.junit.Test
  public void cbclLiterals002() {
    final XQuery query = new XQuery(
      "'&#x100000000'",
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
   *  test missing digits in hex character reference .
   */
  @org.junit.Test
  public void cbclLiterals003() {
    final XQuery query = new XQuery(
      "'&#x;'",
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
   *  test invalid hex character reference .
   */
  @org.junit.Test
  public void cbclLiterals004() {
    final XQuery query = new XQuery(
      "'&#x0;'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }

  /**
   *  test invalid character in decimal character reference .
   */
  @org.junit.Test
  public void cbclLiterals005() {
    final XQuery query = new XQuery(
      "'&#100000000x'",
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
   *  test missing ; in decimal character reference .
   */
  @org.junit.Test
  public void cbclLiterals006() {
    final XQuery query = new XQuery(
      "'&#100000000'",
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
   *  test missing digits in decimal character reference .
   */
  @org.junit.Test
  public void cbclLiterals007() {
    final XQuery query = new XQuery(
      "'&#;'",
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
   *  test invalid decimal character reference .
   */
  @org.junit.Test
  public void cbclLiterals008() {
    final XQuery query = new XQuery(
      "'&#x0;'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }
}
