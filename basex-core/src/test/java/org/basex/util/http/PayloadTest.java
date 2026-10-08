package org.basex.util.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.time.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.util.*;
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
   * XQuery input is returned as a string.
   * @throws Exception exception
   */
  @Test public void xquery() throws Exception {
    final MediaType type = new MediaType("application/xquery; charset=ISO-8859-1");
    assertFalse(Payload.binary(type));
    final Value value = Payload.value(new IOContent(new byte[] { '"', (byte) 0xE4, '"' }), type,
        OPTIONS);
    assertEquals("\"ä\"", ((Str) value).toJava());
  }

  /**
   * A small multipart file part is bound as an in-memory item.
   * @throws Exception exception
   */
  @Test public void multipartInMemory() throws Exception {
    try(QueryContext qc = new QueryContext(context)) {
      final B64 contents = (B64) files(new ArrayInput(FILE), qc, 1024).get(Str.get("a.bin"));
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
      final B64 contents = (B64) files(new ArrayInput(FILE), qc, 3).get(Str.get("a.bin"));
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
      final XQMap files = files(new ArrayInput(body), qc, 1024);
      assertEquals(2, files.structSize());
      assertArrayEquals(Token.token("--bnd-not-a-delimiter"),
          ((B64) files.get(Str.get("a.bin"))).binary(null));
      assertArrayEquals(Token.token("second"), ((B64) files.get(Str.get("b.bin"))).binary(null));
    }
  }

  /**
   * Binary parts of a multipart body are not decoded with the charset of the outer type.
   * @throws Exception exception
   */
  @Test public void multipartBinaryPart() throws Exception {
    final byte[] bin = { (byte) 0x89, 'P', 'N', 'G', (byte) 0xFF, 0, (byte) 0xC3 };
    final byte[] body = Token.concat(Token.token("--bnd\r\nContent-Type: image/png\r\n\r\n"), bin,
        Token.token("\r\n--bnd--\r\n"));
    final MediaType type = new MediaType("multipart/related; boundary=bnd; charset=UTF-8");
    final Value value = Payload.value(new IOContent(body), type, OPTIONS);
    assertArrayEquals(bin, ((B64) value).binary(null));
  }

  /**
   * Parameter values that are no tokens are quoted, and the parameter order is preserved.
   */
  @Test public void mediaTypeString() {
    final String string = "multipart/related; boundary=\"=-=-=\"; type=text/xml; start=a";
    assertEquals("multipart/related; boundary=\"=-=-=\"; type=\"text/xml\"; start=a",
        new MediaType(string).toString());
    assertEquals("text/plain; x=\"a\\\"b\"", new MediaType("text/plain; x=\"a\\\"b\"").toString());
  }

  /**
   * A large multipart form body is parsed quickly from an unbuffered file stream.
   * @throws Exception exception
   */
  @Test public void multipartLargeFile() throws Exception {
    final String lines = ("x".repeat(998) + "\r\n").repeat(1 << 14);
    final IOFile file = new IOFile(File.createTempFile("basex-test-", IO.TMPSUFFIX));
    try {
      file.write("--bnd\r\nContent-Disposition: form-data; name=\"files\"; " +
          "filename=\"a.bin\"\r\n\r\n" + lines + "--bnd--\r\n");
      try(QueryContext qc = new QueryContext(context);
          InputStream is = file.inputStream()) {
        final XQMap files = assertTimeout(Duration.ofSeconds(5),
            () -> files(is, qc, SpillOutput.THRESHOLD));
        final B64 contents = (B64) files.get(Str.get("a.bin"));
        assertEquals(lines.length() - 2, contents.binary(null).length);
      }
    } finally {
      assertTrue(file.delete());
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
  private static XQMap files(final InputStream body, final QueryContext qc, final int threshold)
      throws Exception {
    final Payload payload = new Payload(body, BodyMode.PARSE, null, OPTIONS);
    final MediaType type = new MediaType("multipart/form-data; boundary=bnd");
    final TempFiles temp = qc.resources.index(TempFiles.class);
    return (XQMap) payload.multiForm(type, temp, threshold).get(Str.get("files"));
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
