package org.basex.http.restxq;

import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.TEN;
import static org.junit.Assert.*;

import org.basex.query.value.item.QNm;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Path matcher tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public final class RestXqPathMatcherTest {
  /** Test.
   * @throws Exception exception */
  @Test
  public void testParseEmptyPath() throws Exception {
    testParse("", "/", 0, ZERO);
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void testParseSimplePath() throws Exception {
    testParse("/a/b/c#xyz", "/a/b/c#xyz", 3, ZERO);
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void testParseSimplePathWithoutSlash() throws Exception {
    testParse("a/b/c#xyz", "/a/b/c#xyz", 3, ZERO);
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void testParseRegexEscape() throws Exception {
    testParse("/a/b/c-d.f#xyz", "/a/b/c\\-d\\.f#xyz", 3, ZERO);
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void testParseSimpleVariables() throws Exception {
    testParse(
            "/a1/{ $var1 }/a2/{ $var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 5, TEN, new QNm("var1"),
            new QNm("var2"));
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void testParseQualifiedVariables() throws Exception {
    testParse(
            "/a1/{ $n:var1 }/a2/{ $m:var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 5, TEN, new QNm("n:var1"),
            new QNm("m:var2"));
  }

  /** Test.
   * @throws Exception exception */
  @Ignore
  @Test
  public void testParseExtendedQualifiedVariables() throws Exception {
    testParse(
            "/a1/{ $n:var1 }/a2/{ $Q{m}var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 5, TEN, new QNm("n:var1"),
            new QNm("var2", "m"));
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void testParseVariablesWithSimpleRegex() throws Exception {
    testParse(
            "/a1/{  $n:var1\t=\t.+ }/a2/{$m:var2=[1-9][0-9]+}/a3",
            "/a1/(.+)/a2/([1-9][0-9]+)/a3", 5, TEN, new QNm("n:var1"),
            new QNm("m:var2"));
  }

  /** Test.
   * @throws Exception exception */
  @Test
  public void testParseVariablesWithComplexRegex() throws Exception {
    testParse(
            "/a1/{  $n:var1\t=\t.+ }/a2/{ $m:var2 = [1-9][0-9]{3,10} }/a3",
            "/a1/(.+)/a2/([1-9][0-9]{3,10})/a3", 5, TEN, new QNm("n:var1"),
            new QNm("m:var2"));
  }

  /**
   * Parses the specified input.
   * @param input input
   * @param regex regular expression
   * @param segments number of segments
   * @param variables variable positions
   * @param vars variables
   * @throws Exception exception
   */
  private static void testParse(final String input, final String regex, final int segments,
      final BigInteger variables, final QNm... vars) throws Exception {

    final RestXqPathMatcher p = RestXqPathMatcher.parse(input, null);
    assertEquals(regex, p.pattern.toString());
    assertEquals(Arrays.asList(vars), p.vars);
    assertEquals(segments, p.segments);
    assertEquals(variables, p.varsPos);
  }
}