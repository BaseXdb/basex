package org.basex.build.fs.parser;

import static org.basex.data.DataText.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.Attribute;
import org.basex.build.fs.parser.Metadata.Definition;
import org.basex.build.fs.parser.Metadata.Element;
import org.basex.build.fs.parser.Metadata.Type;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.DataType;
import org.basex.data.DataText;
import org.basex.util.Atts;

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

  /** If true, the <code>type=""</code> attributes are added to the XML doc. */
  private static final boolean ADD_TYPE_ATTR = false;
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

  /** Attribute container. */
  private final Atts atts = new Atts();
  /** The type of the file. */
  private final Metadata.Type type;
  /** The format of the file (MIME type). */
  private final Metadata.MimeType format;
  /** Valid filename suffixes for the parser. */
  private final Set<String> suffixes;
  /** The current file. */
  protected File file;

  /** The {@link Builder} instance. */
  private Builder builder;
  /** The {@link NewFSParser} instance. */
  protected NewFSParser fsParser;

  /**
   * Sets the {@link NewFSParser}.
   * @param parser the {@link NewFSParser}.
   */
  public void setFSParser(final NewFSParser parser) {
    fsParser = parser;
  }

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
   * Starts reading the metadata of the file.
   * @param f the file to read the metadata from.
   * @param b the {@link Builder} instance to use for generating xml.
   */
  public void readMeta(final File f, final Builder b) {
    FileChannel fc = null;
    try {
      fc = new RandomAccessFile(f, "r").getChannel();
      readMeta(f, fc, fc.size(), b);
    } catch(IOException e) { /* */} finally {
      try {
        if(fc != null) fc.close();
      } catch(IOException e) { /* */}
    }
  }

  /**
   * Reads metadata from the {@link FileChannel}. This method can be used to
   * read metadata from an arbitrary part of the file. It is not intended to be
   * used for parsing a complete file but for parsing only a (small) part of a
   * file. Whenever possible, {@link #readMeta(File, Builder)} should be used
   * instead of this method.
   * @param f the file to read the metadata from.
   * @param fc the {@link FileChannel} to read from. The position has to be
   *          initialized to the first byte of the header.
   * @param limit the maximum number of bytes to read.
   * @param b the {@link Builder} instance to use for generating xml.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  public void readMeta(final File f, final FileChannel fc, final long limit,
      final Builder b) throws IOException {
    builder = b;
    file = f;
    builder.nodeAndText(Element.TYPE.get(), atts.reset(), type.get());
    builder.nodeAndText(Element.FORMAT.get(), atts, format.get());
    readMeta(fc, limit);
  }

  /**
   * Starts reading the content of the file.
   * @param f the file to read the content from.
   * @param b the builder instance to use for generating xml.
   */
  public void readContent(final File f, final Builder b) {
    FileChannel fc = null;
    try {
      fc = new RandomAccessFile(f, "r").getChannel();
      readContent(f, fc, fc.size(), b);
    } catch(IOException e) { /* */} finally {
      try {
        if(fc != null) fc.close();
      } catch(IOException e) { /* */}
    }
  }

  /**
   * Reads content from the {@link FileChannel}. This method can be used to read
   * content from an arbitrary part of the file. It is not intended to be used
   * for parsing a complete file but for parsing only a (small) part of a file.
   * Whenever possible, {@link #readContent(File, Builder)} should be used
   * instead of this method.
   * @param f the file to read the content from.
   * @param fc the {@link FileChannel} to read from. The position has to be
   *          initialized to the first byte of the header.
   * @param limit the maximum number of bytes to read.
   * @param b the {@link Builder} instance to use for generating xml.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  public void readContent(final File f, final FileChannel fc, final long limit,
      final Builder b) throws IOException {
    builder = b;
    file = f;
    builder.startElem(DataText.CONTENT, atts.reset());
    readContent(fc, limit);
    builder.endElem(DataText.CONTENT);
  }

  /**
   * <p>
   * Starts reading the metadata and content of the file.
   * </p>
   * <p>
   * An invocation of this method has exactly the same effect as the invocations
   * 
   * <pre>
   * {@link #readMeta(File, Builder)}
   * {@link #readContent(File, Builder)}
   * </pre>
   * 
   * but is a bit more efficient (reuses the same {@link FileChannel}).
   * </p>
   * @param f the file to read from.
   * @param b the builder instance to use for generating xml.
   */
  public void readMetaAndContent(final File f, final Builder b) {
    builder = b;
    file = f;
    FileChannel fc = null;
    try {
      builder.nodeAndText(Element.TYPE.get(), atts.reset(), type.get());
      builder.nodeAndText(Element.FORMAT.get(), atts, format.get());
      fc = new RandomAccessFile(f, "r").getChannel();
      readMeta(fc, fc.size());
      builder.startElem(DataText.CONTENT, atts.reset());
      readContent(fc, fc.size());
      builder.endElem(DataText.CONTENT);
    } catch(IOException e) { /* */} finally {
      try {
        if(fc != null) fc.close();
      } catch(IOException e) { /* */}
    }
  }

  /**
   * Returns all supported file suffixes (lower case).
   * @return supported file suffixes.
   */
  public Set<String> getSuffixes() {
    return suffixes;
  }

  /**
   * Returns the type of the adapter (e.g. Sound, Mail, Text, ...).
   * @return the type of the adapter.
   */
  public String getType() {
    return type.name();
  }

  /**
   * Returns the format of the adapter (the MIME type).
   * @return the format of the adapter.
   */
  public String getFormat() {
    return format.name();
  }

  // ---------------------------------------------------------------------------
  // ----- constructor / methods for parser implementations --------------------
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
   * Generates an xml representation for a name/value pair and adds it to the
   * current file element.
   * @param element the xml element to create.
   * @param t the type of the xml element.
   * @param definition the precise definition of the xml element.
   * @param language the language of the element.
   * @param value the value of the element.
   * @throws IOException if any error occurs while generating the xml code.
   */
  protected void metaEvent(final Element element, final DataType t,
      final Definition definition, final byte[] language, final byte[] value)
      throws IOException {
    if(ParserUtil.isEmpty(value)) return;
    boolean hasContent = false;
    for(byte v : value) {
      if(v != 0x09 && v != 0x0A && v != 0x0D && v != 0x20) {
        hasContent = true;
        break;
      }
    }
    if(!hasContent) return;
    atts.reset();
    if(language != null) atts.add(Attribute.LANGUAGE.get(), language);
    if(definition != Definition.NONE) atts.add(Attribute.DEFINITION.get(),
        definition.get());
    if(ADD_TYPE_ATTR) atts.add(Attribute.TYPE.get(), t.get());
    builder.nodeAndText(element.get(), atts, value);
  }

  /**
   * <p>
   * Generates a new file element inside the actual element. Can be used e.g.
   * for pictures inside ID3 tags.
   * <p>
   * <p>
   * After calling this method, arbitrary metadata can be added to the file by
   * calling {@link #metaEvent(Element, DataType, Definition, byte[], byte[])}.
   * </p>
   * <p>
   * <b>{@link #endFileEvent()} has to be called afterwards to close the file
   * element!</b>
   * </p>
   * @param name the name of the new file.
   * @param suffix the suffix of the new file.
   * @param offset the offset of the virtual file in the real file.
   * @param size the size of the file.
   * @param mtime the mtime value of the file.
   * @throws IOException if any error occurs while generating the xml code.
   */
  protected void startFileEvent(final byte[] name, final byte[] suffix,
      final byte[] offset, final byte[] size, final byte[] mtime)
      throws IOException {
    atts.reset();
    if(name != null) atts.add(DataText.NAME, name);
    if(suffix != null) atts.add(DataText.SUFFIX, suffix);
    if(offset != null) atts.add(DataText.OFFSET, offset);
    if(size != null) atts.add(DataText.SIZE, size);
    if(mtime != null) atts.add(DataText.MTIME, mtime);
    builder.startElem(DataText.FILE, atts);
  }

  /**
   * Closes the last file element.
   * @throws IOException if any error occurs while generating the xml code.
   */
  protected void endFileEvent() throws IOException {
    builder.endElem(FILE);
  }

  /**
   * Returns the current xml builder.
   * @return the builder.
   */
  protected Builder getBuilder() {
    return builder;
  }

  // ---------------------------------------------------------------------------

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
   * @param f the {@link FileChannel} to read from.
   * @param limit maximum number of bytes to read.
   * @return true if the file is supported.
   * @throws IOException if an error occurs while reading from the file.
   */
  abstract boolean check(final FileChannel f, final long limit)
      throws IOException;

  /**
   * <p>
   * Reads the metadata from a {@link FileChannel} and fires events for each
   * key/value pair. {@link FileChannel#position()} must point to the start of
   * the file header.
   * </p>
   * <p>
   * For regular files, the FileChannel position is zero and the limit equals to
   * the length of the file. For a file that is inside another one (e.g. a
   * picture inside the ID3v2 tag of a mp3-file) the FileChannel position must
   * point to the beginning of this "inner" file and the limit must be the
   * lenght of the "inner" file.
   * </p>
   * @param f {@link FileChannel} to read from.
   * @param limit maximum number of bytes to read.
   * @throws IOException if any error occurs while reading from the file.
   */
  abstract void readMeta(final FileChannel f, final long limit)
      throws IOException;

  /**
   * <p>
   * Reads the textual content from a {@link FileChannel} and fires events.
   * {@link FileChannel#position()} must point to the start of the file header.
   * </p>
   * <p>
   * For regular files, the FileChannel position is zero and the limit equals to
   * the length of the file. For a file that is inside another one (e.g. a
   * picture inside the ID3v2 tag of a mp3-file) the FileChannel position must
   * point to the beginning of this "inner" file and the limit must be the
   * lenght of the "inner" file.
   * </p>
   * @param f the {@link FileChannel} to read from.
   * @param limit maximum number of bytes to read.
   * @throws IOException if any error occurs while reading from the file.
   */
  abstract void readContent(final FileChannel f, final long limit)
      throws IOException;
}
