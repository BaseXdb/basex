package org.basex.query.ann;

import static org.basex.query.QueryText.*;
import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.AtomType.ITEM;

import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Definitions of all built-in XQuery annotations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Annotation {
  /** XQuery annotation. */
  METHOD("method()", params(), XQ_URI, false),
  /** XQuery annotation. */
  PUBLIC("public()", params(), XQ_URI, false),
  /** XQuery annotation. */
  PRIVATE("private()", params(), XQ_URI, false),
  /** XQuery annotation. */
  UPDATING("updating()", params(), XQ_URI, false),

  /** XQuery annotation. */
  _BASEX_LAZY("lazy()", params(), BASEX_URI),
  /** XQuery annotation. */
  _BASEX_INLINE("inline([limit])", params(INTEGER), BASEX_URI),
  /** XQuery annotation. */
  _BASEX_LOCK("lock(key)", params(STRING), BASEX_URI),

  /** XQuery annotation. */
  _INPUT_CSV("csv(options...)", params(STRING), INPUT_URI),
  /** XQuery annotation. */
  _INPUT_HTML("html(options...)", params(STRING), INPUT_URI),
  /** XQuery annotation. */
  _INPUT_JSON("json(options...)", params(STRING), INPUT_URI),

  /** XQuery annotation. */
  _OUTPUT_ALLOW_DUPLICATE_NAMES("allow-duplicate-names(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_BYTE_ORDER_MARK("byte-order-mark(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_CDATA_SECTION_ELEMENTS("cdata-section-elements(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_CSV("csv(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_DOCTYPE_PUBLIC("doctype-public(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_DOCTYPE_SYSTEM("doctype-system(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ENCODING("encoding(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ESCAPE_URI_ATTRIBUTES("escape-uri-attributes(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_HTML_VERSION("html-version(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INCLUDE_CONTENT_TYPE("include-content-type(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INDENT("indent(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INDENTS("indents(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ITEM_SEPARATOR("item-separator(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_JSON("json(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_JSON_NODE_OUTPUT_METHOD("json-node-output-method(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_MEDIA_TYPE("media-type(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_METHOD("method(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_NEWLINE("newline(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_NORMALIZATION_FORM("normalization-form(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_OMIT_XML_DECLARATION("omit-xml-declaration(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_PARAMETER_DOCUMENT("parameter-document(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_STANDALONE("standalone(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_SUPPRESS_INDENTATION("suppress-indentation(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_TABULATOR("tabulator(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_UNDECLARE_PREFIXES("undeclare-prefixes(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_USE_CHARACTER_MAPS("use-character-maps(value)", params(STRING), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_VERSION("version(value)", params(STRING), OUTPUT_URI),

  /** XQuery annotation. */
  _PERM_CHECK("check([path,map])", params(STRING, STRING), PERM_URI),
  /** XQuery annotation. */
  _PERM_ALLOW("allow(names...)", params(STRING), PERM_URI, false),

  /** XQuery annotation. */
  _REST_PATH("path(path)", params(STRING), REST_URI),
  /** XQuery annotation. */
  _REST_ERROR("error(codes...)", params(STRING), REST_URI, false),
  /** XQuery annotation. */
  _REST_CONSUMES("consumes(types...)", params(STRING), REST_URI, false),
  /** XQuery annotation. */
  _REST_PRODUCES("produces(types...)", params(STRING), REST_URI, false),
  /** XQuery annotation. */
  _REST_QUERY_PARAM("query-param(name,variable[,defaults...])",
      params(STRING, STRING, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_FORM_PARAM("form-param(name,variable[,defaults...])",
      params(STRING, STRING, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_HEADER_PARAM("header-param(name,variable[,defaults...])",
      params(STRING, STRING, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_COOKIE_PARAM("cookie-param(name,variable[,defaults...])",
      params(STRING, STRING, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_ERROR_PARAM("error-param(name,variable[,defaults...])",
      params(STRING, STRING, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_METHOD("method(name[,body])", params(STRING, STRING), REST_URI, false),
  /** XQuery annotation. */
  _REST_SINGLE("single([key])", params(STRING), REST_URI),
  /** XQuery annotation. */
  _REST_GET("GET()", params(), REST_URI),
  /** XQuery annotation. */
  _REST_POST("POST([body])", params(STRING), REST_URI),
  /** XQuery annotation. */
  _REST_PUT("PUT([body])", params(STRING), REST_URI),
  /** XQuery annotation. */
  _REST_DELETE("DELETE()", params(), REST_URI),
  /** XQuery annotation. */
  _REST_HEAD("HEAD()", params(), REST_URI),
  /** XQuery annotation. */
  _REST_OPTIONS("OPTIONS()", params(), REST_URI),
  /** XQuery annotation. */
  _REST_PATCH("PATCH([body])", params(STRING), REST_URI),

  /** XQuery annotation. */
  _UNIT_AFTER("after([function])", params(STRING), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_AFTER_MODULE("after-module()", params(), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_BEFORE("before([function])", params(STRING), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_BEFORE_MODULE("before-module()", params(), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_IGNORE("ignore([message])", params(STRING), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_TEST("test(['expected',error])", params(STRING, STRING), UNIT_URI),

  /** XQuery annotation. */
  _WS_CONNECT("connect(path)", params(STRING), WS_URI),
  /** XQuery annotation. */
  _WS_MESSAGE("message(path,message)", params(STRING, STRING), WS_URI),
  /** XQuery annotation. */
  _WS_CLOSE("close(path)", params(STRING), WS_URI),
  /** XQuery annotation. */
  _WS_HEADER_PARAM("header-param(name,variable[,defaults...])",
      params(STRING, STRING, ITEM), WS_URI, false),
  /** XQuery annotation. */
  _WS_ERROR("error(path,message)", params(STRING, STRING), WS_URI);

  /** Parameter types. */
  public final AtomType[] params;
  /** Name of function. */
  public final QNm name;
  /** Minimum and maximum number of arguments. */
  public final int[] minMax;
  /** Annotation must only occur once. */
  public final boolean single;
  /** Descriptive parameter string. */
  private final String paramString;

  /** Cached enums (faster). */
  public static final Annotation[] VALUES = values();

  /** Maps with QName and signature pairs. */
  private static final QNmMap<Annotation> MAP = new QNmMap<>();

  static {
    for(final Annotation value : VALUES) {
      MAP.put(value.name, value);
    }
  }

  /**
   * Constructor.
   * @param desc descriptive function string, containing the function name and its arguments
   * @param params parameter types
   * @param uri URI
   */
  Annotation(final String desc, final AtomType[] params, final byte[] uri) {
    this(desc, params, uri, true);
  }

  /**
   * Constructor.
   * @param string descriptive function string, containing the function name and its arguments
   * @param params parameter types
   * @param uri URI
   * @param single annotation must only occur once
   */
  Annotation(final String string, final AtomType[] params, final byte[] uri, final boolean single) {
    this.params = params;
    this.single = single;

    final int s = string.indexOf('(');
    name = new QNm(NSGlobal.prefix(uri), Token.token(string.substring(0, s)), uri);
    paramString = string.substring(s + 1).replace(")", "");
    minMax = FuncDefinition.minMax(paramString);
  }

  /**
   * Returns an annotation with the specified name.
   * @param name name
   * @return annotation or {@code null}
   */
  public static Annotation get(final QNm name) {
    return MAP.get(name);
  }

  /**
   * Returns an array representation of the specified sequence types.
   * @param params parameter types
   * @return array
   */
  private static AtomType[] params(final AtomType... params) {
    return params;
  }

  @Override
  public String toString() {
    // chop parentheses if annotation has no parameters
    return Strings.concat(name.string(), paramString.isEmpty() ? "" : ('(' + paramString + ')'));
  }
}
