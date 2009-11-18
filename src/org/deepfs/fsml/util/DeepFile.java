package org.deepfs.fsml.util;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import javax.xml.datatype.Duration;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.data.Data;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Type;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.deepfs.fsml.parsers.IFileParser;

/**
 * <p>
 * Storage for metadata information and contents for a single file.
 * </p>
 * <p>
 * A DeepFile can represent a regular file in the file system or a "subfile"
 * that is stored inside another file, e.g. a file in a ZIP-file or a picture
 * that is included in an ID3 tag.
 * </p>
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Bastian Lemke
 */
public class DeepFile {

  // [BL] integrate BufferedFileChannel
  // [BL] allow multiple meta elems with the same name (e.g. multiple comments)
  // [BL] allow multiple file types (e.g. "xml" and "picture" for SVG files)

  /**
   * Default value for the maximum number of bytes to extract from text
   * contents.
   */
  public static final int DEFAULT_TEXT_MAX = 10240;

  /**
   * A reference to the parser registry that can be used to parse file
   * fragments.
   */
  private final ParserRegistry parser;
  /**
   * The file channel to access the file. This channel links the DeepFile object
   * with a file in the file system.
   */
  private final BufferedFileChannel bfc;

  /** Map, containing all metadata key-value pairs for the current file. */
  private final TreeMap<MetaElem, byte[]> metaElements;
  /** List with all file fragments (fs:content elements). */
  private final ArrayList<DeepFile> fileFragments;
  /** All text contents that are extracted from the file. */
  private final ArrayList<TextContent> textContents;
  /** All xml contents that are extracted from the file as string. */
  private final ArrayList<XMLContent> xmlContents;

  /** Flag, if metadata should be extracted from the current file. */
  public final boolean fsmeta;
  /** Flag, if text contents should be extracted from the current file. */
  public final boolean fscont;
  /** Flag, if xml contents should be extracted from the current file. */
  public final boolean fsxml;
  /** Maximum number of bytes to extract from text contents. */
  public final int fstextmax;
  /** Offset of the deep file inside the current regular file. */
  private final long offset;
  /** Size of the deep file in bytes (-1 means unknown size). */
  private long size;

  // ---------------------------------------------------------------------------

  /**
   * <p>
   * Constructor.
   * </p>
   * <p>
   * Creates a DeepFile object for a file that can be used to extract metadata
   * and text/xml contents. By default, metadata and all contents will be
   * extracted.
   * </p>
   * <p>
   * This constructor should only be used to parse a single file. Use
   * {@link #DeepFile(ParserRegistry, BufferedFileChannel, boolean, boolean,
   *   boolean, int)}
   * for parsing several files for better performance.
   * </p>
   * @param file the name of the associated file in the file system.
   * @throws IOException if any I/O error occurs.
   * @see IFileParser#extract(DeepFile)
   */
  public DeepFile(final String file) throws IOException {
    this(new File(file));
  }

  /**
   * <p>
   * Constructor.
   * </p>
   * <p>
   * Creates a DeepFile object for a file that can be used to extract metadata
   * and text/xml contents. By default, metadata and all contents will be
   * extracted.
   * </p>
   * <p>
   * This constructor should only be used to parse a single file. Use
   * {@link #DeepFile(ParserRegistry, BufferedFileChannel, boolean, boolean,
   *   boolean, int)}
   * for parsing several files for better performance.
   * </p>
   * @param file the associated file in the file system.
   * @throws IOException if any I/O error occurs.
   * @see IFileParser#extract(DeepFile)
   */
  public DeepFile(final File file) throws IOException {
    this(new ParserRegistry(), file.isDirectory() ? null
        : new BufferedFileChannel(file), true, true, true, 0,
        file.isDirectory() ? -1 : file.length(), DEFAULT_TEXT_MAX);
  }

  /**
   * <p>
   * Constructor.
   * </p>
   * <p>
   * Creates a DeepFile object for a file that can be used to extract metadata
   * and text/xml contents.
   * </p>
   * <p>
   * This constructor should only be used to parse a single file. Use
   * {@link #DeepFile(ParserRegistry, BufferedFileChannel, boolean, boolean,
   *   boolean, int)}
   * for parsing several files for better performance.
   * </p>
   * @param file the associated file in the file system.
   * @param meta extract/propagate metadata.
   * @param xmlContent extract/propagate xml data.
   * @param textContent extract/propagate content data.
   * @throws IOException if any I/O error occurs.
   * @see IFileParser#extract(DeepFile)
   */
  public DeepFile(final File file, final boolean meta,
      final boolean xmlContent, final boolean textContent) throws IOException {
    this(new ParserRegistry(), file.isDirectory() ? null
        : new BufferedFileChannel(file), meta, xmlContent, textContent, 0,
        file.isDirectory() ? -1 : file.length(), DEFAULT_TEXT_MAX);
  }

  /**
   * Constructor.
   * @param parserRegistry a reference to the parser registry that can be used
   *          to parse file fragments.
   * @param bufferedFileChannel {@link BufferedFileChannel} to access the file.
   * @param meta extract/propagate metadata.
   * @param xmlContent extract/propagate xml data.
   * @param textContent extract/propagate content data.
   * @param textMax maximum number of bytes to extract from texts.
   * @throws IOException if any error occurs;
   */
  public DeepFile(final ParserRegistry parserRegistry,
      final BufferedFileChannel bufferedFileChannel, final boolean meta,
      final boolean xmlContent, final boolean textContent, final int textMax)
      throws IOException {
    this(parserRegistry, bufferedFileChannel, meta, xmlContent, textContent,
        bufferedFileChannel.getOffset(), bufferedFileChannel.size(), textMax);
  }

  /**
   * Constructor.
   * @param parserRegistry a reference to the parser registry that can be used
   *          to parse file fragments.
   * @param bufferedFileChannel {@link BufferedFileChannel} to access the file.
   * @param meta extract/propagate metadata.
   * @param xmlContent extract/propagate xml data.
   * @param textContent extract/propagate content data.
   * @param position the position inside the file in the file system.
   * @param contentSize the size of the deep file.
   * @param textMax maximum number of bytes to extract from texts.
   */
  private DeepFile(final ParserRegistry parserRegistry,
      final BufferedFileChannel bufferedFileChannel, final boolean meta,
      final boolean xmlContent, final boolean textContent, final long position,
      final long contentSize, final int textMax) {
    parser = parserRegistry;
    bfc = bufferedFileChannel;
    fsmeta = meta;
    fsxml = xmlContent;
    fscont = textContent;
    fstextmax = textMax;
    offset = position;
    size = contentSize;
    fileFragments = new ArrayList<DeepFile>();
    metaElements = fsmeta ? new TreeMap<MetaElem, byte[]>() : null;
    textContents = fscont ? new ArrayList<TextContent>() : null;
    xmlContents = fsxml ? new ArrayList<XMLContent>() : null;
  }

  /**
   * Clones the DeepFile to map only a fragment of the file.
   * @param df the DeepFile to clone.
   * @param contentSize the size of the underlying {@link BufferedFileChannel}.
   * @throws IOException if any error occurs.
   */
  private DeepFile(final DeepFile df, final int contentSize)
      throws IOException {

    parser = df.parser;
    bfc = df.bfc.subChannel(df.bfc.getFileName(), contentSize);
    fsmeta = df.fsmeta;
    fsxml = df.fsxml;
    fscont = df.fscont;
    fstextmax = df.fstextmax;
    offset = df.offset;
    size = df.size;
    fileFragments = df.fileFragments;
    metaElements = df.metaElements;
    textContents = df.textContents;
    xmlContents = df.xmlContents;
  }

  /** Removes all metadata and contents. */
  public void clear() {
    metaElements.clear();
    fileFragments.clear();
    textContents.clear();
    xmlContents.clear();
  }

  /**
   * Returns all subfiles.
   * @return the subfiles.
   */
  public DeepFile[] getContent() {
    return fileFragments.toArray(new DeepFile[fileFragments.size()]);
  }

  /**
   * Returns all metadata key-value pairs or <code>null</code> if fsmeta is
   * false.
   * @return the metadata.
   */
  public TreeMap<MetaElem, byte[]> getMeta() {
    return metaElements;
  }

  /**
   * Returns all text sections or <code>null</code> if fscont is false.
   * @return all text sections.
   */
  public TextContent[] getTextContents() {
    return textContents == null ? null
        : textContents.toArray(new TextContent[textContents.size()]);
  }

  /**
   * Returns all xml sections or <code>null</code> if fsxml is false.
   * @return all xml sections.
   */
  public XMLContent[] getXMLContents() {
    return xmlContents == null ? null
        : xmlContents.toArray(new XMLContent[xmlContents.size()]);
  }

  /**
   * Returns the offset of the deep file inside the regular file in the file
   * system.
   * @return the offset.
   */
  public long getOffset() {
    return offset;
  }

  /**
   * Returns the size of the deep file.
   * @return the size.
   */
  public long getSize() {
    return size;
  }

  /**
   * Extract metadata and text/xml contents from the associated file.
   * @throws IOException if any error occurs while reading from the file.
   */
  public void extract() throws IOException {
    if(bfc == null) return;
    final String fname = bfc.getFileName();
    if(fname.indexOf('.') != -1) {
      final int dot = fname.lastIndexOf('.');
      final String suffix = fname.substring(dot + 1).toLowerCase();
      process(this, suffix);
    }
  }

  /**
   * Calls the fallback parser for the associated file to extract text contents.
   * @throws IOException if any error occurs while reading from the file.
   * @throws ParserException if the fallback parser could not be loaded.
   */
  public void fallback() throws IOException, ParserException {
    final IFileParser p = parser.getFallbackParser();
    bfc.reset();
    p.extract(this);
    bfc.finish();
  }

  /**
   * Processes a DeepFile and extracts metadata/content.
   * @param df the DeepFile to process.
   * @param suffix the file suffix(es). More than one suffix means that the file
   *          type is unknown. All given suffixes will be tested.
   * @throws IOException if any error occurs while reading from the file.
   */
  private void process(final DeepFile df, final String... suffix)
      throws IOException {
    IFileParser p = null;
    for(final String s : suffix) {
      try {
        p = parser.getParser(s.toLowerCase());
      } catch(final ParserException e) { /* ignore and continue ... */}
      if(p != null) break;
    }
    if(p != null) p.extract(df);
  }

  // ---------------------------------------------------------------------------
  // ----- Methods for parser implementations ----------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Returns the associated {@link BufferedFileChannel} that links this
   * {@link DeepFile} with a file in the file system.
   * @return the {@link BufferedFileChannel}.
   */
  public BufferedFileChannel getBufferedFileChannel() {
    return bfc;
  }

  // ----- metadata ------------------------------------------------------------

  /**
   * Sets the type of the file (e.g. audio, video, ...).
   * @param type the file type.
   */
  public void setFileType(final FileType type) {
    if(!fsmeta) return;
    metaElements.put(MetaElem.TYPE, type.get());
  }

  /**
   * Sets the MIME type of the file.
   * @param format the MIME type.
   */
  public void setFileFormat(final MimeType format) {
    if(!fsmeta) return;
    metaElements.put(MetaElem.FORMAT, format.get());
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param e metadata element (the key).
   * @param value value as byte array. Must contain only correct UTF-8 values!
   * @param dataType the xml data type to set for this metadata element or
   *          <code>null</code> if the default data type should be used.
   */
  private void addMeta(final MetaElem e, final byte[] value,
      final Type dataType) {
    if(!fsmeta) return;
    if(e.equals(MetaElem.TYPE) | e.equals(MetaElem.FORMAT)) {
      Main.bug(
          "The metadata attributes " + MetaElem.TYPE + " and "
          + MetaElem.FORMAT
          + " must not be set by an addMetaElem() method." +
          " Use setMetaType() and setFormat() instead.");
    }
    if(dataType != null) e.refineDataType(dataType);
    else e.reset();
    metaElements.put(e, value);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key). Must be a string attribute.
   * @param value string value as byte array.
   */
  public void addMeta(final MetaElem elem, final byte[] value) {
    if(!elem.getType().instance(Type.STR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (string - as byte array).");
    addMeta(elem, ParserUtil.checkUTF(value), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value string value.
   */
  public void addMeta(final MetaElem elem, final String value) {
    if(!elem.getType().instance(Type.STR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (string).");
    addMeta(elem, ParserUtil.checkUTF(Token.token(value)), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value integer value.
   */
  public void addMeta(final MetaElem elem, final short value) {
    if(!elem.getType().instance(Type.SHR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (short).");

    addMeta(elem, ParserUtil.checkUTF(Token.token(value)), Type.SHR);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value integer value.
   */
  public void addMeta(final MetaElem elem, final int value) {
    if(!elem.getType().instance(Type.ITR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (int).");
    addMeta(elem, ParserUtil.checkUTF(Token.token(value)), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value long value.
   */
  public void addMeta(final MetaElem elem, final long value) {
    if(!elem.getType().instance(Type.LNG)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (long).");
    addMeta(elem, ParserUtil.checkUTF(Token.token(value)), Type.LNG);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value date value.
   */
  public void addMeta(final MetaElem elem, final Date value) {
    if(!elem.getType().instance(Type.DTM)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (date).");
    addMeta(elem, ParserUtil.convertDate(value), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value duration value.
   */
  public void addMeta(final MetaElem elem, final Duration value) {
    if(!elem.getType().instance(Type.DUR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (date).");
    addMeta(elem, Token.token(value.toString()), null);
  }

  /**
   * Returns the string value for a {@link MetaElem} that was previously added.
   * @param elem the metadata element.
   * @return the metadata value as string.
   */
  public String getValueAsString(final MetaElem elem) {
    return metaElements.containsKey(elem) ?
        Token.string(metaElements.get(elem)) :
        null;
  }

  // ----- contents ------------------------------------------------------------

  // /**
  // * Adds a text section.
  // * @param position the absolute position of the first byte of the file
  // * fragment represented by this content element inside the current
  // * file. A negative value stands for an unknown offset.
  // * @param byteCount the size of the content element.
  // * @param text the text to add.
  // * @param preserveSpace if true, leading and trailing whitespaces are
  // removed.
  // */
  // public void addText(final long position, final int byteCount,
  // final String text, final boolean preserveSpace) {
  // addText(position, byteCount, token(text), preserveSpace);
  // }

  /**
   * Adds a text section.
   * @param position the absolute position of the first byte of the file
   *          fragment represented by this content element inside the current
   *          file. A negative value stands for an unknown offset.
   * @param byteCount the size of the content element.
   * @param text the text to add.
   */
  public void addText(final long position, final int byteCount,
      final byte[] text) {
    addText(position, byteCount, new TokenBuilder(ParserUtil.checkUTF(text)));
  }

  /**
   * <p>
   * Adds a text section. <b><code>text</code> MUST contain only valid UTF-8
   * characters!</b> Otherwise the generated XML document may be not
   * well-formed.
   * </p>
   * @param position the absolute position of the first byte of the file
   *          fragment represented by this content element inside the current
   *          file. A negative value stands for an unknown offset.
   * @param byteCount the size of the content element.
   * @param text the text to add.
   */
  public void addText(final long position, final int byteCount,
      final TokenBuilder text) {
    if(!fscont) return;
    text.chop();
    if(text.size() == 0) return;
    textContents.add(new TextContent(position, byteCount, text.finish()));
  }

  /**
   * Adds a xml document or fragment to the DeepFile.
   * @param position offset of the xml document/fragement inside the file.
   * @param byteCount number of bytes of the xml document/fragment.
   * @param data the xml document/fragment.
   * @throws IOException if any error occurs.
   */
  public void addXML(final long position, final int byteCount,
      final Data data) throws IOException {
    if(!fsxml) return;
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintOutput po = new PrintOutput(baos);
    final XMLSerializer ser = new XMLSerializer(po);
    final Context ctx = new Context();
    ctx.openDB(data);
    final QueryProcessor qp = new QueryProcessor("/", ctx);
    String xmlContent;
    try {
      final Result res = qp.query();
      res.serialize(ser);
      xmlContent = baos.toString();
      ser.close();
      po.close();
    } catch(final QueryException e) {
      xmlContent = "";
    }
    xmlContents.add(new XMLContent(position, byteCount, xmlContent));
  }

  /**
   * <p>
   * Creates a new "subfile" inside the current DeepFile with the given size,
   * beginning at the current position of the file channel. This method is
   * intended to be used if the content of the subfile has to be parsed with a
   * different parser implementation than the "main" file. The name of the
   * subfile is set as title metadata.
   * </p>
   * <p>
   * If the content can be parsed with the same parser and this parser can use
   * the same {@link BufferedFileChannel}, then the method
   * {@link #newContentSection(long)} can be used instead.
   * </p>
   * @param fileName the name of the subfile.
   * @param fileSize the size of the subfile.
   * @param suffix the file suffix(es). More than one suffix means that the file
   *          type is unknown. All given suffixes will be tested.
   * @return the subfile.
   * @throws IOException if any error occurs.
   * @see #newContentSection(long)
   */
  public DeepFile subfile(final String fileName, final int fileSize,
      final String... suffix) throws IOException {
    if(bfc == null) throw new IOException(
        "Can't create a subfile for a deep file that is " +
        "not associated with a regular file.");
    final BufferedFileChannel sub = bfc.subChannel(fileName, fileSize);
    final DeepFile content = new DeepFile(parser, sub, fsmeta, fsxml, fscont,
        sub.absolutePosition(), sub.size(), fstextmax);
    fileFragments.add(content);
    if(fileName != null) content.addMeta(MetaElem.TITLE, fileName);
    if(parser != null) process(content, suffix);
    return content;
  }

  /**
   * Clones the DeepFile to map only a part of the file. The returned DeepFile
   * uses an underlying BufferedFileChannel that starts at the given byte
   * position. The cloned DeepFile must be finished after usage.
   * @param position the position where the subchannel should start.
   * @param contentSize the size of the file fragment (the size of the
   *          BufferedFileChannel).
   * @return the new DeepFile.
   * @throws IOException if any error occurs.
   * @see #finish()
   */
  public DeepFile subfile(final long position, final int contentSize)
      throws IOException {
    bfc.position(position);
    return new DeepFile(this, contentSize);
  }

  /**
   * Finishes the underlying BufferedFileChannel.
   * @throws IOException if any error occurs.
   * @see BufferedFileChannel#finish()
   */
  public void finish() throws IOException {
    bfc.finish();
  }

  /**
   * Clones the DeepFile to map only a part of the file. The returned DeepFile
   * uses an underlying BufferedFileChannel that starts at the current byte
   * position. The cloned DeepFile must be finished after usage.
   * @param contentSize the size of the file fragment (the size of the
   *          BufferedFileChannel).
   * @return the new DeepFile.
   * @throws IOException if any error occurs.
   * @see #finish()
   */
  public DeepFile subfile(final int contentSize) throws IOException {
    return new DeepFile(this, contentSize);
  }

  /**
   * <p>
   * Creates a new content section for the current file, beginning at the given
   * position with an unknown size.
   * </p>
   * <p>
   * The returned DeepFile instance uses a subchannel to read from the file. The
   * subchannel has to be finished after usage (
   * {@link BufferedFileChannel#finish()}).
   * </p>
   * @param title the title of the content section.
   * @param position the offset in the regular file where the section starts.
   * @param contentSize the size of the content section.
   * @return the DeepFile instance representing the content section.
   * @throws IOException if any error occurs.
   * @see #subfile(String, int, String...)
   * @see BufferedFileChannel#subChannel(String, int)
   */
  public DeepFile newContentSection(final String title, final long position,
      final int contentSize) throws IOException {
    bfc.position();
    final BufferedFileChannel sub = bfc.subChannel(title, contentSize);
    final DeepFile subFile = new DeepFile(parser, sub, fsmeta, fsxml, fscont,
        position, contentSize, fstextmax);
    subFile.addMeta(MetaElem.TITLE, title);
    return subFile;
  }

  /**
   * <p>
   * Creates a new content section for the current file, beginning at the given
   * position with an unknown size. The size must be set afterwards with
   * {@link #setSize(long)}.
   * </p>
   * <p>
   * The returned DeepFile instance uses the same underlying BufferedFileChannel
   * as the current DeepFile.
   * </p>
   * @param position the offset in the regular file where the section starts.
   * @return the DeepFile instance representing the content section.
   * @see #subfile(String, int, String...)
   * @see #setSize(long)
   */
  public DeepFile newContentSection(final long position) {
    return new DeepFile(parser, bfc, fsmeta, fsxml, fscont, position, -1,
        fstextmax);
  }

  /**
   * Sets the size value for the DeepFile. If the current DeepFile instance is
   * not a content section, or if the size value was set before, this method
   * does nothing.
   * @param contentSize the size value to set for the content section.
   * @see #newContentSection(long)
   */
  public void setSize(final long contentSize) {
    if(size == -1) size = contentSize;
  }

  // ---------------------------------------------------------------------------

  /**
   * Abstract class for file contents.
   * @author Bastian Lemke
   */
  public abstract class AbstrCont {
    /** offset inside the regular file. */
    final long o;
    /** size of the text section. */
    final int s;

    /**
     * Constructor.
     * @param position offset inside the regular file.
     * @param byteCount size of the text section (byte count).
     */
    public AbstrCont(final long position, final int byteCount) {
      o = position;
      s = byteCount;
    }

    /**
     * Returns the offset.
     * @return the offset.
     */
    public long getOffset() {
      return o;
    }

    /**
     * Returns the size.
     * @return the size.
     */
    public int getSize() {
      return s;
    }
  }

  /**
   * Text content of a file.
   * @author Bastian Lemke
   */
  public final class TextContent extends AbstrCont {
    /** byte array, containing the text. */
    private final byte[] t;

    /**
     * Constructor.
     * @param position offset inside the regular file.
     * @param byteCount size of the text section (byte count).
     * @param text byte array, containing the text.
     */
    TextContent(final long position, final int byteCount, final byte[] text) {
      super(position, byteCount);
      t = text;
    }

    /**
     * Returns the text.
     * @return the text.
     */
    public byte[] getText() {
      return t;
    }
  }

  /**
   * XML content of a file.
   * @author Bastian Lemke
   */
  public final class XMLContent extends AbstrCont {
    /** byte array, containing the xml as text. */
    private final String t;

    /**
     * Constructor.
     * @param position offset inside the regular file.
     * @param byteCount size of the text section (byte count).
     * @param xml String, containing the xml document/fragment as text.
     */
    XMLContent(final long position, final int byteCount, final String xml) {
      super(position, byteCount);
      t = xml;
    }

    /**
     * Returns the xml content as string.
     * @return the xml content.
     */
    public String asString() {
      return t;
    }
  }

  /** All namespaces used in the DeepFile. */
  public enum NS {
        /** XML schema namespace. */
    XS("xs", "http://www.w3.org/2001/XMLSchema"),
        /** XML schema instance namespace. */
    XSI("xsi", "http://www.w3.org/2001/XMLSchema-instance"),
        /** DeepFS filesystem namespace. */
    DEEPURL("", "http://www.deepfs.org/fs/1.0/"),
        /** DeepFS metadata namespace. */
    FSMETA("", "http://www.deepfs.org/fsmeta/1.0/"),
        /** Dublin Core metadata terms namespace. */
    DCTERMS("", "http://purl.org/dc/terms/");

    /** The namespace prefix. */
    private byte[] prefix;
    /** The namespace URI. */
    private byte[] uri;

    /**
     * Initializes a namespace instance.
     * @param p the prefix.
     * @param u the URI.
     */
    NS(final String p, final String u) {
      prefix = token(p);
      uri = token(u);
    }

    @Override
    public String toString() {
      final StringBuilder str = new StringBuilder("xmlns");
      if(prefix.length > 0) {
        str.append(':');
        str.append(string(prefix));
      }
      str.append("=\"");
      str.append(string(uri));
      str.append("\"");
      return str.toString();
    }

    /**
     * Converts the xml element into a byte array containing the correct
     * namespace prefix.
     * @param element the xml element to convert.
     * @return the converted element as byte array;
     */
    public byte[] tag(final String element) {
      return tag(token(element));
    }

    /**
     * Converts the xml element into a byte array containing the correct
     * namespace prefix.
     * @param element the xml element to convert.
     * @return the converted element as byte array;
     */
    public byte[] tag(final byte[] element) {
      if(prefix.length == 0) return element;
      return concat(prefix, new byte[] { ':'}, element);
    }
  }

  /** DeepFS tag in fs namespace. */
  public static final byte[] DEEPFS_NS = NS.DEEPURL.tag(DEEPFS);
  /** Directory tag in fs namespace. */
  public static final byte[] DIR_NS = NS.DEEPURL.tag(DIR);
  /** File tag in fs namespace. */
  public static final byte[] FILE_NS = NS.DEEPURL.tag(FILE);
  /** Content tag in fs namespace. */
  public static final byte[] CONTENT_NS = NS.DEEPURL.tag(CONTENT);
  /** Text content tag in fs namespace. */
  public static final byte[] TEXT_CONTENT_NS = NS.DEEPURL.tag(TEXT_CONTENT);
  /** XML content tag in fs namespace. */
  public static final byte[] XML_CONTENT_NS = NS.DEEPURL.tag(XML_CONTENT);
}
