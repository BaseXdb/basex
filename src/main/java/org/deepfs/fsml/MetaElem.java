package org.deepfs.fsml;

import static org.deepfs.fsml.DeepNS.*;
import static org.basex.query.item.AtomType.*;
import org.basex.query.item.AtomType;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Available metadata elements.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Bastian Lemke
 */
public enum MetaElem {

  /** Type. */
  TYPE(DCTERMS, "type", STR, true),
  /** Format (MIME type). */
  FORMAT(DCTERMS, "format", STR, false),

  // ----- duration fields ---------------------------------------------------

  /** Duration. */
  DURATION(FSMETA, "duration", DUR, false),

  // ----- date fields -------------------------------------------------------

  /** Date of the last change made to a metadata attribute. */
  DATETIME_ATTRIBUTE_MODIFIED(FSMETA, "dateTimeAttributeModified",
      DTM, false),
  /** Date when the content was created. */
  DATETIME_CREATED(FSMETA, "dateTimeCreated", DTM, false),
  /** Date when the content was digitized. */
  DATETIME_DIGITIZED(FSMETA, "dateTimeDigitized", DTM, false),
  /** Date of the last usage. */
  DATETIME_LAST_USED(FSMETA, "dateTimeLastUsed", DTM, false),
  /** Original date. */
  DATETIME_ORIGINAL(FSMETA, "dateTimeOriginal", DTM, false),
  /** Year. */
  YEAR(FSMETA, "year", YEA, false),

  // ----- integer fields ----------------------------------------------------

  /** Beats per minute. */
  BEATS_PER_MINUTE(FSMETA, "beatsPerMinute", INT, false),
  /** Bitrate. */
  BITRATE_KBIT(FSMETA, "bitrateKBitS", INT, false),
  /** iTunes compilation flag. */
  ITUNES_COMPILATION(FSMETA, "iTunesCompilation", INT, false),
  /** ISO speed ratings. */
  ISO_SPEED_RATINGS(FSMETA, "isoSpeedRatings", INT, false),
  /** Focal length in 35mm film. */
  FOCAL_LENGTH_IN_35MM_FILM(FSMETA, "focalLengthIn35mmFilm", INT, false),
  /** Group id of the owner of the file. */
  FS_OWNER_GROUP_ID(FSMETA, "fsOwnerGroupId", INT, false),
  /** User id of the owner of the file. */
  FS_OWNER_USER_ID(FSMETA, "fsOwnerUserId", INT, false),
  /** Number of pages. */
  NUMBER_OF_PAGES(FSMETA, "numberOfPages", INT, false),
  /** Height in pixels. */
  PIXEL_HEIGHT(FSMETA, "pixelHeight", INT, false),
  /** Width in pixels. */
  PIXEL_WIDTH(FSMETA, "pixelWidth", INT, false),
  /** Sample rate. */
  SAMPLE_RATE(FSMETA, "sampleRate", INT, false),
  /** Track number. */
  TRACK(FSMETA, "track", INT, false),

  // ----- double fields -----------------------------------------------------

  /** Aperture value. */
  APERTURE_VALUE(FSMETA, "apertureValue", DBL, false),
  /** Maximum aperture value. */
  APERTURE_VALUE_MAX(FSMETA, "apertureValueMax", DBL, false),
  /** Brightness value. */
  BRIGHTNESS_VALUE(FSMETA, "brightnessValue", DBL, false),
  /** Compressed bits per pixel. */
  COMPRESSED_BITS_PER_PIXEL(FSMETA, "compressedBitsPerPixel", DBL, false),
  /** Digital zoom ratio. */
  DIGITAL_ZOOM_RATIO(FSMETA, "digitalZoomRatio", DBL, false),
  /** Exposure bias value. */
  EXPOSURE_BIAS_VALUE(FSMETA, "exposureBiasValue", DBL, false),
  /** Exposure index. */
  EXPOSURE_INDEX(FSMETA, "exposureIndex", DBL, false),
  /** Exposure time in seconds. */
  EXPOSURE_TIME_MS(FSMETA, "exposureTimeMs", DBL, false),
  /** F number. */
  F_NUMBER(FSMETA, "fNumber", DBL, false),
  /** Focal length. */
  FOCAL_LENGTH(FSMETA, "focalLengthMM", DBL, false),
  /** Focal plane X resolution. */
  FOCAL_PLANE_X_RESOLUTION(FSMETA, "focalPlaneXresolution", DBL, false),
  /** Focal plane Y resolution. */
  FOCAL_PLANE_Y_RESOLUTION(FSMETA, "focalPlaneYresolution", DBL, false),
  /** Shutter speed value. */
  SHUTTER_SPEED_VALUE(FSMETA, "shutterSpeedValue", DBL, false),
  /** Subject distance. */
  SUBJECT_DISTANCE(FSMETA, "subjectDistance", DBL, false),
  /** X resolution. */
  X_RESOLUTION(FSMETA, "xResolution", DBL, false),
  /** Y resolution. */
  Y_RESOLUTION(FSMETA, "yResolution", DBL, false),

  // ----- string fields -----------------------------------------------------

  /** Album name. */
  ALBUM(FSMETA, "album", STR, true),
  /** Artist. */
  ARTIST(FSMETA, "artist", STR, true),
  /** City. */
  CITY(FSMETA, "city", STR, true),
  /** Codec. */
  CODEC(FSMETA, "codec", STR, false),
  /** Color space. */
  COLOR_SPACE(FSMETA, "colorSpace", STR, false),
  /** Comment. */
  COMMENT(FSMETA, "comment", STR, true),
  /** Composer. */
  COMPOSER(FSMETA, "composer", STR, true),
  /** Contrast. */
  CONTRAST(FSMETA, "contrast", STR, false),
  /** Contributor. */
  CONTRIBUTOR(DCTERMS, "contributor", STR, true),
  /** Carbon copy receiver (name). */
  COPY_RECEIVER_NAME(FSMETA, "copyReceiverName", STR, true),
  /** Carbon copy receiver (email address). */
  COPY_RECEIVER_EMAIL(FSMETA, "copyReceiverEmail", STR, true),
  /** Country. */
  COUNTRY(FSMETA, "country", STR, true),
  /** Creator (name). */
  CREATOR_NAME(FSMETA, "creatorName", STR, true),
  /** Creator (email address). */
  CREATOR_EMAIL(FSMETA, "creatorEmail", STR, true),
  /** Custom rendered. */
  CUSTOM_RENDERED(FSMETA, "customRendered", STR, false),
  /** Description. */
  DESCRIPTION(DCTERMS, "description", STR, true),
  /** Emphasis. */
  EMPHASIS(FSMETA, "emphasis", STR, false),
  /** Encoding software. */
  ENCODER(FSMETA, "encoder", STR, false),
  /** Encoding. */
  ENCODING(FSMETA, "encoding", STR, false),
  /** Exposure mode. */
  EXPOSURE_MODE(FSMETA, "exposureMode", STR, false),
  /** Exposure time as string. */
  EXPOSURE_TIME(FSMETA, "exposureTime", STR, false),
  /** Exposure program. */
  EXPOSURE_PROGRAM(FSMETA, "exposureProgram", STR, false),
  /** Flash. */
  FLASH(FSMETA, "flash", STR, false),
  /** Focal plane resolution unit. */
  FOCAL_PLANE_RESOLUTION_UNIT(FSMETA, "focalPlaneResolutionUnit", STR, false),
  /** Gain control. */
  GAIN_CONTROL(FSMETA, "gainControl", STR, false),
  /** Genre. */
  GENRE(FSMETA, "genre", STR, true),
  /**
   * Headline. Publishable entry providing a synopsis of the contents of the
   * item.
   */
  HEADLINE(FSMETA, "headline", STR, false),
  /** Blind carbon copy receiver (name). */
  HIDDEN_RECEIVER_NAME(FSMETA, "hiddenReceiverName", STR, true),
  /** Blind carbon copy receiver (email address). */
  HIDDEN_RECEIVER_EMAIL(FSMETA, "hiddenReceiverEmail", STR, true),
  /** Unique identifier. */
  IDENTIFIER(DCTERMS, "identifier", STR, false),
  /** Keyword. */
  KEYWORD(FSMETA, "keyword", STR, true),
  /**
   * Language.
   * @see <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a>
   */
  LANGUAGE(DCTERMS, "language", STR, false),
  /** Light source. */
  LIGHT_SOURCE(FSMETA, "lightSource", STR, false),
  /** Lyrics. */
  LYRICS(FSMETA, "lyrics", STR, true),
  /** Lyricist. */
  LYRICIST(FSMETA, "lyricist", STR, true),
  /** Make. */
  MAKE(FSMETA, "make", STR, false),
  /** Metering mode. */
  METERING_MODE(FSMETA, "meteringMode", STR, false),
  /** Mode. */
  MODE(FSMETA, "mode", STR, false),
  /** Model. */
  MODEL(FSMETA, "model", STR, false),
  /** Orientation. */
  ORIENTATION(FSMETA, "orientation", STR, false),
  /** Original artist. */
  ORIGINAL_ARTIST(FSMETA, "originalArtist", STR, false),
  /** Primary chromaticities. */
  PRIMARY_CHROMATICITIES(FSMETA, "primaryChromaticities", STR, false),
  /** Publisher. */
  PUBLISHER(DCTERMS, "publisher", STR, true),
  /** Receiver (name). */
  RECEIVER_NAME(FSMETA, "receiverName", STR, true),
  /** Receiver (email address). */
  RECEIVER_EMAIL(FSMETA, "receiverEmail", STR, true),
  /** ReferenceBlackWhite. */
  REFERENCE_BLACK_WHITE(FSMETA, "referenceBlackWhite", STR, false),
  /** Related sound file. */
  RELATED_SOUND_FILE(FSMETA, "relatedSoundFile", STR, false),
  /** Resolution unit. */
  RESOLUTION_UNIT(FSMETA, "resolutionUnit", STR, false),
  /** Copyright message. */
  RIGHTS(DCTERMS, "rights", STR, false),
  /** Saturation. */
  SATURATION(FSMETA, "saturation", STR, false),
  /** Sharpness. */
  SHARPNESS(FSMETA, "sharpness", STR, false),
  /** Scene capture type. */
  SCENE_CAPTURE_TYPE(FSMETA, "sceneCaptureType", STR, false),
  /** Sender (name). */
  SENDER_NAME(FSMETA, "senderName", STR, false),
  /** Sender (email address). */
  SENDER_EMAIL(FSMETA, "senderEmail", STR, false),
  /** Sensing method. */
  SENSING_METHOD(FSMETA, "sensingMethod", STR, false),
  /** Set. */
  SET(FSMETA, "set", STR, false),
  /** Software. */
  SOFTWARE(FSMETA, "software", STR, false),
  /** Message or document subject. */
  SUBJECT(DCTERMS, "subject", STR, false),
  /** Subject distance range. */
  SUBJECT_DISTANCE_RANGE(FSMETA, "subjectDistanceRange", STR, false),
  /** Title. */
  TITLE(DCTERMS, "title", STR, false),
  /** White balance. */
  WHITE_BALANCE(FSMETA, "whiteBalance", STR, false),
  /** White point. */
  WHITE_POINT(FSMETA, "whitePoint", STR, false),
  /** YCbCrCoefficients. */
  YCBCR_COEFFICIENTS(FSMETA, "yCbCrCoefficients", STR, false),
  /** YCbCrPositioning. */
  YCBCR_POSITIONING(FSMETA, "yCbCrPositioning", STR, false);

  /** Metadata key. */
  private final String n;
  /** Namespace. */
  private final DeepNS ns;
  /** Default XML data type. */
  private final AtomType dt;
  /** More precise data type. */
  private AtomType pdt;
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
      final AtomType dataType, final boolean mv) {
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
  public AtomType getType() {
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
  void refineDataType(final AtomType dataType) {
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
