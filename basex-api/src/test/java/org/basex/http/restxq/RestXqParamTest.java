package org.basex.http.restxq;

import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ parameters.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class RestXqParamTest extends RestXqTest {
  /**
   * Query parameters.
   * @throws Exception exception
   */
  @Test public void queryParams() throws Exception {
    get("1", "declare %R:path('') %R:query-param('a', '{$v}') " +
        "function m:f($v) { $v };", "?a=1");
    get("2", "declare %R:path('') %R:query-param('a', '{$a}') " +
        "function m:f($a) { $a * 2 };", "?a=1");
    get("2", "declare %R:path('') %R:query-param('a', '{$a}') " +
        "function m:f($a as xs:integer*) { count($a) };", "?a=4&a=8");
    get("3", "declare %R:path('') %R:query-param('a', '{$v}', 3) " +
        "function m:f($v) { $v };", "");
    get("2", "declare %R:path('') %R:query-param('a', '{$v}', 4, 8) " +
        "function m:f($v) { count($v) };", "");
    get("6", "declare %R:path('') %R:query-param('a', '{$a}') %R:query-param('b', '{$b}') " +
        "function m:f($a, $b) { $a * $b };", "?a=2&b=3");

    // missing assignment: default value is empty sequence
    get("0", "declare %R:path('') %R:query-param('a', '{$v}') " +
            "function m:f($v) { count($v) };", "");
  }

  /**
   * Erroneous query parameters.
   * @throws Exception exception
   */
  @Test public void queryParamsErrors() throws Exception {
    // missing variable declaration
    get(500, "declare %R:path('') %R:query-param('a', '{$a}') function m:f() { 1 };", "?a=2");
    // variable is specified more than once
    get(500, "declare %R:path('') %R:query-param('a', '{$a}') %R:query-param('a', '{$a}') " +
        "function m:f($a) { $a };", "?a=2");
    // parameter is no string
    get(500, "declare %R:path('') %R:query-param(1, '{$a}') function m:f($a) { $a };", "?a=2");
    // invalid path template
    get(500, "declare %R:path('') %R:query-param('a', '$a') function m:f($a) { $a };", "?a=2");
    // invalid type cardinality
    get(500, "declare %R:path('') %R:query-param('a', '{$a}') " +
        "function m:f($a as item()) { () };", "?a=4&a=8");
  }
}
