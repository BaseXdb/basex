package org.deepfs.fsml.util;

import org.basex.util.Token;

/**
 * Available file types.
 * @author Bastian Lemke
 */
public enum FileType {
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
  FileType() {
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