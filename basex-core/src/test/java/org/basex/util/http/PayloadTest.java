package org.basex.util.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link Payload}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PayloadTest extends SandboxTest {
  /** Main options. */
  private static final MainOptions OPTIONS = new MainOptions();
  /** Test payload. */
  private static final byte[] DATA = Token.token("binary body");
  /** Multipart form body with a single file part. */
  private static final byte[] FILE = Token.concat(Token.token("--bnd\r\nContent-Disposition: "
      + "form-data; name=\"files\"; filename=\"a.bin\"\r\n\r\n"), DATA,
      Token.token("\r\n--bnd--\r\n"));

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

  /**
   * A small multipart file part is bound as an in-memory item.
   * @throws Exception exception
   */
  @Test public void multipartInMemory() throws Exception {
    try(QueryContext qc = new QueryContext(context)) {
      final B64 contents = (B64) files(FILE, qc, 1024).get(Str.get("a.bin"));
      assertFalse(contents instanceof B64Lazy, "expected in-memory item");
      assertArrayEquals(DATA, contents.binary(null));
    }
  }

  /**
   * A multipart file part that outgrows the threshold is spilled to a temporary file.
   * @throws Exception exception
   */
  @Test public void multipartSpilled() throws Exception {
    final File tmp = new File(Prop.TEMPDIR);
    final int before = countTempFiles(tmp);
    try(QueryContext qc = new QueryContext(context)) {
      final B64 contents = (B64) files(FILE, qc, 3).get(Str.get("a.bin"));
      assertTrue(contents instanceof B64Lazy, "expected lazy (spilled) item");
      assertArrayEquals(DATA, contents.binary(null));
      assertEquals(before + 1, countTempFiles(tmp), "temp file should exist while qc is open");
    }
    assertEquals(before, countTempFiles(tmp), "temp file should be deleted after qc closes");
  }

  /**
   * Boundary delimiters tolerate trailing transport-padding, and a line that merely starts with
   * the boundary is content.
   * @throws Exception exception
   */
  @Test public void multipartBoundary() throws Exception {
    final byte[] body = Token.token(
        "--bnd\r\nContent-Disposition: form-data; name=\"files\"; filename=\"a.bin\"\r\n\r\n" +
        "--bnd-not-a-delimiter\r\n" +
        "--bnd \t\r\nContent-Disposition: form-data; name=\"files\"; filename=\"b.bin\"\r\n\r\n" +
        "second\r\n--bnd--  \r\n");
    try(QueryContext qc = new QueryContext(context)) {
      final XQMap files = files(body, qc, 1024);
      assertEquals(2, files.structSize());
      assertArrayEquals(Token.token("--bnd-not-a-delimiter"),
          ((B64) files.get(Str.get("a.bin"))).binary(null));
      assertArrayEquals(Token.token("second"), ((B64) files.get(Str.get("b.bin"))).binary(null));
    }
  }

  /**
   * Parses a multipart form body and returns the map with its file parts.
   * @param body multipart form body
   * @param qc query context
   * @param threshold spill threshold in bytes
   * @return file names and contents
   * @throws Exception exception
   */
  private static XQMap files(final byte[] body, final QueryContext qc, final int threshold)
      throws Exception {
    final Payload payload = new Payload(new ArrayInput(body), true, null, OPTIONS);
    final MediaType type = new MediaType("multipart/form-data; boundary=bnd");
    return (XQMap) payload.multiForm(type, qc, threshold).get(Str.get("files"));
  }

  /**
   * Counts the temporary files in a directory.
   * @param dir directory
   * @return number of temporary files
   */
  private static int countTempFiles(final File dir) {
    final File[] files = dir.listFiles(f -> f.getName().startsWith(Prop.NAME + '-') &&
        f.getName().endsWith(IO.TMPSUFFIX));
    return files == null ? 0 : files.length;
  }
}
