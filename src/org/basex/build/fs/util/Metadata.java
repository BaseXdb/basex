package org.basex.build.fs.util;

import static org.basex.util.Token.token;
import static org.basex.util.Token.string;
import static org.basex.util.Token.trim;
import static org.basex.build.fs.NewFSParser.NS;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.core.Main;
import org.basex.util.Atts;
import org.basex.util.Token;

/**
 * Metadata key-value pairs.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class Metadata {

  // ----- Enums ---------------------------------------------------------------

  /**
   * Available metadata fields with data type date.
   * @author Bastian Lemke
   */
  public enum DateField {
    /** Other date. */
    DATE(NS.DCTERMS, "date"),
    /** Date of the last change made to a metadata attribute. */
    DATE_ATTRIBUTE_MODIFIED(NS.FSMETA, "dateAttributeModified"),
    /** Date of the last change made to the content. */
    DATE_CONTENT_MODIFIED(NS.FSMETA, "dateContentModified"),
    /** Date when the content was created. */
    DATE_CREATED(NS.FSMETA, "dateCreated"),
    /** Date of the last usage. */
    DATE_LAST_USED(NS.FSMETA, "dateLastUsed");

    /** The enum value as byte array. */
    private final byte[] val;

    /** Standard constructor for initializing the enum instance. */
    DateField() {
      val = token(toString());
    }

    /**
     * Standard constructor for initializing the enum instance.
     * @param ns the namespace of the item.
     * @param name the name of the item.
     */
    DateField(final NS ns, final String name) {
      val = ns.tag(name);
    }

    /**
     * Returns the enum value as byte array.
     * @return the enum value.
     */
    public byte[] get() {
      return val;
    }
  }

  /**
   * Available xml data types for {@link DateField} instances.
   * @author Bastian Lemke
   */
  public enum DateType {
    /** xs:date. */
    date,
    /** xs:dateTime. */
    dateTime,
    /** xs:gDay. */
    gDay,
    /** xs:gMonth. */
    gMonth,
    /** xs:gYear. */
    gYear,
    /** xs:gYearMonth. */
    gYearMonth,
    /** xs:time. */
    time;

    /** The enum value as byte array. */
    private final byte[] val;

    /** Standard constructor for initializing the enum instance. */
    DateType() {
      val = token("xs:" + toString());
    }

    /**
     * Constructor for initializing the enum instance with the given value.
     * @param v the value to set.
     */
    DateType(final String v) {
      val = token(v);
    }

    /**
     * Returns the enum value as byte array.
     * @return the enum value.
     */
    public byte[] get() {
      return val;
    }
  }

  /**
   * Available metadata fields with data type integer.
   * @author Bastian Lemke
   */
  public enum IntField {
    /** Group ID of the owner of the file. */
    FS_OWNER_GROUP_ID(NS.FSMETA, "fsOwnerGroupId"),
    /** User ID of the owner of the file. */
    FS_OWNER_USER_ID(NS.FSMETA, "fsOwnerUserId"),
    /** Size of the file in the file system. */
    FS_SIZE(NS.FSMETA, "fsSize"),
    /** Height in millimeters. */
    MM_HEIGHT(NS.FSMETA, "mmHeight"),
    /** Width in millimeters. */
    MM_WIDTH(NS.FSMETA, "mmWidth"),
    /** Number of pages. */
    NUMBER_OF_PAGES(NS.FSMETA, "numberOfPages"),
    /** Height in pixels. */
    PIXEL_HEIGHT(NS.FSMETA, "pixelHeight"),
    /** Width in pixels. */
    PIXEL_WIDTH(NS.FSMETA, "pixelWidth"),
    /** Track number. */
    TRACK(NS.FSMETA, "track");

    /** The enum value as byte array. */
    private final byte[] val;

    /**
     * Standard constructor for initializing the enum instance.
     * @param ns the namespace of the item.
     * @param name the name of the item.
     */
    IntField(final NS ns, final String name) {
      val = ns.tag(name);
    }

    /**
     * Returns the enum value as byte array.
     * @return the enum value.
     */
    public byte[] get() {
      return val;
    }
  }

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
      val = token(toString().toLowerCase());
    }

    /**
     * Returns the enum value as byte array.
     * @return the enum value.
     */
    public byte[] get() {
      return val;
    }
  }

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
      final byte[] token = token(name);
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
      elem = token(element);
      suff = token(defaultSuffix);
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

  /**
   * Available metadata fields with data type string.
   * @author Bastian Lemke
   */
  public enum StringField {
    /** Abstract. */
    ABSTRACT(NS.DCTERMS, "abstract"),
    /** Album name. */
    ALBUM(NS.FSMETA, "album"),
    /** Alternative title. */
    ALTERNATIVE(NS.DCTERMS, "alternative"),
    /** Contributor. */
    CONTRIBUTOR(NS.DCTERMS, "contributor"),
    /** Carbon copy receiver. */
    COPY_RECEIVER(NS.FSMETA, "copyReceiver"),
    /** Creator. */
    CREATOR(NS.DCTERMS, "creator"),
    /** Description. */
    DESCRIPTION(NS.DCTERMS, "description"),
    /** Text encoding. */
    ENCODING(NS.FSMETA, "encoding"),
    /** Genre. */
    GENRE(NS.FSMETA, "genre"),
    /** Blind carbon copy receiver. */
    HIDDEN_RECEIVER(NS.FSMETA, "hiddenReceiver"),
    /** Unique identifier. */
    IDENTIFIER(NS.DCTERMS, "identifier"),
    /** Keyword. */
    KEYWORD(NS.FSMETA, "keyword"),
    /**
     * Language.
     * @see <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a>
     */
    LANGUAGE(NS.DCTERMS, "language"),
    /** Publisher. */
    PUBLISHER(NS.DCTERMS, "publisher"),
    /** Receiver. */
    RECEIVER(NS.FSMETA, "receiver"),
    /** Sender. */
    SENDER(NS.FSMETA, "sender"),
    /** Textual description of the location. */
    SPATIAL(NS.DCTERMS, "spatial"),
    /** Message or document subject. */
    SUBJECT(NS.DCTERMS, "subject"),
    /** Table of contents. */
    TABLE_OF_CONTENTS(NS.DCTERMS, "tableOfContents"),
    /** Title. */
    TITLE(NS.DCTERMS, "title");

    /** The enum value as byte array. */
    private final byte[] val;

    /**
     * Standard constructor for initializing the enum instance.
     * @param ns the namespace of the item.
     * @param name the name of the item.
     */
    StringField(final NS ns, final String name) {
      val = ns.tag(name);
    }

    /**
     * Returns the enum value as byte array.
     * @return the enum value.
     */
    public byte[] get() {
      return val;
    }
  }

  /**
   * Enum for valid xml:space attribute values.
   * @author Bastian Lemke
   */
  public enum XmlSpace {
    /** Don't preserve spaces inside the xml element. */
    DEFAULT,
    /** Preserve spaces inside the xml element. */
    PRESERVE;

    /** The attribute value as byte array. */
    private final byte[] val;

    /** Standard constructor for initializing the enum instance. */
    XmlSpace() {
      val = token(toString().toLowerCase());
    }

    /**
     * Returns the enum value as byte array.
     * @return the enum value.
     */
    public byte[] get() {
      return val;
    }
  }

  // ----- Constants -----------------------------------------------------------

  /** XML data type attribute name. */
  public static final byte[] DATA_TYPE = NS.XSI.tag("type");
  /** Duration metadata xml data type. */
  public static final byte[] DATA_TYPE_DURATION = NS.XS.tag("duration");
  /** Integer data type. */
  public static final byte[] DATA_TYPE_INTEGER = NS.XS.tag("integer");

  /** Long data type. */
  public static final byte[] DATA_TYPE_LONG = NS.XS.tag("long");
  /** Short data type. */
  public static final byte[] DATA_TYPE_SHORT = NS.XS.tag("short");
  /** String data type. */
  public static final byte[] DATA_TYPE_STRING = NS.XS.tag("string");

  /** Duration metadata key. */
  public static final byte[] DURATION = NS.FSMETA.tag("duration");

  /** Metadata format element. */
  public static final byte[] FORMAT = NS.DCTERMS.tag("format");

  /** Metadata type element. */
  public static final byte[] TYPE = NS.DCTERMS.tag("type");

  /** xml:base attribute. */
  public static final byte[] XML_BASE = token("xml:base");

  /** xml:id attribute. */
  public static final byte[] XML_ID = token("xml:id");

  /** xml:lang attribute. */
  public static final byte[] XML_LANG = token("xml:lang");

  /** xml:space attribute. */
  public static final byte[] XML_SPACE = token("xml:space");

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  /** The xml data type of the metadata item. */
  private byte[] dataType;
  /** The key of the metadata key-value pair. */
  private byte[] key;
  /** The value of the metadata key-value pair. */
  private byte[] value;

  /** xml:base attribute value. */
  private byte[] xmlBase = null;
  /** xml:id attribute value. */
  private byte[] xmlId = null;
  /** xml:lang attribute value. */
  private byte[] xmlLang = null;
  /** xml:space attribute value. */
  private byte[] xmlSpace = null;

  /** Standard constructor. */
  public Metadata() {
    value = Token.EMPTY;
  }

  /**
   * Returns all attributes for this metadata item.
   * @return all attributes.
   */
  public Atts getAtts() {
    final Atts atts = new Atts();
    atts.add(DATA_TYPE, dataType);
    if(xmlLang != null) atts.add(XML_LANG, xmlLang);
    if(xmlSpace != null) atts.add(XML_SPACE, xmlSpace);
    if(xmlBase != null) atts.add(XML_BASE, xmlBase);
    if(xmlId != null) atts.add(XML_ID, xmlId);
    return atts;
  }

  /**
   * Returns the xml data type of the metadata object (e.g. xs:string) as byte
   * array.
   * @return the xml data type.
   */
  public byte[] getDataType() {
    return dataType;
  }

  /**
   * Returns the key.
   * @return the key.
   */
  public byte[] getKey() {
    return key;
  }

  /**
   * Returns the value.
   * @return the value.
   */
  public byte[] getValue() {
    return value;
  }

  /**
   * Re-initializes the object with a new key-value pair.
   * @param k the new key to set.
   * @param v the new value to set.
   * @param d the new data type to set.
   * @return the metadata item.
   */
  private Metadata recycle(final byte[] k, final byte[] v, final byte[] d) {
    resetXmlAtts();
    setKey(k);
    setValue(v);
    setDataType(d);
    return this;
  }

  /** Resets all xml:* attribute fields. */
  private void resetXmlAtts() {
    xmlLang = null;
    xmlSpace = null;
    xmlBase = null;
    xmlId = null;
  }

  /**
   * Sets the xml data type.
   * @param dt the dataType to set.
   */
  void setDataType(final byte[] dt) {
    dataType = dt;
  }

  /**
   * Re-initializes the metadata object as date item.
   * @param f the metadata key.
   * @param gcal the {@link XMLGregorianCalendar} instance to take the date
   *          value from.
   * @return the metadata item.
   */
  public Metadata setDate(final DateField f, final XMLGregorianCalendar gcal) {
    final byte[] k = f.get();
    final byte[] v = token(gcal.toXMLFormat());
    final byte[] dt = NS.XS.tag(gcal.getXMLSchemaType().getLocalPart());
    return recycle(k, v, dt);
  }

  /**
   * Re-initializes the metadata object as duration item.
   * @param duration the duration.
   * @return the metadata item.
   */
  public Metadata setDuration(final Duration duration) {
    final byte[] k = DURATION;
    final byte[] v = token(duration.toString());
    byte[] dt;
    try {
      dt = NS.XS.tag(duration.getXMLSchemaType().getLocalPart());
    } catch(final IllegalStateException ex) {
      dt = DATA_TYPE_DURATION;
    }
    return recycle(k, v, dt);
  }

  /**
   * Re-initializes the metadata object as integer item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @return the metadata item.
   */
  public Metadata setInt(final IntField field, final byte[] val) {
    return recycle(field.get(), val, DATA_TYPE_INTEGER);
  }

  /**
   * Re-initializes the metadata object as integer item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @return the metadata item.
   */
  public Metadata setInt(final IntField field, final int val) {
    return setInt(field, token(val));
  }

  /**
   * Sets the key.
   * @param k the key to set.
   */
  void setKey(final byte[] k) {
    key = k;
  }

  /**
   * Re-initializes the metadata object as long item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @return the metadata item.
   */
  public Metadata setLong(final IntField field, final byte[] val) {
    return recycle(field.get(), val, DATA_TYPE_LONG);
  }

  /**
   * Re-initializes the metadata object as long item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @return the metadata item.
   */
  public Metadata setLong(final IntField field, final long val) {
    return setLong(field, token(val));
  }

  /**
   * Re-initializes the metadata object as type item.
   * @param t the meta type to set.
   * @return the metadata item
   */
  public Metadata setMetaType(final MetaType t) {
    return recycle(TYPE, t.get(), DATA_TYPE_STRING);
  }

  /**
   * Re-initializes the metadata object as format item.
   * @param mimeType the MIME type to set
   * @return the metadata item
   */
  public Metadata setMimeType(final MimeType mimeType) {
    return recycle(FORMAT, mimeType.get(), DATA_TYPE_STRING);
  }

  /**
   * Re-initializes the metadata object as short item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @return the metadata item.
   */
  public Metadata setShort(final IntField field, final byte[] val) {
    return recycle(field.get(), val, DATA_TYPE_SHORT);
  }

  /**
   * Re-initializes the metadata object as short item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @return the metadata item.
   */
  public Metadata setShort(final IntField field, final short val) {
    return setShort(field, token(val));
  }

  /**
   * Re-initializes the metadata object as string item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @return the metadata item.
   */
  public Metadata setString(final StringField field, final byte[] val) {
    return setString(field, val, false);
  }

  /**
   * Re-initializes the metadata object as string item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @param trim if true, all leading and trailing whitespaces are removed.
   * @return the metadata item.
   */
  public Metadata setString(final StringField field, final byte[] val,
      final boolean trim) {
    return recycle(field.get(), trim ? trim(val) : val, DATA_TYPE_STRING);
  }

  /**
   * Re-initializes the metadata object as string item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @return the metadata item.
   */
  public Metadata setString(final StringField field, final String val) {
    return setString(field, token(val), false);
  }

  /**
   * Re-initializes the metadata object as string item.
   * @param field the metadata key.
   * @param val the metadata value.
   * @param trim if true, all leading and trailing whitespaces are removed.
   * @return the metadata item.
   */
  public Metadata setString(final StringField field, final String val,
      final boolean trim) {
    return setString(field, token(val), trim);
  }

  /**
   * Sets the value.
   * @param v the value to set.
   */
  void setValue(final byte[] v) {
    value = v;
  }

  /**
   * Adds a xml:base attribute.
   * @param base the attribute value.
   */
  public void setXmlBase(final byte[] base) {
    xmlBase = base;
  }

  /**
   * Adds a xml:base attribute.
   * @param base the attribute value.
   */
  public void setXmlBase(final String base) {
    setXmlBase(token(base));
  }

  /**
   * Adds a xml:id attribute.
   * @param id the attribute value.
   */
  public void setXmlId(final byte[] id) {
    xmlId = id;
  }

  /**
   * Adds a xml:id attribute.
   * @param id the attribute value.
   */
  public void setXmlId(final String id) {
    setXmlId(token(id));
  }

  /**
   * Adds a xml:lang attribute.
   * @param lang the attribute value.
   */
  public void setXmlLang(final byte[] lang) {
    xmlLang = lang;
  }

  /**
   * Adds a xml:lang attribute.
   * @param lang the attribute value.
   */
  public void setXmlLang(final String lang) {
    setXmlLang(token(lang));
  }

  /**
   * Adds a xml:space attribute.
   * @param space the attribute value.
   */
  public void setXmlSpace(final XmlSpace space) {
    xmlSpace = space.get();
  }

  @Override
  public String toString() {
    final String k = string(key);
    final String v = string(value);
    final boolean empty = v.length() == 0;
    final StringBuilder xmlAttrsStr = new StringBuilder("");
    final Atts atts = getAtts();
    for(int i = 0; i < atts.size; i++) {
      xmlAttrsStr.append(" " + string(atts.key[i]));
      xmlAttrsStr.append("=\"");
      xmlAttrsStr.append(string(atts.val[i]));
      xmlAttrsStr.append("\"");
    }
    final String attStr = xmlAttrsStr.toString();
    return "<" + k + attStr + (empty ? " />" : ">" + v + "</" + k + ">");
  }
}
