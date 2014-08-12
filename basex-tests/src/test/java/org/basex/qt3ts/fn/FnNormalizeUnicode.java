package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the normalize-unicode() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNormalizeUnicode extends QT3TestSet {

  /**
   *  A test whose essence is: `normalize-unicode()`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc1() {
    final XQuery query = new XQuery(
      "normalize-unicode()",
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
   *  A test whose essence is: `normalize-unicode("foo", "") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc10() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"\") eq \"foo\"",
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
   *  A test whose essence is: `normalize-unicode("f oo") eq "f oo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc11() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"f oo\") eq \"f oo\"",
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
   *  A test whose essence is: `normalize-unicode("f oo", "NFC") eq "f oo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc12() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"f oo\", \"NFC\") eq \"f oo\"",
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
   *  Static typing implementations may raise XPTY0004. See http://www.w3.org/Bugs/Public/show_bug.cgi?id=4551 for details. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc13() {
    final XQuery query = new XQuery(
      "normalize-unicode((\"a string\", error()), \"NFC\")",
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
        error("FOER0000")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("a string", "NFC", "wrong param")`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc2() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"a string\", \"NFC\", \"wrong param\")",
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
   *  A test whose essence is: `normalize-unicode("a string", "example.com/notSupported/")`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc3() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"a string\", \"example.com/notSupported/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0003")
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("foo") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc4() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\") eq \"foo\"",
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
   *  A test whose essence is: `normalize-unicode("foo", "NFC") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc5() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"NFC\") eq \"foo\"",
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
   *  A test whose essence is: `normalize-unicode("foo", "NFD") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc6() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"NFD\") eq \"foo\"",
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
   *  A test whose essence is: `normalize-unicode("foo", "NFKD") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc7() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"NFKD\") eq \"foo\"",
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
   *  A test whose essence is: `normalize-unicode("foo", "NFKC") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc8() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"NFKC\") eq \"foo\"",
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
   *  A test whose essence is: `normalize-unicode("f oo", "") eq "f oo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc9() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"f oo\", \"\") eq \"f oo\"",
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
   *  Tests boolean(normalize-unicode(...)) .
   */
  @org.junit.Test
  public void cbclFnNormalizeUnicode002() {
    final XQuery query = new XQuery(
      "boolean(normalize-unicode(\"blah\",\"NFC\"))",
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
   *  Tests boolean(normalize-unicode(....)) with an error thrown .
   */
  @org.junit.Test
  public void cbclFnNormalizeUnicode003() {
    final XQuery query = new XQuery(
      "boolean(normalize-unicode(\"blah\",\"ZZZ\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0003")
    );
  }

  /**
   *  tests normalize-unicode on empty .
   */
  @org.junit.Test
  public void cbclFnNormalizeUnicode004() {
    final XQuery query = new XQuery(
      "normalize-unicode((),\"NFC\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  Tests normalize-unicode on empty .
   */
  @org.junit.Test
  public void cbclFnNormalizeUnicode005() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"\",\"NFC\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode to ensure that " NFC " is the same as "NFC". .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1() {
    final XQuery query = new XQuery(
      "fn:concat(fn:normalize-unicode(\"Å\",\" NFC \"),fn:normalize-unicode(\"Å\",\"NFC\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ÅÅ")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with noncharacters in argument .
   */
  @org.junit.Test
  public void fnNormalizeUnicode10() {
    final XQuery query = new XQuery(
      "string-to-codepoints(fn:normalize-unicode('e﷐̂'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("101, 64976, 770")
    );
  }

  /**
   *  Bug 7935: normalize-unicode() applied to unassigned codepoints 
   *         Result should be empty, indicating that normalization is idempotent on these 
   *         characters derived from the Unicode 5.2 database - essentially a list of characters 
   *         that are legal in XML but undefined in Unicode 5.2 .
   */
  @org.junit.Test
  public void fnNormalizeUnicode11() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "fn/normalize-unicode/fn-normalize-unicode-11.xq"
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
      assertSerialization("<out/>", false)
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with a normalization form that attempts to raise a non-implemented form .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2() {
    final XQuery query = new XQuery(
      "fn:normalize-unicode(\"è\",\"chancesareyoudonotsupportthis123ifyoudowaoo\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0003")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:upper-case .
   */
  @org.junit.Test
  public void fnNormalizeUnicode3() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:normalize-unicode(\"normalizedstring\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NORMALIZEDSTRING")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:lower-case .
   */
  @org.junit.Test
  public void fnNormalizeUnicode4() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:normalize-unicode(\"NORMALIZEDSTRING\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "normalizedstring")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:string-length .
   */
  @org.junit.Test
  public void fnNormalizeUnicode5() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:normalize-unicode(\"NORMALIZEDSTRING\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "16")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:concat .
   */
  @org.junit.Test
  public void fnNormalizeUnicode6() {
    final XQuery query = new XQuery(
      "fn:concat(fn:normalize-unicode(\"NORMALIZEDSTRING\"),\"another string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NORMALIZEDSTRINGanother string")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:substring .
   */
  @org.junit.Test
  public void fnNormalizeUnicode7() {
    final XQuery query = new XQuery(
      "fn:substring(fn:normalize-unicode(\"NORMALIZEDSTRING\"),5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ALIZEDSTRING")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with noncharacter argument .
   */
  @org.junit.Test
  public void fnNormalizeUnicode8() {
    final XQuery query = new XQuery(
      "string-to-codepoints(normalize-unicode('﷐'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "64976")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with noncharacters in argument .
   */
  @org.junit.Test
  public void fnNormalizeUnicode9() {
    final XQuery query = new XQuery(
      "string-to-codepoints(fn:normalize-unicode('ê﷐ê﷐ê'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("234, 64976, 234, 64976, 234")
    );
  }

  /**
   * Test normalize-unicode with simple text input .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args1() {
    final XQuery query = new XQuery(
      "normalize-unicode('Nothing to normalize.')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Nothing to normalize.")
    );
  }

  /**
   * Test normalize-unicode with empty sequence argument .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args2() {
    final XQuery query = new XQuery(
      "normalize-unicode(())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test fn:normalize-unicode on combining characters for LATIN CAPITAL LETTER A WITH RING (w/ ACUTE) and ANGSTROM SIGN .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args3() {
    final XQuery query = new XQuery(
      "matches('Ǻ', normalize-unicode('Ǻ'))",
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
   * Test fn:normalize-unicode on combining characters for LATIN CAPITAL LETTER A WITH RING (w/ ACUTE) and ANGSTROM SIGN .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args4() {
    final XQuery query = new XQuery(
      "matches('Å', normalize-unicode('Å'))",
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
   * Test fn:normalize-unicode on combining characters for LATIN CAPITAL LETTER A WITH RING (w/ ACUTE) and ANGSTROM SIGN .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args5() {
    final XQuery query = new XQuery(
      "matches('Å', normalize-unicode('Å'))",
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
   * Test fn:normalize-unicode on combining characters for LATIN CAPITAL LETTER A WITH RING (w/ ACUTE) and ANGSTROM SIGN .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args6() {
    final XQuery query = new XQuery(
      "(normalize-unicode('Å') eq normalize-unicode('Å'))",
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
   * Test normalize-unicode with invalid argument types .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args7() {
    final XQuery query = new XQuery(
      "normalize-unicode(12)",
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
   * Test normalize-unicode with incorrect arity .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args8() {
    final XQuery query = new XQuery(
      "normalize-unicode('','','')",
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
   * Test normalize-unicode with simple text input and NFC .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args1() {
    final XQuery query = new XQuery(
      "normalize-unicode('Nothing to normalize.', 'NFC')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Nothing to normalize.")
    );
  }

  /**
   * Test normalize-unicode with simple text input and NFC spelled differently .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args2() {
    final XQuery query = new XQuery(
      "normalize-unicode('Nothing to normalize.', 'nFc')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Nothing to normalize.")
    );
  }

  /**
   * Test normalize-unicode with empty sequence argument and NFC .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args3() {
    final XQuery query = new XQuery(
      "normalize-unicode((), 'NFC')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test fn:normalize-unicode where the second argument is the zero-length string, no normalization is performed .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args4() {
    final XQuery query = new XQuery(
      "(normalize-unicode('Å', '') eq normalize-unicode('Å', ''))",
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
   * Test normalize-unicode with invalid argument types .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args5() {
    final XQuery query = new XQuery(
      "normalize-unicode('',())",
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
   * Test normalize-unicode with invalid argument types .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args6() {
    final XQuery query = new XQuery(
      "normalize-unicode('',12)",
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
}
