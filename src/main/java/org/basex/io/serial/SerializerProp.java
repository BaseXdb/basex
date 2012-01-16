package org.basex.io.serial;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;

import java.util.Map.Entry;

import org.basex.core.AProp;
import org.basex.core.Prop;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class contains serialization properties.
 *
 * @author BaseX Team 2005-12, BSD License
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
    "media-type", "" };
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

  /** Specific serialization parameter: newline. */
  public static final Object[] S_NEWLINE = { "newline",
    Prop.NL.equals("\r") ? S_CR : Prop.NL.equals("\n") ? S_NL  : S_CRNL };
  /** Specific serialization parameter: formatting. */
  public static final Object[] S_FORMAT = {
    "format", YES };
  /** Specific serialization parameter: indent with spaces or tabs. */
  public static final Object[] S_TABULATOR = {
    "tabulator", NO };
  /** Specific serialization parameter: number of spaces to indent. */
  public static final Object[] S_INDENTS = {
    "indents", "2" };
  /** Specific serialization parameter: prefix of result wrapper. */
  public static final Object[] S_WRAP_PREFIX = {
    "wrap-prefix", "" };
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
   * @param s property string. Properties are separated with commas ({@code ,}),
   * key/values with the equality character ({@code =}).
   * @throws SerializerException serializer exception
   */
  public SerializerProp(final String s) throws SerializerException {
    this();
    if(s == null) return;

    for(final String ser : s.trim().split(",")) {
      if(ser.isEmpty()) continue;
      final String[] sprop = ser.split("=", 2);
      final String key = sprop[0].trim();
      final String val = sprop.length < 2 ? "" : sprop[1].trim();
      if(get(key) == null) SERINVALID.thrwSerial(key);
      set(key, val);
    }
  }

  /**
   * Retrieves a value from the specified property and checks allowed values.
   * @param key property key
   * @param allowed allowed values
   * @return value
   * @throws SerializerException serializer exception
   */
  public String check(final Object[] key, final String... allowed)
      throws SerializerException {

    final String val = get(key);
    for(final String a : allowed) if(a.equals(val)) return val;
    throw error(key[0], val, allowed);
  }

  /**
   * Retrieves a value from the specified property and checks for its boolean
   * value.
   * @param key property key
   * @return value
   * @throws SerializerException serializer exception
   */
  public boolean yes(final Object[] key) throws SerializerException {
    final String val = get(key);
    for(final String a : new String[] { YES, TRUE, ON }) {
      if(a.equals(val)) return true;
    }
    for(final String a : new String[] { NO, FALSE, OFF }) {
      if(a.equals(val)) return false;
    }
    throw error(key[0], val, YES, NO);
  }

  /**
   * Returns an exception string for a wrong key.
   * @param key property key
   * @param found found value
   * @param allowed allowed values
   * @return exception
   * @throws SerializerException serializer exception
   */
  public static SerializerException error(final Object key, final String found,
      final String... allowed) throws SerializerException {

    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(SERVAL, key, allowed[0]);
    for(int a = 1; a < allowed.length; ++a) tb.addExt(SERVAL2, allowed[a]);
    tb.addExt(SERVAL3, found);
    throw SERANY.thrwSerial(tb);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    for(final Entry<String, Object> e : props.entrySet()) {
      if(tb.size() != 0) tb.add(',');
      tb.add(e.getKey()).add('=').addExt(e.getValue());
    }
    return tb.toString();
  }
}
