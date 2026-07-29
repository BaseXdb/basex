package org.basex.io.out;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link SpillOutput}.
 *
 * @author BaseX Team, BSD License
 * @author Vincent Lizzi
 */
public final class SpillOutputTest extends SandboxTest {
  /** Bytes to be returned before a stream fails; must exceed the default spill threshold. */
  private static final long SPILLED = 101_000_000;

  /**
   * Small data stays in memory: result is an in-memory binary item, content is correct.
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  @Test public void inMemoryPath() throws IOException, QueryException {
    final byte[] data = { 1, 2, 3 };
    try(QueryContext qc = new QueryContext(context);
        SpillOutput so = new SpillOutput(qc, 1024)) {
      so.write(data);
      final B64 result = so.finish(QueryError.ARCHIVE_ERROR_X);
      assertFalse(result instanceof B64Lazy, "expected in-memory item");
      assertArrayEquals(data, result.binary(null));
    }
  }

  /**
   * Data exceeding the threshold spills to disk: result is a lazy reference, content is correct.
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  @Test public void spillPath() throws IOException, QueryException {
    final byte[] data = { 10, 20, 30, 40, 50 };
    try(QueryContext qc = new QueryContext(context);
        SpillOutput so = new SpillOutput(qc, 3)) {
      so.write(data);
      final B64 result = so.finish(QueryError.ARCHIVE_ERROR_X);
      assertTrue(result instanceof B64Lazy, "expected lazy (spilled) item");
      assertArrayEquals(data, result.binary(null));
    }
  }

  /**
   * Temp file is deleted when the query context closes.
   * @throws IOException I/O exception
   */
  @Test public void tempFileDeletedOnQueryClose() throws IOException {
    final File tmpDir = new File(Prop.TEMPDIR);
    final int before = countTempFiles(tmpDir);

    try(QueryContext qc = new QueryContext(context)) {
      try(SpillOutput so = new SpillOutput(qc, 0)) {
        so.write(new byte[] { 1, 2, 3 });
        so.finish(QueryError.ARCHIVE_ERROR_X);
      }
      assertEquals(before + 1, countTempFiles(tmpDir), "temp file should exist while qc is open");
    }
    assertEquals(before, countTempFiles(tmpDir), "temp file should be deleted after qc closes");
  }

  /**
   * Without a query context, the caller takes ownership of the temporary file.
   * @throws IOException I/O exception
   */
  @Test public void unregisteredTempFile() throws IOException {
    final IO io;
    try(SpillOutput so = new SpillOutput(null, 0)) {
      so.write(new byte[] { 1, 2, 3 });
      io = so.finish();
    }
    assertTrue(io instanceof IOFile, "expected temporary file");
    assertTrue(io.exists(), "temp file should not be deleted by the stream");
    assertTrue(((IOFile) io).delete());
  }

  /**
   * A stream is read completely, whether or not it spills.
   * @throws IOException I/O exception
   */
  @Test public void readStream() throws IOException {
    final byte[] data = { 1, 2, 3 };
    try(QueryContext qc = new QueryContext(context)) {
      assertArrayEquals(data, SpillOutput.read(new ArrayInput(data), qc).read());
    }
  }

  /**
   * A failed transfer discards the spilled file before the query context is closed.
   */
  @Test public void readStreamFails() {
    final File tmpDir = new File(Prop.TEMPDIR);
    final int before = countTempFiles(tmpDir);
    try(QueryContext qc = new QueryContext(context)) {
      assertThrows(IOException.class, () -> SpillOutput.read(failing(), qc));
      assertEquals(before, countTempFiles(tmpDir), "spilled file should be discarded at once");
    }
  }

  /**
   * A failed transfer discards the spilled file even without a query context,
   * where nothing else would ever delete it.
   */
  @Test public void readStreamFailsUnregistered() {
    final File tmpDir = new File(Prop.TEMPDIR);
    final int before = countTempFiles(tmpDir);
    assertThrows(IOException.class, () -> SpillOutput.read(failing(), null));
    assertEquals(before, countTempFiles(tmpDir), "spilled file should be discarded");
  }

  /**
   * Calling close twice does not throw.
   * @throws IOException I/O exception
   */
  @Test public void closeIsIdempotent() throws IOException {
    try(QueryContext qc = new QueryContext(context)) {
      try(SpillOutput so = new SpillOutput(qc, 0)) {
        so.write(new byte[] { 1 });
        so.close();
        assertDoesNotThrow(so::close);
      }
    }
  }

  /**
   * Returns a stream that yields enough data to be spilled, and then fails.
   * @return input stream
   */
  private static InputStream failing() {
    return new InputStream() {
      /** Number of returned bytes. */
      private long total;

      @Override public int read() {
        return -1;
      }

      @Override public int read(final byte[] b, final int off, final int len) throws IOException {
        if(total > SPILLED) throw new IOException("simulated abort");
        total += len;
        return len;
      }
    };
  }

  /**
   * Counts BaseX temporary files in a directory.
   * @param dir directory to check
   * @return number of matching files
   */
  private static int countTempFiles(final File dir) {
    final File[] files = dir.listFiles(
        f -> f.getName().startsWith(Prop.NAME + '-') &&
             f.getName().endsWith(IO.TMPSUFFIX));
    return files == null ? 0 : files.length;
  }
}
