package org.basex.query.func.archive;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryError;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * An output stream that buffers data in memory up to a threshold derived from
 * available heap, then spills transparently to a temporary file.
 * The threshold uses the same formula as {@code Add#cache}: half of
 * {@code (maxMemory - freeMemory)}, capped at the maximum array size.
 *
 * @author BaseX Team, BSD License
 */
final class SpillOutput extends OutputStream {
  /** In-memory buffer. */
  private byte[] buffer = new byte[Array.INITIAL_CAPACITY];
  /** Number of bytes written to the in-memory buffer. */
  private int bufSize;
  /** Disk output stream (null before spilling). */
  private FileOutputStream disk;
  /** Temporary file (null before spilling). */
  private IOFile tmpFile;
  /** Threshold in bytes before spilling to disk. */
  private final long threshold;
  /** Query context for registering the temporary file on spill. */
  private final QueryContext qc;

  /**
   * Constructor. Computes the spill threshold from current heap availability.
   * @param qc query context
   */
  SpillOutput(final QueryContext qc) {
    this.qc = qc;
    final Runtime rt = Runtime.getRuntime();
    threshold = Math.min((rt.maxMemory() - rt.freeMemory()) / 2, Array.MAX_SIZE);
  }

  @Override
  public void write(final int b) throws IOException {
    if(disk == null && bufSize + 1 > threshold) spill();
    if(disk != null) {
      disk.write(b);
    } else {
      if(bufSize == buffer.length) buffer = Arrays.copyOf(buffer, Array.newCapacity(bufSize));
      buffer[bufSize++] = (byte) b;
    }
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    if(disk == null && (long) bufSize + len > threshold) spill();
    if(disk != null) {
      disk.write(b, off, len);
    } else {
      final int newSize = bufSize + len;
      if(newSize > buffer.length)
        buffer = Arrays.copyOf(buffer, Math.max(Array.newCapacity(buffer.length), newSize));
      System.arraycopy(b, off, buffer, bufSize, len);
      bufSize = newSize;
    }
  }

  /**
   * Returns the result as a binary item: a lazy reference to the temporary file
   * if data was spilled, or an in-memory binary item otherwise.
   * @param error error to raise if the temporary file cannot be read
   * @return binary item
   */
  B64 result(final QueryError error) {
    if(tmpFile != null) return new B64Lazy(tmpFile, error);
    return B64.get(bufSize == 0 ? Token.EMPTY : bufSize == buffer.length ? buffer :
        Arrays.copyOf(buffer, bufSize));
  }

  @Override
  public void close() throws IOException {
    if(disk != null) disk.close();
  }

  /**
   * Spills the in-memory buffer to a temporary file, registers it for deletion
   * when the query context closes, and switches subsequent writes to disk.
   * @throws IOException I/O exception
   */
  private void spill() throws IOException {
    tmpFile = new IOFile(File.createTempFile(Prop.NAME + '-', IO.TMPSUFFIX));
    qc.resources.index(TempFiles.class).add(tmpFile);
    disk = tmpFile.outputStream();
    disk.write(buffer, 0, bufSize);
    buffer = null;
  }
}
