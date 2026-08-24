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
        + "function m:f($x) { convert:binary-to-string($x) };", "", payload,
        MediaType.APPLICATION_OCTET_STREAM);
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
