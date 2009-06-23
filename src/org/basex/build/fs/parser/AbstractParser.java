package org.basex.build.fs.parser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.basex.BaseX;
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

  // ---------------------------------------------------------------------------
  // ----- static stuff --------------------------------------------------------
  // ---------------------------------------------------------------------------

  /** Registry for MetadataAdapter implementations. */
  static final Map<String, Class<? extends AbstractParser>> REGISTRY;

  static {
    REGISTRY = new HashMap<String, Class<? extends AbstractParser>>();
    loadParsers();
  }

  /**
   * Searches in the actual directory for parser implementations and loads the
   * java classes.
   */
  private static void loadParsers() {
    String pkg = AbstractParser.class.getPackage().getName();
    String pkgPath = pkg.replace('.', '/');
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL pkgUrl = cl.getResource(pkgPath);
    File packageDir = new File(pkgUrl.getPath());
    for(File f : packageDir.listFiles()) {
      String fname = f.getName();
      if(fname.endsWith("Parser.class")
          && !fname.equals("AbstractParser.class")) {
        String name = fname.substring(0, fname.length() - 6);
        String clazz = pkg + "." + name;
        try {
          int numParsers = REGISTRY.size();
          Class.forName(clazz);
          if(REGISTRY.size() == numParsers + 1) {
            BaseX.debug("Successfully loaded parser: " + clazz);
          }
        } catch(ClassNotFoundException e) {
          BaseX.debug("Failed to load parser: " + clazz);
        }
      }
    }
  }

  /**
   * Returns the classes of all registered MetadataAdapter implementations.
   * @return the classes of all registered adapters.
   */
  public static Map<String, Class<? extends AbstractParser>> getAdapters() {
    return REGISTRY;
  }

  // ---------------------------------------------------------------------------
  // ----- public implemented methods ------------------------------------------
  // ---------------------------------------------------------------------------

  /** The type of the file. */
  private final Metadata.Type type;
  /** The format of the file (MIME type). */
  private final Metadata.MimeType format;
  /** Valid filename suffixes for the parser. */
  private final Set<String> suffixes;

  /**
   * Tests if the file type is supported and the parser is able to read the
   * file.
   * @param f the file to check.
   * @return true if the file is supported
   * @throws IOException if an error occurs while reading the file.
   */
  public boolean isValid(final File f) throws IOException {
    String name = f.getName();
    int dotPos = name.lastIndexOf('.');
    String suffix = name.substring(dotPos + 1).toLowerCase();
    if(suffixes.contains(suffix)) {
      FileChannel fc = new RandomAccessFile(f, "r").getChannel();
      return check(fc, fc.size());
    }
    return false;
  }

  /**
   * Returns all supported file suffixes (lower case).
   * @return supported file suffixes.
   */
  public Set<String> getSuffixes() {
    return suffixes;
  }

  /**
   * Returns the type of the adapter (e.g. Sound, Mail, Text, ...) as string.
   * @return the type of the adapter.
   */
  public String getTypeString() {
    return type.name();
  }

  /**
   * Returns the type of the adapter (e.g. Sound, Mail, Text, ...) as byte
   * array.
   * @return the type of the adapter.
   */
  public byte[] getType() {
    return type.get();
  }

  /**
   * Returns the format of the adapter (the MIME type) as string.
   * @return the format of the adapter.
   */
  public String getFormatString() {
    return format.name();
  }

  /**
   * Returns the format of the adapter (the MIME type) as byte array.
   * @return the format of the adapter.
   */
  public byte[] getFormat() {
    return format.get();
  }

  // ---------------------------------------------------------------------------
  // ----- constructor / abstract methods for parser implementations -----------
  // ---------------------------------------------------------------------------

  /**
   * Constructor for initializing a parser.
   * @param validSuffixes filename suffixes that are allowed for this parser
   *          implementation.
   * @param adapterType the type of the file.
   * @param adapterFormat the format of the file (MIME type).
   */
  protected AbstractParser(final Set<String> validSuffixes,
      final Metadata.Type adapterType, final Metadata.MimeType adapterFormat) {
    suffixes = validSuffixes;
    type = adapterType;
    format = adapterFormat;
  }

  /**
   * <p>
   * Checks if there is a File in correct format and can be read by the parser.
   * Checks e.g. header bytes. {@link FileChannel#position()} must point to the
   * start of the file header.
   * </p>
   * <p>
   * For regular files, the FileChannel position is zero and the limit equals to
   * the length of the file. For a file that is inside another one (e.g. a
   * picture inside the ID3v2 tag of a mp3-file) the FileChannel position must
   * point to the beginning of this "inner" file and the limit must be the
   * lenght of the "inner" file.
   * </p>
   * 
   * @param fc the {@link FileChannel} to read from.
   * @param limit maximum number of bytes to read.
   * @return true if the file is supported.
   * @throws IOException if an error occurs while reading from the file.
   */
  abstract boolean check(final FileChannel fc, final long limit)
      throws IOException;

  /**
   * <p>
   * Reads the metadata from a {@link FileChannel} and fires events for each
   * key/value pair. {@link FileChannel#position()} must point to the start of
   * the file header.
   * </p>
   * <p>
   * For regular files, the FileChannel position has to be zero and the limit
   * equals to the length of the file. For a file that is inside another one
   * (e.g. a picture inside the ID3v2 tag of a mp3-file) the FileChannel
   * position must point to the beginning of this "inner" file and the limit
   * must be the lenght of the "inner" file.
   * </p>
   * @param fc {@link FileChannel} to read from.
   * @param limit maximum number of bytes to read.
   * @param fsParser the {@link NewFSParser} instance to fire events.
   * @throws IOException if any error occurs while reading from the file.
   * @see NewFSParser#metaEvent(Element, DataType, Definition, byte[], byte[])
   */
  public abstract void readMeta(final FileChannel fc, final long limit,
      final NewFSParser fsParser) throws IOException;

  /**
   * <p>
   * Reads the textual content from a {@link FileChannel} and fires events.
   * {@link FileChannel#position()} must point to the start of the file header.
   * </p>
   * <p>
   * For regular files, the FileChannel position has to be zero and the limit
   * equals to the length of the file. For a file that is inside another one
   * (e.g. a picture inside the ID3v2 tag of a mp3-file) the FileChannel
   * position must point to the beginning of this "inner" file and the limit
   * must be the lenght of the "inner" file.
   * </p>
   * @param fc the {@link FileChannel} to read from.
   * @param limit maximum number of bytes to read.
   * @param fsParser the {@link NewFSParser} instance to write the content to.
   * @throws IOException if any error occurs while reading from the file.
   */
  public abstract void readContent(final FileChannel fc, final long limit,
      final NewFSParser fsParser) throws IOException;
}
