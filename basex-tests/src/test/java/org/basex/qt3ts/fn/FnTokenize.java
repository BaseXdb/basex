package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests the fn:tokenize() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnTokenize extends QT3TestSet {

  /**
   *  fn:tokenize takes at least two arguments. .
   */
  @org.junit.Test
  public void kTokenizeFunc1() {
    final XQuery query = new XQuery(
      "tokenize(\"input\")",
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
   *  The pattern can't be the empty sequence. .
   */
  @org.junit.Test
  public void kTokenizeFunc2() {
    final XQuery query = new XQuery(
      "tokenize(\"input\", ())",
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
   *  The flags argument cannot contain whitespace. .
   */
  @org.junit.Test
  public void kTokenizeFunc3() {
    final XQuery query = new XQuery(
      "tokenize(\"input\", \"pattern\", \" \")",
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
  public void kTokenizeFunc4() {
    final XQuery query = new XQuery(
      "tokenize(\"input\", \"pattern\", \"X\")",
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
   *  Only three arguments are accepted. .
   */
  @org.junit.Test
  public void kTokenizeFunc5() {
    final XQuery query = new XQuery(
      "tokenize(\"input\", \"pattern\", \"\", ())",
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
   *  fn:tokenize with a positional predicate. .
   */
  @org.junit.Test
  public void k2TokenizeFunc1() {
    final XQuery query = new XQuery(
      "fn:tokenize((\"abracadabra\", current-time())[1] treat as xs:string, \"(ab)|(a)\")[last()] eq \"\"",
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
   *  fn:tokenize with a positional predicate. .
   */
  @org.junit.Test
  public void k2TokenizeFunc2() {
    final XQuery query = new XQuery(
      "empty(fn:tokenize((\"abracadabra\", current-time())[1] treat as xs:string, \"(ab)|(a)\")[last() + 1])",
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
   *  fn:tokenize with a positional predicate(#2). .
   */
  @org.junit.Test
  public void k2TokenizeFunc3() {
    final XQuery query = new XQuery(
      "fn:tokenize((\"abracadabra\", current-time())[1] treat as xs:string, \"(ab)|(a)\")[last() - 1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "r")
    );
  }

  /**
   *  fn:tokenize with a positional predicate(#3). .
   */
  @org.junit.Test
  public void k2TokenizeFunc4() {
    final XQuery query = new XQuery(
      "fn:tokenize((\"abracadabra\", current-time())[1] treat as xs:string, \"(ab)|(a)\")[last() - 3]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "c")
    );
  }

  /**
   *  Tokenize a sequence of words. .
   */
  @org.junit.Test
  public void k2TokenizeFunc5() {
    final XQuery query = new XQuery(
      "deep-equal(fn:tokenize(\"The cat sat on the mat\", \"\\s+\"), (\"The\", \"cat\", \"sat\", \"on\", \"the\", \"mat\")), count(fn:tokenize(\"The cat sat on the mat\", \"\\s+\")), count(fn:tokenize(\" The cat sat on the mat \", \"\\s+\")), fn:tokenize(\"The cat sat on the mat\", \"\\s+\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true 6 8 The cat sat on the mat")
    );
  }

  /**
   *  A regexp that some Java versions have trouble with. .
   */
  @org.junit.Test
  public void k2TokenizeFunc6() {
    final XQuery query = new XQuery(
      "replace('APXterms6', '\\w{3}\\d*([^TKR0-9]+).*$', '$1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "terms")
    );
  }

  /**
   *  Tokenize on a single whitespace. .
   */
  @org.junit.Test
  public void k2TokenizeFunc7() {
    final XQuery query = new XQuery(
      "count(tokenize(\"a b\", \" \")), count(tokenize(\"a b\", \"\\s\")), string-join(tokenize(\"a b\", \" \"), '|'), string-join(tokenize(\"a b\", \"\\s\"), '|'), tokenize(\"a b\", \" \"), tokenize(\"a b\", \"\\s\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 2 a|b a|b a b a b")
    );
  }

  /**
   *  Test boolean on tokenize .
   */
  @org.junit.Test
  public void cbclFnTokenize001() {
    final XQuery query = new XQuery(
      "\n" +
      "      for $x in xs:string(zero-or-one((1 to 10)[. mod 2 = -1])) return tokenize($x,',')\n" +
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
      assertStringValue(false, "")
    );
  }

  /**
   *  Test invalid regex expression .
   */
  @org.junit.Test
  public void cbclFnTokenize002() {
    final XQuery query = new XQuery(
      "\n" +
      "      tokenize(string-join(for $x in (1 to 10)[. mod 2 = 0] return string($x),','),'[')\n" +
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
      error("FORX0002")
    );
  }

  /**
   *  Test tokenize on empty string .
   */
  @org.junit.Test
  public void cbclFnTokenize003() {
    final XQuery query = new XQuery(
      "\n" +
      "      tokenize(string-join(for $x in (1 to 10)[. mod 2 < 0] return string($x),','),',')\n" +
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
      assertStringValue(false, "")
    );
  }

  /**
   *  Evaluation of tokenize function where pattern matches the zero length string. Given on example 3 for this function in the Func and Ops specs. .
   */
  @org.junit.Test
  public void fnTokenize1() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abba\", \".?\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORX0003")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern that does not match the input string. .
   */
  @org.junit.Test
  public void fnTokenize10() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra\", \"ww\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abracadabra")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "^a". .
   */
  @org.junit.Test
  public void fnTokenize11() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra\", \"^a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"\", \"bracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\^". .
   */
  @org.junit.Test
  public void fnTokenize12() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra^abracadabra\", \"\\^\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\?" for an input string that contains "?". .
   */
  @org.junit.Test
  public void fnTokenize13() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra?abracadabra\", \"\\?\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\*" for an input string that contains "*". .
   */
  @org.junit.Test
  public void fnTokenize14() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra*abracadabra\", \"\\*\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\+" for an input string that contains "+". .
   */
  @org.junit.Test
  public void fnTokenize15() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra+abracadabra\", \"\\+\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\{" for an input string that contains "}". .
   */
  @org.junit.Test
  public void fnTokenize16() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra{abracadabra\", \"\\{\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\}" for an input string that contains "}". .
   */
  @org.junit.Test
  public void fnTokenize17() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra}abracadabra\", \"\\}\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\(" for an input string that contains "(". .
   */
  @org.junit.Test
  public void fnTokenize18() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra(abracadabra\", \"\\(\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\)" for an input string that contains ")". .
   */
  @org.junit.Test
  public void fnTokenize19() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra)abracadabra\", \"\\)\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function whith an invalid value for the flags .
   */
  @org.junit.Test
  public void fnTokenize2() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"The cat sat on the mat\", \"\\s+\", \"t\")",
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
   *  Evaluation of tokenize function with pattern set to "\[" for an input string that contains "[". .
   */
  @org.junit.Test
  public void fnTokenize20() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra[abracadabra\", \"\\[\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\]" for an input string that contains "]". .
   */
  @org.junit.Test
  public void fnTokenize21() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra]abracadabra\", \"\\]\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\-" for an input string that contains "-". .
   */
  @org.junit.Test
  public void fnTokenize22() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra-abracadabra\", \"\\-\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\." for an input string that contains ".". .
   */
  @org.junit.Test
  public void fnTokenize23() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra.abracadabra\", \"\\.\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\|" for an input string that contains "|". .
   */
  @org.junit.Test
  public void fnTokenize24() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra|abracadabra\", \"\\|\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\\" for an input string that contains "\". .
   */
  @org.junit.Test
  public void fnTokenize25() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra\\abracadabra\", \"\\\\\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\t" for an input string that contains the tab character. .
   */
  @org.junit.Test
  public void fnTokenize26() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra\tabracadabra\", \"\\t\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\n" for an input string that contains the newline character. .
   */
  @org.junit.Test
  public void fnTokenize27() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabra\n" +
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
      assertDeepEq("\"abracadabra\", \"abracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "aa{1}" (exact quantity) for an input string that contains the "aa" string. .
   */
  @org.junit.Test
  public void fnTokenize28() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabraabracadabra\", \"aa{1}\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabr\", \"bracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "aa{1,}" (min quantity) for an input string that contains the "aa" string twice. .
   */
  @org.junit.Test
  public void fnTokenize29() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabraabracadabraabracadabra\", \"aa{1,}\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabr\", \"bracadabr\", \"bracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\s+" as per example 1 for this functions from the Func and Ops specs. .
   */
  @org.junit.Test
  public void fnTokenize3() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"The cat sat on the mat\", \"\\s+\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "The cat sat on the mat")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "aa{1,2}" (range quantity) for an input string that contains the "aa" string twice. .
   */
  @org.junit.Test
  public void fnTokenize30() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abracadabraabracadabraabracadabra\", \"aa{1,2}\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abracadabr\", \"bracadabr\", \"bracadabra\"")
    );
  }

  /**
   *  Evaluation of tokenize function with regex 'q' flag. .
   */
  @org.junit.Test
  public void fnTokenize31() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"abc.def.gh.ijk\", \".\", \"q\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"abc\", \"def\", \"gh\", \"ijk\"")
    );
  }

  /**
   *  Evaluation of tokenize function with regex 'q' and "i" flags. .
   */
  @org.junit.Test
  public void fnTokenize32() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"A.BRA.CADA.BRA\", \"a.\", \"qi\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"\", \"BR\", \"CAD\", \"BRA\"")
    );
  }

  /**
   *  Evaluation of tokenize function with non-capturing group in the regex. .
   */
  @org.junit.Test
  public void fnTokenize33() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"ABRACADABRA\", \"A(?:B)\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"\", \"RACAD\", \"RA\"")
    );
  }

  /**
   *  "." does NOT match CR in default mode.
   */
  @org.junit.Test
  public void fnTokenize34() {
    final XQuery query = new XQuery(
      "fn:tokenize(concat('Mary', codepoints-to-string(13), 'Jones'), 'y.J')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("concat('Mary', codepoints-to-string(13), 'Jones')")
    );
  }

  /**
   *  "." does match CR in dot-all mode.
   */
  @org.junit.Test
  public void fnTokenize35() {
    final XQuery query = new XQuery(
      "fn:tokenize(concat('Mary', codepoints-to-string(13), 'Jones'), 'y.J', 's')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"Mar\", \"ones\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\s*" as per example 2 for this functions from the Func and Ops specs. .
   */
  @org.junit.Test
  public void fnTokenize4() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"1, 15, 24, 50\", \",\\s*\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 15 24 50")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern set to "\s*<br>\s*" and flag set to "i" as per example 4 for this functions from the Func and Ops specs. .
   */
  @org.junit.Test
  public void fnTokenize5() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"Some unparsed <br> HTML <BR> text\", \"\\s*<br>\\s*\", \"i\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"Some unparsed\", \"HTML\", \"text\"")
    );
  }

  /**
   *  Evaluation of tokenize function with pattern with flags arguments set to empty string. .
   */
  @org.junit.Test
  public void fnTokenize6() {
    final XQuery query = new XQuery(
      "fn:tokenize(\"Some unparsed <br> HTML <BR> text\", \"\\s*<br>\\s*\", \"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"Some unparsed\", \"HTML <BR> text\"")
    );
  }

  /**
   *  Evaluation of tokenize function with $input set to empty sequence Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnTokenize7() {
    final XQuery query = new XQuery(
      "fn:count(fn:tokenize((), \"\\s+\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluation of tokenize function with $input set to zero length string. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnTokenize8() {
    final XQuery query = new XQuery(
      "fn:count(fn:tokenize(\"\", \"\\s+\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluation of tokenize function with two patterms matching the input string. .
   */
  @org.junit.Test
  public void fnTokenize9() {
    final XQuery query = new XQuery(
      "string-join(fn:tokenize(\"abracadabra\", \"(ab)|(a)\"), '#')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "#r#c#d#r#")
    );
  }
}
