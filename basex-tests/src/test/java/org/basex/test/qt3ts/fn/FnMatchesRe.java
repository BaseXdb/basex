package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Test regular expression syntax in the fn:matches() function.
 *             These tests are created from the XSLT test suite, which in turn are derived
 *             from the Microsoft regular expression tests submitted to W3C as part of the
 *             XML Schema test suite, with expected results added based on actual Saxon results.
 *             
 *             Because the tests have been generated to use non-capturing groups, they require
 *             support for XPath 3.0 regular expressions..
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMatchesRe extends QT3TestSet {

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00001() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(',', ',') satisfies matches($s, '^(?:)$')) and (every $s in tokenize('a,#x20;,#xD;,#x9;,#xA;', ',') satisfies not(matches($s, '^(?:)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00002() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a', ',') satisfies matches($s, '^(?:a)$')) and (every $s in tokenize('aa,b,', ',') satisfies not(matches($s, '^(?:a)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00003() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a', ',') satisfies matches($s, '^(?:a|a)$')) and (every $s in tokenize('aa,b,', ',') satisfies not(matches($s, '^(?:a|a)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00004() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a,b', ',') satisfies matches($s, '^(?:a|b)$')) and (every $s in tokenize('aa,bb,ab,', ',') satisfies not(matches($s, '^(?:a|b)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00005() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ab', ',') satisfies matches($s, '^(?:ab)$')) and (every $s in tokenize('a,b,aa,bb,', ',') satisfies not(matches($s, '^(?:ab)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00006() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a,b,c,d', ',') satisfies matches($s, '^(?:a|b|a|c|b|d|a)$')) and (every $s in tokenize('aa,ac,e', ',') satisfies not(matches($s, '^(?:a|b|a|c|b|d|a)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00007() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('       a', ',') satisfies matches($s, '^(?:       a|b      )$')) and (every $s in tokenize('abc', ',') satisfies not(matches($s, '^(?:       a|b      )$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00008() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ac,abc', ',') satisfies matches($s, '^(?:ab?c)$')) and (every $s in tokenize('a,ab,bc,', ',') satisfies not(matches($s, '^(?:ab?c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00009() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ab,abc', ',') satisfies matches($s, '^(?:abc?)$')) and (every $s in tokenize('a,bc,abcc,', ',') satisfies not(matches($s, '^(?:abc?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00010() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abc,abbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbc', ',') satisfies matches($s, '^(?:ab+c)$')) and (every $s in tokenize('ac,bbbc,abbb,', ',') satisfies not(matches($s, '^(?:ab+c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00011() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abc,abccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc', ',') satisfies matches($s, '^(?:abc+)$')) and (every $s in tokenize('a,ab,abcd', ',') satisfies not(matches($s, '^(?:abc+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00012() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abc,abbbbbbbc,ac', ',') satisfies matches($s, '^(?:ab*c)$')) and (every $s in tokenize('a,ab,bc,c,abcb,', ',') satisfies not(matches($s, '^(?:ab*c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00013() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abc,ab,abccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc', ',') satisfies matches($s, '^(?:abc*)$')) and (every $s in tokenize('a,abcd,abbc,', ',') satisfies not(matches($s, '^(?:abc*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00014() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('b,ab,bcccccc,abc,abbbc', ',') satisfies matches($s, '^(?:a?b+c*)$')) and (every $s in tokenize('aabc,a,c,ac,', ',') satisfies not(matches($s, '^(?:a?b+c*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00015() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abc?,abbbc??,abca??,abbbbca?', ',') satisfies matches($s, '^(?:(ab+c)a?\\?\\??)$')) and (every $s in tokenize('ac??,bc??,abc,abc???', ',') satisfies not(matches($s, '^(?:(ab+c)a?\\?\\??)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00016() {
    final XQuery query = new XQuery(
      "matches('qwerty','?a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00017() {
    final XQuery query = new XQuery(
      "matches('qwerty','+a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00018() {
    final XQuery query = new XQuery(
      "matches('qwerty','*a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00019() {
    final XQuery query = new XQuery(
      "matches('qwerty','{1}a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00020() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(',', ',') satisfies matches($s, '^(?:a{0})$')) and (every $s in tokenize('a', ',') satisfies not(matches($s, '^(?:a{0})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00021() {
    final XQuery query = new XQuery(
      "matches('qwerty','a{2,1}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00022() {
    final XQuery query = new XQuery(
      "matches('qwerty','a{1,0}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00023() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abab,', ',') satisfies matches($s, '^(?:((ab){2})?)$')) and (every $s in tokenize('a,ab,ababa,abababab', ',') satisfies not(matches($s, '^(?:((ab){2})?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00024() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('aa,aaaa,aaaaaaaaaaaaaaaaaaaa', ',') satisfies matches($s, '^(?:(a{2})+)$')) and (every $s in tokenize(',a,a2,aaa', ',') satisfies not(matches($s, '^(?:(a{2})+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00025() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(',aa,aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', ',') satisfies matches($s, '^(?:(a{2})*)$')) and (every $s in tokenize('a,aaa,aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', ',') satisfies not(matches($s, '^(?:(a{2})*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00026() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abbc', ',') satisfies matches($s, '^(?:ab{2}c)$')) and (every $s in tokenize('ac,abc,abbbc,a,', ',') satisfies not(matches($s, '^(?:ab{2}c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00027() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abcc', ',') satisfies matches($s, '^(?:abc{2})$')) and (every $s in tokenize('abc,abccc,', ',') satisfies not(matches($s, '^(?:abc{2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00028() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('aaabbb,bb,bbb,bbbb', ',') satisfies matches($s, '^(?:a*b{2,4}c{0})$')) and (every $s in tokenize('ab,abbc,bbc,abbbbb,', ',') satisfies not(matches($s, '^(?:a*b{2,4}c{0})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00029() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ab,abac,abacac', ',') satisfies matches($s, '^(?:((ab)(ac){0,2})?)$')) and (every $s in tokenize('ac,abacacac,abaca,abab,abacabac', ',') satisfies not(matches($s, '^(?:((ab)(ac){0,2})?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00030() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(',a b,a ba b', ',') satisfies matches($s, '^(?:(a\\sb){0,2})$')) and (every $s in tokenize('a ba ba b,ab,a b a b,a  b', ',') satisfies not(matches($s, '^(?:(a\\sb){0,2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00031() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abab,ababab,ababababababababababababababababababababababababababababababababab', ',') satisfies matches($s, '^(?:(ab){2,})$')) and (every $s in tokenize('ab,ababa,ababaa,ababababa,abab abab,', ',') satisfies not(matches($s, '^(?:(ab){2,})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00032() {
    final XQuery query = new XQuery(
      "matches('qwerty','a{,2}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00033() {
    final XQuery query = new XQuery(
      "matches('qwerty','(ab){2,0}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00034() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(ab){0,0})$')) and (every $s in tokenize('a,ab', ',') satisfies not(matches($s, '^(?:(ab){0,0})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00035() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abcc,abccc,abbcc,abbccc,bbcc,bbccc', ',') satisfies matches($s, '^(?:a{0,1}b{1,2}c{2,3})$')) and (every $s in tokenize('aabcc,bbbcc,acc,aabcc,abbc,abbcccc', ',') satisfies not(matches($s, '^(?:a{0,1}b{1,2}c{2,3})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00036() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(',boy0xx,woman1y,girl1xymany,boy0xxwoman1ygirl1xymany,boy0xxwoman1ygirl1xymanyboy0xxwoman1ygirl1xymany', ',') satisfies matches($s, '^(?:(((((boy)|(girl))[0-1][x-z]{2})?)|(man|woman)[0-1]?[y|n])*)$')) and (every $s in tokenize('boy0xxwoman1ygirl1xyman,boyxx', ',') satisfies not(matches($s, '^(?:(((((boy)|(girl))[0-1][x-z]{2})?)|(man|woman)[0-1]?[y|n])*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00037() {
    final XQuery query = new XQuery(
      "matches('qwerty','((a)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00038() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00039() {
    final XQuery query = new XQuery(
      "matches('qwerty','ab|(d))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00040() {
    final XQuery query = new XQuery(
      "matches('qwerty','((a*(b*)((a))*(a))))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00041() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00042() {
    final XQuery query = new XQuery(
      "matches('qwerty','?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00043() {
    final XQuery query = new XQuery(
      "matches('qwerty','*')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00044() {
    final XQuery query = new XQuery(
      "matches('qwerty','+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00045() {
    final XQuery query = new XQuery(
      "matches('qwerty','(')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00046() {
    final XQuery query = new XQuery(
      "matches('qwerty',')')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00047() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:|)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:|)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00048() {
    final XQuery query = new XQuery(
      "matches('qwerty','[')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00049() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('.\\?*+{}[]()|', ',') satisfies matches($s, '^(?:\\.\\\\\\?\\*\\+\\{\\}\\[\\]\\(\\)\\|)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\.\\\\\\?\\*\\+\\{\\}\\[\\]\\(\\)\\|)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00050() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('.\\?*+{}[]()|.\\?*+{}[]()|.\\?*+{}[]()|', ',') satisfies matches($s, '^(?:(([\\.\\\\\\?\\*\\+\\{\\}\\[\\]\\(\\)\\|]?)*)+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([\\.\\\\\\?\\*\\+\\{\\}\\[\\]\\(\\)\\|]?)*)+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00051() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1z', ',') satisfies matches($s, '^(?:[^2-9a-x]{2})$')) and (every $s in tokenize('1x', ',') satisfies not(matches($s, '^(?:[^2-9a-x]{2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00052() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abc', ',') satisfies matches($s, '^(?:[^\\s]{3})$')) and (every $s in tokenize('a c', ',') satisfies not(matches($s, '^(?:[^\\s]{3})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00053() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(',a,ab, a', ',') satisfies matches($s, '^(?:[^@]{0,2})$')) and (every $s in tokenize('@', ',') satisfies not(matches($s, '^(?:[^@]{0,2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00054() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[^-z]+)$')) and (every $s in tokenize('aaz,a-z', ',') satisfies not(matches($s, '^(?:[^-z]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00055() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-d-[b-c]])$')) and (every $s in tokenize('b,c', ',') satisfies not(matches($s, '^(?:[a-d-[b-c]])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00056() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[^a-d-b-c])$')) and (every $s in tokenize('a-b,c-c,ab,cc', ',') satisfies not(matches($s, '^(?:[^a-d-b-c])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00057() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abcxyz}', ',') satisfies matches($s, '^(?:[a-\\}]+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[a-\\}]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00058() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-b-[0-9]]+)$')) and (every $s in tokenize('a1', ',') satisfies not(matches($s, '^(?:[a-b-[0-9]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00059() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-c-[^a-c]])$')) and (every $s in tokenize('d', ',') satisfies not(matches($s, '^(?:[a-c-[^a-c]])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00060() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-z-[^a]])$')) and (every $s in tokenize('b', ',') satisfies not(matches($s, '^(?:[a-z-[^a]])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00061() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Ā', ',') satisfies matches($s, '^(?:[^\\p{IsBasicLatin}]+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[^\\p{IsBasicLatin}]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00062() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[^\\p{IsBasicLatin}]*)$')) and (every $s in tokenize('a', ',') satisfies not(matches($s, '^(?:[^\\p{IsBasicLatin}]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00063() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a', ',') satisfies matches($s, '^(?:[^\\P{IsBasicLatin}])$')) and (every $s in tokenize('Ā', ',') satisfies not(matches($s, '^(?:[^\\P{IsBasicLatin}])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00064() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[^\\?])$')) and (every $s in tokenize('?', ',') satisfies not(matches($s, '^(?:[^\\?])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00065() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a+*abc', ',') satisfies matches($s, '^(?:([^\\?])*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([^\\?])*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00066() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a*a', ',') satisfies matches($s, '^(?:\\c[^\\d]\\c)$')) and (every $s in tokenize('aa', ',') satisfies not(matches($s, '^(?:\\c[^\\d]\\c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00067() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\c[^\\s]\\c)$')) and (every $s in tokenize('a c,a\rz,a\n" +
      "c,a\tr', ',') satisfies not(matches($s, '^(?:\\c[^\\s]\\c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00068() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[^\\^a])$')) and (every $s in tokenize('^,a', ',') satisfies not(matches($s, '^(?:[^\\^a])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00069() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abc', ',') satisfies matches($s, '^(?:[a-abc]{3})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[a-abc]{3})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00070() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('}-', ',') satisfies matches($s, '^(?:[a-\\}-]+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[a-\\}-]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00071() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a--b]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00072() {
    final XQuery query = new XQuery(
      "matches('qwerty','[^[a-b]]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00073() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a])$')) and (every $s in tokenize('b,', ',') satisfies not(matches($s, '^(?:[a])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00074() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('123', ',') satisfies matches($s, '^(?:[1-3]{1,4})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[1-3]{1,4})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00075() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a', ',') satisfies matches($s, '^(?:[a-a])$')) and (every $s in tokenize('b', ',') satisfies not(matches($s, '^(?:[a-a])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00076() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1234567890:;<=>?@Azaz', ',') satisfies matches($s, '^(?:[0-z]*)$')) and (every $s in tokenize('{,/', ',') satisfies not(matches($s, '^(?:[0-z]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00077() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\n" +
      "', ',') satisfies matches($s, '^(?:[\\n])$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\n])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00078() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\t', ',') satisfies matches($s, '^(?:[\\t])$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\t])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00079() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\|.?*+(){}-[]^', ',') satisfies matches($s, '^(?:[\\\\\\|\\.\\?\\*\\+\\(\\)\\{\\}\\-\\[\\]\\^]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\\\\\|\\.\\?\\*\\+\\(\\)\\{\\}\\-\\[\\]\\^]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00080() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[^a-z^])$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[^a-z^])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00081() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\\\-\\{^])$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\\\-\\{^])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00082() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('?a?,?b?,?c?', ',') satisfies matches($s, '^(?:[\\C\\?a-c\\?]+)$')) and (every $s in tokenize('?d?', ',') satisfies not(matches($s, '^(?:[\\C\\?a-c\\?]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00083() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('?', ',') satisfies matches($s, '^(?:[\\c\\?a-c\\?]+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\c\\?a-c\\?]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00084() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\D\\?a-c\\?]+)$')) and (every $s in tokenize('?1?', ',') satisfies not(matches($s, '^(?:[\\D\\?a-c\\?]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00085() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\S\\?a-c\\?]+)$')) and (every $s in tokenize('? ?,?\t?', ',') satisfies not(matches($s, '^(?:[\\S\\?a-c\\?]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00086() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(',a-1x-7,c-4z-9,a-1z-8a-1z-9,a1z-9,a-1z8,a-1,z-9', ',') satisfies matches($s, '^(?:[a-c-1-4x-z-7-9]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[a-c-1-4x-z-7-9]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00087() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a-\\\\]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00088() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a-\\[]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00089() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a*a****aaaaa*', ',') satisfies matches($s, '^(?:[\\*a]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\*a]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00090() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a-;]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00091() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1]', ',') satisfies matches($s, '^(?:[1-\\]]+)$')) and (every $s in tokenize('0,^', ',') satisfies not(matches($s, '^(?:[1-\\]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00092() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('=,>', ',') satisfies matches($s, '^(?:[=->])$')) and (every $s in tokenize('\\?', ',') satisfies not(matches($s, '^(?:[=->])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00093() {
    final XQuery query = new XQuery(
      "matches('qwerty','[>-=]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00094() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('@', ',') satisfies matches($s, '^(?:[@])$')) and (every $s in tokenize('a', ',') satisfies not(matches($s, '^(?:[@])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00095() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0fff', ',') satisfies matches($s, '^(?:[\u0fff])$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\u0fff])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00096() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('𐀀', ',') satisfies matches($s, '^(?:[𐀀])$')) and (every $s in tokenize('𐀁', ',') satisfies not(matches($s, '^(?:[𐀀])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00097() {
    final XQuery query = new XQuery(
      "matches('qwerty','[\\]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00098() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\,[,],\\[,\\[],[],[\\\\,\\]\\,[][', ',') satisfies matches($s, '^(?:[\\\\\\[\\]]{0,3})$')) and (every $s in tokenize('\\[][,\\]\\],[][]', ',') satisfies not(matches($s, '^(?:[\\\\\\[\\]]{0,3})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00099() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('-', ',') satisfies matches($s, '^(?:[-])$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[-])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00100() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a--aa---', ',') satisfies matches($s, '^(?:[-a]+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[-a]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00101() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a--aa---', ',') satisfies matches($s, '^(?:[a-]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[a-]*)$')))",
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
        assertBoolean(true)
      ||
        error("FORX0002")
      )
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00102() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a-x', ',') satisfies matches($s, '^(?:[a-a-x-x]+)$')) and (every $s in tokenize('j,a-b', ',') satisfies not(matches($s, '^(?:[a-a-x-x]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00103() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\|.-^?*+[]{}()*[[]{}}))\n" +
      "\r\t\t\n" +
      "\n" +
      "\r*()', ',') satisfies matches($s, '^(?:[\\n\\r\\t\\\\\\|\\.\\-\\^\\?\\*\\+\\{\\}\\[\\]\\(\\)]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\n\\r\\t\\\\\\|\\.\\-\\^\\?\\*\\+\\{\\}\\[\\]\\(\\)]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00104() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a**,aa*,a', ',') satisfies matches($s, '^(?:[a\\*]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[a\\*]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00105() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a?,a?a?a?,a,a??,aa?', ',') satisfies matches($s, '^(?:[(a\\?)?]+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[(a\\?)?]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00106() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\t', ',') satisfies matches($s, '^(?:\\\\t)$')) and (every $s in tokenize('t,\\\\t,\t', ',') satisfies not(matches($s, '^(?:\\\\t)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00107() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\n', ',') satisfies matches($s, '^(?:\\\\n)$')) and (every $s in tokenize('n,\\\\n,\n" +
      "', ',') satisfies not(matches($s, '^(?:\\\\n)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00108() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\r', ',') satisfies matches($s, '^(?:\\\\r)$')) and (every $s in tokenize('r,\\\\r,\r', ',') satisfies not(matches($s, '^(?:\\\\r)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00109() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\n" +
      "', ',') satisfies matches($s, '^(?:\\n)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\n)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00110() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\t', ',') satisfies matches($s, '^(?:\\t)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\t)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00111() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\', ',') satisfies matches($s, '^(?:\\\\)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\\\)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00112() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('|', ',') satisfies matches($s, '^(?:\\|)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\|)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00113() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('.', ',') satisfies matches($s, '^(?:\\.)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\.)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00114() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('-', ',') satisfies matches($s, '^(?:\\-)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\-)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00115() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('^', ',') satisfies matches($s, '^(?:\\^)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\^)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00116() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('?', ',') satisfies matches($s, '^(?:\\?)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00117() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('*', ',') satisfies matches($s, '^(?:\\*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00118() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('+', ',') satisfies matches($s, '^(?:\\+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00119() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('{', ',') satisfies matches($s, '^(?:\\{)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\{)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00120() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('}', ',') satisfies matches($s, '^(?:\\})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00121() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('(', ',') satisfies matches($s, '^(?:\\()$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\()$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00122() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(')', ',') satisfies matches($s, '^(?:\\))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00123() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('[', ',') satisfies matches($s, '^(?:\\[)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\[)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00124() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(']', ',') satisfies matches($s, '^(?:\\])$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00125() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\n\\\\\\r\\|\\t\\.\\-\\^\\?\\*\\+\\{\\}\\(\\)\\[\\])$')) and (every $s in tokenize('\n" +
      "\\\r|\t.-^?*+{}()[,\\\r|\t.-^?*+{}()[],\n" +
      "\\\r|\t-^?*+{}()[]', ',') satisfies not(matches($s, '^(?:\\n\\\\\\r\\|\\t\\.\\-\\^\\?\\*\\+\\{\\}\\(\\)\\[\\])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00126() {
    final XQuery query = new XQuery(
      "not(matches('', '^(?:\\n\\na\\n\\nb\\n\\n)$')) and\n" +
      "         (every $s in tokenize('\n" +
      "\n" +
      "a\n" +
      "\n" +
      "b;\n" +
      ",\n" +
      "a\n" +
      "\n" +
      "b;\n" +
      "\n" +
      ",\n" +
      "\n" +
      "a\n" +
      "\n" +
      "b;\n" +
      "\r', ',') \n" +
      "                satisfies not(matches($s, '^(?:\\n\\na\\n\\nb\\n\\n)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00127a() {
    final XQuery query = new XQuery(
      "\n" +
      "        matches('&#xD;&#xD;a&#xD;&#xD;b&#xD;&#xD;',         '^\\r\\ra\\r\\rb\\r\\r$'),\n" +
      "        matches('&#xD;&#xD;a&#xD;&#xD;b&#xD;&#xD;',         '^\\r\\ra\\r\\rb\\r\\r$'),\n" +
      "        matches('&#xD;&#xD;a&#xD;&#xD;b&#xD;',              '^(?:\\r\\ra\\r\\rb\\r\\r)$'),\n" +
      "        matches('&#xD;a&#xD;&#xD;b&#xD;&#xD;',              '^(?:\\r\\ra\\r\\rb\\r\\r)$'),\n" +
      "        matches('&#xD;&#xD;a&#xD;&#xD;&#xD;&#xD;',          '^(?:\\r\\ra\\r\\rb\\r\\r)$'),\n" +
      "        matches('&#xD;&#xD;a&#xD;&#xA;&#xD;b&#xD;&#xD;',    '^(?:\\r\\ra\\r\\rb\\r\\r)$')\n" +
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
      assertDeepEq("true(), true(), false(), false(), false(), false()")
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00128() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\t\\ta\\t\\tb\\t\\t)$')) and (every $s in tokenize('\t\ta\t\tb\t,\ta\t\tb\t\t,\t\ta\t\t\t\t,\t\ta\t\t\tb\t\t', ',') satisfies not(matches($s, '^(?:\\t\\ta\\t\\tb\\t\\t)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00129a() {
    final XQuery query = new XQuery(
      "matches('a&#xD;&#xA;b', '^(?:a\\r\\nb)$'), matches('ab', '^(?:a\\r\\nb)$')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("true(), false()")
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00130a() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('&#xA;&#xD;a&#xA;&#xD;b', ',') satisfies (matches($s, '^(?:\\n\\ra\\n\\rb)$'))) and\n" +
      "         (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\n\\ra\\n\\rb)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00131() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\ta\tb\tc\t', ',') satisfies matches($s, '^(?:\\ta\\tb\\tc\\t)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\ta\\tb\\tc\\t)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00132() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\n" +
      "a\n" +
      "b\n" +
      "c\n" +
      "', ',') satisfies matches($s, '^(?:\\na\\nb\\nc\\n)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\na\\nb\\nc\\n)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00133() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\ta \n" +
      "\r\n" +
      " \r\tb, a  b, a  b ,\ta \n" +
      "\r\n" +
      " \rb', ',') satisfies matches($s, '^(?:(\\t|\\s)a(\\r\\n|\\r|\\n|\\s)+(\\s|\\t)b(\\s|\\r\\n|\\r|\\n)*)$')) and (every $s in tokenize(' a b, a b ', ',') satisfies not(matches($s, '^(?:(\\t|\\s)a(\\r\\n|\\r|\\n|\\s)+(\\s|\\t)b(\\s|\\r\\n|\\r|\\n)*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00134() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\c', ',') satisfies matches($s, '^(?:\\\\c)$')) and (every $s in tokenize('\\p{_xmlC},\\\\c,\\\\', ',') satisfies not(matches($s, '^(?:\\\\c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00135() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\.,\\s,\\S,\\i,\\I,\\c,\\C,\\d,\\D,\\w,\\W', ';') satisfies matches($s, '^(?:\\\\.,\\\\s,\\\\S,\\\\i,\\\\I,\\\\c,\\\\C,\\\\d,\\\\D,\\\\w,\\\\W)$')) and (every $s in tokenize('', ';') satisfies not(matches($s, '^(?:\\\\.,\\\\s,\\\\S,\\\\i,\\\\I,\\\\c,\\\\C,\\\\d,\\\\D,\\\\w,\\\\W)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00136() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\.abcd,\\sssss,\\SSSSSS,\\iiiiiii,\\,\\c,\\CCCCCC,\\ddd,\\D,\\wwwwwww,\\WWW', ';') satisfies matches($s, '^(?:\\\\.*,\\\\s*,\\\\S*,\\\\i*,\\\\I?,\\\\c+,\\\\C+,\\\\d{0,3},\\\\D{1,1000},\\\\w*,\\\\W+)$')) and (every $s in tokenize('', ';') satisfies not(matches($s, '^(?:\\\\.*,\\\\s*,\\\\S*,\\\\i*,\\\\I?,\\\\c+,\\\\C+,\\\\d{0,3},\\\\D{1,1000},\\\\w*,\\\\W+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00137() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('aX', ',') satisfies matches($s, '^(?:[\\p{L}*]{0,2})$')) and (every $s in tokenize('aBC', ',') satisfies not(matches($s, '^(?:[\\p{L}*]{0,2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00138() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(\\p{Ll}\\p{Cc}\\p{Nd})*)$')) and (every $s in tokenize('\u1680', ',') satisfies not(matches($s, '^(?:(\\p{Ll}\\p{Cc}\\p{Nd})*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00139() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{L}*)$')) and (every $s in tokenize('⃝', ',') satisfies not(matches($s, '^(?:\\p{L}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00140() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('A𝞨', ',') satisfies matches($s, '^(?:\\p{Lu}*)$')) and (every $s in tokenize('a', ',') satisfies not(matches($s, '^(?:\\p{Lu}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00141() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a𝟉', ',') satisfies matches($s, '^(?:\\p{Ll}*)$')) and (every $s in tokenize('ǅ', ',') satisfies not(matches($s, '^(?:\\p{Ll}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00142() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ǅῼ', ',') satisfies matches($s, '^(?:\\p{Lt}*)$')) and (every $s in tokenize('ʰ', ',') satisfies not(matches($s, '^(?:\\p{Lt}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00143() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ʰﾟ', ',') satisfies matches($s, '^(?:\\p{Lm}*)$')) and (every $s in tokenize('א', ',') satisfies not(matches($s, '^(?:\\p{Lm}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00144() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('א𪘀', ',') satisfies matches($s, '^(?:\\p{Lo}*)$')) and (every $s in tokenize('ً', ',') satisfies not(matches($s, '^(?:\\p{Lo}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00145() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ً𝆭ः𝅲ः𝅲⃝⃝⃠', ',') satisfies matches($s, '^(?:\\p{M}*)$')) and (every $s in tokenize('ǅ', ',') satisfies not(matches($s, '^(?:\\p{M}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00146() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ً𝆭', ',') satisfies matches($s, '^(?:\\p{Mn}*)$')) and (every $s in tokenize('ः', ',') satisfies not(matches($s, '^(?:\\p{Mn}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00147() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ः𝅲', ',') satisfies matches($s, '^(?:\\p{Mc}*)$')) and (every $s in tokenize('⃝', ',') satisfies not(matches($s, '^(?:\\p{Mc}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00148() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⃝⃠', ',') satisfies matches($s, '^(?:\\p{Me}*)$')) and (every $s in tokenize('０', ',') satisfies not(matches($s, '^(?:\\p{Me}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00149() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('０𝟿𐍊𐍊〥²²𐌣', ',') satisfies matches($s, '^(?:\\p{N}*)$')) and (every $s in tokenize('ः', ',') satisfies not(matches($s, '^(?:\\p{N}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00150() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('０𝟿', ',') satisfies matches($s, '^(?:\\p{Nd}*)$')) and (every $s in tokenize('𐍊', ',') satisfies not(matches($s, '^(?:\\p{Nd}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00151() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('𐍊〥', ',') satisfies matches($s, '^(?:\\p{Nl}*)$')) and (every $s in tokenize('²', ',') satisfies not(matches($s, '^(?:\\p{Nl}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00152() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('²𐌣', ',') satisfies matches($s, '^(?:\\p{No}*)$')) and (every $s in tokenize('\u203f', ',') satisfies not(matches($s, '^(?:\\p{No}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00153() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u203f\uff65\u301c\u301c\uff0d\u301d\u301d\uff62\u301e\u301e\uff63««\u2039»»\u203a¿¿\uff64', ',') satisfies matches($s, '^(?:\\p{P}*)$')) and (every $s in tokenize('²', ',') satisfies not(matches($s, '^(?:\\p{P}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00154() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{Pc}*)$')) and (every $s in tokenize('\u301c', ',') satisfies not(matches($s, '^(?:\\p{Pc}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00155() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u301c\uff0d', ',') satisfies matches($s, '^(?:\\p{Pd}*)$')) and (every $s in tokenize('\u301d', ',') satisfies not(matches($s, '^(?:\\p{Pd}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00156() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u301d\uff62', ',') satisfies matches($s, '^(?:\\p{Ps}*)$')) and (every $s in tokenize('\u301e', ',') satisfies not(matches($s, '^(?:\\p{Ps}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00157() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u301e\uff63', ',') satisfies matches($s, '^(?:\\p{Pe}*)$')) and (every $s in tokenize('«', ',') satisfies not(matches($s, '^(?:\\p{Pe}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00158() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('«\u2039', ',') satisfies matches($s, '^(?:\\p{Pi}*)$')) and (every $s in tokenize('»', ',') satisfies not(matches($s, '^(?:\\p{Pi}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00159() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('»\u203a', ',') satisfies matches($s, '^(?:\\p{Pf}*)$')) and (every $s in tokenize('¿', ',') satisfies not(matches($s, '^(?:\\p{Pf}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00160() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('¿\uff64', ',') satisfies matches($s, '^(?:\\p{Po}*)$')) and (every $s in tokenize('\u1680', ',') satisfies not(matches($s, '^(?:\\p{Po}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00161() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u1680\u3000\u2028\u2028\u2029\u2029', ',') satisfies matches($s, '^(?:\\p{Z}*)$')) and (every $s in tokenize('¿', ',') satisfies not(matches($s, '^(?:\\p{Z}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00162() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u1680\u3000', ',') satisfies matches($s, '^(?:\\p{Zs}*)$')) and (every $s in tokenize('\u2028', ',') satisfies not(matches($s, '^(?:\\p{Zs}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00163() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u2028', ',') satisfies matches($s, '^(?:\\p{Zl}*)$')) and (every $s in tokenize('\u2029', ',') satisfies not(matches($s, '^(?:\\p{Zl}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00164() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u2029', ',') satisfies matches($s, '^(?:\\p{Zp}*)$')) and (every $s in tokenize('⁄', ',') satisfies not(matches($s, '^(?:\\p{Zp}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00165() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⁄￢₠₠￦゛゛￣㆐㆐𝇝', ',') satisfies matches($s, '^(?:\\p{S}*)$')) and (every $s in tokenize('\u1680', ',') satisfies not(matches($s, '^(?:\\p{S}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00166() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⁄￢', ',') satisfies matches($s, '^(?:\\p{Sm}*)$')) and (every $s in tokenize('₠', ',') satisfies not(matches($s, '^(?:\\p{Sm}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00167() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('₠￦', ',') satisfies matches($s, '^(?:\\p{Sc}*)$')) and (every $s in tokenize('゛', ',') satisfies not(matches($s, '^(?:\\p{Sc}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00168() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('゛￣', ',') satisfies matches($s, '^(?:\\p{Sk}*)$')) and (every $s in tokenize('㆐', ',') satisfies not(matches($s, '^(?:\\p{Sk}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00169() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('㆐𝇝', ',') satisfies matches($s, '^(?:\\p{So}*)$')) and (every $s in tokenize('\t', ',') satisfies not(matches($s, '^(?:\\p{So}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00170() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{C}*)$')) and (every $s in tokenize('₠', ',') satisfies not(matches($s, '^(?:\\p{C}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00171() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\t', ',') satisfies matches($s, '^(?:\\p{Cc}*)$')) and (every $s in tokenize('\u070f', ',') satisfies not(matches($s, '^(?:\\p{Cc}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00172() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u070f\udb40\udc78', ',') satisfies matches($s, '^(?:\\p{Cf}*)$')) and (every $s in tokenize('\ue000', ',') satisfies not(matches($s, '^(?:\\p{Cf}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00173() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\ue000\udbc0\udc00\udb80\udc00\udbbf\udffd\udbff\udffd', ',') satisfies matches($s, '^(?:(\\p{Co})*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\p{Co})*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00174() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{Co}*)$')) and (every $s in tokenize('⁄', ',') satisfies not(matches($s, '^(?:\\p{Co}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00175() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{Cn}*)$')) and (every $s in tokenize('\t', ',') satisfies not(matches($s, '^(?:\\p{Cn}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00176() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('_,⃝', ',') satisfies matches($s, '^(?:\\P{L}*)$')) and (every $s in tokenize('aAbB,A𝞨aa𝟉ǅǅῼʰʰﾟאא𪘀', ',') satisfies not(matches($s, '^(?:\\P{L}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00177() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(',#$', ',') satisfies matches($s, '^(?:[\\P{L}*]{0,2})$')) and (every $s in tokenize('!$#,A', ',') satisfies not(matches($s, '^(?:[\\P{L}*]{0,2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00178() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a', ',') satisfies matches($s, '^(?:\\P{Lu}*)$')) and (every $s in tokenize('A𝞨', ',') satisfies not(matches($s, '^(?:\\P{Lu}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00179() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ǅ', ',') satisfies matches($s, '^(?:\\P{Ll}*)$')) and (every $s in tokenize('a𝟉', ',') satisfies not(matches($s, '^(?:\\P{Ll}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00180() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ʰ', ',') satisfies matches($s, '^(?:\\P{Lt}*)$')) and (every $s in tokenize('ǅῼ', ',') satisfies not(matches($s, '^(?:\\P{Lt}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00181() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('א', ',') satisfies matches($s, '^(?:\\P{Lm}*)$')) and (every $s in tokenize('ʰﾟ', ',') satisfies not(matches($s, '^(?:\\P{Lm}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00182() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ً', ',') satisfies matches($s, '^(?:\\P{Lo}*)$')) and (every $s in tokenize('א𪘀', ',') satisfies not(matches($s, '^(?:\\P{Lo}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00183() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ǅ', ',') satisfies matches($s, '^(?:\\P{M}*)$')) and (every $s in tokenize('ً𝆭ः𝅲ः𝅲⃝⃝⃠', ',') satisfies not(matches($s, '^(?:\\P{M}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00184() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ः𝅲', ',') satisfies matches($s, '^(?:\\P{Mn}*)$')) and (every $s in tokenize('ً𝆭', ',') satisfies not(matches($s, '^(?:\\P{Mn}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00185() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⃝', ',') satisfies matches($s, '^(?:\\P{Mc}*)$')) and (every $s in tokenize('ः𝅲', ',') satisfies not(matches($s, '^(?:\\P{Mc}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00186() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('０', ',') satisfies matches($s, '^(?:\\P{Me}*)$')) and (every $s in tokenize('⃝⃠', ',') satisfies not(matches($s, '^(?:\\P{Me}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00187() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ः', ',') satisfies matches($s, '^(?:\\P{N}*)$')) and (every $s in tokenize('０𝟿𐍊𐍊〥²²𐌣', ',') satisfies not(matches($s, '^(?:\\P{N}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00188() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('𐍊', ',') satisfies matches($s, '^(?:\\P{Nd}*)$')) and (every $s in tokenize('０𝟿', ',') satisfies not(matches($s, '^(?:\\P{Nd}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00189() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('²', ',') satisfies matches($s, '^(?:\\P{Nl}*)$')) and (every $s in tokenize('𐍊〥', ',') satisfies not(matches($s, '^(?:\\P{Nl}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00190() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u203f', ',') satisfies matches($s, '^(?:\\P{No}*)$')) and (every $s in tokenize('²𐌣', ',') satisfies not(matches($s, '^(?:\\P{No}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00191() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('²', ',') satisfies matches($s, '^(?:\\P{P}*)$')) and (every $s in tokenize('\u203f\uff65\u301c\u301c\uff0d\u301d\u301d\uff62\u301e\u301e\uff63««\u2039»»\u203a¿¿\uff64', ',') satisfies not(matches($s, '^(?:\\P{P}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00192() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u301c', ',') satisfies matches($s, '^(?:\\P{Pc}*)$')) and (every $s in tokenize('\u203f\uff65', ',') satisfies not(matches($s, '^(?:\\P{Pc}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00193() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u301d', ',') satisfies matches($s, '^(?:\\P{Pd}*)$')) and (every $s in tokenize('\u301c\uff0d', ',') satisfies not(matches($s, '^(?:\\P{Pd}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00194() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u301e', ',') satisfies matches($s, '^(?:\\P{Ps}*)$')) and (every $s in tokenize('\u301d\uff62', ',') satisfies not(matches($s, '^(?:\\P{Ps}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00195() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('«', ',') satisfies matches($s, '^(?:\\P{Pe}*)$')) and (every $s in tokenize('\u301e\uff63', ',') satisfies not(matches($s, '^(?:\\P{Pe}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00196() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('»', ',') satisfies matches($s, '^(?:\\P{Pi}*)$')) and (every $s in tokenize('«\u2039', ',') satisfies not(matches($s, '^(?:\\P{Pi}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00197() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('¿', ',') satisfies matches($s, '^(?:\\P{Pf}*)$')) and (every $s in tokenize('»\u203a', ',') satisfies not(matches($s, '^(?:\\P{Pf}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00198() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u1680', ',') satisfies matches($s, '^(?:\\P{Po}*)$')) and (every $s in tokenize('¿\uff64', ',') satisfies not(matches($s, '^(?:\\P{Po}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00199() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('¿', ',') satisfies matches($s, '^(?:\\P{Z}*)$')) and (every $s in tokenize('\u1680\u3000\u2028\u2028\u2029\u2029', ',') satisfies not(matches($s, '^(?:\\P{Z}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00200() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u2028', ',') satisfies matches($s, '^(?:\\P{Zs}*)$')) and (every $s in tokenize('\u1680\u3000', ',') satisfies not(matches($s, '^(?:\\P{Zs}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00201() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u2029', ',') satisfies matches($s, '^(?:\\P{Zl}*)$')) and (every $s in tokenize('\u2028', ',') satisfies not(matches($s, '^(?:\\P{Zl}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00202() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⁄', ',') satisfies matches($s, '^(?:\\P{Zp}*)$')) and (every $s in tokenize('\u2029', ',') satisfies not(matches($s, '^(?:\\P{Zp}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00203() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u1680', ',') satisfies matches($s, '^(?:\\P{S}*)$')) and (every $s in tokenize('⁄￢₠₠￦゛゛￣㆐㆐𝇝', ',') satisfies not(matches($s, '^(?:\\P{S}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00204() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('₠', ',') satisfies matches($s, '^(?:\\P{Sm}*)$')) and (every $s in tokenize('⁄￢', ',') satisfies not(matches($s, '^(?:\\P{Sm}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00205() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('゛', ',') satisfies matches($s, '^(?:\\P{Sc}*)$')) and (every $s in tokenize('₠￦', ',') satisfies not(matches($s, '^(?:\\P{Sc}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00206() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('㆐', ',') satisfies matches($s, '^(?:\\P{Sk}*)$')) and (every $s in tokenize('゛￣', ',') satisfies not(matches($s, '^(?:\\P{Sk}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00207() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\t', ',') satisfies matches($s, '^(?:\\P{So}*)$')) and (every $s in tokenize('㆐𝇝', ',') satisfies not(matches($s, '^(?:\\P{So}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00208() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('₠', ',') satisfies matches($s, '^(?:\\P{C}*)$')) and (every $s in tokenize('\t\u070f\u070f\udb40\udc78\ue000\ue000\udbc0\udc00\udb80\udc00\udbbf\udffd\udbff\udffd', ',') satisfies not(matches($s, '^(?:\\P{C}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00209() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u070f', ',') satisfies matches($s, '^(?:\\P{Cc}*)$')) and (every $s in tokenize('\t', ',') satisfies not(matches($s, '^(?:\\P{Cc}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00210() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\ue000', ',') satisfies matches($s, '^(?:\\P{Cf}*)$')) and (every $s in tokenize('\u070f\udb40\udc78', ',') satisfies not(matches($s, '^(?:\\P{Cf}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00211() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⁄', ',') satisfies matches($s, '^(?:\\P{Co}*)$')) and (every $s in tokenize('\ue000\udbc0\udc00\udb80\udc00\udbbf\udffd\udbff\udffd', ',') satisfies not(matches($s, '^(?:\\P{Co}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00212() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\p{\\\\L}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00213() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\\a', ',') satisfies matches($s, '^(?:\\\\\\p{L}*)$')) and (every $s in tokenize('a', ',') satisfies not(matches($s, '^(?:\\\\\\p{L}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00214() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\p{Is}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00215() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\P{Is}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00216() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\p{IsaA0-a9}')",
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
        error("FORX0002")
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00217() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\t\n" +
      "\r \u007f\ue001\t\n" +
      "\r !\"#$%''''()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u007f', '\ue001') satisfies matches($s, '^(?:\\p{IsBasicLatin}+)$')) and (every $s in tokenize('', '\ue001') satisfies not(matches($s, '^(?:\\p{IsBasicLatin}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00218() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0080ÿ,\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008a\u008b\u008c\u008d\u008e\u008f\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009a\u009b\u009c\u009d\u009e\u009f ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ', ',') satisfies matches($s, '^(?:\\p{IsLatin-1Supplement}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsLatin-1Supplement}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00219() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Āſ,ĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİıĲĳĴĵĶķĸĹĺĻļĽľĿŀŁłŃńŅņŇňŉŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŦŧŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſ', ',') satisfies matches($s, '^(?:\\p{IsLatinExtended-A}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtended-A}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00220() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ƀɏ,ƀƁƂƃƄƅƆƇƈƉƊƋƌƍƎƏƐƑƒƓƔƕƖƗƘƙƚƛƜƝƞƟƠơƢƣƤƥƦƧƨƩƪƫƬƭƮƯưƱƲƳƴƵƶƷƸƹƺƻƼƽƾƿǀǁǂǃǄǅǆǇǈǉǊǋǌǍǎǏǐǑǒǓǔǕǖǗǘǙǚǛǜǝǞǟǠǡǢǣǤǥǦǧǨǩǪǫǬǭǮǯǰǱǲǳǴǵǶǷǸǹǺǻǼǽǾǿȀȁȂȃȄȅȆȇȈȉȊȋȌȍȎȏȐȑȒȓȔȕȖȗȘșȚțȜȝȞȟȠȡȢȣȤȥȦȧȨȩȪȫȬȭȮȯȰȱȲȳȴȵȶȷȸȹȺȻȼȽȾȿɀɁɂɃɄɅɆɇɈɉɊɋɌɍɎɏ', ',') satisfies matches($s, '^(?:\\p{IsLatinExtended-B}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtended-B}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00221() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ɐʯ,ɐɑɒɓɔɕɖɗɘəɚɛɜɝɞɟɠɡɢɣɤɥɦɧɨɩɪɫɬɭɮɯɰɱɲɳɴɵɶɷɸɹɺɻɼɽɾɿʀʁʂʃʄʅʆʇʈʉʊʋʌʍʎʏʐʑʒʓʔʕʖʗʘʙʚʛʜʝʞʟʠʡʢʣʤʥʦʧʨʩʪʫʬʭʮʯ', ',') satisfies matches($s, '^(?:\\p{IsIPAExtensions}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsIPAExtensions}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00222() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ʰ˿,ʰʱʲʳʴʵʶʷʸʹʺʻʼʽʾʿˀˁ˂˃˄˅ˆˇˈˉˊˋˌˍˎˏːˑ˒˓˔˕˖˗˘˙˚˛˜˝˞˟ˠˡˢˣˤ˥˦˧˨˩˪˫ˬ˭ˮ˯˰˱˲˳˴˵˶˷˸˹˺˻˼˽˾˿', ',') satisfies matches($s, '^(?:\\p{IsSpacingModifierLetters}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsSpacingModifierLetters}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00223() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0530\u058f,\u0530ԱԲԳԴԵԶԷԸԹԺԻԼԽԾԿՀՁՂՃՄՅՆՇՈՉՊՋՌՍՎՏՐՑՒՓՔՕՖ\u0557\u0558ՙ\u055a\u055b\u055c\u055d\u055e\u055f\u0560աբգդեզէըթժիլխծկհձղճմյնշոչպջռսվտրցւփքօֆև\u0588\u0589\u058a\u058b\u058c\u058d\u058e\u058f', ',') satisfies matches($s, '^(?:\\p{IsArmenian}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsArmenian}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00224() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0590\u05ff,\u0590ְֱֲֳִֵֶַָֹֺֻּֽ֑֖֛֢֣֤֥֦֧֪֚֭֮֒֓֔֕֗֘֙֜֝֞֟֠֡֨֩֫֬֯\u05beֿ\u05c0ׁׂ\u05c3ׅׄ\u05c6ׇ\u05c8\u05c9\u05ca\u05cb\u05cc\u05cd\u05ce\u05cfאבגדהוזחטיךכלםמןנסעףפץצקרשת\u05eb\u05ec\u05ed\u05ee\u05efװױײ\u05f3\u05f4\u05f5\u05f6\u05f7\u05f8\u05f9\u05fa\u05fb\u05fc\u05fd\u05fe\u05ff', ',') satisfies matches($s, '^(?:\\p{IsHebrew}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsHebrew}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00225() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0600ۿ,\u0600\u0601\u0602\u0603\u0604\u0605؆؇؈\u0609\u060a؋\u060c\u060d؎؏ؘؙؚؐؑؒؓؔؕؖؗ\u061b\u061c\u061d\u061e\u061fؠءآأؤإئابةتثجحخدذرزسشصضطظعغػؼؽؾؿـفقكلمنهوىيًٌٍَُِّْٕٖٜٟٓٔٗ٘ٙٚٛٝٞ٠١٢٣٤٥٦٧٨٩\u066a\u066b\u066c\u066dٮٯٰٱٲٳٴٵٶٷٸٹٺٻټٽپٿڀځڂڃڄڅچڇڈډڊڋڌڍڎڏڐڑڒړڔڕږڗژڙښڛڜڝڞڟڠڡڢڣڤڥڦڧڨکڪګڬڭڮگڰڱڲڳڴڵڶڷڸڹںڻڼڽھڿۀہۂۃۄۅۆۇۈۉۊۋیۍێۏېۑےۓ\u06d4ەۖۗۘۙۚۛۜ\u06dd۞ۣ۟۠ۡۢۤۥۦۧۨ۩۪ۭ۫۬ۮۯ۰۱۲۳۴۵۶۷۸۹ۺۻۼ۽۾ۿ', ',') satisfies matches($s, '^(?:\\p{IsArabic}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsArabic}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00226() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0700ݏ,\u0700\u0701\u0702\u0703\u0704\u0705\u0706\u0707\u0708\u0709\u070a\u070b\u070c\u070d\u070e\u070fܐܑܒܓܔܕܖܗܘܙܚܛܜܝܞܟܠܡܢܣܤܥܦܧܨܩܪܫܬܭܮܯܱܴܷܸܹܻܼܾ݂݄݆݈ܰܲܳܵܶܺܽܿ݀݁݃݅݇݉݊\u074b\u074cݍݎݏ', ',') satisfies matches($s, '^(?:\\p{IsSyriac}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsSyriac}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00227() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ހ\u07bf,ހށނރބޅކއވމފދތލގޏސޑޒޓޔޕޖޗޘޙޚޛޜޝޞޟޠޡޢޣޤޥަާިީުޫެޭޮޯްޱ\u07b2\u07b3\u07b4\u07b5\u07b6\u07b7\u07b8\u07b9\u07ba\u07bb\u07bc\u07bd\u07be\u07bf', ',') satisfies matches($s, '^(?:\\p{IsThaana}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsThaana}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00228() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ऀॿ,ऀँंःऄअआइईउऊऋऌऍऎएऐऑऒओऔकखगघङचछजझञटठडढणतथदधनऩपफबभमयरऱलळऴवशषसहऺऻ़ऽािीुूृॄॅॆेैॉॊोौ्ॎॏॐ॒॑॓॔ॕॖॗक़ख़ग़ज़ड़ढ़फ़य़ॠॡॢॣ\u0964\u0965०१२३४५६७८९\u0970ॱॲॳॴॵॶॷ\u0978ॹॺॻॼॽॾॿ', ',') satisfies matches($s, '^(?:\\p{IsDevanagari}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsDevanagari}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00229() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0980\u09ff,\u0980ঁংঃ\u0984অআইঈউঊঋঌ\u098d\u098eএঐ\u0991\u0992ওঔকখগঘঙচছজঝঞটঠডঢণতথদধন\u09a9পফবভমযর\u09b1ল\u09b3\u09b4\u09b5শষসহ\u09ba\u09bb়ঽািীুূৃৄ\u09c5\u09c6েৈ\u09c9\u09caোৌ্ৎ\u09cf\u09d0\u09d1\u09d2\u09d3\u09d4\u09d5\u09d6ৗ\u09d8\u09d9\u09da\u09dbড়ঢ়\u09deয়ৠৡৢৣ\u09e4\u09e5০১২৩৪৫৬৭৮৯ৰৱ৲৳৴৵৶৷৸৹৺৻\u09fc\u09fd\u09fe\u09ff', ',') satisfies matches($s, '^(?:\\p{IsBengali}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsBengali}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00230() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0a00\u0a7f,\u0a00ਁਂਃ\u0a04ਅਆਇਈਉਊ\u0a0b\u0a0c\u0a0d\u0a0eਏਐ\u0a11\u0a12ਓਔਕਖਗਘਙਚਛਜਝਞਟਠਡਢਣਤਥਦਧਨ\u0a29ਪਫਬਭਮਯਰ\u0a31ਲਲ਼\u0a34ਵਸ਼\u0a37ਸਹ\u0a3a\u0a3b਼\u0a3dਾਿੀੁੂ\u0a43\u0a44\u0a45\u0a46ੇੈ\u0a49\u0a4aੋੌ੍\u0a4e\u0a4f\u0a50ੑ\u0a52\u0a53\u0a54\u0a55\u0a56\u0a57\u0a58ਖ਼ਗ਼ਜ਼ੜ\u0a5dਫ਼\u0a5f\u0a60\u0a61\u0a62\u0a63\u0a64\u0a65੦੧੨੩੪੫੬੭੮੯ੰੱੲੳੴੵ\u0a76\u0a77\u0a78\u0a79\u0a7a\u0a7b\u0a7c\u0a7d\u0a7e\u0a7f', ',') satisfies matches($s, '^(?:\\p{IsGurmukhi}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsGurmukhi}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00231() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0a80\u0aff,\u0a80ઁંઃ\u0a84અઆઇઈઉઊઋઌઍ\u0a8eએઐઑ\u0a92ઓઔકખગઘઙચછજઝઞટઠડઢણતથદધન\u0aa9પફબભમયર\u0ab1લળ\u0ab4વશષસહ\u0aba\u0abb઼ઽાિીુૂૃૄૅ\u0ac6ેૈૉ\u0acaોૌ્\u0ace\u0acfૐ\u0ad1\u0ad2\u0ad3\u0ad4\u0ad5\u0ad6\u0ad7\u0ad8\u0ad9\u0ada\u0adb\u0adc\u0add\u0ade\u0adfૠૡૢૣ\u0ae4\u0ae5૦૧૨૩૪૫૬૭૮૯\u0af0૱\u0af2\u0af3\u0af4\u0af5\u0af6\u0af7\u0af8\u0af9\u0afa\u0afb\u0afc\u0afd\u0afe\u0aff', ',') satisfies matches($s, '^(?:\\p{IsGujarati}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsGujarati}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00232() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0b00\u0b7f,\u0b00ଁଂଃ\u0b04ଅଆଇଈଉଊଋଌ\u0b0d\u0b0eଏଐ\u0b11\u0b12ଓଔକଖଗଘଙଚଛଜଝଞଟଠଡଢଣତଥଦଧନ\u0b29ପଫବଭମଯର\u0b31ଲଳ\u0b34ଵଶଷସହ\u0b3a\u0b3b଼ଽାିୀୁୂୃୄ\u0b45\u0b46େୈ\u0b49\u0b4aୋୌ୍\u0b4e\u0b4f\u0b50\u0b51\u0b52\u0b53\u0b54\u0b55ୖୗ\u0b58\u0b59\u0b5a\u0b5bଡ଼ଢ଼\u0b5eୟୠୡୢୣ\u0b64\u0b65୦୧୨୩୪୫୬୭୮୯୰ୱ୲୳୴୵୶୷\u0b78\u0b79\u0b7a\u0b7b\u0b7c\u0b7d\u0b7e\u0b7f', ',') satisfies matches($s, '^(?:\\p{IsOriya}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsOriya}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00233() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0b80\u0bff,\u0b80\u0b81ஂஃ\u0b84அஆஇஈஉஊ\u0b8b\u0b8c\u0b8dஎஏஐ\u0b91ஒஓஔக\u0b96\u0b97\u0b98ஙச\u0b9bஜ\u0b9dஞட\u0ba0\u0ba1\u0ba2ணத\u0ba5\u0ba6\u0ba7நனப\u0bab\u0bac\u0badமயரறலளழவஶஷஸஹ\u0bba\u0bbb\u0bbc\u0bbdாிீுூ\u0bc3\u0bc4\u0bc5ெேை\u0bc9ொோௌ்\u0bce\u0bcfௐ\u0bd1\u0bd2\u0bd3\u0bd4\u0bd5\u0bd6ௗ\u0bd8\u0bd9\u0bda\u0bdb\u0bdc\u0bdd\u0bde\u0bdf\u0be0\u0be1\u0be2\u0be3\u0be4\u0be5௦௧௨௩௪௫௬௭௮௯௰௱௲௳௴௵௶௷௸௹௺\u0bfb\u0bfc\u0bfd\u0bfe\u0bff', ',') satisfies matches($s, '^(?:\\p{IsTamil}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsTamil}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00234() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0c00౿,\u0c00ఁంః\u0c04అఆఇఈఉఊఋఌ\u0c0dఎఏఐ\u0c11ఒఓఔకఖగఘఙచఛజఝఞటఠడఢణతథదధన\u0c29పఫబభమయరఱలళ\u0c34వశషసహ\u0c3a\u0c3b\u0c3cఽాిీుూృౄ\u0c45ెేై\u0c49ొోౌ్\u0c4e\u0c4f\u0c50\u0c51\u0c52\u0c53\u0c54ౕౖ\u0c57ౘౙ\u0c5a\u0c5b\u0c5c\u0c5d\u0c5e\u0c5fౠౡౢౣ\u0c64\u0c65౦౧౨౩౪౫౬౭౮౯\u0c70\u0c71\u0c72\u0c73\u0c74\u0c75\u0c76\u0c77౸౹౺౻౼౽౾౿', ',') satisfies matches($s, '^(?:\\p{IsTelugu}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsTelugu}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00235() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0c80\u0cff,\u0c80\u0c81ಂಃ\u0c84ಅಆಇಈಉಊಋಌ\u0c8dಎಏಐ\u0c91ಒಓಔಕಖಗಘಙಚಛಜಝಞಟಠಡಢಣತಥದಧನ\u0ca9ಪಫಬಭಮಯರಱಲಳ\u0cb4ವಶಷಸಹ\u0cba\u0cbb಼ಽಾಿೀುೂೃೄ\u0cc5ೆೇೈ\u0cc9ೊೋೌ್\u0cce\u0ccf\u0cd0\u0cd1\u0cd2\u0cd3\u0cd4ೕೖ\u0cd7\u0cd8\u0cd9\u0cda\u0cdb\u0cdc\u0cddೞ\u0cdfೠೡೢೣ\u0ce4\u0ce5೦೧೨೩೪೫೬೭೮೯\u0cf0ೱೲ\u0cf3\u0cf4\u0cf5\u0cf6\u0cf7\u0cf8\u0cf9\u0cfa\u0cfb\u0cfc\u0cfd\u0cfe\u0cff', ',') satisfies matches($s, '^(?:\\p{IsKannada}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsKannada}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00236() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0d00ൿ,\u0d00\u0d01ംഃ\u0d04അആഇഈഉഊഋഌ\u0d0dഎഏഐ\u0d11ഒഓഔകഖഗഘങചഛജഝഞടഠഡഢണതഥദധനഩപഫബഭമയരറലളഴവശഷസഹഺ\u0d3b\u0d3cഽാിീുൂൃൄ\u0d45െേൈ\u0d49ൊോൌ്ൎ\u0d4f\u0d50\u0d51\u0d52\u0d53\u0d54\u0d55\u0d56ൗ\u0d58\u0d59\u0d5a\u0d5b\u0d5c\u0d5d\u0d5e\u0d5fൠൡൢൣ\u0d64\u0d65൦൧൨൩൪൫൬൭൮൯൰൱൲൳൴൵\u0d76\u0d77\u0d78൹ൺൻർൽൾൿ', ',') satisfies matches($s, '^(?:\\p{IsMalayalam}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsMalayalam}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00237() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0d80\u0dff,\u0d80\u0d81ංඃ\u0d84අආඇඈඉඊඋඌඍඎඏඐඑඒඓඔඕඖ\u0d97\u0d98\u0d99කඛගඝඞඟචඡජඣඤඥඦටඨඩඪණඬතථදධන\u0db2ඳපඵබභමඹයර\u0dbcල\u0dbe\u0dbfවශෂසහළෆ\u0dc7\u0dc8\u0dc9්\u0dcb\u0dcc\u0dcd\u0dceාැෑිීු\u0dd5ූ\u0dd7ෘෙේෛොෝෞෟ\u0de0\u0de1\u0de2\u0de3\u0de4\u0de5\u0de6\u0de7\u0de8\u0de9\u0dea\u0deb\u0dec\u0ded\u0dee\u0def\u0df0\u0df1ෲෳ\u0df4\u0df5\u0df6\u0df7\u0df8\u0df9\u0dfa\u0dfb\u0dfc\u0dfd\u0dfe\u0dff', ',') satisfies matches($s, '^(?:\\p{IsSinhala}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsSinhala}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00238() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0e00\u0e7f,\u0e00กขฃคฅฆงจฉชซฌญฎฏฐฑฒณดตถทธนบปผฝพฟภมยรฤลฦวศษสหฬอฮฯะัาำิีึืฺุู\u0e3b\u0e3c\u0e3d\u0e3e฿เแโใไๅๆ็่้๊๋์ํ๎\u0e4f๐๑๒๓๔๕๖๗๘๙\u0e5a\u0e5b\u0e5c\u0e5d\u0e5e\u0e5f\u0e60\u0e61\u0e62\u0e63\u0e64\u0e65\u0e66\u0e67\u0e68\u0e69\u0e6a\u0e6b\u0e6c\u0e6d\u0e6e\u0e6f\u0e70\u0e71\u0e72\u0e73\u0e74\u0e75\u0e76\u0e77\u0e78\u0e79\u0e7a\u0e7b\u0e7c\u0e7d\u0e7e\u0e7f', ',') satisfies matches($s, '^(?:\\p{IsThai}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsThai}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00239() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u0e80\u0eff,\u0e80ກຂ\u0e83ຄ\u0e85\u0e86ງຈ\u0e89ຊ\u0e8b\u0e8cຍ\u0e8e\u0e8f\u0e90\u0e91\u0e92\u0e93ດຕຖທ\u0e98ນບປຜຝພຟ\u0ea0ມຢຣ\u0ea4ລ\u0ea6ວ\u0ea8\u0ea9ສຫ\u0eacອຮຯະັາຳິີຶືຸູ\u0ebaົຼຽ\u0ebe\u0ebfເແໂໃໄ\u0ec5ໆ\u0ec7່້໊໋໌ໍ\u0ece\u0ecf໐໑໒໓໔໕໖໗໘໙\u0eda\u0edbໜໝ\u0ede\u0edf\u0ee0\u0ee1\u0ee2\u0ee3\u0ee4\u0ee5\u0ee6\u0ee7\u0ee8\u0ee9\u0eea\u0eeb\u0eec\u0eed\u0eee\u0eef\u0ef0\u0ef1\u0ef2\u0ef3\u0ef4\u0ef5\u0ef6\u0ef7\u0ef8\u0ef9\u0efa\u0efb\u0efc\u0efd\u0efe\u0eff', ',') satisfies matches($s, '^(?:\\p{IsLao}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsLao}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00240() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ༀ\u0fff,ༀ༁༂༃\u0f04\u0f05\u0f06\u0f07\u0f08\u0f09\u0f0a\u0f0b\u0f0c\u0f0d\u0f0e\u0f0f\u0f10\u0f11\u0f12༓༔༕༖༗༘༙༚༛༜༝༞༟༠༡༢༣༤༥༦༧༨༩༪༫༬༭༮༯༰༱༲༳༴༵༶༷༸༹\u0f3a\u0f3b\u0f3c\u0f3d༾༿ཀཁགགྷངཅཆཇ\u0f48ཉཊཋཌཌྷཎཏཐདདྷནཔཕབབྷམཙཚཛཛྷཝཞཟའཡརལཤཥསཧཨཀྵཪཫཬ\u0f6d\u0f6e\u0f6f\u0f70ཱཱཱིིུུྲྀཷླྀཹེཻོཽཾཿ྄ཱྀྀྂྃ\u0f85྆྇ྈྉྊྋྌྍྎྏྐྑྒྒྷྔྕྖྗ\u0f98ྙྚྛྜྜྷྞྟྠྡྡྷྣྤྥྦྦྷྨྩྪྫྫྷྭྮྯྰྱྲླྴྵྶྷྸྐྵྺྻྼ\u0fbd྾྿࿀࿁࿂࿃࿄࿅࿆࿇࿈࿉࿊࿋࿌\u0fcd࿎࿏\u0fd0\u0fd1\u0fd2\u0fd3\u0fd4࿕࿖࿗࿘\u0fd9\u0fda\u0fdb\u0fdc\u0fdd\u0fde\u0fdf\u0fe0\u0fe1\u0fe2\u0fe3\u0fe4\u0fe5\u0fe6\u0fe7\u0fe8\u0fe9\u0fea\u0feb\u0fec\u0fed\u0fee\u0fef\u0ff0\u0ff1\u0ff2\u0ff3\u0ff4\u0ff5\u0ff6\u0ff7\u0ff8\u0ff9\u0ffa\u0ffb\u0ffc\u0ffd\u0ffe\u0fff', ',') satisfies matches($s, '^(?:\\p{IsTibetan}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsTibetan}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00241() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('က႟,ကခဂဃငစဆဇဈဉညဋဌဍဎဏတထဒဓနပဖဗဘမယရလဝသဟဠအဢဣဤဥဦဧဨဩဪါာိီုူေဲဳဴဵံ့း္်ျြွှဿ၀၁၂၃၄၅၆၇၈၉\u104a\u104b\u104c\u104d\u104e\u104fၐၑၒၓၔၕၖၗၘၙၚၛၜၝၞၟၠၡၢၣၤၥၦၧၨၩၪၫၬၭၮၯၰၱၲၳၴၵၶၷၸၹၺၻၼၽၾၿႀႁႂႃႄႅႆႇႈႉႊႋႌႍႎႏ႐႑႒႓႔႕႖႗႘႙ႚႛႜႝ႞႟', ',') satisfies matches($s, '^(?:\\p{IsMyanmar}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsMyanmar}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00242() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Ⴀ\u10ff,ႠႡႢႣႤႥႦႧႨႩႪႫႬႭႮႯႰႱႲႳႴႵႶႷႸႹႺႻႼႽႾႿჀჁჂჃჄჅ\u10c6\u10c7\u10c8\u10c9\u10ca\u10cb\u10cc\u10cd\u10ce\u10cfაბგდევზთიკლმნოპჟრსტუფქღყშჩცძწჭხჯჰჱჲჳჴჵჶჷჸჹჺ\u10fbჼ\u10fd\u10fe\u10ff', ',') satisfies matches($s, '^(?:\\p{IsGeorgian}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsGeorgian}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00243() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ᄀᇿ,ᄀᄁᄂᄃᄄᄅᄆᄇᄈᄉᄊᄋᄌᄍᄎᄏᄐᄑᄒᄓᄔᄕᄖᄗᄘᄙᄚᄛᄜᄝᄞᄟᄠᄡᄢᄣᄤᄥᄦᄧᄨᄩᄪᄫᄬᄭᄮᄯᄰᄱᄲᄳᄴᄵᄶᄷᄸᄹᄺᄻᄼᄽᄾᄿᅀᅁᅂᅃᅄᅅᅆᅇᅈᅉᅊᅋᅌᅍᅎᅏᅐᅑᅒᅓᅔᅕᅖᅗᅘᅙᅚᅛᅜᅝᅞᅟᅠᅡᅢᅣᅤᅥᅦᅧᅨᅩᅪᅫᅬᅭᅮᅯᅰᅱᅲᅳᅴᅵᅶᅷᅸᅹᅺᅻᅼᅽᅾᅿᆀᆁᆂᆃᆄᆅᆆᆇᆈᆉᆊᆋᆌᆍᆎᆏᆐᆑᆒᆓᆔᆕᆖᆗᆘᆙᆚᆛᆜᆝᆞᆟᆠᆡᆢᆣᆤᆥᆦᆧᆨᆩᆪᆫᆬᆭᆮᆯᆰᆱᆲᆳᆴᆵᆶᆷᆸᆹᆺᆻᆼᆽᆾᆿᇀᇁᇂᇃᇄᇅᇆᇇᇈᇉᇊᇋᇌᇍᇎᇏᇐᇑᇒᇓᇔᇕᇖᇗᇘᇙᇚᇛᇜᇝᇞᇟᇠᇡᇢᇣᇤᇥᇦᇧᇨᇩᇪᇫᇬᇭᇮᇯᇰᇱᇲᇳᇴᇵᇶᇷᇸᇹᇺᇻᇼᇽᇾᇿ', ',') satisfies matches($s, '^(?:\\p{IsHangulJamo}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsHangulJamo}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00244() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ሀ\u137f,ሀሁሂሃሄህሆሇለሉሊላሌልሎሏሐሑሒሓሔሕሖሗመሙሚማሜምሞሟሠሡሢሣሤሥሦሧረሩሪራሬርሮሯሰሱሲሳሴስሶሷሸሹሺሻሼሽሾሿቀቁቂቃቄቅቆቇቈ\u1249ቊቋቌቍ\u124e\u124fቐቑቒቓቔቕቖ\u1257ቘ\u1259ቚቛቜቝ\u125e\u125fበቡቢባቤብቦቧቨቩቪቫቬቭቮቯተቱቲታቴትቶቷቸቹቺቻቼችቾቿኀኁኂኃኄኅኆኇኈ\u1289ኊኋኌኍ\u128e\u128fነኑኒናኔንኖኗኘኙኚኛኜኝኞኟአኡኢኣኤእኦኧከኩኪካኬክኮኯኰ\u12b1ኲኳኴኵ\u12b6\u12b7ኸኹኺኻኼኽኾ\u12bfዀ\u12c1ዂዃዄዅ\u12c6\u12c7ወዉዊዋዌውዎዏዐዑዒዓዔዕዖ\u12d7ዘዙዚዛዜዝዞዟዠዡዢዣዤዥዦዧየዩዪያዬይዮዯደዱዲዳዴድዶዷዸዹዺዻዼዽዾዿጀጁጂጃጄጅጆጇገጉጊጋጌግጎጏጐ\u1311ጒጓጔጕ\u1316\u1317ጘጙጚጛጜጝጞጟጠጡጢጣጤጥጦጧጨጩጪጫጬጭጮጯጰጱጲጳጴጵጶጷጸጹጺጻጼጽጾጿፀፁፂፃፄፅፆፇፈፉፊፋፌፍፎፏፐፑፒፓፔፕፖፗፘፙፚ\u135b\u135c፝፞፟፠\u1361\u1362\u1363\u1364\u1365\u1366\u1367\u1368፩፪፫፬፭፮፯፰፱፲፳፴፵፶፷፸፹፺፻፼\u137d\u137e\u137f', ',') satisfies matches($s, '^(?:\\p{IsEthiopic}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsEthiopic}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00245() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Ꭰ\u13ff,ᎠᎡᎢᎣᎤᎥᎦᎧᎨᎩᎪᎫᎬᎭᎮᎯᎰᎱᎲᎳᎴᎵᎶᎷᎸᎹᎺᎻᎼᎽᎾᎿᏀᏁᏂᏃᏄᏅᏆᏇᏈᏉᏊᏋᏌᏍᏎᏏᏐᏑᏒᏓᏔᏕᏖᏗᏘᏙᏚᏛᏜᏝᏞᏟᏠᏡᏢᏣᏤᏥᏦᏧᏨᏩᏪᏫᏬᏭᏮᏯᏰᏱᏲᏳᏴ\u13f5\u13f6\u13f7\u13f8\u13f9\u13fa\u13fb\u13fc\u13fd\u13fe\u13ff', ',') satisfies matches($s, '^(?:\\p{IsCherokee}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCherokee}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00246() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u1400ᙿ,\u1400ᐁᐂᐃᐄᐅᐆᐇᐈᐉᐊᐋᐌᐍᐎᐏᐐᐑᐒᐓᐔᐕᐖᐗᐘᐙᐚᐛᐜᐝᐞᐟᐠᐡᐢᐣᐤᐥᐦᐧᐨᐩᐪᐫᐬᐭᐮᐯᐰᐱᐲᐳᐴᐵᐶᐷᐸᐹᐺᐻᐼᐽᐾᐿᑀᑁᑂᑃᑄᑅᑆᑇᑈᑉᑊᑋᑌᑍᑎᑏᑐᑑᑒᑓᑔᑕᑖᑗᑘᑙᑚᑛᑜᑝᑞᑟᑠᑡᑢᑣᑤᑥᑦᑧᑨᑩᑪᑫᑬᑭᑮᑯᑰᑱᑲᑳᑴᑵᑶᑷᑸᑹᑺᑻᑼᑽᑾᑿᒀᒁᒂᒃᒄᒅᒆᒇᒈᒉᒊᒋᒌᒍᒎᒏᒐᒑᒒᒓᒔᒕᒖᒗᒘᒙᒚᒛᒜᒝᒞᒟᒠᒡᒢᒣᒤᒥᒦᒧᒨᒩᒪᒫᒬᒭᒮᒯᒰᒱᒲᒳᒴᒵᒶᒷᒸᒹᒺᒻᒼᒽᒾᒿᓀᓁᓂᓃᓄᓅᓆᓇᓈᓉᓊᓋᓌᓍᓎᓏᓐᓑᓒᓓᓔᓕᓖᓗᓘᓙᓚᓛᓜᓝᓞᓟᓠᓡᓢᓣᓤᓥᓦᓧᓨᓩᓪᓫᓬᓭᓮᓯᓰᓱᓲᓳᓴᓵᓶᓷᓸᓹᓺᓻᓼᓽᓾᓿᔀᔁᔂᔃᔄᔅᔆᔇᔈᔉᔊᔋᔌᔍᔎᔏᔐᔑᔒᔓᔔᔕᔖᔗᔘᔙᔚᔛᔜᔝᔞᔟᔠᔡᔢᔣᔤᔥᔦᔧᔨᔩᔪᔫᔬᔭᔮᔯᔰᔱᔲᔳᔴᔵᔶᔷᔸᔹᔺᔻᔼᔽᔾᔿᕀᕁᕂᕃᕄᕅᕆᕇᕈᕉᕊᕋᕌᕍᕎᕏᕐᕑᕒᕓᕔᕕᕖᕗᕘᕙᕚᕛᕜᕝᕞᕟᕠᕡᕢᕣᕤᕥᕦᕧᕨᕩᕪᕫᕬᕭᕮᕯᕰᕱᕲᕳᕴᕵᕶᕷᕸᕹᕺᕻᕼᕽᕾᕿᖀᖁᖂᖃᖄᖅᖆᖇᖈᖉᖊᖋᖌᖍᖎᖏᖐᖑᖒᖓᖔᖕᖖᖗᖘᖙᖚᖛᖜᖝᖞᖟᖠᖡᖢᖣᖤᖥᖦᖧᖨᖩᖪᖫᖬᖭᖮᖯᖰᖱᖲᖳᖴᖵᖶᖷᖸᖹᖺᖻᖼᖽᖾᖿᗀᗁᗂᗃᗄᗅᗆᗇᗈᗉᗊᗋᗌᗍᗎᗏᗐᗑᗒᗓᗔᗕᗖᗗᗘᗙᗚᗛᗜᗝᗞᗟᗠᗡᗢᗣᗤᗥᗦᗧᗨᗩᗪᗫᗬᗭᗮᗯᗰᗱᗲᗳᗴᗵᗶᗷᗸᗹᗺᗻᗼᗽᗾᗿᘀᘁᘂᘃᘄᘅᘆᘇᘈᘉᘊᘋᘌᘍᘎᘏᘐᘑᘒᘓᘔᘕᘖᘗᘘᘙᘚᘛᘜᘝᘞᘟᘠᘡᘢᘣᘤᘥᘦᘧᘨᘩᘪᘫᘬᘭᘮᘯᘰᘱᘲᘳᘴᘵᘶᘷᘸᘹᘺᘻᘼᘽᘾᘿᙀᙁᙂᙃᙄᙅᙆᙇᙈᙉᙊᙋᙌᙍᙎᙏᙐᙑᙒᙓᙔᙕᙖᙗᙘᙙᙚᙛᙜᙝᙞᙟᙠᙡᙢᙣᙤᙥᙦᙧᙨᙩᙪᙫᙬ\u166d\u166eᙯᙰᙱᙲᙳᙴᙵᙶᙷᙸᙹᙺᙻᙼᙽᙾᙿ', ',') satisfies matches($s, '^(?:\\p{IsUnifiedCanadianAboriginalSyllabics}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsUnifiedCanadianAboriginalSyllabics}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00247() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u1680\u169f,\u1680ᚁᚂᚃᚄᚅᚆᚇᚈᚉᚊᚋᚌᚍᚎᚏᚐᚑᚒᚓᚔᚕᚖᚗᚘᚙᚚ\u169b\u169c\u169d\u169e\u169f', ',') satisfies matches($s, '^(?:\\p{IsOgham}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsOgham}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00248() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ᚠ\u16ff,ᚠᚡᚢᚣᚤᚥᚦᚧᚨᚩᚪᚫᚬᚭᚮᚯᚰᚱᚲᚳᚴᚵᚶᚷᚸᚹᚺᚻᚼᚽᚾᚿᛀᛁᛂᛃᛄᛅᛆᛇᛈᛉᛊᛋᛌᛍᛎᛏᛐᛑᛒᛓᛔᛕᛖᛗᛘᛙᛚᛛᛜᛝᛞᛟᛠᛡᛢᛣᛤᛥᛦᛧᛨᛩᛪ\u16eb\u16ec\u16edᛮᛯᛰ\u16f1\u16f2\u16f3\u16f4\u16f5\u16f6\u16f7\u16f8\u16f9\u16fa\u16fb\u16fc\u16fd\u16fe\u16ff', ',') satisfies matches($s, '^(?:\\p{IsRunic}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsRunic}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00249() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ក\u17ff,កខគឃងចឆជឈញដឋឌឍណតថទធនបផពភមយរលវឝឞសហឡអឣឤឥឦឧឨឩឪឫឬឭឮឯឰឱឲឳ\u17b4\u17b5ាិីឹឺុូួើឿៀេែៃោៅំះៈ៉៊់៌៍៎៏័៑្៓\u17d4\u17d5\u17d6ៗ\u17d8\u17d9\u17da៛ៜ៝\u17de\u17df០១២៣៤៥៦៧៨៩\u17ea\u17eb\u17ec\u17ed\u17ee\u17ef៰៱៲៳៴៵៶៷៸៹\u17fa\u17fb\u17fc\u17fd\u17fe\u17ff', ',') satisfies matches($s, '^(?:\\p{IsKhmer}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsKhmer}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00250() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u1800\u18af,\u1800\u1801\u1802\u1803\u1804\u1805\u1806\u1807\u1808\u1809\u180a᠋᠌᠍\u180e\u180f᠐᠑᠒᠓᠔᠕᠖᠗᠘᠙\u181a\u181b\u181c\u181d\u181e\u181fᠠᠡᠢᠣᠤᠥᠦᠧᠨᠩᠪᠫᠬᠭᠮᠯᠰᠱᠲᠳᠴᠵᠶᠷᠸᠹᠺᠻᠼᠽᠾᠿᡀᡁᡂᡃᡄᡅᡆᡇᡈᡉᡊᡋᡌᡍᡎᡏᡐᡑᡒᡓᡔᡕᡖᡗᡘᡙᡚᡛᡜᡝᡞᡟᡠᡡᡢᡣᡤᡥᡦᡧᡨᡩᡪᡫᡬᡭᡮᡯᡰᡱᡲᡳᡴᡵᡶᡷ\u1878\u1879\u187a\u187b\u187c\u187d\u187e\u187fᢀᢁᢂᢃᢄᢅᢆᢇᢈᢉᢊᢋᢌᢍᢎᢏᢐᢑᢒᢓᢔᢕᢖᢗᢘᢙᢚᢛᢜᢝᢞᢟᢠᢡᢢᢣᢤᢥᢦᢧᢨᢩᢪ\u18ab\u18ac\u18ad\u18ae\u18af', ',') satisfies matches($s, '^(?:\\p{IsMongolian}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsMongolian}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00251() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Ḁỿ,ḀḁḂḃḄḅḆḇḈḉḊḋḌḍḎḏḐḑḒḓḔḕḖḗḘḙḚḛḜḝḞḟḠḡḢḣḤḥḦḧḨḩḪḫḬḭḮḯḰḱḲḳḴḵḶḷḸḹḺḻḼḽḾḿṀṁṂṃṄṅṆṇṈṉṊṋṌṍṎṏṐṑṒṓṔṕṖṗṘṙṚṛṜṝṞṟṠṡṢṣṤṥṦṧṨṩṪṫṬṭṮṯṰṱṲṳṴṵṶṷṸṹṺṻṼṽṾṿẀẁẂẃẄẅẆẇẈẉẊẋẌẍẎẏẐẑẒẓẔẕẖẗẘẙẚẛẜẝẞẟẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹỺỻỼỽỾỿ', ',') satisfies matches($s, '^(?:\\p{IsLatinExtendedAdditional}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtendedAdditional}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00252() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ἀ\u1fff,ἀἁἂἃἄἅἆἇἈἉἊἋἌἍἎἏἐἑἒἓἔἕ\u1f16\u1f17ἘἙἚἛἜἝ\u1f1e\u1f1fἠἡἢἣἤἥἦἧἨἩἪἫἬἭἮἯἰἱἲἳἴἵἶἷἸἹἺἻἼἽἾἿὀὁὂὃὄὅ\u1f46\u1f47ὈὉὊὋὌὍ\u1f4e\u1f4fὐὑὒὓὔὕὖὗ\u1f58Ὑ\u1f5aὛ\u1f5cὝ\u1f5eὟὠὡὢὣὤὥὦὧὨὩὪὫὬὭὮὯὰάὲέὴήὶίὸόὺύὼώ\u1f7e\u1f7fᾀᾁᾂᾃᾄᾅᾆᾇᾈᾉᾊᾋᾌᾍᾎᾏᾐᾑᾒᾓᾔᾕᾖᾗᾘᾙᾚᾛᾜᾝᾞᾟᾠᾡᾢᾣᾤᾥᾦᾧᾨᾩᾪᾫᾬᾭᾮᾯᾰᾱᾲᾳᾴ\u1fb5ᾶᾷᾸᾹᾺΆᾼ᾽ι᾿῀῁ῂῃῄ\u1fc5ῆῇῈΈῊΉῌ῍῎῏ῐῑῒΐ\u1fd4\u1fd5ῖῗῘῙῚΊ\u1fdc῝῞῟ῠῡῢΰῤῥῦῧῨῩῪΎῬ῭΅`\u1ff0\u1ff1ῲῳῴ\u1ff5ῶῷῸΌῺΏῼ´῾\u1fff', ',') satisfies matches($s, '^(?:\\p{IsGreekExtended}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsGreekExtended}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00253() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u2000\u206f,\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u200c\u200d\u200e\u200f\u2010\u2011\u2012\u2013\u2014\u2015\u2016\u2017\u2018\u2019\u201a\u201b\u201c\u201d\u201e\u201f\u2020\u2021\u2022\u2023\u2024\u2025\u2026\u2027\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f\u2030\u2031\u2032\u2033\u2034\u2035\u2036\u2037\u2038\u2039\u203a\u203b\u203c\u203d\u203e\u203f\u2040\u2041\u2042\u2043⁄\u2045\u2046\u2047\u2048\u2049\u204a\u204b\u204c\u204d\u204e\u204f\u2050\u2051⁒\u2053\u2054\u2055\u2056\u2057\u2058\u2059\u205a\u205b\u205c\u205d\u205e\u205f\u2060\u2061\u2062\u2063\u2064\u2065\u2066\u2067\u2068\u2069\u206a\u206b\u206c\u206d\u206e\u206f', ',') satisfies matches($s, '^(?:\\p{IsGeneralPunctuation}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsGeneralPunctuation}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00254() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⁰\u209f,⁰ⁱ\u2072\u2073⁴⁵⁶⁷⁸⁹⁺⁻⁼\u207d\u207eⁿ₀₁₂₃₄₅₆₇₈₉₊₋₌\u208d\u208e\u208fₐₑₒₓₔₕₖₗₘₙₚₛₜ\u209d\u209e\u209f', ',') satisfies matches($s, '^(?:\\p{IsSuperscriptsandSubscripts}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsSuperscriptsandSubscripts}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00255() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('₠\u20cf,₠₡₢₣₤₥₦₧₨₩₪₫€₭₮₯₰₱₲₳₴₵₶₷₸₹\u20ba\u20bb\u20bc\u20bd\u20be\u20bf\u20c0\u20c1\u20c2\u20c3\u20c4\u20c5\u20c6\u20c7\u20c8\u20c9\u20ca\u20cb\u20cc\u20cd\u20ce\u20cf', ',') satisfies matches($s, '^(?:\\p{IsCurrencySymbols}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCurrencySymbols}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00256() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⃐\u20ff', ',') satisfies matches($s, '^(?:\\p{IsCombiningMarksforSymbols}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCombiningMarksforSymbols}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00257() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('℀⅏,℀℁ℂ℃℄℅℆ℇ℈℉ℊℋℌℍℎℏℐℑℒℓ℔ℕ№℗℘ℙℚℛℜℝ℞℟℠℡™℣ℤ℥Ω℧ℨ℩KÅℬℭ℮ℯℰℱℲℳℴℵℶℷℸℹ℺℻ℼℽℾℿ⅀⅁⅂⅃⅄ⅅⅆⅇⅈⅉ⅊⅋⅌⅍ⅎ⅏', ',') satisfies matches($s, '^(?:\\p{IsLetterlikeSymbols}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsLetterlikeSymbols}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00258() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⅐\u218f,⅐⅑⅒⅓⅔⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞⅟ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅬⅭⅮⅯⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹⅺⅻⅼⅽⅾⅿↀↁↂↃↄↅↆↇↈ↉\u218a\u218b\u218c\u218d\u218e\u218f', ',') satisfies matches($s, '^(?:\\p{IsNumberForms}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsNumberForms}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00259() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('←⇿,←↑→↓↔↕↖↗↘↙↚↛↜↝↞↟↠↡↢↣↤↥↦↧↨↩↪↫↬↭↮↯↰↱↲↳↴↵↶↷↸↹↺↻↼↽↾↿⇀⇁⇂⇃⇄⇅⇆⇇⇈⇉⇊⇋⇌⇍⇎⇏⇐⇑⇒⇓⇔⇕⇖⇗⇘⇙⇚⇛⇜⇝⇞⇟⇠⇡⇢⇣⇤⇥⇦⇧⇨⇩⇪⇫⇬⇭⇮⇯⇰⇱⇲⇳⇴⇵⇶⇷⇸⇹⇺⇻⇼⇽⇾⇿', ',') satisfies matches($s, '^(?:\\p{IsArrows}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsArrows}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00260() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('∀⋿,∀∁∂∃∄∅∆∇∈∉∊∋∌∍∎∏∐∑−∓∔∕∖∗∘∙√∛∜∝∞∟∠∡∢∣∤∥∦∧∨∩∪∫∬∭∮∯∰∱∲∳∴∵∶∷∸∹∺∻∼∽∾∿≀≁≂≃≄≅≆≇≈≉≊≋≌≍≎≏≐≑≒≓≔≕≖≗≘≙≚≛≜≝≞≟≠≡≢≣≤≥≦≧≨≩≪≫≬≭≮≯≰≱≲≳≴≵≶≷≸≹≺≻≼≽≾≿⊀⊁⊂⊃⊄⊅⊆⊇⊈⊉⊊⊋⊌⊍⊎⊏⊐⊑⊒⊓⊔⊕⊖⊗⊘⊙⊚⊛⊜⊝⊞⊟⊠⊡⊢⊣⊤⊥⊦⊧⊨⊩⊪⊫⊬⊭⊮⊯⊰⊱⊲⊳⊴⊵⊶⊷⊸⊹⊺⊻⊼⊽⊾⊿⋀⋁⋂⋃⋄⋅⋆⋇⋈⋉⋊⋋⋌⋍⋎⋏⋐⋑⋒⋓⋔⋕⋖⋗⋘⋙⋚⋛⋜⋝⋞⋟⋠⋡⋢⋣⋤⋥⋦⋧⋨⋩⋪⋫⋬⋭⋮⋯⋰⋱⋲⋳⋴⋵⋶⋷⋸⋹⋺⋻⋼⋽⋾⋿', ',') satisfies matches($s, '^(?:\\p{IsMathematicalOperators}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsMathematicalOperators}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00261() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⌀\u23ff,⌀⌁⌂⌃⌄⌅⌆⌇⌈⌉⌊⌋⌌⌍⌎⌏⌐⌑⌒⌓⌔⌕⌖⌗⌘⌙⌚⌛⌜⌝⌞⌟⌠⌡⌢⌣⌤⌥⌦⌧⌨\u2329\u232a⌫⌬⌭⌮⌯⌰⌱⌲⌳⌴⌵⌶⌷⌸⌹⌺⌻⌼⌽⌾⌿⍀⍁⍂⍃⍄⍅⍆⍇⍈⍉⍊⍋⍌⍍⍎⍏⍐⍑⍒⍓⍔⍕⍖⍗⍘⍙⍚⍛⍜⍝⍞⍟⍠⍡⍢⍣⍤⍥⍦⍧⍨⍩⍪⍫⍬⍭⍮⍯⍰⍱⍲⍳⍴⍵⍶⍷⍸⍹⍺⍻⍼⍽⍾⍿⎀⎁⎂⎃⎄⎅⎆⎇⎈⎉⎊⎋⎌⎍⎎⎏⎐⎑⎒⎓⎔⎕⎖⎗⎘⎙⎚⎛⎜⎝⎞⎟⎠⎡⎢⎣⎤⎥⎦⎧⎨⎩⎪⎫⎬⎭⎮⎯⎰⎱⎲⎳⎴⎵⎶⎷⎸⎹⎺⎻⎼⎽⎾⎿⏀⏁⏂⏃⏄⏅⏆⏇⏈⏉⏊⏋⏌⏍⏎⏏⏐⏑⏒⏓⏔⏕⏖⏗⏘⏙⏚⏛⏜⏝⏞⏟⏠⏡⏢⏣⏤⏥⏦⏧⏨⏩⏪⏫⏬⏭⏮⏯⏰⏱⏲⏳\u23f4\u23f5\u23f6\u23f7\u23f8\u23f9\u23fa\u23fb\u23fc\u23fd\u23fe\u23ff', ',') satisfies matches($s, '^(?:\\p{IsMiscellaneousTechnical}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsMiscellaneousTechnical}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00262() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('␀\u243f,␀␁␂␃␄␅␆␇␈␉␊␋␌␍␎␏␐␑␒␓␔␕␖␗␘␙␚␛␜␝␞␟␠␡␢␣␤␥␦\u2427\u2428\u2429\u242a\u242b\u242c\u242d\u242e\u242f\u2430\u2431\u2432\u2433\u2434\u2435\u2436\u2437\u2438\u2439\u243a\u243b\u243c\u243d\u243e\u243f', ',') satisfies matches($s, '^(?:\\p{IsControlPictures}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsControlPictures}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00263() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⑀\u245f,⑀⑁⑂⑃⑄⑅⑆⑇⑈⑉⑊\u244b\u244c\u244d\u244e\u244f\u2450\u2451\u2452\u2453\u2454\u2455\u2456\u2457\u2458\u2459\u245a\u245b\u245c\u245d\u245e\u245f', ',') satisfies matches($s, '^(?:\\p{IsOpticalCharacterRecognition}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsOpticalCharacterRecognition}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00264() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('①⓿,①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳⑴⑵⑶⑷⑸⑹⑺⑻⑼⑽⑾⑿⒀⒁⒂⒃⒄⒅⒆⒇⒈⒉⒊⒋⒌⒍⒎⒏⒐⒑⒒⒓⒔⒕⒖⒗⒘⒙⒚⒛⒜⒝⒞⒟⒠⒡⒢⒣⒤⒥⒦⒧⒨⒩⒪⒫⒬⒭⒮⒯⒰⒱⒲⒳⒴⒵ⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ⓪⓫⓬⓭⓮⓯⓰⓱⓲⓳⓴⓵⓶⓷⓸⓹⓺⓻⓼⓽⓾⓿', ',') satisfies matches($s, '^(?:\\p{IsEnclosedAlphanumerics}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsEnclosedAlphanumerics}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00265() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('─╿,─━│┃┄┅┆┇┈┉┊┋┌┍┎┏┐┑┒┓└┕┖┗┘┙┚┛├┝┞┟┠┡┢┣┤┥┦┧┨┩┪┫┬┭┮┯┰┱┲┳┴┵┶┷┸┹┺┻┼┽┾┿╀╁╂╃╄╅╆╇╈╉╊╋╌╍╎╏═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬╭╮╯╰╱╲╳╴╵╶╷╸╹╺╻╼╽╾╿', ',') satisfies matches($s, '^(?:\\p{IsBoxDrawing}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsBoxDrawing}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00266() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('▀▟,▀▁▂▃▄▅▆▇█▉▊▋▌▍▎▏▐░▒▓▔▕▖▗▘▙▚▛▜▝▞▟', ',') satisfies matches($s, '^(?:\\p{IsBlockElements}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsBlockElements}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00267() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('■◿,■□▢▣▤▥▦▧▨▩▪▫▬▭▮▯▰▱▲△▴▵▶▷▸▹►▻▼▽▾▿◀◁◂◃◄◅◆◇◈◉◊○◌◍◎●◐◑◒◓◔◕◖◗◘◙◚◛◜◝◞◟◠◡◢◣◤◥◦◧◨◩◪◫◬◭◮◯◰◱◲◳◴◵◶◷◸◹◺◻◼◽◾◿', ',') satisfies matches($s, '^(?:\\p{IsGeometricShapes}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsGeometricShapes}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00268() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('☀⛿,☀☁☂☃☄★☆☇☈☉☊☋☌☍☎☏☐☑☒☓☔☕☖☗☘☙☚☛☜☝☞☟☠☡☢☣☤☥☦☧☨☩☪☫☬☭☮☯☰☱☲☳☴☵☶☷☸☹☺☻☼☽☾☿♀♁♂♃♄♅♆♇♈♉♊♋♌♍♎♏♐♑♒♓♔♕♖♗♘♙♚♛♜♝♞♟♠♡♢♣♤♥♦♧♨♩♪♫♬♭♮♯♰♱♲♳♴♵♶♷♸♹♺♻♼♽♾♿⚀⚁⚂⚃⚄⚅⚆⚇⚈⚉⚊⚋⚌⚍⚎⚏⚐⚑⚒⚓⚔⚕⚖⚗⚘⚙⚚⚛⚜⚝⚞⚟⚠⚡⚢⚣⚤⚥⚦⚧⚨⚩⚪⚫⚬⚭⚮⚯⚰⚱⚲⚳⚴⚵⚶⚷⚸⚹⚺⚻⚼⚽⚾⚿⛀⛁⛂⛃⛄⛅⛆⛇⛈⛉⛊⛋⛌⛍⛎⛏⛐⛑⛒⛓⛔⛕⛖⛗⛘⛙⛚⛛⛜⛝⛞⛟⛠⛡⛢⛣⛤⛥⛦⛧⛨⛩⛪⛫⛬⛭⛮⛯⛰⛱⛲⛳⛴⛵⛶⛷⛸⛹⛺⛻⛼⛽⛾⛿', ',') satisfies matches($s, '^(?:\\p{IsMiscellaneousSymbols}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsMiscellaneousSymbols}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00269() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u2700➿,\u2700✁✂✃✄✅✆✇✈✉✊✋✌✍✎✏✐✑✒✓✔✕✖✗✘✙✚✛✜✝✞✟✠✡✢✣✤✥✦✧✨✩✪✫✬✭✮✯✰✱✲✳✴✵✶✷✸✹✺✻✼✽✾✿❀❁❂❃❄❅❆❇❈❉❊❋❌❍❎❏❐❑❒❓❔❕❖❗❘❙❚❛❜❝❞❟❠❡❢❣❤❥❦❧\u2768\u2769\u276a\u276b\u276c\u276d\u276e\u276f\u2770\u2771\u2772\u2773\u2774\u2775❶❷❸❹❺❻❼❽❾❿➀➁➂➃➄➅➆➇➈➉➊➋➌➍➎➏➐➑➒➓➔➕➖➗➘➙➚➛➜➝➞➟➠➡➢➣➤➥➦➧➨➩➪➫➬➭➮➯➰➱➲➳➴➵➶➷➸➹➺➻➼➽➾➿', ',') satisfies matches($s, '^(?:\\p{IsDingbats}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsDingbats}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00270() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⠀⣿,⠀⠁⠂⠃⠄⠅⠆⠇⠈⠉⠊⠋⠌⠍⠎⠏⠐⠑⠒⠓⠔⠕⠖⠗⠘⠙⠚⠛⠜⠝⠞⠟⠠⠡⠢⠣⠤⠥⠦⠧⠨⠩⠪⠫⠬⠭⠮⠯⠰⠱⠲⠳⠴⠵⠶⠷⠸⠹⠺⠻⠼⠽⠾⠿⡀⡁⡂⡃⡄⡅⡆⡇⡈⡉⡊⡋⡌⡍⡎⡏⡐⡑⡒⡓⡔⡕⡖⡗⡘⡙⡚⡛⡜⡝⡞⡟⡠⡡⡢⡣⡤⡥⡦⡧⡨⡩⡪⡫⡬⡭⡮⡯⡰⡱⡲⡳⡴⡵⡶⡷⡸⡹⡺⡻⡼⡽⡾⡿⢀⢁⢂⢃⢄⢅⢆⢇⢈⢉⢊⢋⢌⢍⢎⢏⢐⢑⢒⢓⢔⢕⢖⢗⢘⢙⢚⢛⢜⢝⢞⢟⢠⢡⢢⢣⢤⢥⢦⢧⢨⢩⢪⢫⢬⢭⢮⢯⢰⢱⢲⢳⢴⢵⢶⢷⢸⢹⢺⢻⢼⢽⢾⢿⣀⣁⣂⣃⣄⣅⣆⣇⣈⣉⣊⣋⣌⣍⣎⣏⣐⣑⣒⣓⣔⣕⣖⣗⣘⣙⣚⣛⣜⣝⣞⣟⣠⣡⣢⣣⣤⣥⣦⣧⣨⣩⣪⣫⣬⣭⣮⣯⣰⣱⣲⣳⣴⣵⣶⣷⣸⣹⣺⣻⣼⣽⣾⣿', ',') satisfies matches($s, '^(?:\\p{IsBraillePatterns}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsBraillePatterns}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00271() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⺀\u2eff,⺀⺁⺂⺃⺄⺅⺆⺇⺈⺉⺊⺋⺌⺍⺎⺏⺐⺑⺒⺓⺔⺕⺖⺗⺘⺙\u2e9a⺛⺜⺝⺞⺟⺠⺡⺢⺣⺤⺥⺦⺧⺨⺩⺪⺫⺬⺭⺮⺯⺰⺱⺲⺳⺴⺵⺶⺷⺸⺹⺺⺻⺼⺽⺾⺿⻀⻁⻂⻃⻄⻅⻆⻇⻈⻉⻊⻋⻌⻍⻎⻏⻐⻑⻒⻓⻔⻕⻖⻗⻘⻙⻚⻛⻜⻝⻞⻟⻠⻡⻢⻣⻤⻥⻦⻧⻨⻩⻪⻫⻬⻭⻮⻯⻰⻱⻲⻳\u2ef4\u2ef5\u2ef6\u2ef7\u2ef8\u2ef9\u2efa\u2efb\u2efc\u2efd\u2efe\u2eff', ',') satisfies matches($s, '^(?:\\p{IsCJKRadicalsSupplement}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCJKRadicalsSupplement}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00272() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⼀\u2fdf,⼀⼁⼂⼃⼄⼅⼆⼇⼈⼉⼊⼋⼌⼍⼎⼏⼐⼑⼒⼓⼔⼕⼖⼗⼘⼙⼚⼛⼜⼝⼞⼟⼠⼡⼢⼣⼤⼥⼦⼧⼨⼩⼪⼫⼬⼭⼮⼯⼰⼱⼲⼳⼴⼵⼶⼷⼸⼹⼺⼻⼼⼽⼾⼿⽀⽁⽂⽃⽄⽅⽆⽇⽈⽉⽊⽋⽌⽍⽎⽏⽐⽑⽒⽓⽔⽕⽖⽗⽘⽙⽚⽛⽜⽝⽞⽟⽠⽡⽢⽣⽤⽥⽦⽧⽨⽩⽪⽫⽬⽭⽮⽯⽰⽱⽲⽳⽴⽵⽶⽷⽸⽹⽺⽻⽼⽽⽾⽿⾀⾁⾂⾃⾄⾅⾆⾇⾈⾉⾊⾋⾌⾍⾎⾏⾐⾑⾒⾓⾔⾕⾖⾗⾘⾙⾚⾛⾜⾝⾞⾟⾠⾡⾢⾣⾤⾥⾦⾧⾨⾩⾪⾫⾬⾭⾮⾯⾰⾱⾲⾳⾴⾵⾶⾷⾸⾹⾺⾻⾼⾽⾾⾿⿀⿁⿂⿃⿄⿅⿆⿇⿈⿉⿊⿋⿌⿍⿎⿏⿐⿑⿒⿓⿔⿕\u2fd6\u2fd7\u2fd8\u2fd9\u2fda\u2fdb\u2fdc\u2fdd\u2fde\u2fdf', ',') satisfies matches($s, '^(?:\\p{IsKangxiRadicals}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsKangxiRadicals}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00273() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('⿰\u2fff,⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻\u2ffc\u2ffd\u2ffe\u2fff', ',') satisfies matches($s, '^(?:\\p{IsIdeographicDescriptionCharacters}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsIdeographicDescriptionCharacters}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00274() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u3000〿,\u3000\u3001\u3002\u3003〄々〆〇\u3008\u3009\u300a\u300b\u300c\u300d\u300e\u300f\u3010\u3011〒〓\u3014\u3015\u3016\u3017\u3018\u3019\u301a\u301b\u301c\u301d\u301e\u301f〠〡〢〣〤〥〦〧〨〩〪〭〮〯〫〬\u3030〱〲〳〴〵〶〷〸〹〺〻〼\u303d〾〿', ',') satisfies matches($s, '^(?:\\p{IsCJKSymbolsandPunctuation}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCJKSymbolsandPunctuation}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00275() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u3040ゟ,\u3040ぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽまみむめもゃやゅゆょよらりるれろゎわゐゑをんゔゕゖ\u3097\u3098゙゚゛゜ゝゞゟ', ',') satisfies matches($s, '^(?:\\p{IsHiragana}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsHiragana}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00276() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u30a0ヿ,\u30a0ァアィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾタダチヂッツヅテデトドナニヌネノハバパヒビピフブプヘベペホボポマミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶヷヸヹヺ\u30fbーヽヾヿ', ',') satisfies matches($s, '^(?:\\p{IsKatakana}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsKatakana}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00277() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u3100\u312f,\u3100\u3101\u3102\u3103\u3104ㄅㄆㄇㄈㄉㄊㄋㄌㄍㄎㄏㄐㄑㄒㄓㄔㄕㄖㄗㄘㄙㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦㄧㄨㄩㄪㄫㄬㄭ\u312e\u312f', ',') satisfies matches($s, '^(?:\\p{IsBopomofo}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsBopomofo}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00278() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\u3130\u318f,\u3130ㄱㄲㄳㄴㄵㄶㄷㄸㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅃㅄㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣㅤㅥㅦㅧㅨㅩㅪㅫㅬㅭㅮㅯㅰㅱㅲㅳㅴㅵㅶㅷㅸㅹㅺㅻㅼㅽㅾㅿㆀㆁㆂㆃㆄㆅㆆㆇㆈㆉㆊㆋㆌㆍㆎ\u318f', ',') satisfies matches($s, '^(?:\\p{IsHangulCompatibilityJamo}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsHangulCompatibilityJamo}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00279() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('㆐㆟,㆐㆑㆒㆓㆔㆕㆖㆗㆘㆙㆚㆛㆜㆝㆞㆟', ',') satisfies matches($s, '^(?:\\p{IsKanbun}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsKanbun}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00280() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ㆠ\u31bf,ㆠㆡㆢㆣㆤㆥㆦㆧㆨㆩㆪㆫㆬㆭㆮㆯㆰㆱㆲㆳㆴㆵㆶㆷㆸㆹㆺ\u31bb\u31bc\u31bd\u31be\u31bf', ',') satisfies matches($s, '^(?:\\p{IsBopomofoExtended}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsBopomofoExtended}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00281() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('㈀\u32ff,㈀㈁㈂㈃㈄㈅㈆㈇㈈㈉㈊㈋㈌㈍㈎㈏㈐㈑㈒㈓㈔㈕㈖㈗㈘㈙㈚㈛㈜㈝㈞\u321f㈠㈡㈢㈣㈤㈥㈦㈧㈨㈩㈪㈫㈬㈭㈮㈯㈰㈱㈲㈳㈴㈵㈶㈷㈸㈹㈺㈻㈼㈽㈾㈿㉀㉁㉂㉃㉄㉅㉆㉇㉈㉉㉊㉋㉌㉍㉎㉏㉐㉑㉒㉓㉔㉕㉖㉗㉘㉙㉚㉛㉜㉝㉞㉟㉠㉡㉢㉣㉤㉥㉦㉧㉨㉩㉪㉫㉬㉭㉮㉯㉰㉱㉲㉳㉴㉵㉶㉷㉸㉹㉺㉻㉼㉽㉾㉿㊀㊁㊂㊃㊄㊅㊆㊇㊈㊉㊊㊋㊌㊍㊎㊏㊐㊑㊒㊓㊔㊕㊖㊗㊘㊙㊚㊛㊜㊝㊞㊟㊠㊡㊢㊣㊤㊥㊦㊧㊨㊩㊪㊫㊬㊭㊮㊯㊰㊱㊲㊳㊴㊵㊶㊷㊸㊹㊺㊻㊼㊽㊾㊿㋀㋁㋂㋃㋄㋅㋆㋇㋈㋉㋊㋋㋌㋍㋎㋏㋐㋑㋒㋓㋔㋕㋖㋗㋘㋙㋚㋛㋜㋝㋞㋟㋠㋡㋢㋣㋤㋥㋦㋧㋨㋩㋪㋫㋬㋭㋮㋯㋰㋱㋲㋳㋴㋵㋶㋷㋸㋹㋺㋻㋼㋽㋾\u32ff', ',') satisfies matches($s, '^(?:\\p{IsEnclosedCJKLettersandMonths}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsEnclosedCJKLettersandMonths}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00282() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('㌀㏿,㌀㌁㌂㌃㌄㌅㌆㌇㌈㌉㌊㌋㌌㌍㌎㌏㌐㌑㌒㌓㌔㌕㌖㌗㌘㌙㌚㌛㌜㌝㌞㌟㌠㌡㌢㌣㌤㌥㌦㌧㌨㌩㌪㌫㌬㌭㌮㌯㌰㌱㌲㌳㌴㌵㌶㌷㌸㌹㌺㌻㌼㌽㌾㌿㍀㍁㍂㍃㍄㍅㍆㍇㍈㍉㍊㍋㍌㍍㍎㍏㍐㍑㍒㍓㍔㍕㍖㍗㍘㍙㍚㍛㍜㍝㍞㍟㍠㍡㍢㍣㍤㍥㍦㍧㍨㍩㍪㍫㍬㍭㍮㍯㍰㍱㍲㍳㍴㍵㍶㍷㍸㍹㍺㍻㍼㍽㍾㍿㎀㎁㎂㎃㎄㎅㎆㎇㎈㎉㎊㎋㎌㎍㎎㎏㎐㎑㎒㎓㎔㎕㎖㎗㎘㎙㎚㎛㎜㎝㎞㎟㎠㎡㎢㎣㎤㎥㎦㎧㎨㎩㎪㎫㎬㎭㎮㎯㎰㎱㎲㎳㎴㎵㎶㎷㎸㎹㎺㎻㎼㎽㎾㎿㏀㏁㏂㏃㏄㏅㏆㏇㏈㏉㏊㏋㏌㏍㏎㏏㏐㏑㏒㏓㏔㏕㏖㏗㏘㏙㏚㏛㏜㏝㏞㏟㏠㏡㏢㏣㏤㏥㏦㏧㏨㏩㏪㏫㏬㏭㏮㏯㏰㏱㏲㏳㏴㏵㏶㏷㏸㏹㏺㏻㏼㏽㏾㏿', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibility}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibility}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00283() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('㐀䶵', ',') satisfies matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionA}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionA}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00284() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('一\u9fff,一丁丂七丄丅丆万丈三上下丌不与丏丐丑丒专且丕世丗丘丙业丛东丝丞丟丠両丢丣两严並丧丨丩个丫丬中丮丯丰丱串丳临丵丶丷丸丹为主丼丽举丿乀乁乂乃乄久乆乇么义乊之乌乍乎乏乐乑乒乓乔乕乖乗乘乙乚乛乜九乞也习乡乢乣乤乥书乧乨乩乪乫乬乭乮乯买乱乲乳乴乵乶乷乸乹乺乻乼乽乾乿亀亁亂亃亄亅了亇予争亊事二亍于亏亐云互亓五井亖亗亘亙亚些亜亝亞亟亠亡亢亣交亥亦产亨亩亪享京亭亮亯亰亱亲亳亴亵亶亷亸亹人亻亼亽亾亿什仁仂仃仄仅仆仇仈仉今介仌仍从仏仐仑仒仓仔仕他仗付仙仚仛仜仝仞仟仠仡仢代令以仦仧仨仩仪仫们仭仮仯仰仱仲仳仴仵件价仸仹仺任仼份仾仿伀企伂伃伄伅伆伇伈伉伊伋伌伍伎伏伐休伒伓伔伕伖众优伙会伛伜伝伞伟传伡伢伣伤伥伦伧伨伩伪伫伬伭伮伯估伱伲伳伴伵伶伷伸伹伺伻似伽伾伿佀佁佂佃佄佅但佇佈佉佊佋佌位低住佐佑佒体佔何佖佗佘余佚佛作佝佞佟你佡佢佣佤佥佦佧佨佩佪佫佬佭佮佯佰佱佲佳佴併佶佷佸佹佺佻佼佽佾使侀侁侂侃侄侅來侇侈侉侊例侌侍侎侏侐侑侒侓侔侕侖侗侘侙侚供侜依侞侟侠価侢侣侤侥侦侧侨侩侪侫侬侭侮侯侰侱侲侳侴侵侶侷侸侹侺侻侼侽侾便俀俁係促俄俅俆俇俈俉俊俋俌俍俎俏俐俑俒俓俔俕俖俗俘俙俚俛俜保俞俟俠信俢俣俤俥俦俧俨俩俪俫俬俭修俯俰俱俲俳俴俵俶俷俸俹俺俻俼俽俾俿倀倁倂倃倄倅倆倇倈倉倊個倌倍倎倏倐們倒倓倔倕倖倗倘候倚倛倜倝倞借倠倡倢倣値倥倦倧倨倩倪倫倬倭倮倯倰倱倲倳倴倵倶倷倸倹债倻值倽倾倿偀偁偂偃偄偅偆假偈偉偊偋偌偍偎偏偐偑偒偓偔偕偖偗偘偙做偛停偝偞偟偠偡偢偣偤健偦偧偨偩偪偫偬偭偮偯偰偱偲偳側偵偶偷偸偹偺偻偼偽偾偿傀傁傂傃傄傅傆傇傈傉傊傋傌傍傎傏傐傑傒傓傔傕傖傗傘備傚傛傜傝傞傟傠傡傢傣傤傥傦傧储傩傪傫催傭傮傯傰傱傲傳傴債傶傷傸傹傺傻傼傽傾傿僀僁僂僃僄僅僆僇僈僉僊僋僌働僎像僐僑僒僓僔僕僖僗僘僙僚僛僜僝僞僟僠僡僢僣僤僥僦僧僨僩僪僫僬僭僮僯僰僱僲僳僴僵僶僷僸價僺僻僼僽僾僿儀儁儂儃億儅儆儇儈儉儊儋儌儍儎儏儐儑儒儓儔儕儖儗儘儙儚儛儜儝儞償儠儡儢儣儤儥儦儧儨儩優儫儬儭儮儯儰儱儲儳儴儵儶儷儸儹儺儻儼儽儾儿兀允兂元兄充兆兇先光兊克兌免兎兏児兑兒兓兔兕兖兗兘兙党兛兜兝兞兟兠兡兢兣兤入兦內全兩兪八公六兮兯兰共兲关兴兵其具典兹兺养兼兽兾兿冀冁冂冃冄内円冇冈冉冊冋册再冎冏冐冑冒冓冔冕冖冗冘写冚军农冝冞冟冠冡冢冣冤冥冦冧冨冩冪冫冬冭冮冯冰冱冲决冴况冶冷冸冹冺冻冼冽冾冿净凁凂凃凄凅准凇凈凉凊凋凌凍凎减凐凑凒凓凔凕凖凗凘凙凚凛凜凝凞凟几凡凢凣凤凥処凧凨凩凪凫凬凭凮凯凰凱凲凳凴凵凶凷凸凹出击凼函凾凿刀刁刂刃刄刅分切刈刉刊刋刌刍刎刏刐刑划刓刔刕刖列刘则刚创刜初刞刟删刡刢刣判別刦刧刨利刪别刬刭刮刯到刱刲刳刴刵制刷券刹刺刻刼刽刾刿剀剁剂剃剄剅剆則剈剉削剋剌前剎剏剐剑剒剓剔剕剖剗剘剙剚剛剜剝剞剟剠剡剢剣剤剥剦剧剨剩剪剫剬剭剮副剰剱割剳剴創剶剷剸剹剺剻剼剽剾剿劀劁劂劃劄劅劆劇劈劉劊劋劌劍劎劏劐劑劒劓劔劕劖劗劘劙劚力劜劝办功加务劢劣劤劥劦劧动助努劫劬劭劮劯劰励劲劳労劵劶劷劸効劺劻劼劽劾势勀勁勂勃勄勅勆勇勈勉勊勋勌勍勎勏勐勑勒勓勔動勖勗勘務勚勛勜勝勞募勠勡勢勣勤勥勦勧勨勩勪勫勬勭勮勯勰勱勲勳勴勵勶勷勸勹勺勻勼勽勾勿匀匁匂匃匄包匆匇匈匉匊匋匌匍匎匏匐匑匒匓匔匕化北匘匙匚匛匜匝匞匟匠匡匢匣匤匥匦匧匨匩匪匫匬匭匮匯匰匱匲匳匴匵匶匷匸匹区医匼匽匾匿區十卂千卄卅卆升午卉半卋卌卍华协卐卑卒卓協单卖南単卙博卛卜卝卞卟占卡卢卣卤卥卦卧卨卩卪卫卬卭卮卯印危卲即却卵卶卷卸卹卺卻卼卽卾卿厀厁厂厃厄厅历厇厈厉厊压厌厍厎厏厐厑厒厓厔厕厖厗厘厙厚厛厜厝厞原厠厡厢厣厤厥厦厧厨厩厪厫厬厭厮厯厰厱厲厳厴厵厶厷厸厹厺去厼厽厾县叀叁参參叄叅叆叇又叉及友双反収叏叐发叒叓叔叕取受变叙叚叛叜叝叞叟叠叡叢口古句另叧叨叩只叫召叭叮可台叱史右叴叵叶号司叹叺叻叼叽叾叿吀吁吂吃各吅吆吇合吉吊吋同名后吏吐向吒吓吔吕吖吗吘吙吚君吜吝吞吟吠吡吢吣吤吥否吧吨吩吪含听吭吮启吰吱吲吳吴吵吶吷吸吹吺吻吼吽吾吿呀呁呂呃呄呅呆呇呈呉告呋呌呍呎呏呐呑呒呓呔呕呖呗员呙呚呛呜呝呞呟呠呡呢呣呤呥呦呧周呩呪呫呬呭呮呯呰呱呲味呴呵呶呷呸呹呺呻呼命呾呿咀咁咂咃咄咅咆咇咈咉咊咋和咍咎咏咐咑咒咓咔咕咖咗咘咙咚咛咜咝咞咟咠咡咢咣咤咥咦咧咨咩咪咫咬咭咮咯咰咱咲咳咴咵咶咷咸咹咺咻咼咽咾咿哀品哂哃哄哅哆哇哈哉哊哋哌响哎哏哐哑哒哓哔哕哖哗哘哙哚哛哜哝哞哟哠員哢哣哤哥哦哧哨哩哪哫哬哭哮哯哰哱哲哳哴哵哶哷哸哹哺哻哼哽哾哿唀唁唂唃唄唅唆唇唈唉唊唋唌唍唎唏唐唑唒唓唔唕唖唗唘唙唚唛唜唝唞唟唠唡唢唣唤唥唦唧唨唩唪唫唬唭售唯唰唱唲唳唴唵唶唷唸唹唺唻唼唽唾唿啀啁啂啃啄啅商啇啈啉啊啋啌啍啎問啐啑啒啓啔啕啖啗啘啙啚啛啜啝啞啟啠啡啢啣啤啥啦啧啨啩啪啫啬啭啮啯啰啱啲啳啴啵啶啷啸啹啺啻啼啽啾啿喀喁喂喃善喅喆喇喈喉喊喋喌喍喎喏喐喑喒喓喔喕喖喗喘喙喚喛喜喝喞喟喠喡喢喣喤喥喦喧喨喩喪喫喬喭單喯喰喱喲喳喴喵営喷喸喹喺喻喼喽喾喿嗀嗁嗂嗃嗄嗅嗆嗇嗈嗉嗊嗋嗌嗍嗎嗏嗐嗑嗒嗓嗔嗕嗖嗗嗘嗙嗚嗛嗜嗝嗞嗟嗠嗡嗢嗣嗤嗥嗦嗧嗨嗩嗪嗫嗬嗭嗮嗯嗰嗱嗲嗳嗴嗵嗶嗷嗸嗹嗺嗻嗼嗽嗾嗿嘀嘁嘂嘃嘄嘅嘆嘇嘈嘉嘊嘋嘌嘍嘎嘏嘐嘑嘒嘓嘔嘕嘖嘗嘘嘙嘚嘛嘜嘝嘞嘟嘠嘡嘢嘣嘤嘥嘦嘧嘨嘩嘪嘫嘬嘭嘮嘯嘰嘱嘲嘳嘴嘵嘶嘷嘸嘹嘺嘻嘼嘽嘾嘿噀噁噂噃噄噅噆噇噈噉噊噋噌噍噎噏噐噑噒噓噔噕噖噗噘噙噚噛噜噝噞噟噠噡噢噣噤噥噦噧器噩噪噫噬噭噮噯噰噱噲噳噴噵噶噷噸噹噺噻噼噽噾噿嚀嚁嚂嚃嚄嚅嚆嚇嚈嚉嚊嚋嚌嚍嚎嚏嚐嚑嚒嚓嚔嚕嚖嚗嚘嚙嚚嚛嚜嚝嚞嚟嚠嚡嚢嚣嚤嚥嚦嚧嚨嚩嚪嚫嚬嚭嚮嚯嚰嚱嚲嚳嚴嚵嚶嚷嚸嚹嚺嚻嚼嚽嚾嚿囀囁囂囃囄囅囆囇囈囉囊囋囌囍囎囏囐囑囒囓囔囕囖囗囘囙囚四囜囝回囟因囡团団囤囥囦囧囨囩囪囫囬园囮囯困囱囲図围囵囶囷囸囹固囻囼国图囿圀圁圂圃圄圅圆圇圈圉圊國圌圍圎圏圐圑園圓圔圕圖圗團圙圚圛圜圝圞土圠圡圢圣圤圥圦圧在圩圪圫圬圭圮圯地圱圲圳圴圵圶圷圸圹场圻圼圽圾圿址坁坂坃坄坅坆均坈坉坊坋坌坍坎坏坐坑坒坓坔坕坖块坘坙坚坛坜坝坞坟坠坡坢坣坤坥坦坧坨坩坪坫坬坭坮坯坰坱坲坳坴坵坶坷坸坹坺坻坼坽坾坿垀垁垂垃垄垅垆垇垈垉垊型垌垍垎垏垐垑垒垓垔垕垖垗垘垙垚垛垜垝垞垟垠垡垢垣垤垥垦垧垨垩垪垫垬垭垮垯垰垱垲垳垴垵垶垷垸垹垺垻垼垽垾垿埀埁埂埃埄埅埆埇埈埉埊埋埌埍城埏埐埑埒埓埔埕埖埗埘埙埚埛埜埝埞域埠埡埢埣埤埥埦埧埨埩埪埫埬埭埮埯埰埱埲埳埴埵埶執埸培基埻埼埽埾埿堀堁堂堃堄堅堆堇堈堉堊堋堌堍堎堏堐堑堒堓堔堕堖堗堘堙堚堛堜堝堞堟堠堡堢堣堤堥堦堧堨堩堪堫堬堭堮堯堰報堲堳場堵堶堷堸堹堺堻堼堽堾堿塀塁塂塃塄塅塆塇塈塉塊塋塌塍塎塏塐塑塒塓塔塕塖塗塘塙塚塛塜塝塞塟塠塡塢塣塤塥塦塧塨塩塪填塬塭塮塯塰塱塲塳塴塵塶塷塸塹塺塻塼塽塾塿墀墁墂境墄墅墆墇墈墉墊墋墌墍墎墏墐墑墒墓墔墕墖増墘墙墚墛墜墝增墟墠墡墢墣墤墥墦墧墨墩墪墫墬墭墮墯墰墱墲墳墴墵墶墷墸墹墺墻墼墽墾墿壀壁壂壃壄壅壆壇壈壉壊壋壌壍壎壏壐壑壒壓壔壕壖壗壘壙壚壛壜壝壞壟壠壡壢壣壤壥壦壧壨壩壪士壬壭壮壯声壱売壳壴壵壶壷壸壹壺壻壼壽壾壿夀夁夂夃处夅夆备夈変夊夋夌复夎夏夐夑夒夓夔夕外夗夘夙多夛夜夝夞够夠夡夢夣夤夥夦大夨天太夫夬夭央夯夰失夲夳头夵夶夷夸夹夺夻夼夽夾夿奀奁奂奃奄奅奆奇奈奉奊奋奌奍奎奏奐契奒奓奔奕奖套奘奙奚奛奜奝奞奟奠奡奢奣奤奥奦奧奨奩奪奫奬奭奮奯奰奱奲女奴奵奶奷奸她奺奻奼好奾奿妀妁如妃妄妅妆妇妈妉妊妋妌妍妎妏妐妑妒妓妔妕妖妗妘妙妚妛妜妝妞妟妠妡妢妣妤妥妦妧妨妩妪妫妬妭妮妯妰妱妲妳妴妵妶妷妸妹妺妻妼妽妾妿姀姁姂姃姄姅姆姇姈姉姊始姌姍姎姏姐姑姒姓委姕姖姗姘姙姚姛姜姝姞姟姠姡姢姣姤姥姦姧姨姩姪姫姬姭姮姯姰姱姲姳姴姵姶姷姸姹姺姻姼姽姾姿娀威娂娃娄娅娆娇娈娉娊娋娌娍娎娏娐娑娒娓娔娕娖娗娘娙娚娛娜娝娞娟娠娡娢娣娤娥娦娧娨娩娪娫娬娭娮娯娰娱娲娳娴娵娶娷娸娹娺娻娼娽娾娿婀婁婂婃婄婅婆婇婈婉婊婋婌婍婎婏婐婑婒婓婔婕婖婗婘婙婚婛婜婝婞婟婠婡婢婣婤婥婦婧婨婩婪婫婬婭婮婯婰婱婲婳婴婵婶婷婸婹婺婻婼婽婾婿媀媁媂媃媄媅媆媇媈媉媊媋媌媍媎媏媐媑媒媓媔媕媖媗媘媙媚媛媜媝媞媟媠媡媢媣媤媥媦媧媨媩媪媫媬媭媮媯媰媱媲媳媴媵媶媷媸媹媺媻媼媽媾媿嫀嫁嫂嫃嫄嫅嫆嫇嫈嫉嫊嫋嫌嫍嫎嫏嫐嫑嫒嫓嫔嫕嫖嫗嫘嫙嫚嫛嫜嫝嫞嫟嫠嫡嫢嫣嫤嫥嫦嫧嫨嫩嫪嫫嫬嫭嫮嫯嫰嫱嫲嫳嫴嫵嫶嫷嫸嫹嫺嫻嫼嫽嫾嫿嬀嬁嬂嬃嬄嬅嬆嬇嬈嬉嬊嬋嬌嬍嬎嬏嬐嬑嬒嬓嬔嬕嬖嬗嬘嬙嬚嬛嬜嬝嬞嬟嬠嬡嬢嬣嬤嬥嬦嬧嬨嬩嬪嬫嬬嬭嬮嬯嬰嬱嬲嬳嬴嬵嬶嬷嬸嬹嬺嬻嬼嬽嬾嬿孀孁孂孃孄孅孆孇孈孉孊孋孌孍孎孏子孑孒孓孔孕孖字存孙孚孛孜孝孞孟孠孡孢季孤孥学孧孨孩孪孫孬孭孮孯孰孱孲孳孴孵孶孷學孹孺孻孼孽孾孿宀宁宂它宄宅宆宇守安宊宋完宍宎宏宐宑宒宓宔宕宖宗官宙定宛宜宝实実宠审客宣室宥宦宧宨宩宪宫宬宭宮宯宰宱宲害宴宵家宷宸容宺宻宼宽宾宿寀寁寂寃寄寅密寇寈寉寊寋富寍寎寏寐寑寒寓寔寕寖寗寘寙寚寛寜寝寞察寠寡寢寣寤寥實寧寨審寪寫寬寭寮寯寰寱寲寳寴寵寶寷寸对寺寻导寽対寿尀封専尃射尅将將專尉尊尋尌對導小尐少尒尓尔尕尖尗尘尙尚尛尜尝尞尟尠尡尢尣尤尥尦尧尨尩尪尫尬尭尮尯尰就尲尳尴尵尶尷尸尹尺尻尼尽尾尿局屁层屃屄居屆屇屈屉届屋屌屍屎屏屐屑屒屓屔展屖屗屘屙屚屛屜屝属屟屠屡屢屣層履屦屧屨屩屪屫屬屭屮屯屰山屲屳屴屵屶屷屸屹屺屻屼屽屾屿岀岁岂岃岄岅岆岇岈岉岊岋岌岍岎岏岐岑岒岓岔岕岖岗岘岙岚岛岜岝岞岟岠岡岢岣岤岥岦岧岨岩岪岫岬岭岮岯岰岱岲岳岴岵岶岷岸岹岺岻岼岽岾岿峀峁峂峃峄峅峆峇峈峉峊峋峌峍峎峏峐峑峒峓峔峕峖峗峘峙峚峛峜峝峞峟峠峡峢峣峤峥峦峧峨峩峪峫峬峭峮峯峰峱峲峳峴峵島峷峸峹峺峻峼峽峾峿崀崁崂崃崄崅崆崇崈崉崊崋崌崍崎崏崐崑崒崓崔崕崖崗崘崙崚崛崜崝崞崟崠崡崢崣崤崥崦崧崨崩崪崫崬崭崮崯崰崱崲崳崴崵崶崷崸崹崺崻崼崽崾崿嵀嵁嵂嵃嵄嵅嵆嵇嵈嵉嵊嵋嵌嵍嵎嵏嵐嵑嵒嵓嵔嵕嵖嵗嵘嵙嵚嵛嵜嵝嵞嵟嵠嵡嵢嵣嵤嵥嵦嵧嵨嵩嵪嵫嵬嵭嵮嵯嵰嵱嵲嵳嵴嵵嵶嵷嵸嵹嵺嵻嵼嵽嵾嵿嶀嶁嶂嶃嶄嶅嶆嶇嶈嶉嶊嶋嶌嶍嶎嶏嶐嶑嶒嶓嶔嶕嶖嶗嶘嶙嶚嶛嶜嶝嶞嶟嶠嶡嶢嶣嶤嶥嶦嶧嶨嶩嶪嶫嶬嶭嶮嶯嶰嶱嶲嶳嶴嶵嶶嶷嶸嶹嶺嶻嶼嶽嶾嶿巀巁巂巃巄巅巆巇巈巉巊巋巌巍巎巏巐巑巒巓巔巕巖巗巘巙巚巛巜川州巟巠巡巢巣巤工左巧巨巩巪巫巬巭差巯巰己已巳巴巵巶巷巸巹巺巻巼巽巾巿帀币市布帄帅帆帇师帉帊帋希帍帎帏帐帑帒帓帔帕帖帗帘帙帚帛帜帝帞帟帠帡帢帣帤帥带帧帨帩帪師帬席帮帯帰帱帲帳帴帵帶帷常帹帺帻帼帽帾帿幀幁幂幃幄幅幆幇幈幉幊幋幌幍幎幏幐幑幒幓幔幕幖幗幘幙幚幛幜幝幞幟幠幡幢幣幤幥幦幧幨幩幪幫幬幭幮幯幰幱干平年幵并幷幸幹幺幻幼幽幾广庀庁庂広庄庅庆庇庈庉床庋庌庍庎序庐庑庒库应底庖店庘庙庚庛府庝庞废庠庡庢庣庤庥度座庨庩庪庫庬庭庮庯庰庱庲庳庴庵庶康庸庹庺庻庼庽庾庿廀廁廂廃廄廅廆廇廈廉廊廋廌廍廎廏廐廑廒廓廔廕廖廗廘廙廚廛廜廝廞廟廠廡廢廣廤廥廦廧廨廩廪廫廬廭廮廯廰廱廲廳廴廵延廷廸廹建廻廼廽廾廿开弁异弃弄弅弆弇弈弉弊弋弌弍弎式弐弑弒弓弔引弖弗弘弙弚弛弜弝弞弟张弡弢弣弤弥弦弧弨弩弪弫弬弭弮弯弰弱弲弳弴張弶強弸弹强弻弼弽弾弿彀彁彂彃彄彅彆彇彈彉彊彋彌彍彎彏彐彑归当彔录彖彗彘彙彚彛彜彝彞彟彠彡形彣彤彥彦彧彨彩彪彫彬彭彮彯彰影彲彳彴彵彶彷彸役彺彻彼彽彾彿往征徂徃径待徆徇很徉徊律後徍徎徏徐徑徒従徔徕徖得徘徙徚徛徜徝從徟徠御徢徣徤徥徦徧徨復循徫徬徭微徯徰徱徲徳徴徵徶德徸徹徺徻徼徽徾徿忀忁忂心忄必忆忇忈忉忊忋忌忍忎忏忐忑忒忓忔忕忖志忘忙忚忛応忝忞忟忠忡忢忣忤忥忦忧忨忩忪快忬忭忮忯忰忱忲忳忴念忶忷忸忹忺忻忼忽忾忿怀态怂怃怄怅怆怇怈怉怊怋怌怍怎怏怐怑怒怓怔怕怖怗怘怙怚怛怜思怞怟怠怡怢怣怤急怦性怨怩怪怫怬怭怮怯怰怱怲怳怴怵怶怷怸怹怺总怼怽怾怿恀恁恂恃恄恅恆恇恈恉恊恋恌恍恎恏恐恑恒恓恔恕恖恗恘恙恚恛恜恝恞恟恠恡恢恣恤恥恦恧恨恩恪恫恬恭恮息恰恱恲恳恴恵恶恷恸恹恺恻恼恽恾恿悀悁悂悃悄悅悆悇悈悉悊悋悌悍悎悏悐悑悒悓悔悕悖悗悘悙悚悛悜悝悞悟悠悡悢患悤悥悦悧您悩悪悫悬悭悮悯悰悱悲悳悴悵悶悷悸悹悺悻悼悽悾悿惀惁惂惃惄情惆惇惈惉惊惋惌惍惎惏惐惑惒惓惔惕惖惗惘惙惚惛惜惝惞惟惠惡惢惣惤惥惦惧惨惩惪惫惬惭惮惯惰惱惲想惴惵惶惷惸惹惺惻惼惽惾惿愀愁愂愃愄愅愆愇愈愉愊愋愌愍愎意愐愑愒愓愔愕愖愗愘愙愚愛愜愝愞感愠愡愢愣愤愥愦愧愨愩愪愫愬愭愮愯愰愱愲愳愴愵愶愷愸愹愺愻愼愽愾愿慀慁慂慃慄慅慆慇慈慉慊態慌慍慎慏慐慑慒慓慔慕慖慗慘慙慚慛慜慝慞慟慠慡慢慣慤慥慦慧慨慩慪慫慬慭慮慯慰慱慲慳慴慵慶慷慸慹慺慻慼慽慾慿憀憁憂憃憄憅憆憇憈憉憊憋憌憍憎憏憐憑憒憓憔憕憖憗憘憙憚憛憜憝憞憟憠憡憢憣憤憥憦憧憨憩憪憫憬憭憮憯憰憱憲憳憴憵憶憷憸憹憺憻憼憽憾憿懀懁懂懃懄懅懆懇懈應懊懋懌懍懎懏懐懑懒懓懔懕懖懗懘懙懚懛懜懝懞懟懠懡懢懣懤懥懦懧懨懩懪懫懬懭懮懯懰懱懲懳懴懵懶懷懸懹懺懻懼懽懾懿戀戁戂戃戄戅戆戇戈戉戊戋戌戍戎戏成我戒戓戔戕或戗战戙戚戛戜戝戞戟戠戡戢戣戤戥戦戧戨戩截戫戬戭戮戯戰戱戲戳戴戵戶户戸戹戺戻戼戽戾房所扁扂扃扄扅扆扇扈扉扊手扌才扎扏扐扑扒打扔払扖扗托扙扚扛扜扝扞扟扠扡扢扣扤扥扦执扨扩扪扫扬扭扮扯扰扱扲扳扴扵扶扷扸批扺扻扼扽找承技抁抂抃抄抅抆抇抈抉把抋抌抍抎抏抐抑抒抓抔投抖抗折抙抚抛抜抝択抟抠抡抢抣护报抦抧抨抩抪披抬抭抮抯抰抱抲抳抴抵抶抷抸抹抺抻押抽抾抿拀拁拂拃拄担拆拇拈拉拊拋拌拍拎拏拐拑拒拓拔拕拖拗拘拙拚招拜拝拞拟拠拡拢拣拤拥拦拧拨择拪拫括拭拮拯拰拱拲拳拴拵拶拷拸拹拺拻拼拽拾拿挀持挂挃挄挅挆指挈按挊挋挌挍挎挏挐挑挒挓挔挕挖挗挘挙挚挛挜挝挞挟挠挡挢挣挤挥挦挧挨挩挪挫挬挭挮振挰挱挲挳挴挵挶挷挸挹挺挻挼挽挾挿捀捁捂捃捄捅捆捇捈捉捊捋捌捍捎捏捐捑捒捓捔捕捖捗捘捙捚捛捜捝捞损捠捡换捣捤捥捦捧捨捩捪捫捬捭据捯捰捱捲捳捴捵捶捷捸捹捺捻捼捽捾捿掀掁掂掃掄掅掆掇授掉掊掋掌掍掎掏掐掑排掓掔掕掖掗掘掙掚掛掜掝掞掟掠採探掣掤接掦控推掩措掫掬掭掮掯掰掱掲掳掴掵掶掷掸掹掺掻掼掽掾掿揀揁揂揃揄揅揆揇揈揉揊揋揌揍揎描提揑插揓揔揕揖揗揘揙揚換揜揝揞揟揠握揢揣揤揥揦揧揨揩揪揫揬揭揮揯揰揱揲揳援揵揶揷揸揹揺揻揼揽揾揿搀搁搂搃搄搅搆搇搈搉搊搋搌損搎搏搐搑搒搓搔搕搖搗搘搙搚搛搜搝搞搟搠搡搢搣搤搥搦搧搨搩搪搫搬搭搮搯搰搱搲搳搴搵搶搷搸搹携搻搼搽搾搿摀摁摂摃摄摅摆摇摈摉摊摋摌摍摎摏摐摑摒摓摔摕摖摗摘摙摚摛摜摝摞摟摠摡摢摣摤摥摦摧摨摩摪摫摬摭摮摯摰摱摲摳摴摵摶摷摸摹摺摻摼摽摾摿撀撁撂撃撄撅撆撇撈撉撊撋撌撍撎撏撐撑撒撓撔撕撖撗撘撙撚撛撜撝撞撟撠撡撢撣撤撥撦撧撨撩撪撫撬播撮撯撰撱撲撳撴撵撶撷撸撹撺撻撼撽撾撿擀擁擂擃擄擅擆擇擈擉擊擋擌操擎擏擐擑擒擓擔擕擖擗擘擙據擛擜擝擞擟擠擡擢擣擤擥擦擧擨擩擪擫擬擭擮擯擰擱擲擳擴擵擶擷擸擹擺擻擼擽擾擿攀攁攂攃攄攅攆攇攈攉攊攋攌攍攎攏攐攑攒攓攔攕攖攗攘攙攚攛攜攝攞攟攠攡攢攣攤攥攦攧攨攩攪攫攬攭攮支攰攱攲攳攴攵收攷攸改攺攻攼攽放政敀敁敂敃敄故敆敇效敉敊敋敌敍敎敏敐救敒敓敔敕敖敗敘教敚敛敜敝敞敟敠敡敢散敤敥敦敧敨敩敪敫敬敭敮敯数敱敲敳整敵敶敷數敹敺敻敼敽敾敿斀斁斂斃斄斅斆文斈斉斊斋斌斍斎斏斐斑斒斓斔斕斖斗斘料斚斛斜斝斞斟斠斡斢斣斤斥斦斧斨斩斪斫斬断斮斯新斱斲斳斴斵斶斷斸方斺斻於施斾斿旀旁旂旃旄旅旆旇旈旉旊旋旌旍旎族旐旑旒旓旔旕旖旗旘旙旚旛旜旝旞旟无旡既旣旤日旦旧旨早旪旫旬旭旮旯旰旱旲旳旴旵时旷旸旹旺旻旼旽旾旿昀昁昂昃昄昅昆昇昈昉昊昋昌昍明昏昐昑昒易昔昕昖昗昘昙昚昛昜昝昞星映昡昢昣昤春昦昧昨昩昪昫昬昭昮是昰昱昲昳昴昵昶昷昸昹昺昻昼昽显昿晀晁時晃晄晅晆晇晈晉晊晋晌晍晎晏晐晑晒晓晔晕晖晗晘晙晚晛晜晝晞晟晠晡晢晣晤晥晦晧晨晩晪晫晬晭普景晰晱晲晳晴晵晶晷晸晹智晻晼晽晾晿暀暁暂暃暄暅暆暇暈暉暊暋暌暍暎暏暐暑暒暓暔暕暖暗暘暙暚暛暜暝暞暟暠暡暢暣暤暥暦暧暨暩暪暫暬暭暮暯暰暱暲暳暴暵暶暷暸暹暺暻暼暽暾暿曀曁曂曃曄曅曆曇曈曉曊曋曌曍曎曏曐曑曒曓曔曕曖曗曘曙曚曛曜曝曞曟曠曡曢曣曤曥曦曧曨曩曪曫曬曭曮曯曰曱曲曳更曵曶曷書曹曺曻曼曽曾替最朁朂會朄朅朆朇月有朊朋朌服朎朏朐朑朒朓朔朕朖朗朘朙朚望朜朝朞期朠朡朢朣朤朥朦朧木朩未末本札朮术朰朱朲朳朴朵朶朷朸朹机朻朼朽朾朿杀杁杂权杄杅杆杇杈杉杊杋杌杍李杏材村杒杓杔杕杖杗杘杙杚杛杜杝杞束杠条杢杣杤来杦杧杨杩杪杫杬杭杮杯杰東杲杳杴杵杶杷杸杹杺杻杼杽松板枀极枂枃构枅枆枇枈枉枊枋枌枍枎枏析枑枒枓枔枕枖林枘枙枚枛果枝枞枟枠枡枢枣枤枥枦枧枨枩枪枫枬枭枮枯枰枱枲枳枴枵架枷枸枹枺枻枼枽枾枿柀柁柂柃柄柅柆柇柈柉柊柋柌柍柎柏某柑柒染柔柕柖柗柘柙柚柛柜柝柞柟柠柡柢柣柤查柦柧柨柩柪柫柬柭柮柯柰柱柲柳柴柵柶柷柸柹柺査柼柽柾柿栀栁栂栃栄栅栆标栈栉栊栋栌栍栎栏栐树栒栓栔栕栖栗栘栙栚栛栜栝栞栟栠校栢栣栤栥栦栧栨栩株栫栬栭栮栯栰栱栲栳栴栵栶样核根栺栻格栽栾栿桀桁桂桃桄桅框桇案桉桊桋桌桍桎桏桐桑桒桓桔桕桖桗桘桙桚桛桜桝桞桟桠桡桢档桤桥桦桧桨桩桪桫桬桭桮桯桰桱桲桳桴桵桶桷桸桹桺桻桼桽桾桿梀梁梂梃梄梅梆梇梈梉梊梋梌梍梎梏梐梑梒梓梔梕梖梗梘梙梚梛梜條梞梟梠梡梢梣梤梥梦梧梨梩梪梫梬梭梮梯械梱梲梳梴梵梶梷梸梹梺梻梼梽梾梿检棁棂棃棄棅棆棇棈棉棊棋棌棍棎棏棐棑棒棓棔棕棖棗棘棙棚棛棜棝棞棟棠棡棢棣棤棥棦棧棨棩棪棫棬棭森棯棰棱棲棳棴棵棶棷棸棹棺棻棼棽棾棿椀椁椂椃椄椅椆椇椈椉椊椋椌植椎椏椐椑椒椓椔椕椖椗椘椙椚椛検椝椞椟椠椡椢椣椤椥椦椧椨椩椪椫椬椭椮椯椰椱椲椳椴椵椶椷椸椹椺椻椼椽椾椿楀楁楂楃楄楅楆楇楈楉楊楋楌楍楎楏楐楑楒楓楔楕楖楗楘楙楚楛楜楝楞楟楠楡楢楣楤楥楦楧楨楩楪楫楬業楮楯楰楱楲楳楴極楶楷楸楹楺楻楼楽楾楿榀榁概榃榄榅榆榇榈榉榊榋榌榍榎榏榐榑榒榓榔榕榖榗榘榙榚榛榜榝榞榟榠榡榢榣榤榥榦榧榨榩榪榫榬榭榮榯榰榱榲榳榴榵榶榷榸榹榺榻榼榽榾榿槀槁槂槃槄槅槆槇槈槉槊構槌槍槎槏槐槑槒槓槔槕槖槗様槙槚槛槜槝槞槟槠槡槢槣槤槥槦槧槨槩槪槫槬槭槮槯槰槱槲槳槴槵槶槷槸槹槺槻槼槽槾槿樀樁樂樃樄樅樆樇樈樉樊樋樌樍樎樏樐樑樒樓樔樕樖樗樘標樚樛樜樝樞樟樠模樢樣樤樥樦樧樨権横樫樬樭樮樯樰樱樲樳樴樵樶樷樸樹樺樻樼樽樾樿橀橁橂橃橄橅橆橇橈橉橊橋橌橍橎橏橐橑橒橓橔橕橖橗橘橙橚橛橜橝橞機橠橡橢橣橤橥橦橧橨橩橪橫橬橭橮橯橰橱橲橳橴橵橶橷橸橹橺橻橼橽橾橿檀檁檂檃檄檅檆檇檈檉檊檋檌檍檎檏檐檑檒檓檔檕檖檗檘檙檚檛檜檝檞檟檠檡檢檣檤檥檦檧檨檩檪檫檬檭檮檯檰檱檲檳檴檵檶檷檸檹檺檻檼檽檾檿櫀櫁櫂櫃櫄櫅櫆櫇櫈櫉櫊櫋櫌櫍櫎櫏櫐櫑櫒櫓櫔櫕櫖櫗櫘櫙櫚櫛櫜櫝櫞櫟櫠櫡櫢櫣櫤櫥櫦櫧櫨櫩櫪櫫櫬櫭櫮櫯櫰櫱櫲櫳櫴櫵櫶櫷櫸櫹櫺櫻櫼櫽櫾櫿欀欁欂欃欄欅欆欇欈欉權欋欌欍欎欏欐欑欒欓欔欕欖欗欘欙欚欛欜欝欞欟欠次欢欣欤欥欦欧欨欩欪欫欬欭欮欯欰欱欲欳欴欵欶欷欸欹欺欻欼欽款欿歀歁歂歃歄歅歆歇歈歉歊歋歌歍歎歏歐歑歒歓歔歕歖歗歘歙歚歛歜歝歞歟歠歡止正此步武歧歨歩歪歫歬歭歮歯歰歱歲歳歴歵歶歷歸歹歺死歼歽歾歿殀殁殂殃殄殅殆殇殈殉殊残殌殍殎殏殐殑殒殓殔殕殖殗殘殙殚殛殜殝殞殟殠殡殢殣殤殥殦殧殨殩殪殫殬殭殮殯殰殱殲殳殴段殶殷殸殹殺殻殼殽殾殿毀毁毂毃毄毅毆毇毈毉毊毋毌母毎每毐毑毒毓比毕毖毗毘毙毚毛毜毝毞毟毠毡毢毣毤毥毦毧毨毩毪毫毬毭毮毯毰毱毲毳毴毵毶毷毸毹毺毻毼毽毾毿氀氁氂氃氄氅氆氇氈氉氊氋氌氍氎氏氐民氒氓气氕氖気氘氙氚氛氜氝氞氟氠氡氢氣氤氥氦氧氨氩氪氫氬氭氮氯氰氱氲氳水氵氶氷永氹氺氻氼氽氾氿汀汁求汃汄汅汆汇汈汉汊汋汌汍汎汏汐汑汒汓汔汕汖汗汘汙汚汛汜汝汞江池污汢汣汤汥汦汧汨汩汪汫汬汭汮汯汰汱汲汳汴汵汶汷汸汹決汻汼汽汾汿沀沁沂沃沄沅沆沇沈沉沊沋沌沍沎沏沐沑沒沓沔沕沖沗沘沙沚沛沜沝沞沟沠没沢沣沤沥沦沧沨沩沪沫沬沭沮沯沰沱沲河沴沵沶沷沸油沺治沼沽沾沿泀況泂泃泄泅泆泇泈泉泊泋泌泍泎泏泐泑泒泓泔法泖泗泘泙泚泛泜泝泞泟泠泡波泣泤泥泦泧注泩泪泫泬泭泮泯泰泱泲泳泴泵泶泷泸泹泺泻泼泽泾泿洀洁洂洃洄洅洆洇洈洉洊洋洌洍洎洏洐洑洒洓洔洕洖洗洘洙洚洛洜洝洞洟洠洡洢洣洤津洦洧洨洩洪洫洬洭洮洯洰洱洲洳洴洵洶洷洸洹洺活洼洽派洿浀流浂浃浄浅浆浇浈浉浊测浌浍济浏浐浑浒浓浔浕浖浗浘浙浚浛浜浝浞浟浠浡浢浣浤浥浦浧浨浩浪浫浬浭浮浯浰浱浲浳浴浵浶海浸浹浺浻浼浽浾浿涀涁涂涃涄涅涆涇消涉涊涋涌涍涎涏涐涑涒涓涔涕涖涗涘涙涚涛涜涝涞涟涠涡涢涣涤涥润涧涨涩涪涫涬涭涮涯涰涱液涳涴涵涶涷涸涹涺涻涼涽涾涿淀淁淂淃淄淅淆淇淈淉淊淋淌淍淎淏淐淑淒淓淔淕淖淗淘淙淚淛淜淝淞淟淠淡淢淣淤淥淦淧淨淩淪淫淬淭淮淯淰深淲淳淴淵淶混淸淹淺添淼淽淾淿渀渁渂渃渄清渆渇済渉渊渋渌渍渎渏渐渑渒渓渔渕渖渗渘渙渚減渜渝渞渟渠渡渢渣渤渥渦渧渨温渪渫測渭渮港渰渱渲渳渴渵渶渷游渹渺渻渼渽渾渿湀湁湂湃湄湅湆湇湈湉湊湋湌湍湎湏湐湑湒湓湔湕湖湗湘湙湚湛湜湝湞湟湠湡湢湣湤湥湦湧湨湩湪湫湬湭湮湯湰湱湲湳湴湵湶湷湸湹湺湻湼湽湾湿満溁溂溃溄溅溆溇溈溉溊溋溌溍溎溏源溑溒溓溔溕準溗溘溙溚溛溜溝溞溟溠溡溢溣溤溥溦溧溨溩溪溫溬溭溮溯溰溱溲溳溴溵溶溷溸溹溺溻溼溽溾溿滀滁滂滃滄滅滆滇滈滉滊滋滌滍滎滏滐滑滒滓滔滕滖滗滘滙滚滛滜滝滞滟滠满滢滣滤滥滦滧滨滩滪滫滬滭滮滯滰滱滲滳滴滵滶滷滸滹滺滻滼滽滾滿漀漁漂漃漄漅漆漇漈漉漊漋漌漍漎漏漐漑漒漓演漕漖漗漘漙漚漛漜漝漞漟漠漡漢漣漤漥漦漧漨漩漪漫漬漭漮漯漰漱漲漳漴漵漶漷漸漹漺漻漼漽漾漿潀潁潂潃潄潅潆潇潈潉潊潋潌潍潎潏潐潑潒潓潔潕潖潗潘潙潚潛潜潝潞潟潠潡潢潣潤潥潦潧潨潩潪潫潬潭潮潯潰潱潲潳潴潵潶潷潸潹潺潻潼潽潾潿澀澁澂澃澄澅澆澇澈澉澊澋澌澍澎澏澐澑澒澓澔澕澖澗澘澙澚澛澜澝澞澟澠澡澢澣澤澥澦澧澨澩澪澫澬澭澮澯澰澱澲澳澴澵澶澷澸澹澺澻澼澽澾澿激濁濂濃濄濅濆濇濈濉濊濋濌濍濎濏濐濑濒濓濔濕濖濗濘濙濚濛濜濝濞濟濠濡濢濣濤濥濦濧濨濩濪濫濬濭濮濯濰濱濲濳濴濵濶濷濸濹濺濻濼濽濾濿瀀瀁瀂瀃瀄瀅瀆瀇瀈瀉瀊瀋瀌瀍瀎瀏瀐瀑瀒瀓瀔瀕瀖瀗瀘瀙瀚瀛瀜瀝瀞瀟瀠瀡瀢瀣瀤瀥瀦瀧瀨瀩瀪瀫瀬瀭瀮瀯瀰瀱瀲瀳瀴瀵瀶瀷瀸瀹瀺瀻瀼瀽瀾瀿灀灁灂灃灄灅灆灇灈灉灊灋灌灍灎灏灐灑灒灓灔灕灖灗灘灙灚灛灜灝灞灟灠灡灢灣灤灥灦灧灨灩灪火灬灭灮灯灰灱灲灳灴灵灶灷灸灹灺灻灼災灾灿炀炁炂炃炄炅炆炇炈炉炊炋炌炍炎炏炐炑炒炓炔炕炖炗炘炙炚炛炜炝炞炟炠炡炢炣炤炥炦炧炨炩炪炫炬炭炮炯炰炱炲炳炴炵炶炷炸点為炻炼炽炾炿烀烁烂烃烄烅烆烇烈烉烊烋烌烍烎烏烐烑烒烓烔烕烖烗烘烙烚烛烜烝烞烟烠烡烢烣烤烥烦烧烨烩烪烫烬热烮烯烰烱烲烳烴烵烶烷烸烹烺烻烼烽烾烿焀焁焂焃焄焅焆焇焈焉焊焋焌焍焎焏焐焑焒焓焔焕焖焗焘焙焚焛焜焝焞焟焠無焢焣焤焥焦焧焨焩焪焫焬焭焮焯焰焱焲焳焴焵然焷焸焹焺焻焼焽焾焿煀煁煂煃煄煅煆煇煈煉煊煋煌煍煎煏煐煑煒煓煔煕煖煗煘煙煚煛煜煝煞煟煠煡煢煣煤煥煦照煨煩煪煫煬煭煮煯煰煱煲煳煴煵煶煷煸煹煺煻煼煽煾煿熀熁熂熃熄熅熆熇熈熉熊熋熌熍熎熏熐熑熒熓熔熕熖熗熘熙熚熛熜熝熞熟熠熡熢熣熤熥熦熧熨熩熪熫熬熭熮熯熰熱熲熳熴熵熶熷熸熹熺熻熼熽熾熿燀燁燂燃燄燅燆燇燈燉燊燋燌燍燎燏燐燑燒燓燔燕燖燗燘燙燚燛燜燝燞營燠燡燢燣燤燥燦燧燨燩燪燫燬燭燮燯燰燱燲燳燴燵燶燷燸燹燺燻燼燽燾燿爀爁爂爃爄爅爆爇爈爉爊爋爌爍爎爏爐爑爒爓爔爕爖爗爘爙爚爛爜爝爞爟爠爡爢爣爤爥爦爧爨爩爪爫爬爭爮爯爰爱爲爳爴爵父爷爸爹爺爻爼爽爾爿牀牁牂牃牄牅牆片版牉牊牋牌牍牎牏牐牑牒牓牔牕牖牗牘牙牚牛牜牝牞牟牠牡牢牣牤牥牦牧牨物牪牫牬牭牮牯牰牱牲牳牴牵牶牷牸特牺牻牼牽牾牿犀犁犂犃犄犅犆犇犈犉犊犋犌犍犎犏犐犑犒犓犔犕犖犗犘犙犚犛犜犝犞犟犠犡犢犣犤犥犦犧犨犩犪犫犬犭犮犯犰犱犲犳犴犵状犷犸犹犺犻犼犽犾犿狀狁狂狃狄狅狆狇狈狉狊狋狌狍狎狏狐狑狒狓狔狕狖狗狘狙狚狛狜狝狞狟狠狡狢狣狤狥狦狧狨狩狪狫独狭狮狯狰狱狲狳狴狵狶狷狸狹狺狻狼狽狾狿猀猁猂猃猄猅猆猇猈猉猊猋猌猍猎猏猐猑猒猓猔猕猖猗猘猙猚猛猜猝猞猟猠猡猢猣猤猥猦猧猨猩猪猫猬猭献猯猰猱猲猳猴猵猶猷猸猹猺猻猼猽猾猿獀獁獂獃獄獅獆獇獈獉獊獋獌獍獎獏獐獑獒獓獔獕獖獗獘獙獚獛獜獝獞獟獠獡獢獣獤獥獦獧獨獩獪獫獬獭獮獯獰獱獲獳獴獵獶獷獸獹獺獻獼獽獾獿玀玁玂玃玄玅玆率玈玉玊王玌玍玎玏玐玑玒玓玔玕玖玗玘玙玚玛玜玝玞玟玠玡玢玣玤玥玦玧玨玩玪玫玬玭玮环现玱玲玳玴玵玶玷玸玹玺玻玼玽玾玿珀珁珂珃珄珅珆珇珈珉珊珋珌珍珎珏珐珑珒珓珔珕珖珗珘珙珚珛珜珝珞珟珠珡珢珣珤珥珦珧珨珩珪珫珬班珮珯珰珱珲珳珴珵珶珷珸珹珺珻珼珽現珿琀琁琂球琄琅理琇琈琉琊琋琌琍琎琏琐琑琒琓琔琕琖琗琘琙琚琛琜琝琞琟琠琡琢琣琤琥琦琧琨琩琪琫琬琭琮琯琰琱琲琳琴琵琶琷琸琹琺琻琼琽琾琿瑀瑁瑂瑃瑄瑅瑆瑇瑈瑉瑊瑋瑌瑍瑎瑏瑐瑑瑒瑓瑔瑕瑖瑗瑘瑙瑚瑛瑜瑝瑞瑟瑠瑡瑢瑣瑤瑥瑦瑧瑨瑩瑪瑫瑬瑭瑮瑯瑰瑱瑲瑳瑴瑵瑶瑷瑸瑹瑺瑻瑼瑽瑾瑿璀璁璂璃璄璅璆璇璈璉璊璋璌璍璎璏璐璑璒璓璔璕璖璗璘璙璚璛璜璝璞璟璠璡璢璣璤璥璦璧璨璩璪璫璬璭璮璯環璱璲璳璴璵璶璷璸璹璺璻璼璽璾璿瓀瓁瓂瓃瓄瓅瓆瓇瓈瓉瓊瓋瓌瓍瓎瓏瓐瓑瓒瓓瓔瓕瓖瓗瓘瓙瓚瓛瓜瓝瓞瓟瓠瓡瓢瓣瓤瓥瓦瓧瓨瓩瓪瓫瓬瓭瓮瓯瓰瓱瓲瓳瓴瓵瓶瓷瓸瓹瓺瓻瓼瓽瓾瓿甀甁甂甃甄甅甆甇甈甉甊甋甌甍甎甏甐甑甒甓甔甕甖甗甘甙甚甛甜甝甞生甠甡產産甤甥甦甧用甩甪甫甬甭甮甯田由甲申甴电甶男甸甹町画甼甽甾甿畀畁畂畃畄畅畆畇畈畉畊畋界畍畎畏畐畑畒畓畔畕畖畗畘留畚畛畜畝畞畟畠畡畢畣畤略畦畧畨畩番畫畬畭畮畯異畱畲畳畴畵當畷畸畹畺畻畼畽畾畿疀疁疂疃疄疅疆疇疈疉疊疋疌疍疎疏疐疑疒疓疔疕疖疗疘疙疚疛疜疝疞疟疠疡疢疣疤疥疦疧疨疩疪疫疬疭疮疯疰疱疲疳疴疵疶疷疸疹疺疻疼疽疾疿痀痁痂痃痄病痆症痈痉痊痋痌痍痎痏痐痑痒痓痔痕痖痗痘痙痚痛痜痝痞痟痠痡痢痣痤痥痦痧痨痩痪痫痬痭痮痯痰痱痲痳痴痵痶痷痸痹痺痻痼痽痾痿瘀瘁瘂瘃瘄瘅瘆瘇瘈瘉瘊瘋瘌瘍瘎瘏瘐瘑瘒瘓瘔瘕瘖瘗瘘瘙瘚瘛瘜瘝瘞瘟瘠瘡瘢瘣瘤瘥瘦瘧瘨瘩瘪瘫瘬瘭瘮瘯瘰瘱瘲瘳瘴瘵瘶瘷瘸瘹瘺瘻瘼瘽瘾瘿癀癁療癃癄癅癆癇癈癉癊癋癌癍癎癏癐癑癒癓癔癕癖癗癘癙癚癛癜癝癞癟癠癡癢癣癤癥癦癧癨癩癪癫癬癭癮癯癰癱癲癳癴癵癶癷癸癹発登發白百癿皀皁皂皃的皅皆皇皈皉皊皋皌皍皎皏皐皑皒皓皔皕皖皗皘皙皚皛皜皝皞皟皠皡皢皣皤皥皦皧皨皩皪皫皬皭皮皯皰皱皲皳皴皵皶皷皸皹皺皻皼皽皾皿盀盁盂盃盄盅盆盇盈盉益盋盌盍盎盏盐监盒盓盔盕盖盗盘盙盚盛盜盝盞盟盠盡盢監盤盥盦盧盨盩盪盫盬盭目盯盰盱盲盳直盵盶盷相盹盺盻盼盽盾盿眀省眂眃眄眅眆眇眈眉眊看県眍眎眏眐眑眒眓眔眕眖眗眘眙眚眛眜眝眞真眠眡眢眣眤眥眦眧眨眩眪眫眬眭眮眯眰眱眲眳眴眵眶眷眸眹眺眻眼眽眾眿着睁睂睃睄睅睆睇睈睉睊睋睌睍睎睏睐睑睒睓睔睕睖睗睘睙睚睛睜睝睞睟睠睡睢督睤睥睦睧睨睩睪睫睬睭睮睯睰睱睲睳睴睵睶睷睸睹睺睻睼睽睾睿瞀瞁瞂瞃瞄瞅瞆瞇瞈瞉瞊瞋瞌瞍瞎瞏瞐瞑瞒瞓瞔瞕瞖瞗瞘瞙瞚瞛瞜瞝瞞瞟瞠瞡瞢瞣瞤瞥瞦瞧瞨瞩瞪瞫瞬瞭瞮瞯瞰瞱瞲瞳瞴瞵瞶瞷瞸瞹瞺瞻瞼瞽瞾瞿矀矁矂矃矄矅矆矇矈矉矊矋矌矍矎矏矐矑矒矓矔矕矖矗矘矙矚矛矜矝矞矟矠矡矢矣矤知矦矧矨矩矪矫矬短矮矯矰矱矲石矴矵矶矷矸矹矺矻矼矽矾矿砀码砂砃砄砅砆砇砈砉砊砋砌砍砎砏砐砑砒砓研砕砖砗砘砙砚砛砜砝砞砟砠砡砢砣砤砥砦砧砨砩砪砫砬砭砮砯砰砱砲砳破砵砶砷砸砹砺砻砼砽砾砿础硁硂硃硄硅硆硇硈硉硊硋硌硍硎硏硐硑硒硓硔硕硖硗硘硙硚硛硜硝硞硟硠硡硢硣硤硥硦硧硨硩硪硫硬硭确硯硰硱硲硳硴硵硶硷硸硹硺硻硼硽硾硿碀碁碂碃碄碅碆碇碈碉碊碋碌碍碎碏碐碑碒碓碔碕碖碗碘碙碚碛碜碝碞碟碠碡碢碣碤碥碦碧碨碩碪碫碬碭碮碯碰碱碲碳碴碵碶碷碸碹確碻碼碽碾碿磀磁磂磃磄磅磆磇磈磉磊磋磌磍磎磏磐磑磒磓磔磕磖磗磘磙磚磛磜磝磞磟磠磡磢磣磤磥磦磧磨磩磪磫磬磭磮磯磰磱磲磳磴磵磶磷磸磹磺磻磼磽磾磿礀礁礂礃礄礅礆礇礈礉礊礋礌礍礎礏礐礑礒礓礔礕礖礗礘礙礚礛礜礝礞礟礠礡礢礣礤礥礦礧礨礩礪礫礬礭礮礯礰礱礲礳礴礵礶礷礸礹示礻礼礽社礿祀祁祂祃祄祅祆祇祈祉祊祋祌祍祎祏祐祑祒祓祔祕祖祗祘祙祚祛祜祝神祟祠祡祢祣祤祥祦祧票祩祪祫祬祭祮祯祰祱祲祳祴祵祶祷祸祹祺祻祼祽祾祿禀禁禂禃禄禅禆禇禈禉禊禋禌禍禎福禐禑禒禓禔禕禖禗禘禙禚禛禜禝禞禟禠禡禢禣禤禥禦禧禨禩禪禫禬禭禮禯禰禱禲禳禴禵禶禷禸禹禺离禼禽禾禿秀私秂秃秄秅秆秇秈秉秊秋秌种秎秏秐科秒秓秔秕秖秗秘秙秚秛秜秝秞租秠秡秢秣秤秥秦秧秨秩秪秫秬秭秮积称秱秲秳秴秵秶秷秸秹秺移秼秽秾秿稀稁稂稃稄稅稆稇稈稉稊程稌稍税稏稐稑稒稓稔稕稖稗稘稙稚稛稜稝稞稟稠稡稢稣稤稥稦稧稨稩稪稫稬稭種稯稰稱稲稳稴稵稶稷稸稹稺稻稼稽稾稿穀穁穂穃穄穅穆穇穈穉穊穋穌積穎穏穐穑穒穓穔穕穖穗穘穙穚穛穜穝穞穟穠穡穢穣穤穥穦穧穨穩穪穫穬穭穮穯穰穱穲穳穴穵究穷穸穹空穻穼穽穾穿窀突窂窃窄窅窆窇窈窉窊窋窌窍窎窏窐窑窒窓窔窕窖窗窘窙窚窛窜窝窞窟窠窡窢窣窤窥窦窧窨窩窪窫窬窭窮窯窰窱窲窳窴窵窶窷窸窹窺窻窼窽窾窿竀竁竂竃竄竅竆竇竈竉竊立竌竍竎竏竐竑竒竓竔竕竖竗竘站竚竛竜竝竞竟章竡竢竣竤童竦竧竨竩竪竫竬竭竮端竰竱竲竳竴竵競竷竸竹竺竻竼竽竾竿笀笁笂笃笄笅笆笇笈笉笊笋笌笍笎笏笐笑笒笓笔笕笖笗笘笙笚笛笜笝笞笟笠笡笢笣笤笥符笧笨笩笪笫第笭笮笯笰笱笲笳笴笵笶笷笸笹笺笻笼笽笾笿筀筁筂筃筄筅筆筇筈等筊筋筌筍筎筏筐筑筒筓答筕策筗筘筙筚筛筜筝筞筟筠筡筢筣筤筥筦筧筨筩筪筫筬筭筮筯筰筱筲筳筴筵筶筷筸筹筺筻筼筽签筿简箁箂箃箄箅箆箇箈箉箊箋箌箍箎箏箐箑箒箓箔箕箖算箘箙箚箛箜箝箞箟箠管箢箣箤箥箦箧箨箩箪箫箬箭箮箯箰箱箲箳箴箵箶箷箸箹箺箻箼箽箾箿節篁篂篃範篅篆篇篈築篊篋篌篍篎篏篐篑篒篓篔篕篖篗篘篙篚篛篜篝篞篟篠篡篢篣篤篥篦篧篨篩篪篫篬篭篮篯篰篱篲篳篴篵篶篷篸篹篺篻篼篽篾篿簀簁簂簃簄簅簆簇簈簉簊簋簌簍簎簏簐簑簒簓簔簕簖簗簘簙簚簛簜簝簞簟簠簡簢簣簤簥簦簧簨簩簪簫簬簭簮簯簰簱簲簳簴簵簶簷簸簹簺簻簼簽簾簿籀籁籂籃籄籅籆籇籈籉籊籋籌籍籎籏籐籑籒籓籔籕籖籗籘籙籚籛籜籝籞籟籠籡籢籣籤籥籦籧籨籩籪籫籬籭籮籯籰籱籲米籴籵籶籷籸籹籺类籼籽籾籿粀粁粂粃粄粅粆粇粈粉粊粋粌粍粎粏粐粑粒粓粔粕粖粗粘粙粚粛粜粝粞粟粠粡粢粣粤粥粦粧粨粩粪粫粬粭粮粯粰粱粲粳粴粵粶粷粸粹粺粻粼粽精粿糀糁糂糃糄糅糆糇糈糉糊糋糌糍糎糏糐糑糒糓糔糕糖糗糘糙糚糛糜糝糞糟糠糡糢糣糤糥糦糧糨糩糪糫糬糭糮糯糰糱糲糳糴糵糶糷糸糹糺系糼糽糾糿紀紁紂紃約紅紆紇紈紉紊紋紌納紎紏紐紑紒紓純紕紖紗紘紙級紛紜紝紞紟素紡索紣紤紥紦紧紨紩紪紫紬紭紮累細紱紲紳紴紵紶紷紸紹紺紻紼紽紾紿絀絁終絃組絅絆絇絈絉絊絋経絍絎絏結絑絒絓絔絕絖絗絘絙絚絛絜絝絞絟絠絡絢絣絤絥給絧絨絩絪絫絬絭絮絯絰統絲絳絴絵絶絷絸絹絺絻絼絽絾絿綀綁綂綃綄綅綆綇綈綉綊綋綌綍綎綏綐綑綒經綔綕綖綗綘継続綛綜綝綞綟綠綡綢綣綤綥綦綧綨綩綪綫綬維綮綯綰綱網綳綴綵綶綷綸綹綺綻綼綽綾綿緀緁緂緃緄緅緆緇緈緉緊緋緌緍緎総緐緑緒緓緔緕緖緗緘緙線緛緜緝緞緟締緡緢緣緤緥緦緧編緩緪緫緬緭緮緯緰緱緲緳練緵緶緷緸緹緺緻緼緽緾緿縀縁縂縃縄縅縆縇縈縉縊縋縌縍縎縏縐縑縒縓縔縕縖縗縘縙縚縛縜縝縞縟縠縡縢縣縤縥縦縧縨縩縪縫縬縭縮縯縰縱縲縳縴縵縶縷縸縹縺縻縼總績縿繀繁繂繃繄繅繆繇繈繉繊繋繌繍繎繏繐繑繒繓織繕繖繗繘繙繚繛繜繝繞繟繠繡繢繣繤繥繦繧繨繩繪繫繬繭繮繯繰繱繲繳繴繵繶繷繸繹繺繻繼繽繾繿纀纁纂纃纄纅纆纇纈纉纊纋續纍纎纏纐纑纒纓纔纕纖纗纘纙纚纛纜纝纞纟纠纡红纣纤纥约级纨纩纪纫纬纭纮纯纰纱纲纳纴纵纶纷纸纹纺纻纼纽纾线绀绁绂练组绅细织终绉绊绋绌绍绎经绐绑绒结绔绕绖绗绘给绚绛络绝绞统绠绡绢绣绤绥绦继绨绩绪绫绬续绮绯绰绱绲绳维绵绶绷绸绹绺绻综绽绾绿缀缁缂缃缄缅缆缇缈缉缊缋缌缍缎缏缐缑缒缓缔缕编缗缘缙缚缛缜缝缞缟缠缡缢缣缤缥缦缧缨缩缪缫缬缭缮缯缰缱缲缳缴缵缶缷缸缹缺缻缼缽缾缿罀罁罂罃罄罅罆罇罈罉罊罋罌罍罎罏罐网罒罓罔罕罖罗罘罙罚罛罜罝罞罟罠罡罢罣罤罥罦罧罨罩罪罫罬罭置罯罰罱署罳罴罵罶罷罸罹罺罻罼罽罾罿羀羁羂羃羄羅羆羇羈羉羊羋羌羍美羏羐羑羒羓羔羕羖羗羘羙羚羛羜羝羞羟羠羡羢羣群羥羦羧羨義羪羫羬羭羮羯羰羱羲羳羴羵羶羷羸羹羺羻羼羽羾羿翀翁翂翃翄翅翆翇翈翉翊翋翌翍翎翏翐翑習翓翔翕翖翗翘翙翚翛翜翝翞翟翠翡翢翣翤翥翦翧翨翩翪翫翬翭翮翯翰翱翲翳翴翵翶翷翸翹翺翻翼翽翾翿耀老耂考耄者耆耇耈耉耊耋而耍耎耏耐耑耒耓耔耕耖耗耘耙耚耛耜耝耞耟耠耡耢耣耤耥耦耧耨耩耪耫耬耭耮耯耰耱耲耳耴耵耶耷耸耹耺耻耼耽耾耿聀聁聂聃聄聅聆聇聈聉聊聋职聍聎聏聐聑聒聓联聕聖聗聘聙聚聛聜聝聞聟聠聡聢聣聤聥聦聧聨聩聪聫聬聭聮聯聰聱聲聳聴聵聶職聸聹聺聻聼聽聾聿肀肁肂肃肄肅肆肇肈肉肊肋肌肍肎肏肐肑肒肓肔肕肖肗肘肙肚肛肜肝肞肟肠股肢肣肤肥肦肧肨肩肪肫肬肭肮肯肰肱育肳肴肵肶肷肸肹肺肻肼肽肾肿胀胁胂胃胄胅胆胇胈胉胊胋背胍胎胏胐胑胒胓胔胕胖胗胘胙胚胛胜胝胞胟胠胡胢胣胤胥胦胧胨胩胪胫胬胭胮胯胰胱胲胳胴胵胶胷胸胹胺胻胼能胾胿脀脁脂脃脄脅脆脇脈脉脊脋脌脍脎脏脐脑脒脓脔脕脖脗脘脙脚脛脜脝脞脟脠脡脢脣脤脥脦脧脨脩脪脫脬脭脮脯脰脱脲脳脴脵脶脷脸脹脺脻脼脽脾脿腀腁腂腃腄腅腆腇腈腉腊腋腌腍腎腏腐腑腒腓腔腕腖腗腘腙腚腛腜腝腞腟腠腡腢腣腤腥腦腧腨腩腪腫腬腭腮腯腰腱腲腳腴腵腶腷腸腹腺腻腼腽腾腿膀膁膂膃膄膅膆膇膈膉膊膋膌膍膎膏膐膑膒膓膔膕膖膗膘膙膚膛膜膝膞膟膠膡膢膣膤膥膦膧膨膩膪膫膬膭膮膯膰膱膲膳膴膵膶膷膸膹膺膻膼膽膾膿臀臁臂臃臄臅臆臇臈臉臊臋臌臍臎臏臐臑臒臓臔臕臖臗臘臙臚臛臜臝臞臟臠臡臢臣臤臥臦臧臨臩自臫臬臭臮臯臰臱臲至致臵臶臷臸臹臺臻臼臽臾臿舀舁舂舃舄舅舆與興舉舊舋舌舍舎舏舐舑舒舓舔舕舖舗舘舙舚舛舜舝舞舟舠舡舢舣舤舥舦舧舨舩航舫般舭舮舯舰舱舲舳舴舵舶舷舸船舺舻舼舽舾舿艀艁艂艃艄艅艆艇艈艉艊艋艌艍艎艏艐艑艒艓艔艕艖艗艘艙艚艛艜艝艞艟艠艡艢艣艤艥艦艧艨艩艪艫艬艭艮良艰艱色艳艴艵艶艷艸艹艺艻艼艽艾艿芀芁节芃芄芅芆芇芈芉芊芋芌芍芎芏芐芑芒芓芔芕芖芗芘芙芚芛芜芝芞芟芠芡芢芣芤芥芦芧芨芩芪芫芬芭芮芯芰花芲芳芴芵芶芷芸芹芺芻芼芽芾芿苀苁苂苃苄苅苆苇苈苉苊苋苌苍苎苏苐苑苒苓苔苕苖苗苘苙苚苛苜苝苞苟苠苡苢苣苤若苦苧苨苩苪苫苬苭苮苯苰英苲苳苴苵苶苷苸苹苺苻苼苽苾苿茀茁茂范茄茅茆茇茈茉茊茋茌茍茎茏茐茑茒茓茔茕茖茗茘茙茚茛茜茝茞茟茠茡茢茣茤茥茦茧茨茩茪茫茬茭茮茯茰茱茲茳茴茵茶茷茸茹茺茻茼茽茾茿荀荁荂荃荄荅荆荇荈草荊荋荌荍荎荏荐荑荒荓荔荕荖荗荘荙荚荛荜荝荞荟荠荡荢荣荤荥荦荧荨荩荪荫荬荭荮药荰荱荲荳荴荵荶荷荸荹荺荻荼荽荾荿莀莁莂莃莄莅莆莇莈莉莊莋莌莍莎莏莐莑莒莓莔莕莖莗莘莙莚莛莜莝莞莟莠莡莢莣莤莥莦莧莨莩莪莫莬莭莮莯莰莱莲莳莴莵莶获莸莹莺莻莼莽莾莿菀菁菂菃菄菅菆菇菈菉菊菋菌菍菎菏菐菑菒菓菔菕菖菗菘菙菚菛菜菝菞菟菠菡菢菣菤菥菦菧菨菩菪菫菬菭菮華菰菱菲菳菴菵菶菷菸菹菺菻菼菽菾菿萀萁萂萃萄萅萆萇萈萉萊萋萌萍萎萏萐萑萒萓萔萕萖萗萘萙萚萛萜萝萞萟萠萡萢萣萤营萦萧萨萩萪萫萬萭萮萯萰萱萲萳萴萵萶萷萸萹萺萻萼落萾萿葀葁葂葃葄葅葆葇葈葉葊葋葌葍葎葏葐葑葒葓葔葕葖著葘葙葚葛葜葝葞葟葠葡葢董葤葥葦葧葨葩葪葫葬葭葮葯葰葱葲葳葴葵葶葷葸葹葺葻葼葽葾葿蒀蒁蒂蒃蒄蒅蒆蒇蒈蒉蒊蒋蒌蒍蒎蒏蒐蒑蒒蒓蒔蒕蒖蒗蒘蒙蒚蒛蒜蒝蒞蒟蒠蒡蒢蒣蒤蒥蒦蒧蒨蒩蒪蒫蒬蒭蒮蒯蒰蒱蒲蒳蒴蒵蒶蒷蒸蒹蒺蒻蒼蒽蒾蒿蓀蓁蓂蓃蓄蓅蓆蓇蓈蓉蓊蓋蓌蓍蓎蓏蓐蓑蓒蓓蓔蓕蓖蓗蓘蓙蓚蓛蓜蓝蓞蓟蓠蓡蓢蓣蓤蓥蓦蓧蓨蓩蓪蓫蓬蓭蓮蓯蓰蓱蓲蓳蓴蓵蓶蓷蓸蓹蓺蓻蓼蓽蓾蓿蔀蔁蔂蔃蔄蔅蔆蔇蔈蔉蔊蔋蔌蔍蔎蔏蔐蔑蔒蔓蔔蔕蔖蔗蔘蔙蔚蔛蔜蔝蔞蔟蔠蔡蔢蔣蔤蔥蔦蔧蔨蔩蔪蔫蔬蔭蔮蔯蔰蔱蔲蔳蔴蔵蔶蔷蔸蔹蔺蔻蔼蔽蔾蔿蕀蕁蕂蕃蕄蕅蕆蕇蕈蕉蕊蕋蕌蕍蕎蕏蕐蕑蕒蕓蕔蕕蕖蕗蕘蕙蕚蕛蕜蕝蕞蕟蕠蕡蕢蕣蕤蕥蕦蕧蕨蕩蕪蕫蕬蕭蕮蕯蕰蕱蕲蕳蕴蕵蕶蕷蕸蕹蕺蕻蕼蕽蕾蕿薀薁薂薃薄薅薆薇薈薉薊薋薌薍薎薏薐薑薒薓薔薕薖薗薘薙薚薛薜薝薞薟薠薡薢薣薤薥薦薧薨薩薪薫薬薭薮薯薰薱薲薳薴薵薶薷薸薹薺薻薼薽薾薿藀藁藂藃藄藅藆藇藈藉藊藋藌藍藎藏藐藑藒藓藔藕藖藗藘藙藚藛藜藝藞藟藠藡藢藣藤藥藦藧藨藩藪藫藬藭藮藯藰藱藲藳藴藵藶藷藸藹藺藻藼藽藾藿蘀蘁蘂蘃蘄蘅蘆蘇蘈蘉蘊蘋蘌蘍蘎蘏蘐蘑蘒蘓蘔蘕蘖蘗蘘蘙蘚蘛蘜蘝蘞蘟蘠蘡蘢蘣蘤蘥蘦蘧蘨蘩蘪蘫蘬蘭蘮蘯蘰蘱蘲蘳蘴蘵蘶蘷蘸蘹蘺蘻蘼蘽蘾蘿虀虁虂虃虄虅虆虇虈虉虊虋虌虍虎虏虐虑虒虓虔處虖虗虘虙虚虛虜虝虞號虠虡虢虣虤虥虦虧虨虩虪虫虬虭虮虯虰虱虲虳虴虵虶虷虸虹虺虻虼虽虾虿蚀蚁蚂蚃蚄蚅蚆蚇蚈蚉蚊蚋蚌蚍蚎蚏蚐蚑蚒蚓蚔蚕蚖蚗蚘蚙蚚蚛蚜蚝蚞蚟蚠蚡蚢蚣蚤蚥蚦蚧蚨蚩蚪蚫蚬蚭蚮蚯蚰蚱蚲蚳蚴蚵蚶蚷蚸蚹蚺蚻蚼蚽蚾蚿蛀蛁蛂蛃蛄蛅蛆蛇蛈蛉蛊蛋蛌蛍蛎蛏蛐蛑蛒蛓蛔蛕蛖蛗蛘蛙蛚蛛蛜蛝蛞蛟蛠蛡蛢蛣蛤蛥蛦蛧蛨蛩蛪蛫蛬蛭蛮蛯蛰蛱蛲蛳蛴蛵蛶蛷蛸蛹蛺蛻蛼蛽蛾蛿蜀蜁蜂蜃蜄蜅蜆蜇蜈蜉蜊蜋蜌蜍蜎蜏蜐蜑蜒蜓蜔蜕蜖蜗蜘蜙蜚蜛蜜蜝蜞蜟蜠蜡蜢蜣蜤蜥蜦蜧蜨蜩蜪蜫蜬蜭蜮蜯蜰蜱蜲蜳蜴蜵蜶蜷蜸蜹蜺蜻蜼蜽蜾蜿蝀蝁蝂蝃蝄蝅蝆蝇蝈蝉蝊蝋蝌蝍蝎蝏蝐蝑蝒蝓蝔蝕蝖蝗蝘蝙蝚蝛蝜蝝蝞蝟蝠蝡蝢蝣蝤蝥蝦蝧蝨蝩蝪蝫蝬蝭蝮蝯蝰蝱蝲蝳蝴蝵蝶蝷蝸蝹蝺蝻蝼蝽蝾蝿螀螁螂螃螄螅螆螇螈螉螊螋螌融螎螏螐螑螒螓螔螕螖螗螘螙螚螛螜螝螞螟螠螡螢螣螤螥螦螧螨螩螪螫螬螭螮螯螰螱螲螳螴螵螶螷螸螹螺螻螼螽螾螿蟀蟁蟂蟃蟄蟅蟆蟇蟈蟉蟊蟋蟌蟍蟎蟏蟐蟑蟒蟓蟔蟕蟖蟗蟘蟙蟚蟛蟜蟝蟞蟟蟠蟡蟢蟣蟤蟥蟦蟧蟨蟩蟪蟫蟬蟭蟮蟯蟰蟱蟲蟳蟴蟵蟶蟷蟸蟹蟺蟻蟼蟽蟾蟿蠀蠁蠂蠃蠄蠅蠆蠇蠈蠉蠊蠋蠌蠍蠎蠏蠐蠑蠒蠓蠔蠕蠖蠗蠘蠙蠚蠛蠜蠝蠞蠟蠠蠡蠢蠣蠤蠥蠦蠧蠨蠩蠪蠫蠬蠭蠮蠯蠰蠱蠲蠳蠴蠵蠶蠷蠸蠹蠺蠻蠼蠽蠾蠿血衁衂衃衄衅衆衇衈衉衊衋行衍衎衏衐衑衒術衔衕衖街衘衙衚衛衜衝衞衟衠衡衢衣衤补衦衧表衩衪衫衬衭衮衯衰衱衲衳衴衵衶衷衸衹衺衻衼衽衾衿袀袁袂袃袄袅袆袇袈袉袊袋袌袍袎袏袐袑袒袓袔袕袖袗袘袙袚袛袜袝袞袟袠袡袢袣袤袥袦袧袨袩袪被袬袭袮袯袰袱袲袳袴袵袶袷袸袹袺袻袼袽袾袿裀裁裂裃裄装裆裇裈裉裊裋裌裍裎裏裐裑裒裓裔裕裖裗裘裙裚裛補裝裞裟裠裡裢裣裤裥裦裧裨裩裪裫裬裭裮裯裰裱裲裳裴裵裶裷裸裹裺裻裼製裾裿褀褁褂褃褄褅褆複褈褉褊褋褌褍褎褏褐褑褒褓褔褕褖褗褘褙褚褛褜褝褞褟褠褡褢褣褤褥褦褧褨褩褪褫褬褭褮褯褰褱褲褳褴褵褶褷褸褹褺褻褼褽褾褿襀襁襂襃襄襅襆襇襈襉襊襋襌襍襎襏襐襑襒襓襔襕襖襗襘襙襚襛襜襝襞襟襠襡襢襣襤襥襦襧襨襩襪襫襬襭襮襯襰襱襲襳襴襵襶襷襸襹襺襻襼襽襾西覀要覂覃覄覅覆覇覈覉覊見覌覍覎規覐覑覒覓覔覕視覗覘覙覚覛覜覝覞覟覠覡覢覣覤覥覦覧覨覩親覫覬覭覮覯覰覱覲観覴覵覶覷覸覹覺覻覼覽覾覿觀见观觃规觅视觇览觉觊觋觌觍觎觏觐觑角觓觔觕觖觗觘觙觚觛觜觝觞觟觠觡觢解觤觥触觧觨觩觪觫觬觭觮觯觰觱觲觳觴觵觶觷觸觹觺觻觼觽觾觿言訁訂訃訄訅訆訇計訉訊訋訌訍討訏訐訑訒訓訔訕訖託記訙訚訛訜訝訞訟訠訡訢訣訤訥訦訧訨訩訪訫訬設訮訯訰許訲訳訴訵訶訷訸訹診註証訽訾訿詀詁詂詃詄詅詆詇詈詉詊詋詌詍詎詏詐詑詒詓詔評詖詗詘詙詚詛詜詝詞詟詠詡詢詣詤詥試詧詨詩詪詫詬詭詮詯詰話該詳詴詵詶詷詸詹詺詻詼詽詾詿誀誁誂誃誄誅誆誇誈誉誊誋誌認誎誏誐誑誒誓誔誕誖誗誘誙誚誛誜誝語誟誠誡誢誣誤誥誦誧誨誩說誫説読誮誯誰誱課誳誴誵誶誷誸誹誺誻誼誽誾調諀諁諂諃諄諅諆談諈諉諊請諌諍諎諏諐諑諒諓諔諕論諗諘諙諚諛諜諝諞諟諠諡諢諣諤諥諦諧諨諩諪諫諬諭諮諯諰諱諲諳諴諵諶諷諸諹諺諻諼諽諾諿謀謁謂謃謄謅謆謇謈謉謊謋謌謍謎謏謐謑謒謓謔謕謖謗謘謙謚講謜謝謞謟謠謡謢謣謤謥謦謧謨謩謪謫謬謭謮謯謰謱謲謳謴謵謶謷謸謹謺謻謼謽謾謿譀譁譂譃譄譅譆譇譈證譊譋譌譍譎譏譐譑譒譓譔譕譖譗識譙譚譛譜譝譞譟譠譡譢譣譤譥警譧譨譩譪譫譬譭譮譯議譱譲譳譴譵譶護譸譹譺譻譼譽譾譿讀讁讂讃讄讅讆讇讈讉變讋讌讍讎讏讐讑讒讓讔讕讖讗讘讙讚讛讜讝讞讟讠计订讣认讥讦讧讨让讪讫讬训议讯记讱讲讳讴讵讶讷许讹论讻讼讽设访诀证诂诃评诅识诇诈诉诊诋诌词诎诏诐译诒诓诔试诖诗诘诙诚诛诜话诞诟诠诡询诣诤该详诧诨诩诪诫诬语诮误诰诱诲诳说诵诶请诸诹诺读诼诽课诿谀谁谂调谄谅谆谇谈谉谊谋谌谍谎谏谐谑谒谓谔谕谖谗谘谙谚谛谜谝谞谟谠谡谢谣谤谥谦谧谨谩谪谫谬谭谮谯谰谱谲谳谴谵谶谷谸谹谺谻谼谽谾谿豀豁豂豃豄豅豆豇豈豉豊豋豌豍豎豏豐豑豒豓豔豕豖豗豘豙豚豛豜豝豞豟豠象豢豣豤豥豦豧豨豩豪豫豬豭豮豯豰豱豲豳豴豵豶豷豸豹豺豻豼豽豾豿貀貁貂貃貄貅貆貇貈貉貊貋貌貍貎貏貐貑貒貓貔貕貖貗貘貙貚貛貜貝貞貟負財貢貣貤貥貦貧貨販貪貫責貭貮貯貰貱貲貳貴貵貶買貸貹貺費貼貽貾貿賀賁賂賃賄賅賆資賈賉賊賋賌賍賎賏賐賑賒賓賔賕賖賗賘賙賚賛賜賝賞賟賠賡賢賣賤賥賦賧賨賩質賫賬賭賮賯賰賱賲賳賴賵賶賷賸賹賺賻購賽賾賿贀贁贂贃贄贅贆贇贈贉贊贋贌贍贎贏贐贑贒贓贔贕贖贗贘贙贚贛贜贝贞负贠贡财责贤败账货质贩贪贫贬购贮贯贰贱贲贳贴贵贶贷贸费贺贻贼贽贾贿赀赁赂赃资赅赆赇赈赉赊赋赌赍赎赏赐赑赒赓赔赕赖赗赘赙赚赛赜赝赞赟赠赡赢赣赤赥赦赧赨赩赪赫赬赭赮赯走赱赲赳赴赵赶起赸赹赺赻赼赽赾赿趀趁趂趃趄超趆趇趈趉越趋趌趍趎趏趐趑趒趓趔趕趖趗趘趙趚趛趜趝趞趟趠趡趢趣趤趥趦趧趨趩趪趫趬趭趮趯趰趱趲足趴趵趶趷趸趹趺趻趼趽趾趿跀跁跂跃跄跅跆跇跈跉跊跋跌跍跎跏跐跑跒跓跔跕跖跗跘跙跚跛跜距跞跟跠跡跢跣跤跥跦跧跨跩跪跫跬跭跮路跰跱跲跳跴践跶跷跸跹跺跻跼跽跾跿踀踁踂踃踄踅踆踇踈踉踊踋踌踍踎踏踐踑踒踓踔踕踖踗踘踙踚踛踜踝踞踟踠踡踢踣踤踥踦踧踨踩踪踫踬踭踮踯踰踱踲踳踴踵踶踷踸踹踺踻踼踽踾踿蹀蹁蹂蹃蹄蹅蹆蹇蹈蹉蹊蹋蹌蹍蹎蹏蹐蹑蹒蹓蹔蹕蹖蹗蹘蹙蹚蹛蹜蹝蹞蹟蹠蹡蹢蹣蹤蹥蹦蹧蹨蹩蹪蹫蹬蹭蹮蹯蹰蹱蹲蹳蹴蹵蹶蹷蹸蹹蹺蹻蹼蹽蹾蹿躀躁躂躃躄躅躆躇躈躉躊躋躌躍躎躏躐躑躒躓躔躕躖躗躘躙躚躛躜躝躞躟躠躡躢躣躤躥躦躧躨躩躪身躬躭躮躯躰躱躲躳躴躵躶躷躸躹躺躻躼躽躾躿軀軁軂軃軄軅軆軇軈軉車軋軌軍軎軏軐軑軒軓軔軕軖軗軘軙軚軛軜軝軞軟軠軡転軣軤軥軦軧軨軩軪軫軬軭軮軯軰軱軲軳軴軵軶軷軸軹軺軻軼軽軾軿輀輁輂較輄輅輆輇輈載輊輋輌輍輎輏輐輑輒輓輔輕輖輗輘輙輚輛輜輝輞輟輠輡輢輣輤輥輦輧輨輩輪輫輬輭輮輯輰輱輲輳輴輵輶輷輸輹輺輻輼輽輾輿轀轁轂轃轄轅轆轇轈轉轊轋轌轍轎轏轐轑轒轓轔轕轖轗轘轙轚轛轜轝轞轟轠轡轢轣轤轥车轧轨轩轪轫转轭轮软轰轱轲轳轴轵轶轷轸轹轺轻轼载轾轿辀辁辂较辄辅辆辇辈辉辊辋辌辍辎辏辐辑辒输辔辕辖辗辘辙辚辛辜辝辞辟辠辡辢辣辤辥辦辧辨辩辪辫辬辭辮辯辰辱農辳辴辵辶辷辸边辺辻込辽达辿迀迁迂迃迄迅迆过迈迉迊迋迌迍迎迏运近迒迓返迕迖迗还这迚进远违连迟迠迡迢迣迤迥迦迧迨迩迪迫迬迭迮迯述迱迲迳迴迵迶迷迸迹迺迻迼追迾迿退送适逃逄逅逆逇逈选逊逋逌逍逎透逐逑递逓途逕逖逗逘這通逛逜逝逞速造逡逢連逤逥逦逧逨逩逪逫逬逭逮逯逰週進逳逴逵逶逷逸逹逺逻逼逽逾逿遀遁遂遃遄遅遆遇遈遉遊運遌遍過遏遐遑遒道達違遖遗遘遙遚遛遜遝遞遟遠遡遢遣遤遥遦遧遨適遪遫遬遭遮遯遰遱遲遳遴遵遶遷選遹遺遻遼遽遾避邀邁邂邃還邅邆邇邈邉邊邋邌邍邎邏邐邑邒邓邔邕邖邗邘邙邚邛邜邝邞邟邠邡邢那邤邥邦邧邨邩邪邫邬邭邮邯邰邱邲邳邴邵邶邷邸邹邺邻邼邽邾邿郀郁郂郃郄郅郆郇郈郉郊郋郌郍郎郏郐郑郒郓郔郕郖郗郘郙郚郛郜郝郞郟郠郡郢郣郤郥郦郧部郩郪郫郬郭郮郯郰郱郲郳郴郵郶郷郸郹郺郻郼都郾郿鄀鄁鄂鄃鄄鄅鄆鄇鄈鄉鄊鄋鄌鄍鄎鄏鄐鄑鄒鄓鄔鄕鄖鄗鄘鄙鄚鄛鄜鄝鄞鄟鄠鄡鄢鄣鄤鄥鄦鄧鄨鄩鄪鄫鄬鄭鄮鄯鄰鄱鄲鄳鄴鄵鄶鄷鄸鄹鄺鄻鄼鄽鄾鄿酀酁酂酃酄酅酆酇酈酉酊酋酌配酎酏酐酑酒酓酔酕酖酗酘酙酚酛酜酝酞酟酠酡酢酣酤酥酦酧酨酩酪酫酬酭酮酯酰酱酲酳酴酵酶酷酸酹酺酻酼酽酾酿醀醁醂醃醄醅醆醇醈醉醊醋醌醍醎醏醐醑醒醓醔醕醖醗醘醙醚醛醜醝醞醟醠醡醢醣醤醥醦醧醨醩醪醫醬醭醮醯醰醱醲醳醴醵醶醷醸醹醺醻醼醽醾醿釀釁釂釃釄釅釆采釈釉释釋里重野量釐金釒釓釔釕釖釗釘釙釚釛釜針釞釟釠釡釢釣釤釥釦釧釨釩釪釫釬釭釮釯釰釱釲釳釴釵釶釷釸釹釺釻釼釽釾釿鈀鈁鈂鈃鈄鈅鈆鈇鈈鈉鈊鈋鈌鈍鈎鈏鈐鈑鈒鈓鈔鈕鈖鈗鈘鈙鈚鈛鈜鈝鈞鈟鈠鈡鈢鈣鈤鈥鈦鈧鈨鈩鈪鈫鈬鈭鈮鈯鈰鈱鈲鈳鈴鈵鈶鈷鈸鈹鈺鈻鈼鈽鈾鈿鉀鉁鉂鉃鉄鉅鉆鉇鉈鉉鉊鉋鉌鉍鉎鉏鉐鉑鉒鉓鉔鉕鉖鉗鉘鉙鉚鉛鉜鉝鉞鉟鉠鉡鉢鉣鉤鉥鉦鉧鉨鉩鉪鉫鉬鉭鉮鉯鉰鉱鉲鉳鉴鉵鉶鉷鉸鉹鉺鉻鉼鉽鉾鉿銀銁銂銃銄銅銆銇銈銉銊銋銌銍銎銏銐銑銒銓銔銕銖銗銘銙銚銛銜銝銞銟銠銡銢銣銤銥銦銧銨銩銪銫銬銭銮銯銰銱銲銳銴銵銶銷銸銹銺銻銼銽銾銿鋀鋁鋂鋃鋄鋅鋆鋇鋈鋉鋊鋋鋌鋍鋎鋏鋐鋑鋒鋓鋔鋕鋖鋗鋘鋙鋚鋛鋜鋝鋞鋟鋠鋡鋢鋣鋤鋥鋦鋧鋨鋩鋪鋫鋬鋭鋮鋯鋰鋱鋲鋳鋴鋵鋶鋷鋸鋹鋺鋻鋼鋽鋾鋿錀錁錂錃錄錅錆錇錈錉錊錋錌錍錎錏錐錑錒錓錔錕錖錗錘錙錚錛錜錝錞錟錠錡錢錣錤錥錦錧錨錩錪錫錬錭錮錯錰錱録錳錴錵錶錷錸錹錺錻錼錽錾錿鍀鍁鍂鍃鍄鍅鍆鍇鍈鍉鍊鍋鍌鍍鍎鍏鍐鍑鍒鍓鍔鍕鍖鍗鍘鍙鍚鍛鍜鍝鍞鍟鍠鍡鍢鍣鍤鍥鍦鍧鍨鍩鍪鍫鍬鍭鍮鍯鍰鍱鍲鍳鍴鍵鍶鍷鍸鍹鍺鍻鍼鍽鍾鍿鎀鎁鎂鎃鎄鎅鎆鎇鎈鎉鎊鎋鎌鎍鎎鎏鎐鎑鎒鎓鎔鎕鎖鎗鎘鎙鎚鎛鎜鎝鎞鎟鎠鎡鎢鎣鎤鎥鎦鎧鎨鎩鎪鎫鎬鎭鎮鎯鎰鎱鎲鎳鎴鎵鎶鎷鎸鎹鎺鎻鎼鎽鎾鎿鏀鏁鏂鏃鏄鏅鏆鏇鏈鏉鏊鏋鏌鏍鏎鏏鏐鏑鏒鏓鏔鏕鏖鏗鏘鏙鏚鏛鏜鏝鏞鏟鏠鏡鏢鏣鏤鏥鏦鏧鏨鏩鏪鏫鏬鏭鏮鏯鏰鏱鏲鏳鏴鏵鏶鏷鏸鏹鏺鏻鏼鏽鏾鏿鐀鐁鐂鐃鐄鐅鐆鐇鐈鐉鐊鐋鐌鐍鐎鐏鐐鐑鐒鐓鐔鐕鐖鐗鐘鐙鐚鐛鐜鐝鐞鐟鐠鐡鐢鐣鐤鐥鐦鐧鐨鐩鐪鐫鐬鐭鐮鐯鐰鐱鐲鐳鐴鐵鐶鐷鐸鐹鐺鐻鐼鐽鐾鐿鑀鑁鑂鑃鑄鑅鑆鑇鑈鑉鑊鑋鑌鑍鑎鑏鑐鑑鑒鑓鑔鑕鑖鑗鑘鑙鑚鑛鑜鑝鑞鑟鑠鑡鑢鑣鑤鑥鑦鑧鑨鑩鑪鑫鑬鑭鑮鑯鑰鑱鑲鑳鑴鑵鑶鑷鑸鑹鑺鑻鑼鑽鑾鑿钀钁钂钃钄钅钆钇针钉钊钋钌钍钎钏钐钑钒钓钔钕钖钗钘钙钚钛钜钝钞钟钠钡钢钣钤钥钦钧钨钩钪钫钬钭钮钯钰钱钲钳钴钵钶钷钸钹钺钻钼钽钾钿铀铁铂铃铄铅铆铇铈铉铊铋铌铍铎铏铐铑铒铓铔铕铖铗铘铙铚铛铜铝铞铟铠铡铢铣铤铥铦铧铨铩铪铫铬铭铮铯铰铱铲铳铴铵银铷铸铹铺铻铼铽链铿销锁锂锃锄锅锆锇锈锉锊锋锌锍锎锏锐锑锒锓锔锕锖锗锘错锚锛锜锝锞锟锠锡锢锣锤锥锦锧锨锩锪锫锬锭键锯锰锱锲锳锴锵锶锷锸锹锺锻锼锽锾锿镀镁镂镃镄镅镆镇镈镉镊镋镌镍镎镏镐镑镒镓镔镕镖镗镘镙镚镛镜镝镞镟镠镡镢镣镤镥镦镧镨镩镪镫镬镭镮镯镰镱镲镳镴镵镶長镸镹镺镻镼镽镾长門閁閂閃閄閅閆閇閈閉閊開閌閍閎閏閐閑閒間閔閕閖閗閘閙閚閛閜閝閞閟閠閡関閣閤閥閦閧閨閩閪閫閬閭閮閯閰閱閲閳閴閵閶閷閸閹閺閻閼閽閾閿闀闁闂闃闄闅闆闇闈闉闊闋闌闍闎闏闐闑闒闓闔闕闖闗闘闙闚闛關闝闞闟闠闡闢闣闤闥闦闧门闩闪闫闬闭问闯闰闱闲闳间闵闶闷闸闹闺闻闼闽闾闿阀阁阂阃阄阅阆阇阈阉阊阋阌阍阎阏阐阑阒阓阔阕阖阗阘阙阚阛阜阝阞队阠阡阢阣阤阥阦阧阨阩阪阫阬阭阮阯阰阱防阳阴阵阶阷阸阹阺阻阼阽阾阿陀陁陂陃附际陆陇陈陉陊陋陌降陎陏限陑陒陓陔陕陖陗陘陙陚陛陜陝陞陟陠陡院陣除陥陦陧陨险陪陫陬陭陮陯陰陱陲陳陴陵陶陷陸陹険陻陼陽陾陿隀隁隂隃隄隅隆隇隈隉隊隋隌隍階随隐隑隒隓隔隕隖隗隘隙隚際障隝隞隟隠隡隢隣隤隥隦隧隨隩險隫隬隭隮隯隰隱隲隳隴隵隶隷隸隹隺隻隼隽难隿雀雁雂雃雄雅集雇雈雉雊雋雌雍雎雏雐雑雒雓雔雕雖雗雘雙雚雛雜雝雞雟雠雡離難雤雥雦雧雨雩雪雫雬雭雮雯雰雱雲雳雴雵零雷雸雹雺電雼雽雾雿需霁霂霃霄霅霆震霈霉霊霋霌霍霎霏霐霑霒霓霔霕霖霗霘霙霚霛霜霝霞霟霠霡霢霣霤霥霦霧霨霩霪霫霬霭霮霯霰霱露霳霴霵霶霷霸霹霺霻霼霽霾霿靀靁靂靃靄靅靆靇靈靉靊靋靌靍靎靏靐靑青靓靔靕靖靗靘静靚靛靜靝非靟靠靡面靣靤靥靦靧靨革靪靫靬靭靮靯靰靱靲靳靴靵靶靷靸靹靺靻靼靽靾靿鞀鞁鞂鞃鞄鞅鞆鞇鞈鞉鞊鞋鞌鞍鞎鞏鞐鞑鞒鞓鞔鞕鞖鞗鞘鞙鞚鞛鞜鞝鞞鞟鞠鞡鞢鞣鞤鞥鞦鞧鞨鞩鞪鞫鞬鞭鞮鞯鞰鞱鞲鞳鞴鞵鞶鞷鞸鞹鞺鞻鞼鞽鞾鞿韀韁韂韃韄韅韆韇韈韉韊韋韌韍韎韏韐韑韒韓韔韕韖韗韘韙韚韛韜韝韞韟韠韡韢韣韤韥韦韧韨韩韪韫韬韭韮韯韰韱韲音韴韵韶韷韸韹韺韻韼韽韾響頀頁頂頃頄項順頇須頉頊頋頌頍頎頏預頑頒頓頔頕頖頗領頙頚頛頜頝頞頟頠頡頢頣頤頥頦頧頨頩頪頫頬頭頮頯頰頱頲頳頴頵頶頷頸頹頺頻頼頽頾頿顀顁顂顃顄顅顆顇顈顉顊顋題額顎顏顐顑顒顓顔顕顖顗願顙顚顛顜顝類顟顠顡顢顣顤顥顦顧顨顩顪顫顬顭顮顯顰顱顲顳顴页顶顷顸项顺须顼顽顾顿颀颁颂颃预颅领颇颈颉颊颋颌颍颎颏颐频颒颓颔颕颖颗题颙颚颛颜额颞颟颠颡颢颣颤颥颦颧風颩颪颫颬颭颮颯颰颱颲颳颴颵颶颷颸颹颺颻颼颽颾颿飀飁飂飃飄飅飆飇飈飉飊飋飌飍风飏飐飑飒飓飔飕飖飗飘飙飚飛飜飝飞食飠飡飢飣飤飥飦飧飨飩飪飫飬飭飮飯飰飱飲飳飴飵飶飷飸飹飺飻飼飽飾飿餀餁餂餃餄餅餆餇餈餉養餋餌餍餎餏餐餑餒餓餔餕餖餗餘餙餚餛餜餝餞餟餠餡餢餣餤餥餦餧館餩餪餫餬餭餮餯餰餱餲餳餴餵餶餷餸餹餺餻餼餽餾餿饀饁饂饃饄饅饆饇饈饉饊饋饌饍饎饏饐饑饒饓饔饕饖饗饘饙饚饛饜饝饞饟饠饡饢饣饤饥饦饧饨饩饪饫饬饭饮饯饰饱饲饳饴饵饶饷饸饹饺饻饼饽饾饿馀馁馂馃馄馅馆馇馈馉馊馋馌馍馎馏馐馑馒馓馔馕首馗馘香馚馛馜馝馞馟馠馡馢馣馤馥馦馧馨馩馪馫馬馭馮馯馰馱馲馳馴馵馶馷馸馹馺馻馼馽馾馿駀駁駂駃駄駅駆駇駈駉駊駋駌駍駎駏駐駑駒駓駔駕駖駗駘駙駚駛駜駝駞駟駠駡駢駣駤駥駦駧駨駩駪駫駬駭駮駯駰駱駲駳駴駵駶駷駸駹駺駻駼駽駾駿騀騁騂騃騄騅騆騇騈騉騊騋騌騍騎騏騐騑騒験騔騕騖騗騘騙騚騛騜騝騞騟騠騡騢騣騤騥騦騧騨騩騪騫騬騭騮騯騰騱騲騳騴騵騶騷騸騹騺騻騼騽騾騿驀驁驂驃驄驅驆驇驈驉驊驋驌驍驎驏驐驑驒驓驔驕驖驗驘驙驚驛驜驝驞驟驠驡驢驣驤驥驦驧驨驩驪驫马驭驮驯驰驱驲驳驴驵驶驷驸驹驺驻驼驽驾驿骀骁骂骃骄骅骆骇骈骉骊骋验骍骎骏骐骑骒骓骔骕骖骗骘骙骚骛骜骝骞骟骠骡骢骣骤骥骦骧骨骩骪骫骬骭骮骯骰骱骲骳骴骵骶骷骸骹骺骻骼骽骾骿髀髁髂髃髄髅髆髇髈髉髊髋髌髍髎髏髐髑髒髓體髕髖髗高髙髚髛髜髝髞髟髠髡髢髣髤髥髦髧髨髩髪髫髬髭髮髯髰髱髲髳髴髵髶髷髸髹髺髻髼髽髾髿鬀鬁鬂鬃鬄鬅鬆鬇鬈鬉鬊鬋鬌鬍鬎鬏鬐鬑鬒鬓鬔鬕鬖鬗鬘鬙鬚鬛鬜鬝鬞鬟鬠鬡鬢鬣鬤鬥鬦鬧鬨鬩鬪鬫鬬鬭鬮鬯鬰鬱鬲鬳鬴鬵鬶鬷鬸鬹鬺鬻鬼鬽鬾鬿魀魁魂魃魄魅魆魇魈魉魊魋魌魍魎魏魐魑魒魓魔魕魖魗魘魙魚魛魜魝魞魟魠魡魢魣魤魥魦魧魨魩魪魫魬魭魮魯魰魱魲魳魴魵魶魷魸魹魺魻魼魽魾魿鮀鮁鮂鮃鮄鮅鮆鮇鮈鮉鮊鮋鮌鮍鮎鮏鮐鮑鮒鮓鮔鮕鮖鮗鮘鮙鮚鮛鮜鮝鮞鮟鮠鮡鮢鮣鮤鮥鮦鮧鮨鮩鮪鮫鮬鮭鮮鮯鮰鮱鮲鮳鮴鮵鮶鮷鮸鮹鮺鮻鮼鮽鮾鮿鯀鯁鯂鯃鯄鯅鯆鯇鯈鯉鯊鯋鯌鯍鯎鯏鯐鯑鯒鯓鯔鯕鯖鯗鯘鯙鯚鯛鯜鯝鯞鯟鯠鯡鯢鯣鯤鯥鯦鯧鯨鯩鯪鯫鯬鯭鯮鯯鯰鯱鯲鯳鯴鯵鯶鯷鯸鯹鯺鯻鯼鯽鯾鯿鰀鰁鰂鰃鰄鰅鰆鰇鰈鰉鰊鰋鰌鰍鰎鰏鰐鰑鰒鰓鰔鰕鰖鰗鰘鰙鰚鰛鰜鰝鰞鰟鰠鰡鰢鰣鰤鰥鰦鰧鰨鰩鰪鰫鰬鰭鰮鰯鰰鰱鰲鰳鰴鰵鰶鰷鰸鰹鰺鰻鰼鰽鰾鰿鱀鱁鱂鱃鱄鱅鱆鱇鱈鱉鱊鱋鱌鱍鱎鱏鱐鱑鱒鱓鱔鱕鱖鱗鱘鱙鱚鱛鱜鱝鱞鱟鱠鱡鱢鱣鱤鱥鱦鱧鱨鱩鱪鱫鱬鱭鱮鱯鱰鱱鱲鱳鱴鱵鱶鱷鱸鱹鱺鱻鱼鱽鱾鱿鲀鲁鲂鲃鲄鲅鲆鲇鲈鲉鲊鲋鲌鲍鲎鲏鲐鲑鲒鲓鲔鲕鲖鲗鲘鲙鲚鲛鲜鲝鲞鲟鲠鲡鲢鲣鲤鲥鲦鲧鲨鲩鲪鲫鲬鲭鲮鲯鲰鲱鲲鲳鲴鲵鲶鲷鲸鲹鲺鲻鲼鲽鲾鲿鳀鳁鳂鳃鳄鳅鳆鳇鳈鳉鳊鳋鳌鳍鳎鳏鳐鳑鳒鳓鳔鳕鳖鳗鳘鳙鳚鳛鳜鳝鳞鳟鳠鳡鳢鳣鳤鳥鳦鳧鳨鳩鳪鳫鳬鳭鳮鳯鳰鳱鳲鳳鳴鳵鳶鳷鳸鳹鳺鳻鳼鳽鳾鳿鴀鴁鴂鴃鴄鴅鴆鴇鴈鴉鴊鴋鴌鴍鴎鴏鴐鴑鴒鴓鴔鴕鴖鴗鴘鴙鴚鴛鴜鴝鴞鴟鴠鴡鴢鴣鴤鴥鴦鴧鴨鴩鴪鴫鴬鴭鴮鴯鴰鴱鴲鴳鴴鴵鴶鴷鴸鴹鴺鴻鴼鴽鴾鴿鵀鵁鵂鵃鵄鵅鵆鵇鵈鵉鵊鵋鵌鵍鵎鵏鵐鵑鵒鵓鵔鵕鵖鵗鵘鵙鵚鵛鵜鵝鵞鵟鵠鵡鵢鵣鵤鵥鵦鵧鵨鵩鵪鵫鵬鵭鵮鵯鵰鵱鵲鵳鵴鵵鵶鵷鵸鵹鵺鵻鵼鵽鵾鵿鶀鶁鶂鶃鶄鶅鶆鶇鶈鶉鶊鶋鶌鶍鶎鶏鶐鶑鶒鶓鶔鶕鶖鶗鶘鶙鶚鶛鶜鶝鶞鶟鶠鶡鶢鶣鶤鶥鶦鶧鶨鶩鶪鶫鶬鶭鶮鶯鶰鶱鶲鶳鶴鶵鶶鶷鶸鶹鶺鶻鶼鶽鶾鶿鷀鷁鷂鷃鷄鷅鷆鷇鷈鷉鷊鷋鷌鷍鷎鷏鷐鷑鷒鷓鷔鷕鷖鷗鷘鷙鷚鷛鷜鷝鷞鷟鷠鷡鷢鷣鷤鷥鷦鷧鷨鷩鷪鷫鷬鷭鷮鷯鷰鷱鷲鷳鷴鷵鷶鷷鷸鷹鷺鷻鷼鷽鷾鷿鸀鸁鸂鸃鸄鸅鸆鸇鸈鸉鸊鸋鸌鸍鸎鸏鸐鸑鸒鸓鸔鸕鸖鸗鸘鸙鸚鸛鸜鸝鸞鸟鸠鸡鸢鸣鸤鸥鸦鸧鸨鸩鸪鸫鸬鸭鸮鸯鸰鸱鸲鸳鸴鸵鸶鸷鸸鸹鸺鸻鸼鸽鸾鸿鹀鹁鹂鹃鹄鹅鹆鹇鹈鹉鹊鹋鹌鹍鹎鹏鹐鹑鹒鹓鹔鹕鹖鹗鹘鹙鹚鹛鹜鹝鹞鹟鹠鹡鹢鹣鹤鹥鹦鹧鹨鹩鹪鹫鹬鹭鹮鹯鹰鹱鹲鹳鹴鹵鹶鹷鹸鹹鹺鹻鹼鹽鹾鹿麀麁麂麃麄麅麆麇麈麉麊麋麌麍麎麏麐麑麒麓麔麕麖麗麘麙麚麛麜麝麞麟麠麡麢麣麤麥麦麧麨麩麪麫麬麭麮麯麰麱麲麳麴麵麶麷麸麹麺麻麼麽麾麿黀黁黂黃黄黅黆黇黈黉黊黋黌黍黎黏黐黑黒黓黔黕黖黗默黙黚黛黜黝點黟黠黡黢黣黤黥黦黧黨黩黪黫黬黭黮黯黰黱黲黳黴黵黶黷黸黹黺黻黼黽黾黿鼀鼁鼂鼃鼄鼅鼆鼇鼈鼉鼊鼋鼌鼍鼎鼏鼐鼑鼒鼓鼔鼕鼖鼗鼘鼙鼚鼛鼜鼝鼞鼟鼠鼡鼢鼣鼤鼥鼦鼧鼨鼩鼪鼫鼬鼭鼮鼯鼰鼱鼲鼳鼴鼵鼶鼷鼸鼹鼺鼻鼼鼽鼾鼿齀齁齂齃齄齅齆齇齈齉齊齋齌齍齎齏齐齑齒齓齔齕齖齗齘齙齚齛齜齝齞齟齠齡齢齣齤齥齦齧齨齩齪齫齬齭齮齯齰齱齲齳齴齵齶齷齸齹齺齻齼齽齾齿龀龁龂龃龄龅龆龇龈龉龊龋龌龍龎龏龐龑龒龓龔龕龖龗龘龙龚龛龜龝龞龟龠龡龢龣龤龥龦龧龨龩龪龫龬龭龮龯龰龱龲龳龴龵龶龷龸龹龺龻龼龽龾龿鿀鿁鿂鿃鿄鿅鿆鿇鿈鿉鿊鿋\u9fcc\u9fcd\u9fce\u9fcf\u9fd0\u9fd1\u9fd2\u9fd3\u9fd4\u9fd5\u9fd6\u9fd7\u9fd8\u9fd9\u9fda\u9fdb\u9fdc\u9fdd\u9fde\u9fdf\u9fe0\u9fe1\u9fe2\u9fe3\u9fe4\u9fe5\u9fe6\u9fe7\u9fe8\u9fe9\u9fea\u9feb\u9fec\u9fed\u9fee\u9fef\u9ff0\u9ff1\u9ff2\u9ff3\u9ff4\u9ff5\u9ff6\u9ff7\u9ff8\u9ff9\u9ffa\u9ffb\u9ffc\u9ffd\u9ffe\u9fff', ',') satisfies matches($s, '^(?:\\p{IsCJKUnifiedIdeographs}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCJKUnifiedIdeographs}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00285() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ꀀ\ua48f,ꀀꀁꀂꀃꀄꀅꀆꀇꀈꀉꀊꀋꀌꀍꀎꀏꀐꀑꀒꀓꀔꀕꀖꀗꀘꀙꀚꀛꀜꀝꀞꀟꀠꀡꀢꀣꀤꀥꀦꀧꀨꀩꀪꀫꀬꀭꀮꀯꀰꀱꀲꀳꀴꀵꀶꀷꀸꀹꀺꀻꀼꀽꀾꀿꁀꁁꁂꁃꁄꁅꁆꁇꁈꁉꁊꁋꁌꁍꁎꁏꁐꁑꁒꁓꁔꁕꁖꁗꁘꁙꁚꁛꁜꁝꁞꁟꁠꁡꁢꁣꁤꁥꁦꁧꁨꁩꁪꁫꁬꁭꁮꁯꁰꁱꁲꁳꁴꁵꁶꁷꁸꁹꁺꁻꁼꁽꁾꁿꂀꂁꂂꂃꂄꂅꂆꂇꂈꂉꂊꂋꂌꂍꂎꂏꂐꂑꂒꂓꂔꂕꂖꂗꂘꂙꂚꂛꂜꂝꂞꂟꂠꂡꂢꂣꂤꂥꂦꂧꂨꂩꂪꂫꂬꂭꂮꂯꂰꂱꂲꂳꂴꂵꂶꂷꂸꂹꂺꂻꂼꂽꂾꂿꃀꃁꃂꃃꃄꃅꃆꃇꃈꃉꃊꃋꃌꃍꃎꃏꃐꃑꃒꃓꃔꃕꃖꃗꃘꃙꃚꃛꃜꃝꃞꃟꃠꃡꃢꃣꃤꃥꃦꃧꃨꃩꃪꃫꃬꃭꃮꃯꃰꃱꃲꃳꃴꃵꃶꃷꃸꃹꃺꃻꃼꃽꃾꃿꄀꄁꄂꄃꄄꄅꄆꄇꄈꄉꄊꄋꄌꄍꄎꄏꄐꄑꄒꄓꄔꄕꄖꄗꄘꄙꄚꄛꄜꄝꄞꄟꄠꄡꄢꄣꄤꄥꄦꄧꄨꄩꄪꄫꄬꄭꄮꄯꄰꄱꄲꄳꄴꄵꄶꄷꄸꄹꄺꄻꄼꄽꄾꄿꅀꅁꅂꅃꅄꅅꅆꅇꅈꅉꅊꅋꅌꅍꅎꅏꅐꅑꅒꅓꅔꅕꅖꅗꅘꅙꅚꅛꅜꅝꅞꅟꅠꅡꅢꅣꅤꅥꅦꅧꅨꅩꅪꅫꅬꅭꅮꅯꅰꅱꅲꅳꅴꅵꅶꅷꅸꅹꅺꅻꅼꅽꅾꅿꆀꆁꆂꆃꆄꆅꆆꆇꆈꆉꆊꆋꆌꆍꆎꆏꆐꆑꆒꆓꆔꆕꆖꆗꆘꆙꆚꆛꆜꆝꆞꆟꆠꆡꆢꆣꆤꆥꆦꆧꆨꆩꆪꆫꆬꆭꆮꆯꆰꆱꆲꆳꆴꆵꆶꆷꆸꆹꆺꆻꆼꆽꆾꆿꇀꇁꇂꇃꇄꇅꇆꇇꇈꇉꇊꇋꇌꇍꇎꇏꇐꇑꇒꇓꇔꇕꇖꇗꇘꇙꇚꇛꇜꇝꇞꇟꇠꇡꇢꇣꇤꇥꇦꇧꇨꇩꇪꇫꇬꇭꇮꇯꇰꇱꇲꇳꇴꇵꇶꇷꇸꇹꇺꇻꇼꇽꇾꇿꈀꈁꈂꈃꈄꈅꈆꈇꈈꈉꈊꈋꈌꈍꈎꈏꈐꈑꈒꈓꈔꈕꈖꈗꈘꈙꈚꈛꈜꈝꈞꈟꈠꈡꈢꈣꈤꈥꈦꈧꈨꈩꈪꈫꈬꈭꈮꈯꈰꈱꈲꈳꈴꈵꈶꈷꈸꈹꈺꈻꈼꈽꈾꈿꉀꉁꉂꉃꉄꉅꉆꉇꉈꉉꉊꉋꉌꉍꉎꉏꉐꉑꉒꉓꉔꉕꉖꉗꉘꉙꉚꉛꉜꉝꉞꉟꉠꉡꉢꉣꉤꉥꉦꉧꉨꉩꉪꉫꉬꉭꉮꉯꉰꉱꉲꉳꉴꉵꉶꉷꉸꉹꉺꉻꉼꉽꉾꉿꊀꊁꊂꊃꊄꊅꊆꊇꊈꊉꊊꊋꊌꊍꊎꊏꊐꊑꊒꊓꊔꊕꊖꊗꊘꊙꊚꊛꊜꊝꊞꊟꊠꊡꊢꊣꊤꊥꊦꊧꊨꊩꊪꊫꊬꊭꊮꊯꊰꊱꊲꊳꊴꊵꊶꊷꊸꊹꊺꊻꊼꊽꊾꊿꋀꋁꋂꋃꋄꋅꋆꋇꋈꋉꋊꋋꋌꋍꋎꋏꋐꋑꋒꋓꋔꋕꋖꋗꋘꋙꋚꋛꋜꋝꋞꋟꋠꋡꋢꋣꋤꋥꋦꋧꋨꋩꋪꋫꋬꋭꋮꋯꋰꋱꋲꋳꋴꋵꋶꋷꋸꋹꋺꋻꋼꋽꋾꋿꌀꌁꌂꌃꌄꌅꌆꌇꌈꌉꌊꌋꌌꌍꌎꌏꌐꌑꌒꌓꌔꌕꌖꌗꌘꌙꌚꌛꌜꌝꌞꌟꌠꌡꌢꌣꌤꌥꌦꌧꌨꌩꌪꌫꌬꌭꌮꌯꌰꌱꌲꌳꌴꌵꌶꌷꌸꌹꌺꌻꌼꌽꌾꌿꍀꍁꍂꍃꍄꍅꍆꍇꍈꍉꍊꍋꍌꍍꍎꍏꍐꍑꍒꍓꍔꍕꍖꍗꍘꍙꍚꍛꍜꍝꍞꍟꍠꍡꍢꍣꍤꍥꍦꍧꍨꍩꍪꍫꍬꍭꍮꍯꍰꍱꍲꍳꍴꍵꍶꍷꍸꍹꍺꍻꍼꍽꍾꍿꎀꎁꎂꎃꎄꎅꎆꎇꎈꎉꎊꎋꎌꎍꎎꎏꎐꎑꎒꎓꎔꎕꎖꎗꎘꎙꎚꎛꎜꎝꎞꎟꎠꎡꎢꎣꎤꎥꎦꎧꎨꎩꎪꎫꎬꎭꎮꎯꎰꎱꎲꎳꎴꎵꎶꎷꎸꎹꎺꎻꎼꎽꎾꎿꏀꏁꏂꏃꏄꏅꏆꏇꏈꏉꏊꏋꏌꏍꏎꏏꏐꏑꏒꏓꏔꏕꏖꏗꏘꏙꏚꏛꏜꏝꏞꏟꏠꏡꏢꏣꏤꏥꏦꏧꏨꏩꏪꏫꏬꏭꏮꏯꏰꏱꏲꏳꏴꏵꏶꏷꏸꏹꏺꏻꏼꏽꏾꏿꐀꐁꐂꐃꐄꐅꐆꐇꐈꐉꐊꐋꐌꐍꐎꐏꐐꐑꐒꐓꐔꐕꐖꐗꐘꐙꐚꐛꐜꐝꐞꐟꐠꐡꐢꐣꐤꐥꐦꐧꐨꐩꐪꐫꐬꐭꐮꐯꐰꐱꐲꐳꐴꐵꐶꐷꐸꐹꐺꐻꐼꐽꐾꐿꑀꑁꑂꑃꑄꑅꑆꑇꑈꑉꑊꑋꑌꑍꑎꑏꑐꑑꑒꑓꑔꑕꑖꑗꑘꑙꑚꑛꑜꑝꑞꑟꑠꑡꑢꑣꑤꑥꑦꑧꑨꑩꑪꑫꑬꑭꑮꑯꑰꑱꑲꑳꑴꑵꑶꑷꑸꑹꑺꑻꑼꑽꑾꑿꒀꒁꒂꒃꒄꒅꒆꒇꒈꒉꒊꒋꒌ\ua48d\ua48e\ua48f', ',') satisfies matches($s, '^(?:\\p{IsYiSyllables}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsYiSyllables}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00286() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('꒐\ua4cf,꒐꒑꒒꒓꒔꒕꒖꒗꒘꒙꒚꒛꒜꒝꒞꒟꒠꒡꒢꒣꒤꒥꒦꒧꒨꒩꒪꒫꒬꒭꒮꒯꒰꒱꒲꒳꒴꒵꒶꒷꒸꒹꒺꒻꒼꒽꒾꒿꓀꓁꓂꓃꓄꓅꓆\ua4c7\ua4c8\ua4c9\ua4ca\ua4cb\ua4cc\ua4cd\ua4ce\ua4cf', ',') satisfies matches($s, '^(?:\\p{IsYiRadicals}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsYiRadicals}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00287() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('가힣', ',') satisfies matches($s, '^(?:\\p{IsHangulSyllables}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsHangulSyllables}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00288() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\ue000\uf8ff,\udb80\udc00\udbbf\udffd,\udbc0\udc00\udbff\udffd', ',') satisfies matches($s, '^(?:\\p{IsPrivateUse}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsPrivateUse}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00289() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('豈\ufaff,豈更車賈滑串句龜龜契金喇奈懶癩羅蘿螺裸邏樂洛烙珞落酪駱亂卵欄爛蘭鸞嵐濫藍襤拉臘蠟廊朗浪狼郎來冷勞擄櫓爐盧老蘆虜路露魯鷺碌祿綠菉錄鹿論壟弄籠聾牢磊賂雷壘屢樓淚漏累縷陋勒肋凜凌稜綾菱陵讀拏樂諾丹寧怒率異北磻便復不泌數索參塞省葉說殺辰沈拾若掠略亮兩凉梁糧良諒量勵呂女廬旅濾礪閭驪麗黎力曆歷轢年憐戀撚漣煉璉秊練聯輦蓮連鍊列劣咽烈裂說廉念捻殮簾獵令囹寧嶺怜玲瑩羚聆鈴零靈領例禮醴隸惡了僚寮尿料樂燎療蓼遼龍暈阮劉杻柳流溜琉留硫紐類六戮陸倫崙淪輪律慄栗率隆利吏履易李梨泥理痢罹裏裡里離匿溺吝燐璘藺隣鱗麟林淋臨立笠粒狀炙識什茶刺切度拓糖宅洞暴輻行降見廓兀嗀﨎﨏塚﨑晴﨓﨔凞猪益礼神祥福靖精羽﨟蘒﨡諸﨣﨤逸都﨧﨨﨩飯飼館鶴\ufa2e\ufa2f侮僧免勉勤卑喝嘆器塀墨層屮悔慨憎懲敏既暑梅海渚漢煮爫琢碑社祉祈祐祖祝禍禎穀突節練縉繁署者臭艹艹著褐視謁謹賓贈辶逸難響頻恵𤋮舘\ufa6e\ufa6f並况全侀充冀勇勺喝啕喙嗢塚墳奄奔婢嬨廒廙彩徭惘慎愈憎慠懲戴揄搜摒敖晴朗望杖歹殺流滛滋漢瀞煮瞧爵犯猪瑱甆画瘝瘟益盛直睊着磌窱節类絛練缾者荒華蝹襁覆視調諸請謁諾諭謹變贈輸遲醙鉶陼難靖韛響頋頻鬒龜𢡊𢡄𣏕㮝䀘䀹𥉉𥳐𧻓齃龎\ufada\ufadb\ufadc\ufadd\ufade\ufadf\ufae0\ufae1\ufae2\ufae3\ufae4\ufae5\ufae6\ufae7\ufae8\ufae9\ufaea\ufaeb\ufaec\ufaed\ufaee\ufaef\ufaf0\ufaf1\ufaf2\ufaf3\ufaf4\ufaf5\ufaf6\ufaf7\ufaf8\ufaf9\ufafa\ufafb\ufafc\ufafd\ufafe\ufaff', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibilityIdeographs}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibilityIdeographs}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00290() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ﬀﭏ,ﬀﬁﬂﬃﬄﬅﬆ\ufb07\ufb08\ufb09\ufb0a\ufb0b\ufb0c\ufb0d\ufb0e\ufb0f\ufb10\ufb11\ufb12ﬓﬔﬕﬖﬗ\ufb18\ufb19\ufb1a\ufb1b\ufb1cיִﬞײַﬠﬡﬢﬣﬤﬥﬦﬧﬨ﬩שׁשׂשּׁשּׂאַאָאּבּגּדּהּוּזּ\ufb37טּיּךּכּלּ\ufb3dמּ\ufb3fנּסּ\ufb42ףּפּ\ufb45צּקּרּשּתּוֹבֿכֿפֿﭏ', ',') satisfies matches($s, '^(?:\\p{IsAlphabeticPresentationForms}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsAlphabeticPresentationForms}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00291() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ﭐ\ufdff,ﭐﭑﭒﭓﭔﭕﭖﭗﭘﭙﭚﭛﭜﭝﭞﭟﭠﭡﭢﭣﭤﭥﭦﭧﭨﭩﭪﭫﭬﭭﭮﭯﭰﭱﭲﭳﭴﭵﭶﭷﭸﭹﭺﭻﭼﭽﭾﭿﮀﮁﮂﮃﮄﮅﮆﮇﮈﮉﮊﮋﮌﮍﮎﮏﮐﮑﮒﮓﮔﮕﮖﮗﮘﮙﮚﮛﮜﮝﮞﮟﮠﮡﮢﮣﮤﮥﮦﮧﮨﮩﮪﮫﮬﮭﮮﮯﮰﮱ﮲﮳﮴﮵﮶﮷﮸﮹﮺﮻﮼﮽﮾﮿﯀﯁\ufbc2\ufbc3\ufbc4\ufbc5\ufbc6\ufbc7\ufbc8\ufbc9\ufbca\ufbcb\ufbcc\ufbcd\ufbce\ufbcf\ufbd0\ufbd1\ufbd2ﯓﯔﯕﯖﯗﯘﯙﯚﯛﯜﯝﯞﯟﯠﯡﯢﯣﯤﯥﯦﯧﯨﯩﯪﯫﯬﯭﯮﯯﯰﯱﯲﯳﯴﯵﯶﯷﯸﯹﯺﯻﯼﯽﯾﯿﰀﰁﰂﰃﰄﰅﰆﰇﰈﰉﰊﰋﰌﰍﰎﰏﰐﰑﰒﰓﰔﰕﰖﰗﰘﰙﰚﰛﰜﰝﰞﰟﰠﰡﰢﰣﰤﰥﰦﰧﰨﰩﰪﰫﰬﰭﰮﰯﰰﰱﰲﰳﰴﰵﰶﰷﰸﰹﰺﰻﰼﰽﰾﰿﱀﱁﱂﱃﱄﱅﱆﱇﱈﱉﱊﱋﱌﱍﱎﱏﱐﱑﱒﱓﱔﱕﱖﱗﱘﱙﱚﱛﱜﱝﱞﱟﱠﱡﱢﱣﱤﱥﱦﱧﱨﱩﱪﱫﱬﱭﱮﱯﱰﱱﱲﱳﱴﱵﱶﱷﱸﱹﱺﱻﱼﱽﱾﱿﲀﲁﲂﲃﲄﲅﲆﲇﲈﲉﲊﲋﲌﲍﲎﲏﲐﲑﲒﲓﲔﲕﲖﲗﲘﲙﲚﲛﲜﲝﲞﲟﲠﲡﲢﲣﲤﲥﲦﲧﲨﲩﲪﲫﲬﲭﲮﲯﲰﲱﲲﲳﲴﲵﲶﲷﲸﲹﲺﲻﲼﲽﲾﲿﳀﳁﳂﳃﳄﳅﳆﳇﳈﳉﳊﳋﳌﳍﳎﳏﳐﳑﳒﳓﳔﳕﳖﳗﳘﳙﳚﳛﳜﳝﳞﳟﳠﳡﳢﳣﳤﳥﳦﳧﳨﳩﳪﳫﳬﳭﳮﳯﳰﳱﳲﳳﳴﳵﳶﳷﳸﳹﳺﳻﳼﳽﳾﳿﴀﴁﴂﴃﴄﴅﴆﴇﴈﴉﴊﴋﴌﴍﴎﴏﴐﴑﴒﴓﴔﴕﴖﴗﴘﴙﴚﴛﴜﴝﴞﴟﴠﴡﴢﴣﴤﴥﴦﴧﴨﴩﴪﴫﴬﴭﴮﴯﴰﴱﴲﴳﴴﴵﴶﴷﴸﴹﴺﴻﴼﴽ\ufd3e\ufd3f\ufd40\ufd41\ufd42\ufd43\ufd44\ufd45\ufd46\ufd47\ufd48\ufd49\ufd4a\ufd4b\ufd4c\ufd4d\ufd4e\ufd4fﵐﵑﵒﵓﵔﵕﵖﵗﵘﵙﵚﵛﵜﵝﵞﵟﵠﵡﵢﵣﵤﵥﵦﵧﵨﵩﵪﵫﵬﵭﵮﵯﵰﵱﵲﵳﵴﵵﵶﵷﵸﵹﵺﵻﵼﵽﵾﵿﶀﶁﶂﶃﶄﶅﶆﶇﶈﶉﶊﶋﶌﶍﶎﶏ\ufd90\ufd91ﶒﶓﶔﶕﶖﶗﶘﶙﶚﶛﶜﶝﶞﶟﶠﶡﶢﶣﶤﶥﶦﶧﶨﶩﶪﶫﶬﶭﶮﶯﶰﶱﶲﶳﶴﶵﶶﶷﶸﶹﶺﶻﶼﶽﶾﶿﷀﷁﷂﷃﷄﷅﷆﷇ\ufdc8\ufdc9\ufdca\ufdcb\ufdcc\ufdcd\ufdce\ufdcf﷐﷑﷒﷓﷔﷕﷖﷗﷘﷙﷚﷛﷜﷝﷞﷟﷠﷡﷢﷣﷤﷥﷦﷧﷨﷩﷪﷫﷬﷭﷮﷯ﷰﷱﷲﷳﷴﷵﷶﷷﷸﷹﷺﷻ﷼﷽\ufdfe\ufdff', ',') satisfies matches($s, '^(?:\\p{IsArabicPresentationForms-A}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsArabicPresentationForms-A}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00292() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('︠\ufe2f,︠︡︢︣︤︥︦\ufe27\ufe28\ufe29\ufe2a\ufe2b\ufe2c\ufe2d\ufe2e\ufe2f', ',') satisfies matches($s, '^(?:\\p{IsCombiningHalfMarks}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCombiningHalfMarks}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00293() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\ufe30\ufe4f,\ufe30\ufe31\ufe32\ufe33\ufe34\ufe35\ufe36\ufe37\ufe38\ufe39\ufe3a\ufe3b\ufe3c\ufe3d\ufe3e\ufe3f\ufe40\ufe41\ufe42\ufe43\ufe44\ufe45\ufe46\ufe47\ufe48\ufe49\ufe4a\ufe4b\ufe4c\ufe4d\ufe4e\ufe4f', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibilityForms}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibilityForms}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00294() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\ufe50\ufe6f,\ufe50\ufe51\ufe52\ufe53\ufe54\ufe55\ufe56\ufe57\ufe58\ufe59\ufe5a\ufe5b\ufe5c\ufe5d\ufe5e\ufe5f\ufe60\ufe61﹢\ufe63﹤﹥﹦\ufe67\ufe68﹩\ufe6a\ufe6b\ufe6c\ufe6d\ufe6e\ufe6f', ',') satisfies matches($s, '^(?:\\p{IsSmallFormVariants}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsSmallFormVariants}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00295() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ﹰ\ufefe', ',') satisfies matches($s, '^(?:\\p{IsArabicPresentationForms-B}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsArabicPresentationForms-B}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00296() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\uff00\uffef,\uff00\uff01\uff02\uff03＄\uff05\uff06\uff07\uff08\uff09\uff0a＋\uff0c\uff0d\uff0e\uff0f０１２３４５６７８９\uff1a\uff1b＜＝＞\uff1f\uff20ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ\uff3b\uff3c\uff3d＾\uff3f｀ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ\uff5b｜\uff5d～\uff5f\uff60\uff61\uff62\uff63\uff64\uff65ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝﾞﾟﾠﾡﾢﾣﾤﾥﾦﾧﾨﾩﾪﾫﾬﾭﾮﾯﾰﾱﾲﾳﾴﾵﾶﾷﾸﾹﾺﾻﾼﾽﾾ\uffbf\uffc0\uffc1ￂￃￄￅￆￇ\uffc8\uffc9ￊￋￌￍￎￏ\uffd0\uffd1ￒￓￔￕￖￗ\uffd8\uffd9ￚￛￜ\uffdd\uffde\uffdf￠￡￢￣￤￥￦\uffe7￨￩￪￫￬￭￮\uffef', ',') satisfies matches($s, '^(?:\\p{IsHalfwidthandFullwidthForms}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsHalfwidthandFullwidthForms}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00297() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\ufff0�,\ufff0\ufff1\ufff2\ufff3\ufff4\ufff5\ufff6\ufff7\ufff8\ufff9\ufffa\ufffb￼�', ',') satisfies matches($s, '^(?:\\p{IsSpecials}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsSpecials}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00298() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBasicLatin}?)$')) and (every $s in tokenize('\u0080', ',') satisfies not(matches($s, '^(?:\\p{IsBasicLatin}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00299() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLatin-1Supplement}?)$')) and (every $s in tokenize('Ā', ',') satisfies not(matches($s, '^(?:\\p{IsLatin-1Supplement}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00300() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLatinExtended-A}?)$')) and (every $s in tokenize('ƀ', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtended-A}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00301() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLatinExtended-B}?)$')) and (every $s in tokenize('ɐ', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtended-B}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00302() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsIPAExtensions}?)$')) and (every $s in tokenize('ʰ', ',') satisfies not(matches($s, '^(?:\\p{IsIPAExtensions}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00303() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSpacingModifierLetters}?)$')) and (every $s in tokenize('̀', ',') satisfies not(matches($s, '^(?:\\p{IsSpacingModifierLetters}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00304() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCyrillic}?)$')) and (every $s in tokenize('\u0530', ',') satisfies not(matches($s, '^(?:\\p{IsCyrillic}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00305() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArmenian}?)$')) and (every $s in tokenize('\u0590', ',') satisfies not(matches($s, '^(?:\\p{IsArmenian}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00306() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHebrew}?)$')) and (every $s in tokenize('\u0600', ',') satisfies not(matches($s, '^(?:\\p{IsHebrew}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00307() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArabic}?)$')) and (every $s in tokenize('\u0700', ',') satisfies not(matches($s, '^(?:\\p{IsArabic}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00308() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSyriac}?)$')) and (every $s in tokenize('ހ', ',') satisfies not(matches($s, '^(?:\\p{IsSyriac}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00309() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsThaana}?)$')) and (every $s in tokenize('ऀ', ',') satisfies not(matches($s, '^(?:\\p{IsThaana}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00310() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsDevanagari}?)$')) and (every $s in tokenize('\u0980', ',') satisfies not(matches($s, '^(?:\\p{IsDevanagari}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00311() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBengali}?)$')) and (every $s in tokenize('\u0a00', ',') satisfies not(matches($s, '^(?:\\p{IsBengali}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00312() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGurmukhi}?)$')) and (every $s in tokenize('\u0a80', ',') satisfies not(matches($s, '^(?:\\p{IsGurmukhi}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00313() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGujarati}?)$')) and (every $s in tokenize('\u0b00', ',') satisfies not(matches($s, '^(?:\\p{IsGujarati}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00314() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsOriya}?)$')) and (every $s in tokenize('\u0b80', ',') satisfies not(matches($s, '^(?:\\p{IsOriya}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00315() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsTamil}?)$')) and (every $s in tokenize('\u0c00', ',') satisfies not(matches($s, '^(?:\\p{IsTamil}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00316() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsTelugu}?)$')) and (every $s in tokenize('\u0c80', ',') satisfies not(matches($s, '^(?:\\p{IsTelugu}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00317() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKannada}?)$')) and (every $s in tokenize('\u0d00', ',') satisfies not(matches($s, '^(?:\\p{IsKannada}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00318() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMalayalam}?)$')) and (every $s in tokenize('\u0d80', ',') satisfies not(matches($s, '^(?:\\p{IsMalayalam}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00319() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSinhala}?)$')) and (every $s in tokenize('\u0e00', ',') satisfies not(matches($s, '^(?:\\p{IsSinhala}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00320() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsThai}?)$')) and (every $s in tokenize('\u0e80', ',') satisfies not(matches($s, '^(?:\\p{IsThai}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00321() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLao}?)$')) and (every $s in tokenize('ༀ', ',') satisfies not(matches($s, '^(?:\\p{IsLao}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00322() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsTibetan}?)$')) and (every $s in tokenize('က', ',') satisfies not(matches($s, '^(?:\\p{IsTibetan}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00323() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMyanmar}?)$')) and (every $s in tokenize('Ⴀ', ',') satisfies not(matches($s, '^(?:\\p{IsMyanmar}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00324() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGeorgian}?)$')) and (every $s in tokenize('ᄀ', ',') satisfies not(matches($s, '^(?:\\p{IsGeorgian}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00325() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHangulJamo}?)$')) and (every $s in tokenize('ሀ', ',') satisfies not(matches($s, '^(?:\\p{IsHangulJamo}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00326() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsEthiopic}?)$')) and (every $s in tokenize('Ꭰ', ',') satisfies not(matches($s, '^(?:\\p{IsEthiopic}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00327() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCherokee}?)$')) and (every $s in tokenize('\u1400', ',') satisfies not(matches($s, '^(?:\\p{IsCherokee}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00328() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsUnifiedCanadianAboriginalSyllabics}?)$')) and (every $s in tokenize('\u1680', ',') satisfies not(matches($s, '^(?:\\p{IsUnifiedCanadianAboriginalSyllabics}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00329() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsOgham}?)$')) and (every $s in tokenize('ᚠ', ',') satisfies not(matches($s, '^(?:\\p{IsOgham}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00330() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsRunic}?)$')) and (every $s in tokenize('ក', ',') satisfies not(matches($s, '^(?:\\p{IsRunic}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00331() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKhmer}?)$')) and (every $s in tokenize('\u1800', ',') satisfies not(matches($s, '^(?:\\p{IsKhmer}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00332() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMongolian}?)$')) and (every $s in tokenize('Ḁ', ',') satisfies not(matches($s, '^(?:\\p{IsMongolian}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00333() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLatinExtendedAdditional}?)$')) and (every $s in tokenize('ἀ', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtendedAdditional}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00334() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGreekExtended}?)$')) and (every $s in tokenize('\u2000', ',') satisfies not(matches($s, '^(?:\\p{IsGreekExtended}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00335() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGeneralPunctuation}?)$')) and (every $s in tokenize('⁰', ',') satisfies not(matches($s, '^(?:\\p{IsGeneralPunctuation}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00336() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSuperscriptsandSubscripts}?)$')) and (every $s in tokenize('₠', ',') satisfies not(matches($s, '^(?:\\p{IsSuperscriptsandSubscripts}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00337() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCurrencySymbols}?)$')) and (every $s in tokenize('⃐', ',') satisfies not(matches($s, '^(?:\\p{IsCurrencySymbols}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00338() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCombiningMarksforSymbols}?)$')) and (every $s in tokenize('℀', ',') satisfies not(matches($s, '^(?:\\p{IsCombiningMarksforSymbols}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00339() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLetterlikeSymbols}?)$')) and (every $s in tokenize('⅐', ',') satisfies not(matches($s, '^(?:\\p{IsLetterlikeSymbols}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00340() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsNumberForms}?)$')) and (every $s in tokenize('←', ',') satisfies not(matches($s, '^(?:\\p{IsNumberForms}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00341() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArrows}?)$')) and (every $s in tokenize('∀', ',') satisfies not(matches($s, '^(?:\\p{IsArrows}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00342() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMathematicalOperators}?)$')) and (every $s in tokenize('⌀', ',') satisfies not(matches($s, '^(?:\\p{IsMathematicalOperators}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00343() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMiscellaneousTechnical}?)$')) and (every $s in tokenize('␀', ',') satisfies not(matches($s, '^(?:\\p{IsMiscellaneousTechnical}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00344() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsControlPictures}?)$')) and (every $s in tokenize('⑀', ',') satisfies not(matches($s, '^(?:\\p{IsControlPictures}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00345() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsOpticalCharacterRecognition}?)$')) and (every $s in tokenize('①', ',') satisfies not(matches($s, '^(?:\\p{IsOpticalCharacterRecognition}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00346() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsEnclosedAlphanumerics}?)$')) and (every $s in tokenize('─', ',') satisfies not(matches($s, '^(?:\\p{IsEnclosedAlphanumerics}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00347() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBoxDrawing}?)$')) and (every $s in tokenize('▀', ',') satisfies not(matches($s, '^(?:\\p{IsBoxDrawing}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00348() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBlockElements}?)$')) and (every $s in tokenize('■', ',') satisfies not(matches($s, '^(?:\\p{IsBlockElements}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00349() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGeometricShapes}?)$')) and (every $s in tokenize('☀', ',') satisfies not(matches($s, '^(?:\\p{IsGeometricShapes}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00350() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMiscellaneousSymbols}?)$')) and (every $s in tokenize('\u2700', ',') satisfies not(matches($s, '^(?:\\p{IsMiscellaneousSymbols}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00351() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsDingbats}?)$')) and (every $s in tokenize('⠀', ',') satisfies not(matches($s, '^(?:\\p{IsDingbats}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00352() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBraillePatterns}?)$')) and (every $s in tokenize('⺀', ',') satisfies not(matches($s, '^(?:\\p{IsBraillePatterns}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00353() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKRadicalsSupplement}?)$')) and (every $s in tokenize('⼀', ',') satisfies not(matches($s, '^(?:\\p{IsCJKRadicalsSupplement}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00354() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKangxiRadicals}?)$')) and (every $s in tokenize('⿰', ',') satisfies not(matches($s, '^(?:\\p{IsKangxiRadicals}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00355() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsIdeographicDescriptionCharacters}?)$')) and (every $s in tokenize('\u3000', ',') satisfies not(matches($s, '^(?:\\p{IsIdeographicDescriptionCharacters}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00356() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKSymbolsandPunctuation}?)$')) and (every $s in tokenize('\u3040', ',') satisfies not(matches($s, '^(?:\\p{IsCJKSymbolsandPunctuation}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00357() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHiragana}?)$')) and (every $s in tokenize('\u30a0', ',') satisfies not(matches($s, '^(?:\\p{IsHiragana}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00358() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKatakana}?)$')) and (every $s in tokenize('\u3100', ',') satisfies not(matches($s, '^(?:\\p{IsKatakana}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00359() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBopomofo}?)$')) and (every $s in tokenize('\u3130', ',') satisfies not(matches($s, '^(?:\\p{IsBopomofo}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00360() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHangulCompatibilityJamo}?)$')) and (every $s in tokenize('㆐', ',') satisfies not(matches($s, '^(?:\\p{IsHangulCompatibilityJamo}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00361() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKanbun}?)$')) and (every $s in tokenize('ㆠ', ',') satisfies not(matches($s, '^(?:\\p{IsKanbun}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00362() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBopomofoExtended}?)$')) and (every $s in tokenize('㈀', ',') satisfies not(matches($s, '^(?:\\p{IsBopomofoExtended}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00363() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsEnclosedCJKLettersandMonths}?)$')) and (every $s in tokenize('㌀', ',') satisfies not(matches($s, '^(?:\\p{IsEnclosedCJKLettersandMonths}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00364() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibility}?)$')) and (every $s in tokenize('㐀', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibility}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00365() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionA}?)$')) and (every $s in tokenize('一', ',') satisfies not(matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionA}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00366() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKUnifiedIdeographs}?)$')) and (every $s in tokenize('ꀀ', ',') satisfies not(matches($s, '^(?:\\p{IsCJKUnifiedIdeographs}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00367() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsYiSyllables}?)$')) and (every $s in tokenize('꒐', ',') satisfies not(matches($s, '^(?:\\p{IsYiSyllables}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00368() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsYiRadicals}?)$')) and (every $s in tokenize('가', ',') satisfies not(matches($s, '^(?:\\p{IsYiRadicals}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00369() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLowSurrogates}?)$')) and (every $s in tokenize('\ue000', ',') satisfies not(matches($s, '^(?:\\p{IsLowSurrogates}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00370() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\udbc0\udc00', ',') satisfies matches($s, '^(?:\\p{IsPrivateUse}?)$')) and (every $s in tokenize('豈,\u007f', ',') satisfies not(matches($s, '^(?:\\p{IsPrivateUse}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00371() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibilityIdeographs}?)$')) and (every $s in tokenize('ﬀ', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibilityIdeographs}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00372() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsAlphabeticPresentationForms}?)$')) and (every $s in tokenize('ﭐ', ',') satisfies not(matches($s, '^(?:\\p{IsAlphabeticPresentationForms}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00373() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArabicPresentationForms-A}?)$')) and (every $s in tokenize('︠', ',') satisfies not(matches($s, '^(?:\\p{IsArabicPresentationForms-A}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00374() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCombiningHalfMarks}?)$')) and (every $s in tokenize('\ufe30', ',') satisfies not(matches($s, '^(?:\\p{IsCombiningHalfMarks}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00375() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibilityForms}?)$')) and (every $s in tokenize('\ufe50', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibilityForms}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00376() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSmallFormVariants}?)$')) and (every $s in tokenize('ﹰ', ',') satisfies not(matches($s, '^(?:\\p{IsSmallFormVariants}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00377() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSpecials}?)$')) and (every $s in tokenize('\uff00,𐌀', ',') satisfies not(matches($s, '^(?:\\p{IsSpecials}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00378() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHalfwidthandFullwidthForms}?)$')) and (every $s in tokenize('\ufff0', ',') satisfies not(matches($s, '^(?:\\p{IsHalfwidthandFullwidthForms}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00379() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsOldItalic}?)$')) and (every $s in tokenize('𐌰', ',') satisfies not(matches($s, '^(?:\\p{IsOldItalic}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00380() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGothic}?)$')) and (every $s in tokenize('𐐀', ',') satisfies not(matches($s, '^(?:\\p{IsGothic}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00381() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsDeseret}?)$')) and (every $s in tokenize('𝀀', ',') satisfies not(matches($s, '^(?:\\p{IsDeseret}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00382() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsByzantineMusicalSymbols}?)$')) and (every $s in tokenize('𝄀', ',') satisfies not(matches($s, '^(?:\\p{IsByzantineMusicalSymbols}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00383() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMusicalSymbols}?)$')) and (every $s in tokenize('𝐀', ',') satisfies not(matches($s, '^(?:\\p{IsMusicalSymbols}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00384() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMathematicalAlphanumericSymbols}?)$')) and (every $s in tokenize('𠀀', ',') satisfies not(matches($s, '^(?:\\p{IsMathematicalAlphanumericSymbols}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00385() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionB}?)$')) and (every $s in tokenize('丽', ',') satisfies not(matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionB}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00386() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibilityIdeographsSupplement}?)$')) and (every $s in tokenize('\udb40\udc00', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibilityIdeographsSupplement}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00387() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsTags}?)$')) and (every $s in tokenize('\udb80\udc00', ',') satisfies not(matches($s, '^(?:\\p{IsTags}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00388() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBasicLatin})$')) and (every $s in tokenize('ۿ', ',') satisfies not(matches($s, '^(?:\\p{IsBasicLatin})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00389() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLatin-1Supplement})$')) and (every $s in tokenize('\u007f', ',') satisfies not(matches($s, '^(?:\\p{IsLatin-1Supplement})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00390() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLatinExtended-A})$')) and (every $s in tokenize('ÿ', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtended-A})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00391() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLatinExtended-B})$')) and (every $s in tokenize('ſ', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtended-B})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00392() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsIPAExtensions})$')) and (every $s in tokenize('ɏ', ',') satisfies not(matches($s, '^(?:\\p{IsIPAExtensions})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00393() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSpacingModifierLetters})$')) and (every $s in tokenize('ʯ', ',') satisfies not(matches($s, '^(?:\\p{IsSpacingModifierLetters})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00394() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGreek})$')) and (every $s in tokenize('ͯ', ',') satisfies not(matches($s, '^(?:\\p{IsGreek})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00395() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCyrillic})$')) and (every $s in tokenize('Ͽ', ',') satisfies not(matches($s, '^(?:\\p{IsCyrillic})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00396() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArmenian})$')) and (every $s in tokenize('ӿ', ',') satisfies not(matches($s, '^(?:\\p{IsArmenian})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00397() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHebrew})$')) and (every $s in tokenize('\u058f', ',') satisfies not(matches($s, '^(?:\\p{IsHebrew})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00398() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArabic})$')) and (every $s in tokenize('\u05ff', ',') satisfies not(matches($s, '^(?:\\p{IsArabic})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00399() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSyriac})$')) and (every $s in tokenize('ۿ', ',') satisfies not(matches($s, '^(?:\\p{IsSyriac})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00400() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsThaana})$')) and (every $s in tokenize('ݏ', ',') satisfies not(matches($s, '^(?:\\p{IsThaana})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00401() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsDevanagari})$')) and (every $s in tokenize('\u07bf', ',') satisfies not(matches($s, '^(?:\\p{IsDevanagari})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00402() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBengali})$')) and (every $s in tokenize('ॿ', ',') satisfies not(matches($s, '^(?:\\p{IsBengali})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00403() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGurmukhi})$')) and (every $s in tokenize('\u09ff', ',') satisfies not(matches($s, '^(?:\\p{IsGurmukhi})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00404() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGujarati})$')) and (every $s in tokenize('\u0a7f', ',') satisfies not(matches($s, '^(?:\\p{IsGujarati})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00405() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsOriya})$')) and (every $s in tokenize('\u0aff', ',') satisfies not(matches($s, '^(?:\\p{IsOriya})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00406() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsTamil})$')) and (every $s in tokenize('\u0b7f', ',') satisfies not(matches($s, '^(?:\\p{IsTamil})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00407() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsTelugu})$')) and (every $s in tokenize('\u0bff', ',') satisfies not(matches($s, '^(?:\\p{IsTelugu})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00408() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKannada})$')) and (every $s in tokenize('౿', ',') satisfies not(matches($s, '^(?:\\p{IsKannada})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00409() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMalayalam})$')) and (every $s in tokenize('\u0cff', ',') satisfies not(matches($s, '^(?:\\p{IsMalayalam})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00410() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSinhala})$')) and (every $s in tokenize('ൿ', ',') satisfies not(matches($s, '^(?:\\p{IsSinhala})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00411() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsThai})$')) and (every $s in tokenize('\u0dff', ',') satisfies not(matches($s, '^(?:\\p{IsThai})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00412() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLao})$')) and (every $s in tokenize('\u0e7f', ',') satisfies not(matches($s, '^(?:\\p{IsLao})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00413() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsTibetan})$')) and (every $s in tokenize('\u0eff', ',') satisfies not(matches($s, '^(?:\\p{IsTibetan})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00414() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMyanmar})$')) and (every $s in tokenize('\u0fff', ',') satisfies not(matches($s, '^(?:\\p{IsMyanmar})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00415() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGeorgian})$')) and (every $s in tokenize('႟', ',') satisfies not(matches($s, '^(?:\\p{IsGeorgian})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00416() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHangulJamo})$')) and (every $s in tokenize('\u10ff', ',') satisfies not(matches($s, '^(?:\\p{IsHangulJamo})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00417() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsEthiopic})$')) and (every $s in tokenize('ᇿ', ',') satisfies not(matches($s, '^(?:\\p{IsEthiopic})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00418() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCherokee})$')) and (every $s in tokenize('\u137f', ',') satisfies not(matches($s, '^(?:\\p{IsCherokee})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00419() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsUnifiedCanadianAboriginalSyllabics})$')) and (every $s in tokenize('\u13ff', ',') satisfies not(matches($s, '^(?:\\p{IsUnifiedCanadianAboriginalSyllabics})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00420() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsOgham})$')) and (every $s in tokenize('ᙿ', ',') satisfies not(matches($s, '^(?:\\p{IsOgham})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00421() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsRunic})$')) and (every $s in tokenize('\u169f', ',') satisfies not(matches($s, '^(?:\\p{IsRunic})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00422() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKhmer})$')) and (every $s in tokenize('\u16ff', ',') satisfies not(matches($s, '^(?:\\p{IsKhmer})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00423() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMongolian})$')) and (every $s in tokenize('\u17ff', ',') satisfies not(matches($s, '^(?:\\p{IsMongolian})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00424() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLatinExtendedAdditional})$')) and (every $s in tokenize('\u18af', ',') satisfies not(matches($s, '^(?:\\p{IsLatinExtendedAdditional})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00425() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGreekExtended})$')) and (every $s in tokenize('ỿ', ',') satisfies not(matches($s, '^(?:\\p{IsGreekExtended})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00426() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGeneralPunctuation})$')) and (every $s in tokenize('\u1fff', ',') satisfies not(matches($s, '^(?:\\p{IsGeneralPunctuation})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00427() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSuperscriptsandSubscripts})$')) and (every $s in tokenize('\u206f', ',') satisfies not(matches($s, '^(?:\\p{IsSuperscriptsandSubscripts})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00428() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCurrencySymbols})$')) and (every $s in tokenize('\u209f', ',') satisfies not(matches($s, '^(?:\\p{IsCurrencySymbols})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00429() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCombiningMarksforSymbols})$')) and (every $s in tokenize('\u20cf', ',') satisfies not(matches($s, '^(?:\\p{IsCombiningMarksforSymbols})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00430() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsLetterlikeSymbols})$')) and (every $s in tokenize('\u20ff', ',') satisfies not(matches($s, '^(?:\\p{IsLetterlikeSymbols})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00431() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsNumberForms})$')) and (every $s in tokenize('⅏', ',') satisfies not(matches($s, '^(?:\\p{IsNumberForms})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00432() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArrows})$')) and (every $s in tokenize('\u218f', ',') satisfies not(matches($s, '^(?:\\p{IsArrows})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00433() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMathematicalOperators})$')) and (every $s in tokenize('⇿', ',') satisfies not(matches($s, '^(?:\\p{IsMathematicalOperators})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00434() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMiscellaneousTechnical})$')) and (every $s in tokenize('⋿', ',') satisfies not(matches($s, '^(?:\\p{IsMiscellaneousTechnical})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00435() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsControlPictures})$')) and (every $s in tokenize('\u23ff', ',') satisfies not(matches($s, '^(?:\\p{IsControlPictures})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00436() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsOpticalCharacterRecognition})$')) and (every $s in tokenize('\u243f', ',') satisfies not(matches($s, '^(?:\\p{IsOpticalCharacterRecognition})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00437() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsEnclosedAlphanumerics})$')) and (every $s in tokenize('\u245f', ',') satisfies not(matches($s, '^(?:\\p{IsEnclosedAlphanumerics})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00438() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBoxDrawing})$')) and (every $s in tokenize('⓿', ',') satisfies not(matches($s, '^(?:\\p{IsBoxDrawing})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00439() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBlockElements})$')) and (every $s in tokenize('╿', ',') satisfies not(matches($s, '^(?:\\p{IsBlockElements})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00440() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGeometricShapes})$')) and (every $s in tokenize('▟', ',') satisfies not(matches($s, '^(?:\\p{IsGeometricShapes})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00441() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMiscellaneousSymbols})$')) and (every $s in tokenize('◿', ',') satisfies not(matches($s, '^(?:\\p{IsMiscellaneousSymbols})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00442() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsDingbats})$')) and (every $s in tokenize('⛿', ',') satisfies not(matches($s, '^(?:\\p{IsDingbats})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00443() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBraillePatterns})$')) and (every $s in tokenize('➿', ',') satisfies not(matches($s, '^(?:\\p{IsBraillePatterns})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00444() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKRadicalsSupplement})$')) and (every $s in tokenize('⣿', ',') satisfies not(matches($s, '^(?:\\p{IsCJKRadicalsSupplement})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00445() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKangxiRadicals})$')) and (every $s in tokenize('\u2eff', ',') satisfies not(matches($s, '^(?:\\p{IsKangxiRadicals})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00446() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsIdeographicDescriptionCharacters})$')) and (every $s in tokenize('\u2fdf', ',') satisfies not(matches($s, '^(?:\\p{IsIdeographicDescriptionCharacters})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00447() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKSymbolsandPunctuation})$')) and (every $s in tokenize('\u2fff', ',') satisfies not(matches($s, '^(?:\\p{IsCJKSymbolsandPunctuation})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00448() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHiragana})$')) and (every $s in tokenize('〿', ',') satisfies not(matches($s, '^(?:\\p{IsHiragana})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00449() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKatakana})$')) and (every $s in tokenize('ゟ', ',') satisfies not(matches($s, '^(?:\\p{IsKatakana})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00450() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBopomofo})$')) and (every $s in tokenize('ヿ', ',') satisfies not(matches($s, '^(?:\\p{IsBopomofo})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00451() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHangulCompatibilityJamo})$')) and (every $s in tokenize('\u312f', ',') satisfies not(matches($s, '^(?:\\p{IsHangulCompatibilityJamo})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00452() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsKanbun})$')) and (every $s in tokenize('\u318f', ',') satisfies not(matches($s, '^(?:\\p{IsKanbun})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00453() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsBopomofoExtended})$')) and (every $s in tokenize('㆟', ',') satisfies not(matches($s, '^(?:\\p{IsBopomofoExtended})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00454() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsEnclosedCJKLettersandMonths})$')) and (every $s in tokenize('\u31bf', ',') satisfies not(matches($s, '^(?:\\p{IsEnclosedCJKLettersandMonths})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00455() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibility})$')) and (every $s in tokenize('\u32ff', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibility})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00456() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionA})$')) and (every $s in tokenize('㏿', ',') satisfies not(matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionA})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00457() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKUnifiedIdeographs})$')) and (every $s in tokenize('䶵', ',') satisfies not(matches($s, '^(?:\\p{IsCJKUnifiedIdeographs})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00458() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsYiSyllables})$')) and (every $s in tokenize('\u9fff', ',') satisfies not(matches($s, '^(?:\\p{IsYiSyllables})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00459() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsYiRadicals})$')) and (every $s in tokenize('\ua48f', ',') satisfies not(matches($s, '^(?:\\p{IsYiRadicals})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00460() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHangulSyllables})$')) and (every $s in tokenize('\ua4cf', ',') satisfies not(matches($s, '^(?:\\p{IsHangulSyllables})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00461() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHighSurrogates})$')) and (every $s in tokenize('''힣', ',') satisfies not(matches($s, '^(?:\\p{IsHighSurrogates})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00462() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibilityIdeographs})$')) and (every $s in tokenize('\uf8ff', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibilityIdeographs})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00463() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsAlphabeticPresentationForms})$')) and (every $s in tokenize('\ufaff', ',') satisfies not(matches($s, '^(?:\\p{IsAlphabeticPresentationForms})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00464() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArabicPresentationForms-A})$')) and (every $s in tokenize('ﭏ', ',') satisfies not(matches($s, '^(?:\\p{IsArabicPresentationForms-A})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00465() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCombiningHalfMarks})$')) and (every $s in tokenize('\ufdff', ',') satisfies not(matches($s, '^(?:\\p{IsCombiningHalfMarks})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00466() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibilityForms})$')) and (every $s in tokenize('\ufe2f', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibilityForms})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00467() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSmallFormVariants})$')) and (every $s in tokenize('\ufe4f', ',') satisfies not(matches($s, '^(?:\\p{IsSmallFormVariants})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00468() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsArabicPresentationForms-B})$')) and (every $s in tokenize('\ufe6f', ',') satisfies not(matches($s, '^(?:\\p{IsArabicPresentationForms-B})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00469() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsSpecials})$')) and (every $s in tokenize('\ufefe,\uffef', ',') satisfies not(matches($s, '^(?:\\p{IsSpecials})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00470() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHalfwidthandFullwidthForms})$')) and (every $s in tokenize('\ufeff', ',') satisfies not(matches($s, '^(?:\\p{IsHalfwidthandFullwidthForms})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00471() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsOldItalic})$')) and (every $s in tokenize('�', ',') satisfies not(matches($s, '^(?:\\p{IsOldItalic})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00472() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsGothic})$')) and (every $s in tokenize('\ud800\udf2f', ',') satisfies not(matches($s, '^(?:\\p{IsGothic})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00473() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsDeseret})$')) and (every $s in tokenize('\ud800\udf4f', ',') satisfies not(matches($s, '^(?:\\p{IsDeseret})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00474() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsByzantineMusicalSymbols})$')) and (every $s in tokenize('𐑏', ',') satisfies not(matches($s, '^(?:\\p{IsByzantineMusicalSymbols})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00475() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMusicalSymbols})$')) and (every $s in tokenize('\ud834\udcff', ',') satisfies not(matches($s, '^(?:\\p{IsMusicalSymbols})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00476() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsMathematicalAlphanumericSymbols})$')) and (every $s in tokenize('\ud834\uddff', ',') satisfies not(matches($s, '^(?:\\p{IsMathematicalAlphanumericSymbols})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00477() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionB})$')) and (every $s in tokenize('𝟿', ',') satisfies not(matches($s, '^(?:\\p{IsCJKUnifiedIdeographsExtensionB})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00478() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsCJKCompatibilityIdeographsSupplement})$')) and (every $s in tokenize('𪛖', ',') satisfies not(matches($s, '^(?:\\p{IsCJKCompatibilityIdeographsSupplement})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00479() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsTags})$')) and (every $s in tokenize('\ud87e\ude1f', ',') satisfies not(matches($s, '^(?:\\p{IsTags})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00480() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('\udbbf\udffd', ',') satisfies matches($s, '^(?:\\p{IsPrivateUse})$')) and (every $s in tokenize('\udb40\udc7f', ',') satisfies not(matches($s, '^(?:\\p{IsPrivateUse})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00481() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a, ', ',') satisfies matches($s, '^(?:.)$')) and (every $s in tokenize('aa,', ',') satisfies not(matches($s, '^(?:.)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00482() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(' ,\n" +
      ",\r,\t', ',') satisfies matches($s, '^(?:\\s)$')) and (every $s in tokenize('a,', ',') satisfies not(matches($s, '^(?:\\s)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00483() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('  \t\n" +
      "\ra c\n" +
      "\t\r a \n" +
      "\r\t   \r\n" +
      ",aa a', ',') satisfies matches($s, '^(?:\\s*\\c\\s?\\c\\s+\\c\\s*)$')) and (every $s in tokenize(' a  a a,aaa, a aa ', ',') satisfies not(matches($s, '^(?:\\s*\\c\\s?\\c\\s+\\c\\s*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00484() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('aa,a a,a   a', ',') satisfies matches($s, '^(?:a\\s{0,3}a)$')) and (every $s in tokenize('a    a,aa a', ',') satisfies not(matches($s, '^(?:a\\s{0,3}a)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00485() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:a\\sb)$')) and (every $s in tokenize('a \n" +
      "b', ',') satisfies not(matches($s, '^(?:a\\sb)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00486() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a', ',') satisfies matches($s, '^(?:\\S)$')) and (every $s in tokenize(' ,\n" +
      ",\r,\t,aa', ',') satisfies not(matches($s, '^(?:\\S)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00487() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\S+)$')) and (every $s in tokenize('a b', ',') satisfies not(matches($s, '^(?:\\S+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00488() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\S*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\S*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00489() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a b\t, a  \r', ',') satisfies matches($s, '^(?:\\S?\\s?\\S?\\s+)$')) and (every $s in tokenize('a  b, a b,ab', ',') satisfies not(matches($s, '^(?:\\S?\\s?\\S?\\s+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00490() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('_,:,a', ',') satisfies matches($s, '^(?:\\i)$')) and (every $s in tokenize(' ,\n" +
      ",\r,\t', ',') satisfies not(matches($s, '^(?:\\i)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00491() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('_:abcdefghijklmnopqrstuvwxyzAZ:_', ',') satisfies matches($s, '^(?:\\i*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\i*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00492() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\i+)$')) and (every $s in tokenize('a b', ',') satisfies not(matches($s, '^(?:\\i+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00493() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('zabcsdea', ',') satisfies matches($s, '^(?:\\c\\i*a)$')) and (every $s in tokenize('ab', ',') satisfies not(matches($s, '^(?:\\c\\i*a)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00494() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a b  c  Z  :_   d\ry \tb \n" +
      "   ', ',') satisfies matches($s, '^(?:[\\s\\i]*)$')) and (every $s in tokenize('1', ',') satisfies not(matches($s, '^(?:[\\s\\i]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00495() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(' ,\n" +
      ",\r,\t', ',') satisfies matches($s, '^(?:\\I)$')) and (every $s in tokenize('_,:,a', ',') satisfies not(matches($s, '^(?:\\I)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00496() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1234', ',') satisfies matches($s, '^(?:\\I*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\I*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00497() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a  123c', ',') satisfies matches($s, '^(?:a\\I+\\c)$')) and (every $s in tokenize('b123c,a123 123cc', ',') satisfies not(matches($s, '^(?:a\\I+\\c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00498() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('_,:,a', ',') satisfies matches($s, '^(?:\\c)$')) and (every $s in tokenize(' ,\n" +
      ",\r,\t', ',') satisfies not(matches($s, '^(?:\\c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00499() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('c?1 abc,?0\rzzz', ',') satisfies matches($s, '^(?:\\c?\\?\\d\\s\\c+)$')) and (every $s in tokenize('aa?3 c,a?2\n" +
      "', ',') satisfies not(matches($s, '^(?:\\c?\\?\\d\\s\\c+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00500() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a,aa,aaaaaaaaaaaaaaaaaaaaaaaaaa', ',') satisfies matches($s, '^(?:\\c?\\c+\\c*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\c?\\c+\\c*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00501() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(' ,\n" +
      ",\r,\t', ',') satisfies matches($s, '^(?:\\C)$')) and (every $s in tokenize('_,:,a', ',') satisfies not(matches($s, '^(?:\\C)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00502() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a*a**a***,aa*a', ',') satisfies matches($s, '^(?:\\c\\C?\\c\\C+\\c\\C*)$')) \n" +
      "        and (every $s in tokenize(',a12b1c1,ab12345,1a2a2,a1b1c1a', ',') satisfies not(matches($s, '^(?:\\c\\C?\\c\\C+\\c\\C*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00503() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('0,۰,০,੦,૦,୦,௧,౦,೦,൦,๐,໐,༠,၀,០,᠐,０,𝟎,9,٩,۹,९,৯,੯,૯,୯,௯,౯,೯,൯,๙,໙,༩,၉,៩,᠙,９,𝟿', ',') satisfies matches($s, '^(?:\\d)$')) \n" +
      "        and (every $s in tokenize('),ٙ,ۮ,\u0965,\u09e5,\u0a65,\u0ae5,\u0b65,\u0c65,\u0ce5,\u0d65,้,\u0ecf,༙,္,\u1368,\u17df,\u1809,\uff09,\ud835\udfcd,:,\u066a,ۺ,\u0970,ৰ,\u0a79,\u0af0,୰,௰,\u0c70,\u0cf0,൰,\u0e5a,\u0eda,༪,\u104a,፲,\u17ea,\u181a,\uff1a,\ud836\udc00', ',') satisfies not(matches($s, '^(?:\\d)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00504() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('),ٙ,ۮ,\u0965,\u09e5,\u0a65,\u0ae5,\u0b65,\u0c65,\u0ce5,\u0d65,้,\u0ecf,༙,္,\u1368,\u17df,\u1809,\uff09,\ud835\udfcd,:,\u066a,ۺ,\u0970,ৰ,\u0a79,\u0af0,୰,௰,\u0c70,\u0cf0,൰,\u0e5a,\u0eda,༪,\u104a,፲,\u17ea,\u181a,\uff1a,\ud836\udc00', ',') satisfies matches($s, '^(?:\\D)$')) \n" +
      "        and (every $s in tokenize('0,٠,۰,०,০,૦,௧,౦,೦,൦,๐,໐,༠,၀,០,᠐,０,𝟎,9,٩,۹,९,৯,੯,૯,୯,௯,౯,೯,൯,๙,໙,༩,၉,៩,᠙,９,𝟿', ',') satisfies not(matches($s, '^(?:\\D)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00505() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\w)$')) and (every $s in tokenize('\uf8ff,\u070f,\u007f,\u2010,\ufe37,},\u201c,»,\u0f04, ,\u2028,\u2029', ',') satisfies not(matches($s, '^(?:\\w)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00506() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\W)$')) and (every $s in tokenize('A,𝞨,a,a,𝟉,ǅ,ǅ,ῼ,ʰ,ʰ,ﾟ,א,א,𪘀,ً,𝆭,ः,𝅲,ः,𝅲,⃝,⃝,⃢,０,𝟿,𐍊,𐍊,〥,²,²,𐌣,⁄,￢,₠,₠,￦,゛,゛,￣,㆐,㆐,𝇝', ',') satisfies not(matches($s, '^(?:\\W)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00507() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('true', ',') satisfies matches($s, '^(?:true)$')) and (every $s in tokenize('false', ',') satisfies not(matches($s, '^(?:true)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00508() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('false', ',') satisfies matches($s, '^(?:false)$')) and (every $s in tokenize('true', ',') satisfies not(matches($s, '^(?:false)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00509() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('true,false', ',') satisfies matches($s, '^(?:(true|false))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(true|false))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00510() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1', ',') satisfies matches($s, '^(?:(1|true))$')) and (every $s in tokenize('0', ',') satisfies not(matches($s, '^(?:(1|true))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00511() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('0', ',') satisfies matches($s, '^(?:(1|true|false|0|0))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(1|true|false|0|0))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00512() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1111,11001010', ',') satisfies matches($s, '^(?:([0-1]{4}|(0|1){8}))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([0-1]{4}|(0|1){8}))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00513() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('AF01D1', ',') satisfies matches($s, '^(?:AF01D1)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:AF01D1)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00514() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1.001,1.001', ',') satisfies matches($s, '^(?:\\d*\\.\\d+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\d*\\.\\d+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00515() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('http://www.foo.com', ',') satisfies matches($s, '^(?:http://\\c*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:http://\\c*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00516() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a:b', ',') satisfies matches($s, '^(?:[\\i\\c]+:[\\i\\c]+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\i\\c]+:[\\i\\c]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00517() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('P1111Y12M', ',') satisfies matches($s, '^(?:P\\p{Nd}{4}Y\\p{Nd}{2}M)$')) and (every $s in tokenize('P111Y12M,P1111Y1M,P11111Y12M,P1111Y,P12M,P11111Y00M,P11111Y13M', ',') satisfies not(matches($s, '^(?:P\\p{Nd}{4}Y\\p{Nd}{2}M)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00518() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('2001-06-06T12:12:00', ',') satisfies matches($s, '^(?:\\p{Nd}{4}-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{Nd}{4}-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00519() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('11:00:00,13:20:00-05:00', ',') satisfies matches($s, '^(?:\\p{Nd}{2}:\\d\\d:\\d\\d(\\-\\d\\d:\\d\\d)?)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{Nd}{2}:\\d\\d:\\d\\d(\\-\\d\\d:\\d\\d)?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00520() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1999-12-12', ',') satisfies matches($s, '^(?:\\p{Nd}{4}-\\p{Nd}{2}-\\p{Nd}{2})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{Nd}{4}-\\p{Nd}{2}-\\p{Nd}{2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00521() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\p{Nd}{4}-\\[{Nd}{2}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00522() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1999', ',') satisfies matches($s, '^(?:\\p{Nd}{4})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{Nd}{4})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00523() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{Nd}{2})$')) and (every $s in tokenize('1999', ',') satisfies not(matches($s, '^(?:\\p{Nd}{2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00524() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('--03-14', ',') satisfies matches($s, '^(?:--0[123]\\-(12|14))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:--0[123]\\-(12|14))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00525() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('---30', ',') satisfies matches($s, '^(?:---([123]0)|([12]?[1-9])|(31))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:---([123]0)|([12]?[1-9])|(31))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00526() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('--12--', ',') satisfies matches($s, '^(?:--((0[1-9])|(1(1|2)))--)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:--((0[1-9])|(1(1|2)))--)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00527() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a,abcdef', ',') satisfies matches($s, '^(?:\\c+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\c+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00528() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ch-a', ',') satisfies matches($s, '^(?:\\c{2,4})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\c{2,4})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00529() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ab', ',') satisfies matches($s, '^(?:[\\i\\c]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\i\\c]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00530() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('a1b,ab,ab,name1', ',') satisfies matches($s, '^(?:\\c[\\c\\d]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\c[\\c\\d]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00531() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('10000101,10000201', ',') satisfies matches($s, '^(?:\\p{Nd}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{Nd}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00532() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\-\\d\\d)$')) and (every $s in tokenize('11', ',') satisfies not(matches($s, '^(?:\\-\\d\\d)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00533() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\-?\\d)$')) and (every $s in tokenize('+1', ',') satisfies not(matches($s, '^(?:\\-?\\d)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00534() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('123,12', ',') satisfies matches($s, '^(?:\\d+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\d+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00535() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('-300', ',') satisfies matches($s, '^(?:\\-?[0-3]{3})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\-?[0-3]{3})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00536() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('-128', ',') satisfies matches($s, '^(?:((\\-|\\+)?[1-127])|(\\-?128))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:((\\-|\\+)?[1-127])|(\\-?128))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00537() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1111', ',') satisfies matches($s, '^(?:\\p{Nd}\\d+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{Nd}\\d+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00538() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('123', ',') satisfies matches($s, '^(?:\\d+\\d+\\d+)$')) and (every $s in tokenize('12', ',') satisfies not(matches($s, '^(?:\\d+\\d+\\d+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00539() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\d+\\d+\\p{Nd}\\d+)$')) and (every $s in tokenize('123', ',') satisfies not(matches($s, '^(?:\\d+\\d+\\p{Nd}\\d+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00540() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('+1,1,+9', ',') satisfies matches($s, '^(?:\\+?\\d)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\+?\\d)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00541() {
    final XQuery query = new XQuery(
      "matches('qwerty','++')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00542() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('9,0', ',') satisfies matches($s, '^(?:[0-9]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[0-9]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00543() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('-11111,-9', ',') satisfies matches($s, '^(?:\\-[0-9]*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\-[0-9]*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00544() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('1,3', ',') satisfies matches($s, '^(?:[13])$')) and (every $s in tokenize('2', ',') satisfies not(matches($s, '^(?:[13])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00545() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('112233123,abcaabbccabc', ',') satisfies matches($s, '^(?:[123]+|[abc]+)$')) and (every $s in tokenize('1a,a1', ',') satisfies not(matches($s, '^(?:[123]+|[abc]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00546() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('112233123,abcaabbccabc,abab', ',') satisfies matches($s, '^(?:([abc]+)|([123]+))$')) and (every $s in tokenize('1a,1a,x', ',') satisfies not(matches($s, '^(?:([abc]+)|([123]+))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00547() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abab', ',') satisfies matches($s, '^(?:[abxyz]+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[abxyz]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00548() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Hello World', ',') satisfies matches($s, '^(?:(\\p{Lu}\\w*)\\s(\\p{Lu}\\w*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\p{Lu}\\w*)\\s(\\p{Lu}\\w*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00549() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Hello World', ',') satisfies matches($s, '^(?:(\\p{Lu}\\p{Ll}*)\\s(\\p{Lu}\\p{Ll}*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\p{Lu}\\p{Ll}*)\\s(\\p{Lu}\\p{Ll}*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00550() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Hello World', ',') satisfies matches($s, '^(?:(\\P{Ll}\\p{Ll}*)\\s(\\P{Ll}\\p{Ll}*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\P{Ll}\\p{Ll}*)\\s(\\P{Ll}\\p{Ll}*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00551() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('hellO worlD', ',') satisfies matches($s, '^(?:(\\P{Lu}+\\p{Lu})\\s(\\P{Lu}+\\p{Lu}))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\P{Lu}+\\p{Lu})\\s(\\P{Lu}+\\p{Lu}))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00552() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ǅello ǅorld', ',') satisfies matches($s, '^(?:(\\p{Lt}\\w*)\\s(\\p{Lt}*\\w*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\p{Lt}\\w*)\\s(\\p{Lt}*\\w*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00553() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Hello World', ',') satisfies matches($s, '^(?:(\\P{Lt}\\w*)\\s(\\P{Lt}*\\w*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\P{Lt}\\w*)\\s(\\P{Lt}*\\w*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00554() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[@-D]+)$')) and (every $s in tokenize('eE?@ABCDabcdeE', ',') satisfies not(matches($s, '^(?:[@-D]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00555() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[>-D]+)$')) and (every $s in tokenize('eE=>?@ABCDabcdeE', ',') satisfies not(matches($s, '^(?:[>-D]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00556() {
    final XQuery query = new XQuery(
      "matches('qwerty','[\\u0554-\\u0557]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00557() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[X-\\]]+)$')) and (every $s in tokenize('wWXYZxyz[\\]^', ',') satisfies not(matches($s, '^(?:[X-\\]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00558() {
    final XQuery query = new XQuery(
      "matches('qwerty','[X-\\u0533]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00559() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[X-a]+)$')) and (every $s in tokenize('wWAXYZaxyz', ',') satisfies not(matches($s, '^(?:[X-a]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00560() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[X-c]+)$')) and (every $s in tokenize('wWABCXYZabcxyz', ',') satisfies not(matches($s, '^(?:[X-c]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00561() {
    final XQuery query = new XQuery(
      "matches('qwerty','[X-\\u00C0]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00562() {
    final XQuery query = new XQuery(
      "matches('qwerty','[\\u0100\\u0102\\u0104]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00563() {
    final XQuery query = new XQuery(
      "matches('qwerty','[B-D\\u0130]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00564() {
    final XQuery query = new XQuery(
      "matches('qwerty','[\\u013B\\u013D\\u013F]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00565() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Foo Bar,Foo Bar', ',') satisfies matches($s, '^(?:(Foo) (Bar))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(Foo) (Bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00566() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\p{klsak')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00567() {
    final XQuery query = new XQuery(
      "matches('qwerty','{5')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00568() {
    final XQuery query = new XQuery(
      "matches('qwerty','{5,')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00569() {
    final XQuery query = new XQuery(
      "matches('qwerty','{5,6')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00570() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?r:foo)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00571() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?c:foo)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00572() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?n:(foo)(\\s+)(bar))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00573() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?e:foo)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00574() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?+i:foo)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00575() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:foo([\\d]*)bar)$')) and (every $s in tokenize('hello123foo230927bar1412d,hello123foo230927bar1412d', ',') satisfies not(matches($s, '^(?:foo([\\d]*)bar)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00576() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([\\D]*)bar)$')) and (every $s in tokenize('65498foobar58719,65498foobar58719', ',') satisfies not(matches($s, '^(?:([\\D]*)bar)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00577() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:foo([\\s]*)bar)$')) and (every $s in tokenize('wiofoo   bar3270,wiofoo   bar3270', ',') satisfies not(matches($s, '^(?:foo([\\s]*)bar)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00578() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:foo([\\S]*))$')) and (every $s in tokenize('sfdfoobar    3270,sfdfoobar    3270', ',') satisfies not(matches($s, '^(?:foo([\\S]*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00579() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:foo([\\w]*))$')) and (every $s in tokenize('sfdfoobar    3270,sfdfoobar    3270', ',') satisfies not(matches($s, '^(?:foo([\\w]*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00580() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:foo([\\W]*)bar)$')) and (every $s in tokenize('wiofoo   bar3270,wiofoo   bar3270', ',') satisfies not(matches($s, '^(?:foo([\\W]*)bar)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00581() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Hello World,Hello World', ',') satisfies matches($s, '^(?:([\\p{Lu}]\\w*)\\s([\\p{Lu}]\\w*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([\\p{Lu}]\\w*)\\s([\\p{Lu}]\\w*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00582() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Hello World,Hello World', ',') satisfies matches($s, '^(?:([\\P{Ll}][\\p{Ll}]*)\\s([\\P{Ll}][\\p{Ll}]*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([\\P{Ll}][\\p{Ll}]*)\\s([\\P{Ll}][\\p{Ll}]*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00583() {
    final XQuery query = new XQuery(
      "matches('qwerty','foo([a-\\d]*)bar')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00584() {
    final XQuery query = new XQuery(
      "matches('qwerty','([5-\\D]*)bar')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00585() {
    final XQuery query = new XQuery(
      "matches('qwerty','foo([6-\\s]*)bar')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00586() {
    final XQuery query = new XQuery(
      "matches('qwerty','foo([c-\\S]*)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00587() {
    final XQuery query = new XQuery(
      "matches('qwerty','foo([7-\\w]*)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00588() {
    final XQuery query = new XQuery(
      "matches('qwerty','foo([a-\\W]*)bar')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00589() {
    final XQuery query = new XQuery(
      "matches('qwerty','([f-\\p{Lu}]\\w*)\\s([\\p{Lu}]\\w*)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00590() {
    final XQuery query = new XQuery(
      "matches('qwerty','([1-\\P{Ll}][\\p{Ll}]*)\\s([\\P{Ll}][\\p{Ll}]*)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00591() {
    final XQuery query = new XQuery(
      "matches('qwerty','[\\p]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00592() {
    final XQuery query = new XQuery(
      "matches('qwerty','[\\P]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00593() {
    final XQuery query = new XQuery(
      "matches('qwerty','([\\pfoo])')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00594() {
    final XQuery query = new XQuery(
      "matches('qwerty','([\\Pfoo])')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00595() {
    final XQuery query = new XQuery(
      "matches('qwerty','(\\p{')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00596() {
    final XQuery query = new XQuery(
      "matches('qwerty','(\\p{Ll')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00597() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)([\\x41]*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00598() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)([\\u0041]*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00599() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)([\\r]*)(bar))$')) and (every $s in tokenize('foo   bar', ',') satisfies not(matches($s, '^(?:(foo)([\\r]*)(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00600() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)([\\o]*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00601() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)\\d*bar)$')) and (every $s in tokenize('hello123foo230927bar1412d', ',') satisfies not(matches($s, '^(?:(foo)\\d*bar)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00602() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\D*(bar))$')) and (every $s in tokenize('65498foobar58719', ',') satisfies not(matches($s, '^(?:\\D*(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00603() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)\\s*(bar))$')) and (every $s in tokenize('wiofoo   bar3270', ',') satisfies not(matches($s, '^(?:(foo)\\s*(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00604() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)\\S*)$')) and (every $s in tokenize('sfdfoobar    3270', ',') satisfies not(matches($s, '^(?:(foo)\\S*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00605() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)\\w*)$')) and (every $s in tokenize('sfdfoobar    3270', ',') satisfies not(matches($s, '^(?:(foo)\\w*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00606() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)\\W*(bar))$')) and (every $s in tokenize('wiofoo   bar3270', ',') satisfies not(matches($s, '^(?:(foo)\\W*(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00607() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Hello World', ',') satisfies matches($s, '^(?:\\p{Lu}(\\w*)\\s\\p{Lu}(\\w*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{Lu}(\\w*)\\s\\p{Lu}(\\w*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00608() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Hello World', ',') satisfies matches($s, '^(?:\\P{Ll}\\p{Ll}*\\s\\P{Ll}\\p{Ll}*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\P{Ll}\\p{Ll}*\\s\\P{Ll}\\p{Ll}*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00609() {
    final XQuery query = new XQuery(
      "matches('qwerty','foo(?(?#COMMENT)foo)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00610() {
    final XQuery query = new XQuery(
      "matches('qwerty','foo(?(?afdfoo)bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00611() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo) #foo        \\s+ #followed by 1 or more whitespace        (bar)  #followed by bar        )$')) and (every $s in tokenize('foo    bar', ',') satisfies not(matches($s, '^(?:(foo) #foo        \\s+ #followed by 1 or more whitespace        (bar)  #followed by bar        )$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00612() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo) #foo        \\s+ #followed by 1 or more whitespace        (bar)  #followed by bar)$')) and (every $s in tokenize('foo    bar', ',') satisfies not(matches($s, '^(?:(foo) #foo        \\s+ #followed by 1 or more whitespace        (bar)  #followed by bar)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00613() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo) (?#foo) \\s+ (?#followed by 1 or more whitespace) (bar)  (?#followed by bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00614() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo) (?#foo) \\s+ (?#followed by 1 or more whitespace')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00615() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\077)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00616() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\77)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00617() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)(\\176))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(foo)(\\176))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00618() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\300)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00619() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\477)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00620() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\777)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00621() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\7770)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00622() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\7)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00623() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\40)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00624() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\040)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00625() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\377)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00626() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\400)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00627() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2a*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00628() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2b*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00629() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2c*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00630() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2d*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00631() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2e*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00632() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2f*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00633() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2A*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00634() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2B*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00635() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2C*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00636() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2D*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00637() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2E*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00638() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\x2F*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00639() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)(\\c*)(bar))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(foo)(\\c*)(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00640() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)\\c)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(foo)\\c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00641() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)(\\c *)(bar))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(foo)(\\c *)(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00642() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)(\\c?*)(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00643() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)(\\c`*)(bar))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(foo)(\\c`*)(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00644() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)(\\c\\|*)(bar))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(foo)(\\c\\|*)(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00645() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)(\\c\\[*)(bar))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(foo)(\\c\\[*)(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00646() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\A(foo)\\s+(bar)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00647() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)\\s+(bar)\\Z')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00648() {
    final XQuery query = new XQuery(
      "matches('qwerty','(foo)\\s+(bar)\\z')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00649() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\b@foo')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00650() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\b,foo')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00651() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\b\\[foo')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00652() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\B@foo')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00653() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\B,foo')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00654() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\B\\[foo')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00655() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('fooʰ barʱ', ',') satisfies matches($s, '^(?:(\\w+)\\s+(\\w+))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\w+)\\s+(\\w+))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00656() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo\\w+)\\s+(bar\\w+))$')) and (every $s in tokenize('STARTfooー bar々END,STARTfooﾞ barﾟEND', ',') satisfies not(matches($s, '^(?:(foo\\w+)\\s+(bar\\w+))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00657() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([^{}]|\\n)+)$')) and (every $s in tokenize('{{{{Hello  World  }END', ',') satisfies not(matches($s, '^(?:([^{}]|\\n)+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00658() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(([0-9])|([a-z])|([A-Z]))*)$')) and (every $s in tokenize('{hello 1234567890 world},{HELLO 1234567890 world},{1234567890 hello  world}', ',') satisfies not(matches($s, '^(?:(([0-9])|([a-z])|([A-Z]))*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00659() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(([0-9])|([a-z])|([A-Z]))+)$')) and (every $s in tokenize('{hello 1234567890 world},{HELLO 1234567890 world},{1234567890 hello world}', ',') satisfies not(matches($s, '^(?:(([0-9])|([a-z])|([A-Z]))+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00660() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('aaabbbcccdddeeefff', ',') satisfies matches($s, '^(?:(([a-d]*)|([a-z]*)))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([a-d]*)|([a-z]*)))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00661() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('dddeeeccceee', ',') satisfies matches($s, '^(?:(([d-f]*)|([c-e]*)))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([d-f]*)|([c-e]*)))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00662() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('dddeeeccceee', ',') satisfies matches($s, '^(?:(([c-e]*)|([d-f]*)))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([c-e]*)|([d-f]*)))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00663() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('aaabbbcccdddeeefff', ',') satisfies matches($s, '^(?:(([a-d]*)|(.*)))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([a-d]*)|(.*)))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00664() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('dddeeeccceee', ',') satisfies matches($s, '^(?:(([d-f]*)|(.*)))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([d-f]*)|(.*)))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00665() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('dddeeeccceee', ',') satisfies matches($s, '^(?:(([c-e]*)|(.*)))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([c-e]*)|(.*)))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00666() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:CH)$')) and (every $s in tokenize('Ch,Ch', ',') satisfies not(matches($s, '^(?:CH)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00667() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:cH)$')) and (every $s in tokenize('Ch,Ch', ',') satisfies not(matches($s, '^(?:cH)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00668() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:AA)$')) and (every $s in tokenize('Aa,Aa', ',') satisfies not(matches($s, '^(?:AA)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00669() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:aA)$')) and (every $s in tokenize('Aa,Aa', ',') satisfies not(matches($s, '^(?:aA)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00670() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ı)$')) and (every $s in tokenize('I,I,I,i,I,i', ',') satisfies not(matches($s, '^(?:ı)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00671() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:İ)$')) and (every $s in tokenize('i,i,I,i,I,i', ',') satisfies not(matches($s, '^(?:İ)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00672() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([0-9]+?)([\\w]+?))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([0-9]+?)([\\w]+?))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00673() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([0-9]+?)([a-z]+?))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([0-9]+?)([a-z]+?))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00674() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]{0,16}*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00675() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]{1,}*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00676() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]{1}*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00677() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]{0,16}?*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00678() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]{1,}?*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00679() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]{1}?*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00680() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]*+$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00681() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]+*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00682() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]?*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00683() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]*?+$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00684() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]+?*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00685() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]??*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00686() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]*{0,5}$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00687() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]+{0,5}$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00688() {
    final XQuery query = new XQuery(
      "matches('qwerty','^[abcd]?{0,5}$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00689() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:http://([a-zA-z0-9\\-]*\\.?)*?(:[0-9]*)??/)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:http://([a-zA-z0-9\\-]*\\.?)*?(:[0-9]*)??/)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00690() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:http://([a-zA-Z0-9\\-]*\\.?)*?/)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:http://([a-zA-Z0-9\\-]*\\.?)*?/)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00691() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([a-z]*?)([\\w]))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([a-z]*?)([\\w]))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00692() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('foo', ',') satisfies matches($s, '^(?:([a-z]*)([\\w]))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([a-z]*)([\\w]))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00693() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[abcd-[d]]+)$')) and (every $s in tokenize('dddaabbccddd', ',') satisfies not(matches($s, '^(?:[abcd-[d]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00694() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[357]]+)$')) and (every $s in tokenize('33312468955,51246897,3312468977', ',') satisfies not(matches($s, '^(?:[\\d-[357]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00695() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\w-[b-y]]+)$')) and (every $s in tokenize('bbbaaaABCD09zzzyyy,bbbaaaABCD09zzzyyy,bbbaaaABCD09zzzyyy,bbbaaaABCD09zzzyyy', ',') satisfies not(matches($s, '^(?:[\\w-[b-y]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00696() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\w-[\\d]]+)$')) and (every $s in tokenize('0AZaz9', ',') satisfies not(matches($s, '^(?:[\\w-[\\d]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00697() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\w-[\\p{Ll}]]+)$')) and (every $s in tokenize('a09AZz', ',') satisfies not(matches($s, '^(?:[\\w-[\\p{Ll}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00698() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[13579]]+)$')) and (every $s in tokenize('1024689', ',') satisfies not(matches($s, '^(?:[\\d-[13579]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00699() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\p{Ll}-[ae-z]]+)$')) and (every $s in tokenize('aaabbbcccdddeee', ',') satisfies not(matches($s, '^(?:[\\p{Ll}-[ae-z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00700() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\p{Nd}-[2468]]+)$')) and (every $s in tokenize('20135798', ',') satisfies not(matches($s, '^(?:[\\p{Nd}-[2468]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00701() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\P{Lu}-[ae-z]]+)$')) and (every $s in tokenize('aaabbbcccdddeee', ',') satisfies not(matches($s, '^(?:[\\P{Lu}-[ae-z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00702() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[abcd-[def]]+)$')) and (every $s in tokenize('fedddaabbccddd', ',') satisfies not(matches($s, '^(?:[abcd-[def]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00703() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[357a-z]]+)$')) and (every $s in tokenize('az33312468955', ',') satisfies not(matches($s, '^(?:[\\d-[357a-z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00704() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[de357fgA-Z]]+)$')) and (every $s in tokenize('AZ51246897', ',') satisfies not(matches($s, '^(?:[\\d-[de357fgA-Z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00705() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[357\\p{Ll}]]+)$')) and (every $s in tokenize('az3312468977', ',') satisfies not(matches($s, '^(?:[\\d-[357\\p{Ll}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00706() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\w-[b-y\\s]]+)$')) and (every $s in tokenize('  bbbaaaABCD09zzzyyy', ',') satisfies not(matches($s, '^(?:[\\w-[b-y\\s]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00707() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\w-[\\d\\p{Po}]]+)$')) and (every $s in tokenize('!#0AZaz9', ',') satisfies not(matches($s, '^(?:[\\w-[\\d\\p{Po}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00708() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\w-[\\p{Ll}\\s]]+)$')) and (every $s in tokenize('a09AZz', ',') satisfies not(matches($s, '^(?:[\\w-[\\p{Ll}\\s]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00709() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[13579a-zA-Z]]+)$')) and (every $s in tokenize('AZ1024689', ',') satisfies not(matches($s, '^(?:[\\d-[13579a-zA-Z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00710() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[13579abcd]]+)$')) and (every $s in tokenize('abcd١02468٠', ',') satisfies not(matches($s, '^(?:[\\d-[13579abcd]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00711() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[13579\\s]]+)$')) and (every $s in tokenize('  ١02468٠', ',') satisfies not(matches($s, '^(?:[\\d-[13579\\s]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00712() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\w-[b-y\\p{Po}]]+)$')) and (every $s in tokenize('!#bbbaaaABCD09zzzyyy', ',') satisfies not(matches($s, '^(?:[\\w-[b-y\\p{Po}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00713() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ';') satisfies matches($s, '^(?:[\\w-[b-y!.,]]+)$')) and (every $s in tokenize('!.,bbbaaaABCD09zzzyyy', ';') satisfies not(matches($s, '^(?:[\\w-[b-y!.,]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00714() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\p{Ll}-[ae-z0-9]]+)$')) and (every $s in tokenize('09aaabbbcccdddeee', ',') satisfies not(matches($s, '^(?:[\\p{Ll}-[ae-z0-9]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00715() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\p{Nd}-[2468az]]+)$')) and (every $s in tokenize('az20135798', ',') satisfies not(matches($s, '^(?:[\\p{Nd}-[2468az]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00716() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\P{Lu}-[ae-zA-Z]]+)$')) and (every $s in tokenize('AZaaabbbcccdddeee', ',') satisfies not(matches($s, '^(?:[\\P{Lu}-[ae-zA-Z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00717() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[abc-[defg]]+)$')) and (every $s in tokenize('dddaabbccddd', ',') satisfies not(matches($s, '^(?:[abc-[defg]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00718() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[abc]]+)$')) and (every $s in tokenize('abc09abc', ',') satisfies not(matches($s, '^(?:[\\d-[abc]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00719() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[a-zA-Z]]+)$')) and (every $s in tokenize('az09AZ,azAZ١02468٠', ',') satisfies not(matches($s, '^(?:[\\d-[a-zA-Z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00720() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[\\p{Ll}]]+)$')) and (every $s in tokenize('az09az', ',') satisfies not(matches($s, '^(?:[\\d-[\\p{Ll}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00721() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\w-[\\p{Po}]]+)$')) and (every $s in tokenize('#a09AZz!', ',') satisfies not(matches($s, '^(?:[\\w-[\\p{Po}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00722() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\d-[\\D]]+)$')) and (every $s in tokenize('azAZ1024689', ',') satisfies not(matches($s, '^(?:[\\d-[\\D]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00723() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-zA-Z0-9-[\\s]]+)$')) and (every $s in tokenize('  azAZ09', ',') satisfies not(matches($s, '^(?:[a-zA-Z0-9-[\\s]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00724() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\p{Ll}-[A-Z]]+)$')) and (every $s in tokenize('AZaz09', ',') satisfies not(matches($s, '^(?:[\\p{Ll}-[A-Z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00725() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\p{Nd}-[a-z]]+)$')) and (every $s in tokenize('az09', ',') satisfies not(matches($s, '^(?:[\\p{Nd}-[a-z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00726() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\P{Lu}-[\\p{Lu}]]+)$')) and (every $s in tokenize('AZazAZ', ',') satisfies not(matches($s, '^(?:[\\P{Lu}-[\\p{Lu}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00727() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\P{Lu}-[A-Z]]+)$')) and (every $s in tokenize('AZazAZ', ',') satisfies not(matches($s, '^(?:[\\P{Lu}-[A-Z]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00728() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\P{Nd}-[\\p{Nd}]]+)$')) and (every $s in tokenize('azAZ09', ',') satisfies not(matches($s, '^(?:[\\P{Nd}-[\\p{Nd}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00729() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\P{Nd}-[2-8]]+)$')) and (every $s in tokenize('1234567890azAZ1234567890', ',') satisfies not(matches($s, '^(?:[\\P{Nd}-[2-8]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00730() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([ ]|[\\w-[0-9]])+)$')) and (every $s in tokenize('09az AZ90', ',') satisfies not(matches($s, '^(?:([ ]|[\\w-[0-9]])+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00731() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([0-9-[02468]]|[0-9-[13579]])+)$')) and (every $s in tokenize('az1234567890za', ',') satisfies not(matches($s, '^(?:([0-9-[02468]]|[0-9-[13579]])+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00732() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([^0-9-[a-zAE-Z]]|[\\w-[a-zAF-Z]])+)$')) and (every $s in tokenize('azBCDE1234567890BCDEFza', ',') satisfies not(matches($s, '^(?:([^0-9-[a-zAE-Z]]|[\\w-[a-zAF-Z]])+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00733() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([\\p{Ll}-[aeiou]]|[^\\w-[\\s]])+)$')) and (every $s in tokenize('aeiobcdxyz!@#aeio', ',') satisfies not(matches($s, '^(?:([\\p{Ll}-[aeiou]]|[^\\w-[\\s]])+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00734() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:98[\\d-[9]][\\d-[8]][\\d-[0]])$')) and (every $s in tokenize('98911 98881 98870 98871', ',') satisfies not(matches($s, '^(?:98[\\d-[9]][\\d-[8]][\\d-[0]])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00735() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:m[\\w-[^aeiou]][\\w-[^aeiou]]t)$')) and (every $s in tokenize('mbbt mect meet', ',') satisfies not(matches($s, '^(?:m[\\w-[^aeiou]][\\w-[^aeiou]]t)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00736() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[abcdef-[^bce]]+)$')) and (every $s in tokenize('adfbcefda', ',') satisfies not(matches($s, '^(?:[abcdef-[^bce]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00737() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[^cde-[ag]]+)$')) and (every $s in tokenize('agbfxyzga', ',') satisfies not(matches($s, '^(?:[^cde-[ag]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00738() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[\\p{IsGreek}-[\\P{Lu}]]+)$')) and (every $s in tokenize('ΐϾΆΈϬϮЀ', ',') satisfies not(matches($s, '^(?:[\\p{IsGreek}-[\\P{Lu}]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00739() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-zA-Z-[aeiouAEIOU]]+)$')) and (every $s in tokenize('aeiouAEIOUbcdfghjklmnpqrstvwxyz', ',') satisfies not(matches($s, '^(?:[a-zA-Z-[aeiouAEIOU]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00740() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[abcd\\-d-[bc]]+)$')) and (every $s in tokenize('bbbaaa---dddccc,bbbaaa---dddccc', ',') satisfies not(matches($s, '^(?:[abcd\\-d-[bc]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00741() {
    final XQuery query = new XQuery(
      "matches('qwerty','[^a-f-[\\x00-\\x60\\u007B-\\uFFFF]]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00742() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a-f-[]]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00743() {
    final XQuery query = new XQuery(
      "matches('qwerty','[\\[\\]a-f-[[]]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00744() {
    final XQuery query = new XQuery(
      "matches('qwerty','[\\[\\]a-f-[]]]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00745() {
    final XQuery query = new XQuery(
      "matches('qwerty','[ab\\-\\[cd-[-[]]]]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00746() {
    final XQuery query = new XQuery(
      "matches('qwerty','[ab\\-\\[cd-[[]]]]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00747() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-[a-f]])$')) and (every $s in tokenize('abcdefghijklmnopqrstuvwxyz', ',') satisfies not(matches($s, '^(?:[a-[a-f]])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00748() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-[c-e]]+)$')) and (every $s in tokenize('bbbaaaccc,```aaaccc', ',') satisfies not(matches($s, '^(?:[a-[c-e]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00749() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-d\\--[bc]]+)$')) and (every $s in tokenize('cccaaa--dddbbb', ',') satisfies not(matches($s, '^(?:[a-d\\--[bc]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00750() {
    final XQuery query = new XQuery(
      "matches('qwerty','[[abcd]-[bc]]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00751() {
    final XQuery query = new XQuery(
      "matches('qwerty','[-[e-g]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00752() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[-e-g]+)$')) and (every $s in tokenize('ddd---eeefffggghhh,ddd---eeefffggghhh', ',') satisfies not(matches($s, '^(?:[-e-g]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00753() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a-e - m-p]+)$')) and (every $s in tokenize('---a b c d e m n o p---', ',') satisfies not(matches($s, '^(?:[a-e - m-p]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00754() {
    final XQuery query = new XQuery(
      "matches('qwerty','[^-[bc]]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00755() {
    final XQuery query = new XQuery(
      "matches('qwerty','[A-[]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00756() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a\\-[bc]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00757() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a\\-[\\-\\-bc]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00758() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[a\\-\\[\\-\\[\\-bc]+)$')) and (every $s in tokenize('```bbbaaa---[[[cccddd', ',') satisfies not(matches($s, '^(?:[a\\-\\[\\-\\[\\-bc]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00759() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[abc\\--[b]]+)$')) and (every $s in tokenize('[[[```bbbaaa---cccddd', ',') satisfies not(matches($s, '^(?:[abc\\--[b]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00760() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:[abc\\-z-[b]]+)$')) and (every $s in tokenize('```aaaccc---zzzbbb', ',') satisfies not(matches($s, '^(?:[abc\\-z-[b]]+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00761() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a-d\\-[b]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00762() {
    final XQuery query = new XQuery(
      "matches('qwerty','[abcd\\-d\\-[bc]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00763() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a - c - [ b ] ]+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00764() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a - c - [ b ] +')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00765() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?<first_name>\\\\S+)\\\\s(?<last_name>\\\\S+)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00766() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a+)(?:b*)(ccc)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00767() {
    final XQuery query = new XQuery(
      "matches('qwerty','abc(?=XXX)\\w+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00768() {
    final XQuery query = new XQuery(
      "matches('qwerty','abc(?!XXX)\\w+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00769() {
    final XQuery query = new XQuery(
      "matches('qwerty','[^0-9]+(?>[0-9]+)3')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00770() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:^aa$)$')) and (every $s in tokenize('aA', ',') satisfies not(matches($s, '^(?:^aa$)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00771() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:^Aa$)$')) and (every $s in tokenize('aA', ',') satisfies not(matches($s, '^(?:^Aa$)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00772() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\s+\\d+)$')) and (every $s in tokenize('sdf 12sad', ',') satisfies not(matches($s, '^(?:\\s+\\d+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00773() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:foo\\d+)$')) and (every $s in tokenize('0123456789foo4567890foo         ,0123456789foo4567890foo1foo  0987', ',') satisfies not(matches($s, '^(?:foo\\d+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00774() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:foo\\s+)$')) and (every $s in tokenize('0123456789foo4567890foo         ', ',') satisfies not(matches($s, '^(?:foo\\s+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00775() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('hellofoo barworld', ',') satisfies matches($s, '^(?:(hello)foo\\s+bar(world))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(hello)foo\\s+bar(world))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00776() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(hello)\\s+(world))$')) and (every $s in tokenize('What the hello world goodby,What the hello world goodby,START hello    world END,START hello    world END', ',') satisfies not(matches($s, '^(?:(hello)\\s+(world))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00777() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(foo)\\s+(bar))$')) and (every $s in tokenize('before textfoo barafter text,before textfoo barafter text,before textfoo barafter text', ',') satisfies not(matches($s, '^(?:(foo)\\s+(bar))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00778() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(d)(o)(g)(\\s)(c)(a)(t)(\\s)(h)(a)(s))$')) and (every $s in tokenize('My dog cat has fleas.,My dog cat has fleas.', ',') satisfies not(matches($s, '^(?:(d)(o)(g)(\\s)(c)(a)(t)(\\s)(h)(a)(s))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00779() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:^([a-z0-9]+)@([a-z]+)\\.([a-z]+)$)$')) and (every $s in tokenize('bar@bar.foo.com', ',') satisfies not(matches($s, '^(?:^([a-z0-9]+)@([a-z]+)\\.([a-z]+)$)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00780() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:^http://www.([a-zA-Z0-9]+)\\.([a-z]+)$)$')) and (every $s in tokenize('http://www.foo.bar.com', ',') satisfies not(matches($s, '^(?:^http://www.([a-zA-Z0-9]+)\\.([a-z]+)$)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00781() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('abc\\nsfc', ',') satisfies matches($s, '^(?:(.*))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(.*))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00782() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:            ((.)+)      )$')) and (every $s in tokenize('abc', ',') satisfies not(matches($s, '^(?:            ((.)+)      )$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00783() {
    final XQuery query = new XQuery(
      "(every $s in tokenize(' abc       ', ',') satisfies matches($s, '^(?: ([^/]+)       )$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?: ([^/]+)       )$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00784() {
    final XQuery query = new XQuery(
      "matches('qwerty','.*\\B(SUCCESS)\\B.*')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00785() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\060(\\061)?\\061')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00786() {
    final XQuery query = new XQuery(
      "matches('qwerty','(\\x30\\x31\\x32)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00787() {
    final XQuery query = new XQuery(
      "matches('qwerty','(\\u0034)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00788() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(a+)(b*)(c?))$')) and (every $s in tokenize('aaabbbccc', ',') satisfies not(matches($s, '^(?:(a+)(b*)(c?))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00789() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(d+?)(e*?)(f??))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(d+?)(e*?)(f??))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00790() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('aaa', ',') satisfies matches($s, '^(?:(111|aaa))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(111|aaa))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00791() {
    final XQuery query = new XQuery(
      "matches('qwerty','(abbc)(?(1)111|222)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00792() {
    final XQuery query = new XQuery(
      "matches('qwerty','.*\\b(\\w+)\\b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00793() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ab.cc', ',') satisfies matches($s, '^(?:a+\\.?b*\\.+c{2})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:a+\\.?b*\\.+c{2})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00794() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(abra(cad)?)+)$')) and (every $s in tokenize('abracadabra1abracadabra2abracadabra3', ',') satisfies not(matches($s, '^(?:(abra(cad)?)+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00795() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:^(cat|chat))$')) and (every $s in tokenize('cats are bad', ',') satisfies not(matches($s, '^(?:^(cat|chat))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00796() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('209.25.0.111', ',') satisfies matches($s, '^(?:([0-9]+(\\.[0-9]+){3}))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([0-9]+(\\.[0-9]+){3}))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00797() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:qqq(123)*)$')) and (every $s in tokenize('Startqqq123123End', ',') satisfies not(matches($s, '^(?:qqq(123)*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00798() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(\\s)?(-))$')) and (every $s in tokenize('once -upon-a time', ',') satisfies not(matches($s, '^(?:(\\s)?(-))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00799() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:a(.)c(.)e)$')) and (every $s in tokenize('123abcde456aBCDe789', ',') satisfies not(matches($s, '^(?:a(.)c(.)e)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00800() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('Price: 5 dollars', ',') satisfies matches($s, '^(?:(\\S+):\\W(\\d+)\\s(\\D+))$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(\\S+):\\W(\\d+)\\s(\\D+))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00801() {
    final XQuery query = new XQuery(
      "matches('qwerty','a[b-a]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00802() {
    final XQuery query = new XQuery(
      "matches('qwerty','a[]b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00803() {
    final XQuery query = new XQuery(
      "matches('qwerty','a[')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00804() {
    final XQuery query = new XQuery(
      "matches('qwerty','a]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00805() {
    final XQuery query = new XQuery(
      "matches('qwerty','a[]]b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00806() {
    final XQuery query = new XQuery(
      "matches('qwerty','a[^]b]c')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00807() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\ba\\b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00808() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\by\\b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00809() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\Ba\\B')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00810() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\By\\b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00811() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\by\\B')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00812() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\By\\B')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00813() {
    final XQuery query = new XQuery(
      "matches('qwerty','(*)b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00814() {
    final XQuery query = new XQuery(
      "matches('qwerty','a\\')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00815() {
    final XQuery query = new XQuery(
      "matches('qwerty','abc)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00816() {
    final XQuery query = new XQuery(
      "matches('qwerty','(abc')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00817() {
    final XQuery query = new XQuery(
      "matches('qwerty','a**')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00818() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:a.+?c)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:a.+?c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00819() {
    final XQuery query = new XQuery(
      "matches('qwerty','))((')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00820() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\10((((((((((a))))))))))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00821() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\1(abc)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00822() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\1([a-c]*)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00823() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\1')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00824() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\2')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00825() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(a)|\\1)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(a)|\\1)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00826() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a)|\\6')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00827() {
    final XQuery query = new XQuery(
      "matches('qwerty','(\\2b*?([a-c]))*')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00828() {
    final XQuery query = new XQuery(
      "matches('qwerty','(\\2b*?([a-c])){3}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00829() {
    final XQuery query = new XQuery(
      "matches('qwerty','(x(a)\\3(\\2|b))+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00830() {
    final XQuery query = new XQuery(
      "matches('qwerty','((a)\\3(\\2|b)){2,}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00831() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab*?bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab*?bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00832() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab{0,}?bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab{0,}?bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00833() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab+?bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab+?bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00834() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab{1,}?bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab{1,}?bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00835() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab{1,3}?bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab{1,3}?bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00836() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab{3,4}?bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab{3,4}?bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00837() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab{4,5}?bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab{4,5}?bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00838() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab??bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab??bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00839() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab{0,1}?bc)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab{0,1}?bc)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00840() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab??c)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab??c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00841() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:ab{0,1}?c)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:ab{0,1}?c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00842() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:a.*?c)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:a.*?c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00843() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:a.{0,5}?c)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:a.{0,5}?c)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00844() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(a+|b){0,1}?)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(a+|b){0,1}?)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00845() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:(?:(?:(?:(?:(?:(?:(?:(?:(a))))))))))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00846() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:(?:(?:(?:(?:(?:(?:(?:(?:(a|b|c))))))))))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00847() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d)a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00848() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d)*a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00849() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d)+?a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00850() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d)+a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00851() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){2}a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00852() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){4,5}a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00853() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){4,5}?a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00854() {
    final XQuery query = new XQuery(
      "matches('qwerty',':(?:')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00855() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){6,7}a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00856() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){6,7}?a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00857() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){5,6}a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00858() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){5,6}?a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00859() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){5,7}a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00860() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|c|d){5,7}?a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00861() {
    final XQuery query = new XQuery(
      "matches('qwerty','(.)(?:b|(c|e){1,2}?|d)+?a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00862() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:a\\1?){4}$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00863() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:a(?(1)\\1)){4}$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00864() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:(f)(o)(o)|(b)(a)(r))*')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00865() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:..)*a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00866() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:..)*?a')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00867() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:(?i)a)b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00868() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?i)a)b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00869() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?i:a)b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00870() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?i:a))b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00871() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:(?-i)a)b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00872() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?-i)a)b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00873() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?-i:a)b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00874() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?-i:a))b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00875() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?-i:a.))b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00876() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?s-i:a.))b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00877() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:c|d)(?:)(?:a(?:)(?:b)(?:b(?:))(?:b(?:)(?:b)))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00878() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?:c|d)(?:)(?:aaaaaaaa(?:)(?:bbbbbbbb)(?:bbbbbbbb(?:))(?:bbbbbbbb(?:)(?:bbbbbbbb)))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00879() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\1\\d(ab)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00880() {
    final XQuery query = new XQuery(
      "matches('qwerty','x(~~)*(?:(?:F)?)?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00881() {
    final XQuery query = new XQuery(
      "matches('qwerty','^a(?#xxx){3}c')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00882() {
    final XQuery query = new XQuery(
      "matches('qwerty','^a (?#xxx) (?#yyy) {3}c')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00883() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:?:a?b?)*$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00884() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?s)^a(.))((?m)^b$)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00885() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?m)^b$)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00886() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?m)^b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00887() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?m)^(b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00888() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?m)^b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00889() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\n((?m)^b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00890() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?s).)c(?!.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00891() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?s)b.)c(?!.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00892() {
    final XQuery query = new XQuery(
      "matches('qwerty','((c*)(?(1)a|b))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00893() {
    final XQuery query = new XQuery(
      "matches('qwerty','((q*)(?(1)b|a))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00894() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(1)a|b)(x)?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00895() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(1)b|a)(x)?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00896() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(1)b|a)()?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00897() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(1)b|a)()')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00898() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(1)a|b)()?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00899() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:?(2)(\\())blah(\\))?$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00900() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:?(2)(\\())blah(\\)+)?$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00901() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(1?)a|b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00902() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(1)a|b|c)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00903() {
    final XQuery query = new XQuery(
      "matches('qwerty','(ba\\2)(?=(a+?))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00904() {
    final XQuery query = new XQuery(
      "matches('qwerty','ba\\1(?=(a+?))$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00905() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?>a+)b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00906() {
    final XQuery query = new XQuery(
      "matches('qwerty','([[:]+)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00907() {
    final XQuery query = new XQuery(
      "matches('qwerty','([[=]+)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00908() {
    final XQuery query = new XQuery(
      "matches('qwerty','([[.]+)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00909() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a[:xyz:')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00910() {
    final XQuery query = new XQuery(
      "matches('qwerty','[a[:xyz:]')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00911() {
    final XQuery query = new XQuery(
      "matches('qwerty','([a[:xyz:]b]+)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00912() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?>a+)b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00913() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?>(a+))b')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00914() {
    final XQuery query = new XQuery(
      "matches('qwerty','((?>[^()]+)|\\([^()]*\\))+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00915() {
    final XQuery query = new XQuery(
      "matches('qwerty','a{37,17}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00916() {
    final XQuery query = new XQuery(
      "matches('qwerty','a\\Z')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00917() {
    final XQuery query = new XQuery(
      "matches('qwerty','b\\Z')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00918() {
    final XQuery query = new XQuery(
      "matches('qwerty','b\\z')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00919() {
    final XQuery query = new XQuery(
      "matches('qwerty','round\\(((?>[^()]+))\\)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00920() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a\\1|(?(1)\\1)){2}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00921() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a\\1|(?(1)\\1)){1,2}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00922() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a\\1|(?(1)\\1)){0,2}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00923() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a\\1|(?(1)\\1)){2,}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00924() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a\\1|(?(1)\\1)){1,2}?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00925() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a\\1|(?(1)\\1)){0,2}?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00926() {
    final XQuery query = new XQuery(
      "matches('qwerty','(a\\1|(?(1)\\1)){2,}?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00927() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\1a(\\d*){0,2}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00928() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\1a(\\d*){2,}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00929() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\1a(\\d*){0,2}?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00930() {
    final XQuery query = new XQuery(
      "matches('qwerty','\\1a(\\d*){2,}?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00931() {
    final XQuery query = new XQuery(
      "matches('qwerty','z\\1a(\\d*){2,}?')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00932() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:((((((((((a))))))))))\\10)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:((((((((((a))))))))))\\10)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00933() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(abc)\\1)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(abc)\\1)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00934() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:([a-c]*)\\1)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:([a-c]*)\\1)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00935() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(([a-c])b*?\\2)*)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([a-c])b*?\\2)*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00936() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:(([a-c])b*?\\2){3})$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:(([a-c])b*?\\2){3})$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00937() {
    final XQuery query = new XQuery(
      "matches('qwerty','((\\3|b)\\2(a)x)+')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00938() {
    final XQuery query = new XQuery(
      "matches('qwerty','((\\3|b)\\2(a)){2,}')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00939() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?!b).')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00940() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?=d).')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00941() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?=c|d).')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00942() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d)(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00943() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d)*(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00944() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d)+?(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00945() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d)+(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00946() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){2}(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00947() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){4,5}(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00948() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){4,5}?(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00949() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){6,7}(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00950() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){6,7}?(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00951() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){5,6}(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00952() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){5,6}?(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00953() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){5,7}(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00954() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|c|d){5,7}?(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00955() {
    final XQuery query = new XQuery(
      "matches('qwerty','a(?:b|(c|e){1,2}?|d)+?(.)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00956() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:?:b|a(?=(.)))*\\1')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00957() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ab9ab', ',') satisfies matches($s, '^(?:(ab)\\d\\1)$')) and (every $s in tokenize('ab9aa', ',') satisfies not(matches($s, '^(?:(ab)\\d\\1)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00958() {
    final XQuery query = new XQuery(
      "matches('qwerty','((q*)(?(1)a|b))')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00959() {
    final XQuery query = new XQuery(
      "matches('qwerty','(x)?(?(1)a|b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00960() {
    final XQuery query = new XQuery(
      "matches('qwerty','(x)?(?(1)b|a)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00961() {
    final XQuery query = new XQuery(
      "matches('qwerty','()?(?(1)b|a)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00962() {
    final XQuery query = new XQuery(
      "matches('qwerty','()(?(1)b|a)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00963() {
    final XQuery query = new XQuery(
      "matches('qwerty','()?(?(1)a|b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00964() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:\\()?blah(?(1)(\\)))$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00965() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:\\(+)?blah(?(1)(\\)))$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00966() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(?!a)a|b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00967() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(?!a)b|a)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00968() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(?=a)b|a)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00969() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?(?=a)a|b)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00970() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?=(a+?))(\\1ab)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00971() {
    final XQuery query = new XQuery(
      "matches('qwerty','^(?:?=(a+?))\\1ab')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00972() {
    final XQuery query = new XQuery(
      "matches('33a34', '^(\\d){0,2}a\\1$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00973() {
    final XQuery query = new XQuery(
      "matches('333a334', '^(\\d*){2,}a\\1$')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00974() {
    final XQuery query = new XQuery(
      "\n" +
      "         matches('22a3', '^(\\d*){0,2}?a\\1$')\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00975() {
    final XQuery query = new XQuery(
      "\n" +
      "        matches('22a3', '^(\\d*){2,}?a\\1$')\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00976() {
    final XQuery query = new XQuery(
      "\n" +
      "        matches('22a22z', '^(\\d*){2,}?a\\1z$')\n" +
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
      assertType("xs:boolean")
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00976a() {
    final XQuery query = new XQuery(
      "\n" +
      "        matches('22a22', '^(\\d*){2,}?a\\1z$')\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00976b() {
    final XQuery query = new XQuery(
      "\n" +
      "        matches('22a22', '^(\\d{2,})a\\1$')\n" +
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
      assertBoolean(true)
    );
  }

  /**
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00977() {
    final XQuery query = new XQuery(
      "matches('qwerty','(?>\\d+)3')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00978() {
    final XQuery query = new XQuery(
      "matches('qwerty','(\\w(?=aa)aa)')",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00979() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('̴̵̶̷̸̡̢̧̨̛̖̗̘̙̜̝̞̟̠̣̤̥̦̩̪̫̬̭̮̯̰̱̲̳̹̺̻̼͇͈͉͍͎̀́̂̃̄̅̆̇̈̉̊̋̌̍̎̏̐̑̒̓̔̽̾̿̀́͂̓̈́͆͊͋͌̕̚ͅ͏͓͔͕͖͙͚͐͑͒͗͛ͣͤͥͦͧͨͩͪͫͬͭͮͯ͘͜͟͢͝͞͠͡', ',') satisfies matches($s, '^(?:\\p{IsCombiningDiacriticalMarks}+)$')) and (every $s in tokenize('a', ',') satisfies not(matches($s, '^(?:\\p{IsCombiningDiacriticalMarks}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00980() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('ЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюяѐёђѓєѕіїјљњћќѝўџѠѡѢѣѤѥѦѧѨѩѪѫѬѭѮѯѰѱѲѳѴѵѶѷѸѹѺѻѼѽѾѿҀҁ҂҃҄҅҆҇҈҉ҊҋҌҍҎҏҐґҒғҔҕҖҗҘҙҚқҜҝҞҟҠҡҢңҤҥҦҧҨҩҪҫҬҭҮүҰұҲҳҴҵҶҷҸҹҺһҼҽҾҿӀӁӂӃӄӅӆӇӈӉӊӋӌӍӎӏӐӑӒӓӔӕӖӗӘәӚӛӜӝӞӟӠӡӢӣӤӥӦӧӨөӪӫӬӭӮӯӰӱӲӳӴӵӶӷӸӹӺӻӼӽӾӿ', ',') satisfies matches($s, '^(?:\\p{IsCyrillic}+)$')) and (every $s in tokenize('a', ',') satisfies not(matches($s, '^(?:\\p{IsCyrillic}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00981() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('', ',') satisfies matches($s, '^(?:\\p{IsHighSurrogates}+)$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:\\p{IsHighSurrogates}+)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00982() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('test@someverylongemailaddress.com', ',') satisfies matches($s, '^(?:^([0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*@(([0-9a-zA-Z])+([-\\w]*[0-9a-zA-Z])*\\.)+[a-zA-Z]{2,9}))$')) and (every $s in tokenize('mhk%mhk.me.uk', ',') satisfies not(matches($s, '^(?:^([0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*@(([0-9a-zA-Z])+([-\\w]*[0-9a-zA-Z])*\\.)+[a-zA-Z]{2,9}))$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00983() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('first.last@seznam.cz,first-last@seznam.cz', ',') satisfies matches($s, '^(?:[\\w\\-\\.]+@.*)$')) and (every $s in tokenize('first_last@seznam.cz', ',') satisfies not(matches($s, '^(?:[\\w\\-\\.]+@.*)$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00984() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('2,3,4,5,6,7,8,9,A,B,C,D,E,F,G,H,I,P,Q,R,S,T,U,V,W,X,Y,`,a,b,c,d,e,f,g,h,i,p,q,r,s,t,u,v,w,x,y,Ā,ā,Ă,ă,Ą,ą,Ć,ć,Ĉ,ĉ,Đ,đ,Ē,ē,Ĕ,ĕ,Ė,ė,Ę,ę,Ġ,ġ,Ģ,ģ,Ĥ,ĥ,Ħ,ħ,Ĩ,ĩ,İ,ı,Ĳ,ĳ,Ĵ,ĵ,Ķ,ķ,ĸ,Ĺ,ŀ,Ł,ł,Ń,ń,Ņ,ņ,Ň,ň,ŉ,Ő,ő,Œ,œ,Ŕ,ŕ,Ŗ,ŗ,Ř,ř,Š,š,Ţ,ţ,Ť,ť,Ŧ,ŧ,Ũ,ũ,Ű,ű,Ų,ų,Ŵ,ŵ,Ŷ,ŷ,Ÿ,Ź,ƀ,Ɓ,Ƃ,ƃ,Ƅ,ƅ,Ɔ,Ƈ,ƈ,Ɖ,Ɛ,Ƒ,ƒ,Ɠ,Ɣ,ƕ,Ɩ,Ɨ,Ƙ,ƙ,Ȁ,ȁ,Ȃ,ȃ,Ȅ,ȅ,Ȇ,ȇ,Ȉ,ȉ,Ȑ,ȑ,Ȓ,ȓ,Ȕ,ȕ,Ȗ,ȗ,Ș,ș,Ƞ,Ȣ,ȣ,Ȥ,ȥ,Ȧ,ȧ,Ȩ,ȩ,Ȱ,ȱ,Ȳ,ȳ,ɐ,ɑ,ɒ,ɓ,ɔ,ɕ,ɖ,ɗ,ɘ,ə,ɠ,ɡ,ɢ,ɣ,ɤ,ɥ,ɦ,ɧ,ɨ,ɩ,ɰ,ɱ,ɲ,ɳ,ɴ,ɵ,ɶ,ɷ,ɸ,ɹ,ʀ,ʁ,ʂ,ʃ,ʄ,ʅ,ʆ,ʇ,ʈ,ʉ,ʐ,ʑ,ʒ,ʓ,ʔ,ʕ,ʖ,ʗ,ʘ,ʙ,̀,́,̂,̃,̄,̅,̆,̇,̈,̉,̐,̑,̒,̓,̔,̕,̖,̗,̘,̙,̠,̡,̢,̣,̤,̥,̦,̧,̨,̩,̰,̱,̲,̳,̴,̵,̶,̷,̸,̹,̀,́,͂,̓,̈́,ͅ,͆,͇,͈,͉,͠,͡,͢,ͣ,ͤ,ͥ,ͦ,ͧ,ͨ,ͩ,ʹ,͵,΄,΅,Ά,Έ,Ή,ΐ,Α,Β,Γ,Δ,Ε,Ζ,Η,Θ,Ι,Ѐ,Ё,Ђ,Ѓ,Є,Ѕ,І,Ї,Ј,Љ,А,Б,В,Г,Д,Е,Ж,З,И,Й,Р,С,Т,У,Ф,Х,Ц,Ч,Ш,Щ,а,б,в,г,д,е,ж,з,и,й,р,с,т,у,ф,х,ц,ч,ш,щ,ѐ,ё,ђ,ѓ,є,ѕ,і,ї,ј,љ,Ѡ,ѡ,Ѣ,ѣ,Ѥ,ѥ,Ѧ,ѧ,Ѩ,ѩ,Ѱ,ѱ,Ѳ,ѳ,Ѵ,ѵ,Ѷ,ѷ,Ѹ,ѹ,Ҁ,ҁ,҂,҃,҄,҅,҆,҈,҉,Ґ,ґ,Ғ,ғ,Ҕ,ҕ,Җ,җ,Ҙ,ҙ,Ԁ,ԁ,Ԃ,ԃ,Ԅ,ԅ,Ԇ,ԇ,Ԉ,ԉ,Ա,Բ,Գ,Դ,Ե,Զ,Է,Ը,Թ,Հ,Ձ,Ղ,Ճ,Մ,Յ,Ն,Շ,Ո,Չ,Ր,Ց,Ւ,Փ,Ք,Օ,Ֆ,ՙ,ա,բ,գ,դ,ե,զ,է,ը,թ,հ,ձ,ղ,ճ,մ,յ,ն,շ,ո,չ,ր,ց,ւ,փ,ք,օ,ֆ,և,֑,֒,֓,֔,֕,֖,֗,֘,֙,ء,آ,أ,ؤ,إ,ئ,ا,ب,ة,ذ,ر,ز,س,ش,ص,ض,ط,ظ,ع,ـ,ف,ق,ك,ل,م,ن,ه,و,ى,ِ,ّ,ْ,ٓ,ٔ,ٕ,٠,١,٢,٣,٤,٥,٦,٧,٨,٩,ٰ,ٱ,ٲ,ٳ,ٴ,ٵ,ٶ,ٷ,ٸ,ٹ,ڀ,ځ,ڂ,ڃ,ڄ,څ,چ,ڇ,ڈ,ډ,ڐ,ڑ,ڒ,ړ,ڔ,ڕ,ږ,ڗ,ژ,ڙ,ܐ,ܑ,ܒ,ܓ,ܔ,ܕ,ܖ,ܗ,ܘ,ܙ,ܠ,ܡ,ܢ,ܣ,ܤ,ܥ,ܦ,ܧ,ܨ,ܩ,ܰ,ܱ,ܲ,ܳ,ܴ,ܵ,ܶ'||\n" +
      "      ',ܷ,ܸ,ܹ,݀,݁,݂,݃,݄,݅,݆,݇,݈,݉,ހ,ށ,ނ,ރ,ބ,ޅ,ކ,އ,ވ,މ,ސ,ޑ,ޒ,ޓ,ޔ,ޕ,ޖ,ޗ,ޘ,ޙ,ँ,ं,ः,अ,आ,इ,ई,उ,ऐ,ऑ,ऒ,ओ,औ,क,ख,ग,घ,ङ,ठ,ड,ढ,ण,त,थ,द,ध,न,ऩ,र,ऱ,ल,ळ,ऴ,व,श,ष,स,ह,ी,ु,ू,ृ,ॄ,ॅ,ॆ,े,ै,ॉ,ॐ,॑,॒,॓,॔,क़,ख़,ॠ,ॡ,ॢ,ॣ,०,१,२,३,ঁ,ং,ঃ,অ,আ,ই,ঈ,উ,ঐ,ও,ঔ,ক,খ,গ,ঘ,ঙ,က,ခ,ဂ,ဃ,င,စ,ဆ,ဇ,ဈ,ဉ,တ,ထ,ဒ,ဓ,န,ပ,ဖ,ဗ,ဘ,မ,ဠ,အ,ဣ,ဤ,ဥ,ဦ,ဧ,ဩ,ူ,ေ,ဲ,ံ,့,း,္,၀,၁,၂,၃,၄,၅,၆,၇,၈,၉,ၐ,ၑ,ၒ,ၓ,ၔ,ၕ,ၖ,ၗ,ၘ,ၙ,ᄀ,ᄁ,ᄂ,ᄃ,ᄄ,ᄅ,ᄆ,ᄇ,ᄈ,ᄉ,ᄐ,ᄑ,ᄒ,ᄓ,ᄔ,ᄕ,ᄖ,ᄗ,ᄘ,ᄙ,ᄠ,ᄡ,ᄢ,ᄣ,ᄤ,ᄥ,ᄦ,ᄧ,ᄨ,ᄩ,ᄰ,ᄱ,ᄲ,ᄳ,ᄴ,ᄵ,ᄶ,ᄷ,ᄸ,ᄹ,ᅀ,ᅁ,ᅂ,ᅃ,ᅄ,ᅅ,ᅆ,ᅇ,ᅈ,ᅉ,ᅐ,ᅑ,ᅒ,ᅓ,ᅔ,ᅕ,ᅖ,ᅗ,ᅘ,ᅙ,ᅠ,ᅡ,ᅢ,ᅣ,ᅤ,ᅥ,ᅦ,ᅧ,ᅨ,ᅩ,ᅰ,ᅱ,ᅲ,ᅳ,ᅴ,ᅵ,ᅶ,ᅷ,ᅸ,ᅹ,ᆀ,ᆁ,ᆂ,ᆃ,ᆄ,ᆅ,ᆆ,ᆇ,ᆈ,ᆉ,ᆐ,ᆑ,ᆒ,ᆓ,ᆔ,ᆕ,ᆖ,ᆗ,ᆘ,ᆙ,ሀ,ሁ,ሂ,ሃ,ሄ,ህ,ሆ,ለ,ሉ,ሐ,ሑ,ሒ,ሓ,ሔ,ሕ,ሖ,ሗ,መ,ሙ,ሠ,ሡ,ሢ,ሣ,ሤ,ሥ,ሦ,ሧ,ረ,ሩ,ሰ,ሱ,ሲ,ሳ,ሴ,ስ,ሶ,ሷ,ሸ,ሹ,ቀ,ቁ,ቂ,ቃ,ቄ,ቅ,ቆ,ቈ,ቐ,ቑ,ቒ,ቓ,ቔ,ቕ,ቖ,ቘ,በ,ቡ,ቢ,ባ,ቤ,ብ,ቦ,ቧ,ቨ,ቩ,ተ,ቱ,ቲ,ታ,ቴ,ት,ቶ,ቷ,ቸ,ቹ,ኀ,ኁ,ኂ,ኃ,ኄ,ኅ,ኆ,ኈ,ነ,ኑ,ኒ,ና,ኔ,ን,ኖ,ኗ,ኘ,ኙ,ጀ,ጁ,ጂ,ጃ,ጄ,ጅ,ጆ,ጇ,ገ,ጉ,ጐ,ጒ,ጓ,ጔ,ጕ,ጘ,ጙ,ጠ,ጡ,ጢ,ጣ,ጤ,ጥ,ጦ,ጧ,ጨ,ጩ,ጰ,ጱ,ጲ,ጳ,ጴ,ጵ,ጶ,ጷ,ጸ,ጹ,ፀ,ፁ,ፂ,ፃ,ፄ,ፅ,ፆ,ፈ,ፉ,ፐ,ፑ,ፒ,ፓ,ፔ,ፕ,ፖ,ፗ,ፘ,ፙ,፩,፰,፱,፲,፳,፴,፵,፶,፷,፸,፹,ᐁ,ᐂ,ᐃ,ᐄ,ᐅ,ᐆ,ᐇ,ᐈ,ᐉ,ᐐ,ᐑ,ᐒ,ᐓ,ᐔ,ᐕ,ᐖ,ᐗ,ᐘ,ᐙ,ᐠ,ᐡ,ᐢ,ᐣ,ᐤ,ᐥ,ᐦ,ᐧ,ᐨ,ᐩ,ᐰ,ᐱ,ᐲ,ᐳ,ᐴ,ᐵ,ᐶ,ᐷ,ᐸ,ᐹ,ᑀ,ᑁ,ᑂ,ᑃ,ᑄ,ᑅ,ᑆ,ᑇ,ᑈ,ᑉ,ᑐ,ᑑ,ᑒ,ᑓ,ᑔ,ᑕ,ᑖ,ᑗ,ᑘ,ᑙ,ᑠ,ᑡ,ᑢ,ᑣ,ᑤ,ᑥ,ᑦ,ᑧ,ᑨ,ᑩ,ᑰ,ᑱ,ᑲ,ᑳ,ᑴ,ᑵ,ᑶ,ᑷ,ᑸ,ᑹ,ᒀ,ᒁ,ᒂ,ᒃ,ᒄ,ᒅ,ᒆ,ᒇ,ᒈ,ᒉ,ᒐ'||\n" +
      "      ',ᒑ,ᒒ,ᒓ,ᒔ,ᒕ,ᒖ,ᒗ,ᒘ,ᒙ,ᔀ,ᔁ,ᔂ,ᔃ,ᔄ,ᔅ,ᔆ,ᔇ,ᔈ,ᔉ,ᔐ,ᔑ,ᔒ,ᔓ,ᔔ,ᔕ,ᔖ,ᔗ,ᔘ,ᔙ,ᔠ,ᔡ,ᔢ,ᔣ,ᔤ,ᔥ,ᔦ,ᔧ,ᔨ,ᔩ,ᔰ,ᔱ,ᔲ,ᔳ,ᔴ,ᔵ,ᔶ,ᔷ,ᔸ,ᔹ,ᕀ,ᕁ,ᕂ,ᕃ,ᕄ,ᕅ,ᕆ,ᕇ,ᕈ,ᕉ,ᕐ,ᕑ,ᕒ,ᕓ,ᕔ,ᕕ,ᕖ,ᕗ,ᕘ,ᕙ,ᕠ,ᕡ,ᕢ,ᕣ,ᕤ,ᕥ,ᕦ,ᕧ,ᕨ,ᕩ,ᕰ,ᕱ,ᕲ,ᕳ,ᕴ,ᕵ,ᕶ,ᕷ,ᕸ,ᕹ,ᖀ,ᖁ,ᖂ,ᖃ,ᖄ,ᖅ,ᖆ,ᖇ,ᖈ,ᖉ,ᖐ,ᖑ,ᖒ,ᖓ,ᖔ,ᖕ,ᖖ,ᖗ,ᖘ,ᖙ,ᘀ,ᘁ,ᘂ,ᘃ,ᘄ,ᘅ,ᘆ,ᘇ,ᘈ,ᘉ,ᘐ,ᘑ,ᘒ,ᘓ,ᘔ,ᘕ,ᘖ,ᘗ,ᘘ,ᘙ,ᘠ,ᘡ,ᘢ,ᘣ,ᘤ,ᘥ,ᘦ,ᘧ,ᘨ,ᘩ,ᘰ,ᘱ,ᘲ,ᘳ,ᘴ,ᘵ,ᘶ,ᘷ,ᘸ,ᘹ,ᙀ,ᙁ,ᙂ,ᙃ,ᙄ,ᙅ,ᙆ,ᙇ,ᙈ,ᙉ,ᙐ,ᙑ,ᙒ,ᙓ,ᙔ,ᙕ,ᙖ,ᙗ,ᙘ,ᙙ,ᙠ,ᙡ,ᙢ,ᙣ,ᙤ,ᙥ,ᙦ,ᙧ,ᙨ,ᙩ,ᙰ,ᙱ,ᙲ,ᙳ,ᙴ,ᙵ,ᙶ,ᚁ,ᚂ,ᚃ,ᚄ,ᚅ,ᚆ,ᚇ,ᚈ,ᚉ,ᚐ,ᚑ,ᚒ,ᚓ,ᚔ,ᚕ,ᚖ,ᚗ,ᚘ,ᚙ,ᜀ,ᜁ,ᜂ,ᜃ,ᜄ,ᜅ,ᜆ,ᜇ,ᜈ,ᜉ,ᜐ,ᜑ,ᜒ,ᜓ,᜔,ᜠ,ᜡ,ᜢ,ᜣ,ᜤ,ᜥ,ᜦ,ᜧ,ᜨ,ᜩ,ᜰ,ᜱ,ᜲ,ᜳ,᜴,ᝀ,ᝁ,ᝂ,ᝃ,ᝄ,ᝅ,ᝆ,ᝇ,ᝈ,ᝉ,ᝐ,ᝑ,ᝒ,ᝓ,ᝠ,ᝡ,ᝢ,ᝣ,ᝤ,ᝥ,ᝦ,ᝧ,ᝨ,ᝩ,ᝰ,ᝲ,ᝳ,ក,ខ,គ,ឃ,ង,ច,ឆ,ជ,ឈ,ញ,ថ,ទ,ធ,ន,ប,ផ,ព,ភ,ម,យ,᠐,᠑,᠒,᠓,᠔,᠕,᠖,᠗,᠘,᠙,ᠠ,ᠡ,ᠢ,ᠣ,ᠤ,ᠥ,ᠦ,ᠧ,ᠨ,ᠩ,ᠰ,ᠱ,ᠲ,ᠳ,ᠴ,ᠵ,ᠶ,ᠷ,ᠸ,ᠹ,ᡀ,ᡁ,ᡂ,ᡃ,ᡄ,ᡅ,ᡆ,ᡇ,ᡈ,ᡉ,ᡐ,ᡑ,ᡒ,ᡓ,ᡔ,ᡕ,ᡖ,ᡗ,ᡘ,ᡙ,ᡠ,ᡡ,ᡢ,ᡣ,ᡤ,ᡥ,ᡦ,ᡧ,ᡨ,ᡩ,ᡰ,ᡱ,ᡲ,ᡳ,ᡴ,ᡵ,ᡶ,ᡷ,ᢀ,ᢁ,ᢂ,ᢃ,ᢄ,ᢅ,ᢆ,ᢇ,ᢈ,ᢉ,ᢐ,ᢑ,ᢒ,ᢓ,ᢔ,ᢕ,ᢖ,ᢗ,ᢘ,ᢙ,⁄,⁒,⁰,ⁱ,⁴,⁵,⁶,⁷,⁸,⁹,₀,₁,₂,₃,₄,₅,₆,₇,₈,₉,℀,℁,ℂ,℃,℄,℅,℆,ℇ,℈,℉,ℐ,ℑ,ℒ,ℓ,℔,ℕ,№,℗,℘,ℙ,℠,℡,™,℣,ℤ,℥,Ω,℧,ℨ,℩,ℰ,ℱ,Ⅎ,ℳ,ℴ,ℵ,ℶ,ℷ,ℸ,ℹ,⅀,⅁,⅂,⅃,⅄,ⅅ,ⅆ,ⅇ,ⅈ,ⅉ,⅓,⅔,⅕,⅖,⅗,⅘,⅙,Ⅰ,Ⅱ,Ⅲ,Ⅳ,Ⅴ,Ⅵ,Ⅶ,Ⅷ,Ⅸ,Ⅹ,ⅰ,ⅱ,ⅲ,ⅳ,ⅴ,ⅵ,ⅶ,ⅷ,ⅸ,ⅹ,ↀ,ↁ,ↂ,Ↄ,←,↑,→,↓,↔,↕,↖,↗,↘,↙,∀,∁,∂,∃,∄,∅,∆,∇,∈,∉,∐,∑,−,∓,∔,∕,∖,∗,∘,∙,∠,∡,∢,∣,∤,∥,∦,∧,∨,∩,∰,∱,∲,∳,∴,∵'||\n" +
      "      ',∶,∷,∸,∹,≀,≁,≂,≃,≄,≅,≆,≇,≈,≉,≐,≑,≒,≓,≔,≕,≖,≗,≘,≙,≠,≡,≢,≣,≤,≥,≦,≧,≨,≩,≰,≱,≲,≳,≴,≵,≶,≷,≸,≹,⊀,⊁,⊂,⊃,⊄,⊅,⊆,⊇,⊈,⊉,⊐,⊑,⊒,⊓,⊔,⊕,⊖,⊗,⊘,⊙,⌀,⌁,⌂,⌃,⌄,⌅,⌆,⌇,⌈,⌉,⌐,⌑,⌒,⌓,⌔,⌕,⌖,⌗,⌘,⌙,⌠,⌡,⌢,⌣,⌤,⌥,⌦,⌧,⌨,⌰,⌱,⌲,⌳,⌴,⌵,⌶,⌷,⌸,⌹,⍀,⍁,⍂,⍃,⍄,⍅,⍆,⍇,⍈,⍉,⍐,⍑,⍒,⍓,⍔,⍕,⍖,⍗,⍘,⍙,⍠,⍡,⍢,⍣,⍤,⍥,⍦,⍧,⍨,⍩,⍰,⍱,⍲,⍳,⍴,⍵,⍶,⍷,⍸,⍹,⎀,⎁,⎂,⎃,⎄,⎅,⎆,⎇,⎈,⎉,⎐,⎑,⎒,⎓,⎔,⎕,⎖,⎗,⎘,⎙,␀,␁,␂,␃,␄,␅,␆,␇,␈,␉,␐,␑,␒,␓,␔,␕,␖,␗,␘,␙,␠,␡,␢,␣,␤,␥,␦,⑀,⑁,⑂,⑃,⑄,⑅,⑆,⑇,⑈,⑉,①,②,③,④,⑤,⑥,⑦,⑧,⑨,⑩,⑰,⑱,⑲,⑳,⑴,⑵,⑶,⑷,⑸,⑹,⒀,⒁,⒂,⒃,⒄,⒅,⒆,⒇,⒈,⒉,⒐,⒑,⒒,⒓,⒔,⒕,⒖,⒗,⒘,⒙,─,━,│,┃,┄,┅,┆,┇,┈,┉,┐,┑,┒,┓,└,┕,┖,┗,┘,┙,┠,┡,┢,┣,┤,┥,┦,┧,┨,┩,┰,┱,┲,┳,┴,┵,┶,┷,┸,┹,╀,╁,╂,╃,╄,╅,╆,╇,╈,╉,═,║,╒,╓,╔,╕,╖,╗,╘,╙,╠,╡,╢,╣,╤,╥,╦,╧,╨,╩,╰,╱,╲,╳,╴,╵,╶,╷,╸,╹,▀,▁,▂,▃,▄,▅,▆,▇,█,▉,▐,░,▒,▓,▔,▕,▖,▗,▘,▙,☀,☁,☂,☃,☄,★,☆,☇,☈,☉,☐,☑,☒,☓,☖,☗,☙,☠,☡,☢,☣,☤,☥,☦,☧,☨,☩,☰,☱,☲,☳,☴,☵,☶,☷,☸,☹,♀,♁,♂,♃,♄,♅,♆,♇,♈,♉,♐,♑,♒,♓,♔,♕,♖,♗,♘,♙,♠,♡,♢,♣,♤,♥,♦,♧,♨,♩,♰,♱,♲,♳,♴,♵,♶,♷,♸,♹,⚀,⚁,⚂,⚃,⚄,⚅,⚆,⚇,⚈,⚉,✁,✂,✃,✄,✆,✇,✈,✉,✐,✑,✒,✓,✔,✕,✖,✗,✘,✙,✠,✡,✢,✣,✤,✥,✦,✧,✩,✰,✱,✲,✳,✴,✵,✶,✷,✸,✹,❀,❁,❂,❃,❄,❅,❆,❇,❈,❉,❐,❑,❒,❖,❘,❙,❡,❢,❣,❤,❥,❦,❧,❶,❷,❸,❹,➀,➁,➂,➃,➄,➅,➆,➇,➈,➉,➐,➑,➒,➓,➔,➘,➙,⠀,⠁,⠂,⠃,⠄,⠅,⠆,⠇,⠈,⠉,⠐,⠑,⠒,⠓,⠔,⠕,⠖,⠗,⠘,⠙,⠠,⠡,⠢,⠣,⠤,⠥,⠦,⠧,⠨,⠩,⠰,⠱,⠲,⠳,⠴,⠵,⠶,⠷,⠸,⠹,⡀,⡁,⡂,⡃,⡄,⡅,⡆,⡇,⡈,⡉,⡐,⡑,⡒,⡓,⡔,⡕,⡖,⡗,⡘,⡙,⡠,⡡,⡢,⡣,⡤,⡥,⡦,⡧,⡨,⡩,⡰,⡱,⡲,⡳,⡴,⡵,⡶,⡷,⡸,⡹,⢀,⢁,⢂,⢃,⢄,⢅,⢆,⢇,⢈,⢉,⢐,⢑,⢒,⢓,⢔,⢕,⢖,⢗,⢘,⢙,⤀,⤁,⤂,⤃,⤄,⤅,⤆,⤇,⤈,⤉,⤐,⤑,⤒,⤓,⤔,⤕,⤖,⤗,⤘,⤙,⤠,⤡,⤢,⤣,⤤,⤥,⤦,⤧,⤨,⤩,⤰,⤱,⤲,⤳,⤴,⤵,⤶,⤷,⤸,⤹,⥀,⥁,⥂,⥃,⥄,⥅,⥆,⥇,⥈,⥉,⥐,⥑,⥒,⥓,⥔,⥕,⥖,⥗,⥘,⥙,⥠,⥡,⥢,⥣,⥤,⥥,⥦,⥧,⥨,⥩,⥰,⥱,⥲,⥳,⥴,⥵,⥶,⥷,⥸,⥹,⦀,⦁,⦂,⦙,〄,々,〆,〇,〒,〓,〠,〡,〢,〣,〤,〥,〦,〧,〨,〩,〱,〲,〳,〴,〵,〶,〷,〸,〹,ぁ,あ,ぃ,い,ぅ,う,ぇ,え,ぉ,ぐ,け,げ,こ,ご,さ,ざ,し,じ,す,だ,ち,ぢ,っ,つ,づ,て,で,と,ど,ば,ぱ,ひ,び,ぴ,ふ,ぶ,ぷ,へ,べ,む,め,も,ゃ,や,ゅ,ゆ,ょ,よ,ら,ゐ,ゑ,を,ん,ゔ,ゕ,ゖ,゙,ㄅ,ㄆ,ㄇ,ㄈ,ㄉ,ㄐ,ㄑ,ㄒ,ㄓ,ㄔ,ㄕ,ㄖ,ㄗ,ㄘ,ㄙ,ㄠ,ㄡ,ㄢ,ㄣ,ㄤ,ㄥ,ㄦ,ㄧ,ㄨ,ㄩ,ㄱ,ㄲ,ㄳ,ㄴ,ㄵ,ㄶ,ㄷ,ㄸ,ㄹ,ㅀ,ㅁ,ㅂ,ㅃ,ㅄ,ㅅ,ㅆ,ㅇ,ㅈ,ㅉ,ㅐ,ㅑ,ㅒ,ㅓ,ㅔ,ㅕ,ㅖ,ㅗ,ㅘ,ㅙ,ㅠ,ㅡ,ㅢ,ㅣ,ㅤ,ㅥ,ㅦ,ㅧ,ㅨ,ㅩ,ㅰ,ㅱ,ㅲ,ㅳ,ㅴ,ㅵ,ㅶ,ㅷ,ㅸ,ㅹ,ㆀ,ㆁ,ㆂ,ㆃ,ㆄ,ㆅ,ㆆ,ㆇ,ㆈ,ㆉ,㆐,㆑,㆒,㆓,㆔,㆕,㆖,㆗,㆘,㆙,㈀,㈁,㈂,㈃,㈄,㈅,㈆,㈇,㈈,㈉,㈐,㈑,㈒,㈓,㈔,㈕,㈖,㈗,㈘,㈙,㈠,㈡,㈢,㈣,㈤,㈥,㈦,㈧,㈨,㈩,㈰,㈱,㈲,㈳,㈴,㈵,㈶,㈷,㈸,㈹,㉀,㉁,㉂,㉃,㉑,㉒,㉓,㉔,㉕,㉖,㉗,㉘,㉙,㉠,㉡,㉢,㉣,㉤,㉥,㉦,㉧,㉨,㉩,㉰,㉱,㉲,㉳,㉴,㉵,㉶,㉷,㉸,㉹,㊀,㊁,㊂,㊃,㊄,㊅,㊆,㊇,㊈,㊉,㊐,㊑,㊒,㊓,㊔,㊕,㊖,㊗,㊘,㊙,㌀,㌁,㌂,㌃,㌄,㌅,㌆,㌇,㌈,㌉,㌐,㌑,㌒,㌓,㌔,㌕,㌖,㌗,㌘,㌙,㌠,㌡,㌢,㌣,㌤,㌥,㌦,㌧,㌨,㌩,㌰,㌱,㌲,㌳,㌴,㌵,㌶,㌷,㌸,㌹,㍀,㍁,㍂,㍃,㍄,㍅,㍆,㍇,㍈,㍉,㍐,㍑,㍒,㍓,㍔,㍕,㍖,㍗,㍘,㍙,㍠,㍡,㍢,㍣,㍤,㍥,㍦,㍧,㍨,㍩,㍰,㍱,㍲,㍳,㍴,㍵,㍶,㎀,㎁,㎂,㎃,㎄,㎅,㎆,㎇,㎈,㎉,㎐,㎑,㎒,㎓,㎔,㎕,㎖,㎗,㎘,㎙,㐀,㐁,㐂,㐃,㐄,㐅,㐆,㐇,㐈,㐉,㐐,㐑,㐒,㐓,㐔,㐕,㐖,㐗,㐘,㐙,㐠,㐡,㐢,㐣,㐤,㐥,㐦,㐧,㐨,㐩,㐰,㐱,㐲,㐳,㐴,㐵,㐶,㐷,㐸,㐹,㑀,㑁,㑂,㑃,㑄,㑅,㑆,㑇,㑈,㑉,㑐,㑑,㑒,㑓,㑔,㑕,㑖,㑗,㑘,㑙,㑠,㑡,㑢,㑣,㑤,㑥,㑦,㑧,㑨,㑩,㑰,㑱,㑲,㑳,㑴,㑵,㑶,㑷,㑸,㑹,㒀,㒁,㒂,㒃,㒄,㒅,㒆,㒇,㒈,㒉,㒐,㒑,㒒,㒓,㒔,㒕,㒖,㒗,㒘,㒙,㔀,㔁,㔂,㔃,㔄,㔅,㔆,㔇,㔈,㔉,㔐,㔑,㔒,㔓,㔔,㔕,㔖,㔗,㔘,㔙,㔠,㔡,㔢,㔣,㔤,㔥,㔦,㔧,㔨,㔩,㔰,㔱,㔲,㔳,㔴,㔵,㔶,㔷,㔸,㔹,㕀,㕁,㕂,㕃,㕄,㕅,㕆,㕇,㕈,㕉,㕐,㕑,㕒,㕓,㕔,㕕,㕖,㕗,㕘,㕙,㕠,㕡,㕢,㕣,㕤,㕥,㕦,㕧,㕨,㕩,㕰,㕱,㕲,㕳,㕴,㕵,㕶,㕷,㕸,㕹,㖀,㖁,㖂,㖃,㖄,㖅,㖆,㖇,㖈,㖉,㖐,㖑,㖒,㖓,㖔,㖕,㖖,㖗,㖘,㖙,㘀,㘁,㘂,㘃,㘄,㘅,㘆,㘇,㘈,㘉,㘐,㘑,㘒,㘓,㘔,㘕,㘖,㘗,㘘,㘙,㘠,㘡,㘢,㘣,㘤,㘥,㘦,㘧,㘨,㘩,㘰,㘱,㘲,㘳,㘴,㘵,㘶,㘷,㘸,㘹,㙀,㙁,㙂,㙃,㙄,㙅,㙆,㙇,㙈,㙉,㙐,㙑,㙒,㙓,㙔,㙕,㙖,㙗,㙘,㙙,㙠,㙡,㙢,㙣,㙤,㙥,㙦,㙧,㙨,㙩,㙰,㙱,㙲,㙳,㙴,㙵,㙶,㙷,㙸,㙹,㚀,㚁,㚂,㚃,㚄,㚅,㚆,㚇,㚈,㚉,㚐,㚑,㚒,㚓,㚔,㚕,㚖,㚗,㚘,㚙,㜀,㜁,㜂,㜃,㜄,㜅,㜆,㜇,㜈,㜉,㜐,㜑,㜒,㜓,㜔,㜕,㜖,㜗,㜘,㜙,㜠,㜡,㜢,㜣,㜤,㜥,㜦,㜧,㜨,㜩,㜰,㜱,㜲,㜳,㜴,㜵,㜶,㜷,㜸,㜹,㝀,㝁,㝂,㝃,㝄,㝅,㝆,㝇,㝈,㝉,㝐,㝑,㝒,㝓,㝔,㝕,㝖,㝗,㝘,㝙,㝠,㝡,㝢,㝣,㝤,㝥,㝦,㝧,㝨,㝩,㝰,㝱,㝲,㝳,㝴,㝵,㝶,㝷,㝸,㝹,㞀,㞁,㞂,㞃,㞄,㞅,㞆,㞇,㞈,㞉,㞐,㞑,㞒,㞓,㞔,㞕,㞖,㞗,㞘,㞙,㠀,㠁,㠂,㠃,㠄,㠅,㠆,㠇,㠈,㠉,㠐,㠑,㠒,㠓,㠔,㠕,㠖,㠗,㠘,㠙,㠠,㠡,㠢,㠣,㠤,㠥,㠦,㠧,㠨,㠩,㠰,㠱,㠲,㠳,㠴,㠵,㠶,㠷,㠸,㠹,㡀,㡁,㡂,㡃,㡄,㡅,㡆,㡇,㡈,㡉,㡐,㡑,㡒,㡓,㡔,㡕,㡖,㡗,㡘,㡙,㡠,㡡,㡢,㡣,㡤,㡥,㡦,㡧,㡨,㡩,㡰,㡱,㡲,㡳,㡴,㡵,㡶,㡷,㡸,㡹,㢀,㢁,㢂,㢃,㢄,㢅,㢆,㢇,㢈,㢉,㢐,㢑,㢒,㢓,㢔,㢕,㢖,㢗,㢘,㢙,㤀,㤁,㤂,㤃,㤄,㤅,㤆,㤇,㤈,㤉,㤐,㤑,㤒,㤓,㤔,㤕,㤖,㤗,㤘,㤙,㤠,㤡,㤢,㤣,㤤,㤥,㤦,㤧,㤨,㤩,㤰,㤱,㤲,㤳,㤴,㤵,㤶,㤷,㤸,㤹,㥀,㥁,㥂,㥃,㥄,㥅,㥆,㥇,㥈,㥉,㥐,㥑,㥒,㥓,㥔,㥕,㥖,㥗,㥘,㥙,㥠,㥡,㥢,㥣,㥤,㥥,㥦,㥧,㥨,㥩,㥰,㥱,㥲,㥳,㥴,㥵,㥶,㥷,㥸,㥹,㦀,㦁,㦂,㦃,㦄,㦅,㦆,㦇,㦈,㦉,㦐,㦑,㦒,㦓,㦔,㦕,㦖,㦗,㦘,㦙,䀀,䀁,䀂,䀃,䀄,䀅,䀆,䀇,䀈,䀉,䀐,䀑,䀒,䀓,䀔,䀕,䀖,䀗,䀘,䀙,䀠,䀡,䀢,䀣,䀤,䀥,䀦,䀧,䀨,䀩,䀰,䀱,䀲,䀳,䀴,䀵,䀶,䀷,䀸,䀹,䁀,䁁,䁂,䁃,䁄,䁅,䁆,䁇,䁈,䁉,䁐,䁑,䁒,䁓,䁔,䁕,䁖,䁗,䁘,䁙,䁠,䁡,䁢,䁣,䁤,䁥,䁦,䁧,䁨,䁩,䁰,䁱,䁲,䁳,䁴,䁵,䁶,䁷,䁸,䁹,䂀,䂁,䂂,䂃,䂄,䂅,䂆,䂇,䂈,䂉,䂐,䂑,䂒,䂓,䂔,䂕,䂖,䂗,䂘,䂙,䄀,䄁,䄂,䄃,䄄,䄅,䄆,䄇,䄈,䄉,䄐,䄑,䄒,䄓,䄔,䄕,䄖,䄗,䄘,䄙,䄠,䄡,䄢,䄣,䄤,䄥,䄦,䄧,䄨,䄩,䄰,䄱,䄲,䄳,䄴,䄵,䄶,䄷,䄸,䄹,䅀,䅁,䅂,䅃,䅄,䅅,䅆,䅇,䅈,䅉,䅐,䅑,䅒,䅓,䅔,䅕,䅖,䅗,䅘,䅙,䅠,䅡,䅢,䅣,䅤,䅥,䅦,䅧,䅨,䅩,䅰,䅱,䅲,䅳,䅴,䅵,䅶,䅷,䅸,䅹,䆀,䆁,䆂,䆃,䆄,䆅,䆆,䆇,䆈,䆉,䆐,䆑,䆒,䆓,䆔,䆕,䆖,䆗,䆘,䆙,䈀,䈁,䈂,䈃,䈄,䈅,䈆,䈇,䈈,䈉,䈐,䈑,䈒,䈓,䈔,䈕,䈖,䈗,䈘,䈙,䈠,䈡,䈢,䈣,䈤,䈥,䈦,䈧,䈨,䈩,䈰,䈱,䈲,䈳,䈴,䈵,䈶,䈷,䈸,䈹,䉀,䉁,䉂,䉃,䉄,䉅,䉆,䉇,䉈,䉉,䉐,䉑,䉒,䉓,䉔,䉕,䉖,䉗,䉘,䉙,䉠,䉡,䉢,䉣,䉤,䉥,䉦,䉧,䉨,䉩,䉰,䉱,䉲,䉳,䉴,䉵,䉶,䉷,䉸,䉹,䊀,䊁,䊂,䊃,䊄,䊅,䊆,䊇,䊈,䊉,䊐,䊑,䊒,䊓,䊔,䊕,䊖,䊗,䊘,䊙,䌀,䌁,䌂,䌃,䌄,䌅,䌆,䌇,䌈,䌉,䌐,䌑,䌒,䌓,䌔,䌕,䌖,䌗,䌘,䌙,䌠,䌡,䌢,䌣,䌤,䌥,䌦,䌧,䌨,䌩,䌰,䌱,䌲,䌳,䌴,䌵,䌶,䌷,䌸,䌹,䍀,䍁,䍂,䍃,䍄,䍅,䍆,䍇,䍈,䍉,䍐,䍑,䍒,䍓,䍔,䍕,䍖,䍗,䍘,䍙,䍠,䍡,䍢,䍣,䍤,䍥,䍦,䍧,䍨,䍩,䍰,䍱,䍲,䍳,䍴,䍵,䍶,䍷,䍸,䍹,䎀,䎁,䎂,䎃,䎄,䎅,䎆,䎇,䎈,䎉,䎐,䎑,䎒,䎓,䎔,䎕,䎖,䎗,䎘,䎙,䐀,䐁,䐂,䐃,䐄,䐅,䐆,䐇,䐈,䐉,䐐,䐑,䐒,䐓,䐔,䐕,䐖,䐗,䐘,䐙,䐠,䐡,䐢,䐣,䐤,䐥,䐦,䐧,䐨,䐩,䐰,䐱,䐲,䐳,䐴,䐵,䐶,䐷,䐸,䐹,䑀,䑁,䑂,䑃,䑄,䑅,䑆,䑇,䑈,䑉,䑐,䑑,䑒,䑓,䑔,䑕,䑖,䑗,䑘,䑙,䑠,䑡,䑢,䑣,䑤,䑥,䑦,䑧,䑨,䑩,䑰,䑱,䑲,䑳,䑴,䑵,䑶,䑷,䑸,䑹,䒀,䒁,䒂,䒃,䒄,䒅,䒆,䒇,䒈,䒉,䒐,䒑,䒒,䒓,䒔,䒕,䒖,䒗,䒘,䒙,䔀,䔁,䔂,䔃,䔄,䔅,䔆,䔇,䔈,䔉,䔐,䔑,䔒,䔓,䔔,䔕,䔖,䔗,䔘,䔙,䔠,䔡,䔢,䔣,䔤,䔥,䔦,䔧,䔨,䔩,䔰,䔱,䔲,䔳,䔴,䔵,䔶,䔷,䔸,䔹,䕀,䕁,䕂,䕃,䕄,䕅,䕆,䕇,䕈,䕉,䕐,䕑,䕒,䕓,䕔,䕕,䕖,䕗,䕘,䕙,䕠,䕡,䕢,䕣,䕤,䕥,䕦,䕧,䕨,䕩,䕰,䕱,䕲,䕳,䕴,䕵,䕶,䕷,䕸,䕹,䖀,䖁,䖂,䖃,䖄,䖅,䖆,䖇,䖈,䖉,䖐,䖑,䖒,䖓,䖔,䖕,䖖,䖗,䖘,䖙,䘀,䘁,䘂,䘃,䘄,䘅,䘆,䘇,䘈,䘉,䘐,䘑,䘒,䘓,䘔,䘕,䘖,䘗,䘘,䘙,䘠,䘡,䘢,䘣,䘤,䘥,䘦,䘧,䘨,䘩,䘰,䘱,䘲,䘳,䘴,䘵,䘶,䘷,䘸,䘹,䙀,䙁,䙂,䙃,䙄,䙅,䙆,䙇,䙈,䙉,䙐,䙑,䙒,䙓,䙔,䙕,䙖,䙗,䙘,䙙,䙠,䙡,䙢,䙣,䙤,䙥,䙦,䙧,䙨,䙩,䙰,䙱,䙲,䙳,䙴,䙵,䙶,䙷,䙸,䙹,䚀,䚁,䚂,䚃,䚄,䚅,䚆,䚇,䚈,䚉,䚐,䚑,䚒,䚓,䚔,䚕,䚖,䚗,䚘,䚙,䜀,䜁,䜂,䜃,䜄,䜅,䜆,䜇,䜈,䜉,䜐,䜑,䜒,䜓,䜔,䜕,䜖,䜗,䜘,䜙,䜠,䜡,䜢,䜣,䜤,䜥,䜦,䜧,䜨,䜩,䜰,䜱,䜲,䜳,䜴,䜵,䜶,䜷,䜸,䜹,䝀,䝁,䝂,䝃,䝄,䝅,䝆,䝇,䝈,䝉,䝐,䝑,䝒,䝓,䝔,䝕,䝖,䝗,䝘,䝙,䝠,䝡,䝢,䝣,䝤,䝥,䝦,䝧,䝨,䝩,䝰,䝱,䝲,䝳,䝴,䝵,䝶,䝷,䝸,䝹,䞀,䞁,䞂,䞃,䞄,䞅,䞆,䞇,䞈,䞉,䞐,䞑,䞒,䞓,䞔,䞕,䞖,䞗,䞘,䞙,䠀,䠁,䠂,䠃,䠄,䠅,䠆,䠇,䠈,䠉,䠐,䠑,䠒,䠓,䠔,䠕,䠖,䠗,䠘,䠙,䠠,䠡,䠢,䠣,䠤,䠥,䠦,䠧,䠨,䠩,䠰,䠱,䠲,䠳,䠴,䠵,䠶,䠷,䠸,䠹,䡀,䡁,䡂,䡃,䡄,䡅,䡆,䡇,䡈,䡉,䡐,䡑,䡒,䡓,䡔,䡕,䡖,䡗,䡘,䡙,䡠,䡡,䡢,䡣,䡤,䡥,䡦,䡧,䡨,䡩,䡰,䡱,䡲,䡳,䡴,䡵,䡶,䡷,䡸,䡹,䢀,䢁,䢂,䢃,䢄,䢅,䢆,䢇,䢈,䢉,䢐,䢑,䢒,䢓,䢔,䢕,䢖,䢗,䢘,䢙,䤀,䤁,䤂,䤃,䤄,䤅,䤆,䤇,䤈,䤉,䤐,䤑,䤒,䤓,䤔,䤕,䤖,䤗,䤘,䤙,䤠,䤡,䤢,䤣,䤤,䤥,䤦,䤧,䤨,䤩,䤰,䤱,䤲,䤳,䤴,䤵,䤶,䤷,䤸,䤹,䥀,䥁,䥂,䥃,䥄,䥅,䥆,䥇,䥈,䥉,䥐,䥑,䥒,䥓,䥔,䥕,䥖,䥗,䥘,䥙,䥠,䥡,䥢,䥣,䥤,䥥,䥦,䥧,䥨,䥩,䥰,䥱,䥲,䥳,䥴,䥵,䥶,䥷,䥸,䥹,䦀,䦁,䦂,䦃,䦄,䦅,䦆,䦇,䦈,䦉,䦐,䦑,䦒,䦓,䦔,䦕,䦖,䦗,䦘,䦙,倀,倁,倂,倃,倄,倅,倆,倇,倈,倉,倐,們,倒,倓,倔,倕,倖,倗,倘,候,倠,倡,倢,倣,値,倥,倦,倧,倨,倩,倰,倱,倲,倳,倴,倵,倶,倷,倸,倹,偀,偁,偂,偃,偄,偅,偆,假,偈,偉,偐,偑,偒,偓,偔,偕,偖,偗,偘,偙,偠,偡,偢,偣,偤,健,偦,偧,偨,偩,偰,偱,偲,偳,側,偵,偶,偷,偸,偹,傀,傁,傂,傃,傄,傅,傆,傇,傈,傉,傐,傑,傒,傓,傔,傕,傖,傗,傘,備,儀,儁,儂,儃,億,儅,儆,儇,儈,儉,儐,儑,儒,儓,儔,儕,儖,儗,儘,儙,儠,儡,儢,儣,儤,儥,儦,儧,儨,儩,儰,儱,儲,儳,儴,儵,儶,儷,儸,儹,兀,允,兂,元,兄,充,兆,兇,先,光,児,兑,兒,兓,兔,兕,兖,兗,兘,兙,兠,兡,兢,兣,兤,入,兦,內,全,兩,兰,共,兲,关,兴,兵,其,具,典,兹,冀,冁,冂,冃,冄,内,円,冇,冈,冉,冐,冑,冒,冓,冔,冕,冖,冗,冘,写,刀,刁,刂,刃,刄,刅,分,切,刈,刉,刐,刑,划,刓,刔,刕,刖,列,刘,则,删,刡,刢,刣,判,別,刦,刧,刨,利,到,刱,刲,刳,刴,刵,制,刷,券,刹,剀,剁,剂,剃,剄,剅,剆,則,剈,剉,剐,剑,剒,剓,剔,剕,剖,剗,剘,剙,剠,剡,剢,剣,剤,剥,剦,剧,剨,剩,剰,剱,割,剳,剴,創,剶,剷,剸,剹,劀,劁,劂,劃,劄,劅,劆,劇,劈,劉,劐,劑,劒,劓,劔,劕,劖,劗,劘,劙,匀,匁,匂,匃,匄,包,匆,匇,匈,匉,匐,匑,匒,匓,匔,匕,化,北,匘,匙,匠,匡,匢,匣,匤,匥,匦,匧,匨,匩,匰,匱,匲,匳,匴,匵,匶,匷,匸,匹,區,十,卂,千,卄,卅,卆,升,午,卉,卐,卑,卒,卓,協,单,卖,南,単,卙,占,卡,卢,卣,卤,卥,卦,卧,卨,卩,印,危,卲,即,却,卵,卶,卷,卸,卹,厀,厁,厂,厃,厄,厅,历,厇,厈,厉,厐,厑,厒,厓,厔,厕,厖,厗,厘,厙,吀,吁,吂,吃,各,吅,吆,吇,合,吉,吐,向,吒,吓,吔,吕,吖,吗,吘,吙,吠,吡,吢,吣,吤,吥,否,吧,吨,吩,吰,吱,吲,吳,吴,吵,吶,吷,吸,吹,呀,呁,呂,呃,呄,呅,呆,呇,呈,呉,呐,呑,呒,呓,呔,呕,呖,呗,员,呙,呠,呡,呢,呣,呤,呥,呦,呧,周,呩,呰,呱,呲,味,呴,呵,呶,呷,呸,呹,咀,咁,咂,咃,咄,咅,咆,咇,咈,咉,咐,咑,咒,咓,咔,咕,咖,咗,咘,咙,唀,唁,唂,唃,唄,唅,唆,唇,唈,唉,唐,唑,唒,唓,唔,唕,唖,唗,唘,唙,唠,唡,唢,唣,唤,唥,唦,唧,唨,唩,唰,唱,唲,唳,唴,唵,唶,唷,唸,唹,啀,啁,啂,啃,啄,啅,商,啇,啈,啉,啐,啑,啒,啓,啔,啕,啖,啗,啘,啙,啠,啡,啢,啣,啤,啥,啦,啧,啨,啩,啰,啱,啲,啳,啴,啵,啶,啷,啸,啹,喀,喁,喂,喃,善,喅,喆,喇,喈,喉,喐,喑,喒,喓,喔,喕,喖,喗,喘,喙,嘀,嘁,嘂,嘃,嘄,嘅,嘆,嘇,嘈,嘉,嘐,嘑,嘒,嘓,嘔,嘕,嘖,嘗,嘘,嘙,嘠,嘡,嘢,嘣,嘤,嘥,嘦,嘧,嘨,嘩,嘰,嘱,嘲,嘳,嘴,嘵,嘶,嘷,嘸,嘹,噀,噁,噂,噃,噄,噅,噆,噇,噈,噉,噐,噑,噒,噓,噔,噕,噖,噗,噘,噙,噠,噡,噢,噣,噤,噥,噦,噧,器,噩,噰,噱,噲,噳,噴,噵,噶,噷,噸,噹,嚀,嚁,嚂,嚃,嚄,嚅,嚆,嚇,嚈,嚉,嚐,嚑,嚒,嚓,嚔,嚕,嚖,嚗,嚘,嚙,圀,圁,圂,圃,圄,圅,圆,圇,圈,圉,圐,圑,園,圓,圔,圕,圖,圗,團,圙,圠,圡,圢,圣,圤,圥,圦,圧,在,圩,地,圱,圲,圳,圴,圵,圶,圷,圸,圹,址,坁,坂,坃,坄,坅,坆,均,坈,坉,坐,坑,坒,坓,坔,坕,坖,块,坘,坙,坠,坡,坢,坣,坤,坥,坦,坧,坨,坩,坰,坱,坲,坳,坴,坵,坶,坷,坸,坹,垀,垁,垂,垃,垄,垅,垆,垇,垈,垉,垐,垑,垒,垓,垔,垕,垖,垗,垘,垙,堀,堁,堂,堃,堄,堅,堆,堇,堈,堉,堐,堑,堒,堓,堔,堕,堖,堗,堘,堙,堠,堡,堢,堣,堤,堥,堦,堧,堨,堩,堰,報,堲,堳,場,堵,堶,堷,堸,堹,塀,塁,塂,塃,塄,塅,塆,塇,塈,塉,塐,塑,塒,塓,塔,塕,塖,塗,塘,塙,塠,塡,塢,塣,塤,塥,塦,塧,塨,塩,塰,塱,塲,塳,塴,塵,塶,塷,塸,塹,墀,墁,墂,境,墄,墅,墆,墇,墈,墉,墐,墑,墒,墓,墔,墕,墖,増,墘,墙,夀,夁,夂,夃,处,夅,夆,备,夈,変,夐,夑,夒,夓,夔,夕,外,夗,夘,夙,夠,夡,夢,夣,夤,夥,夦,大,夨,天,夰,失,夲,夳,头,夵,夶,夷,夸,夹,奀,奁,奂,奃,奄,奅,奆,奇,奈,奉,奐,契,奒,奓,奔,奕,奖,套,奘,奙,奠,奡,奢,奣,奤,奥,奦,奧,奨,奩,奰,奱,奲,女,奴,奵,奶,奷,奸,她,妀,妁,如,妃,妄,妅,妆,妇,妈,妉,妐,妑,妒,妓,妔,妕,妖,妗,妘,妙,怀,态,怂,怃,怄,怅,怆,怇,怈,怉,怐,怑,怒,怓,怔,怕,怖,怗,怘,怙,怠,怡,怢,怣,怤,急,怦,性,怨,怩,怰,怱,怲,怳,怴,怵,怶,怷,怸,怹,恀,恁,恂,恃,恄,恅,恆,恇,恈,恉,恐,恑,恒,恓,恔,恕,恖,恗,恘,恙,恠,恡,恢,恣,恤,恥,恦,恧,恨,恩,恰,恱,恲,恳,恴,恵,恶,恷,恸,恹,悀,悁,悂,悃,悄,悅,悆,悇,悈,悉,悐,悑,悒,悓,悔,悕,悖,悗,悘,悙,愀,愁,愂,愃,愄,愅,愆,愇,愈,愉,愐,愑,愒,愓,愔,愕,愖,愗,愘,愙,愠,愡,愢,愣,愤,愥,愦,愧,愨,愩,愰,愱,愲,愳,愴,愵,愶,愷,愸,愹,慀,慁,慂,慃,慄,慅,慆,慇,慈,慉,慐,慑,慒,慓,慔,慕,慖,慗,慘,慙,慠,慡,慢,慣,慤,慥,慦,慧,慨,慩,慰,慱,慲,慳,慴,慵,慶,慷,慸,慹,憀,憁,憂,憃,憄,憅,憆,憇,憈,憉,憐,憑,憒,憓,憔,憕,憖,憗,憘,憙,戀,戁,戂,戃,戄,戅,戆,戇,戈,戉,成,我,戒,戓,戔,戕,或,戗,战,戙,戠,戡,戢,戣,戤,戥,戦,戧,戨,戩,戰,戱,戲,戳,戴,戵,戶,户,戸,戹,所,扁,扂,扃,扄,扅,扆,扇,扈,扉,扐,扑,扒,打,扔,払,扖,扗,托,扙,扠,扡,扢,扣,扤,扥,扦,执,扨,扩,扰,扱,扲,扳,扴,扵,扶,扷,扸,批,技,抁,抂,抃,抄,抅,抆,抇,抈,抉,抐,抑,抒,抓,抔,投,抖,抗,折,抙,挀,持,挂,挃,挄,挅,挆,指,挈,按,挐,挑,挒,挓,挔,挕,挖,挗,挘,挙,挠,挡,挢,挣,挤,挥,挦,挧,挨,挩,挰,挱,挲,挳,挴,挵,挶,挷,挸,挹,捀,捁,捂,捃,捄,捅,捆,捇,捈,捉,捐,捑,捒,捓,捔,捕,捖,捗,捘,捙,捠,捡,换,捣,捤,捥,捦,捧,捨,捩,捰,捱,捲,捳,捴,捵,捶,捷,捸,捹,掀,掁,掂,掃,掄,掅,掆,掇,授,掉,掐,掑,排,掓,掔,掕,掖,掗,掘,掙,搀,搁,搂,搃,搄,搅,搆,搇,搈,搉,搐,搑,搒,搓,搔,搕,搖,搗,搘,搙,搠,搡,搢,搣,搤,搥,搦,搧,搨,搩,搰,搱,搲,搳,搴,搵,搶,搷,搸,搹,摀,摁,摂,摃,摄,摅,摆,摇,摈,摉,摐,摑,摒,摓,摔,摕,摖,摗,摘,摙,摠,摡,摢,摣,摤,摥,摦,摧,摨,摩,摰,摱,摲,摳,摴,摵,摶,摷,摸,摹,撀,撁,撂,撃,撄,撅,撆,撇,撈,撉,撐,撑,撒,撓,撔,撕,撖,撗,撘,撙,攀,攁,攂,攃,攄,攅,攆,攇,攈,攉,攐,攑,攒,攓,攔,攕,攖,攗,攘,攙,攠,攡,攢,攣,攤,攥,攦,攧,攨,攩,攰,攱,攲,攳,攴,攵,收,攷,攸,改,敀,敁,敂,敃,敄,故,敆,敇,效,敉,敐,救,敒,敓,敔,敕,敖,敗,敘,教,敠,敡,敢,散,敤,敥,敦,敧,敨,敩,数,敱,敲,敳,整,敵,敶,敷,數,敹,斀,斁,斂,斃,斄,斅,斆,文,斈,斉,斐,斑,斒,斓,斔,斕,斖,斗,斘,料,昀,昁,昂,昃,昄,昅,昆,昇,昈,昉,昐,昑,昒,易,昔,昕,昖,昗,昘,昙,映,昡,昢,昣,昤,春,昦,昧,昨,昩,昰,昱,昲,昳,昴,昵,昶,昷,昸,昹,晀,晁,時,晃,晄,晅,晆,晇,晈,晉,晐,晑,晒,晓,晔,晕,晖,晗,晘,晙,晠,晡,晢,晣,晤,晥,晦,晧,晨,晩,晰,晱,晲,晳,晴,晵,晶,晷,晸,晹,暀,暁,暂,暃,暄,暅,暆,暇,暈,暉,暐,暑,暒,暓,暔,暕,暖,暗,暘,暙,最,朁,朂,會,朄,朅,朆,朇,月,有,朐,朑,朒,朓,朔,朕,朖,朗,朘,朙,朠,朡,朢,朣,朤,朥,朦,朧,木,朩,朰,朱,朲,朳,朴,朵,朶,朷,朸,朹,杀,杁,杂,权,杄,杅,杆,杇,杈,杉,材,村,杒,杓,杔,杕,杖,杗,杘,杙,杠,条,杢,杣,杤,来,杦,杧,杨,杩,杰,東,杲,杳,杴,杵,杶,杷,杸,杹,枀,极,枂,枃,构,枅,枆,枇,枈,枉,析,枑,枒,枓,枔,枕,枖,林,枘,枙,栀,栁,栂,栃,栄,栅,栆,标,栈,栉,栐,树,栒,栓,栔,栕,栖,栗,栘,栙,栠,校,栢,栣,栤,栥,栦,栧,栨,栩,栰,栱,栲,栳,栴,栵,栶,样,核,根,桀,桁,桂,桃,桄,桅,框,桇,案,桉,桐,桑,桒,桓,桔,桕,桖,桗,桘,桙,桠,桡,桢,档,桤,桥,桦,桧,桨,桩,桰,桱,桲,桳,桴,桵,桶,桷,桸,桹,梀,梁,梂,梃,梄,梅,梆,梇,梈,梉,梐,梑,梒,梓,梔,梕,梖,梗,梘,梙,椀,椁,椂,椃,椄,椅,椆,椇,椈,椉,椐,椑,椒,椓,椔,椕,椖,椗,椘,椙,椠,椡,椢,椣,椤,椥,椦,椧,椨,椩,椰,椱,椲,椳,椴,椵,椶,椷,椸,椹,楀,楁,楂,楃,楄,楅,楆,楇,楈,楉,楐,楑,楒,楓,楔,楕,楖,楗,楘,楙,楠,楡,楢,楣,楤,楥,楦,楧,楨,楩,楰,楱,楲,楳,楴,極,楶,楷,楸,楹,榀,榁,概,榃,榄,榅,榆,榇,榈,榉,榐,榑,榒,榓,榔,榕,榖,榗,榘,榙,瀀,瀁,瀂,瀃,瀄,瀅,瀆,瀇,瀈,瀉,瀐,瀑,瀒,瀓,瀔,瀕,瀖,瀗,瀘,瀙,瀠,瀡,瀢,瀣,瀤,瀥,瀦,瀧,瀨,瀩,瀰,瀱,瀲,瀳,瀴,瀵,瀶,瀷,瀸,瀹,灀,灁,灂,灃,灄,灅,灆,灇,灈,灉,灐,灑,灒,灓,灔,灕,灖,灗,灘,灙,灠,灡,灢,灣,灤,灥,灦,灧,灨,灩,灰,灱,灲,灳,灴,灵,灶,灷,灸,灹,炀,炁,炂,炃,炄,炅,炆,炇,炈,炉,炐,炑,炒,炓,炔,炕,炖,炗,炘,炙,焀,焁,焂,焃,焄,焅,焆,焇,焈,焉,焐,焑,焒,焓,焔,焕,焖,焗,焘,焙,焠,無,焢,焣,焤,焥,焦,焧,焨,焩,焰,焱,焲,焳,焴,焵,然,焷,焸,焹,煀,煁,煂,煃,煄,煅,煆,煇,煈,煉,煐,煑,煒,煓,煔,煕,煖,煗,煘,煙,煠,煡,煢,煣,煤,煥,煦,照,煨,煩,煰,煱,煲,煳,煴,煵,煶,煷,煸,煹,熀,熁,熂,熃,熄,熅,熆,熇,熈,熉,熐,熑,熒,熓,熔,熕,熖,熗,熘,熙,爀,爁,爂,爃,爄,爅,爆,爇,爈,爉,爐,爑,爒,爓,爔,爕,爖,爗,爘,爙,爠,爡,爢,爣,爤,爥,爦,爧,爨,爩,爰,爱,爲,爳,爴,爵,父,爷,爸,爹,牀,牁,牂,牃,牄,牅,牆,片,版,牉,牐,牑,牒,牓,牔,牕,牖,牗,牘,牙,牠,牡,牢,牣,牤,牥,牦,牧,牨,物,牰,牱,牲,牳,牴,牵,牶,牷,牸,特,犀,犁,犂,犃,犄,犅,犆,犇,犈,犉,犐,犑,犒,犓,犔,犕,犖,犗,犘,犙,猀,猁,猂,猃,猄,猅,猆,猇,猈,猉,猐,猑,猒,猓,猔,猕,猖,猗,猘,猙,猠,猡,猢,猣,猤,猥,猦,猧,猨,猩,猰,猱,猲,猳,猴,猵,猶,猷,猸,猹,獀,獁,獂,獃,獄,獅,獆,獇,獈,獉,獐,獑,獒,獓,獔,獕,獖,獗,獘,獙,獠,獡,獢,獣,獤,獥,獦,獧,獨,獩,獰,獱,獲,獳,獴,獵,獶,獷,獸,獹,玀,玁,玂,玃,玄,玅,玆,率,玈,玉,玐,玑,玒,玓,玔,玕,玖,玗,玘,玙,琀,琁,琂,球,琄,琅,理,琇,琈,琉,琐,琑,琒,琓,琔,琕,琖,琗,琘,琙,琠,琡,琢,琣,琤,琥,琦,琧,琨,琩,琰,琱,琲,琳,琴,琵,琶,琷,琸,琹,瑀,瑁,瑂,瑃,瑄,瑅,瑆,瑇,瑈,瑉,瑐,瑑,瑒,瑓,瑔,瑕,瑖,瑗,瑘,瑙,瑠,瑡,瑢,瑣,瑤,瑥,瑦,瑧,瑨,瑩,瑰,瑱,瑲,瑳,瑴,瑵,瑶,瑷,瑸,瑹,璀,璁,璂,璃,璄,璅,璆,璇,璈,璉,璐,璑,璒,璓,璔,璕,璖,璗,璘,璙,甀,甁,甂,甃,甄,甅,甆,甇,甈,甉,甐,甑,甒,甓,甔,甕,甖,甗,甘,甙,甠,甡,產,産,甤,甥,甦,甧,用,甩,田,由,甲,申,甴,电,甶,男,甸,甹,畀,畁,畂,畃,畄,畅,畆,畇,畈,畉,畐,畑,畒,畓,畔,畕,畖,畗,畘,留,畠,畡,畢,畣,畤,略,畦,畧,畨,畩,異,畱,畲,畳,畴,畵,當,畷,畸,畹,疀,疁,疂,疃,疄,疅,疆,疇,疈,疉,疐,疑,疒,疓,疔,疕,疖,疗,疘,疙,瘀,瘁,瘂,瘃,瘄,瘅,瘆,瘇,瘈,瘉,瘐,瘑,瘒,瘓,瘔,瘕,瘖,瘗,瘘,瘙,瘠,瘡,瘢,瘣,瘤,瘥,瘦,瘧,瘨,瘩,瘰,瘱,瘲,瘳,瘴,瘵,瘶,瘷,瘸,瘹,癀,癁,療,癃,癄,癅,癆,癇,癈,癉,癐,癑,癒,癓,癔,癕,癖,癗,癘,癙,癠,癡,癢,癣,癤,癥,癦,癧,癨,癩,癰,癱,癲,癳,癴,癵,癶,癷,癸,癹,皀,皁,皂,皃,的,皅,皆,皇,皈,皉,皐,皑,皒,皓,皔,皕,皖,皗,皘,皙,眀,省,眂,眃,眄,眅,眆,眇,眈,眉,眐,眑,眒,眓,眔,眕,眖,眗,眘,眙,眠,眡,眢,眣,眤,眥,眦,眧,眨,眩,眰,眱,眲,眳,眴,眵,眶,眷,眸,眹,着,睁,睂,睃,睄,睅,睆,睇,睈,睉,睐,睑,睒,睓,睔,睕,睖,睗,睘,睙,睠,睡,睢,督,睤,睥,睦,睧,睨,睩,睰,睱,睲,睳,睴,睵,睶,睷,睸,睹,瞀,瞁,瞂,瞃,瞄,瞅,瞆,瞇,瞈,瞉,瞐,瞑,瞒,瞓,瞔,瞕,瞖,瞗,瞘,瞙,砀,码,砂,砃,砄,砅,砆,砇,砈,砉,砐,砑,砒,砓,研,砕,砖,砗,砘,砙,砠,砡,砢,砣,砤,砥,砦,砧,砨,砩,砰,砱,砲,砳,破,砵,砶,砷,砸,砹,础,硁,硂,硃,硄,硅,硆,硇,硈,硉,硐,硑,硒,硓,硔,硕,硖,硗,硘,硙,硠,硡,硢,硣,硤,硥,硦,硧,硨,硩,硰,硱,硲,硳,硴,硵,硶,硷,硸,硹,碀,碁,碂,碃,碄,碅,碆,碇,碈,碉,碐,碑,碒,碓,碔,碕,碖,碗,碘,碙,礀,礁,礂,礃,礄,礅,礆,礇,礈,礉,礐,礑,礒,礓,礔,礕,礖,礗,礘,礙,礠,礡,礢,礣,礤,礥,礦,礧,礨,礩,礰,礱,礲,礳,礴,礵,礶,礷,礸,礹,祀,祁,祂,祃,祄,祅,祆,祇,祈,祉,祐,祑,祒,祓,祔,祕,祖,祗,祘,祙,祠,祡,祢,祣,祤,祥,祦,祧,票,祩,祰,祱,祲,祳,祴,祵,祶,祷,祸,祹,禀,禁,禂,禃,禄,禅,禆,禇,禈,禉,禐,禑,禒,禓,禔,禕,禖,禗,禘,禙,耀,老,耂,考,耄,者,耆,耇,耈,耉,耐,耑,耒,耓,耔,耕,耖,耗,耘,耙,耠,耡,耢,耣,耤,耥,耦,耧,耨,耩,耰,耱,耲,耳,耴,耵,耶,耷,耸,耹,聀,聁,聂,聃,聄,聅,聆,聇,聈,聉,聐,聑,聒,聓,联,聕,聖,聗,聘,聙,聠,聡,聢,聣,聤,聥,聦,聧,聨,聩,聰,聱,聲,聳,聴,聵,聶,職,聸,聹,肀,肁,肂,肃,肄,肅,肆,肇,肈,肉,肐,肑,肒,肓,肔,肕,肖,肗,肘,肙,脀,脁,脂,脃,脄,脅,脆,脇,脈,脉,脐,脑,脒,脓,脔,脕,脖,脗,脘,脙,脠,脡,脢,脣,脤,脥,脦,脧,脨,脩,脰,脱,脲,脳,脴,脵,脶,脷,脸,脹,腀,腁,腂,腃,腄,腅,腆,腇,腈,腉,腐,腑,腒,腓,腔,腕,腖,腗,腘,腙,腠,腡,腢,腣,腤,腥,腦,腧,腨,腩,腰,腱,腲,腳,腴,腵,腶,腷,腸,腹,膀,膁,膂,膃,膄,膅,膆,膇,膈,膉,膐,膑,膒,膓,膔,膕,膖,膗,膘,膙,舀,舁,舂,舃,舄,舅,舆,與,興,舉,舐,舑,舒,舓,舔,舕,舖,舗,舘,舙,舠,舡,舢,舣,舤,舥,舦,舧,舨,舩,舰,舱,舲,舳,舴,舵,舶,舷,舸,船,艀,艁,艂,艃,艄,艅,艆,艇,艈,艉,艐,艑,艒,艓,艔,艕,艖,艗,艘,艙,艠,艡,艢,艣,艤,艥,艦,艧,艨,艩,艰,艱,色,艳,艴,艵,艶,艷,艸,艹,芀,芁,节,芃,芄,芅,芆,芇,芈,芉,芐,芑,芒,芓,芔,芕,芖,芗,芘,芙,茀,茁,茂,范,茄,茅,茆,茇,茈,茉,茐,茑,茒,茓,茔,茕,茖,茗,茘,茙,茠,茡,茢,茣,茤,茥,茦,茧,茨,茩,茰,茱,茲,茳,茴,茵,茶,茷,茸,茹,荀,荁,荂,荃,荄,荅,荆,荇,荈,草,荐,荑,荒,荓,荔,荕,荖,荗,荘,荙,荠,荡,荢,荣,荤,荥,荦,荧,荨,荩,荰,荱,荲,荳,荴,荵,荶,荷,荸,荹,莀,莁,莂,莃,莄,莅,莆,莇,莈,莉,莐,莑,莒,莓,莔,莕,莖,莗,莘,莙,萀,萁,萂,萃,萄,萅,萆,萇,萈,萉,萐,萑,萒,萓,萔,萕,萖,萗,萘,萙,萠,萡,萢,萣,萤,营,萦,萧,萨,萩,萰,萱,萲,萳,萴,萵,萶,萷,萸,萹,葀,葁,葂,葃,葄,葅,葆,葇,葈,葉,葐,葑,葒,葓,葔,葕,葖,著,葘,葙,葠,葡,葢,董,葤,葥,葦,葧,葨,葩,葰,葱,葲,葳,葴,葵,葶,葷,葸,葹,蒀,蒁,蒂,蒃,蒄,蒅,蒆,蒇,蒈,蒉,蒐,蒑,蒒,蒓,蒔,蒕,蒖,蒗,蒘,蒙,蔀,蔁,蔂,蔃,蔄,蔅,蔆,蔇,蔈,蔉,蔐,蔑,蔒,蔓,蔔,蔕,蔖,蔗,蔘,蔙,蔠,蔡,蔢,蔣,蔤,蔥,蔦,蔧,蔨,蔩,蔰,蔱,蔲,蔳,蔴,蔵,蔶,蔷,蔸,蔹,蕀,蕁,蕂,蕃,蕄,蕅,蕆,蕇,蕈,蕉,蕐,蕑,蕒,蕓,蕔,蕕,蕖,蕗,蕘,蕙,蕠,蕡,蕢,蕣,蕤,蕥,蕦,蕧,蕨,蕩,蕰,蕱,蕲,蕳,蕴,蕵,蕶,蕷,蕸,蕹,薀,薁,薂,薃,薄,薅,薆,薇,薈,薉,薐,薑,薒,薓,薔,薕,薖,薗,薘,薙,蘀,蘁,蘂,蘃,蘄,蘅,蘆,蘇,蘈,蘉,蘐,蘑,蘒,蘓,蘔,蘕,蘖,蘗,蘘,蘙,蘠,蘡,蘢,蘣,蘤,蘥,蘦,蘧,蘨,蘩,蘰,蘱,蘲,蘳,蘴,蘵,蘶,蘷,蘸,蘹,虀,虁,虂,虃,虄,虅,虆,虇,虈,虉,虐,虑,虒,虓,虔,處,虖,虗,虘,虙,虠,虡,虢,虣,虤,虥,虦,虧,虨,虩,虰,虱,虲,虳,虴,虵,虶,虷,虸,虹,蚀,蚁,蚂,蚃,蚄,蚅,蚆,蚇,蚈,蚉,蚐,蚑,蚒,蚓,蚔,蚕,蚖,蚗,蚘,蚙,蜀,蜁,蜂,蜃,蜄,蜅,蜆,蜇,蜈,蜉,蜐,蜑,蜒,蜓,蜔,蜕,蜖,蜗,蜘,蜙,蜠,蜡,蜢,蜣,蜤,蜥,蜦,蜧,蜨,蜩,蜰,蜱,蜲,蜳,蜴,蜵,蜶,蜷,蜸,蜹,蝀,蝁,蝂,蝃,蝄,蝅,蝆,蝇,蝈,蝉,蝐,蝑,蝒,蝓,蝔,蝕,蝖,蝗,蝘,蝙,蝠,蝡,蝢,蝣,蝤,蝥,蝦,蝧,蝨,蝩,蝰,蝱,蝲,蝳,蝴,蝵,蝶,蝷,蝸,蝹,螀,螁,螂,螃,螄,螅,螆,螇,螈,螉,螐,螑,螒,螓,螔,螕,螖,螗,螘,螙,蠀,蠁,蠂,蠃,蠄,蠅,蠆,蠇,蠈,蠉,蠐,蠑,蠒,蠓,蠔,蠕,蠖,蠗,蠘,蠙,蠠,蠡,蠢,蠣,蠤,蠥,蠦,蠧,蠨,蠩,蠰,蠱,蠲,蠳,蠴,蠵,蠶,蠷,蠸,蠹,血,衁,衂,衃,衄,衅,衆,衇,衈,衉,衐,衑,衒,術,衔,衕,衖,街,衘,衙,衠,衡,衢,衣,衤,补,衦,衧,表,衩,衰,衱,衲,衳,衴,衵,衶,衷,衸,衹,袀,袁,袂,袃,袄,袅,袆,袇,袈,袉,袐,袑,袒,袓,袔,袕,袖,袗,袘,袙,褀,褁,褂,褃,褄,褅,褆,複,褈,褉,褐,褑,褒,褓,褔,褕,褖,褗,褘,褙,褠,褡,褢,褣,褤,褥,褦,褧,褨,褩,褰,褱,褲,褳,褴,褵,褶,褷,褸,褹,襀,襁,襂,襃,襄,襅,襆,襇,襈,襉,襐,襑,襒,襓,襔,襕,襖,襗,襘,襙,襠,襡,襢,襣,襤,襥,襦,襧,襨,襩,襰,襱,襲,襳,襴,襵,襶,襷,襸,襹,覀,要,覂,覃,覄,覅,覆,覇,覈,覉,覐,覑,覒,覓,覔,覕,視,覗,覘,覙,退,送,适,逃,逄,逅,逆,逇,逈,选,逐,逑,递,逓,途,逕,逖,逗,逘,這,造,逡,逢,連,逤,逥,逦,逧,逨,逩,逰,週,進,逳,逴,逵,逶,逷,逸,逹,遀,遁,遂,遃,遄,遅,遆,遇,遈,遉,遐,遑,遒,道,達,違,遖,遗,遘,遙,遠,遡,遢,遣,遤,遥,遦,遧,遨,適,遰,遱,遲,遳,遴,遵,遶,遷,選,遹,邀,邁,邂,邃,還,邅,邆,邇,邈,邉,邐,邑,邒,邓,邔,邕,邖,邗,邘,邙,鄀,鄁,鄂,鄃,鄄,鄅,鄆,鄇,鄈,鄉,鄐,鄑,鄒,鄓,鄔,鄕,鄖,鄗,鄘,鄙,鄠,鄡,鄢,鄣,鄤,鄥,鄦,鄧,鄨,鄩,鄰,鄱,鄲,鄳,鄴,鄵,鄶,鄷,鄸,鄹,酀,酁,酂,酃,酄,酅,酆,酇,酈,酉,酐,酑,酒,酓,酔,酕,酖,酗,酘,酙,酠,酡,酢,酣,酤,酥,酦,酧,酨,酩,酰,酱,酲,酳,酴,酵,酶,酷,酸,酹,醀,醁,醂,醃,醄,醅,醆,醇,醈,醉,醐,醑,醒,醓,醔,醕,醖,醗,醘,醙,鈀,鈁,鈂,鈃,鈄,鈅,鈆,鈇,鈈,鈉,鈐,鈑,鈒,鈓,鈔,鈕,鈖,鈗,鈘,鈙,鈠,鈡,鈢,鈣,鈤,鈥,鈦,鈧,鈨,鈩,鈰,鈱,鈲,鈳,鈴,鈵,鈶,鈷,鈸,鈹,鉀,鉁,鉂,鉃,鉄,鉅,鉆,鉇,鉈,鉉,鉐,鉑,鉒,鉓,鉔,鉕,鉖,鉗,鉘,鉙,鉠,鉡,鉢,鉣,鉤,鉥,鉦,鉧,鉨,鉩,鉰,鉱,鉲,鉳,鉴,鉵,鉶,鉷,鉸,鉹,銀,銁,銂,銃,銄,銅,銆,銇,銈,銉,銐,銑,銒,銓,銔,銕,銖,銗,銘,銙,錀,錁,錂,錃,錄,錅,錆,錇,錈,錉,錐,錑,錒,錓,錔,錕,錖,錗,錘,錙,錠,錡,錢,錣,錤,錥,錦,錧,錨,錩,錰,錱,録,錳,錴,錵,錶,錷,錸,錹,鍀,鍁,鍂,鍃,鍄,鍅,鍆,鍇,鍈,鍉,鍐,鍑,鍒,鍓,鍔,鍕,鍖,鍗,鍘,鍙,鍠,鍡,鍢,鍣,鍤,鍥,鍦,鍧,鍨,鍩,鍰,鍱,鍲,鍳,鍴,鍵,鍶,鍷,鍸,鍹,鎀,鎁,鎂,鎃,鎄,鎅,鎆,鎇,鎈,鎉,鎐,鎑,鎒,鎓,鎔,鎕,鎖,鎗,鎘,鎙,鐀,鐁,鐂,鐃,鐄,鐅,鐆,鐇,鐈,鐉,鐐,鐑,鐒,鐓,鐔,鐕,鐖,鐗,鐘,鐙,鐠,鐡,鐢,鐣,鐤,鐥,鐦,鐧,鐨,鐩,鐰,鐱,鐲,鐳,鐴,鐵,鐶,鐷,鐸,鐹,鑀,鑁,鑂,鑃,鑄,鑅,鑆,鑇,鑈,鑉,鑐,鑑,鑒,鑓,鑔,鑕,鑖,鑗,鑘,鑙,鑠,鑡,鑢,鑣,鑤,鑥,鑦,鑧,鑨,鑩,鑰,鑱,鑲,鑳,鑴,鑵,鑶,鑷,鑸,鑹,钀,钁,钂,钃,钄,钅,钆,钇,针,钉,钐,钑,钒,钓,钔,钕,钖,钗,钘,钙,销,锁,锂,锃,锄,锅,锆,锇,锈,锉,锐,锑,锒,锓,锔,锕,锖,锗,锘,错,锠,锡,锢,锣,锤,锥,锦,锧,锨,锩,锰,锱,锲,锳,锴,锵,锶,锷,锸,锹,镀,镁,镂,镃,镄,镅,镆,镇,镈,镉,镐,镑,镒,镓,镔,镕,镖,镗,镘,镙,镠,镡,镢,镣,镤,镥,镦,镧,镨,镩,镰,镱,镲,镳,镴,镵,镶,長,镸,镹,門,閁,閂,閃,閄,閅,閆,閇,閈,閉,閐,閑,閒,間,閔,閕,閖,閗,閘,閙,阀,阁,阂,阃,阄,阅,阆,阇,阈,阉,阐,阑,阒,阓,阔,阕,阖,阗,阘,阙,阠,阡,阢,阣,阤,阥,阦,阧,阨,阩,阰,阱,防,阳,阴,阵,阶,阷,阸,阹,陀,陁,陂,陃,附,际,陆,陇,陈,陉,限,陑,陒,陓,陔,陕,陖,陗,陘,陙,陠,陡,院,陣,除,陥,陦,陧,陨,险,陰,陱,陲,陳,陴,陵,陶,陷,陸,陹,隀,隁,隂,隃,隄,隅,隆,隇,隈,隉,隐,隑,隒,隓,隔,隕,隖,隗,隘,隙,需,霁,霂,霃,霄,霅,霆,震,霈,霉,霐,霑,霒,霓,霔,霕,霖,霗,霘,霙,霠,霡,霢,霣,霤,霥,霦,霧,霨,霩,霰,霱,露,霳,霴,霵,霶,霷,霸,霹,靀,靁,靂,靃,靄,靅,靆,靇,靈,靉,靐,靑,青,靓,靔,靕,靖,靗,靘,静,靠,靡,面,靣,靤,靥,靦,靧,靨,革,靰,靱,靲,靳,靴,靵,靶,靷,靸,靹,鞀,鞁,鞂,鞃,鞄,鞅,鞆,鞇,鞈,鞉,鞐,鞑,鞒,鞓,鞔,鞕,鞖,鞗,鞘,鞙,頀,頁,頂,頃,頄,項,順,頇,須,頉,預,頑,頒,頓,頔,頕,頖,頗,領,頙,頠,頡,頢,頣,頤,頥,頦,頧,頨,頩,頰,頱,頲,頳,頴,頵,頶,頷,頸,頹,顀,顁,顂,顃,顄,顅,顆,顇,顈,顉,顐,顑,顒,顓,顔,顕,顖,顗,願,顙,顠,顡,顢,顣,顤,顥,顦,顧,顨,顩,顰,顱,顲,顳,顴,页,顶,顷,顸,项,颀,颁,颂,颃,预,颅,领,颇,颈,颉,颐,频,颒,颓,颔,颕,颖,颗,题,颙,餀,餁,餂,餃,餄,餅,餆,餇,餈,餉,餐,餑,餒,餓,餔,餕,餖,餗,餘,餙,餠,餡,餢,餣,餤,餥,餦,餧,館,餩,餰,餱,餲,餳,餴,餵,餶,餷,餸,餹,饀,饁,饂,饃,饄,饅,饆,饇,饈,饉,饐,饑,饒,饓,饔,饕,饖,饗,饘,饙,饠,饡,饢,饣,饤,饥,饦,饧,饨,饩,饰,饱,饲,饳,饴,饵,饶,饷,饸,饹,馀,馁,馂,馃,馄,馅,馆,馇,馈,馉,馐,馑,馒,馓,馔,馕,首,馗,馘,香', ',') satisfies matches($s, '^(?:[\\w])$')) and (every $s in tokenize('', ',') satisfies not(matches($s, '^(?:[\\w])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00985() {
    final XQuery query = new XQuery(
      "(every $s in tokenize('0,1,2,3,4,5,6,7,8,9,٠,١,٢,٣,٤,٥,٦,٧,٨,٩,۰,۱,۲,۳,۴,۵,۶,۷,۸,۹,०,१,२,३,४,५,६,७,८,९,০,১,২,৩,৪,৫,৬,৭,৮,৯,੦,੧,੨,੩,੪,੫,੬,੭,੮,੯,૦,૧,૨,૩,૪,૫,૬,૭,૮,૯,୦,୧,୨,୩,୪,୫,୬,୭,୮,୯,௧,௨,௩,௪,௫,௬,௭,௮,௯,౦,౧,౨,౩,౪,౫,౬,౭,౮,౯,೦,೧,೨,೩,೪,೫,೬,೭,೮,೯,൦,൧,൨,൩,൪,൫,൬,൭,൮,൯,๐,๑,๒,๓,๔,๕,๖,๗,๘,๙,໐,໑,໒,໓,໔,໕,໖,໗,໘,໙,༠,༡,༢,༣,༤,༥,༦,༧,༨,༩,၀,၁,၂,၃,၄,၅,၆,၇,၈,၉,០,១,២,៣,៤,៥,៦,៧,៨,៩,᠐,᠑,᠒,᠓,᠔,᠕,᠖,᠗,᠘,᠙,０,１,２,３,４,５,６,７,８,９', ',') satisfies matches($s, '^(?:[\\d])$')) and (every $s in tokenize('\t,\n" +
      ",\r, ,!,\",#,$,왣,왤,왥,왦,왧,왨,왩,왪,왫,왬,왭,왮,왯,왰,왱,왲,왳,왴,왵,왶,왷,외,왹,왺,왻,왼,왽,왾,왿,욀,욁,욂,욃,욄,욅,욆,욇,욈,욉,욊,욋,욌,욍,욎,욏,욐,욑,욒,욓,요,욕,욖,욗,욘,욙,욚,욛,욜,욝,욞,욟,욠,욡,욢,욣,욤,욥,욦,욧,욨,용,욪,욫,욬,욭,욮,욯,우,욱,욲,욳,운,욵,욶,욷,울,욹,욺,욻,욼,욽,욾,욿,움,웁,웂,웃,웄,웅,웆,웇,웈,웉,웊,웋,워,웍,웎,웏,원,웑,웒,웓,월,웕,웖,웗,웘,웙,웚,웛,웜,웝,웞,웟,웠,웡,웢,웣,웤,웥,웦,웧,웨,웩,웪,웫,웬,웭,웮,웯,웰,웱,웲,웳,웴,웵,웶,웷,웸,웹,웺,웻,웼,웽,웾,웿,윀,윁,윂,윃,위,윅,윆,윇,윈,윉,윊,윋,윌,윍,윎,윏,윐,윑,윒,윓,윔,윕,윖,윗,윘,윙,윚,윛,윜,윝,윞,윟,유,육,윢,윣,윤,윥,윦,윧,율,윩,윪,윫,윬,윭,윮,윯,윰,윱,윲,윳,윴,융,윶,윷,윸,윹,윺,윻,으,윽,윾,윿,은,읁,읂,읃,을,읅,읆,읇,읈,읉,읊,읋,음,읍,읎,읏,읐,응,읒,읓,읔,읕,읖,읗,의,읙,읚,읛,읜,읝,읞,읟,읠,읡,읢,읣,읤,읥,읦,읧,읨,읩,읪,읫,읬,읭,읮,읯,읰,읱,읲,읳,이,익,읶,읷,인,읹,읺,읻,일,재,잭,잮,잯,잰,잱,잲,잳,잴,잵,잶,잷,잸,잹,잺,잻,잼,잽,잾,잿,쟀,쟁,쟂,쟃,쟄,쟅,쟆,쟇,쟈,쟉,쟊,쟋,쟌,쟍,쟎,쟏,쟐,쟑,쟒,쟓,쟔,쟕,쟖,쟗,쟘,쟙,쟚,쟛,쟜,쟝,쟞,쟟,쟠,쟡,쟢,쟣,쟤,쟥,쟦,쟧,쟨,쟩,쟪,쟫,쟬,쟭,쟮,쟯,쟰,쟱,쟲,쟳,쟴,쟵,쟶,쟷,쟸,쟹,쟺,쟻,쟼,쟽,쟾,쟿,저,적,젂,젃,전,젅,젆,젇,절,젉,젊,젋,젌,젍,젎,젏,점,접,젒,젓,젔,정,젖,젗,젘,젙,젚,젛,제,젝,젞,젟,젠,젡,젢,젣,젤,젥,젦,젧,젨,젩,젪,젫,젬,젭,젮,젯,젰,젱,젲,젳,젴,젵,젶,젷,져,젹,젺,젻,젼,젽,젾,젿,졀,졁,졂,졃,졄,졅,졆,졇,졈,졉,졊,졋'||\n" +
      "',졌,졍,졎,졏,졐,졑,졒,졓,졔,졕,졖,졗,졘,졙,졚,졛,졜,졝,졞,졟,졠,졡,졢,졣,졤,졥,졦,졧,졨,졩,졪,졫,졬,졭,졮,졯,조,족,졲,졳,존,졵,졶,졷,졸,졹,졺,졻,졼,졽,졾,졿,좀,좁,좂,좃,좄,종,좆,좇,좈,좉,좊,좋,좌,좍,좎,좏,좐,좑,좒,좓,좔,좕,좖,좗,좘,좙,좚,좛,좜,좝,좞,좟,좠,좡,좢,좣,좤,좥,좦,좧,좨,좩,좪,좫,좬,좭,좮,좯,좰,좱,좲,좳,좴,좵,좶,좷,좸,좹,좺,좻,좼,좽,좾,좿,죀,죁,죂,죃,죄,죅,죆,죇,죈,죉,죊,죋,죌,죍,죎,죏,죐,죑,죒,죓,죔,죕,죖,죗,죘,죙,죚,죛,죜,죝,죞,죟,죠,죡,죢,죣,죤,죥,죦,죧,죨,죩,죪,죫,죬,죭,죮,죯,죰,죱,죲,죳,죴,죵,죶,죷,죸,죹,죺,죻,주,죽,죾,죿,준,줁,줂,줃,줄,줅,줆,줇,줈,줉,줊,줋,줌,줍,줎,줏,줐,중,줒,줓,줔,줕,줖,줗,줘,줙,줚,줛,줜,줝,줞,줟,줠,줡,줢,줣,줤,줥,줦,줧,줨,줩,줪,줫,줬,줭,줮,줯,줰,줱,줲,줳,줴,줵,줶,줷,줸,줹,줺,줻,줼,줽,줾,줿,쥀,쥁,쥂,쥃,쥄,쥅,쥆,쥇,쥈,쥉,쥊,쥋,쥌,쥍,쥎,쥏,쥐,쥑,쥒,쥓,쥔,쥕,쥖,쥗,쥘,쥙,쥚,쥛,쥜,쥝,쥞,쥟,쥠,쥡,쥢,쥣,쥤,쥥,쥦,쥧,쥨,쥩,쥪,쥫,쥬,쥭,쥮,쥯,쥰,쥱,쥲,\uff02,\uff03,＄,\uff05,\uff06,\uff07,\uff08,\uff09,\uff0a,＋,\uff0c,\uff0d,\uff0e,\uff0f,\uff1a,\uff1b,＜,＝,＞,\uff1f,\uff20,Ａ,Ｂ,Ｃ,Ｄ,Ｅ,Ｆ,Ｇ,Ｈ,Ｉ,Ｊ,Ｋ,Ｌ,Ｍ,Ｎ,Ｏ,Ｐ,Ｑ,Ｒ,Ｓ,Ｔ,Ｕ,Ｖ,Ｗ,Ｘ,Ｙ,Ｚ,\uff3b,\uff3c,\uff3d,＾,\uff3f,｀,ａ,ｂ,ｃ,ｄ,ｅ,ｆ,ｇ,ｈ,ｉ,ｊ,ｋ,ｌ,ｍ,ｎ,ｏ,ｐ,ｑ,ｒ,ｓ,ｔ,ｕ,ｖ,ｗ,ｘ,ｙ,ｚ,\uff5b,｜,\uff5d,～,\uff5f,\uff60,\uff61,\uff62,\uff63,\uff64,\uff65,ｦ,ｧ,ｨ,ｩ,ｪ,ｫ,ｬ,ｭ,ｮ,ｯ,ｰ,ｱ,ｲ,ｳ,ｴ,ｵ,ｶ,ｷ,ｸ,ｹ,ｺ,ｻ,ｼ,ｽ,ｾ,ｿ,ﾀ,ﾁ,ﾂ,ﾃ,ﾄ,ﾅ,ﾆ,ﾇ,ﾈ,ﾉ,ﾊ,ﾋ,ﾌ,ﾍ,￢,￣,￤,￥,￦,\uffe7,￨,￩,￪,￫,￬,￭,￮,\uffef,\ufff0,\ufff1,\ufff2,\ufff3,\ufff4,\ufff5,\ufff6,\ufff7,\ufff8,\ufff9,\ufffa,\ufffb,￼,�', ',') satisfies not(matches($s, '^(?:[\\d])$')))",
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
   * Test regex syntax.
   */
  @org.junit.Test
  public void re00987() {
    final XQuery query = new XQuery(
      "(for $range in\n" +
      "            tokenize('65-90;97-122;192-214;216-246;248-305;308-318;321-328;330-382;384-451;461-496;500-501;506-535;592-680;699-705;902-902;904-906;908-908;910-929;931-974;976-982;986-986;988-988;990-990;992-992;994-1011;1025-1036;1038-1103;1105-1116;1118-1153;1168-1220;1223-1224;1227-1228;1232-1259;1262-1269;1272-1273;1329-1366;1369-1369;1377-1414;1488-1514;1520-1522;1569-1594;1601-1610;1649-1719;1722-1726;1728-1742;1744-1747;1749-1749;1765-1766;2309-2361;2365-2365;2392-2401;2437-2444;2447-2448;2451-2472;2474-2480;2482-2482;2486-2489;2524-2525;2527-2529;2544-2545;2565-2570;'||\n" +
      "            '2575-2576;2579-2600;2602-2608;2610-2611;2613-2614;2616-2617;2649-2652;2654-2654;2674-2676;2693-2699;2701-2701;2703-2705;2707-2728;2730-2736;2738-2739;2741-2745;2749-2749;2784-2784;2821-2828;2831-2832;2835-2856;2858-2864;2866-2867;2870-2873;2877-2877;2908-2909;2911-2913;2949-2954;2958-2960;2962-2965;2969-2970;2972-2972;2974-2975;2979-2980;2984-2986;2990-2997;2999-3001;3077-3084;3086-3088;3090-3112;3114-3123;3125-3129;3168-3169;3205-3212;3214-3216;3218-3240;3242-3251;3253-3257;3294-3294;3296-3297;3333-3340;3342-3344;3346-3368;3370-3385;3424-3425;3585-3630;3632-3632;'||\n" +
      "            '3634-3635;3648-3653;3713-3714;3716-3716;3719-3720;3722-3722;3725-3725;3732-3735;3737-3743;3745-3747;3749-3749;3751-3751;3754-3755;3757-3758;3760-3760;3762-3763;3773-3773;3776-3780;3904-3911;3913-3945;4256-4293;4304-4342;4352-4352;4354-4355;4357-4359;4361-4361;4363-4364;4366-4370;4412-4412;4414-4414;4416-4416;4428-4428;4430-4430;4432-4432;4436-4437;4441-4441;4447-4449;4451-4451;4453-4453;4455-4455;4457-4457;4461-4462;4466-4467;4469-4469;4510-4510;4520-4520;4523-4523;4526-4527;4535-4536;4538-4538;4540-4546;4587-4587;4592-4592;4601-4601;7680-7835;7840-7929;7936-7957;'||\n" +
      "            '7960-7965;7968-8005;8008-8013;8016-8023;8025-8025;8027-8027;8029-8029;8031-8061;8064-8116;8118-8124;8126-8126;8130-8132;8134-8140;8144-8147;8150-8155;8160-8172;8178-8180;8182-8188;8486-8486;8490-8491;8494-8494;8576-8578;12353-12436;12449-12538;12549-12588;44032-55203;19968-40869;12295-12295;12321-12329;48-57;1632-1641;1776-1785;2406-2415;2534-2543;2662-2671;2790-2799;2918-2927;3047-3055;3174-3183;3302-3311;3430-3439;3664-3673;3792-3801;3872-3881;768-837;864-865;1155-1158;1425-1441;1443-1465;1467-1469;1471-1471;1473-1474;1476-1476;1611-1618;1648-1648;1750-1764;1767-1768;'||\n" +
      "            '1770-1773;2305-2307;2364-2364;2366-2381;2385-2388;2402-2403;2433-2435;2492-2492;2494-2500;2503-2504;2507-2509;2519-2519;2530-2531;2562-2562;2620-2620;2622-2626;2631-2632;2635-2637;2672-2673;2689-2691;2748-2748;2750-2757;2759-2761;2763-2765;2817-2819;2876-2876;2878-2883;2887-2888;2891-2893;2902-2903;2946-2947;3006-3010;3014-3016;3018-3021;3031-3031;3073-3075;3134-3140;3142-3144;3146-3149;3157-3158;3202-3203;3262-3268;3270-3272;3274-3277;3285-3286;3330-3331;3390-3395;3398-3400;3402-3405;3415-3415;3633-3633;3636-3642;3655-3662;3761-3761;3764-3769;3771-3772;3784-3789;3864-3865;'||\n" +
      "            '3893-3893;3895-3895;3897-3897;3902-3903;3953-3972;3974-3979;3984-3989;3991-3991;3993-4013;4017-4023;4025-4025;8400-8412;8417-8417;12330-12335;12441-12442;183-183;720-721;903-903;1600-1600;3654-3654;3782-3782;12293-12293;12337-12341;12445-12446;12540-12542;58-58;95-95;45-46;65-90;97-122;192-214;216-246;248-305;308-318;321-328;330-382;384-451;461-496;500-501;506-535;592-680;699-705;902-902;904-906;908-908;910-929;931-974;976-982;986-986;988-988;990-990;992-992;994-1011;1025-1036;1038-1103;1105-1116;1118-1153;1168-1220;1223-1224;1227-1228;1232-1259;1262-1269;1272-1273;1329-1366;'||\n" +
      "            '1369-1369;1377-1414;1488-1514;1520-1522;1569-1594;1601-1610;1649-1719;1722-1726;1728-1742;1744-1747;1749-1749;1765-1766;2309-2361;2365-2365;2392-2401;2437-2444;2447-2448;2451-2472;2474-2480;2482-2482;2486-2489;2524-2525;2527-2529;2544-2545;2565-2570;2575-2576;2579-2600;2602-2608;2610-2611;2613-2614;2616-2617;2649-2652;2654-2654;2674-2676;2693-2699;2701-2701;2703-2705;2707-2728;2730-2736;2738-2739;2741-2745;2749-2749;2784-2784;2821-2828;2831-2832;2835-2856;2858-2864;2866-2867;2870-2873;2877-2877;2908-2909;2911-2913;2949-2954;2958-2960;2962-2965;2969-2970;2972-2972;2974-2975;'||\n" +
      "            '2979-2980;2984-2986;2990-2997;2999-3001;3077-3084;3086-3088;3090-3112;3114-3123;3125-3129;3168-3169;3205-3212;3214-3216;3218-3240;3242-3251;3253-3257;3294-3294;3296-3297;3333-3340;3342-3344;3346-3368;3370-3385;3424-3425;3585-3630;3632-3632;3634-3635;3648-3653;3713-3714;3716-3716;3719-3720;3722-3722;3725-3725;3732-3735;3737-3743;3745-3747;3749-3749;3751-3751;3754-3755;3757-3758;3760-3760;3762-3763;3773-3773;3776-3780;3904-3911;3913-3945;4256-4293;4304-4342;4352-4352;4354-4355;4357-4359;4361-4361;4363-4364;4366-4370;4412-4412;4414-4414;4416-4416;4428-4428;4430-4430;4432-4432;'||\n" +
      "            '4436-4437;4441-4441;4447-4449;4451-4451;4453-4453;4455-4455;4457-4457;4461-4462;4466-4467;4469-4469;4510-4510;4520-4520;4523-4523;4526-4527;4535-4536;4538-4538;4540-4546;4587-4587;4592-4592;4601-4601;7680-7835;7840-7929;7936-7957;7960-7965;7968-8005;8008-8013;8016-8023;8025-8025;8027-8027;8029-8029;8031-8061;8064-8116;8118-8124;8126-8126;8130-8132;8134-8140;8144-8147;8150-8155;8160-8172;8178-8180;8182-8188;8486-8486;8490-8491;8494-8494;8576-8578;12353-12436;12449-12538;12549-12588;44032-55203;19968-40869;12295-12295;12321-12329;48-57;1632-1641;1776-1785;2406-2415;2534-2543;'||\n" +
      "            '2662-2671;2790-2799;2918-2927;3047-3055;3174-3183;3302-3311;3430-3439;3664-3673;3792-3801;3872-3881;768-836;864-865;1155-1158;1425-1441;1443-1465;1467-1469;1471-1471;1473-1474;1476-1476;1611-1618;1648-1648;1750-1764;1767-1768;1770-1773;2305-2307;2364-2364;2366-2381;2385-2388;2402-2403;2433-2435;2492-2492;2494-2500;2503-2504;2507-2509;2519-2519;2530-2531;2562-2562;2620-2620;2622-2626;2631-2632;2635-2637;2672-2673;2689-2691;2748-2748;2750-2757;2759-2761;2763-2765;2817-2819;2876-2876;2878-2883;2887-2888;2891-2893;'||\n" +
      "            '2902-2903;2946-2947;3006-3010;3014-3016;3018-3021;3031-3031;3073-3075;3134-3140;3142-3144;3146-3149;3157-3158;3202-3203;3262-3268;3270-3272;3274-3277;3285-3286;3330-3331;3390-3395;3398-3400;3402-3405;3415-3415;3633-3633;3636-3642;3655-3662;3761-3761;3764-3769;3771-3772;3784-3789;3864-3865;3893-3893;3895-3895;3897-3897;3902-3903;3953-3972;3974-3979;3984-3989;3991-3991;3993-4013;4017-4023;4025-4025;8400-8412;8417-8417;12330-12335;12441-12442;183-183;720-721;903-903;1600-1600;3654-3654;3782-3782;12293-12293;12337-12341;12445-12446;12540-12542;58-58;95-95;45-46', ';')\n" +
      "            let $s := xs:integer(substring-before($range, '-')) \n" +
      "            let $e := xs:integer(substring-after($range, '-')) \n" +
      "            for $c in ($s to $e)!codepoints-to-string(.)\n" +
      "            where not(matches($c, '^([\\c])$')) \n" +
      "            return string-to-codepoints($c)), \n" +
      "            \n" +
      "            (for $range in tokenize('161-161', ';') \n" +
      "            let $s := xs:integer(substring-before($range, '-')) \n" +
      "            let $e := xs:integer(substring-after($range, '-')) \n" +
      "            for $c in ($s to $e)!codepoints-to-string(.)\n" +
      "            where (matches($c, '^([\\c])$')) \n" +
      "            return string-to-codepoints($c)) \n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Test regex syntax: invalid subtraction.
   */
  @org.junit.Test
  public void re00988() {
    final XQuery query = new XQuery(
      "matches('qwerty','[-[xyz]]')",
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
   * Test regex syntax: invalid subtraction.
   */
  @org.junit.Test
  public void re00989() {
    final XQuery query = new XQuery(
      "matches('qwerty','[^-[xyz]]')",
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
   * Test regex syntax: escaped dollar is OK in XPath.
   */
  @org.junit.Test
  public void re00990() {
    final XQuery query = new XQuery(
      "matches('$', '^\\$$') and not(matches('\\$', '^\\$$'))",
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
   * Test regex syntax: escaped dollar is OK in XPath.
   */
  @org.junit.Test
  public void re00991() {
    final XQuery query = new XQuery(
      "matches('$', '[\\$]') and not(matches('\\$', '^[\\$]$'))",
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
   * Backreference at end of string.
   */
  @org.junit.Test
  public void re00992() {
    final XQuery query = new XQuery(
      "matches('$$', '(.)\\1')",
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
   * Backreference followed by digit.
   */
  @org.junit.Test
  public void re00993() {
    final XQuery query = new XQuery(
      "matches('$$9', '(.)\\19')",
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
   * Two-digit Backreference.
   */
  @org.junit.Test
  public void re00994() {
    final XQuery query = new XQuery(
      "matches('$$9', '(((((((((((.)))))))))))\\119')",
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
   * Invalid single-digit Backreference.
   */
  @org.junit.Test
  public void re00995() {
    final XQuery query = new XQuery(
      "matches('$$9', '(.)\\2')",
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
   * Invalid single-digit Backreference.
   */
  @org.junit.Test
  public void re00996() {
    final XQuery query = new XQuery(
      "matches('$$9', '(.)(\\2)')",
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
   * Invalid two-digit Backreference.
   */
  @org.junit.Test
  public void re00997() {
    final XQuery query = new XQuery(
      "matches('$$9', '((((((((((.))))))))))(\\11)9')",
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
   * Quantifier after '^' (useless but allowed).
   */
  @org.junit.Test
  public void re00998() {
    final XQuery query = new XQuery(
      "matches('alpha', 'alp^?ha')",
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
   * Quantifier after '^' (useless but allowed).
   */
  @org.junit.Test
  public void re00999() {
    final XQuery query = new XQuery(
      "matches('alpha', 'alp^+ha')",
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
   * Quantifier after '^' (useless but allowed).
   */
  @org.junit.Test
  public void re01000() {
    final XQuery query = new XQuery(
      "matches('alpha', '^{2}alpha') and not(matches('zalpha', '^{2}alpha'))",
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
   * Quantifier after '$' (useless but allowed).
   */
  @org.junit.Test
  public void re01001() {
    final XQuery query = new XQuery(
      "matches('alpha', 'alp$?ha')",
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
   * Quantifier after '$' (useless but allowed).
   */
  @org.junit.Test
  public void re01002() {
    final XQuery query = new XQuery(
      "matches('alpha', 'alp${2,4}ha')",
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
   * Quantifier after '$' (useless but allowed).
   */
  @org.junit.Test
  public void re01003() {
    final XQuery query = new XQuery(
      "matches('alpha', 'alpha$+') and not(matches('alphax', 'alpha$+'))",
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
}
