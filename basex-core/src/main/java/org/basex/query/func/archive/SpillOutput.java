package org.basex.query.func.archive;

import java.io.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryError;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Spill output stream.
 *
 * This class provides an output stream that buffers data in memory, then spills transparently to a
 * temporary file if the data exceeds the maximum array size or a given threshold.
 * The result can be retrieved as a binary item via the {@link #finish} method, which returns a lazy
 * reference to the temporary file if data was spilled, or an in-memory binary item otherwise. The
 * temporary file is registered with the query context's resources for automatic deletion when the
 * query finishes.
 *
 * This class is used by {@link ArchiveCreateFrom} and {@link ArchiveCreate}.
 *
 * @author BaseX Team, BSD License
 * @author Vincent Lizzi
 */
final class SpillOutput extends OutputStream {
  /** Query context for registering the temporary file on spill. */
  private final QueryContext qc;
  /** Threshold in bytes before spilling to disk. */
  private final int threshold;

  /** In-memory buffer ({@code null} after spilling). */
  private ArrayOutput array = new ArrayOutput();
  /** Disk output stream ({@code null} before spilling). */
  private OutputStream file;
  /** Temporary file ({@code null} before spilling). */
  private IOFile io;

  /**
   * Constructor.
   * @param qc query context
   */
  SpillOutput(final QueryContext qc) {
    this(qc, Array.MAX_SIZE);
  }

  /**
   * Constructor with an explicit spill threshold (used for testing).
   * @param qc query context
   * @param threshold spill threshold in bytes
   */
  SpillOutput(final QueryContext qc, final int threshold) {
    this.qc = qc;
    this.threshold = threshold;
  }

  @Override
  public void write(final int b) throws IOException {
    if(file == null && array.size() == threshold) spill();
    if(file != null) file.write(b);
    else array.write(b);
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    if(file == null && array.size() + len > threshold) spill();
    if(file != null) file.write(b, off, len);
    else array.write(b, off, len);
  }

  /**
   * Returns the result as a binary item: a lazy reference to the temporary file
   * if data was spilled, or an in-memory binary item otherwise.
   * Any buffered disk output is flushed first so that callers may read the temporary file
   * even if the stream has not yet been closed.
   * @param error error to raise if the temporary file cannot be read
   * @return binary item
   * @throws IOException I/O exception
   */
  B64 finish(final QueryError error) throws IOException {
    if(file != null) file.flush();
    return io != null ? new B64Lazy(io, error) : B64.get(array.finish());
  }

  /**
   * Closes the disk output stream if one was opened. The in-memory buffer and the
   * temporary file reference are intentionally preserved so that {@link #finish} can
   * still be called after {@code close}.
   */
  @Override
  public void close() throws IOException {
    if(file != null) {
      file.close();
      file = null;
    }
  }

  /**
   * Spills the in-memory buffer to a temporary file, registers it for deletion
   * when the query context closes, and switches subsequent writes to disk.
   * @throws IOException I/O exception
   */
  private void spill() throws IOException {
    io = new IOFile(File.createTempFile(Prop.NAME + '-', IO.TMPSUFFIX));
    qc.resources.index(TempFiles.class).add(io);
    file = new BufferOutput(io);
    file.write(array.buffer(), 0, (int) array.size());
    array = null;
  }
}
