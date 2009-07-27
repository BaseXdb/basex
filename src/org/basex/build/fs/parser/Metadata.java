package org.basex.build.fs.parser;

import static org.basex.util.Token.token;
import static org.basex.util.Token.string;
import static org.basex.util.Token.trim;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.basex.util.Atts;

/**
 * Metadata key-value pairs.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public class Metadata {

  // ----- Constants -----------------------------------------------------------

  /** xml:lang attribute. */
  private static final byte[] XML_LANG = token("xml:lang");
  /** xml:space attribute. */
  private static final byte[] XML_SPACE = token("xml:space");
  /** xml:base attribute. */
  private static final byte[] XML_BASE = token("xml:base");
  /** xml:id attribute. */
  private static final byte[] XML_ID = token("xml:id");
  /** XML schema prefix. */
  public static final String XML_SCHEMA_PREFIX = "xs";
  /** XML data type attribute name. */
  private static final byte[] DATA_TYPE = token("xsi:type");
  /** Metadata type element. */
  static final byte[] TYPE = token("type");
  /** Metadata format element. */
  static final byte[] FORMAT = token("format");
  /** String data type. */
  static final byte[] DATA_TYPE_STRING = token(XML_SCHEMA_PREFIX + ":string");
  /** Short data type. */
  static final byte[] DATA_TYPE_SHORT = token(XML_SCHEMA_PREFIX + ":short");
  /** Integer data type. */
  static final byte[] DATA_TYPE_INTEGER = token(XML_SCHEMA_PREFIX + ":integer");
  /** Long data type. */
  static final byte[] DATA_TYPE_LONG = token(XML_SCHEMA_PREFIX + ":long");
  /** Duration metadata key. */
  static final byte[] DURATION = token("duration");
  /** Duration metadata xml data type. */
  static final byte[] DATA_TYPE_DURATION = token(XML_SCHEMA_PREFIX
      + ":duration");

  // ----- Enums ---------------------------------------------------------------

  /**
   * Enum for valid xml:space attribute values.
   * @author Bastian Lemke
   */
  public enum XmlSpace {
    /** Preserve spaces inside the xml element. */
    PRESERVE,
    /** Don't preserve spaces inside the xml element. */
    DEFAULT;

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

  /**
   * Available MIME types.
   * @author Bastian Lemke
   */
  public enum MimeType {
    /** Unknown media. */
    UNKNOWN("unkown", ""),
    /** XML. */
    XML("application/xml", "xml"),
    /** KML. */
    KML("application/vnd.google-earth.kml+xml", "kml"),
    /** MP3. */
    MP3("audio/mp3", "mp3"),
    /** PNG. */
    PNG("image/png", "png"),
    /** JPG. */
    JPG("image/jpeg", "jpg"),
    /** BMP. */
    BMP("image/bmp", "bmp");

    /** The element name as byte array. */
    private final byte[] elem;
    /** The default file suffix. */
    private final byte[] suff;

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
     * Constructor for initializing an element.
     * @param element the xml element string.
     * @param defaultSuffix the default file suffix for the MIME type.
     */
    MimeType(final String element, final String defaultSuffix) {
      elem = token(element);
      suff = token(defaultSuffix);
    }
  }

  /**
   * Available metadata types.
   * @author Bastian Lemke
   */
  public enum MetaType {
    /** Application resource. */
    APPLICATION,
    /** Archive resource (e.g. ZIP file). */
    ARCHIVE,
    /** Audio resource (e.g. MP3 file). */
    AUDIO,
    /** Contact resource (e.g. VCF file). */
    CONTACT,
    /** Document resource (e.g. DOC file). */
    DOCUMENT,
    /** Image resource (e.g. JPG file). */
    IMAGE,
    /** Message resource (e.g. email). */
    MESSAGE,
    /** Movie resource (e.g. MPEG file). */
    MOVIE,
    /** Presentation resource (e.g. PPT file). */
    PRESENTATION,
    /** Text resource (e.g. plain text file). */
    TEXT,
    /** Unknown resource type. */
    UNKNOWN,
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
   * Available metadata fields with data type string.
   * @author Bastian Lemke
   */
  public enum StringField {
    /** Abstract. */
    Abstract("abstract"),
    /** Album name. */
    album,
    /** Alternative title. */
    alternativeTitle,
    /** Author. */
    author,
    /** Blind carbon copy receiver. */
    bcc,
    /** Carbon copy receiver. */
    cc,
    /** Comment. */
    comment,
    /** Composer. */
    composer,
    /** Contributor. */
    contributor,
    /** Creator. */
    creator,
    /** Description. */
    description,
    /** Sender. */
    from,
    /** Genre. */
    genre,
    /** Unique identifier. */
    identifier,
    /** Keyword. */
    keyword,
    /**
     * Language.
     * @see <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a>
     */
    language,
    /** Textual description of the location. */
    location,
    /** Person that wrote the lyrics. */
    lyricist,
    /** Organization. */
    organization,
    /** Original creator. */
    originalCreator,
    /** Publisher. */
    publisher,
    /** Message or document subject. */
    subject,
    /** Subtitle. */
    subTitle,
    /** Table of contents. */
    tableOfContents,
    /** Text writer. */
    textWriter,
    /** Title. */
    title,
    /** Receiver. */
    to,
    /** Version number. */
    version;

    /** The enum value as byte array. */
    private final byte[] val;

    /** Standard constructor for initializing the enum instance. */
    StringField() {
      val = token(toString());
    }

    /**
     * Constructor for initializing the enum instance with the given value.
     * 
     * @param v the value to set.
     */
    StringField(final String v) {
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
    /** User ID of the owner of the file. */
    fsOwnerUserId,
    /** Group ID of the owner of the file. */
    fsOwnerGroupId,
    /** Size of the file in the file system. */
    fsSize,
    /** Height in millimeters. */
    mmHeight,
    /** Width in millimeters. */
    mmWidth,
    /** Number of pages. */
    numberOfPages,
    /** Height in pixels. */
    pixelHeight,
    /** Width in pixels. */
    pixelWidth,
    /** Track number. */
    track;

    /** The enum value as byte array. */
    private final byte[] val;

    /** Standard constructor for initializing the enum instance. */
    IntField() {
      val = token(toString());
    }

    /**
     * Constructor for initializing the enum instance with the given value.
     * 
     * @param v the value to set.
     */
    IntField(final String v) {
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
   * Available metadata fields with data type date.
   * @author Bastian Lemke
   */
  public enum DateField {
    /** Date of the last change made to a metadata attribute. */
    dateAttributeChanged,
    /** Date of the last change made to the content. */
    dateModified,
    /** Date of the last usage. */
    dateLastUsed,
    /** Date of the recording. */
    dateRecorded,
    /** Release date. */
    dateReleased,
    /** Date when the content was created. */
    dateCreated,
    /** Submission date. */
    dateSubmitted,
    /** Date of acception. */
    dateAccepted;

    /** The enum value as byte array. */
    private final byte[] val;

    /** Standard constructor for initializing the enum instance. */
    DateField() {
      val = token(toString());
    }

    /**
     * Constructor for initializing the enum instance with the given value.
     * 
     * @param v the value to set.
     */
    DateField(final String v) {
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
   * Available xml data types for {@link DateField} instances.
   * @author Bastian Lemke
   */
  public enum DateType {
    /** xs:date. */
    date,
    /** xs:time. */
    time,
    /** xs:dateTime. */
    dateTime,
    /** xs:gDay. */
    gDay,
    /** xs:gMonth. */
    gMonth,
    /** xs:gYear. */
    gYear,
    /** xs:gYearMonth. */
    gYearMonth;

    /** The enum value as byte array. */
    private final byte[] val;

    /** Standard constructor for initializing the enum instance. */
    DateType() {
      val = token("xs:" + toString());
    }

    /**
     * Constructor for initializing the enum instance with the given value.
     * 
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

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  /** The key of the metadata key-value pair. */
  private byte[] key;
  /** The value of the metadata key-value pair. */
  private byte[] value;
  /** The xml data type of the metadata item. */
  private byte[] dataType;

  /** xml:lang attribute value. */
  private byte[] xmlLang = null;
  /** xml:space attribute value. */
  private byte[] xmlSpace = null;
  /** xml:base attribute value. */
  private byte[] xmlBase = null;
  /** xml:id attribute value. */
  private byte[] xmlId = null;

  /**
   * Constructor for a new metadata item.
   * @param k the key of the metadata key-value pair.
   * @param v the value of the metadata key-value pair.
   * @param dt the xml data type of the metadata item.
   */
  private Metadata(final byte[] k, final byte[] v, final byte[] dt) {
    key = k;
    value = v;
    dataType = dt;
  }

  /** Standard constructor. */
  public Metadata() { /* outsmarted the formatter ;-) */}

  /**
   * Initializes the metadata object as special XML element "type".
   * @param t the type to set.
   */
  Metadata(final MetaType t) {
    this(TYPE, t.get(), DATA_TYPE_STRING);
  }

  /**
   * Initializes the metadata object as special XML element "format".
   * @param mimeType the MIME type to set.
   */
  Metadata(final String mimeType) {
    this(token(mimeType));
  }

  /**
   * Initializes the metadata object as special XML element "format".
   * @param mimeType the MIME type to set (as byte array).
   */
  Metadata(final byte[] mimeType) {
    this(FORMAT, mimeType, DATA_TYPE_STRING);
  }

  /**
   * Returns the value.
   * @return the value.
   */
  public byte[] getValue() {
    return value;
  }

  /**
   * Returns the key.
   * @return the key.
   */
  public byte[] getKey() {
    return key;
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
   * Sets the key.
   * @param k the key to set.
   */
  void setKey(final byte[] k) {
    key = k;
  }

  /**
   * Sets the value.
   * @param v the value to set.
   */
  void setValue(final byte[] v) {
    value = v;
  }

  /**
   * Sets the xml data type.
   * @param dt the dataType to set.
   */
  void setDataType(final byte[] dt) {
    dataType = dt;
  }

  /**
   * Adds a xml:lang attribute.
   * @param lang the attribute value.
   */
  public void setXmlLang(final String lang) {
    setXmlLang(token(lang));
  }

  /**
   * Adds a xml:lang attribute.
   * @param lang the attribute value.
   */
  public void setXmlLang(final byte[] lang) {
    xmlLang = lang;
  }

  /**
   * Adds a xml:space attribute.
   * @param space the attribute value.
   */
  public void setXmlSpace(final XmlSpace space) {
    xmlSpace = space.get();
  }

  /**
   * Adds a xml:base attribute.
   * @param base the attribute value.
   */
  public void setXmlBase(final String base) {
    setXmlBase(token(base));
  }

  /**
   * Adds a xml:base attribute.
   * @param base the attribute value.
   */
  public void setXmlBase(final byte[] base) {
    xmlBase = base;
  }

  /**
   * Adds a xml:id attribute.
   * @param id the attribute value.
   */
  public void setXmlId(final String id) {
    setXmlId(token(id));
  }

  /**
   * Adds a xml:id attribute.
   * @param id the attribute value.
   */
  public void setXmlId(final byte[] id) {
    xmlId = id;
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

  /** Resets all xml:* attribute fields. */
  private void resetXmlAtts() {
    xmlLang = null;
    xmlSpace = null;
    xmlBase = null;
    xmlId = null;
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
  public Metadata setString(final StringField field, final String val,
      final boolean trim) {
    return setString(field, token(val), trim);
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
   * Re-initializes the metadata object as date item.
   * @param field the metadata key.
   * @param gcal the {@link XMLGregorianCalendar} instance to take the date
   *          value from.
   * @return the metadata item.
   */
  public Metadata setDate(final DateField field,
      final XMLGregorianCalendar gcal) {
    final byte[] k = field.get();
    final byte[] v = token(gcal.toXMLFormat());
    final byte[] dt = token(XML_SCHEMA_PREFIX + ":"
        + gcal.getXMLSchemaType().getLocalPart());
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
      dt = token(XML_SCHEMA_PREFIX + ":"
          + duration.getXMLSchemaType().getLocalPart());
    } catch(final IllegalStateException e) {
      dt = DATA_TYPE_DURATION;
    }
    return recycle(k, v, dt);
  }
}
