package org.deepfs.fsml.util;

import static org.basex.util.Token.string;
import org.deepfs.fsml.util.DeepFile.NS;
import org.basex.core.Main;
import org.basex.query.item.Type;

/**
 * Available metadata elements.
 * @author Bastian Lemke
 */
public enum MetaElem {

  /** Duration. */
  DURATION(NS.FSMETA, "duration", Type.DUR),

  // ----- date fields -------------------------------------------------------

  /** Other date. */
  DATE(NS.DCTERMS, "date", Type.DAT),

  /** Date of the last change made to a metadata attribute. */
  DATE_ATTRIBUTE_MODIFIED(NS.FSMETA, "dateAttributeModified", Type.DTM),
  /** Date of the last change made to the content. */
  DATE_CONTENT_MODIFIED(NS.FSMETA, "dateContentModified", Type.DTM),
  /** Date when the content was created. */
  DATE_CREATED(NS.FSMETA, "dateCreated", Type.DTM),
  /** Date of the last usage. */
  DATE_LAST_USED(NS.FSMETA, "dateLastUsed", Type.DTM),

  // ----- integer fields ----------------------------------------------------

  /** Group ID of the owner of the file. */
  FS_OWNER_GROUP_ID(NS.FSMETA, "fsOwnerGroupId", Type.ITR),
  /** User ID of the owner of the file. */
  FS_OWNER_USER_ID(NS.FSMETA, "fsOwnerUserId", Type.ITR),
  /** Size of the file in the file system. */
  FS_SIZE(NS.FSMETA, "fsSize", Type.ITR),
  /** Height in millimeters. */
  MM_HEIGHT(NS.FSMETA, "mmHeight", Type.ITR),
  /** Width in millimeters. */
  MM_WIDTH(NS.FSMETA, "mmWidth", Type.ITR),
  /** Number of pages. */
  NUMBER_OF_PAGES(NS.FSMETA, "numberOfPages", Type.ITR),
  /** Height in pixels. */
  PIXEL_HEIGHT(NS.FSMETA, "pixelHeight", Type.ITR),
  /** Width in pixels. */
  PIXEL_WIDTH(NS.FSMETA, "pixelWidth", Type.ITR),
  /** Track number. */
  TRACK(NS.FSMETA, "track", Type.ITR),

  // ----- string fields -----------------------------------------------------

  /** Abstract. */
  ABSTRACT(NS.DCTERMS, "abstract", Type.STR),
  /** Album name. */
  ALBUM(NS.FSMETA, "album", Type.STR),
  /** Alternative title. */
  ALTERNATIVE(NS.DCTERMS, "alternative", Type.STR),
  /** Comment. */
  COMMENT(NS.FSMETA, "comment", Type.STR),
  /** Composer. */
  COMPOSER(NS.FSMETA, "composer", Type.STR),
  /** Contributor. */
  CONTRIBUTOR(NS.DCTERMS, "contributor", Type.STR),
  /** Carbon copy receiver. */
  COPY_RECEIVER(NS.FSMETA, "copyReceiver", Type.STR),
  /** Creator. */
  CREATOR(NS.DCTERMS, "creator", Type.STR),
  /** Description. */
  DESCRIPTION(NS.DCTERMS, "description", Type.STR),
  /** Text encoding. */
  ENCODING(NS.FSMETA, "encoding", Type.STR),
  /** Genre. */
  GENRE(NS.FSMETA, "genre", Type.STR),
  /**
   * Headline. Publishable entry providing a synopsis of the contents of the
   * item.
   */
  HEADLINE(NS.FSMETA, "headline", Type.STR),
  /** Blind carbon copy receiver. */
  HIDDEN_RECEIVER(NS.FSMETA, "hiddenReceiver", Type.STR),
  /** Unique identifier. */
  IDENTIFIER(NS.DCTERMS, "identifier", Type.STR),
  /** Keyword. */
  KEYWORD(NS.FSMETA, "keyword", Type.STR),
  /**
   * Language.
   * @see <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a>
   */
  LANGUAGE(NS.DCTERMS, "language", Type.STR),
  /** Lyricist. */
  LYRICIST(NS.FSMETA, "lyricist", Type.STR),

  // ----- location -----
  /** City. */
  CITY(NS.FSMETA, "city", Type.STR),
  /** Country. */
  COUNTRY(NS.FSMETA, "country", Type.STR),

  /** Publisher. */
  PUBLISHER(NS.DCTERMS, "publisher", Type.STR),
  /** Receiver. */
  RECEIVER(NS.FSMETA, "receiver", Type.STR),
  /** Sender. */
  SENDER(NS.FSMETA, "sender", Type.STR),
  /** Message or document subject. */
  SUBJECT(NS.DCTERMS, "subject", Type.STR),
  /** Table of contents. */
  TABLE_OF_CONTENTS(NS.DCTERMS, "tableOfContents", Type.STR),
  /** Title. */
  TITLE(NS.DCTERMS, "title", Type.STR),
  /** Type. */
  TYPE(NS.DCTERMS, "type", Type.STR),
  /** Format (MIME type). */
  FORMAT(NS.DCTERMS, "format", Type.STR),

  /** container element "content". */
  CONTENT(NS.DEEPURL, "content");

  /** Metadata key as byte array. */
  private final byte[] n;
  /** Namespace. */
  private final NS ns;
  /** Default XML data type. */
  private final Type dt;
  /** More precise data type. */
  private Type pdt;

  /*
   * content container element. private final TreeMap<MetaElem, byte[]> c;
   */

  /**
   * Constructor for key-value pairs.
   * @param name metadata key.
   * @param namespace namespace for the metadata attribute.
   * @param dataType xml datatype.
   */
  private MetaElem(final NS namespace, final String name, final Type dataType) {
    ns = namespace;
    n = ns.tag(name);
    dt = dataType;
    // c = null;
  }

  /**
   * Constructor for the content container element (map with several key-value
   * pairs).
   * @param namespace namespace for the container element.
   * @param name name of the container element.
   */
  private MetaElem(final NS namespace, final String name) {
    ns = namespace;
    n = ns.tag(name);
    dt = null;
    // c = new TreeMap<MetaElem, byte[]>();
  }

  /**
   * Returns the metadata attribute name as byte array.
   * @return the metadata attribute name.
   */
  public byte[] get() {
    return n;
  }

  /**
   * Returns the xml datatype for the metadata attribute.
   * @return the xml datatype for the metadata attribute.
   */
  public Type getType() {
    if(pdt != null) return pdt;
    return dt;
  }

  /**
   * Returns the content for a container element.
   * @return the content as map (containing key-value pairs). public
   *         TreeMap<MetaElem, byte[]> getContent() { return c; }
   */

  @Override
  public String toString() {
    return string(n);
  }

  /**
   * Override the default data type of the metadata element with a more precise
   * data type (e.g. "short" instead of "integer").
   * @param dataType the new xml data type to set for this metadata element.
   */
  void refineDataType(final Type dataType) {
    if(!dataType.instance(dt)) Main.bug("Failed to refine the xml data type "
        + "for the metadata element " + string(n) + " (invalid data type: "
        + dataType + ")");
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
