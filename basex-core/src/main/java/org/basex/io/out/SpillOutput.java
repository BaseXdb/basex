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
 * If a registry for temporary files is supplied, the temporary file is deleted when the query
 * finishes; otherwise, the caller takes ownership of the returned {@link IOFile}.
 *
 * @author BaseX Team, BSD License
 * @author Vincent Lizzi
 */
public final class SpillOutput extends OutputStream {
  /** Default threshold in bytes. */
  public static final int THRESHOLD = 100_000_000;

  /** Registry for temporary files (can be {@code null}). */
  private final TempFiles temp;
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
   * @param temp registry for temporary files (can be {@code null})
   */
  public SpillOutput(final TempFiles temp) {
    this(temp, THRESHOLD);
  }

  /**
   * Constructor with an explicit spill threshold.
   * @param temp registry for temporary files (can be {@code null})
   * @param threshold spill threshold in bytes
   */
  public SpillOutput(final TempFiles temp, final int threshold) {
    this.temp = temp;
    this.threshold = threshold;
  }

  /**
   * Reads an input stream, spilling to a temporary file if it outgrows the default threshold.
   * The stream is not closed.
   * @param is input stream
   * @param temp registry for temporary files (can be {@code null})
   * @return input reference
   * @throws IOException I/O exception
   */
  public static IO read(final InputStream is, final TempFiles temp) throws IOException {
    try(SpillOutput so = new SpillOutput(temp)) {
      try {
        is.transferTo(so);
        return so.finish();
      } catch(final Throwable th) {
        so.discard();
        throw th;
      }
    }
  }

  /**
   * Reads the contents of an input, unless it is a local file or in-memory content.
   * @param input input
   * @param temp registry for temporary files (can be {@code null})
   * @return input reference
   * @throws IOException I/O exception
   */
  public static IO read(final IO input, final TempFiles temp) throws IOException {
    if(input instanceof IOFile || input instanceof IOContent) return input;
    try(InputStream is = input.inputStream()) {
      return read(is, temp);
    }
  }

  @Override
  public void write(final int b) throws IOException {
    if(file == null && array.size() >= threshold) spill();
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
    if(temp != null) temp.add(io);
    file = new BufferOutput(io);
    file.write(array.buffer(), 0, (int) array.size());
    array = null;
  }
}
