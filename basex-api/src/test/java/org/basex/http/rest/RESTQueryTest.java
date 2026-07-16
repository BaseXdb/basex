package org.basex.http.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.io.in.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API and the QUERY method.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RESTQueryTest extends RESTTest {
  /**
   * QUERY Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test public void query() throws IOException {
    assertEquals("123", query(200, "123"));
    assertEquals("<a/>", query(200, "<a/>"));
  }

  /**
   * QUERY Test: bind an external variable.
   * @throws IOException I/O exception
   */
  @Test public void queryVariable() throws IOException {
    assertEquals("123", query(200, "declare variable $a as xs:integer external; $a", "a", "123"));
  }

  /**
   * QUERY Test: assign a serialization parameter.
   * @throws IOException I/O exception
   */
  @Test public void queryParameter() throws IOException {
    assertEquals("1", query(200, "<a>1</a>", "method", "text"));
  }

  /**
   * QUERY Test: execute buggy query.
   * @throws IOException I/O exception
   */
  @Test public void queryErr() throws IOException {
    query(500, "(");
  }

  /**
   * Executes the specified QUERY request and returns the result.
   * @param status status code to check
   * @param query query to be executed
   * @param params query parameters (keys and values)
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  private static String query(final int status, final String query, final Object... params)
      throws IOException {
    return send(status, "QUERY", new ArrayInput(query), null, "", params).replaceAll("\\r", "");
  }
}
