package org.basex.test.query.func;

import org.basex.test.query.*;

/**
 * XQuery functions tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FuncTest extends QueryTest {
  /** Constructor. */
  static {
    doc =
      "<desclist xml:lang='en'>" +
      "<desc xml:lang='en-US'><line>blue</line></desc>" +
      "<desc xml:lang='fr'><line>bleu</line></desc>" +
      "</desclist>";

    queries = new Object[][] {
      { "false 1", bool(false), "false()" },
      { "true  2", bool(true),  "true()" },

      { "count  1", "count()" },
      { "count  2", "count(1, 1)" },
      { "count  3", bool(false), "count(1[.]) eq 0" },
      { "count  4", bool(false), "count(1[.]) <= 0" },
      { "count  5", bool(false), "count(1[.]) < 0" },
      { "count  6", bool(true), "count(1[.]) != 0" },
      { "count  7", bool(true), "count(1[.]) >= 0" },
      { "count  8", bool(true), "count(1[.]) > 0" },
      { "count  9", bool(true), "count(1[.]) = 1" },
      { "count 10", bool(true), "count(1[.]) le 1" },
      { "count 11", bool(false), "count(1[.]) < 1" },
      { "count 12", bool(false), "count(1[.]) != 1" },
      { "count 13", bool(true), "count(1[.]) >= 1" },
      { "count 14", bool(false), "count(1[.]) > 1" },
      { "count 15", bool(false), "count(1[.]) = 2" },
      { "count 16", bool(true), "count(1[.]) <= 2" },
      { "count 17", bool(true), "count(1[.]) lt 2" },
      { "count 18", bool(true), "count(1[.]) != 2" },
      { "count 19", bool(false), "count(1[.]) >= 2" },
      { "count 20", bool(false), "count(1[.]) > 2" },
      { "count 21", bool(false), "count(1[.]) = 1.1" },
      { "count 22", bool(true), "count(1[.]) <= 1.1" },
      { "count 23", bool(true), "count(1[.]) < 1.1" },
      { "count 24", bool(true), "count(1[.]) ne 1.1" },
      { "count 25", bool(false), "count(1[.]) >= 1.1" },
      { "count 26", bool(false), "count(1[.]) > 1.1" },
      { "count 27", bool(false), "count(1[.]) = -1.1" },
      { "count 28", bool(false), "count(1[.]) <= -1.1" },
      { "count 29", bool(false), "count(1[.]) < -1.1" },
      { "count 30", bool(true), "count(1[.]) != -1.1" },
      { "count 31", bool(true), "count(1[.]) ge -1.1" },
      { "count 32", bool(true), "count(1[.]) gt -1.1" },
      { "count 33", itr(10000000),
        "count(for $i in 1 to 10000000 return $i)" },
      { "count 34", itr(100000),
        "count(for $i in 1 to 100000 return $i * $i)" },
      { "count 35", itr(1000000000000l),
        "count(for $i in 1 to 10000000 for $i in 1 to 100000 return $i * $i)" },
      { "count 36", itr(2),
        "count((for $a in (1,2) for $b in <b/> return $b)/.)" },
      { "count 37", itr(2),
        "count((for $a in (1,2) let $b := <b/> return $b)/.)" },
      { "count 38", itr(2), "count(//desc/1)" },
      { "count 39", itr(4), "count(//desc/(1,2))" },

      { "empty 1", bool(false), "empty(math:random())" },
      { "empty 2", bool(false, false), "for $x in 1 to 2 return empty($x)" },

      { "exists 1", bool(true), "exists(math:random())" },
      { "exists 2", bool(true, true), "for $x in 1 to 2 return exists($x)" },

      { "contains 1", "contains(.)" },
      { "contains 2", "contains(. .)" },

      { "deep-equal  1", bool(true),  "deep-equal(1, 1)" },
      { "deep-equal  2", bool(false), "deep-equal(1, 2)" },
      { "deep-equal  3", bool(true),  "deep-equal('a', 'a')" },
      { "deep-equal  4", bool(false), "deep-equal('a', 'b')" },
      { "deep-equal  5", bool(true),  "deep-equal(1.0, 1)" },
      { "deep-equal  6", bool(false), "deep-equal('1', 1)" },
      { "deep-equal  7", bool(true),  "deep-equal((), ())" },
      { "deep-equal  8", bool(false), "deep-equal(<a>1</a>, 1)" },
      { "deep-equal  9", bool(true),  "deep-equal(text{'a'},text{'a'})" },
      { "deep-equal 10", bool(false), "deep-equal(text{'a'},text{'b'})" },
      { "deep-equal 11", bool(true), "deep-equal(comment{'a'},comment{'a'})" },
      { "deep-equal 12", bool(false), "deep-equal(comment{'a'},comment{'b'})" },
      { "deep-equal 13", bool(false), "deep-equal(text{'a'},comment{'a'})" },
      { "deep-equal 14", bool(false),
        "deep-equal(comment{ 'a' }, processing-instruction{ 'a' } { 'a' })" },

      { "number 1", dbl(1), "number(true())" },
      { "number 2", dbl(0), "number(false())" },
      { "number 3", dbl(Double.NaN), "number(xs:gYear('2005'))" },

      // http://www.xqueryfunctions.com/xq/fn_lang.html
      { "lang 1", node(3), "//desc[lang('en')]" },
      { "lang 2", node(3), "//desc[lang('en-US')]" },
      { "lang 3", node(7), "//desc[lang('fr')]" },
      { "lang 4", node(5), "//desc/line[lang('en')]" },
      { "lang 5", empty(),  "/.[lang('en-US')]" },
      { "lang 6", node(7), "//desc[lang('FR')]" },

      { "last 1", itr(1), "1[last()]" },
      { "last 2", itr(2), "(1 to 2)[last()]" },
      { "last 3", itr(2), "(1 to 2)[position()=last()]" },
      { "last 4", itr(1, 2), "(1 to 2)[position()=1 or position()=last()]" },
      { "last 5", empty(), "(1 to 2)[position()=1 and position()=last()]" },

      { "pow 1", dbl(1.0e0), "math:pow(1, xs:double('INF'))" },
      { "pow 2", dbl(1.0e0), "math:pow(1, xs:double('-INF'))" },
      { "pow 3", dbl(1.0e0), "math:pow(-1, xs:double('INF'))" },
      { "pow 4", dbl(1.0e0), "math:pow(-1, xs:double('-INF'))" },
      { "pow 5", dbl(1.0e0), "math:pow(1, xs:double('NaN'))" },
      { "pow 6", dbl(Double.NaN), "math:pow(-2.5e0, 2.00000001e0)" },

      { "distinct-values 1", str("blue", "bleu"),
        "for $i in distinct-values(//line) return string($i)" },
      { "distinct-values 2", itr(2),
        "count(distinct-values(//line/text()))" },

      { "substring 1", str("bar"), "substring('foobar', 4)" },
      { "substring 2", str("foo"), "substring('foobar', 1, 3)" },
      { "substring 3", str(""), "substring('foobar', xs:double('NaN'), 3)" },
      { "substring 4", str("foo"), "substring('foobar', -1, 5)" },

      { "coll 1", bool(true), "exists(collection('src/test/resources/dir/'))" },
      { "coll 2", bool(true), "exists(collection('src/test/resources/input.xml'))" },

      { "doc 1", bool(true), "exists(doc('src/test/resources/input.xml'))" },
      { "doc 2", "exists(doc('src/test/resources/dir/'))" },
    };
  }

  /* TABLE REPRESENTATION
  PRE  DIS  SIZ  ATS  NS  KIND  CONTENT
  -------------------------------------------------
    0    1   11    1  +0  DOC   test.xml
    1    1   10    2   0  ELEM  desclist
    2    1    1    1   0  ATTR  xml:lang="en"
    3    2    4    2   0  ELEM  desc
    4    1    1    1   0  ATTR  xml:lang="en-US"
    5    2    2    1   0  ELEM  line
    6    1    1    1   0  TEXT  A line of text.
    7    6    4    2   0  ELEM  desc
    8    1    1    1   0  ATTR  xml:lang="fr"
    9    2    2    1   0  ELEM  line
   10    1    1    1   0  TEXT  Une ligne de texte.
  */
}
