package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.AProp;
import org.basex.core.Main;
import org.basex.util.Token;
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
  /** Omit flag. */
  public static final String OMIT = "omit";
  /** Version 1.0. */
  public static final String V10 = "1.0";
  /** Version 1.1. */
  public static final String V11 = "1.1";
  /** Version 4.0. */
  public static final String V40 = "4.0";
  /** Version 4.01. */
  public static final String V401 = "4.01";
  /** Method. */
  public static final String M_XML = "xml";
  /** Method. */
  public static final String M_XHTML = "xhtml";
  /** Method. */
  public static final String M_HTML = "html";
  /** Method. */
  public static final String M_TEXT = "text";

  // SUPPORTED PARAMETERS =====================================================
  
  /** Serialization parameter: yes/no. */
  public static final Object[] S_BYTE_ORDER_MARK = {
    "byte-order-mark", NO };
  /** Serialization parameter: list of QNames. */
  public static final Object[] S_CDATA_SECTION_ELEMENTS = {
    "cdata-section-elements", "" };
  /** Serialization parameter. */
  public static final Object[] S_DOCTYPE_PUBLIC = {
    "doctype-public", "" };
  /** Serialization parameter. */
  public static final Object[] S_DOCTYPE_SYSTEM = {
    "doctype-system", "" };
  /** Serialization parameter: valid encoding. */
  public static final Object[] S_ENCODING = {
    "encoding", Token.UTF8 };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_ESCAPE_URI_ATTRIBUTES = {
    "escape-uri-attributes", YES };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_INCLUDE_CONTENT_TYPE = {
    "include-content-type", YES };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_INDENT = {
    "indent", YES };
  /** Serialization parameter. */
  public static final Object[] S_MEDIA_TYPE = {
    "media-type", "text/xml" };
  /** Serialization parameter: xml/xhtml/html/text. */
  public static final Object[] S_METHOD = {
    "method", "xml" };
  /** Serialization parameter: NFC/NFD/NFKC/NKFD/fully-normalized/none. */
  public static final Object[] S_NORMALIZATION_FORM = {
    "normalization-form", "NFC" };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_OMIT_XML_DECLARATION = {
    "omit-xml-declaration", YES };
  /** Serialization parameter: yes/no/omit. */
  public static final Object[] S_STANDALONE = {
    "standalone", "omit" };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_UNDECLARE_PREFIXES = {
    "undeclare-prefixes", NO };
  /** Serialization parameter. */
  public static final Object[] S_USE_CHARACTER_MAPS = {
    "use-character-maps", "" };
  /** Serialization parameter: 1.0/1.1. */
  public static final Object[] S_VERSION = {
    "version", "" };

  /** Hidden serialization parameter. */
  public static final Object[] S_WRAP_PRE = {
    "wrap-pre", "" };
  /** Hidden serialization parameter. */
  public static final Object[] S_WRAP_URI = {
    "wrap-uri", "" };

  /**
   * Constructor.
   */
  public SerializeProp() {
    super(null);
  }

  /**
   * Constructor, specifying initial properties.
   * @param s property string. Properties are separated with commas,
   * key/values with equality.
   * @throws IOException I/O exception
   */
  public SerializeProp(final String s) throws IOException {
    this();
    if(s == null) return;

    for(final String ser : s.trim().split(",")) {
      if(ser.length() == 0) continue;
      final String[] sprop = ser.split("=", 2);
      final String key = sprop[0].trim();
      final String val = sprop.length < 2 ? "" : sprop[1].trim();
      if(object(key) != null) set(key, val);
      else throw new IOException(Main.info(SERKEY, key));
    }
  }
  
  /**
   * Retrieves a value from the specified property and checks allowed values.
   * @param key property key
   * @param allowed allowed values
   * @return value
   * @throws IOException I/O exception
   */
  public String check(final Object[] key, final String... allowed)
      throws IOException {

    final String val = get(key);
    for(final String a : allowed) if(a.equals(val)) return val;
    throw new IOException(error(key[0].toString(), allowed));
  }
  
  /**
   * Returns an exception string for a wrong key.
   * @param key property key
   * @param allowed allowed values
   * @return string
   */
  public static String error(final String key, final String... allowed) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(SERVAL, key, allowed[0]);
    for(int a = 1; a < allowed.length; a++) tb.add(SERVAL2, allowed[a]);
    return tb.toString();
  }
}
