package org.basex.query.func.archive;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.io.*;
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
