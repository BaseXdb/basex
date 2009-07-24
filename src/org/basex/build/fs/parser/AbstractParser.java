package org.basex.build.fs.parser;

import java.io.IOException;
import java.nio.channels.FileChannel;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.DataType;
import org.basex.build.fs.parser.Metadata.Definition;
import org.basex.build.fs.parser.Metadata.Element;

/**
 * Abstract class for metadata extractors / file parsers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public abstract class AbstractParser {
  /** The type of the file. */
  private final Metadata.Type type;
  /** The format of the file (MIME type). */
  private final Metadata.MimeType format;

  /**
   * Returns the type of the adapter (e.g. Sound, Mail, Text, ...) as byte
   * array.
   * @return the type of the adapter.
   */
  public byte[] getType() {
    return type.get();
  }

  /**
   * Returns the format of the adapter (the MIME type) as byte array.
   * @return the format of the adapter.
   */
  public byte[] getFormat() {
    return format.get();
  }

  /*
   * Returns the type of the adapter (e.g. Sound, Mail, Text, ...) as string.
   * @return the type of the adapter.
  public String getTypeString() {
    return type.name();
  }
   */

  /*
   * Returns the format of the adapter (the MIME type) as string.
   * @return the format of the adapter.
  public String getFormatString() {
    return format.name();
  }
  */

  // ---------------------------------------------------------------------------
  // ----- constructor / abstract methods for parser implementations -----------
  // ---------------------------------------------------------------------------

  /**
   * Constructor for initializing a parser.
   * @param adapterType the type of the file.
   * @param adapterFormat the format of the file (MIME type).
   */
  protected AbstractParser(final Metadata.Type adapterType,
      final Metadata.MimeType adapterFormat) {
    type = adapterType;
    format = adapterFormat;
  }

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
   * Reads the metadata from a {@link BufferedFileChannel} and fires events for
   * each key/value pair.
   * </p>
   * @param bfc {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser} instance to fire events.
   * @throws IOException if any error occurs while reading from the file.
   * @see NewFSParser#metaEvent(Element, DataType, Definition, byte[], byte[])
   */
  public abstract void readMeta(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException;

  /**
   * <p>
   * Reads the textual content from a {@link FileChannel} and fires events.
   * </p>
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser} instance to write the content to.
   * @throws IOException if any error occurs while reading from the file.
   */
  public abstract void readContent(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException;
}
