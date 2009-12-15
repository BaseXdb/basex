package org.deepfs.fsml;

import org.basex.core.Main;
import org.basex.query.item.Type;
import org.basex.util.Token;

/**
 * Available metadata elements.
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Bastian Lemke
 */
public enum MetaElem {

  /** Type. */
  TYPE(DeepNS.DCTERMS, "type", Type.STR, true),
  /** Format (MIME type). */
  FORMAT(DeepNS.DCTERMS, "format", Type.STR, false),

  /** container element "content". */
  CONTENT(DeepNS.DEEPURL, "content"),

  // ----- duration fields ---------------------------------------------------

  /** Duration. */
  DURATION(DeepNS.FSMETA, "duration", Type.DUR, false),

  // ----- date fields -------------------------------------------------------

  /** Other date. */
  DATE(DeepNS.DCTERMS, "date", Type.DAT, false),

  /** Date of the last change made to a metadata attribute. */
  DATETIME_ATTRIBUTE_MODIFIED(DeepNS.FSMETA, "dateTimeAttributeModified",
      Type.DTM, false),
  /** Date of the last change made to the content. */
  DATETIME_CONTENT_MODIFIED(DeepNS.FSMETA, "dateTimeContentModified", Type.DTM,
      false),
  /** Date when the content was created. */
  DATETIME_CREATED(DeepNS.FSMETA, "dateTimeCreated", Type.DTM, false),
  /** Date when the content was digitized. */
  DATETIME_DIGITIZED(DeepNS.FSMETA, "dateTimeDigitized", Type.DTM, false),
  /** Date of the last usage. */
  DATETIME_LAST_USED(DeepNS.FSMETA, "dateTimeLastUsed", Type.DTM, false),
  /** Original date. */
  DATETIME_ORIGINAL(DeepNS.FSMETA, "dateTimeOriginal", Type.DTM, false),
  /** Year. */
  YEAR(DeepNS.FSMETA, "year", Type.YEA, false),

  // ----- integer fields ----------------------------------------------------

  /** Beats per minute. */
  BEATS_PER_MINUTE(DeepNS.FSMETA, "beatsPerMinute", Type.INT, false),
  /** Bitrate. */
  BITRATE_KBIT(DeepNS.FSMETA, "bitrateKBitS", Type.INT, false),
  /** ISO speed ratings. */
  ISO_SPEED_RATINGS(DeepNS.FSMETA, "isoSpeedRatings", Type.INT, false),
  /** Focal length in 35mm film. */
  FOCAL_LENGTH_IN_35MM_FILM(DeepNS.FSMETA, "focalLengthIn35mmFilm", Type.INT,
      false),
  /** Group ID of the owner of the file. */
  FS_OWNER_GROUP_ID(DeepNS.FSMETA, "fsOwnerGroupId", Type.INT, false),
  /** User ID of the owner of the file. */
  FS_OWNER_USER_ID(DeepNS.FSMETA, "fsOwnerUserId", Type.INT, false),
  /** Size of the file in the file system. */
  FS_SIZE(DeepNS.FSMETA, "fsSize", Type.INT, false),
  /** Height in millimeters. */
  MM_HEIGHT(DeepNS.FSMETA, "mmHeight", Type.INT, false),
  /** Width in millimeters. */
  MM_WIDTH(DeepNS.FSMETA, "mmWidth", Type.INT, false),
  /** Number of pages. */
  NUMBER_OF_PAGES(DeepNS.FSMETA, "numberOfPages", Type.INT, false),
  /** Height in pixels. */
  PIXEL_HEIGHT(DeepNS.FSMETA, "pixelHeight", Type.INT, false),
  /** Width in pixels. */
  PIXEL_WIDTH(DeepNS.FSMETA, "pixelWidth", Type.INT, false),
  /** Sample rate. */
  SAMPLE_RATE(DeepNS.FSMETA, "sampleRate", Type.INT, false),
  /** Track number. */
  TRACK(DeepNS.FSMETA, "track", Type.INT, false),

  // ----- double fields -----------------------------------------------------

  /** Aperture value. */
  APERTURE_VALUE(DeepNS.FSMETA, "apertureValue", Type.DBL, false),
  /** Maximum aperture value. */
  APERTURE_VALUE_MAX(DeepNS.FSMETA, "apertureValueMax", Type.DBL, false),
  /** Brightness value. */
  BRIGHTNESS_VALUE(DeepNS.FSMETA, "brightnessValue", Type.DBL, false),
  /** Compressed bits per pixel. */
  COMPRESSED_BITS_PER_PIXEL(DeepNS.FSMETA, "compressedBitsPerPixel", Type.DBL,
      false),
  /** Digital zoom ratio. */
  DIGITAL_ZOOM_RATIO(DeepNS.FSMETA, "digitalZoomRatio", Type.DBL, false),
  /** Exposure bias value. */
  EXPOSURE_BIAS_VALUE(DeepNS.FSMETA, "exposureBiasValue", Type.DBL, false),
  /** Exposure index. */
  EXPOSURE_INDEX(DeepNS.FSMETA, "exposureIndex", Type.DBL, false),
  /** Exposure time in seconds. */
  EXPOSURE_TIME_MS(DeepNS.FSMETA, "exposureTimeMs", Type.DBL, false),
  /** F number. */
  F_NUMBER(DeepNS.FSMETA, "fNumber", Type.DBL, false),
  /** Focal length. */
  FOCAL_LENGTH(DeepNS.FSMETA, "focalLengthMM", Type.DBL, false),
  /** Focal plane X resolution. */
  FOCAL_PLANE_X_RESOLUTION(DeepNS.FSMETA, "focalPlaneXresolution", Type.DBL,
      false),
  /** Focal plane Y resolution. */
  FOCAL_PLANE_Y_RESOLUTION(DeepNS.FSMETA, "focalPlaneYresolution", Type.DBL,
      false),
  /** Shutter speed value. */
  SHUTTER_SPEED_VALUE(DeepNS.FSMETA, "shutterSpeedValue", Type.DBL, false),
  /** Subject distance. */
  SUBJECT_DISTANCE(DeepNS.FSMETA, "subjectDistance", Type.DBL, false),
  /** X resolution. */
  X_RESOLUTION(DeepNS.FSMETA, "xResolution", Type.DBL, false),
  /** Y resolution. */
  Y_RESOLUTION(DeepNS.FSMETA, "yResolution", Type.DBL, false),

  // ----- string fields -----------------------------------------------------

  /** Abstract. */
  ABSTRACT(DeepNS.DCTERMS, "abstract", Type.STR, true),
  /** Album name. */
  ALBUM(DeepNS.FSMETA, "album", Type.STR, true),
  /** Alternative title. */
  ALTERNATIVE(DeepNS.DCTERMS, "alternative", Type.STR, true),
  /** Artist. */
  ARTIST(DeepNS.FSMETA, "artist", Type.STR, true),
  /** City. */
  CITY(DeepNS.FSMETA, "city", Type.STR, true),
  /** Codec. */
  CODEC(DeepNS.FSMETA, "codec", Type.STR, false),
  /** Color space. */
  COLOR_SPACE(DeepNS.FSMETA, "colorSpace", Type.STR, false),
  /** Comment. */
  COMMENT(DeepNS.FSMETA, "comment", Type.STR, true),
  /** Composer. */
  COMPOSER(DeepNS.FSMETA, "composer", Type.STR, true),
  /** Contrast. */
  CONTRAST(DeepNS.FSMETA, "contrast", Type.STR, false),
  /** Contributor. */
  CONTRIBUTOR(DeepNS.DCTERMS, "contributor", Type.STR, true),
  /** Carbon copy receiver (name). */
  COPY_RECEIVER_NAME(DeepNS.FSMETA, "copyReceiverName", Type.STR, true),
  /** Carbon copy receiver (email address). */
  COPY_RECEIVER_EMAIL(DeepNS.FSMETA, "copyReceiverEmail", Type.STR, true),
  /** Country. */
  COUNTRY(DeepNS.FSMETA, "country", Type.STR, true),
  /** Creator (name). */
  CREATOR_NAME(DeepNS.FSMETA, "creatorName", Type.STR, true),
  /** Creator (email address). */
  CREATOR_EMAIL(DeepNS.FSMETA, "creatorEmail", Type.STR, true),
  /** Custom rendered. */
  CUSTOM_RENDERED(DeepNS.FSMETA, "customRendered", Type.STR, false),
  /** Description. */
  DESCRIPTION(DeepNS.DCTERMS, "description", Type.STR, true),
  /** Emphasis. */
  EMPHASIS(DeepNS.FSMETA, "emphasis", Type.STR, false),
  /** Encoding software. */
  ENCODER(DeepNS.FSMETA, "encoder", Type.STR, false),
  /** Encoding. */
  ENCODING(DeepNS.FSMETA, "encoding", Type.STR, false),
  /** Exposure mode. */
  EXPOSURE_MODE(DeepNS.FSMETA, "exposureMode", Type.STR, false),
  /** Exposure time as string. */
  EXPOSURE_TIME(DeepNS.FSMETA, "exposureTime", Type.STR, false),
  /** Exposure program. */
  EXPOSURE_PROGRAM(DeepNS.FSMETA, "exposureProgram", Type.STR, false),
  /** Flash. */
  FLASH(DeepNS.FSMETA, "flash", Type.STR, false),
  /** Focal plane resolution unit. */
  FOCAL_PLANE_RESOLUTION_UNIT(DeepNS.FSMETA, "focalPlaneResolutionUnit",
      Type.STR, false),
  /** Gain control. */
  GAIN_CONTROL(DeepNS.FSMETA, "gainControl", Type.STR, false),
  /** Genre. */
  GENRE(DeepNS.FSMETA, "genre", Type.STR, true),
  /**
   * Headline. Publishable entry providing a synopsis of the contents of the
   * item.
   */
  HEADLINE(DeepNS.FSMETA, "headline", Type.STR, false),
  /** Blind carbon copy receiver (name). */
  HIDDEN_RECEIVER_NAME(DeepNS.FSMETA, "hiddenReceiverName", Type.STR, true),
  /** Blind carbon copy receiver (email address). */
  HIDDEN_RECEIVER_EMAIL(DeepNS.FSMETA, "hiddenReceiverEmail", Type.STR, true),
  /** Unique identifier. */
  IDENTIFIER(DeepNS.DCTERMS, "identifier", Type.STR, false),
  /** Keyword. */
  KEYWORD(DeepNS.FSMETA, "keyword", Type.STR, true),
  /**
   * Language.
   * @see <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a>
   */
  LANGUAGE(DeepNS.DCTERMS, "language", Type.STR, false),
  /** Light source. */
  LIGHT_SOURCE(DeepNS.FSMETA, "lightSource", Type.STR, false),
  /** Lyrics. */
  LYRICS(DeepNS.FSMETA, "lyrics", Type.STR, true),
  /** Lyricist. */
  LYRICIST(DeepNS.FSMETA, "lyricist", Type.STR, true),
  /** Make. */
  MAKE(DeepNS.FSMETA, "make", Type.STR, false),
  /** Metering mode. */
  METERING_MODE(DeepNS.FSMETA, "meteringMode", Type.STR, false),
  /** Mode. */
  MODE(DeepNS.FSMETA, "mode", Type.STR, false),
  /** Model. */
  MODEL(DeepNS.FSMETA, "model", Type.STR, false),
  /** Orientation. */
  ORIENTATION(DeepNS.FSMETA, "orientation", Type.STR, false),
  /** Original artist. */
  ORIGINAL_ARTIST(DeepNS.FSMETA, "originalArtist", Type.STR, false),
  /** Primary chromaticities. */
  PRIMARY_CHROMATICITIES(DeepNS.FSMETA, "primaryChromaticities", Type.STR,
      false),
  /** Publisher. */
  PUBLISHER(DeepNS.DCTERMS, "publisher", Type.STR, true),
  /** Receiver (name). */
  RECEIVER_NAME(DeepNS.FSMETA, "receiverName", Type.STR, true),
  /** Receiver (email address). */
  RECEIVER_EMAIL(DeepNS.FSMETA, "receiverEmail", Type.STR, true),
  /** ReferenceBlackWhite. */
  REFERENCE_BLACK_WHITE(DeepNS.FSMETA, "referenceBlackWhite", Type.STR, false),
  /** Related sound file. */
  RELATED_SOUND_FILE(DeepNS.FSMETA, "relatedSoundFile", Type.STR, false),
  /** Resolution unit. */
  RESOLUTION_UNIT(DeepNS.FSMETA, "resolutionUnit", Type.STR, false),
  /** Copyright message. */
  RIGHTS(DeepNS.DCTERMS, "rights", Type.STR, false),
  /** Saturation. */
  SATURATION(DeepNS.FSMETA, "saturation", Type.STR, false),
  /** Sharpness. */
  SHARPNESS(DeepNS.FSMETA, "sharpness", Type.STR, false),
  /** Scene capture type. */
  SCENE_CAPTURE_TYPE(DeepNS.FSMETA, "sceneCaptureType", Type.STR, false),
  /** Sender (name). */
  SENDER_NAME(DeepNS.FSMETA, "senderName", Type.STR, false),
  /** Sender (email address). */
  SENDER_EMAIL(DeepNS.FSMETA, "senderEmail", Type.STR, false),
  /** Sensing method. */
  SENSING_METHOD(DeepNS.FSMETA, "sensingMethod", Type.STR, false),
  /** Set. */
  SET(DeepNS.FSMETA, "set", Type.STR, false),
  /** Software. */
  SOFTWARE(DeepNS.FSMETA, "software", Type.STR, false),
  /** Message or document subject. */
  SUBJECT(DeepNS.DCTERMS, "subject", Type.STR, false),
  /** Subject distance range. */
  SUBJECT_DISTANCE_RANGE(DeepNS.FSMETA, "subjectDistanceRange", Type.STR,
      false),
  /** Table of contents. */
  TABLE_OF_CONTENTS(DeepNS.DCTERMS, "tableOfContents", Type.STR, false),
  /** Title. */
  TITLE(DeepNS.DCTERMS, "title", Type.STR, false),
  /** White balance. */
  WHITE_BALANCE(DeepNS.FSMETA, "whiteBalance", Type.STR, false),
  /** White point. */
  WHITE_POINT(DeepNS.FSMETA, "whitePoint", Type.STR, false),
  /** YCbCrCoefficients. */
  YCBCR_COEFFICIENTS(DeepNS.FSMETA, "yCbCrCoefficients", Type.STR, false),
  /** YCbCrPositioning. */
  YCBCR_POSITIONING(DeepNS.FSMETA, "yCbCrPositioning", Type.STR, false);

  /** Metadata key. */
  private final String n;
  /** Namespace. */
  private final DeepNS ns;
  /** Default XML data type. */
  private final Type dt;
  /** More precise data type. */
  private Type pdt;
  /** Flag, if the metadata element may have multiple values. */
  private final boolean multiVal;

  /*
   * content container element. private final TreeMap<MetaElem, byte[]> c;
   */

  /**
   * Constructor for key-value pairs.
   * @param name metadata key
   * @param namespace namespace for the metadata attribute
   * @param dataType xml datatype
   * @param mv flag, if the metadata element may have multiple values
   */
  private MetaElem(final DeepNS namespace, final String name,
      final Type dataType, final boolean mv) {
    ns = namespace;
    n = ns.tag(name);
    dt = dataType;
    multiVal = mv;
  }

  /**
   * Constructor for the content container element (map with several key-value
   * pairs).
   * @param namespace namespace for the container element
   * @param name name of the container element
   */
  private MetaElem(final DeepNS namespace, final String name) {
    this(namespace, name, null, false);
  }

  /**
   * Returns the metadata attribute name.
   * @return the metadata attribute name
   */
  public String get() {
    return n;
  }

  /**
   * Returns the metadata attribute name as byte[].
   * @return the metadata attribute name as byte[]
   */
  public byte[] tok() {
    return Token.token(n);
  }

  /**
   * Returns the xml datatype for the metadata attribute.
   * @return the xml datatype for the metadata attribute
   */
  public Type getType() {
    if(pdt != null) return pdt;
    return dt;
  }

  /**
   * Returns true, if multiple values are allowed for the metadata attribute.
   * @return true, if multiple values are allowed for the metadata attribute
   */
  public boolean isMultiVal() {
    return multiVal;
  }

  /**
   * Returns the content for a container element.
   * @return the content as map (containing key-value pairs). public
   *         TreeMap<MetaElem, byte[]> getContent() { return c; }
   */

  @Override
  public String toString() {
    return n;
  }

  /**
   * Overrides the default data type of the metadata element with a more precise
   * data type (e.g. "short" instead of "integer").
   * @param dataType the new xml data type to set for this metadata element
   */
  void refineDataType(final Type dataType) {
    if(!dataType.instance(dt)) Main.notexpected("Failed to refine the xml " +
        "data type " + "for the metadata element " + n
        + " (invalid data type: " + dataType + ")");
    else pdt = dataType;
  }

  /**
   * Resets this metadata element to its default values (e.g. removes a
   * previously set refined xml data type).
   */
  void reset() {
    pdt = null;
  }
}
