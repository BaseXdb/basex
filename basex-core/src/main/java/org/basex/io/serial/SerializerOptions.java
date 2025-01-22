package org.basex.io.serial;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.function.*;

import org.basex.build.csv.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * This class defines all available serialization parameters.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SerializerOptions extends Options {
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> BYTE_ORDER_MARK =
      new EnumOption<>("byte-order-mark", YesNo.NO);
  /** Serialization parameter: list of QNames. */
  public static final StringOption CDATA_SECTION_ELEMENTS =
      new StringOption("cdata-section-elements", "");
  /** Serialization parameter. */
  public static final StringOption DOCTYPE_PUBLIC =
      new StringOption("doctype-public", "");
  /** Serialization parameter. */
  public static final StringOption DOCTYPE_SYSTEM =
      new StringOption("doctype-system", "");
  /** Serialization parameter: valid encoding. */
  public static final StringOption ENCODING =
      new StringOption("encoding", Strings.UTF8);
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> ESCAPE_SOLIDUS =
      new EnumOption<>("escape-solidus", YesNo.YES);
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> ESCAPE_URI_ATTRIBUTES =
      new EnumOption<>("escape-uri-attributes", YesNo.NO);
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> INCLUDE_CONTENT_TYPE =
      new EnumOption<>("include-content-type", YesNo.YES);
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> INDENT =
      new EnumOption<>("indent", YesNo.NO);
  /** Serialization parameter. */
  public static final StringOption SUPPRESS_INDENTATION =
      new StringOption("suppress-indentation", "");
  /** Serialization parameter. */
  public static final StringOption MEDIA_TYPE =
      new StringOption("media-type", "");
  /** Serialization parameter: xml/xhtml/html/text/json/csv/raw/adaptive. */
  public static final EnumOption<SerialMethod> METHOD =
      new EnumOption<>("method", SerialMethod.BASEX);
  /** Serialization parameter: NFC/NFD/NFKC/NKFD/fully-normalized/none. */
  public static final StringOption NORMALIZATION_FORM =
      new StringOption("normalization-form", "none");
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> OMIT_XML_DECLARATION =
      new EnumOption<>("omit-xml-declaration", YesNo.YES);
  /** Serialization parameter: yes/no/omit. */
  public static final EnumOption<YesNoOmit> STANDALONE =
      new EnumOption<>("standalone", YesNoOmit.OMIT);
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> UNDECLARE_PREFIXES =
      new EnumOption<>("undeclare-prefixes", YesNo.NO);
  /** Serialization parameter. */
  public static final StringOption USE_CHARACTER_MAPS =
      new StringOption("use-character-maps", "");
  /** Serialization parameter. */
  public static final StringOption ITEM_SEPARATOR =
      new StringOption("item-separator");
  /** Serialization parameter: 1.0/1.1. */
  public static final StringOption VERSION =
      new StringOption("version", "");
  /** Serialization parameter: 4.0/4.01/5.0. */
  public static final StringOption HTML_VERSION =
      new StringOption("html-version", "");
  /** Parameter document. */
  public static final StringOption PARAMETER_DOCUMENT =
      new StringOption("parameter-document", "");
  /** Serialization parameter: xml/xhtml/html/text. */
  public static final EnumOption<YesNo> ALLOW_DUPLICATE_NAMES =
      new EnumOption<>("allow-duplicate-names", YesNo.NO);
  /** Serialization parameter: xml/xhtml/html/text. */
  public static final EnumOption<SerialMethod> JSON_NODE_OUTPUT_METHOD =
      new EnumOption<>("json-node-output-method", SerialMethod.XML);
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> JSON_LINES =
      new EnumOption<>("json-lines", YesNo.NO);

  /** Specific serialization parameter. */
  public static final OptionsOption<CsvOptions> CSV =
      new OptionsOption<>("csv", new CsvOptions());
  /** Specific serialization parameter. */
  public static final OptionsOption<JsonSerialOptions> JSON =
      new OptionsOption<>("json", new JsonSerialOptions());
  /** Specific serialization parameter: newline. */
  public static final EnumOption<Newline> NEWLINE =
      new EnumOption<>("newline",
        "\r".equals(Prop.NL) ? Newline.CR : "\n".equals(Prop.NL) ? Newline.NL : Newline.CRNL);
  /** Specific serialization parameter: indent with spaces or tabs. */
  public static final EnumOption<YesNo> TABULATOR =
      new EnumOption<>("tabulator", YesNo.NO);
  /** Specific serialization parameter: number of spaces to indent. */
  public static final NumberOption INDENTS =
      new NumberOption("indents", 2);
  /** Specific serialization parameter: maximum number of bytes to serialize. */
  public static final NumberOption LIMIT =
      new NumberOption("limit", -1);
  /** Specific serialization parameter: binary serialization. */
  public static final EnumOption<YesNo> BINARY =
      new EnumOption<>("binary", YesNo.YES);
  /** Specific serialization parameter: attribute indentation. */
  public static final EnumOption<YesNo> INDENT_ATTRIBUTES =
      new EnumOption<>("indent-attributes", YesNo.NO);

  /** QName. */
  public static final QNm Q_ROOT =
      new QNm(OUTPUT_PREFIX, "serialization-parameters", OUTPUT_URI);
  /** Name test. */
  public static final NameTest T_ROOT = new NameTest(Q_ROOT);
  /** Value. */
  private static final QNm VALUE = new QNm("value");

  /** Newlines. */
  public enum Newline {
    /** NL.   */ NL("\\n", "\n"),
    /** CR.   */ CR("\\r", "\r"),
    /** CRNL. */ CRNL("\\r\\n", "\r\n");

    /** Name. */
    private final String name;
    /** Newline. */
    private final String newline;

    /**
     * Constructor.
     * @param name name
     * @param newline newline string
     */
    Newline(final String name, final String newline) {
      this.name = name;
      this.newline = newline;
    }

    /**
     * Returns the newline string.
     * @return newline string
     */
    String newline() {
      return newline;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Checks if the specified option is true.
   * @param option option
   * @return value
   */
  public boolean yes(final EnumOption<YesNo> option) {
    return get(option) == YesNo.YES;
  }

  /**
   * Default constructor.
   */
  public SerializerOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public SerializerOptions(final SerializerOptions opts) {
    super(opts);
  }

  /**
   * Converts the specified output parameter item to serializer options.
   * @param item node with serialization parameters
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public void assign(final Item item, final InputInfo info) throws QueryException {
    if(!T_ROOT.matches(item)) throw ELMMAP_X_X_X.get(info, Q_ROOT.prefixId(XML), item.type, item);
    try {
      assign(toString((ANode) item, new QNmSet(), info));
    } catch(final BaseXException ex) {
      throw SERDOC_X.get(info, ex);
    }
  }

  /**
   * Parses options.
   * @param name name of option
   * @param value value
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public void parse(final String name, final String value, final InputInfo info)
      throws QueryException {

    final Option<?> option = option(name);
    if(option == PARAMETER_DOCUMENT) {
      // parse parameters and character map
      Uri uri = Uri.get(value);
      if(!uri.isValid()) throw INVURI_X.get(info, value);
      if(!uri.isAbsolute()) uri = info.sc().baseURI().resolve(uri, info);
      final IO io = IO.get(string(uri.string()));
      final ANode root;
      try {
        root = new DBNode(io).childIter().next();
      } catch(final IOException ex) {
        throw PARAMDOC_X.get(info, ex);
      }
      if(root != null) assign(root, info);

      for(final ANode child : root.childIter()) {
        if(child.type != NodeType.ELEMENT) continue;
        if(option(string(child.qname().local())) == USE_CHARACTER_MAPS) {
          set(USE_CHARACTER_MAPS, characterMap(child, info));
        }
      }
    } else {
      // parse remaining parameters
      try {
        assign(name, value);
      } catch(final BaseXException ex) {
        throw (option != null ? SERPARAM_X : OUTPUT_X).get(info, ex);
      }
    }
  }

  /**
   * Extracts a character map.
   * @param elem child node
   * @param info input info
   * @return character map or {@code null} if map is invalid
   * @throws QueryException query exception
   */
  public static String characterMap(final ANode elem, final InputInfo info) throws QueryException {
    final Supplier<QueryException> error = () -> SERDOC_X.get(info,
        Util.info("Serialization parameter '%' is invalid.", USE_CHARACTER_MAPS.name()));
    if(elem.attributeIter().next() != null) throw error.get();

    final TokenMap map = new TokenMap();
    for(final ANode child : elem.childIter()) {
      if(child.type != NodeType.ELEMENT) continue;
      final byte[] name = child.qname().local();
      if(eq(name, CHARACTER_MAP)) {
        byte[] key = null, val = null;
        for(final ANode attr : child.attributeIter()) {
          final byte[] att = attr.name();
          if(eq(att, CHARACTER)) key = attr.string();
          else if(eq(att, MAP_STRING)) val = attr.string();
          else throw error.get();
        }
        if(key == null || val == null) throw error.get();
        if(map.get(key) != null) throw SERCHARDUP_X.get(info, key);
        if(length(key) != 1) throw SERDOC_X.get(info,
            Util.info("Key in character map is not a single character: %.", key));
        map.put(key, val);
      } else {
        throw error.get();
      }
    }

    // return string representation
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] key : map) {
      if(!tb.isEmpty()) tb.add(',');
      tb.add(key).add('=').add(string(map.get(key)).replace(",", ",,"));
    }
    return tb.toString();
  }

  /**
   * Builds a string representation of the specified node.
   * @param node node
   * @param cache name cache
   * @param info input info
   * @return string
   * @throws QueryException query exception
   */
  private String toString(final ANode node, final QNmSet cache, final InputInfo info)
      throws QueryException {

    final ANode att = node.attributeIter().next();
    if(att != null) throw SERDOC_X.get(info, Util.info("Invalid attribute: '%'", att.name()));

    final StringBuilder sb = new StringBuilder();
    // interpret options
    for(final ANode child : node.childIter()) {
      if(child.type != NodeType.ELEMENT) continue;

      // ignore elements in other namespace
      final QNm qname = child.qname();
      if(!cache.add(qname)) throw SERDUP_X.get(info, qname);

      if(!eq(qname.uri(), Q_ROOT.uri())) {
        if(qname.uri().length != 0) continue;
        throw SERDOC_X.get(info, Util.info("Element has no namespace: '%'", qname));
      }
      // retrieve key from element name and value from "value" attribute or text node
      final String name = string(qname.local());
      final Option<?> option = option(name);
      String value = null;
      if(option == USE_CHARACTER_MAPS) {
        value = characterMap(child, info);
      } else if(hasElements(child)) {
        value = toString(child, cache, info);
      } else {
        for(final ANode attr : child.attributeIter()) {
          if(attr.qname().eq(VALUE)) {
            value = string(attr.string());
            if(option == CDATA_SECTION_ELEMENTS) value = cdataSectionElements(child, value);
          } else {
            throw SERDOC_X.get(info, Util.info("Invalid attribute: '%'", attr.name()));
          }
        }
        if(value == null) throw SERDOC_X.get(info, "Missing 'value' attribute.");
      }
      sb.append(name).append('=').append(value.trim().replace(",", ",,")).append(',');
    }
    return sb.toString();
  }

  /**
   * Converts QNames with prefixes to the EQName notation.
   * @param elem root element
   * @param value value
   * @return name with resolved QNames
   */
  private static String cdataSectionElements(final ANode elem, final String value) {
    if(!Strings.contains(value, ':')) return value;

    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] name : distinctTokens(token(value))) {
      byte[] qname = name;
      final int i = indexOf(name, ':');
      if(i != -1) {
        final byte[] uri = elem.nsScope(null).value(substring(name, 0, i));
        if(uri != null) qname = QNm.eqName(uri, substring(name, i + 1));
      }
      tb.add(qname).add(' ');
    }
    return tb.toString();
  }

  /**
   * Checks if the specified node has elements as children.
   * @param node node
   * @return result of check
   */
  private static boolean hasElements(final ANode node) {
    for(final ANode nd : node.childIter()) {
      if(nd.type == NodeType.ELEMENT) return true;
    }
    return false;
  }
}
