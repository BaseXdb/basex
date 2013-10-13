package org.basex.io.serial;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class defines all available serialization parameters.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class SerializerOptions extends Options {
  /** Undefined flag. */
  static final String UNDEFINED = "\u0001";

  /** Serialization parameter: yes/no. */
  public static final StringOption S_BYTE_ORDER_MARK = new StringOption("byte-order-mark", NO);
  /** Serialization parameter: list of QNames. */
  public static final StringOption S_CDATA_SECTION_ELEMENTS =
      new StringOption("cdata-section-elements", "");
  /** Serialization parameter. */
  public static final StringOption S_DOCTYPE_PUBLIC = new StringOption("doctype-public", "");
  /** Serialization parameter. */
  public static final StringOption S_DOCTYPE_SYSTEM = new StringOption("doctype-system", "");
  /** Serialization parameter: valid encoding. */
  public static final StringOption S_ENCODING = new StringOption("encoding", Token.UTF8);
  /** Serialization parameter: yes/no. */
  public static final StringOption S_ESCAPE_URI_ATTRIBUTES =
      new StringOption("escape-uri-attributes", NO);
  /** Serialization parameter: yes/no. */
  public static final StringOption S_INCLUDE_CONTENT_TYPE =
      new StringOption("include-content-type", NO);
  /** Serialization parameter: yes/no. */
  public static final StringOption S_INDENT = new StringOption("indent", YES);
  /** Serialization parameter. */
  public static final StringOption S_SUPPRESS_INDENTATION =
      new StringOption("suppress-indentation", "");
  /** Serialization parameter. */
  public static final StringOption S_MEDIA_TYPE = new StringOption("media-type", "");
  /** Serialization parameter: xml/xhtml/html/text. */
  public static final StringOption S_METHOD = new StringOption("method", M_XML);
  /** Serialization parameter: NFC/NFD/NFKC/NKFD/fully-normalized/none. */
  public static final StringOption S_NORMALIZATION_FORM =
      new StringOption("normalization-form", NFC);
  /** Serialization parameter: yes/no. */
  public static final StringOption S_OMIT_XML_DECLARATION =
      new StringOption("omit-xml-declaration", YES);
  /** Serialization parameter: yes/no/omit. */
  public static final StringOption S_STANDALONE = new StringOption("standalone", OMIT);
  /** Serialization parameter: yes/no. */
  public static final StringOption S_UNDECLARE_PREFIXES =
      new StringOption("undeclare-prefixes", NO);
  /** Serialization parameter. */
  public static final StringOption S_USE_CHARACTER_MAPS =
      new StringOption("use-character-maps", "");
  /** Serialization parameter. */
  public static final StringOption S_ITEM_SEPARATOR =
      new StringOption("item-separator", UNDEFINED);
  /** Serialization parameter: 1.0/1.1. */
  public static final StringOption S_VERSION = new StringOption("version", "");
  /** Serialization parameter: 4.0/4.01/5.0. */
  public static final StringOption S_HTML_VERSION = new StringOption("html-version", "");
  /** Parameter document. */
  public static final StringOption S_PARAMETER_DOCUMENT =
      new StringOption("parameter-document", "");

  /** Specific serialization parameter: newline. */
  public static final StringOption S_NEWLINE = new StringOption(
    "newline", Prop.NL.equals("\r") ? S_CR : Prop.NL.equals("\n") ? S_NL : S_CRNL);
  /** Specific serialization parameter: formatting. */
  public static final StringOption S_FORMAT = new StringOption("format", YES);
  /** Specific serialization parameter: indent with spaces or tabs. */
  public static final StringOption S_TABULATOR = new StringOption("tabulator", NO);
  /** Specific serialization parameter: number of spaces to indent. */
  public static final StringOption S_INDENTS = new StringOption("indents", "2");
  /** Specific serialization parameter: item separator. */
  public static final StringOption S_SEPARATOR = new StringOption("separator", UNDEFINED);
  /** Specific serialization parameter: prefix of result wrapper. */
  public static final StringOption S_WRAP_PREFIX = new StringOption("wrap-prefix", "");
  /** Specific serialization parameter: URI of result wrapper. */
  public static final StringOption S_WRAP_URI = new StringOption("wrap-uri", "");

  /** Specific serialization parameter. */
  public static final StringOption S_CSV = new StringOption("csv", "");
  /** Specific serialization parameter. */
  public static final StringOption S_JSON = new StringOption("json", "");

  /**
   * Constructor.
   */
  public SerializerOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string. Options are separated with commas ({@code ,}),
   * key/values with the equality character ({@code =}).
   * @throws BaseXException database exception
   */
  public SerializerOptions(final String opts) throws BaseXException {
    super(opts);
  }

  /**
   * Retrieves a value from the specified option and checks allowed values.
   * @param option option
   * @param allowed allowed values
   * @return value
   * @throws QueryIOException query I/O exception
   */
  public String check(final StringOption option, final String... allowed)
      throws QueryIOException {

    final String val = get(option);
    for(final String a : allowed) if(a.equals(val)) return val;
    throw error(option.name(), val, allowed);
  }

  /**
   * Retrieves a value from the specified option and checks for supported values.
   * @param option option
   * @param allowed allowed values
   * @return value
   * @throws QueryIOException query I/O exception
   */
  public String supported(final StringOption option, final String... allowed)
      throws QueryIOException {

    final String val = get(option);
    if(val.isEmpty()) return allowed.length > 0 ? allowed[0] : val;
    for(final String a : allowed) if(a.equals(val)) return val;
    throw SERNOTSUPP.thrwIO(allowed(option.name(), val, allowed));
  }

  /**
   * Retrieves a value from the specified option and checks for its boolean value.
   * @param option option
   * @return value
   * @throws QueryIOException query I/O exception
   */
  public boolean yes(final StringOption option) throws QueryIOException {
    return yes(option.name(), get(option));
  }

  /**
   * Converts the specified value to a boolean or throws an exception if value is unknown.
   * @param key key
   * @param value value
   * @return result of check
   * @throws QueryIOException query I/O exception
   */
  public static boolean yes(final String key, final String value) throws QueryIOException {
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
   * @throws QueryIOException query I/O exception
   */
  public static QueryIOException error(final String name, final String found,
      final String... allowed) throws QueryIOException {
    throw SEROPT.thrwIO(allowed(name, found, allowed));
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
