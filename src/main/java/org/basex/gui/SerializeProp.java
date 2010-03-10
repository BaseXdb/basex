package org.basex.gui;

import static org.basex.core.Text.*;
import org.basex.core.AProp;
import org.basex.util.TokenBuilder;

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

  /** Serialization parameter. */
  public static final Object[] BYTE_ORDER_MARK = { "byte-order-mark", "" };
  /** Serialization parameter. */
  public static final Object[] CDATA_SECTION_ELEMENTS =
    { "cdata-section-elements", "" };
  /** Serialization parameter. */
  public static final Object[] DOCTYPE_PUBLIC = { "doctype-public", "" };
  /** Serialization parameter. */
  public static final Object[] DOCTYPE_SYSTEM = { "doctype-system", "" };
  /** Serialization parameter. */
  public static final Object[] ENCODING = { "encoding", "UTF-8" };
  /** Serialization parameter. */
  public static final Object[] ESCAPE_URI_ATTRIBUTES = {
    "escape-uri-attributes", true };
  /** Serialization parameter. */
  public static final Object[] INCLUDE_CONTENT_TYPE = {
    "include-content-type", true };
  /** Serialization parameter. */
  public static final Object[] INDENT = { "indent", true };
  /** Serialization parameter. */
  public static final Object[] MEDIA_TYPE = { "media-type", "" };
  /** Serialization parameter. */
  public static final Object[] METHOD = { "method", "xml" };
  /** Serialization parameter. */
  public static final Object[] NORMALIZATION_FORM = {
    "normalization-form", "NFC" };
  /** Serialization parameter. */
  public static final Object[] OMIT_XML_DECLARATION = {
    "omit-xml-declaration", true };
  /** Serialization parameter. */
  public static final Object[] STANDALONE = { "standalone", false };
  /** Serialization parameter. */
  public static final Object[] UNDECLARE_PREFIXES = {
    "undeclare-prefixes", true };
  /** Serialization parameter. */
  public static final Object[] USE_CHARACTER_MAPS = {
    "use-character-maps", true };
  /** Serialization parameter. */
  public static final Object[] VERSION = { "version", "1.0" };
  /** Hidden serialization parameter. */
  public static final Object[] WRAP_PRE = { "wrap-pre", "" };
  /** Hidden serialization parameter. */
  public static final Object[] WRAP_URI = { "wrap-uri", "" };

  /** Info log. */
  private final TokenBuilder log = new TokenBuilder();
  
  /**
   * Constructor.
   */
  public SerializeProp() {
    this("");
  }
  
  /**
   * Constructor, specifying initial properties.
   * @param s property string. Properties are separated with commas,
   * key/values with equality.
   */
  public SerializeProp(final String s) {
    super(null);
    if(s == null) return;
    
    for(final String ser : s.split(",")) {
      final String[] sprop = ser.split("=");
      final String key = sprop[0].trim();
      final Object obj = object(key);
      if(sprop.length != 2 || obj == null) {
        log.add(SETKEY + NL, key);
      } else {
        final Object val = sprop[1].trim();
        if(obj instanceof Boolean) {
          if(!val.equals(YES) && !val.equals(NO)) {
            log.add(SETVAL + NL, key, val);
          } else {
            set(key, val.equals(YES));
          }
        } else {
          set(key, val);
        }
      }
    }
  }
  
  /**
   * Returns logging information.
   * @return logging string
   */
  public String log() {
    return log.toString();
  }
}
