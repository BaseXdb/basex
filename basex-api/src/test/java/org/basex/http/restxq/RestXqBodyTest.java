package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.util.http.MediaType;
import org.junit.jupiter.api.*;

/**
 * This test checks how request bodies are bound.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqBodyTest extends RestXqTest {
  /**
   * A binary body is bound as a binary item and is returned unchanged.
   * Bodies that outgrow the maximum array size are spilled to disk; this is covered by
   * {@code SpillOutputTest} and {@code PayloadTest}, as it cannot be provoked via HTTP.
   * @throws IOException I/O exception
   */
  @Test public void binaryBody() throws IOException {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < 1000; i++) sb.append("0123456789");
    final String payload = sb.toString();

    post(payload, "declare %R:POST('{$x}') %R:path('') "
        + "function m:f($x) { bin:decode-string($x) };", "", payload,
        MediaType.APPLICATION_OCTET_STREAM);
  }

  /**
   * A forwarded response body is returned unchanged, whether it is streamed or not.
   * @throws IOException I/O exception
   */
  @Test public void forwardedBody() throws IOException {
    final String body = "http:get('" + HTTP_ROOT + "src', { 'response-body': 'binary' })?body";
    final String text = "fetch:text('" + HTTP_ROOT + "src')";
    register("declare %R:path('src') %output:method('basex') "
        + "function m:src() { bin:encode-string('abc') };");
    add("declare %R:path('fwd/{$n}') %output:method('basex') function m:fwd($n) { "
        + "let $b := " + body + " return switch($n) { case '1' return $b "
        + "case '2' return ($b, $b) default return $b[bin:length(.) = 3] } };");
    get("abc", "fwd/1");
    get("abc\nabc", "fwd/2");
    get("abc", "fwd/3");

    // REST: last item of a query result
    for(final String query : new String[] { body, text }) {
      get("abc", "rest", "query", query);
      get("abc\nabc", "rest", "query", "let $v := " + query + " return ($v, $v)");
    }
  }

  /**
   * Bodies that cannot be parsed are rejected as bad requests.
   * @throws IOException I/O exception
   */
  @Test public void invalidBody() throws IOException {
    register("declare %R:POST('{$x}') %R:path('') function m:f($x) { $x };");
    assertContains(post(400, "no xml", MediaType.APPLICATION_XML, ""),
        "Body cannot be parsed as application/xml");
    assertEquals("Body cannot be parsed as application/json: " +
        "[FOJS0001] (1:2): Expected: string, found: o.",
        post(400, "{oops", MediaType.APPLICATION_JSON, ""));
  }
}
