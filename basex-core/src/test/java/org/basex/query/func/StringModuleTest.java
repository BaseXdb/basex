package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the String Module.
 * Many of the Soundex and ColognePhonetic tests have been adopted from the
 * Apache Commons project (SoundexTest.java, ColognePhoneticTest.java).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void colognePhonetic() {
    colognePhonetic("", "");

    colognePhonetic("wikipedia", "3412");
    colognePhonetic("WIKIPEDIA", "3412");

    colognePhonetic("ßüöä", "8");
    colognePhonetic("suoa", "8");

    colognePhonetic("Aabjoe", "01");
    colognePhonetic("Aaclan", "0856");
    colognePhonetic("Aychlmajr", "04567");

    colognePhonetic("Müller-Lüdenscheidt", "65752682");
    colognePhonetic("bergisch-gladbach", "174845214");

    // edge cases
    colognePhonetic("a", "0");
    colognePhonetic("e", "0");
    colognePhonetic("i", "0");
    colognePhonetic("o", "0");
    colognePhonetic("u", "0");
    colognePhonetic("ä", "0");
    colognePhonetic("ö", "0");
    colognePhonetic("ü", "0");
    colognePhonetic("aa", "0");
    colognePhonetic("ha", "0");
    colognePhonetic("h", "");
    colognePhonetic("aha", "0");
    colognePhonetic("b", "1");
    colognePhonetic("p", "1");
    colognePhonetic("ph", "3");
    colognePhonetic("f", "3");
    colognePhonetic("v", "3");
    colognePhonetic("w", "3");
    colognePhonetic("g", "4");
    colognePhonetic("k", "4");
    colognePhonetic("q", "4");
    colognePhonetic("x", "48");
    colognePhonetic("ax", "048");
    colognePhonetic("cx", "48");
    colognePhonetic("l", "5");
    colognePhonetic("cl", "45");
    colognePhonetic("acl", "085");
    colognePhonetic("mn", "6");
    colognePhonetic("r", "7");

    // phonetic examples
    colognePhonetic("müller", "657");
    colognePhonetic("schmidt", "862");
    colognePhonetic("schneider", "8627");
    colognePhonetic("fischer", "387");
    colognePhonetic("weber", "317");
    colognePhonetic("wagner", "3467");
    colognePhonetic("becker", "147");
    colognePhonetic("hoffmann", "0366");
    colognePhonetic("schäfer", "837");
    colognePhonetic("Breschnew", "17863");
    colognePhonetic("Wikipedia", "3412");
    colognePhonetic("peter", "127");
    colognePhonetic("pharma", "376");
    colognePhonetic("mönchengladbach", "664645214");
    colognePhonetic("deutsch", "28");
    colognePhonetic("deutz", "28");
    colognePhonetic("hamburg", "06174");
    colognePhonetic("hannover", "0637");
    colognePhonetic("christstollen", "478256");
    colognePhonetic("Xanthippe", "48621");
    colognePhonetic("Zacharias", "8478");
    colognePhonetic("Holzbau", "0581");
    colognePhonetic("matsch", "68");
    colognePhonetic("matz", "68");
    colognePhonetic("Arbeitsamt", "071862");
    colognePhonetic("Eberhard", "01772");
    colognePhonetic("Eberhardt", "01772");
    colognePhonetic("heithabu", "021");
  }

  /** Test method. */
  @Test public void colognePhoneticEquals() {
    cologneEquals("Mayr", "Meyer");
    cologneEquals("house", "house");
    cologneEquals("House", "house");
    cologneEquals("Haus", "house");
    cologneEquals("Gans", "ganz");
    cologneEquals("Gänse", "ganz");
    cologneEquals("Miyagi", "Miyako");
  }

  /** Test method. */
  @Test public void colognePhoneticVariations() {
    cologneVariations("65", "mella", "milah", "moulla", "mellah", "muehle", "mule");
    cologneVariations("67", "Meier", "Maier", "Mair", "Meyer", "Meyr", "Mejer", "Major");
  }

  /** Test method. */
  @Test public void options() {
    final Function lev = _STRING_LEVENSHTEIN, dist = _STRING_LEVENSHTEIN_DISTANCE;
    final Function set = _STRING_TOKEN_SET_RATIO, sort = _STRING_TOKEN_SORT_RATIO;
    final Function jw = _STRING_JARO_WINKLER, ngrams = _STRING_NGRAMS;

    // default: strings are compared literally
    query(lev.args("flower", "FLOWER"), 0);
    query(lev.args("flower", "FLOWER", " {}"), 0);
    query(lev.args("Munch", "Münch", " { 'diacritics': 'sensitive' }"), 0.8);

    // full-text options
    query(lev.args("flower", "FLOWER", " { 'case': 'insensitive' }"), 1);
    query(lev.args("Munch", "Münch", " { 'diacritics': 'insensitive' }"), 1);
    query(lev.args("HOUSES", "house", " { 'stemming': true(), 'case': 'insensitive' }"), 1);
    query(jw.args("flower", "FLOWER", " { 'case': 'insensitive' }"), 1);
    query(dist.args("flower", "FLOWER", " ()", " { 'case': 'insensitive' }"), 0);
    query(ngrams.args("Rüb", " ()", " { 'case': 'insensitive', 'diacritics': 'insensitive' }"),
        "ru\nub");

    // token ratios: full-text options also strip punctuation
    query(set.args("Ruisdael, Jacob van", "Jacob van Ruisdael"), 0.9473684210526315);
    query(set.args("Ruisdael, Jacob van", "Jacob van Ruisdael", " { 'case': 'insensitive' }"), 1);
    query(sort.args("Gogh, Vincent van", "vincent van gogh", " { 'case': 'insensitive' }"), 1);

    error(lev.args("a", "b", " { 'case': 'unknown' }"), INVALIDOPTION_X);
  }

  /** Test method. */
  @Test public void format() {
    final Function func = _STRING_FORMAT;
    query(func.args("x", "x"), "x");
    query(func.args("%d", " 1"), "1");
    query(func.args("%2d", " 1"), " 1");
    query(func.args("%05d", " 123"), "00123");
    query(func.args("%s is %d", "Alice", 42), "Alice is 42");

    // the result must not depend on the locale of the system
    query(func.args("%e", 1234.5678), "1.234568e+03");
    query(func.args("%,d", 1234567), "1,234,567");
    query(func.args("%.2f", 1234.5678), "1234.57");

    // a format specifier requires a value
    error(func.args("%s", " ()"), INVTYPE_X);
    error(func.args("%d", " ()"), INVTYPE_X);
  }

  /** Test method. */
  @Test public void jaroWinkler() {
    final Function func = _STRING_JARO_WINKLER;
    query(func.args("", ""), 1);
    query(func.args("", "a"), 0);
    query(func.args("aaapppp", ""), 0);
    query(func.args("frog", "fog"), .93);
    query(func.args("fly", "ant"), 0);
    query(func.args("elephant", "hippo"), .44);
    query(func.args("hippo", "elephant"), .44);
    query(func.args("hippo", "zzzzzzzz"), 0);
    query(func.args("hello", "hallo"), .88);
    query(func.args("ABC Corporation", "ABC Corp"), .91);
    query(func.args("D N H Enterprises Inc", "D &amp; H Enterprises, Inc."), .95);
    query(func.args("My Gym Children's Fitness Center", "My Gym. Childrens Fitness"), .94);
    query(func.args("PENNSYLVANIA", "PENNCISYLVNIA"), .9);

    // the common prefix is rewarded, but only up to 4 characters
    query(func.args("abcdefgh", "abcdefghx"), .98);
  }

  /** Test method. */
  @Test public void levenshtein() {
    final Function func = _STRING_LEVENSHTEIN;
    // queries
    query(func.args("", ""), 1);
    query(func.args("a", "a"), 1);
    query(func.args("ab", "ab"), 1);
    query(func.args("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"), 1);
    query("string-join(replicate('x', 1000)) ! " + func.args(" .", " ."), 1);

    query(func.args("a", "b"), 0);
    query(func.args("b", "a"), 0);

    query(func.args("ab", "a"), 0.5);
    query(func.args("a", "ab"), 0.5);

    query(func.args("ab", ""), 0);
    query(func.args("", "ab"), 0);

    query(func.args("ac", "ab"), 0.5);
    query(func.args("ab", "ac"), 0.5);

    query(func.args("ab", "ba"), 0.5);
    query(func.args("ba", "ab"), 0.5);

    // optimal string alignment: no substring is edited more than once
    query(func.args("bcb", "cbc"), 1e0 / 3);
    query(func.args("abcde", "acbed"), 0.6);

    query(func.args("ab", "axb"), 2e0 / 3);
    query(func.args("ab", "axx"), 1e0 / 3);

    query(func.args("xyb", "ab"), 1e0 / 3);
    query(func.args("ab", "xyb"), 1e0 / 3);

    query(func.args(" string-join(replicate('x', 10000))", "x"), "0.0001");
    query(func.args("x", " string-join(replicate('x', 10000))"), "0.0001");

    error(func.args(" string-join(replicate('x', 10001))", "x"), STRING_BOUNDS_X);
  }

  /** Test method. */
  @Test public void levenshteinDistance() {
    final Function func = _STRING_LEVENSHTEIN_DISTANCE;
    query(func.args("", ""), 0);
    query(func.args("a", "a"), 0);
    query(func.args("", "abc"), 3);
    query(func.args("flower", "flowers"), 1);
    query(func.args("kitten", "sitting"), 3);
    // transposition of adjacent characters is a single edit
    query(func.args("ab", "ba"), 1);
    // optimal string alignment: no substring is edited more than once
    query(func.args("CA", "ABC"), 3);
    query(func.args("bcb", "cbc"), 2);
    query(func.args("abcde", "acbed"), 2);
    // comparison is case- and diacritic-sensitive
    query(func.args("flower", "FLOWER"), 6);
    query(func.args("Munch", "Münch"), 1);
    // characters outside the BMP are single codepoints
    query(func.args(" 'a&#x1D11E;'", "a"), 1);

    // bounded computation: empty sequence if the distance exceeds the maximum
    query(func.args("kitten", "sitting", 3), 3);
    query(func.args("kitten", "sitting", 2), "");
    query(func.args("a", "a", 0), 0);
    query(func.args("a", "b", 0), "");
    query(func.args("a", "a", -1), "");

    // the length is only limited if the distance is computed exhaustively
    final String long1 = " string-join(replicate('x', 10001))";
    error(func.args(long1, "x"), STRING_BOUNDS_X);
    query(func.args(long1, long1 + " || 'yy'", 5), 2);
    query(func.args(long1, long1 + " || 'yy'", 1), "");
  }

  /** Test method. */
  @Test public void closest() {
    final Function func = _STRING_CLOSEST;
    final String candidates = " ('Rembrandt', 'Rembrand', 'Rubens')";

    // best candidate, returned by default
    query(func.args("Rembrant", " ()"), "");
    query(func.args("Rembrant", candidates) + "?value", "Rembrandt");
    query("count(" + func.args("Rembrant", candidates) + ')', 1);
    query(func.args("Rembrandt", candidates) + "?similarity", 1);

    // limit and ranking: equal similarities preserve the input order
    query(func.args("Rembrant", candidates, " { 'limit': 0 }") + "?value",
        "Rembrandt\nRembrand\nRubens");
    query(func.args("x", " ('a', 'b')", " { 'limit': 0 }") + "?value", "a\nb");

    // threshold
    query(func.args("Rembrant", candidates, " { 'limit': 0, 'threshold': 0.8 }") + "?value",
        "Rembrandt\nRembrand");
    query(func.args("Rembrant", candidates, " { 'threshold': 1 }"), "");

    // candidates can be untyped (e.g. the index entries returned by ft:tokens)
    query(func.args("Rembrant", " <entry>Rembrandt</entry>") + "?value", "Rembrandt");
    query(func.args("Rembrant", " (<e>Rubens</e>, <e>Rembrandt</e>)") + "?value", "Rembrandt");

    // measures: named function references are computed internally
    query(func.args("Ishida Yutei", " ('Yutei Ishida')",
        " { 'measure': string:token-sort-ratio#2 }") + "?similarity", 1);
    query(func.args("Ishida Yutei", " ('Yutei Ishida')",
        " { 'measure': string:levenshtein#2 }") + "?similarity", 0);
    query(func.args("Rembrandt", " ('Rembrandt van Rijn')",
        " { 'measure': string:partial-ratio#2 }") + "?similarity", 1);

    // partially applied functions must not be confused with their named references
    query(func.args("night", " ('nacht')",
        " { 'measure': string:ngram-similarity#2 }") + "?similarity", 0.25);
    query(func.args("night", " ('nacht')",
        " { 'measure': string:ngram-similarity(?, ?, 3) }") + "?similarity", 0);

    // user-defined measures
    query(func.args("abc", " ('abd', 'xyz')",
        " { 'measure': fn($a, $b) { if($a = $b) then 1 else 0.5 }, 'limit': 0 }") + "?similarity",
        "0.5\n0.5");
    error(func.args("a", " ('b')", " { 'measure': 'levenshtein' }"), INVALIDOPTION_X_X_X_X);
    error(func.args("a", " ('b')", " { 'measure': string:soundex#1 }"), INVTYPE_X);

    // strings that are too long
    final String long1 = " string-join(replicate('x', 10001))";
    error(func.args(long1, " ('a')"), STRING_BOUNDS_X);
    error(func.args("a", long1), STRING_BOUNDS_X);
    query("exists(" + func.args("a", long1, " { 'measure': string:jaro-winkler#2 }") + ')', true);

    // edge cases
    query(func.args("", " ('', 'a')", " { 'limit': 0 }") + "?similarity", "1\n0");
    query("count(" + func.args("a", " ('a', 'a', 'b')", " { 'limit': 0 }") + ')', 3);
    query("count(" + func.args("a", " ('a')", " { 'threshold': 1.5 }") + ')', 0);
    query("count(" + func.args("a", " ('a', 'b')", " { 'limit': -1 }") + ')', 2);

    query("count(" + func.args("a", " ('a', 'b')",
        " { 'threshold': xs:double('NaN'), 'limit': 0 }") + ')', 0);

    // the measure must return a number
    error(func.args("a", " ('b')", " { 'measure': fn($x, $y) { () } }"), INVTYPE_X);
    error(func.args("a", " ('b')", " { 'measure': fn($x, $y) { 'x' } }"), NONUMBER_X_X);
  }

  /** Test method. */
  @Test public void partialRatio() {
    final Function func = _STRING_PARTIAL_RATIO;
    // the shorter string is compared with the best matching substring of the longer one
    query(func.args("Rembrandt", "Rembrandt van Rijn"), 1);
    query(func.args("Rembrandt van Rijn", "Rembrandt"), 1);

    // the measure is symmetric; strings of equal length yield the levenshtein similarity
    query(func.args("bbc", "bdb"), 1e0 / 3);
    query(func.args("bdb", "bbc"), 1e0 / 3);
    query(func.args("night", "nacht"), 0.6);
    query(func.args("van Rijn", "Rembrandt van Rijn"), 1);
    query(func.args("Rembrant", "Rembrandt van Rijn"), 0.875);
    // a transposition is a single edit
    query(func.args("ba", "xxabxx"), 0.5);
    query(func.args("abc", "abc"), 1);
    query(func.args("", ""), 1);
    query(func.args("", "abc"), 0);
    query(func.args("abc", ""), 0);
    query(func.args("xyz", "abc"), 0);
    query(func.args("c", "abcdef"), 1);
    query(func.args("abcdef", "cd"), 1);
    query(func.args(" '&#x1D11E;'", " 'x&#x1D11E;y'"), 1);

    // options
    query(func.args("RÜBENS", "peter paul rubens",
        " { 'case': 'insensitive', 'diacritics': 'insensitive' }"), 1);

    error(func.args(" string-join(replicate('x', 10001))", "x"), STRING_BOUNDS_X);
  }

  /** Test method. */
  @Test public void ngramSimilarity() {
    final Function func = _STRING_NGRAM_SIMILARITY;
    // default n-gram length (2)
    query(func.args("", ""), 1);
    query(func.args("abc", "abc"), 1);
    query(func.args("abc", ""), 0);
    query(func.args("", "abc"), 0);
    query(func.args("abc", "xyz"), 0);
    query(func.args("night", "nacht"), 0.25);
    // set semantics: repeated n-grams are collapsed
    query(func.args("aaa", "aa"), 1);

    // explicit n-gram length
    query(func.args("abcd", "abcd", 1), 1);
    query(func.args("abc", "abc", 3), 1);
    query(func.args("abc", "abd", 3), 0);
    // strings shorter than n are treated as a single n-gram
    query(func.args("ab", "ab", 5), 1);
    query(func.args("ab", "cd", 5), 0);

    error(func.args("a", "b", 0), STRING_NGRAM_X);
    error(func.args("a", "b", -1), STRING_NGRAM_X);
  }

  /** Test method. */
  @Test public void ngrams() {
    final Function func = _STRING_NGRAMS;
    // default n-gram length (2)
    query("string-join(" + func.args("abc") + ", '|')", "ab|bc");
    query("string-join(" + func.args("a") + ", '|')", "a");
    query("string-join(" + func.args("") + ", '|')", "");
    query("count(" + func.args("hello") + ")", 4);
    // n-grams are returned in order, including duplicates
    query("string-join(" + func.args("aaa") + ", '|')", "aa|aa");

    // explicit n-gram length
    query("string-join(" + func.args("abc", 1) + ", '|')", "a|b|c");
    query("string-join(" + func.args("abcd", 3) + ", '|')", "abc|bcd");
    // strings shorter than n yield a single n-gram with the whole string
    query("string-join(" + func.args("ab", 5) + ", '|')", "ab");
    query("count(" + func.args("", 5) + ")", 0);

    // invariant: ngram-similarity is the Sørensen-Dice coefficient over the distinct n-grams
    query("let $a := 'night', $b := 'nacht', $n := 2 "
        + "let $g1 := distinct-values(" + func.args(" $a", " $n") + ") "
        + "let $g2 := distinct-values(" + func.args(" $b", " $n") + ") "
        + "return 2e0 * count($g1[. = $g2]) div (count($g1) + count($g2)) = "
        + _STRING_NGRAM_SIMILARITY.args(" $a", " $b", " $n"), true);

    // codepoint-safe: characters outside the BMP are not split into surrogate halves
    query("let $s := codepoints-to-string((120094, 120095, 120096)) "
        + "let $grams := " + func.args(" $s", 2)
        + "return count($grams) = 2 and every($grams, fn { string-length() = 2 })", true);

    error(func.args("a", 0), STRING_NGRAM_X);
    error(func.args("a", -1), STRING_NGRAM_X);
  }

  /** Test method. */
  @Test public void tokenSetRatio() {
    final Function func = _STRING_TOKEN_SET_RATIO;
    query(func.args("", ""), 1);
    query(func.args("abc", ""), 0);
    query(func.args("abc", "abc"), 1);
    query(func.args("abc", "xyz"), 0);
    // order-independent
    query(func.args("hello world", "world hello"), 1);
    // tokens of one string are a subset of the other
    query(func.args("Fortuny y Marsal", "Fortuny Marsal"), 1);
    query(func.args("a b c", "c a"), 1);
    // whitespace is collapsed: repeated/trailing spaces, tabs and newlines
    query(func.args("a   b  ", "b a"), 1);
    query(func.args("a\tb\nc", "a b"), 1);

    error(func.args(" string-join(replicate('x', 10001))", "x"), STRING_BOUNDS_X);
  }

  /** Test method. */
  @Test public void tokenSortRatio() {
    final Function func = _STRING_TOKEN_SORT_RATIO;
    query(func.args("", ""), 1);
    query(func.args("abc", "abc"), 1);
    // order-independent
    query(func.args("hello world", "world hello"), 1);
    query(func.args("a b c", "c b a"), 1);
    // partial overlap
    query(func.args("a b c", "a b"), 0.6);
    query(func.args("abc", "xyz"), 0);
    // whitespace is collapsed: repeated/trailing spaces, tabs and newlines
    query(func.args("a   b  ", "b a"), 1);
    query(func.args("a\tb\nc", "c b a"), 1);

    error(func.args(" string-join(replicate('x', 10001))", "x"), STRING_BOUNDS_X);
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test public void soundex() {
    final Function func = _STRING_SOUNDEX;
    // queries
    query(func.args(""), "0000");
    query(func.args(" \"\""), "0000");

    query(func.args(" \"&#x9;&#xa;&#xd; Washington &#x9;&#xa;&#xd;\" "), "W252");
    query(func.args("Ashcraft"), "A261");
    query(func.args("BOOTHDAVIS"), "B312");
    query(func.args("BOOTH-DAVIS"), "B312");
    query(func.args("Smith"), "S530");
    query(func.args("Smythe"), "S530");
    query(func.args("Williams"), "W452");

    query(func.args("testing"), "T235");
    query(func.args("The"), "T000");
    query(func.args("quick"), "Q200");
    query(func.args("brown"), "B650");
    query(func.args("fox"), "F200");
    query(func.args("jumped"), "J513");
    query(func.args("over"), "O160");
    query(func.args("the"), "T000");
    query(func.args("lazy"), "L200");
    query(func.args("dogs"), "D200");

    query(func.args("Allricht"), "A462");
    query(func.args("Eberhard"), "E166");
    query(func.args("Engebrethson"), "E521");
    query(func.args("Heimbach"), "H512");
    query(func.args("Hanselmann"), "H524");
    query(func.args("Hildebrand"), "H431");
    query(func.args("Kavanagh"), "K152");
    query(func.args("Lind"), "L530");
    query(func.args("Lukaschowsky"), "L222");
    query(func.args("McDonnell"), "M235");
    query(func.args("McGee"), "M200");
    query(func.args("Opnian"), "O155");
    query(func.args("Oppenheimer"), "O155");
    query(func.args("Riedemanas"), "R355");
    query(func.args("Zita"), "Z300");
    query(func.args("Zitzmeinn"), "Z325");

    query(func.args("Washington"), "W252");
    query(func.args("Lee"), "L000");
    query(func.args("Gutierrez"), "G362");
    query(func.args("Pfister"), "P236");
    query(func.args("Jackson"), "J250");
    query(func.args("Tymczak"), "T522");
    query(func.args("VanDeusen"), "V532");

    query(func.args("HOLMES"), "H452");
    query(func.args("ADOMOMI"), "A355");
    query(func.args("VONDERLEHR"), "V536");
    query(func.args("BALL"), "B400");
    query(func.args("SHAW"), "S000");
    query(func.args("JACKSON"), "J250");
    query(func.args("SCANLON"), "S545");
    query(func.args("SAINTJOHN"), "S532");

    query(func.args("Ann"), "A500");
    query(func.args("Andrew"), "A536");
    query(func.args("Janet"), "J530");
    query(func.args("Margaret"), "M626");
    query(func.args("Steven"), "S315");
    query(func.args("Michael"), "M240");
    query(func.args("Robert"), "R163");
    query(func.args("Laura"), "L600");
    query(func.args("Anne"), "A500");
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test public void soundexDifference() {
    soundexDiff("Smith", "Smythe", 4);
    soundexDiff("Ann", "Andrew", 2);
    soundexDiff("Margaret", "Andrew", 1);
    soundexDiff("Janet", "Margaret", 0);

    soundexDiff("Green", "Greene", 4);
    soundexDiff("Blotchet-Halls", "Greene", 0);

    soundexDiff("Smith", "Smythe", 4);
    soundexDiff("Smithers", "Smythers", 4);
    soundexDiff("Anothers", "Brothers", 2);
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test public void soundexVariations() {
    soundexVariations("B650",
      "BARHAM", "BARONE", "BARRON", "BERNA", "BIRNEY", "BIRNIE", "BOOROM", "BOREN", "BORN", "BOURN",
      "BOURNE", "BOWRON", "BRAIN", "BRAME", "BRANN", "BRAUN", "BREEN", "BRIEN", "BRIM", "BRIMM",
      "BRINN", "BRION", "BROOM", "BROOME", "BROWN", "BROWNE", "BRUEN", "BRUHN", "BRUIN", "BRUMM",
      "BRUN", "BRUNO", "BRYAN", "BURIAN", "BURN", "BURNEY", "BYRAM", "BYRNE", "BYRON", "BYRUM"
    );
    soundexVariations("O165",
      "OBrien", "'OBrien", "O'Brien", "OB'rien", "OBr'ien", "OBri'en", "OBrie'n", "OBrien'"
    );
    soundexVariations("K525",
      "KINGSMITH", "-KINGSMITH", "K-INGSMITH", "KI-NGSMITH", "KIN-GSMITH", "KING-SMITH",
      "KINGS-MITH", "KINGSM-ITH", "KINGSMI-TH", "KINGSMIT-H", "KINGSMITH-"
    );
    soundexVariations("W452",
      "Williams"
    );
    soundexVariations("S460",
      "Sgler", "Swhgler",
      "SAILOR", "SALYER", "SAYLOR", "SCHALLER", "SCHELLER", "SCHILLER", "SCHOOLER", "SCHULER",
      "SCHUYLER", "SEILER", "SEYLER", "SHOLAR", "SHULER", "SILAR", "SILER", "SILLER"
    );
    soundexVariations("E625",
      "Erickson", "Erickson", "Erikson", "Ericson", "Ericksen", "Ericsen"
    );
  }

  /**
   * Checks Soundex variations.
   * @param code expected code
   * @param variations variations
   */
  private static void soundexVariations(final String code, final String... variations) {
    final Function func = _STRING_SOUNDEX;
    for(final String string : variations) query(func.args(string), code);
  }

  /**
   * Checks Soundex differences.
   * @param string1 first string
   * @param string2 second string
   * @param diff difference
   */
  private static void soundexDiff(final String string1, final String string2, final int diff) {
    final Function func = _STRING_SOUNDEX;
    // queries
    query("sum(for-each-pair(" +
      "string-to-codepoints(" + func.args(string1) + "), " +
      "string-to-codepoints(" + func.args(string2) + "), " +
      "function($cp1, $cp2) { if($cp1 = $cp2) then 1 else 0 }))", diff);
  }

  /**
   * Tests the cologne-phonetic method.
   * @param arg argument
   * @param result result
   */
  private static void colognePhonetic(final String arg, final String result) {
    final Function func = _STRING_COLOGNE_PHONETIC;
    query(func.args(arg), result);
  }

  /**
   * Tests the cologne-phonetic method.
   * @param string1 first string
   * @param string2 second string
   */
  private static void cologneEquals(final String string1, final String string2) {
    final Function func = _STRING_COLOGNE_PHONETIC;
    query(func.args(string1) + " = " + func.args(string2), true);
  }

  /**
   * Checks Cologne Phonetic variations.
   * @param code expected code
   * @param variations variations
   */
  private static void cologneVariations(final String code, final String... variations) {
    final Function func = _STRING_COLOGNE_PHONETIC;
    for(final String string : variations) query(func.args(string), code);
  }
}
