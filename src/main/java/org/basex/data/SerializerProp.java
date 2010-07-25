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
public final class SerializerProp extends AProp {
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
    "escape-uri-attributes", NO };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_INCLUDE_CONTENT_TYPE = {
    "include-content-type", NO };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_INDENT = {
    "indent", YES };
  /** Serialization parameter. */
  public static final Object[] S_MEDIA_TYPE = {
    "media-type", "text/html" };
  /** Serialization parameter: xml/xhtml/html/text. */
  public static final Object[] S_METHOD = {
    "method", M_XML };
  /** Serialization parameter: NFC/NFD/NFKC/NKFD/fully-normalized/none. */
  public static final Object[] S_NORMALIZATION_FORM = {
    "normalization-form", NFC };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_OMIT_XML_DECLARATION = {
    "omit-xml-declaration", YES };
  /** Serialization parameter: yes/no/omit. */
  public static final Object[] S_STANDALONE = {
    "standalone", OMIT };
  /** Serialization parameter: yes/no. */
  public static final Object[] S_UNDECLARE_PREFIXES = {
    "undeclare-prefixes", NO };
  /** Serialization parameter. */
  public static final Object[] S_USE_CHARACTER_MAPS = {
    "use-character-maps", "" };
  /** Serialization parameter: 1.0/1.1. */
  public static final Object[] S_VERSION = {
    "version", "" };

  /** Specific serialization parameter: number of spaces to indent. */
  public static final Object[] S_INDENT_SPACES = {
    "indent-spaces", "2" };
  /** Specific serialization parameter: prefix of result wrapper. */
  public static final Object[] S_WRAP_PRE = {
    "wrap-pre", "" };
  /** Specific serialization parameter: URI of result wrapper. */
  public static final Object[] S_WRAP_URI = {
    "wrap-uri", "" };

  /**
   * Constructor.
   */
  public SerializerProp() {
    super(null);
  }

  /**
   * Constructor, specifying initial properties.
   * @param s property string. Properties are separated with commas,
   * key/values with equality
   * @throws IOException I/O exception
   */
  public SerializerProp(final String s) throws IOException {
    this();
    if(s == null) return;

    for(final String ser : s.trim().split(",")) {
      if(ser.isEmpty()) continue;
      final String[] sprop = ser.split("=", 2);
      final String key = sprop[0].trim();
      final String val = sprop.length < 2 ? "" : sprop[1].trim();
      if(get(key) != null) set(key, val);
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
