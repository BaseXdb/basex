package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.http.MediaType;
import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ parameters.
 *
 * @author BaseX Team, BSD License
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
   * Kind of parameter in binding errors.
   * @throws Exception exception
   */
  @Test public void parameterKinds() throws Exception {
    register("declare %R:path('') %R:query-param('a', '{$a}') "
        + "function m:f($a as xs:integer) { $a };");
    assertContains(get(400, "", "a", "x"), "Query parameter 'a'");

    register("declare %R:path('') %R:POST %R:form-param('a', '{$a}') "
        + "function m:f($a as xs:integer) { $a };");
    assertContains(post(400, "a=x", MediaType.APPLICATION_X_WWW_FORM_URLENCODED, ""),
        "Form parameter 'a'");

    register("declare %R:path('') %R:header-param('X-Num', '{$a}') "
        + "function m:f($a as xs:integer) { $a };");
    assertContains(send(400, "GET", null, null, Map.of("X-Num", "x"), ""), "Header 'X-Num'");

    register("declare %R:path('') %R:cookie-param('num', '{$a}') "
        + "function m:f($a as xs:integer) { $a };");
    assertContains(send(400, "GET", null, null, Map.of("Cookie", "num=x"), ""), "Cookie 'num'");
  }

  /**
   * Uploaded files are bound as a map with file names and contents. Parts that outgrow the
   * spill threshold are covered by {@code PayloadTest}, as they cannot be provoked via HTTP.
   * @throws Exception exception
   */
  @Test public void multipartUpload() throws Exception {
    register("declare %R:path('') %R:POST %R:form-param('files', '{$files}') " +
        "function m:f($files) { string-join(map:for-each($files, fn($name, $content) { " +
        "$name || '=' || convert:binary-to-string($content) }), ',') };");

    final byte[] body = Token.token(
        "--bnd\r\nContent-Disposition: form-data; name=\"files\"; filename=\"a.txt\"\r\n\r\n" +
        "A\r\n" +
        "--bnd\r\nContent-Disposition: form-data; name=\"files\"; filename=\"b.txt\"\r\n\r\n" +
        "BB\r\n--bnd--\r\n");
    assertEquals("a.txt=A,b.txt=BB", send(200, "POST", new ArrayInput(body),
        new MediaType("multipart/form-data; boundary=bnd"), ""));
  }

  /**
   * Parameters that are required, but not supplied.
   * @throws Exception exception
   */
  @Test public void missingParameter() throws Exception {
    register("declare %R:path('') %R:query-param('a', '{$a}') "
        + "function m:f($a as xs:integer) { $a };");
    assertEquals("Query parameter 'a' is missing (expected type: xs:integer).", get(400, ""));
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
    get(400, "declare %R:path('') %R:query-param('a', '{$a}') " +
        "function m:f($a as item()) { () };", "?a=4&a=8");
  }
}
