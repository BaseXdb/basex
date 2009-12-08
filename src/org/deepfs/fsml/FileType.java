package org.deepfs.fsml;

/**
 * Available file types.
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
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
  /** Script. */
  SCRIPT,
  /** Source code. */
  SOURCE_CODE,
  /** Text(-based) resource (e.g. plain text file). */
  TEXT,
  /** Unknown resource type. */
  UNKNOWN,
  /** Video resource (e.g. MPEG file). */
  VIDEO,
  /** Website. */
  WEBSITE,
  /** XML(-based) resource. */
  XML;

  @Override
  public String toString() {
    return name().toLowerCase().replace("_", " ");
  }
}
