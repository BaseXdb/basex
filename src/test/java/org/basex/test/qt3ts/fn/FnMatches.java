package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Test the fn:matches() function.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORX0002")
    );
  }

  /**
   *  Check for the correct behavior of ^ and $ in multi-line mode This test case was motivated by the resolution of Bug Report 4543 .
   */
  @org.junit.Test
  public void fnMatches26() {
    final XQuery query = new XQuery(
      "fn:matches(\"concat('abcd', codepoints-to-string(10), 'defg', codepoints-to-string(10))\", \"^$\", \"m\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  "." does match CR in default mode.
   */
  @org.junit.Test
  public void fnMatches45() {
    final XQuery query = new XQuery(
      "fn:matches(concat('Mary', codepoints-to-string(13), 'Jones'), 'Mary.Jones')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORX0002")
    );
  }
}
