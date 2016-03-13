package org.basex.io.serial;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.csv.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class defines all available serialization parameters.
 *
 * @author BaseX Team 2005-16, BSD License
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
  public static final EnumOption<YesNo> ESCAPE_URI_ATTRIBUTES =
      new EnumOption<>("escape-uri-attributes", YesNo.NO);
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> INCLUDE_CONTENT_TYPE =
      new EnumOption<>("include-content-type", YesNo.NO);
  /** Serialization parameter: yes/no. */
  public static final EnumOption<YesNo> INDENT =
      new EnumOption<>("indent", YesNo.YES);
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
      new StringOption("normalization-form", Text.NONE);
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

  /** Static WebDAV character map. */
  public static final String WEBDAV = "\u00a0=&#xA0;";

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
   * Parses options.
   * @param name name of option
   * @param value value
   * @param sc static context
   * @param info input info
   * @throws QueryException query exception
   */
  public void parse(final String name, final byte[] value, final StaticContext sc,
      final InputInfo info) throws QueryException {

    try {
      if(name.equals(USE_CHARACTER_MAPS.name()) && !eq(value, token(WEBDAV)))
        throw OUTMAP_X.get(info, value);
      assign(name, string(value));
    } catch(final BaseXException ex) {
      for(final Option<?> o : this) if(o.name().equals(name)) throw SER_X.get(info, ex);
      throw OUTINVALID_X.get(info, ex);
    }

    if(name.equals(PARAMETER_DOCUMENT.name())) {
      Uri uri = Uri.uri(value);
      if(!uri.isValid()) throw INVURI_X.get(info, value);
      if(!uri.isAbsolute()) uri = sc.baseURI().resolve(uri, info);
      final IO io = IO.get(string(uri.string()));
      try {
        // check parameters and add values to serialization parameters
        final ANode root = new DBNode(io).children().next();
        FuncOptions.serializer(root, this, info);

        final HashMap<String, String> free = free();
        if(!free.isEmpty()) throw SEROPTION_X.get(info, free.keySet().iterator().next());

        final byte[] mapsId = QNm.get(USE_CHARACTER_MAPS.name(), QueryText.OUTPUT_URI).id();
        final byte[] mapId = QNm.get("character-map", QueryText.OUTPUT_URI).id();
        if(!get(USE_CHARACTER_MAPS).isEmpty()) {
          final TokenBuilder tb = new TokenBuilder();
          for(final ANode option : XMLAccess.children(root, mapsId)) {
            for(final ANode child : XMLAccess.children(option, mapId)) {
              if(!tb.isEmpty()) tb.add(',');
              tb.add(child.attribute("character")).add('=').add(child.attribute("map-string"));
            }
          }
          set(USE_CHARACTER_MAPS, tb.toString());
        }

      } catch(final IOException ex) {
        Util.debug(ex);
        throw OUTDOC_X.get(info, value);
      }
    }
  }
}
