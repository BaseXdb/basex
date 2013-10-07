package org.basex.io.serial;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * This class defines all available serialization parameters.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SerializerOptions extends Options {
  /** Undefined flag. */
  static final String UNDEFINED = "\u0001";

  /** Serialization parameter: yes/no. */
  public static final Option S_BYTE_ORDER_MARK = new Option("byte-order-mark", NO);
  /** Serialization parameter: list of QNames. */
  public static final Option S_CDATA_SECTION_ELEMENTS =
      new Option("cdata-section-elements", "");
  /** Serialization parameter. */
  public static final Option S_DOCTYPE_PUBLIC = new Option("doctype-public", "");
  /** Serialization parameter. */
  public static final Option S_DOCTYPE_SYSTEM = new Option("doctype-system", "");
  /** Serialization parameter: valid encoding. */
  public static final Option S_ENCODING = new Option("encoding", Token.UTF8);
  /** Serialization parameter: yes/no. */
  public static final Option S_ESCAPE_URI_ATTRIBUTES = new Option("escape-uri-attributes", NO);
  /** Serialization parameter: yes/no. */
  public static final Option S_INCLUDE_CONTENT_TYPE = new Option("include-content-type", NO);
  /** Serialization parameter: yes/no. */
  public static final Option S_INDENT = new Option("indent", YES);
  /** Serialization parameter. */
  public static final Option S_SUPPRESS_INDENTATION = new Option("suppress-indentation", "");
  /** Serialization parameter. */
  public static final Option S_MEDIA_TYPE = new Option("media-type", "");
  /** Serialization parameter: xml/xhtml/html/text. */
  public static final Option S_METHOD = new Option("method", M_XML);
  /** Serialization parameter: NFC/NFD/NFKC/NKFD/fully-normalized/none. */
  public static final Option S_NORMALIZATION_FORM = new Option("normalization-form", NFC);
  /** Serialization parameter: yes/no. */
  public static final Option S_OMIT_XML_DECLARATION = new Option("omit-xml-declaration", YES);
  /** Serialization parameter: yes/no/omit. */
  public static final Option S_STANDALONE = new Option("standalone", OMIT);
  /** Serialization parameter: yes/no. */
  public static final Option S_UNDECLARE_PREFIXES = new Option("undeclare-prefixes", NO);
  /** Serialization parameter. */
  public static final Option S_USE_CHARACTER_MAPS = new Option("use-character-maps", "");
  /** Serialization parameter. */
  public static final Option S_ITEM_SEPARATOR = new Option("item-separator", UNDEFINED);
  /** Serialization parameter: 1.0/1.1. */
  public static final Option S_VERSION = new Option("version", "");
  /** Serialization parameter: 4.0/4.01/5.0. */
  public static final Option S_HTML_VERSION = new Option("html-version", "");
  /** Parameter document. */
  public static final Option S_PARAMETER_DOCUMENT = new Option("parameter-document", "");

  /** Specific serialization parameter: newline. */
  public static final Option S_NEWLINE = new Option(
    "newline", Prop.NL.equals("\r") ? S_CR : Prop.NL.equals("\n") ? S_NL : S_CRNL);
  /** Specific serialization parameter: formatting. */
  public static final Option S_FORMAT = new Option("format", YES);
  /** Specific serialization parameter: indent with spaces or tabs. */
  public static final Option S_TABULATOR = new Option("tabulator", NO);
  /** Specific serialization parameter: number of spaces to indent. */
  public static final Option S_INDENTS = new Option("indents", "2");
  /** Specific serialization parameter: item separator. */
  public static final Option S_SEPARATOR = new Option("separator", UNDEFINED);
  /** Specific serialization parameter: prefix of result wrapper. */
  public static final Option S_WRAP_PREFIX = new Option("wrap-prefix", "");
  /** Specific serialization parameter: URI of result wrapper. */
  public static final Option S_WRAP_URI = new Option("wrap-uri", "");

  /** Specific serialization parameter. */
  public static final Option S_CSV = new Option("csv", "");
  /** Specific serialization parameter. */
  public static final Option S_JSON = new Option("json", "");

  /**
   * Constructor.
   */
  public SerializerOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param string options string. Options are separated with commas ({@code ,}),
   * key/values with the equality character ({@code =}).
   */
  public SerializerOptions(final String string) {
    parse(string);
  }

  /**
   * Retrieves a value from the specified option and checks allowed values.
   * @param option option
   * @param allowed allowed values
   * @return value
   * @throws SerializerException serializer exception
   */
  public String check(final Option option, final String... allowed) throws SerializerException {
    final String val = string(option);
    for(final String a : allowed) if(a.equals(val)) return val;
    throw error(option.name, val, allowed);
  }

  /**
   * Retrieves a value from the specified option and checks for supported values.
   * @param option option
   * @param allowed allowed values
   * @return value
   * @throws SerializerException serializer exception
   */
  public String supported(final Option option, final String... allowed)
      throws SerializerException {

    final String val = string(option);
    if(val.isEmpty()) return allowed.length > 0 ? allowed[0] : val;
    for(final String a : allowed) if(a.equals(val)) return val;
    throw SERNOTSUPP.thrwSerial(allowed(option.name, val, allowed));
  }

  /**
   * Retrieves a value from the specified option and checks for its boolean value.
   * @param option option
   * @return value
   * @throws SerializerException serializer exception
   */
  public boolean yes(final Option option) throws SerializerException {
    return yes(option.name, string(option));
  }

  /**
   * Converts the specified value to a boolean or throws an exception if value is unknown.
   * @param key key
   * @param value value
   * @return result of check
   * @throws SerializerException serializer exception
   */
  public static boolean yes(final String key, final String value) throws SerializerException {
    if(Util.yes(value)) return true;
    if(Util.no(value)) return false;
    throw error(key, value, YES, NO);
  }

  /**
   * Returns an exception string for a wrong key.
   * @param name name of option
   * @param found found value
   * @param allowed allowed values
   * @return exception
   * @throws SerializerException serializer exception
   */
  public static SerializerException error(final String name, final String found,
      final String... allowed) throws SerializerException {
    throw SEROPT.thrwSerial(allowed(name, found, allowed));
  }

  /**
   * Returns a list of allowed keys.
   * @param name name of option
   * @param found found value
   * @param all allowed values
   * @return exception
   */
  private static String allowed(final String name, final String found, final String... all) {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(SERVAL, name, all[0]);
    for(int a = 1; a < all.length; ++a) tb.addExt(SERVAL2, all[a]);
    tb.addExt(SERVAL3, found);
    return tb.toString();
  }
}
