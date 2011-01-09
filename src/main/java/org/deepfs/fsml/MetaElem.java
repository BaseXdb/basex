package org.deepfs.fsml;

import static org.deepfs.fsml.DeepNS.*;
import org.basex.query.item.Type;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Available metadata elements.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Bastian Lemke
 */
public enum MetaElem {

  /** Type. */
  TYPE(DCTERMS, "type", Type.STR, true),
  /** Format (MIME type). */
  FORMAT(DCTERMS, "format", Type.STR, false),

  // ----- duration fields ---------------------------------------------------

  /** Duration. */
  DURATION(FSMETA, "duration", Type.DUR, false),

  // ----- date fields -------------------------------------------------------

  /** Date of the last change made to a metadata attribute. */
  DATETIME_ATTRIBUTE_MODIFIED(FSMETA, "dateTimeAttributeModified",
      Type.DTM, false),
  /** Date when the content was created. */
  DATETIME_CREATED(FSMETA, "dateTimeCreated", Type.DTM, false),
  /** Date when the content was digitized. */
  DATETIME_DIGITIZED(FSMETA, "dateTimeDigitized", Type.DTM, false),
  /** Date of the last usage. */
  DATETIME_LAST_USED(FSMETA, "dateTimeLastUsed", Type.DTM, false),
  /** Original date. */
  DATETIME_ORIGINAL(FSMETA, "dateTimeOriginal", Type.DTM, false),
  /** Year. */
  YEAR(FSMETA, "year", Type.YEA, false),

  // ----- integer fields ----------------------------------------------------

  /** Beats per minute. */
  BEATS_PER_MINUTE(FSMETA, "beatsPerMinute", Type.INT, false),
  /** Bitrate. */
  BITRATE_KBIT(FSMETA, "bitrateKBitS", Type.INT, false),
  /** iTunes compilation flag. */
  ITUNES_COMPILATION(FSMETA, "iTunesCompilation", Type.INT, false),
  /** ISO speed ratings. */
  ISO_SPEED_RATINGS(FSMETA, "isoSpeedRatings", Type.INT, false),
  /** Focal length in 35mm film. */
  FOCAL_LENGTH_IN_35MM_FILM(FSMETA, "focalLengthIn35mmFilm", Type.INT, false),
  /** Group id of the owner of the file. */
  FS_OWNER_GROUP_ID(FSMETA, "fsOwnerGroupId", Type.INT, false),
  /** User id of the owner of the file. */
  FS_OWNER_USER_ID(FSMETA, "fsOwnerUserId", Type.INT, false),
  /** Number of pages. */
  NUMBER_OF_PAGES(FSMETA, "numberOfPages", Type.INT, false),
  /** Height in pixels. */
  PIXEL_HEIGHT(FSMETA, "pixelHeight", Type.INT, false),
  /** Width in pixels. */
  PIXEL_WIDTH(FSMETA, "pixelWidth", Type.INT, false),
  /** Sample rate. */
  SAMPLE_RATE(FSMETA, "sampleRate", Type.INT, false),
  /** Track number. */
  TRACK(FSMETA, "track", Type.INT, false),

  // ----- double fields -----------------------------------------------------

  /** Aperture value. */
  APERTURE_VALUE(FSMETA, "apertureValue", Type.DBL, false),
  /** Maximum aperture value. */
  APERTURE_VALUE_MAX(FSMETA, "apertureValueMax", Type.DBL, false),
  /** Brightness value. */
  BRIGHTNESS_VALUE(FSMETA, "brightnessValue", Type.DBL, false),
  /** Compressed bits per pixel. */
  COMPRESSED_BITS_PER_PIXEL(FSMETA, "compressedBitsPerPixel", Type.DBL, false),
  /** Digital zoom ratio. */
  DIGITAL_ZOOM_RATIO(FSMETA, "digitalZoomRatio", Type.DBL, false),
  /** Exposure bias value. */
  EXPOSURE_BIAS_VALUE(FSMETA, "exposureBiasValue", Type.DBL, false),
  /** Exposure index. */
  EXPOSURE_INDEX(FSMETA, "exposureIndex", Type.DBL, false),
  /** Exposure time in seconds. */
  EXPOSURE_TIME_MS(FSMETA, "exposureTimeMs", Type.DBL, false),
  /** F number. */
  F_NUMBER(FSMETA, "fNumber", Type.DBL, false),
  /** Focal length. */
  FOCAL_LENGTH(FSMETA, "focalLengthMM", Type.DBL, false),
  /** Focal plane X resolution. */
  FOCAL_PLANE_X_RESOLUTION(FSMETA, "focalPlaneXresolution", Type.DBL, false),
  /** Focal plane Y resolution. */
  FOCAL_PLANE_Y_RESOLUTION(FSMETA, "focalPlaneYresolution", Type.DBL, false),
  /** Shutter speed value. */
  SHUTTER_SPEED_VALUE(FSMETA, "shutterSpeedValue", Type.DBL, false),
  /** Subject distance. */
  SUBJECT_DISTANCE(FSMETA, "subjectDistance", Type.DBL, false),
  /** X resolution. */
  X_RESOLUTION(FSMETA, "xResolution", Type.DBL, false),
  /** Y resolution. */
  Y_RESOLUTION(FSMETA, "yResolution", Type.DBL, false),

  // ----- string fields -----------------------------------------------------

  /** Album name. */
  ALBUM(FSMETA, "album", Type.STR, true),
  /** Artist. */
  ARTIST(FSMETA, "artist", Type.STR, true),
  /** City. */
  CITY(FSMETA, "city", Type.STR, true),
  /** Codec. */
  CODEC(FSMETA, "codec", Type.STR, false),
  /** Color space. */
  COLOR_SPACE(FSMETA, "colorSpace", Type.STR, false),
  /** Comment. */
  COMMENT(FSMETA, "comment", Type.STR, true),
  /** Composer. */
  COMPOSER(FSMETA, "composer", Type.STR, true),
  /** Contrast. */
  CONTRAST(FSMETA, "contrast", Type.STR, false),
  /** Contributor. */
  CONTRIBUTOR(DCTERMS, "contributor", Type.STR, true),
  /** Carbon copy receiver (name). */
  COPY_RECEIVER_NAME(FSMETA, "copyReceiverName", Type.STR, true),
  /** Carbon copy receiver (email address). */
  COPY_RECEIVER_EMAIL(FSMETA, "copyReceiverEmail", Type.STR, true),
  /** Country. */
  COUNTRY(FSMETA, "country", Type.STR, true),
  /** Creator (name). */
  CREATOR_NAME(FSMETA, "creatorName", Type.STR, true),
  /** Creator (email address). */
  CREATOR_EMAIL(FSMETA, "creatorEmail", Type.STR, true),
  /** Custom rendered. */
  CUSTOM_RENDERED(FSMETA, "customRendered", Type.STR, false),
  /** Description. */
  DESCRIPTION(DCTERMS, "description", Type.STR, true),
  /** Emphasis. */
  EMPHASIS(FSMETA, "emphasis", Type.STR, false),
  /** Encoding software. */
  ENCODER(FSMETA, "encoder", Type.STR, false),
  /** Encoding. */
  ENCODING(FSMETA, "encoding", Type.STR, false),
  /** Exposure mode. */
  EXPOSURE_MODE(FSMETA, "exposureMode", Type.STR, false),
  /** Exposure time as string. */
  EXPOSURE_TIME(FSMETA, "exposureTime", Type.STR, false),
  /** Exposure program. */
  EXPOSURE_PROGRAM(FSMETA, "exposureProgram", Type.STR, false),
  /** Flash. */
  FLASH(FSMETA, "flash", Type.STR, false),
  /** Focal plane resolution unit. */
  FOCAL_PLANE_RESOLUTION_UNIT(FSMETA, "focalPlaneResolutionUnit",
      Type.STR, false),
  /** Gain control. */
  GAIN_CONTROL(FSMETA, "gainControl", Type.STR, false),
  /** Genre. */
  GENRE(FSMETA, "genre", Type.STR, true),
  /**
   * Headline. Publishable entry providing a synopsis of the contents of the
   * item.
   */
  HEADLINE(FSMETA, "headline", Type.STR, false),
  /** Blind carbon copy receiver (name). */
  HIDDEN_RECEIVER_NAME(FSMETA, "hiddenReceiverName", Type.STR, true),
  /** Blind carbon copy receiver (email address). */
  HIDDEN_RECEIVER_EMAIL(FSMETA, "hiddenReceiverEmail", Type.STR, true),
  /** Unique identifier. */
  IDENTIFIER(DCTERMS, "identifier", Type.STR, false),
  /** Keyword. */
  KEYWORD(FSMETA, "keyword", Type.STR, true),
  /**
   * Language.
   * @see <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a>
   */
  LANGUAGE(DCTERMS, "language", Type.STR, false),
  /** Light source. */
  LIGHT_SOURCE(FSMETA, "lightSource", Type.STR, false),
  /** Lyrics. */
  LYRICS(FSMETA, "lyrics", Type.STR, true),
  /** Lyricist. */
  LYRICIST(FSMETA, "lyricist", Type.STR, true),
  /** Make. */
  MAKE(FSMETA, "make", Type.STR, false),
  /** Metering mode. */
  METERING_MODE(FSMETA, "meteringMode", Type.STR, false),
  /** Mode. */
  MODE(FSMETA, "mode", Type.STR, false),
  /** Model. */
  MODEL(FSMETA, "model", Type.STR, false),
  /** Orientation. */
  ORIENTATION(FSMETA, "orientation", Type.STR, false),
  /** Original artist. */
  ORIGINAL_ARTIST(FSMETA, "originalArtist", Type.STR, false),
  /** Primary chromaticities. */
  PRIMARY_CHROMATICITIES(FSMETA, "primaryChromaticities", Type.STR, false),
  /** Publisher. */
  PUBLISHER(DCTERMS, "publisher", Type.STR, true),
  /** Receiver (name). */
  RECEIVER_NAME(FSMETA, "receiverName", Type.STR, true),
  /** Receiver (email address). */
  RECEIVER_EMAIL(FSMETA, "receiverEmail", Type.STR, true),
  /** ReferenceBlackWhite. */
  REFERENCE_BLACK_WHITE(FSMETA, "referenceBlackWhite", Type.STR, false),
  /** Related sound file. */
  RELATED_SOUND_FILE(FSMETA, "relatedSoundFile", Type.STR, false),
  /** Resolution unit. */
  RESOLUTION_UNIT(FSMETA, "resolutionUnit", Type.STR, false),
  /** Copyright message. */
  RIGHTS(DCTERMS, "rights", Type.STR, false),
  /** Saturation. */
  SATURATION(FSMETA, "saturation", Type.STR, false),
  /** Sharpness. */
  SHARPNESS(FSMETA, "sharpness", Type.STR, false),
  /** Scene capture type. */
  SCENE_CAPTURE_TYPE(FSMETA, "sceneCaptureType", Type.STR, false),
  /** Sender (name). */
  SENDER_NAME(FSMETA, "senderName", Type.STR, false),
  /** Sender (email address). */
  SENDER_EMAIL(FSMETA, "senderEmail", Type.STR, false),
  /** Sensing method. */
  SENSING_METHOD(FSMETA, "sensingMethod", Type.STR, false),
  /** Set. */
  SET(FSMETA, "set", Type.STR, false),
  /** Software. */
  SOFTWARE(FSMETA, "software", Type.STR, false),
  /** Message or document subject. */
  SUBJECT(DCTERMS, "subject", Type.STR, false),
  /** Subject distance range. */
  SUBJECT_DISTANCE_RANGE(FSMETA, "subjectDistanceRange", Type.STR, false),
  /** Title. */
  TITLE(DCTERMS, "title", Type.STR, false),
  /** White balance. */
  WHITE_BALANCE(FSMETA, "whiteBalance", Type.STR, false),
  /** White point. */
  WHITE_POINT(FSMETA, "whitePoint", Type.STR, false),
  /** YCbCrCoefficients. */
  YCBCR_COEFFICIENTS(FSMETA, "yCbCrCoefficients", Type.STR, false),
  /** YCbCrPositioning. */
  YCBCR_POSITIONING(FSMETA, "yCbCrPositioning", Type.STR, false);

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
    if(!dataType.instance(dt)) Util.notexpected("Failed to refine the xml " +
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
