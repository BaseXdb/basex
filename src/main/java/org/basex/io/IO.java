package org.basex.io;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.basex.data.Data;
import org.basex.io.in.BufferInput;
import org.basex.util.Token;
import org.xml.sax.InputSource;

/**
 * Generic representation for inputs and outputs. The underlying source can
 * be a local file ({@link IOFile}), a URL ({@link IOUrl}) or a byte array
 * ({@link IOContent}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class IO {
  /** Database file suffix. */
  public static final String BASEXSUFFIX = ".basex";
  /** XQuery file suffix. */
  public static final String XQSUFFIX = ".xq";
  /** XML file suffix. */
  public static final String XMLSUFFIX = ".xml";
  /** ZIP file suffix. */
  public static final String ZIPSUFFIX = ".zip";
  /** CSV file suffix. */
  public static final String CSVSUFFIX = ".csv";
  /** Text file suffix. */
  public static final String TXTSUFFIX = ".txt";
  /** JSON file suffix. */
  public static final String JSONSUFFIX = ".json";
  /** GZIP file suffix. */
  public static final String GZSUFFIX = ".gz";
  /** File prefix. */
  public static final String FILEPREF = "file:";
  /** Date format which is appended to backups. */
  public static final SimpleDateFormat DATE =
    new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  /** Date pattern. */
  public static final String DATEPATTERN =
    "-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}";

  /** XQuery suffixes. */
  public static final String[] XQSUFFIXES =
    { XQSUFFIX, ".xqm", ".xqy", ".xql", ".xquery" };
  /** ZIP suffixes. */
  public static final String[] ZIPSUFFIXES =
    { ZIPSUFFIX, ".docx", ".pptx", ".xslx", ".odt", ".odp", ".ods", ".gz" };
  /** XML suffixes. */
  static final String[] XMLSUFFIXES =
    { XMLSUFFIX, ".xsl", ".xslt" };
  /** HTML suffixes. */
  public static final String[] HTMLSUFFIXES =
    { ".xhtml", ".html", ".htm" };

  /** Disk block/page size (default: 4096). */
  public static final int BLOCKSIZE = 1 << 12;
  /** Table node size power (default: 4). */
  public static final int NODEPOWER = 4;
  /** Table node size power (default: 4). */
  public static final int NODESIZE = 1 << NODEPOWER;
  /** Entries per block (default: 256). */
  public static final int ENTRIES = BLOCKSIZE >>> NODEPOWER;

  /** Maximum number of attributes (see bit layout in {@link Data} class). */
  public static final int MAXATTS = 0x1F;
  /** Offset for inlining numbers (see bit layout in {@link Data} class). */
  public static final long OFFNUM = 0x8000000000L;
  /** Offset for compressing texts (see bit layout in {@link Data} class). */
  public static final long OFFCOMP = 0x4000000000L;

  /** File path. The path uses forward slashes, no matter which OS is used. */
  String path;
  /** First call. */
  private boolean more;
  /** File name. */
  String name;

  /**
   * Protected constructor.
   * @param p path
   */
  IO(final String p) {
    init(p);
  }

  /**
   * Sets the file path and name.
   * @param p file path
   */
  final void init(final String p) {
    path = p;
    final String n = p.substring(p.lastIndexOf('/') + 1);
    // use current time if no name is given
    name = n.isEmpty() ? Long.toString(System.currentTimeMillis()) +
        XMLSUFFIX : n;
  }

  /**
   * <p>Returns an {@link IO} representation for the specified string. The type
   * of the returned {@link IO} instance is dynamically chosen; it depends
   * on the string value:</p>
   * <ul>
   * <li>{@link IOContent}: if the string starts with an angle bracket (&lt;)
   *   or if it is a {@code null} reference, it is interpreted as XML fragment
   *   and handled as byte array</li>
   * <li>{@link IOFile}: if the string starts with <code>file:</code>, or if it
   *   does not contain the substring <code>://</code>, it is interpreted as
   *   local file instance</li>
   * <li>{@link IOUrl}: otherwise, it is handled as URL</li>
   * </ul>
   * If the content of the string value is known in advance, it is advisable
   * to call the direct constructors of the correspondent sub class.
   *
   * @param source source string
   * @return IO reference
   */
  public static IO get(final String source) {
    if(source == null) return new IOContent(Token.EMPTY);
    final String s = source.trim();
    if(s.startsWith("<"))      return new IOContent(Token.token(s));
    if(s.startsWith(FILEPREF)) return new IOFile(IOUrl.file(s));
    if(IOFile.valid(s))        return new IOFile(s);
    return new IOUrl(s);
  }

  /**
   * Returns the contents.
   * @return contents
   * @throws IOException I/O exception
   */
  public abstract byte[] read() throws IOException;

  /**
   * Tests if the file exists.
   * @return result of check
   */
  public boolean exists() {
    return true;
  }

  /**
   * Tests if this is a directory instance.
   * @return result of check
   */
  public boolean isDir() {
    return false;
  }

  /**
   * Returns the modification date of this file.
   * @return modification date
   */
  public long date() {
    return System.currentTimeMillis();
  }

  /**
   * Returns the file length.
   * @return file length
   */
  public abstract long length();

  /**
   * Checks if more input streams are found.
   * @param archive parse archives
   * @return result of check
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public boolean more(final boolean archive) throws IOException {
    return more ^= true;
  }

  /**
   * Returns the next input source.
   * @return input source
   */
  public abstract InputSource inputSource();

  /**
   * Returns a buffered reader for the input.
   * @return buffered reader
   * @throws IOException I/O exception
   */
  public abstract BufferInput buffer() throws IOException;

  /**
   * Merges two filenames.
   * @param fn file name/path to be merged
   * @return contents
   */
  public abstract IO merge(final String fn);

  /**
   * Checks if this file is an archive.
   * @return result of check
   */
  public boolean isArchive() {
    return false;
  }

  /**
   * Checks if this file contains XML.
   * @return result of check
   */
  public boolean isXML() {
    return false;
  }

  /**
   * Chops the path and the file suffix of the specified filename
   * and returns the database name.
   * @return database name
   */
  public final String dbname() {
    final String n = name();
    final int i = n.lastIndexOf(".");
    return (i != -1 ? n.substring(0, i) : n).replaceAll("[^\\w-]", "");
  }

  /**
   * Returns the name of the resource.
   * @return file name
   */
  public final String name() {
    return name;
  }

  /**
   * Sets the name of the resource.
   * @param n file name
   */
  public final void name(final String n) {
    name = n;
    if(path.isEmpty()) path = n;
  }

  /**
   * Returns the path.
   * The path uses forward slashes, no matter which OS is used.
   * @return path
   */
  public final String path() {
    return path;
  }

  /**
   * Creates a URL from the specified path.
   * @return URL
   */
  public String url() {
    return path;
  }

  /**
   * Returns the directory.
   * @return chopped filename
   */
  public String dir() {
    return "";
  }

  /**
   * Compares the filename of the specified IO reference.
   * @param io io reference
   * @return result of check
   */
  public final boolean eq(final IO io) {
    return path.equals(io.path);
  }

  @Override
  public String toString() {
    return path;
  }
}
