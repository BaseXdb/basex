package org.basex.http.restxq;

import static java.math.BigInteger.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import java.util.*;

import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Path matcher tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class RestXqPathMatcherTest {
  /** Test.
   * @throws Exception exception */
  @Test public void testParseEmptyPath() throws Exception {
    testParse("", "/", 0, ZERO);
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testParseSimplePath() throws Exception {
    testParse("/a/b/c#xyz", "/a/b/c#xyz", 3, ZERO);
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testParseSimplePathWithoutSlash() throws Exception {
    testParse("a/b/c#xyz", "/a/b/c#xyz", 3, ZERO);
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testParseRegexEscape() throws Exception {
    testParse("/a/b/c-d.f#xyz", "/a/b/c\\-d\\.f#xyz", 3, ZERO);
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testParseSimpleVariables() throws Exception {
    testParse(
            "/a1/{ $var1 }/a2/{ $var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 5, TEN, new QNm("var1"),
            new QNm("var2"));
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testParseQualifiedVariables() throws Exception {
    testParse(
            "/a1/{ $n:var1 }/a2/{ $m:var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 5, TEN, new QNm("n:var1"),
            new QNm("m:var2"));
  }

  /** Test.
   * @throws Exception exception */
  @Disabled
  @Test public void testParseExtendedQualifiedVariables() throws Exception {
    testParse(
            "/a1/{ $n:var1 }/a2/{ $Q{m}var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 5, TEN, new QNm("n:var1"),
            new QNm("var2", "m"));
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testParseVariablesWithSimpleRegex() throws Exception {
    testParse(
            "/a1/{  $n:var1\t=\t.+ }/a2/{$m:var2=[1-9][0-9]+}/a3",
            "/a1/(.+)/a2/([1-9][0-9]+)/a3", 5, TEN, new QNm("n:var1"),
            new QNm("m:var2"));
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testParseVariablesWithComplexRegex() throws Exception {
    testParse(
            "/a1/{  $n:var1\t=\t.+ }/a2/{ $m:var2 = [1-9][0-9]{3,10} }/a3",
            "/a1/(.+)/a2/([1-9][0-9]{3,10})/a3", 5, TEN, new QNm("n:var1"),
            new QNm("m:var2"));
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testParseVariablesWithGroupsInRegex() throws Exception {
    testParse(
            "/a1{$p=(/.*)?}",
            "/a1((/.*)?)", 1, ONE, new QNm("p"));
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testValuesNoMatch() throws Exception {
    testValues("/a1{$p=(/.*)?}", "/a1b/c", "p", null);
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testValuesNoTrailingSlash() throws Exception {
    testValues("/a1{$p=(/.*)?}", "/a1", "p", "");
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testValuesWithTrailingSlash() throws Exception {
    testValues("/a1{$p=(/.*)?}", "/a1/", "p", "/");
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testValuesMatched() throws Exception {
    testValues("/a1{$p=(/.*)?}", "/a1/b/c/d", "p", "/b/c/d");
  }

  /** Test.
   * @throws Exception exception */
  @Test public void testValuesMatchedSeveralVariables() throws Exception {
    testValues("/a1/{$l=(b|d)}/{$d=(0|((12)?3))}", "/a1/b/123", "l", "b");
    testValues("/a1/{$l=(b|d)}/{$d=(0|((12)?3))}", "/a1/b/123", "d", "123");
  }

  /**
   * Performs a test.
   * @param template template
   * @param path path
   * @param var variable
   * @param val value
   * @throws Exception arbitrary exception
   */
  private static void testValues(final String template, final String path, final String var,
      final String val) throws Exception {

    final QNmMap<String> actual = RestXqPathMatcher.parse(template, null).values(path);
    assertEquals(val, actual.get(new QNm(var)), "values differ");
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
    assertEquals(Arrays.asList(vars), p.varNames);
    assertEquals(segments, p.segments);
    assertEquals(variables, p.varsPos);
  }
}