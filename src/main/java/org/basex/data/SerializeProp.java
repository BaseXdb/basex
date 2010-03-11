package org.basex.data;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.AProp;
import org.basex.core.Main;

/**
 * This class contains serialization properties.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SerializeProp extends AProp {
  /** Yes flag. */
  public static final String YES = "yes";
  /** No flag. */
  public static final String NO = "no";
  /** Omit flag. */
  public static final String OMIT = "omit";

  // SUPPORTED PARAMETERS =====================================================
  
  /** Serialization parameter: list of QNames. */
  public static final Object[] S_CDATA_SECTION_ELEMENTS = {
    "cdata-section-elements", "" };
  /** Serialization parameter: valid encoding. */
  public static final Object[] S_ENCODING = {
    "encoding", "UTF-8" };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_INDENT = {
    "indent", YES };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_OMIT_XML_DECLARATION = {
    "omit-xml-declaration", YES };
  /** Serialization parameter: yes/no/omit. */
  public static final Object[] S_STANDALONE = {
    "standalone", "omit" };
  /** Serialization parameter: 1.0/1.1. */
  public static final Object[] S_VERSION = {
    "version", "1.0" };

  /** Hidden serialization parameter. */
  public static final Object[] S_WRAP_PRE = {
    "wrap-pre", "" };
  /** Hidden serialization parameter. */
  public static final Object[] S_WRAP_URI = {
    "wrap-uri", "" };

  // UNSUPPORTED PARAMETERS ===================================================
  
  /** Serialization parameter: yes/no. */
  public static final Object[] S_BYTE_ORDER_MARK = {
    "byte-order-mark", NO };
  /** Serialization parameter. */
  public static final Object[] S_DOCTYPE_PUBLIC = {
    "doctype-public", "" };
  /** Serialization parameter. */
  public static final Object[] S_DOCTYPE_SYSTEM = {
    "doctype-system", "" };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_ESCAPE_URI_ATTRIBUTES = {
    "escape-uri-attributes", YES };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_INCLUDE_CONTENT_TYPE = {
    "include-content-type", YES };
  /** Serialization parameter. */
  public static final Object[] S_MEDIA_TYPE = {
    "media-type", "" };
  /** Serialization parameter: xml/xhtml/html/text. */
  public static final Object[] S_METHOD = {
    "method", "xml" };
  /** Serialization parameter: NFC/NFD/NFKC/NKFD/fully-normalized/none. */
  public static final Object[] S_NORMALIZATION_FORM = {
    "normalization-form", "NFC" };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_UNDECLARE_PREFIXES = {
    "undeclare-prefixes", YES };
  /** Serialization parameter. */
  public static final Object[] S_USE_CHARACTER_MAPS = {
    "use-character-maps", YES };

  /**
   * Constructor.
   * @throws IOException I/O exception
   */
  public SerializeProp() throws IOException {
    this(null);
  }

  /**
   * Constructor, specifying initial properties.
   * @param s property string. Properties are separated with commas,
   * key/values with equality.
   * @throws IOException I/O exception
   */
  public SerializeProp(final String s) throws IOException {
    super(null);
    if(s == null) return;

    for(final String ser : s.trim().split(",")) {
      if(ser.length() == 0) continue;
      final String[] sprop = ser.split("=");
      final String key = sprop[0].trim();
      final Object obj = object(key);
      if(sprop.length == 2 && obj != null) set(key, sprop[1].trim());
      else throw new IOException(Main.info(SETKEY, key));
    }
  }
}
