package org.basex.io;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.xml.sax.*;

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
  /** Command script suffix. */
  public static final String BXSSUFFIX = ".bxs";
  /** XQuery file suffix. */
  public static final String XQSUFFIX = ".xq";
  /** XQuery module suffix. */
  public static final String XQMSUFFIX = ".xqm";
  /** XML file suffix. */
  public static final String XMLSUFFIX = ".xml";
  /** ZIP file suffix. */
  public static final String ZIPSUFFIX = ".zip";
  /** CSV file suffix. */
  public static final String CSVSUFFIX = ".csv";
  /** JSON file suffix. */
  public static final String JSONSUFFIX = ".json";
  /** JAR file suffix. */
  public static final String JARSUFFIX = ".jar";
  /** GZIP file suffix. */
  public static final String GZSUFFIX = ".gz";
  /** XAR file suffix. */
  public static final String XARSUFFIX = ".xar";
  /** File prefix. */
  public static final String FILEPREF = "file:";

  /** XQuery suffixes. */
  public static final String[] XQSUFFIXES =
    { XQSUFFIX, XQMSUFFIX, ".xqy", ".xql", ".xqu", ".xquery" };
  /** ZIP suffixes. */
  public static final String[] ZIPSUFFIXES =
    { ZIPSUFFIX, GZSUFFIX, XARSUFFIX, ".docx", ".pptx", ".xslx", ".odt", ".odp", ".ods" };
  /** XML suffixes. */
  public static final String[] XMLSUFFIXES =
    { XMLSUFFIX, ".xsd", ".xsl", ".xslt", ".svg", ".rdf", ".rss", ".rng", ".sch" };
  /** HTML suffixes. */
  public static final String[] HTMLSUFFIXES =
    { ".xhtml", ".html", ".htm" };
  /** Text suffixes. */
  public static final String[] TXTSUFFIXES = {
    ".txt", ".text", ".ini", ".conf" };

  /** Disk block/page size (4096). */
  public static final int BLOCKSIZE = 1 << 12;
  /** Table node size power (4). */
  public static final int NODEPOWER = 4;
  /** Table node size power (16). */
  public static final int NODESIZE = 1 << NODEPOWER;
  /** Entries per block (256). */
  public static final int ENTRIES = BLOCKSIZE >>> NODEPOWER;

  /** Maximum number of attributes (see bit layout in {@link Data} class). */
  public static final int MAXATTS = 0x1F;
  /** Offset for inlining numbers (see bit layout in {@link Data} class). */
  public static final long OFFNUM = 0x8000000000L;
  /** Offset for compressing texts (see bit layout in {@link Data} class). */
  public static final long OFFCOMP = 0x4000000000L;

  /** File path. The path uses forward slashes, no matter which OS is used. */
  String path;
  /** File name. */
  String name;
  /** File length. */
  long len = -1;

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
    name = n.isEmpty() ? Long.toString(System.currentTimeMillis()) + XMLSUFFIX : n;
  }

  /**
   * <p>Returns a class instance for the specified string. The type of the
   * returned instance depends on the string value:</p>
   * <ul>
   * <li>{@link IOFile}: if the string starts with <code>file:</code>, or if it
   *   does not contain the substring <code>://</code>, it is interpreted as
   *   local file instance</li>
   * <li>{@link IOUrl}: if it starts with a valid scheme, it is handled as URL</li>
   * <li>{@link IOContent}: otherwise, it is interpreted as XML fragment and internally
   *   represented as byte array</li>
   * </ul>
   * If the content of the string value is known in advance, it is advisable
   * to call the direct constructors of the correspondent sub class.
   *
   * @param source source string
   * @return IO reference
   */
  public static IO get(final String source) {
    if(source == null) return new IOContent("");
    final String s = source.trim();
    return s.indexOf('<') == 0 ? new IOContent(s) :
           IOUrl.isFileURL(s)  ? new IOFile(IOUrl.file(s)) :
           IOFile.isValid(s)   ? new IOFile(s) :
           IOUrl.isValid(s)    ? new IOUrl(s) :
           new IOContent(s);
  }

  /**
   * Returns the contents.
   * @return contents
   * @throws IOException I/O exception
   */
  public abstract byte[] read() throws IOException;

  /**
   * Tests if the file exists.
   * Returns {@code true} for IO instances other than {@link IOFile}.
   * @return result of check
   */
  public boolean exists() {
    return true;
  }

  /**
   * Tests if this is a directory instance.
   * Returns {@code false} for IO instances other than {@link IOFile}.
   * @return result of check
   */
  public boolean isDir() {
    return false;
  }

  /**
   * Tests if the file suffix matches the specified suffixes.
   * @param suffixes suffixes to compare with
   * @return result of check
   */
  public boolean hasSuffix(final String... suffixes) {
    final int i = path.lastIndexOf('.');
    if(i == -1) return false;
    final String suf = path.substring(i).toLowerCase(Locale.ENGLISH);
    for(final String z : suffixes) if(suf.equals(z)) return true;
    return false;
  }

  /**
   * Returns the time stamp (modification date) of this file.
   * Returns the current time for IO instances other than {@link IOFile}.
   * @return time stamp
   */
  public long timeStamp() {
    return System.currentTimeMillis();
  }

  /**
   * Sets the input length.
   * @param l length
   */
  public void length(final long l) {
    len = l;
  }

  /**
   * Returns the file length.
   * @return file length
   */
  public long length() {
    return len;
  }

  /**
   * Returns an input source.
   * @return input source
   */
  public abstract InputSource inputSource();

  /**
   * Returns an input stream.
   * @return input stream
   * @throws IOException I/O exception
   */
  public abstract InputStream inputStream() throws IOException;

  /**
   * Merges two paths. Returns the new specified path for {@link IOContent} and
   * {@link IOStream} instances.
   * @param in name/path to be appended
   * @return resulting reference
   */
  public IO merge(final String in) {
    return IO.get(in);
  }

  /**
   * Checks if this file is an archive.
   * @return result of check
   */
  public final boolean isArchive() {
    return hasSuffix(ZIPSUFFIXES);
  }

  /**
   * Chops the path and the file suffix of the specified filename
   * and returns the database name.
   * @return database name
   */
  public final String dbname() {
    final String n = name();
    final int i = n.lastIndexOf('.');
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
   * Returns the original path for IO instances other than {@link IOFile}.
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

  /**
   * Returns the suffix of the specified path in lower case.
   * An empty string is returned if the last path segment has no suffix.
   * @param path path to be checked
   * @return mime-type
   */
  public static String suffix(final String path) {
    final int s = path.lastIndexOf('/');
    final int d = path.lastIndexOf('.');
    return d <= s ? "" : path.substring(d + 1).toLowerCase(Locale.ENGLISH);
  }
}
