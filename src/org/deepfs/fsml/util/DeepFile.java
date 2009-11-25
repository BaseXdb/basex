package org.deepfs.fsml.util;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.deepfs.jfuse.JFUSEAdapter.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.data.Data;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Type;
import org.basex.util.Atts;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.deepfs.fs.DeepFS;
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

  /** The file system attributes for the file. */
  private Atts fsAtts;
  /** Map, containing all metadata key-value pairs for the current file. */
  private final TreeMap<MetaElem, ArrayList<byte[]>> metaElements;
  /** List with all file fragments (fs:content elements). */
  private final ArrayList<DeepFile> fileFragments;
  /** All text contents that are extracted from the file. */
  private final ArrayList<TextContent> textContents;
  /** All xml contents that are extracted from the file as string. */
  private final ArrayList<XMLContent> xmlContents;

  /** Flag, if metadata should be extracted from the current file. */
  private final boolean fsmeta;
  /** Flag, if metadata extraction is finished. */
  private boolean metaFinished;
  /** Flag, if text contents should be extracted from the current file. */
  private final boolean fscont;
  /** Flag, if xml contents should be extracted from the current file. */
  private final boolean fsxml;
  /** Maximum number of bytes to extract from text contents. */
  private final int fstextmax;
  /** Offset of the deep file inside the current regular file. */
  private final long offset;
  /** Size of the deep file in bytes (-1 means unknown size). */
  private long size;

  // ---------------------------------------------------------------------------
  // ----- constructors. -------------------------------------------------------
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
   *  boolean, int)}
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
   *  boolean, int)}
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
   * Creates a DeepFile object for a buffered file channel that can be used to
   * extract metadata and text/xml contents. By default, metadata and all
   * contents will be extracted.
   * </p>
   * <p>
   * This constructor should only be used to parse a single file. Use
   * {@link #DeepFile(ParserRegistry, BufferedFileChannel, boolean,
   *   boolean, boolean, int)}
   * for parsing several files for better performance.
   * </p>
   * @param f the {@link BufferedFileChannel}.
   * @throws IOException if any error occurs.
   */
  public DeepFile(final BufferedFileChannel f) throws IOException {
    this(new ParserRegistry(), f, true, true, true, 0, f.size(),
        DEFAULT_TEXT_MAX);
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
   * {@link #DeepFile(ParserRegistry, BufferedFileChannel, boolean,
   *   boolean, boolean, int)}
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
    metaFinished = !fsmeta;
    fsxml = xmlContent;
    fscont = textContent;
    fstextmax = textMax;
    offset = position;
    size = contentSize;
    fileFragments = new ArrayList<DeepFile>();
    metaElements = fsmeta ? new TreeMap<MetaElem, ArrayList<byte[]>>() : null;
    textContents = fscont ? new ArrayList<TextContent>() : null;
    xmlContents = fsxml ? new ArrayList<XMLContent>() : null;
  }

  /**
   * Clones the DeepFile to map only a fragment of the file.
   * @param df the DeepFile to clone
   * @param contentSize the size of the underlying {@link BufferedFileChannel}.
   * @throws IOException if any error occurs.
   */
  private DeepFile(final DeepFile df, final int contentSize)
      throws IOException {
    parser = df.parser;
    bfc = df.bfc.subChannel(df.bfc.getFileName(), contentSize);
    fsmeta = df.fsmeta;
    metaFinished = !fsmeta;
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

  // ---------------------------------------------------------------------------
  // ----- metadata/content extraction -----------------------------------------
  // ---------------------------------------------------------------------------

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
    finish();
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

  // ---------------------------------------------------------------------------
  // ----- getters for deep file settings and extracted metadata/contents ------
  // ---------------------------------------------------------------------------

  // ----- extraction settings -------------------------------------------------

  /**
   * Returns true, if metadata should be extracted.
   * @return true, if metadata should be extracted.
   */
  public boolean extractMeta() {
    return !metaFinished;
  }

  /**
   * Returns true, if text contents should be extracted.
   * @return true, if text contents should be extracted.
   */
  public boolean extractText() {
    return fscont;
  }

  /**
   * Returns true, if xml contents should be extracted.
   * @return true, if xml contents should be extracted.
   */
  public boolean extractXML() {
    return fsxml;
  }

  /**
   * Returns the number of bytes that should be extracted from text and xml
   * contents.
   * @return the maximum number of bytes to extract.
   */
  public int maxTextSize() {
    return fstextmax;
  }

  // ----- deep file properties and metadata/contents --------------------------

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
   * Returns the file system attributes for the deep file. The file system
   * attributes are extracted after finishing the metadata extraction.
   * @return the file system attributes or <code>null</code> if the metadata
   *         extraction was not finished yet.
   * @see #finishMetaExtraction()
   */
  public Atts getFSAtts() {
    return fsAtts;
  }

  /**
   * Returns all metadata key-value pairs or <code>null</code> if metadata
   * extraction is disabled.
   * @return the metadata.
   */
  public TreeMap<MetaElem, ArrayList<byte[]>> getMeta() {
    return metaElements;
  }

  /**
   * Returns all subfiles.
   * @return the subfiles.
   */
  public DeepFile[] getContent() {
    return fileFragments.toArray(new DeepFile[fileFragments.size()]);
  }

  /**
   * Returns all text sections or <code>null</code> if no text content
   * extraction is disabled.
   * @return all text sections.
   */
  public TextContent[] getTextContents() {
    return textContents == null ? null
        : textContents.toArray(new TextContent[textContents.size()]);
  }

  /**
   * Returns all xml sections or <code>null</code> if xml extraction is
   * disabled.
   * @return all xml sections.
   */
  public XMLContent[] getXMLContents() {
    return xmlContents == null ? null
        : xmlContents.toArray(new XMLContent[xmlContents.size()]);
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
    if(metaFinished) return;
    addMeta0(MetaElem.TYPE, type.get());
  }

  /**
   * Sets the MIME type of the file. A previously set value will be replaced.
   * @param format the MIME type.
   */
  public void setFileFormat(final MimeType format) {
    if(metaFinished) return;
    addMeta0(MetaElem.FORMAT, format.get());
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
    if(metaFinished || value.length == 0) return;
    if(e.equals(MetaElem.TYPE) | e.equals(MetaElem.FORMAT)) {
      Main.bug(
          "The metadata attributes " + MetaElem.TYPE + " and "
          + MetaElem.FORMAT
          + " must not be set by an addMetaElem() method." +
          " Use setMetaType() and setFormat() instead.");
    }
    if(dataType != null) e.refineDataType(dataType);
    else e.reset();
    addMeta0(e, value);
  }

  /**
   * Add the metadata to the TreeMap.
   * @param e metadata element (the key).
   * @param value value as byte array. Must contain only correct UTF-8 values!
   */
  private void addMeta0(final MetaElem e, final byte[] value) {
    final TokenBuilder tb = new TokenBuilder(value);
    tb.chop();
    final byte[] data = tb.finish();
    if(data.length == 0) return;
    final ArrayList<byte[]> vals;
    if(metaElements.containsKey(e)) {
      if(!e.isMultiVal()) {
        Main.debug(
            "Failed to add metadata value. Multiple values are forbidden for "
            + "attribute % (%).", e, bfc.getFileName());
        return;
      }
      vals = metaElements.get(e);
    } else {
      vals = new ArrayList<byte[]>();
      metaElements.put(e, vals);
    }
    vals.add(data);
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

    addMeta(elem, Token.token(value), Type.SHR);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value integer value.
   */
  public void addMeta(final MetaElem elem, final int value) {
    if(!elem.getType().instance(Type.ITR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (int).");
    addMeta(elem, Token.token(value), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value long value.
   */
  public void addMeta(final MetaElem elem, final long value) {
    if(!elem.getType().instance(Type.LNG)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (long).");
    addMeta(elem, Token.token(value), Type.LNG);
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
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param xgc calendar value.
   */
  public void addMeta(final MetaElem elem, final XMLGregorianCalendar xgc) {
    Type t = elem.getType();
    QName st = xgc.getXMLSchemaType();
    if((t.instance(Type.DAT) && !st.equals(DatatypeConstants.DATE))
        || (t.instance(Type.YEA) && !st.equals(DatatypeConstants.GYEAR))) {
      Main.debug("Invalid data type for metadata element "
          + elem + " (expected " + t + " got " + st.getLocalPart() + ").");
    }
    addMeta(elem, token(xgc.toXMLFormat()), null);
  }

  /**
   * Finish the extraction of metadata and extracts the file system attributes.
   * @throws IOException if any error occurs while extracting the file system
   *           attributes.
   */
  public void finishMetaExtraction() throws IOException {
    metaFinished = true;
    final ArrayList<byte[]> type = metaElements.get(MetaElem.TYPE);
    if(type != null && Token.eq(type.get(0), FileType.MESSAGE.get())) {
      ArrayList<byte[]> creator = metaElements.get(MetaElem.CREATOR_NAME);
      boolean err = false;
      if(creator != null) {
        if(creator.size() > 1) err = true;
        addMeta0(MetaElem.SENDER_NAME, creator.get(0));
        metaElements.remove(MetaElem.CREATOR_NAME);
      }
      creator = metaElements.get(MetaElem.CREATOR_EMAIL);
      if(creator != null) {
        if(creator.size() > 1) err = true;
        addMeta0(MetaElem.SENDER_EMAIL, creator.get(0));
        metaElements.remove(MetaElem.CREATOR_EMAIL);
      }
      if(err) Main.debug("Found multiple creators for a message. " +
          "All but the first one are dropped (%).", bfc.getFileName());
    }
    if(fsAtts == null) fsAtts = extractFSAtts();
  }

  /**
   * Returns the string value for a {@link MetaElem} that was previously added.
   * @param elem the metadata element.
   * @return the metadata value as string.
   */
  public String[] getValueAsString(final MetaElem elem) {
    final ArrayList<byte[]> vals = metaElements.get(elem);
    if(vals == null) return null;
    final int max = vals.size();
    final String[] strings = new String[max];
    for(int i = 0; i < max; i++)
      strings[i] = Token.string(vals.get(0));
    return strings;
  }

  /**
   * Returns true, if a value is set for the given metadata element.
   * @param elem the metadata element.
   * @return true, if a value is set.
   */
  public boolean isMetaSet(final MetaElem elem) {
    return metaElements.containsKey(elem);
  }

  /**
   * Returns true, if the file type is set for the current deep file.
   * @return true, if the file type is set.
   */
  public boolean isFileTypeSet() {
    return metaElements.containsKey(MetaElem.TYPE);
  }

  // ----- contents ------------------------------------------------------------

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
    final byte[] data;
    if(text.size() > fstextmax) {
      data = new byte[fstextmax];
      System.arraycopy(text.finish(), 0, data, 0, fstextmax);
    } else data = text.finish();
    textContents.add(new TextContent(position, byteCount, data));
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
    byte[] xmlContent;
    try {
      final Result res = qp.query();
      res.serialize(ser);
      xmlContent = baos.toByteArray();
      ser.close();
      po.close();
    } catch(final QueryException e) {
      xmlContent = new byte[] { };
    }
    if(xmlContent.length == 0) return;
    if(xmlContent.length > fstextmax) {
      if(!fscont) return;
      final byte[] text = new byte[fstextmax];
      System.arraycopy(xmlContent, 0, text, 0, fstextmax);
      textContents.add(new TextContent(position, fstextmax, text));
    } else {
      xmlContents.add(new XMLContent(position, byteCount,
          new String(xmlContent)));
    }
  }

  // ---------------------------------------------------------------------------

  /**
   * Extract the file system attributes from the file.
   * @return the file system attributes.
   * @throws IOException if any error occurs while reading from the file.
   */
  private Atts extractFSAtts() throws IOException {
    final File f = bfc.getAssociatedFile();
    final String name = f.getName();
    final byte[] time = token(System.currentTimeMillis());

    /** Temporary attribute array. */
    final Atts atts = new Atts();
    atts.reset();
    atts.add(NAME, token(name));
    atts.add(SIZE, token(bfc.size()));
    if(f.isDirectory()) atts.add(MODE, token(getSIFDIR() | 0755));
    else atts.add(MODE, token(getSIFREG() | 0644));
    final ArrayList<byte[]> uid = metaElements.get(MetaElem.FS_OWNER_USER_ID);
    if(uid != null) {
      atts.add(UID, uid.get(0));
      metaElements.remove(MetaElem.FS_OWNER_USER_ID);
    } else atts.add(UID, token(getUID()));
    final ArrayList<byte[]> gid = metaElements.get(MetaElem.FS_OWNER_GROUP_ID);
    if(gid != null) {
      atts.add(GID, gid.get(0));
      metaElements.remove(MetaElem.FS_OWNER_GROUP_ID);
    } else atts.add(GID, token(getGID()));
    atts.add(ATIME, time);
    atts.add(CTIME, time);
    atts.add(MTIME, token(f.lastModified()));
    atts.add(NLINK, token("1"));
    atts.add(SUFFIX, DeepFS.getSuffix(name));
    return atts;
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

  /**
   * Finishes the deep file.
   * @throws IOException if any error occurs.
   * @see BufferedFileChannel#finish()
   */
  public void finish() throws IOException {
    finishMetaExtraction();
    bfc.finish();
  }

  // ---------------------------------------------------------------------------

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

  /**
   * Returns the xml representation for this deep file.
   * @return the xml representation as string.
   * @throws IOException if any error occurs.
   */
  public String toXML() throws IOException {
    return FSMLSerializer.serialize(this);
  }

  @Override
  public String toString() {
    try {
      return toXML();
    } catch(final IOException e) {
      return bfc.getFileName() + "(" + metaElements.size()
          + " metadata attributes, " + textContents.size() + " text sections, "
          + xmlContents.size() + " xml sections and " + fileFragments.size()
          + " content sections)";
    }
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

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

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
