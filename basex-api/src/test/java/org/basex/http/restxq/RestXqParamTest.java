package org.basex.http.restxq;

import org.junit.*;

/**
 * This test contains RESTXQ parameters.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class RestXqParamTest extends RestXqTest {
  /**
   * Query parameters.
   * @throws Exception exception
   */
  @Test
  public void queryParams() throws Exception {
    // correct syntax
    get("declare %R:path('') %R:query-param('a','{$v}') " +
        "function m:f($v) {$v};", "?a=1", "1");
    get("declare %R:path('') %R:query-param('a','{$a}') " +
        "function m:f($a) {$a*2};", "?a=1", "2");
    get("declare %R:path('') %R:query-param('a','{$a}') " +
        "function m:f($a as xs:integer*) {count($a)};", "?a=4&a=8", "2");
    get("declare %R:path('') %R:query-param('a','{$v}',3) " +
        "function m:f($v) {$v};", "", "3");
    get("declare %R:path('') %R:query-param('a','{$v}',4,8) " +
        "function m:f($v) {count($v)};", "", "2");
    get("declare %R:path('') %R:query-param('a','{$a}') %R:query-param('b','{$b}') " +
        "function m:f($a,$b) {$a*$b};", "?a=2&b=3", "6");
    // missing assignment: default value is empty sequence
    get("declare %R:path('') %R:query-param('a','{$v}') " +
        "function m:f($v) {count($v)};", "", "0");
    // missing variable declaration
    getE("declare %R:path('') %R:query-param('a','{$a}') function m:f() {1};", "?a=2");
    // variable is specified more than once
    getE("declare %R:path('') %R:query-param('a','{$a}') %R:query-param('a','{$a}') " +
        "function m:f($a) {$a};", "?a=2");
    // parameter is no string
    getE("declare %R:path('') %R:query-param(1,'{$a}') function m:f($a) {$a};", "?a=2");
    // invalid path template
    getE("declare %R:path('') %R:query-param('a','$a') function m:f($a) {$a};", "?a=2");
    // invalid type cardinality
    getE("declare %R:path('') %R:query-param('a','{$a}') " +
        "function m:f($a as item()) {()};", "?a=4&a=8");
  }
}
