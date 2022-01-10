package org.basex.query.ann;

import static org.basex.query.QueryText.*;
import static org.basex.query.value.type.SeqType.*;
import static org.basex.util.Token.*;

import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Definitions of all built-in XQuery annotations.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public enum Annotation {
  /** XQuery annotation. */
  PUBLIC("public()", params(), XQ_URI, false),
  /** XQuery annotation. */
  PRIVATE("private()", params(), XQ_URI, false),
  /** XQuery annotation. */
  UPDATING("updating()", params(), XQ_URI, false),

  /** XQuery annotation. */
  _BASEX_LAZY("lazy()", params(), BASEX_URI),
  /** XQuery annotation. */
  _BASEX_INLINE("inline([limit])", params(INTEGER_O), BASEX_URI),
  /** XQuery annotation. */
  _BASEX_LOCK("lock(key)", params(STRING_O), BASEX_URI),

  /** XQuery annotation. */
  _INPUT_CSV("csv(option[,...])", params(STRING_O), INPUT_URI),
  /** XQuery annotation. */
  _INPUT_HTML("html(option[,...])", params(STRING_O), INPUT_URI),
  /** XQuery annotation. */
  _INPUT_JSON("json(option[,...])", params(STRING_O), INPUT_URI),
  /** XQuery annotation. */
  _INPUT_TEXT("text(option[,...])", params(STRING_O), INPUT_URI),

  /** XQuery annotation. */
  _OUTPUT_ALLOW_DUPLICATE_NAMES("allow-duplicate-names(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_BYTE_ORDER_MARK("byte-order-mark(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_CDATA_SECTION_ELEMENTS("cdata-section-elements(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_CSV("csv(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_DOCTYPE_PUBLIC("doctype-public(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_DOCTYPE_SYSTEM("doctype-system(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ENCODING("encoding(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ESCAPE_URI_ATTRIBUTES("escape-uri-attributes(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_HTML_VERSION("html-version(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INCLUDE_CONTENT_TYPE("include-content-type(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INDENT("indent(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INDENTS("indents(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ITEM_SEPARATOR("item-separator(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_JSON("json(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_JSON_NODE_OUTPUT_METHOD("json-node-output-method(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_MEDIA_TYPE("media-type(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_METHOD("method(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_NEWLINE("newline(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_NORMALIZATION_FORM("normalization-form(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_OMIT_XML_DECLARATION("omit-xml-declaration(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_PARAMETER_DOCUMENT("parameter-document(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_STANDALONE("standalone(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_SUPPRESS_INDENTATION("suppress-indentation(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_TABULATOR("tabulator(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_UNDECLARE_PREFIXES("undeclare-prefixes(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_USE_CHARACTER_MAPS("use-character-maps(value)", params(STRING_O), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_VERSION("version(value)", params(STRING_O), OUTPUT_URI),

  /** XQuery annotation. */
  _PERM_CHECK("check([path[,map]])", params(STRING_O, STRING_O), PERM_URI),
  /** XQuery annotation. */
  _PERM_ALLOW("allow(name[,...])", params(STRING_O), PERM_URI, false),

  /** XQuery annotation. */
  _REST_PATH("path(path)", params(STRING_O), REST_URI),
  /** XQuery annotation. */
  _REST_ERROR("error(code[,...])", params(STRING_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_CONSUMES("consumes(type[,...])", params(STRING_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_PRODUCES("produces(type[,...])", params(STRING_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_QUERY_PARAM("query-param(name,variable[,default,...])",
      params(STRING_O, STRING_O, ITEM_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_FORM_PARAM("form-param(name,variable[,default,...])",
      params(STRING_O, STRING_O, ITEM_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_HEADER_PARAM("header-param(name,variable[,default,...])",
      params(STRING_O, STRING_O, ITEM_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_COOKIE_PARAM("cookie-param(name,variable[,default,...])",
      params(STRING_O, STRING_O, ITEM_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_ERROR_PARAM("error-param(name,variable[,default,...])",
      params(STRING_O, STRING_O, ITEM_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_METHOD("method(name[,body])", params(STRING_O, STRING_O), REST_URI, false),
  /** XQuery annotation. */
  _REST_SINGLE("single([key])", params(STRING_O), REST_URI),
  /** XQuery annotation. */
  _REST_GET("GET()", params(), REST_URI),
  /** XQuery annotation. */
  _REST_POST("POST([body])", params(STRING_O), REST_URI),
  /** XQuery annotation. */
  _REST_PUT("PUT([body])", params(STRING_O), REST_URI),
  /** XQuery annotation. */
  _REST_DELETE("DELETE()", params(), REST_URI),
  /** XQuery annotation. */
  _REST_HEAD("HEAD()", params(), REST_URI),
  /** XQuery annotation. */
  _REST_OPTIONS("OPTIONS()", params(), REST_URI),

  /** XQuery annotation. */
  _UNIT_AFTER("after([function])", params(STRING_O), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_AFTER_MODULE("after-module()", params(), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_BEFORE("before([function])", params(STRING_O), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_BEFORE_MODULE("before-module()", params(), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_IGNORE("ignore([message])", params(STRING_O), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_TEST("test(['expected',error])", params(STRING_O, STRING_O), UNIT_URI),

  /** XQuery annotation. */
  _WS_CONNECT("connect(path)", params(STRING_O), WS_URI),
  /** XQuery annotation. */
  _WS_MESSAGE("message(path,message)", params(STRING_O, STRING_O), WS_URI),
  /** XQuery annotation. */
  _WS_CLOSE("close(path)", params(STRING_O), WS_URI),
  /** XQuery annotation. */
  _WS_HEADER_PARAM("header-param(name,variable[,default,...])",
      params(STRING_O, STRING_O, ITEM_O), WS_URI, false),
  /** XQuery annotation. */
  _WS_ERROR("error(path,message)", params(STRING_O, STRING_O), WS_URI);

  /** Parameter types. */
  public final SeqType[] params;
  /** URI. */
  public final byte[] uri;
  /** Minimum and maximum number of arguments. */
  public final int[] minMax;
  /** Annotation must only occur once. */
  public final boolean single;
  /** Descriptions. */
  private final String desc;

  /** Cached enums (faster). */
  public static final Annotation[] VALUES = values();

  /** Maps with QName and signature pairs. */
  private static final TokenObjMap<Annotation> MAP = new TokenObjMap<>();

  static {
    for(final Annotation value : VALUES) {
      MAP.put(new QNm(value.local(), value.uri).id(), value);
    }
  }

  /**
   * Constructor.
   * @param desc descriptive function string, containing the function name and its arguments
   * @param params parameter types
   * @param uri uri
   */
  Annotation(final String desc, final SeqType[] params, final byte[] uri) {
    this(desc, params, uri, true);
  }

  /**
   * Constructor.
   * @param desc descriptive function string, containing the function name and its arguments
   * @param params parameter types
   * @param uri uri
   * @param single annotation must only occur once
   */
  Annotation(final String desc, final SeqType[] params, final byte[] uri, final boolean single) {
    this.desc = desc;
    this.params = params;
    this.uri = uri;
    this.single = single;
    minMax = FuncDefinition.minMax(desc, params);
  }

  /**
   * Returns an annotation with the specified name.
   * @param name name
   * @return annotation or {@code null}
   */
  public static Annotation get(final QNm name) {
    return MAP.get(name.id());
  }

  /**
   * Returns an array representation of the specified sequence types.
   * @param params parameter types
   * @return array
   */
  private static SeqType[] params(final SeqType... params) {
    return params;
  }

  /**
   * Returns the local name of the annotation.
   * @return name
   */
  public byte[] local() {
    return token(desc.substring(0, desc.indexOf('(')));
  }

  /**
   * Returns the QName of the annotation.
   * @return QName
   */
  public QNm qname() {
    return new QNm(id(), uri);
  }

  /**
   * Returns the prefixed name of the annotation.
   * @return name
   */
  public byte[] id() {
    final TokenBuilder tb = new TokenBuilder();
    if(!eq(uri, XQ_URI)) tb.add(NSGlobal.prefix(uri)).add(':');
    return tb.add(local()).finish();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add('%');
    if(!eq(uri, XQ_URI)) tb.add(NSGlobal.prefix(uri)).add(':');
    // chop parentheses if annotation has no parameters
    return tb.add(desc.replace("()", "")).toString();
  }
}
