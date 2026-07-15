package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the DBA editor query endpoints.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryTest extends DBATest {
  /**
   * Tests query evaluation.
   * @throws IOException I/O exception
   */
  @Test public void query() throws IOException {
    assertEquals("2", post("query", "1 + 1"));
    assertEquals("ok", post("query", "'ok'"));
  }

  /**
   * Tests the updating-query check.
   * @throws IOException I/O exception
   */
  @Test public void parse() throws IOException {
    assertEquals("true", post("parse", "delete node <a/>"));
    assertEquals("false", post("parse", "1"));
  }
}
