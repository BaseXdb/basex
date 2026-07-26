package org.basex.util.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link Payload}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PayloadTest {
  /** Main options. */
  private static final MainOptions OPTIONS = new MainOptions();
  /** Test payload. */
  private static final byte[] DATA = Token.token("binary body");

  /**
   * File-based binary input is referenced, not materialized.
   * @throws Exception exception
   */
  @Test public void lazyBinary() throws Exception {
    final IOFile file = new IOFile(File.createTempFile("basex-test-", IO.TMPSUFFIX));
    try {
      file.write(DATA);
      final Value value = Payload.value(file, MediaType.APPLICATION_OCTET_STREAM, OPTIONS);
      assertTrue(value instanceof B64Lazy, "expected lazy item");
      assertFalse(((B64Lazy) value).isCached(), "body must not be materialized");
      assertArrayEquals(DATA, ((B64) value).binary(null));
    } finally {
      assertTrue(file.delete());
    }
  }

  /**
   * In-memory binary input yields an in-memory item.
   * @throws Exception exception
   */
  @Test public void eagerBinary() throws Exception {
    final Value value = Payload.value(new IOContent(DATA), MediaType.APPLICATION_OCTET_STREAM,
        OPTIONS);
    assertFalse(value instanceof B64Lazy, "expected in-memory item");
    assertArrayEquals(DATA, ((B64) value).binary(null));
  }

  /**
   * File-based text input is materialized as a string.
   * @throws Exception exception
   */
  @Test public void text() throws Exception {
    final IOFile file = new IOFile(File.createTempFile("basex-test-", IO.TMPSUFFIX));
    try {
      file.write(DATA);
      final Value value = Payload.value(file, MediaType.TEXT_PLAIN, OPTIONS);
      assertArrayEquals(DATA, ((Str) value).string(null));
    } finally {
      assertTrue(file.delete());
    }
  }
}
