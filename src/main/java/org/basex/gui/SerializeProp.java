package org.basex.gui;

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
  public static final Object[] STANDALONE = { "standalone", "omit" };
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

      if(sprop.length != 2 || obj == null)
        throw new IOException(Main.info(SETKEY, key));

      final Object val = sprop[1].trim();
      if(obj instanceof Boolean) {
        if(!val.equals(YES) && !val.equals(NO))
          throw new IOException(Main.info(SETVAL, key, val));

        set(key, val.equals(YES));
      } else {
        set(key, val);
      }
    }
  }
}
