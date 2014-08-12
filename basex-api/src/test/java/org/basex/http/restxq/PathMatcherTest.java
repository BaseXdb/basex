package org.basex.http.restxq;

import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.TEN;
import static org.junit.Assert.*;

import org.basex.query.value.item.QNm;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

public class PathMatcherTest
{
  @Test
  public void test_parse_empty_path() throws Exception {
    test_parse("", "/", 1, 0, ZERO);
  }

  @Test
  public void test_parse_simple_path() throws Exception {
    test_parse("/a/b/c#xyz", "/a/b/c#xyz", 10, 3, ZERO);
  }

  @Test
  public void test_parse_simple_path_without_slash() throws Exception {
    test_parse("a/b/c#xyz", "/a/b/c#xyz", 10, 3, ZERO);
  }

  @Test
  public void test_parse_regex_escape() throws Exception {
    test_parse("/a/b/c-d.f#xyz", "/a/b/c\\-d\\.f#xyz", 14, 3, ZERO);
  }

  @Test
  public void test_parse_simple_variables() throws Exception {
    test_parse(
            "/a1/{ $var1 }/a2/{ $var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 11, 5, TEN,
            new QNm("var1"), new QNm("var2"));
  }

  @Test
  public void test_parse_qualified_variables() throws Exception {
    test_parse(
            "/a1/{ $n:var1 }/a2/{ $m:var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 11, 5, TEN,
            new QNm("n:var1"), new QNm("m:var2"));
  }

  @Ignore
  @Test
  public void test_parse_extended_qualified_variables() throws Exception {
    test_parse(
            "/a1/{ $n:var1 }/a2/{ $Q{m}var2 }/a3",
            "/a1/([^/]+?)/a2/([^/]+?)/a3", 11, 5, TEN,
            new QNm("n:var1"), new QNm("var2", "m"));
  }

  @Test
  public void test_parse_variables_with_simple_regex() throws Exception {
    test_parse(
            "/a1/{  $n:var1\t=\t.+ }/a2/{$m:var2=[1-9][0-9]+}/a3",
            "/a1/(.+)/a2/([1-9][0-9]+)/a3", 11, 5, TEN,
            new QNm("n:var1"), new QNm("m:var2"));
  }

  @Test
  public void test_parse_variables_with_complex_regex() throws Exception {
    test_parse(
            "/a1/{  $n:var1\t=\t.+ }/a2/{ $m:var2 = [1-9][0-9]{3,10} }/a3",
            "/a1/(.+)/a2/([1-9][0-9]{3,10})/a3", 11, 5, TEN,
            new QNm("n:var1"), new QNm("m:var2"));
  }

  private static void test_parse(String input, String regex, int literalCount, int segmentCount,
      BigInteger variablePositions, QNm... vars) throws Exception {
    PathMatcher p = PathMatcher.parse(input, null);
    assertEquals(regex, p.pattern.pattern());
    assertEquals(Arrays.asList(vars), p.variableNames);
    assertEquals(literalCount, p.literalCount);
    assertEquals(segmentCount, p.segmentCount);
    assertEquals(variablePositions, p.variablePositions);
  }
}