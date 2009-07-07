package org.basex.build.fs.parser;

import static org.basex.util.Token.token;

/**
 * Constants for file metadata and content representation.
 * @author Bastian Lemke
 */
public final class Metadata {
  /**
   * Available xml attributes.
   * @author Bastian Lemke
   */
  public enum Attribute {
    /** Attribute name "type". */
    TYPE("xsi:type"),
    /** Attribute name "def". */
    DEFINITION("def"),
    /** Attribute name "lang". */
    LANGUAGE("xml:lang");

    /** The element name as byte array. */
    private final byte[] elem;

    /**
     * Returns the xml element name as byte array.
     * @return the xml element name.
     */
    public byte[] get() {
      return elem;
    }

    /**
     * Constructor for initializing an element.
     * @param element the xml element string.
     */
    Attribute(final String element) {
      elem = token(element);
    }
  }

  /** Hidden constructor. */
  private Metadata() {

  }

  /**
   * Available xml elements.
   * @author Bastian Lemke
   */
  public enum Element {
    /** Document type. */
    TYPE("type"),
    /** Document format (MIME type). */
    FORMAT("format"),

    /** Document title. */
    TITLE("title"),
    /** Document description. */
    DESCRIPTION("description"),
    /** Document location (text). */
    LOCATION("location"),
    /** Document subject. */
    SUBJECT("subject"),
    /** Comment. */
    COMMENT("comment"),
    /** Genre of the document. */
    GENRE("genre"),
    /** Keyword related to the document. */
    KEYWORD("keyword"),

    /** Creator of the document. */
    CREATOR("creator"),
    /** Date associated with the document. */
    DATE("date"),
    /** Contributor. */
    CONTRIBUTOR("contributor"),
    /** Audience/Receipient. */
    AUDIENCE("audience"),
    /** Duration of the document (xml duration data type: xs:duration). */
    DURATION("duration"),
    /** Width of the document. */
    WIDTH("width"),
    /** Height of the document. */
    HEIGHT("height"),
    /** Publisher of the document. */
    PUBLISHER("publisher"),
    /** Number of pages of the document. */
    PAGES("pages"),
    /** Album to which the document belongs to. */
    ALBUM("album"),
    /** Track number of the document. */
    TRACK("track"),
    /** GPS location related to the document. */
    GPS("gps"),

    /** Document content. */
    CONTENT("content");

    /** The element name as byte array. */
    private final byte[] elem;

    /**
     * Returns the xml element name as byte array.
     * @return the xml element name.
     */
    public byte[] get() {
      return elem;
    }

    /**
     * Constructor for initializing an element.
     * @param element the xml element string.
     */
    Element(final String element) {
      elem = token(element);
    }
  }

  /**
   * Available metadata types.
   * @author Bastian Lemke
   */
  public enum Type {
    /** Audio resource (e.g. MP3 file). */
    AUDIO("audio"),
    /** Text resource (e.g. plain text file). */
    TEXT("text"),
    /** Image resource (e.g. JPG file). */
    IMAGE("image"),
    /** Movie resource (e.g. MPEG file). */
    MOVIE("movie"),
    /** Email resource. */
    MAIL("mail"),
    /** XML(-based) resource. */
    XML("xml"),
    /** Another application-specific ressource. */
    APPLICATION("application");

    /** The element name as byte array. */
    private final byte[] elem;

    /**
     * Returns the xml element name as byte array.
     * @return the xml element name.
     */
    public byte[] get() {
      return elem;
    }

    /**
     * Constructor for initializing an element.
     * @param element the xml element string.
     */
    Type(final String element) {
      elem = token(element);
    }
  }

  /**
   * Supported XML data types.
   * @author Bastian Lemke
   */
  public enum DataType {
    /** String value. */
    STRING("xs:string"),
    /** Boolean value. */
    BOOLEAN("xs:boolean"),
    /** Integer value. */
    INTEGER("xs:integer"),
    /** Float value. */
    FLOAT("xs:float"),
    /** Date value (YYYY-MM-DD), e.g. 2004-02-08. */
    DATE("xs:date"),
    /** Time value (hh:mm:ss), e.g. 12:00:00. */
    TIME("xs:time"),
    /** Date and time value (YYYY-MM-DDThh:mm:ss), e.g. 2004-02-08T12:00:00. */
    DATETIME("xs:dateTime"),
    /** Duration value, e.g. "P2Y3MT5.6S" (2 years, 3 months, 5.6 seconds). */
    DURATION("xs:duration"),
    /** Day value, e.g. "--30" (thirtieth day of each month). */
    DAY("xs:gDay"),
    /** Month value, e.g. "-02-" (second month of each year). */
    MONTH("xs:gMonth"),
    /** Year value, e.g. "2004--" (entire year 2004). */
    YEAR("xs:gYear"),
    /** Year and month value, e.g. "2005-05" (fifth month of the year 2004). */
    YEARMONTH("xs:gYearMonth");

    /** The element name as byte array. */
    private final byte[] elem;

    /**
     * Returns the xml element name as byte array.
     * @return the xml element name.
     */
    public byte[] get() {
      return elem;
    }

    /**
     * Constructor for initializing an element.
     * @param element the xml element string.
     */
    DataType(final String element) {
      elem = token(element);
    }
  }

  /**
   * Available precise definitions for elements.
   * @author Bastian Lemke
   */
  public enum Definition {
    /** Used for elements without precise definition. */
    NONE(""),

    // element "title"
    /** Alternative title. */
    ALTERNATIVE("alternative"),

    // element "creator"
    /** Artist (e.g. ID3). */
    ARTIST("artist"),
    /** Original artist (e.g. ID3). */
    ORIGINAL_ARTIST("original artist"),
    /** Author. */
    AUTHOR("author"),
    /** Sender (e.g. Mail). */
    SENDER("sender"),

    // element "contributor"
    /** Composer (e.g. ID3). */
    COMPOSER("composer"),
    /** Text writer (e.g. ID3). */
    TEXT_WRITER("text writer"),

    // element "date"
    /** Create time. */
    CREATE_TIME("create time"),
    /** Recording time. */
    RECORDING_TIME("recording time"),
    /** Release time. */
    RELEASE_TIME("release time"),

    // element "description"
    /** Abstract of the document. */
    ABSTRACT("abstract"),
    /** Table of contents for the document. */
    TABLE_OF_CONTENTS("table of contents"),

    // element "audience"
    /** Receipient of the document. */
    RECEIPIENT("receipient"),
    /** Carbon copy receipient. */
    CC("cc"),
    /** Blind carbon copy receipient. */
    BCC("bcc"),

    // elements "width" and "height"
    /** Number of pixels. */
    PIXEL("pixel"),
    /** Size in mm. */
    MILLIMETER("millimeter"),
    /** Size in inch. */
    INCH("inch"),
    /** Dots per inch. */
    DOTS_PER_INCH("dots per inch"),
    /** Dots per centimeter. */
    DOTS_PER_CM("dots per cm");

    /** The element name as byte array. */
    private final byte[] elem;

    /**
     * Returns the xml element name as byte array.
     * @return the xml element name.
     */
    public byte[] get() {
      return elem;
    }

    /**
     * Constructor for initializing an element.
     * @param element the xml element string.
     */
    Definition(final String element) {
      elem = token(element);
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
}
