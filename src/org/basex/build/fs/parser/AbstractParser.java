package org.basex.build.fs.parser;

import java.io.IOException;
import java.nio.channels.FileChannel;

import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.MetaStore;

/**
 * Abstract class for metadata extractors / file parsers.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public abstract class AbstractParser {

  /** Metadata storage. */
  protected MetaStore meta = new MetaStore();

  /**
   * <p>
   * Reads the metadata from a {@link BufferedFileChannel} and stores the
   * metadata information as key-value pairs to the xml document.
   * </p>
   * @param bfc {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser} instance to write the data to.
   * @throws IOException if any error occurs while reading from the file.
   */
  public final void readMeta(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException {
    meta.clear();
    meta(bfc, parser);
    meta.write(parser);
  }

  /**
   * <p>
   * Reads the textual content from a {@link FileChannel} and fires events.
   * </p>
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser} instance to write the content to.
   * @throws IOException if any error occurs while reading from the file.
   */
  public final void readContent(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException {
    content(bfc, parser);
  }

  /**
   * <p>
   * Reads metadata and content from a {@link BufferedFileChannel}.
   * </p>
   * <p>
   * Does the same as the two method calls:
   * 
   * <pre>
   * {@link #readMeta(BufferedFileChannel, NewFSParser)};
   * {@link #content(BufferedFileChannel, NewFSParser)};
   * </pre>
   * 
   * but is potentially much more efficient (depends on the actual parser
   * implementation).
   * </p>
   * <p>
   * The order of XML elements may differ from the two separate method calls.
   * </p>
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser} instance to write the content to.
   * @throws IOException if any error occurs while reading from the file.
   */
  public final void readMetaAndContent(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException {
    meta.clear();
    if(!metaAndContent(bfc, parser)) {
      readMeta(bfc, parser);
      bfc.reset();
      readContent(bfc, parser);
    } else meta.write(parser);
  }

  // ---------------------------------------------------------------------------
  // ----- abstract methods for parser implementations -------------------------
  // ---------------------------------------------------------------------------

  /**
   * <p>
   * Checks if there is a File in correct format and can be read by the parser.
   * Checks e.g. header bytes.
   * </p>
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @return true if the file is supported.
   * @throws IOException if an error occurs while reading from the file.
   */
  public abstract boolean check(final BufferedFileChannel bfc)
      throws IOException;

  /**
   * <p>
   * Reads metadata from a {@link BufferedFileChannel} and stores the metadata
   * information as key-value pairs in the xml document.
   * </p>
   * @param bfc {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser} instance to fire events.
   * @throws IOException if any error occurs while reading from the file.
   */
  protected abstract void meta(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException;

  /**
   * <p>
   * Reads the textual content from a {@link FileChannel} and fires events.
   * </p>
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser} instance to write the content to.
   * @throws IOException if any error occurs while reading from the file.
   */
  protected abstract void content(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException;

  /**
   * <p>
   * Reads metadata and content from a {@link BufferedFileChannel} and fires
   * events for each key-value pair.
   * </p>
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser} instance to write the content to.
   * @return true if an optimized parsing method for metadata and content is
   *         implemented, false if the standard methods should be used.
   * @throws IOException if any error occurs while reading from the file.
   */
  protected abstract boolean metaAndContent(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException;
}
