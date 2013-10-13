package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Test the fn:matches() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMatches extends QT3TestSet {

  /**
   *  The pattern can't be the empty sequence. .
   */
  @org.junit.Test
  public void kMatchesFunc1() {
    final XQuery query = new XQuery(
      "matches(\"input\", ())",
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
   *  fn:matches() takes at least two arguments, not one. .
   */
  @org.junit.Test
  public void kMatchesFunc2() {
    final XQuery query = new XQuery(
      "matches(\"input\")",
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
   *  The third argument cannot be the empty sequence. .
   */
  @org.junit.Test
  public void kMatchesFunc3() {
    final XQuery query = new XQuery(
      "matches(\"input\", \"pattern\", ())",
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
   *  Only three arguments are accepted. .
   */
  @org.junit.Test
  public void kMatchesFunc4() {
    final XQuery query = new XQuery(
      "matches(\"input\", \"pattern\", \"\", ())",
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
   *  The flags argument cannot contain whitespace. .
   */
  @org.junit.Test
  public void kMatchesFunc5() {
    final XQuery query = new XQuery(
      "matches(\"input\", \"pattern\", \" \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0001")
    );
  }

  /**
   *  The flags argument cannot contain 'X'. .
   */
  @org.junit.Test
  public void kMatchesFunc6() {
    final XQuery query = new XQuery(
      "matches(\"input\", \"pattern\", \"X\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0001")
    );
  }

  /**
   *  Whitespace in the regexp is collapsed. .
   */
  @org.junit.Test
  public void k2MatchesFunc1() {
    final XQuery query = new XQuery(
      "fn:matches(\"hello world\", \"hello\\ sworld\", \"x\")",
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
   *  A non-matching backwards-reference matches the empty string. .
   */
  @org.junit.Test
  public void k2MatchesFunc10() {
    final XQuery query = new XQuery(
      "matches(\"input\", \"\\3\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use a back reference inside a character class. .
   */
  @org.junit.Test
  public void k2MatchesFunc11() {
    final XQuery query = new XQuery(
      "matches(\"abcd\", \"(asd)[\\1]\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use a back reference inside a character class(#2). .
   */
  @org.junit.Test
  public void k2MatchesFunc12() {
    final XQuery query = new XQuery(
      "matches(\"abcd\", \"(asd)[asd\\1]\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use a back reference inside a character class(#3). .
   */
  @org.junit.Test
  public void k2MatchesFunc13() {
    final XQuery query = new XQuery(
      "matches(\"abcd\", \"(asd)[asd\\0]\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Use a back reference inside a character class(#3). .
   */
  @org.junit.Test
  public void k2MatchesFunc14() {
    final XQuery query = new XQuery(
      "matches(\"abcd\", \"1[asd\\0]\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  A negative character class never match a non-character. .
   */
  @org.junit.Test
  public void k2MatchesFunc15() {
    final XQuery query = new XQuery(
      "fn:matches(\"a\", \"a[^b]\"), fn:matches(\"a \", \"a[^b]\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("false(), true()")
    );
  }

  /**
   *  Use a pattern whose interpretation is unknown. See public report 4466. .
   */
  @org.junit.Test
  public void k2MatchesFunc16() {
    final XQuery query = new XQuery(
      "fn:matches(\"input\", \"[0-9-.]*/\")",
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
        assertStringValue(false, "false")
      ||
        error("FORG0001")
      )
    );
  }

  /**
   *  Caseless match with back-reference. .
   */
  @org.junit.Test
  public void k2MatchesFunc17() {
    final XQuery query = new XQuery(
      "matches('aA', '(a)\\1', 'i')",
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
   *  Whitespace(before) in the regexp is collapsed, but not inside a character class. .
   */
  @org.junit.Test
  public void k2MatchesFunc2() {
    final XQuery query = new XQuery(
      "fn:matches(\"hello world\", \" hello[ ]world\", \"x\")",
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
   *  Whitespace(after) in the regexp is collapsed, but not inside a character class. .
   */
  @org.junit.Test
  public void k2MatchesFunc3() {
    final XQuery query = new XQuery(
      "fn:matches(\"hello world\", \"hello[ ]world \", \"x\")",
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
   *  Whitespace(in the middle) in the regexp is collapsed, but not inside a character class. .
   */
  @org.junit.Test
  public void k2MatchesFunc4() {
    final XQuery query = new XQuery(
      "fn:matches(\"hello world\", \"he ll o[ ]worl d\", \"x\")",
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
   *  whitespace in the regexp is collapsed, and should therefore compile. .
   */
  @org.junit.Test
  public void k2MatchesFunc5() {
    final XQuery query = new XQuery(
      "fn:matches(\"hello world\", \"\\p{ IsBasicLatin}+\", \"x\")",
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
   *  whitespace in the regexp is collapsed completely, and should therefore compile and match. .
   */
  @org.junit.Test
  public void k2MatchesFunc6() {
    final XQuery query = new XQuery(
      "fn:matches(\"hello world\", \"\\p{ I s B a s i c L a t i n }+\", \"x\")",
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
   *  whitespace in the regexp is not collapsed, and should therefore not compile. .
   */
  @org.junit.Test
  public void k2MatchesFunc7() {
    final XQuery query = new XQuery(
      "fn:matches(\"hello world\", \"\\p{ IsBasicLatin}+\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Since no string is captured by the back-reference, the single character is matched. .
   */
  @org.junit.Test
  public void k2MatchesFunc8() {
    final XQuery query = new XQuery(
      "fn:matches(\"h\", \"(.)\\3\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Since no string is captured by the back-reference, the single character is matched(#2). .
   */
  @org.junit.Test
  public void k2MatchesFunc9() {
    final XQuery query = new XQuery(
      "fn:matches(\"h\", \"(.)\\2\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Simple call of matches() with "i" flag .
   */
  @org.junit.Test
  public void caselessmatch01() {
    final XQuery query = new XQuery(
      "matches('abc', 'ABC', 'i')",
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
   *  Call of matches() with "i" flag and a character range .
   */
  @org.junit.Test
  public void caselessmatch02() {
    final XQuery query = new XQuery(
      "matches('abZ', '[A-Z]*', 'i')",
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
   *  Call of matches() with "i" flag and a character range .
   */
  @org.junit.Test
  public void caselessmatch03() {
    final XQuery query = new XQuery(
      "matches('abZ', '[a-z]*', 'i')",
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
   *  Call of matches() with "i" flag and Kelvin sign Kelvin sign .
   */
  @org.junit.Test
  public void caselessmatch04() {
    final XQuery query = new XQuery(
      "matches(codepoints-to-string(8490), '[A-Z]', 'i')",
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
   *  Call of matches() with "i" flag and Kelvin sign Kelvin sign .
   */
  @org.junit.Test
  public void caselessmatch05() {
    final XQuery query = new XQuery(
      "matches(codepoints-to-string(8490), '[a-z]', 'i')",
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
   *  Call of matches() with "i" flag and Kelvin sign Kelvin sign .
   */
  @org.junit.Test
  public void caselessmatch06() {
    final XQuery query = new XQuery(
      "matches(codepoints-to-string(8490), 'K', 'i')",
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
   *  Call of matches() with "i" flag and Kelvin sign Kelvin sign .
   */
  @org.junit.Test
  public void caselessmatch07() {
    final XQuery query = new XQuery(
      "matches(codepoints-to-string(8490), 'k', 'i')",
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
   *  Call of matches() with "i" flag and range subtraction .
   */
  @org.junit.Test
  public void caselessmatch08() {
    final XQuery query = new XQuery(
      "matches('x', '[A-Z-[OI]]', 'i')",
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
   *  Call of matches() with "i" flag and range subtraction .
   */
  @org.junit.Test
  public void caselessmatch09() {
    final XQuery query = new XQuery(
      "matches('X', '[A-Z-[OI]]', 'i')",
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
   *  Call of matches() with "i" flag and range subtraction .
   */
  @org.junit.Test
  public void caselessmatch10() {
    final XQuery query = new XQuery(
      "matches('O', '[A-Z-[OI]]', 'i')",
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
   *  Call of matches() with "i" flag and range subtraction .
   */
  @org.junit.Test
  public void caselessmatch11() {
    final XQuery query = new XQuery(
      "matches('i', '[A-Z-[OI]]', 'i')",
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
   *  Call of matches() with "i" flag and negation .
   */
  @org.junit.Test
  public void caselessmatch12() {
    final XQuery query = new XQuery(
      "matches('Q', '[^Q]', 'i')",
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
   *  Call of matches() with "i" flag and negation .
   */
  @org.junit.Test
  public void caselessmatch13() {
    final XQuery query = new XQuery(
      "matches('q', '[^Q]', 'i')",
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
   *  Call of matches() with "i" flag and upper-case category .
   */
  @org.junit.Test
  public void caselessmatch14() {
    final XQuery query = new XQuery(
      "matches('m', '\\p{Lu}', 'i')",
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
   *  Call of matches() with "i" flag and upper-case category .
   */
  @org.junit.Test
  public void caselessmatch15() {
    final XQuery query = new XQuery(
      "matches('m', '\\P{Lu}', 'i')",
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
   *  test an invalid negative pos char group .
   */
  @org.junit.Test
  public void cbclMatches001() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '[^]')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test an invalid char range .
   */
  @org.junit.Test
  public void cbclMatches002() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '[a-\\b]')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test a two-digit back reference .
   */
  @org.junit.Test
  public void cbclMatches003() {
    final XQuery query = new XQuery(
      "fn:matches('abcdefghijkabcdefghijk', '(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)(k)\\1\\2\\3\\4\\5\\6\\7\\8\\9\\10\\11')",
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
   *  test a very large exact quantifier .
   */
  @org.junit.Test
  public void cbclMatches004() {
    final XQuery query = new XQuery(
      "fn:matches('aaa', 'a{99999999999999999999999999}')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test with an invalid character range .
   */
  @org.junit.Test
  public void cbclMatches005() {
    final XQuery query = new XQuery(
      "fn:matches('a', '[a--]')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test with a character class containing an escaped character .
   */
  @org.junit.Test
  public void cbclMatches006() {
    final XQuery query = new XQuery(
      "fn:matches('&#x9;', '[\\t]')",
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
   *  test with a character class beginning with a '-' .
   */
  @org.junit.Test
  public void cbclMatches007() {
    final XQuery query = new XQuery(
      "fn:matches('-abba-', '[-ab]+')",
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
   *  test a badly formed category name .
   */
  @org.junit.Test
  public void cbclMatches008() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{L')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test a badly formed category name .
   */
  @org.junit.Test
  public void cbclMatches009() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{M')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test a badly formed category name .
   */
  @org.junit.Test
  public void cbclMatches010() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{N')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test a badly formed category name .
   */
  @org.junit.Test
  public void cbclMatches011() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{P')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test a badly formed category name .
   */
  @org.junit.Test
  public void cbclMatches012() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test a badly formed category name .
   */
  @org.junit.Test
  public void cbclMatches013() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{S')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test a badly formed category name .
   */
  @org.junit.Test
  public void cbclMatches014() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{C')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test category name L .
   */
  @org.junit.Test
  public void cbclMatches015() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{L}')",
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
   *  test category name M .
   */
  @org.junit.Test
  public void cbclMatches016() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{M}')",
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
   *  test category name N .
   */
  @org.junit.Test
  public void cbclMatches017() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{N}')",
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
   *  test category name P .
   */
  @org.junit.Test
  public void cbclMatches018() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{P}')",
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
   *  test category name Z .
   */
  @org.junit.Test
  public void cbclMatches019() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Z}')",
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
   *  test category name S .
   */
  @org.junit.Test
  public void cbclMatches020() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{S}')",
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
   *  test category name C .
   */
  @org.junit.Test
  public void cbclMatches021() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{C}')",
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
   *  test category name Lu .
   */
  @org.junit.Test
  public void cbclMatches022() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Lu}')",
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
   *  test category name Me .
   */
  @org.junit.Test
  public void cbclMatches023() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Me}')",
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
   *  test category name No .
   */
  @org.junit.Test
  public void cbclMatches024() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{No}')",
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
   *  test category name Pf .
   */
  @org.junit.Test
  public void cbclMatches025() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Pf}')",
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
   *  test category name Zs .
   */
  @org.junit.Test
  public void cbclMatches026() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Zs}')",
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
   *  test category name Sk .
   */
  @org.junit.Test
  public void cbclMatches027() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Sk}')",
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
   *  test category name Cc .
   */
  @org.junit.Test
  public void cbclMatches028() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Cc}')",
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
   *  test invalid category name La .
   */
  @org.junit.Test
  public void cbclMatches029() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{La}')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test invalid category name Ma .
   */
  @org.junit.Test
  public void cbclMatches030() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Ma}')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test invalid category name Na .
   */
  @org.junit.Test
  public void cbclMatches031() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Na}')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test invalid category name Pa .
   */
  @org.junit.Test
  public void cbclMatches032() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Pa}')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test invalid category name Za .
   */
  @org.junit.Test
  public void cbclMatches033() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Za}')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test invalid category name Sa .
   */
  @org.junit.Test
  public void cbclMatches034() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Sa}')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test invalid category name Ca .
   */
  @org.junit.Test
  public void cbclMatches035() {
    final XQuery query = new XQuery(
      "fn:matches('foo', '\\P{Ca}')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  test an empty branch .
   */
  @org.junit.Test
  public void cbclMatches036() {
    final XQuery query = new XQuery(
      "fn:matches('foo', 'a()b')",
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
   *  test a multibyte Unicode character .
   */
  @org.junit.Test
  public void cbclMatches037() {
    final XQuery query = new XQuery(
      "fn:matches('&#x10000;', '&#x10000;')",
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
   *  test a large exact quantifier .
   */
  @org.junit.Test
  public void cbclMatches038() {
    final XQuery query = new XQuery(
      "fn:matches('aaa', 'a{2147483647}')",
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
   *  test a two-digit back reference .
   */
  @org.junit.Test
  public void cbclMatches039() {
    final XQuery query = new XQuery(
      "fn:matches('abcdefghiabcdefghia0a1', '(a)(b)(c)(d)(e)(f)(g)(h)(i)\\1\\2\\3\\4\\5\\6\\7\\8\\9\\10\\11')",
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
   *  test the multi-character escape \S .
   */
  @org.junit.Test
  public void cbclMatches040() {
    final XQuery query = new XQuery(
      "fn:matches('abc', '\\S+')",
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
   *  test the multi-character escape \S .
   */
  @org.junit.Test
  public void cbclMatches041() {
    final XQuery query = new XQuery(
      "fn:matches('&#xD;&#x20;&#x9;', '\\S+')",
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
   *  test the multi-character escape \i .
   */
  @org.junit.Test
  public void cbclMatches042() {
    final XQuery query = new XQuery(
      "fn:matches('a_:', '\\i+')",
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
   *  test the multi-character escape \i .
   */
  @org.junit.Test
  public void cbclMatches043() {
    final XQuery query = new XQuery(
      "fn:matches('1.0', '\\i+')",
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
   *  test the multi-character escape \I .
   */
  @org.junit.Test
  public void cbclMatches044() {
    final XQuery query = new XQuery(
      "fn:matches('1.0', '\\I+')",
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
   *  test the multi-character escape \I .
   */
  @org.junit.Test
  public void cbclMatches045() {
    final XQuery query = new XQuery(
      "fn:matches('a_:', '\\I+')",
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
   *  test the multi-character escape \c .
   */
  @org.junit.Test
  public void cbclMatches046() {
    final XQuery query = new XQuery(
      "fn:matches('abc', '\\c+')",
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
   *  test the multi-character escape \c .
   */
  @org.junit.Test
  public void cbclMatches047() {
    final XQuery query = new XQuery(
      "fn:matches('&#x20;&#x9;&#xD;', '\\c+')",
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
   *  test the multi-character escape \C .
   */
  @org.junit.Test
  public void cbclMatches048() {
    final XQuery query = new XQuery(
      "fn:matches('&#x20;&#x9;&#xD;', '\\C+')",
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
   *  test the multi-character escape \C .
   */
  @org.junit.Test
  public void cbclMatches049() {
    final XQuery query = new XQuery(
      "fn:matches('abc', '\\C+')",
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
   *  A back-reference is compared using case-blind comparison: that is, each character must either be the same as the corresponding character of the previously matched string, or must be a case-variant of that character. the back reference. For example, the strings "Mum", "mom", "Dad", and "DUD" all match the regular expression "([md])[aeiou]\1" when the "i" flag is used. .
   */
  @org.junit.Test
  public void cbclMatches050() {
    final XQuery query = new XQuery(
      "fn:matches('Mum', '([md])[aeiou]\\1', 'i')",
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
   *  Test back-reference to character above &#FFFF; .
   */
  @org.junit.Test
  public void cbclMatches051() {
    final XQuery query = new XQuery(
      "fn:matches('&#x10000;&#x10000;', '(&#x10000;)\\1')",
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
   *  Test back-reference to character above &#FFFF; .
   */
  @org.junit.Test
  public void cbclMatches052() {
    final XQuery query = new XQuery(
      "fn:matches('&#x10000;&#x10001;', '(&#x10000;)\\1')",
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
   *  A back-reference is compared using case-blind comparison: that is, each character must either be the same as the corresponding character of the previously matched string, or must be a case-variant of that character. the back reference. For example, the strings "Mum", "mom", "Dad", and "DUD" all match the regular expression "([md])[aeiou]\1" when the "i" flag is used. .
   */
  @org.junit.Test
  public void cbclMatches053() {
    final XQuery query = new XQuery(
      "fn:matches('Mud', '([md])[aeiou]\\1', 'i')",
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
   *  Evaluation of matches function as per example 1 (for this function) .
   */
  @org.junit.Test
  public void fnMatches1() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\", \"bra\")",
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
   *  Evaluation of matches function with pattern set to "\{" for an input string that contains "}". .
   */
  @org.junit.Test
  public void fnMatches10() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra{abracadabra\", \"\\{\")",
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
   *  Evaluation of matches function with pattern set to "\}" for an input string that contains "}". .
   */
  @org.junit.Test
  public void fnMatches11() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra}abracadabra\", \"\\}\")",
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
   *  Evaluation of matches function with pattern set to "\(" for an input string that contains "(". .
   */
  @org.junit.Test
  public void fnMatches12() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra(abracadabra\", \"\\(\")",
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
   *  Evaluation of matches function with pattern set to "\)" for an input string that contains ")". .
   */
  @org.junit.Test
  public void fnMatches13() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra)abracadabra\", \"\\)\")",
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
   *  Evaluation of matches function with pattern set to "\[" for an input string that contains "[". .
   */
  @org.junit.Test
  public void fnMatches14() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra[abracadabra\", \"\\[\")",
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
   *  Evaluation of matches function with pattern set to "\]" for an input string that contains "]". .
   */
  @org.junit.Test
  public void fnMatches15() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra]abracadabra\", \"\\]\")",
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
   *  Evaluation of matches function with pattern set to "\-" for an input string that contains "-". .
   */
  @org.junit.Test
  public void fnMatches16() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra-abracadabra\", \"\\-\")",
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
   *  Evaluation of matches function with pattern set to "\." for an input string that contains ".". .
   */
  @org.junit.Test
  public void fnMatches17() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra.abracadabra\", \"\\.\")",
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
   *  Evaluation of matches function with pattern set to "\|" for an input string that contains "|". .
   */
  @org.junit.Test
  public void fnMatches18() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra|abracadabra\", \"\\|\")",
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
   *  Evaluation of matches function with pattern set to "\\" for an input string that contains "\". .
   */
  @org.junit.Test
  public void fnMatches19() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\\abracadabra\", \"\\\\\")",
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
   *  Evaluation of matches function as per example 2 (for this function). Pattern set to "^a.*a$". .
   */
  @org.junit.Test
  public void fnMatches2() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\", \"^a.*a$\")",
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
   *  Evaluation of matches function with pattern set to "\t" for an input string that contains the tab character. .
   */
  @org.junit.Test
  public void fnMatches20() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\tabracadabra\", \"\\t\")",
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
   *  Evaluation of matches function with pattern set to "\n" for an input string that contains the newline character. .
   */
  @org.junit.Test
  public void fnMatches21() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\n" +
      "abracadabra\", \"\\n\")",
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
   *  Evaluation of matches function with pattern set to "aa{1}" (exact quantity) for an input string that contains the "aa" string. .
   */
  @org.junit.Test
  public void fnMatches22() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabraabracadabra\", \"aa{1}\")",
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
   *  Evaluation of matches function with pattern set to "aa{1,}" (min quantity) for an input string that contains the "aa" string twice. .
   */
  @org.junit.Test
  public void fnMatches23() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabraabracadabraabracadabra\", \"aa{1,}\")",
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
   *  Evaluation of matches function with pattern set to "aa{1,2}" (range quantity) for an input string that contains the "aa" string twice. .
   */
  @org.junit.Test
  public void fnMatches24() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabraabracadabraabracadabra\", \"aa{1,2}\")",
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
   *  Evaluation of matches function with invalid regular expression .
   */
  @org.junit.Test
  public void fnMatches25() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\", \"**%%\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Check for the correct behavior of ^ and $ in multi-line mode This test case was motivated by the resolution of Bug Report 4543.
   *       Note that '^' matches the position after any newline other than a newline that is the last character in the input string..
   */
  @org.junit.Test
  public void fnMatches26() {
    final XQuery query = new XQuery(
      "fn:matches(concat('abcd', codepoints-to-string(10), 'defg', codepoints-to-string(10)), \"^$\", \"m\")",
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
   *  Check for the correct behavior of ^ and $ in multi-line mode This test case was motivated by the resolution of Bug Report 4543 .
   */
  @org.junit.Test
  public void fnMatches27() {
    final XQuery query = new XQuery(
      "fn:matches(\"\n" +
      "abcd\n" +
      "defg\n" +
      "\", \"^$\", \"m\")",
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
   *  Check for the correct behavior of ^ and $ in multi-line mode This test case was motivated by the resolution of Bug Report 4543 .
   */
  @org.junit.Test
  public void fnMatches28() {
    final XQuery query = new XQuery(
      "fn:matches(\"abcd\n" +
      "\n" +
      "defg\n" +
      "\", \"^$\", \"m\")",
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
   *  2-digits not treated as a back-reference See erratum FO.E24 .
   */
  @org.junit.Test
  public void fnMatches29() {
    final XQuery query = new XQuery(
      "fn:matches(\"#abc#1\", \"^(#)abc\\11$\")",
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
   *  Evaluation of matches function as per example 3 (for this function). Pattern set to "^bra" .
   */
  @org.junit.Test
  public void fnMatches3() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\", \"^bra\")",
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
   *  2-digits treated as a back-reference See erratum FO.E24 .
   */
  @org.junit.Test
  public void fnMatches30() {
    final XQuery query = new XQuery(
      "fn:matches(\"#abcdefghijklmnopq#1\", \"^(#)(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)(k)(l)(m)(n)(o)(p)(q)\\11$\")",
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
   *  Evaluation of matches function with non-capturing groups (allowed in XPath 3.0) .
   */
  @org.junit.Test
  public void fnMatches31() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\", \"(?:abra(?:cad)?)*\")",
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
   *  Evaluation of matches function with "q" flag (allowed in XQuery 3.0) .
   */
  @org.junit.Test
  public void fnMatches32() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\", \"(?:abra(?:cad)?)*\", \"q\")",
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
   *  Evaluation of matches function with "q" flag (allowed in XQuery 3.0) .
   */
  @org.junit.Test
  public void fnMatches33() {
    final XQuery query = new XQuery(
      "fn:matches(\"x[y-z]\", \"x[y-z]\", \"q\")",
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
   *  Evaluation of matches function with "q" and "i" flags (allowed in XQuery 3.0) .
   */
  @org.junit.Test
  public void fnMatches34() {
    final XQuery query = new XQuery(
      "fn:matches(\"x[Y-z]\", \"X[y-Z]\", \"qi\")",
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
   *  Test for bug fix of 5348 in Errata for F+O. Expect FORX0002 err because \99 is an invalid reference as 99th subexpression does not exist .
   */
  @org.junit.Test
  public void fnMatches35() {
    final XQuery query = new XQuery(
      "fn:matches('aA', '(a)\\99')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Test for bug fix of 5348 in Errata for F+O. ok match here .
   */
  @org.junit.Test
  public void fnMatches36() {
    final XQuery query = new XQuery(
      "fn:matches('abcdefghijj', '(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)\\10')",
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
   *  Test for bug fix of 5348 in Errata for F+O. Expect FORX0002 err because \11 reference is made before the closing right parenthesis of 11th reference .
   */
  @org.junit.Test
  public void fnMatches37() {
    final XQuery query = new XQuery(
      "fn:matches('abcdefghijk', '(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)(k\\11)')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Test for bug fix of 5348 in Errata for F+O. Expect FORX0002 err because \10 reference is made before the closing right parenthesis of 10th reference .
   */
  @org.junit.Test
  public void fnMatches38() {
    final XQuery query = new XQuery(
      "fn:matches('abcdefghijj', '(a)(b)(c)(d)(e)(f)(g)(h)(i)(j\\10)')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Test for bug fix of 5348 in Errata for F+O. Expect FORX0002 err because \9 reference is made before the closing right parenthesis of 9th reference .
   */
  @org.junit.Test
  public void fnMatches39() {
    final XQuery query = new XQuery(
      "fn:matches('abcdefghii', '(a)(b)(c)(d)(e)(f)(g)(h)(i\\9)')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Test that calling the function with flags set to the empty string is the same as ommiting the flags. .
   */
  @org.junit.Test
  public void fnMatches4() {
    final XQuery query = new XQuery(
      "fn:concat(fn:matches(\"abracadabra\", \"^bra\"),fn:matches(\"abracadabra\", \"^bra\", \"\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "falsefalse")
    );
  }

  /**
   *  Test for bug fix of 5348 in Errata for F+O. Expect FORX0002 err because \1 reference is made before the closing right parenthesis of 1st reference .
   */
  @org.junit.Test
  public void fnMatches40() {
    final XQuery query = new XQuery(
      "fn:matches('aa', '(a\\1)')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  Handling of final newline with non-multiline mode .
   */
  @org.junit.Test
  public void fnMatches41() {
    final XQuery query = new XQuery(
      "fn:matches(concat('Mary', codepoints-to-string(10)), 'Mary$')",
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
   *  Handling of final newline with $ in dot-all mode .
   */
  @org.junit.Test
  public void fnMatches42() {
    final XQuery query = new XQuery(
      "fn:matches(concat('Mary', codepoints-to-string(10)), 'Mary$', 's')",
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
   *  "." doesn't normally match newline .
   */
  @org.junit.Test
  public void fnMatches43() {
    final XQuery query = new XQuery(
      "fn:matches(concat('Mary', codepoints-to-string(10), 'Jones'), 'Mary.Jones')",
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
   *  "." does match newline in dot-all mode.
   */
  @org.junit.Test
  public void fnMatches44() {
    final XQuery query = new XQuery(
      "fn:matches(concat('Mary', codepoints-to-string(10), 'Jones'), 'Mary.Jones', 's')",
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
   *  "." does NOT match CR in default mode.
   */
  @org.junit.Test
  public void fnMatches45() {
    final XQuery query = new XQuery(
      "fn:matches(concat('Mary', codepoints-to-string(13), 'Jones'), 'Mary.Jones')",
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
   *  "." does match CR in dot-all mode.
   */
  @org.junit.Test
  public void fnMatches46() {
    final XQuery query = new XQuery(
      "fn:matches(concat('Mary', codepoints-to-string(13), 'Jones'), 'Mary.Jones', 's')",
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
   *  Check for the correct behavior of $ when not in multi-line mode.
   *          The correct answer according to the spec is false; though some regex engines
   *          are known to report true..
   */
  @org.junit.Test
  public void fnMatches47() {
    final XQuery query = new XQuery(
      "fn:matches(concat('abcd', codepoints-to-string(10), 'defg', codepoints-to-string(10)), \"g$\")",
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
   *  Edge condition: match occurs at last character. .
   */
  @org.junit.Test
  public void fnMatches48() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra-abracadabra.\", \"\\.\")",
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
   *  Edge condition: match occurs at last character. .
   */
  @org.junit.Test
  public void fnMatches49() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra-abracadabra-3\", \"(124|864|377|3)\")",
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
   *  Evaluate the fn:mathes function with the input string set to the empty sequence. fn:count used to avoid empty file. .
   */
  @org.junit.Test
  public void fnMatches5() {
    final XQuery query = new XQuery(
      "fn:count(fn:matches(\"()\", \"^bra\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluation of matches function with pattern set to "\^". .
   */
  @org.junit.Test
  public void fnMatches6() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra^abracadabra\", \"\\^\")",
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
   *  Evaluation of matches function with pattern set to "\?" for an input string that contains "?". .
   */
  @org.junit.Test
  public void fnMatches7() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra?abracadabra\", \"\\?\")",
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
   *  Evaluation of matches function with pattern set to "\*" for an input string that contains "*". .
   */
  @org.junit.Test
  public void fnMatches8() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra*abracadabra\", \"\\*\")",
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
   *  Evaluation of matches function with pattern set to "\+" for an input string that contains "+". .
   */
  @org.junit.Test
  public void fnMatches9() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra+abracadabra\", \"\\+\")",
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
   *  Evaluates The "matches" function with the arguments set as follows: $input = xs:string(lower bound) $pattern = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnMatches2args1() {
    final XQuery query = new XQuery(
      "fn:matches(\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "matches" function with the arguments set as follows: $input = xs:string(mid range) $pattern = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnMatches2args2() {
    final XQuery query = new XQuery(
      "fn:matches(\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "matches" function with the arguments set as follows: $input = xs:string(upper bound) $pattern = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnMatches2args3() {
    final XQuery query = new XQuery(
      "fn:matches(\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "matches" function with the arguments set as follows: $input = xs:string(lower bound) $pattern = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnMatches2args4() {
    final XQuery query = new XQuery(
      "fn:matches(\"This is a characte\",\"This is a characte\")",
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
   *  Evaluates The "matches" function with the arguments set as follows: $input = xs:string(lower bound) $pattern = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnMatches2args5() {
    final XQuery query = new XQuery(
      "fn:matches(\"This is a characte\",\"This is a characte\")",
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
   *  Invalid flag for third argument of fn:matches. .
   */
  @org.junit.Test
  public void fnMatchesErr1() {
    final XQuery query = new XQuery(
      "fn:matches(\"abracadabra\", \"bra\", \"p\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0001")
    );
  }

  /**
   *  back-reference illegal in square brackets See erratum FO.E24 .
   */
  @org.junit.Test
  public void fnMatchesErr2() {
    final XQuery query = new XQuery(
      "fn:matches(\"#abc#1\", \"^(#)abc[\\1]1$\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  single-digit back-reference to non-existent group See erratum FO.E24 .
   */
  @org.junit.Test
  public void fnMatchesErr3() {
    final XQuery query = new XQuery(
      "fn:matches(\"#abc#1\", \"^(#)abc\\2$\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  single-digit back-reference to group not yet closed See erratum FO.E24 .
   */
  @org.junit.Test
  public void fnMatchesErr4() {
    final XQuery query = new XQuery(
      "fn:matches(\"#abc#1\", \"^((#)abc\\1)$\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }

  /**
   *  double-digit back-reference to group not yet closed See erratum FO.E24 .
   */
  @org.junit.Test
  public void fnMatchesErr5() {
    final XQuery query = new XQuery(
      "fn:matches(\"abcdefghijklmnopq\", \"(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)(k)(l)((m)(n)(o)(p)(q)\\13)$\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0002")
    );
  }
}
