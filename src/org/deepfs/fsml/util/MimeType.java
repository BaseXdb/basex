package org.deepfs.fsml.util;

import org.basex.core.Main;
import org.basex.util.Token;

/**
 * Available MIME types.
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Bastian Lemke
 */
public enum MimeType {
  /** Apple Mail to do. */
  APPLE_MAIL_TODO("application/vnd.apple.mail+todo", ""),
  /** BMP. */
  BMP("image/bmp", "bmp", FileType.PICTURE),
  /** CSS. */
  CSS("text/css", "css", FileType.TEXT),
  /** text/directory */
  DIRECTORY("text/directory", ""),
  /** M$ Word. */
  DOC("application/msword", "doc", FileType.DOCUMENT),
  /** Email. */
  EML("message/rfc822", "eml", FileType.MESSAGE),
  /** ? */
  FORCE_DOWNLOAD("application/force-download", ""),
  /** GIF. */
  GIF("image/gif", "gif", FileType.PICTURE),
  /** HTML. */
  HTML("text/html", "html", FileType.TEXT),
  /** ICS. */
  ICS("application/ics", "ics", FileType.CALENDAR),
  /** Java archive. */
  JAR("application/java-archive", "jar", FileType.ARCHIVE),
  /** Java source file. */
  JAVA("text/x-java-source", "java", FileType.TEXT),
  /** Java source file (alternate MIME type). */
  JAVA2("text/plain", "java", FileType.TEXT),
  /** JPG. */
  JPG("image/jpeg", "jpg", FileType.PICTURE),
  /** KML. */
  KML("application/vnd.google-earth.kml+xml", "kml", FileType.XML,
      FileType.MAP),
  /** MP3. */
  MP3("audio/mp3", "mp3", FileType.AUDIO),
  /** Object file. */
  O("application/octet-stream", "o"),
  /** ODS. */
  ODS("application/vnd.oasis.opendocument.spreadsheet", "ods",
      FileType.DOCUMENT),
  /** PDF. */
  PDF("application/pdf", "pdf", FileType.DOCUMENT),
  /** pgp signature. */
  PGP("application/pgp-signature", ""),
  /** pkcs7-signature. */
  PKCS7("application/pkcs7-signature", ""),
  /** PNG. */
  PNG("image/png", "png", FileType.PICTURE),
  /** PPS. */
  PPS("application/vnd.ms-powerpoint", "pps", FileType.PRESENTATION),
  /** Rich text format. */
  RTF("text/rtf", "rtf", FileType.TEXT, FileType.DOCUMENT),
  /** TIFF. */
  TIFF("image/tiff", "tif", FileType.PICTURE),
  /** Plaintext. */
  TXT("text/plain", "txt", FileType.TEXT),
  /** Unknown media. */
  UNKNOWN("unkown", "", FileType.UNKNOWN),
  /** Vcard. */
  VCARD("text/x-vcard", "vcf", FileType.CONTACT),
  /** XLS. */
  XLS("application/vnd.ms-excel", "xls", FileType.DOCUMENT),
  /** XML. */
  XML("application/xml", "xml", FileType.XML),
  /** XML. */
  XML2("text/xml", "xml", FileType.XML),
  /** x-pkcs7-signature. */
  XPKCS7("application/x-pkcs7-signature", ""),
  /** Zip. */
  ZIP("application/zip", "zip", FileType.ARCHIVE);

  /** The element name as byte array. */
  private final byte[] elem;
  /** The default file suffix. */
  private final byte[] suff;

  /** The associated meta types. */
  private FileType[] types;

  /**
   * Constructor for initializing an element.
   * @param element the xml element string.
   * @param defaultSuffix the default file suffix for the MIME type.
   * @param mt the associated meta types.
   */
  MimeType(final String element, final String defaultSuffix,
      final FileType... mt) {
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
  public FileType[] getMetaTypes() {
    return types;
  }

  /**
   * Tries to find the MimeType item for the given MIME type string.
   * @param name the MIME type string to find.
   * @return the {@link MimeType} item or <code>null</code> if the item was not
   *         found.
   */
  public static MimeType getItem(final String name) {
    final byte[] token = Token.token(name);
    for(final MimeType mt : MimeType.values()) {
      if(Token.eq(mt.elem, token)) return mt;
    }
    Main.debug("MIME type not found: " + name);
    return null;
  }
}
