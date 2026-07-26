package org.basex.io.out;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Spill output stream.
 *
 * This class provides an output stream that buffers data in memory, then spills transparently to a
 * temporary file if the data exceeds a default or explicitly supplied threshold.
 * The result can be retrieved as an {@link IO} reference via {@link #finish()}, or as a binary item
 * via {@link #finish(QueryError)}, which returns a lazy reference to the temporary file if data was
 * spilled, or an in-memory binary item otherwise.
 * If a query context is supplied, the temporary file is registered with its resources and deleted
 * when the query finishes; otherwise, the caller takes ownership of the returned {@link IOFile}.
 *
 * @author BaseX Team, BSD License
 * @author Vincent Lizzi
 */
public final class SpillOutput extends OutputStream {
  /** Threshold in bytes. */
  private static final int THRESHOLD = 100_000_000;

  /** Query context for registering the temporary file on spill (can be {@code null}). */
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
   * @param qc query context (can be {@code null})
   */
  public SpillOutput(final QueryContext qc) {
    this(qc, THRESHOLD);
  }

  /**
   * Constructor with an explicit spill threshold.
   * @param qc query context (can be {@code null})
   * @param threshold spill threshold in bytes
   */
  public SpillOutput(final QueryContext qc, final int threshold) {
    this.qc = qc;
    this.threshold = threshold;
  }

  /**
   * Reads an input stream, spilling to a temporary file if it outgrows the default threshold.
   * The stream is not closed.
   * @param is input stream
   * @param qc query context (can be {@code null})
   * @return input reference
   * @throws IOException I/O exception
   */
  public static IO read(final InputStream is, final QueryContext qc) throws IOException {
    try(SpillOutput so = new SpillOutput(qc)) {
      try {
        is.transferTo(so);
        return so.finish();
      } catch(final Throwable th) {
        Util.debug(th);
        so.discard();
        throw th;
      }
    }
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
   * Returns the result: a reference to the temporary file if data was spilled, or the in-memory
   * contents otherwise. Any buffered disk output is flushed first so that callers may read the
   * temporary file even if the stream has not yet been closed.
   * @return input reference
   * @throws IOException I/O exception
   */
  public IO finish() throws IOException {
    if(file != null) file.flush();
    return io != null ? io : new IOContent(array.finish());
  }

  /**
   * Returns the result as a binary item: a lazy reference to the temporary file
   * if data was spilled, or an in-memory binary item otherwise.
   * @param error error to raise if the temporary file cannot be read
   * @return binary item
   * @throws IOException I/O exception
   */
  public B64 finish(final QueryError error) throws IOException {
    return B64.get(finish(), error);
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
   * Closes the disk output stream and deletes the temporary file if data was spilled.
   * The stream must be closed first: an open file cannot be deleted on Windows.
   */
  private void discard() {
    try {
      close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
    if(io != null) io.delete();
  }

  /**
   * Spills the in-memory buffer to a temporary file, registers it for deletion
   * when the query context closes, and switches subsequent writes to disk.
   * @throws IOException I/O exception
   */
  private void spill() throws IOException {
    io = new IOFile(File.createTempFile(Prop.NAME + '-', IO.TMPSUFFIX));
    if(qc != null) qc.resources.index(TempFiles.class).add(io);
    file = new BufferOutput(io);
    file.write(array.buffer(), 0, (int) array.size());
    array = null;
  }
}
