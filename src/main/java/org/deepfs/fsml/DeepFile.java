package org.deepfs.fsml;

import static org.deepfs.fs.DeepFS.*;
import static org.basex.util.Token.*;
import static org.deepfs.jfuse.JFUSEAdapter.*;
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
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Type;
import org.basex.util.Atts;
import org.basex.util.XMLToken;
import org.deepfs.fs.DeepFS;
import org.deepfs.fsml.parsers.IFileParser;
import org.deepfs.fsml.ser.FSMLSerializer;

/**
 * <p>
 * Storage for metadata information and contents for a single file.
 * </p>
 * <p>
 * A DeepFile can represent a regular file in the file system or a "subfile"
 * that is stored inside another file, e.g. a file in a ZIP-file or a picture
 * that is included in an ID3 tag.
 * </p>
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Bastian Lemke
 */
public final class DeepFile {
  /**
   * A reference to the parser registry that can be used to parse file
   * fragments.
   */
  private final ParserRegistry registry;
  /**
   * The file channel to access the file. This channel links the DeepFile object
   * with a file in the file system.
   */
  private final BufferedFileChannel bfc;

  /** The file system attributes for the file. */
  private Atts fsAtts;
  /** Map, containing all metadata key-value pairs for the current file. */
  private final TreeMap<MetaElem, ArrayList<String>> metaElements;
  /** List with all file fragments (fs:content elements). */
  private final ArrayList<DeepFile> fileFragments;
  /** All text contents that are extracted from the file. */
  private final ArrayList<Content> textContents;
  /** All xml contents that are extracted from the file as string. */
  private final ArrayList<Content> xmlContents;

  /** Default database context. */
  private static final Context DEFAULT_CONTEXT = new Context();
  /** The database context. */
  private final Context context;

  /** Flag, if metadata extraction is finished. */
  private boolean metaFinished;
  /** Offset of the deep file inside the current regular file. */
  private final long offset;
  /** Size of the deep file in bytes (-1 means unknown size). */
  private long size;

  static {
    final Prop p = DEFAULT_CONTEXT.prop;
    p.set(Prop.FSMETA, true);
    p.set(Prop.FSCONT, true);
    p.set(Prop.FSXML, true);
  }

  // ---------------------------------------------------------------------------
  // ----- constructors. -------------------------------------------------------
  // ---------------------------------------------------------------------------

  /*
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
   * {@link #DeepFile(ParserRegistry, BufferedFileChannel, Context)}
   * for parsing several files for better performance.
   * </p>
   * @param file the name of the associated file in the file system
   * @throws IOException if any I/O error occurs
   * @see IFileParser#extract(DeepFile)
  public DeepFile(final String file) throws IOException {
    this(new File(file));
  }
   */

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
   * {@link #DeepFile(ParserRegistry, BufferedFileChannel, Context)}
   * for parsing several files for better performance.
   * </p>
   * @param file the associated file in the file system
   * @throws IOException if any I/O error occurs
   * @see IFileParser#extract(DeepFile)
   */
  public DeepFile(final File file) throws IOException {
    this(file.isFile() ? new BufferedFileChannel(file) : null);
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
   * {@link #DeepFile(ParserRegistry, BufferedFileChannel, Context)}
   * for parsing multiple files for better performance.
   * </p>
   * @param f the {@link BufferedFileChannel}
   * @throws IOException if any error occurs
   */
  public DeepFile(final BufferedFileChannel f) throws IOException {
    this(new ParserRegistry(), f, DEFAULT_CONTEXT, 0, f.size());
  }

  /**
   * <p>
   * Constructor.
   * </p>
   * <p>
   * Uses the given parser registry to retrieve the corresponding parser for the
   * file which is represented by the buffered file channel. Depending on the
   * properties of the context, metadata, text content and xml content is
   * extracted.
   * </p>
   * <p>
   * The properties can be set as follows:<br />
   * {@code ctx.prop.set(Prop.FSMETA, true); // extract metadata}<br/>
   * {@code ctx.prop.set(Prop.FSCONT, true); // extract text content}<br/>
   * {@code ctx.prop.set(Prop.FSXML, true); // extract xml content}<br/>
   * {@code ctx.prop.set(Prop.FSTEXTMAX, 10240); // amount of text/xml content
   * to extract (in bytes)}
   * <br/>
   * </p>
   * @param parserRegistry a reference to the parser registry that can be used
   *          to parse file fragments
   * @param bufferedFileChannel {@link BufferedFileChannel} to access the file
   * @param ctx the database context
   * @throws IOException if any error occurs
   */
  public DeepFile(final ParserRegistry parserRegistry,
      final BufferedFileChannel bufferedFileChannel, final Context ctx)
      throws IOException {
    this(parserRegistry, bufferedFileChannel, ctx,
        bufferedFileChannel.getOffset(), bufferedFileChannel.size());
  }

  /**
   * Constructor.
   * @param parserRegistry a reference to the parser registry that can be used
   *          to parse file fragments
   * @param bufferedFileChannel {@link BufferedFileChannel} to access the file
   * @param ctx the database context
   * @param position the position inside the file in the file system
   * @param contentSize the size of the deep file
   */
  private DeepFile(final ParserRegistry parserRegistry,
      final BufferedFileChannel bufferedFileChannel, final Context ctx,
      final long position, final long contentSize) {
    registry = parserRegistry;
    bfc = bufferedFileChannel;
    context = ctx;
    final Prop p = ctx.prop;
    final boolean meta = p.is(Prop.FSMETA);
    metaFinished = !meta;
    offset = position;
    size = contentSize;
    fileFragments = new ArrayList<DeepFile>();
    metaElements = meta ? new TreeMap<MetaElem, ArrayList<String>>() : null;
    textContents = p.is(Prop.FSCONT) ? new ArrayList<Content>() : null;
    xmlContents = p.is(Prop.FSXML) ? new ArrayList<Content>() : null;
  }

  /**
   * Clones the DeepFile to map only a fragment of the file.
   * @param df the DeepFile to clone
   * @param contentSize the size of the underlying {@link BufferedFileChannel}
   * @throws IOException if any error occurs
   */
  private DeepFile(final DeepFile df, final int contentSize)
      throws IOException {
    registry = df.registry;
    bfc = df.bfc.subChannel("", contentSize);
    context = df.getContext();
    metaFinished = df.metaFinished;
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
   * Extracts metadata and text/xml contents from the associated file.
   * @throws IOException if any error occurs while reading from the file
   */
  public void extract() throws IOException {
    if(bfc == null) return;
    final String fname = bfc.getFileName();
    String suffix = "";
    if(fname.indexOf('.') != -1) {
      final int dot = fname.lastIndexOf('.');
      suffix = fname.substring(dot + 1).toLowerCase();
    }
    process(this, suffix);
    finish();
  }

  /**
   * Calls the fallback parser for the associated file to extract text contents.
   * @throws IOException if any error occurs while reading from the file
   * @throws ParserException if the fallback parser could not be loaded
   */
  public void fallback() throws IOException, ParserException {
    final IFileParser p = registry.getFallbackParser();
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
   * @return true, if metadata should be extracted
   */
  public boolean extractMeta() {
    return !metaFinished;
  }

  /**
   * Returns true, if text contents should be extracted.
   * @return true, if text contents should be extracted
   */
  public boolean extractText() {
    return context.prop.is(Prop.FSCONT);
  }

  /**
   * Returns true, if xml contents should be extracted.
   * @return true, if xml contents should be extracted
   */
  public boolean extractXML() {
    return context.prop.is(Prop.FSXML);
  }

  /**
   * Returns the number of bytes that should be extracted from text and xml
   * contents.
   * @return the maximum number of bytes to extract
   */
  public int maxTextSize() {
    return context.prop.num(Prop.FSTEXTMAX);
  }

  // ----- deep file properties and metadata/contents --------------------------

  /**
   * Returns the database context.
   * @return the database context
   */
  public Context getContext() {
    return context;
  }

  /**
   * Returns the offset of the deep file inside the regular file in the file
   * system.
   * @return the offset
   */
  public long getOffset() {
    return offset;
  }

  /**
   * Returns the size of the deep file.
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * Returns the file system attributes for the deep file. The file system
   * attributes are extracted after finishing the metadata extraction.
   * @return the file system attributes or {@code null} if the metadata
   *         extraction was not finished yet
   * @see #finishMetaExtraction()
   */
  public Atts getFSAtts() {
    return fsAtts;
  }

  /**
   * Returns all metadata key-value pairs or {@code null} if metadata
   * extraction is disabled.
   * @return the metadata
   */
  public TreeMap<MetaElem, ArrayList<String>> getMeta() {
    return metaElements;
  }

  /**
   * Returns all subfiles.
   * @return the subfiles
   */
  public DeepFile[] getContent() {
    return fileFragments.toArray(new DeepFile[fileFragments.size()]);
  }

  /**
   * Returns all text sections or {@code null} if no text content
   * extraction is disabled.
   * @return all text sections
   */
  public Content[] getTextContents() {
    return textContents == null ? null :
      textContents.toArray(new Content[textContents.size()]);
  }

  /**
   * Returns all xml sections or {@code null} if xml extraction is
   * disabled.
   * @return all xml sections
   */
  public Content[] getXMLContents() {
    return xmlContents == null ? null :
      xmlContents.toArray(new Content[xmlContents.size()]);
  }

  // ---------------------------------------------------------------------------
  // ----- Methods for parser implementations ----------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Returns the associated {@link BufferedFileChannel} that links this
   * {@link DeepFile} with a file in the file system.
   * @return the {@link BufferedFileChannel}
   */
  public BufferedFileChannel getBufferedFileChannel() {
    return bfc;
  }

  // ----- metadata ------------------------------------------------------------

  /**
   * Sets the type of the file (e.g. audio, video, ...).
   * @param type the file type
   */
  public void setFileType(final FileType type) {
    if(metaFinished) return;
    addMeta0(MetaElem.TYPE, type.toString());
  }

  /**
   * Sets the MIME type of the file. A previously set value will be replaced.
   * @param format the MIME type
   */
  public void setFileFormat(final MimeType format) {
    if(metaFinished) return;
    addMeta0(MetaElem.FORMAT, format.toString());
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param e metadata element (the key)
   * @param value the value to add
   * @param type the xml data type to set for this metadata element or
   *          {@code null} if the default data type should be used
   */
  private void addMeta(final MetaElem e, final String value, final Type type) {
    if(metaFinished || value.isEmpty()) return;

    if(e.equals(MetaElem.TYPE) || e.equals(MetaElem.FORMAT)) {
      Main.debug("The metadata attributes " + MetaElem.TYPE + " and "
          + MetaElem.FORMAT + " must not be set by an addMetaElem() method."
          + " Use setMetaType() and setFormat() instead.");
      return;
    }
    if(type != null) e.refineDataType(type);
    else e.reset();
    addMeta0(e, value);
  }

  /**
   * Adds the metadata to the TreeMap.
   * @param e metadata element (the key)
   * @param value the value to add
   */
  private void addMeta0(final MetaElem e, final String value) {
    final String s = value.trim();
    if(s.isEmpty()) return;

    final ArrayList<String> vals;
    if(metaElements.containsKey(e)) {
      if(!e.isMultiVal()) {
        Main.debug(
            "DeepFile: Failed to add metadata value. Multiple values are " +
            "forbidden for " + "metadata element % (%).", e, bfc.getFileName());
        return;
      }
      vals = metaElements.get(e);
    } else {
      vals = new ArrayList<String>();
      metaElements.put(e, vals);
    }
    vals.add(s);
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param elem metadata element (the key). Must be a string attribute
   * @param value string value as byte array
   */
  public void addMeta(final MetaElem elem, final byte[] value) {
    if(value == null) return;
    if(!Type.STR.instance(elem.getType()))
      metaDebug(elem, "string - as byte array");
    else addMeta(elem, string(clean(value, true)), null);
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param elem metadata element (the key)
   * @param value string value
   */
  public void addMeta(final MetaElem elem, final String value) {
    if(value == null) return;
    if(!checkType(elem, Type.STR)) return;
    addMeta(elem, string(clean(token(value), true)), null);
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param elem metadata element (the key)
   * @param value integer value
   */
  public void addMeta(final MetaElem elem, final short value) {
    if(!checkType(elem, Type.SHR)) return;
    addMeta(elem, String.valueOf(value), Type.SHR);
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param elem metadata element (the key)
   * @param value integer value
   */
  public void addMeta(final MetaElem elem, final int value) {
    if(!Type.INT.instance(elem.getType())) {
      if(value <= Short.MAX_VALUE) addMeta(elem, (short) value);
      else metaDebug(elem, "integer");
    } else addMeta(elem, String.valueOf(value), null);
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param elem metadata element (the key)
   * @param value long value
   */
  public void addMeta(final MetaElem elem, final long value) {
    if(!Type.LNG.instance(elem.getType())) {
      if(value <= Integer.MAX_VALUE) addMeta(elem, (int) value);
      else metaDebug(elem, "long");
    } else addMeta(elem, String.valueOf(value), Type.LNG);
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param elem metadata element (the key)
   * @param value double value
   */
  public void addMeta(final MetaElem elem, final double value) {
    if(!checkType(elem, Type.DBL)) return;
    addMeta(elem, String.valueOf(value), Type.DBL);
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param elem metadata element (the key)
   * @param value duration value
   */
  public void addMeta(final MetaElem elem, final Duration value) {
    if(value == null) return;
    if(!checkType(elem, Type.DUR)) return;
    addMeta(elem, String.valueOf(value.toString()), null);
  }

  /**
   * Adds a metadata key-value pair for the current file.
   * @param elem metadata element (the key)
   * @param xgc calendar value
   */
  public void addMeta(final MetaElem elem, final XMLGregorianCalendar xgc) {
    if(xgc == null) return;
    final Type t = elem.getType();
    final QName st = xgc.getXMLSchemaType();
    if(Type.DAT.instance(t) && !st.equals(DatatypeConstants.DATE)
        || Type.YEA.instance(t) && !st.equals(DatatypeConstants.GYEAR))
      metaDebug(elem, st.getLocalPart());
    else {
      try {
        addMeta(elem, xgc.toXMLFormat(), null);
      } catch(final IllegalStateException ex) {
        Main.debug("DeepFile: Invalid date (file: %, error message: %)",
            bfc.getFileName(), ex.getMessage());
      }
    }
  }

  /**
   * Checks if the found type is valid for the given metadata element.
   * @param elem the metadata element
   * @param foundType the found data type
   * @return true if it is valid, false otherwise
   */
  private boolean checkType(final MetaElem elem, final Type foundType) {
    if(foundType.instance(elem.getType())) return true;
    metaDebug(elem, foundType.toString());
    return false;
  }

  /**
   * Prints a debug message if an attempt was made to set an invalid value for a
   * metadata element.
   * @param elem the current metadata element
   * @param foundType the type that was tried to set
   */
  private void metaDebug(final MetaElem elem, final String foundType) {
    Main.debug("DeepFile: Invalid data type (file: %, metadata element: %, " +
        "expected data type: %, found data type: %).", bfc.getFileName(), elem,
        elem.getType(), foundType);
  }

  /**
   * Finishes the extraction of metadata and extracts the file system
   * attributes.
   * @throws IOException if any error occurs while extracting the file system
   *           attributes
   */
  public void finishMetaExtraction() throws IOException {
    metaFinished = true;
    if(metaElements != null) {
      final ArrayList<String> type = metaElements.get(MetaElem.TYPE);
      if(type != null && type.get(0).equals(FileType.MESSAGE.toString())) {
        ArrayList<String> creator = metaElements.get(MetaElem.CREATOR_NAME);
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
    }
    if(fsAtts == null) fsAtts = extractFSAtts();
  }

  /*
   * Returns the string values for the {@link MetaElem}.
   * @param elem the metadata element
   * @return the metadata values as Strings
  public String[] getValues(final MetaElem elem) {
    final ArrayList<String> vals = metaElements.get(elem);
    if(vals == null) return null;
    final int max = vals.size();
    final String[] strings = new String[max];
    return strings;
  }
   */

  /**
   * Returns true, if a value is set for the given metadata element.
   * @param elem the metadata element
   * @return true, if a value is set
   */
  public boolean isMetaSet(final MetaElem elem) {
    return metaElements.containsKey(elem);
  }

  /**
   * Returns true, if the file type is set for the current deep file.
   * @return true, if the file type is set
   */
  public boolean isFileTypeSet() {
    return metaElements.containsKey(MetaElem.TYPE);
  }

  // ----- contents ------------------------------------------------------------

  /**
   * <p>
   * Adds a text section. <b>{@code text} MUST contain only valid UTF-8
   * characters!</b> Otherwise the generated XML document may be not
   * well-formed.
   * </p>
   * @param position the absolute position of the first byte of the file
   *          fragment represented by this content element inside the current
   *          file. A negative value stands for an unknown offset
   * @param byteCount the size of the content element
   * @param text the text to add
   */
  public void addText(final long position, final int byteCount,
      final String text) {
    if(!extractText()) return;
    final String s = text.trim();
    if(s.isEmpty()) return;
    for(int i = 0; i < s.length(); ++i)
      if(!XMLToken.valid(s.charAt(i))) {
        unknown();
        return;
      }
    final int max = context.prop.num(Prop.FSTEXTMAX);
    final Content c = new Content(position, byteCount,
        s.length() > max ? s.substring(0, max) : s);
    textContents.add(c);
  }

  /**
   * Adds a xml document or fragment to the DeepFile.
   * @param position offset of the xml document/fragment inside the file
   * @param byteCount number of bytes of the xml document/fragment
   * @param data the xml document/fragment
   * @throws IOException if any error occurs
   */
  public void addXML(final long position, final int byteCount,
      final Data data) throws IOException {
    final ArrayOutput co = new ArrayOutput();
    final XMLSerializer ser = new XMLSerializer(co);
    final Context ctx = new Context();
    ctx.openDB(data);
    final QueryProcessor qp = new QueryProcessor("/", ctx);
    try {
      final Result res = qp.execute();
      res.serialize(ser);
      final String xml = co.toString();
      ser.close();
      addXML(position, byteCount, xml);
    } catch(final QueryException ex) { return; }
  }

  /**
   * Adds a xml document or fragment to the DeepFile.
   * @param pos offset of the xml document/fragment inside the file
   * @param byteCount number of bytes of the xml document/fragment
   * @param xml the xml document/fragment
   */
  private void addXML(final long pos, final int byteCount, final String xml) {
    if(!extractXML() || xml.isEmpty()) return;
    if(xml.length() > context.prop.num(Prop.FSTEXTMAX))
      addText(pos, byteCount, xml);
    else {
      for(int i = 0; i < xml.length(); ++i)
        if(!XMLToken.valid(xml.charAt(i))) {
          unknown();
          return;
        }
      xmlContents.add(new Content(pos, byteCount, xml));
    }
  }

  /** Sets format and type to 'unknown'. */
  private void unknown() {
    final ArrayList<String> val = new ArrayList<String>();
    val.add(MimeType.UNKNOWN.toString());
    metaElements.put(MetaElem.FORMAT, val);
    val.clear();
    val.add(FileType.UNKNOWN_TYPE.toString());
    metaElements.put(MetaElem.TYPE, val);
  }

  // ---------------------------------------------------------------------------

  /**
   * Extracts the file system attributes from the file.
   * @return the file system attributes
   * @throws IOException if any error occurs while reading from the file
   */
  private Atts extractFSAtts() throws IOException {
    final File f = bfc.getAssociatedFile();
    final String name = f.getName();
    final byte[] time = token(System.currentTimeMillis());

    /** Temporary attribute array. */
    final Atts atts = new Atts();
    atts.add(NAME, token(name));
    atts.add(SIZE, token(bfc.size()));
    if(f.isDirectory()) atts.add(MODE, token(getSIFDIR() | 0755));
    else atts.add(MODE, token(getSIFREG() | 0644));

    String uidVal = null;
    String gidVal = null;
    if(metaElements != null) {
      final ArrayList<String> uid = metaElements.get(MetaElem.FS_OWNER_USER_ID);
      if(uid != null) {
        uidVal = uid.get(0);
        metaElements.remove(MetaElem.FS_OWNER_USER_ID);
      }
      final ArrayList<String> gid =
        metaElements.get(MetaElem.FS_OWNER_GROUP_ID);
      if(gid != null) {
        gidVal = gid.get(0);
        metaElements.remove(MetaElem.FS_OWNER_GROUP_ID);
      }
    }
    atts.add(UID, uidVal == null ? token(getUID()) : token(uidVal));
    atts.add(GID, gidVal == null ? token(getGID()) : token(gidVal));

    atts.add(ATIME, time);
    atts.add(CTIME, time);
    atts.add(MTIME, token(f.lastModified()));
    atts.add(NLINK, ONE);
    atts.add(SUFFIX, DeepFS.getSuffix(name));
    return atts;
  }

  /**
   * Processes a DeepFile and extracts metadata/content.
   * @param df the DeepFile to process
   * @param suffix the file suffix(es). More than one suffix means that the file
   *          type is unknown. All given suffixes will be tested
   * @throws IOException if any error occurs while reading from the file
   */
  private void process(final DeepFile df, final String... suffix)
      throws IOException {
    IFileParser p = null;
    for(final String s : suffix) {
      try {
        p = registry.getParser(s.toLowerCase());
      } catch(final ParserException ex) { /* ignore and continue ... */ }
      if(p != null) break;
    }
    if(p == null) {
      try {
        p = registry.getFallbackParser();
      } catch(final ParserException ex) { /* ignore and continue ... */ }
    }
    if(p != null) p.extract(df);
  }

  /**
   * Finishes the deep file.
   * @throws IOException if any error occurs
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
   * @param fileName the name of the subfile
   * @param fileSize the size of the subfile
   * @param suffix the file suffix(es). More than one suffix means that the file
   *          type is unknown. All given suffixes will be tested
   * @return the subfile
   * @throws IOException if any error occurs
   * @see #newContentSection(long)
   */
  public DeepFile subfile(final String fileName, final int fileSize,
      final String... suffix) throws IOException {
    if(bfc == null) throw new IOException(
        "Can't create a subfile for a deep file that is " +
        "not associated with a regular file.");
    final BufferedFileChannel sub = bfc.subChannel(fileName, fileSize);
    final DeepFile content = new DeepFile(registry, sub, context,
        sub.absolutePosition(), sub.size());
    fileFragments.add(content);
    if(fileName != null) content.addMeta(MetaElem.TITLE, fileName);
    if(registry != null) process(content, suffix);
    sub.finish();
    return content;
  }

  /*
   * Clones the DeepFile to map only a part of the file. The returned DeepFile
   * uses an underlying BufferedFileChannel that starts at the given byte
   * position. The cloned DeepFile must be finished after usage.
   * @param position the position where the subchannel should start
   * @param contentSize the size of the file fragment (the size of the
   *          BufferedFileChannel)
   * @return the new DeepFile
   * @throws IOException if any error occurs
   * @see #finish()
  public DeepFile subfile(final long position, final int contentSize)
      throws IOException {
    bfc.position(position);
    return new DeepFile(this, contentSize);
  }
   */

  /**
   * Clones the DeepFile to map only a part of the file. The returned DeepFile
   * uses an underlying BufferedFileChannel that starts at the current byte
   * position. The cloned DeepFile must be finished after usage.
   * @param contentSize the size of the file fragment (the size of the
   *          BufferedFileChannel)
   * @return the new DeepFile
   * @throws IOException if any error occurs
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
   * @param title the title of the content section
   * @param position the offset in the regular file where the section starts
   * @param contentSize the size of the content section
   * @return the DeepFile instance representing the content section
   * @throws IOException if any error occurs
   * @see #subfile(String, int, String...)
   * @see BufferedFileChannel#subChannel(String, int)
   */
  public DeepFile newContentSection(final String title, final long position,
      final int contentSize) throws IOException {
    bfc.position();
    final BufferedFileChannel sub = bfc.subChannel(title, contentSize);
    final DeepFile subFile = new DeepFile(registry, sub, context, position,
        contentSize);
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
   * @param position the offset in the regular file where the section starts
   * @return the DeepFile instance representing the content section
   * @see #subfile(String, int, String...)
   * @see #setSize(long)
   */
  public DeepFile newContentSection(final long position) {
    return new DeepFile(registry, bfc, context, position, -1);
  }

  /**
   * Sets the size value for the DeepFile. If the current DeepFile instance is
   * not a content section, or if the size value was set before, this method
   * does nothing.
   * @param contentSize the size value to set for the content section
   * @see #newContentSection(long)
   */
  public void setSize(final long contentSize) {
    if(size == -1) size = contentSize;
  }

  /**
   * Returns the xml representation for this deep file.
   * @return the xml representation as string
   * @throws IOException if any error occurs
   */
  private String toXML() throws IOException {
    return FSMLSerializer.serialize(this);
  }

  @Override
  public String toString() {
    try {
      return toXML();
    } catch(final IOException ex) {
      return bfc.getFileName() + "(" + metaElements.size()
          + " metadata attributes, " + textContents.size() + " text sections, "
          + xmlContents.size() + " xml sections and " + fileFragments.size()
          + " content sections)";
    }
  }

  /**
   * Verbose debug message.
   * @param str debug string
   * @param ext text optional extensions
   */
  public void debug(final String str, final Object...ext) {
    if(context.prop.is(Prop.FSVERBOSE))
      Main.debug(bfc.getFileName() + " - " + str, ext);
  }

  // ---------------------------------------------------------------------------

  /**
   * File content.
   * @author Bastian Lemke
   */
  public static final class Content {
    /** offset inside the regular file. */
    private final long o;
    /** size of the text section. */
    private final int s;
    /** the content. */
    private final String t;

    /**
     * Constructor.
     * @param pos offset inside the regular file
     * @param byteCount size of the text section (byte count)
     * @param text the content
     */
    public Content(final long pos, final int byteCount, final String text) {
      o = pos;
      s = byteCount;
      t = text;
    }

    /**
     * Returns the offset.
     * @return the offset
     */
    public long getOffset() {
      return o;
    }

    /**
     * Returns the size.
     * @return the size
     */
    public int getSize() {
      return s;
    }

    /**
     * Returns the content.
     * @return the content
     */
    public String getContent() {
      return t;
    }
  }
}
