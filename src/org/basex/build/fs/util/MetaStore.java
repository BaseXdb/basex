package org.basex.build.fs.util;

import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;
import javax.xml.datatype.Duration;
import org.basex.build.fs.NewFSParser;
import org.basex.core.Main;
import org.basex.query.item.Type;
import org.basex.util.Token;

/**
 * Storage for metadata information for a single file.
 * @author Bastian Lemke
 */
public final class MetaStore {
  
  // [BL] allow nesting of content-elements (nested MetaStores)

  /** Map, containing all metadata key-value pairs for the current file. */
  private final TreeMap<MetaElem, byte[]> metaElements =
      new TreeMap<MetaElem, byte[]>();

  /** Removes all key-value pairs. */
  public void clear() {
    metaElements.clear();
  }

  /**
   * Writes the metadata to the parser.
   * @param parser the parser to write the metadata to.
   * @throws IOException if any error occurs.
   */
  public void write(final NewFSParser parser) throws IOException {
    // [BL] element ordering
    parser.setMeta(metaElements);
  }

  /**
   * Sets the metadata type.
   * @param type the metadata type.
   */
  public void setType(final MetaType type) {
    metaElements.put(MetaElem.TYPE, type.get());
  }

  /**
   * Sets the MIME type.
   * @param format the MIME type.
   */
  public void setFormat(final MimeType format) {
    metaElements.put(MetaElem.FORMAT, format.get());
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param e metadata element (the key).
   * @param value value as byte array. Must contain only correct UTF-8 values!
   * @param dataType the xml data type to set for this metadata element or
   *          <code>null</code> if the default data type should be used.
   */
  private void add(final MetaElem e, final byte[] value, final Type dataType) {
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
  public void add(final MetaElem elem, final byte[] value) {
    if(!elem.getType().instance(Type.STR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (string - as byte array).");
    add(elem, ParserUtil.checkUTF(value), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value string value.
   */
  public void add(final MetaElem elem, final String value) {
    if(!elem.getType().instance(Type.STR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (string).");
    add(elem, ParserUtil.checkUTF(Token.token(value)), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value integer value.
   */
  public void add(final MetaElem elem, final short value) {
    if(!elem.getType().instance(Type.SHR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (short).");

    add(elem, ParserUtil.checkUTF(Token.token(value)), Type.SHR);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value integer value.
   */
  public void add(final MetaElem elem, final int value) {
    if(!elem.getType().instance(Type.ITR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (int).");
    add(elem, ParserUtil.checkUTF(Token.token(value)), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value long value.
   */
  public void add(final MetaElem elem, final long value) {
    if(!elem.getType().instance(Type.LNG)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (long).");
    add(elem, ParserUtil.checkUTF(Token.token(value)), Type.LNG);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value date value.
   */
  public void add(final MetaElem elem, final Date value) {
    if(!elem.getType().instance(Type.DTM)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (date).");
    add(elem, ParserUtil.convertDate(value), null);
  }

  /**
   * Add a metadata key-value pair for the current file.
   * @param elem metadata element (the key).
   * @param value duration value.
   */
  public void add(final MetaElem elem, final Duration value) {
    if(!elem.getType().instance(Type.DUR)) Main.bug("Invalid data type for " +
        "metadata element " + elem + " (date).");
    add(elem, Token.token(value.toString()), null);
  }

  /*
   * Checks if the given {@link MetaElem} was added before for this file and
   * returns the string value of the corresponding metadata attribute.
   * @param elem metadata element to check.
   * @return the metadata value as string.
  public String getValueAsString(final MetaElem elem) {
    return metaElements.containsKey(elem) ?
        Token.string(metaElements.get(elem)) :
        null;
  }
   */

  // ---------------------------------------------------------------------------
  // ----- enums for metadata type and format ----------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Available metadata types. Every file has at least one MetaType. Files may
   * have more than one MetaType (e.g. a SVG file has MetaType "xml" and
   * "image").
   * @author Bastian Lemke
   */
  public enum MetaType {
        /** Archive resource (e.g. ZIP file). */
    ARCHIVE,
        /** Audio resource (e.g. MP3 file). */
    AUDIO,
        /** Binary resource. */
    BINARY,
        /** Calendar resource (e.g. ICS file). */
    CALENDAR,
        /** Contact resource (e.g. VCF file). */
    CONTACT,
        /** Document resource (e.g. DOC or PDF file). */
    DOCUMENT,
        /** Map resource (e.g. KML or GPX file). */
    MAP,
        /** Message resource (e.g. email). */
    MESSAGE,
        /** Picture resource (e.g. JPG file). */
    PICTURE,
        /** Presentation resource (e.g. PPT file). */
    PRESENTATION,
        /** Text(-based) resource (e.g. plain text file). */
    TEXT,
        /** Unknown resource type. */
    UNKNOWN,
        /** Video resource (e.g. MPEG file). */
    VIDEO,
        /** XML(-based) resource. */
    XML;

    /** The attribute value as byte array. */
    private final byte[] val;

    /** Standard constructor for initializing the enum instance. */
    MetaType() {
      val = Token.token(toString().toLowerCase());
    }

    /**
     * Returns the enum value as byte array.
     * @return the enum value.
     */
    public byte[] get() {
      return val;
    }
  }

  // ---------------------------------------------------------------------------
  // ----- enums for metadata type and format ----------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Available MIME types.
   * @author Bastian Lemke
   */
  public enum MimeType {
        /** Apple Mail to do. */
    APPLE_MAIL_TODO("application/vnd.apple.mail+todo", ""),
        /** BMP. */
    BMP("image/bmp", "bmp", MetaType.PICTURE),
        /** CSS. */
    CSS("text/css", "css", MetaType.TEXT),
        /** text/directory */
    DIRECTORY("text/directory", ""),
        /** M$ Word. */
    DOC("application/msword", "doc", MetaType.DOCUMENT),
        /** Email. */
    EML("message/rfc822", "eml", MetaType.MESSAGE),
        /** ? */
    FORCE_DOWNLOAD("application/force-download", ""),
        /** GIF. */
    GIF("image/gif", "gif", MetaType.PICTURE),
        /** HTML. */
    HTML("text/html", "html", MetaType.TEXT),
        /** ICS. */
    ICS("application/ics", "ics", MetaType.CALENDAR),
        /** Java archive. */
    JAR("application/java-archive", "jar", MetaType.ARCHIVE),
        /** JPG. */
    JPG("image/jpeg", "jpg", MetaType.PICTURE),
        /** KML. */
    KML("application/vnd.google-earth.kml+xml", "kml", MetaType.XML,
        MetaType.MAP),
        /** MP3. */
    MP3("audio/mp3", "mp3", MetaType.AUDIO),
        /** Object file. */
    O("application/octet-stream", "o"),
        /** ODS. */
    ODS("application/vnd.oasis.opendocument.spreadsheet", "ods",
        MetaType.DOCUMENT),
        /** PDF. */
    PDF("application/pdf", "pdf", MetaType.DOCUMENT),
        /** pgp signature. */
    PGP("application/pgp-signature", ""),
        /** pkcs7-signature. */
    PKCS7("application/pkcs7-signature", ""),
        /** PNG. */
    PNG("image/png", "png", MetaType.PICTURE),
        /** PPS. */
    PPS("application/vnd.ms-powerpoint", "pps", MetaType.PRESENTATION),
        /** Rich text format. */
    RTF("text/rtf", "rtf", MetaType.TEXT, MetaType.DOCUMENT),
        /** TIFF. */
    TIFF("image/tiff", "tif", MetaType.PICTURE),
        /** Plaintext. */
    TXT("text/plain", "txt", MetaType.TEXT),
        /** Unknown media. */
    UNKNOWN("unkown", "", MetaType.UNKNOWN),
        /** Vcard. */
    VCARD("text/x-vcard", "vcf", MetaType.CONTACT),
        /** XLS. */
    XLS("application/vnd.ms-excel", "xls", MetaType.DOCUMENT),
        /** XML. */
    XML("application/xml", "xml", MetaType.XML),
        /** XML. */
    XML2("text/xml", "xml", MetaType.XML),
        /** x-pkcs7-signature. */
    XPKCS7("application/x-pkcs7-signature", ""),
        /** Zip. */
    ZIP("application/zip", "zip", MetaType.ARCHIVE);

    /**
     * Tries to find the MimeType item for the given MIME type string.
     * @param name the MIME type string to find.
     * @return the {@link MimeType} item or <code>null</code> if the item was
     *         not found.
     */
    public static MimeType getItem(final String name) {
      // [BL] more efficient MimeType retrieval.
      final byte[] token = Token.token(name);
      for(final MimeType mt : MimeType.values()) {
        if(Token.eq(mt.elem, token)) return mt;
      }
      Main.debug("MIME type not found: " + name);
      return null;
    }

    /** The element name as byte array. */
    private final byte[] elem;
    /** The default file suffix. */
    private final byte[] suff;

    /** The associated meta types. */
    private MetaType[] types;

    /**
     * Constructor for initializing an element.
     * @param element the xml element string.
     * @param defaultSuffix the default file suffix for the MIME type.
     * @param mt the associated meta types.
     */
    MimeType(final String element, final String defaultSuffix,
        final MetaType... mt) {
      elem = Token.token(element);
      suff = Token.token(defaultSuffix);
      types = mt;
    }

    /**
     * Returns the xml element name as byte array.
     * @return the xml element name.
     */
    public byte[] get() {
      return elem;
    }

    /**
     * Returns the default file suffix for the MIME type.
     * @return the default file suffix.
     */
    public byte[] getDefaultSuffix() {
      return suff;
    }

    /**
     * Returns the associated meta types.
     * @return the associated meta types.
     */
    public MetaType[] getMetaTypes() {
      return types;
    }
  }
}
