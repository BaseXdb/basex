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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum Annotation {
  /** XQuery annotation. */
  PUBLIC("public()", arg(), XQ_URI, false),
  /** XQuery annotation. */
  PRIVATE("private()", arg(), XQ_URI, false),
  /** XQuery annotation. */
  UPDATING("updating()", arg(), XQ_URI, false),

  /** XQuery annotation. */
  _BASEX_LAZY("lazy()", arg(), BASEX_URI),
  /** XQuery annotation. */
  _BASEX_INLINE("inline([limit])", arg(ITR), BASEX_URI),

  /** XQuery annotation. */
  _OUTPUT_ALLOW_DUPLICATE_NAMES("allow-duplicate-names(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_BYTE_ORDER_MARK("byte-order-mark(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_CDATA_SECTION_ELEMENTS("cdata-section-elements(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_CSV("csv(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_DOCTYPE_PUBLIC("doctype-public(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_DOCTYPE_SYSTEM("doctype-system(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ENCODING("encoding(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ESCAPE_URI_ATTRIBUTES("escape-uri-attributes(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_HTML_VERSION("html-version(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INCLUDE_CONTENT_TYPE("include-content-type(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INDENT("indent(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_INDENTS("indents(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_ITEM_SEPARATOR("item-separator(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_JSON("json(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_JSON_NODE_OUTPUT_METHOD("json-node-output-method(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_MEDIA_TYPE("media-type(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_METHOD("method(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_NEWLINE("newline(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_NORMALIZATION_FORM("normalization-form(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_OMIT_XML_DECLARATION("omit-xml-declaration(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_PARAMETER_DOCUMENT("parameter-document(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_STANDALONE("standalone(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_SUPPRESS_INDENTATION("suppress-indentation(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_TABULATOR("tabulator(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_UNDECLARE_PREFIXES("undeclare-prefixes(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_USE_CHARACTER_MAPS("use-character-maps(value)", arg(STR), OUTPUT_URI),
  /** XQuery annotation. */
  _OUTPUT_VERSION("version(value)", arg(STR), OUTPUT_URI),

  /** XQuery annotation. */
  _REST_PATH("path(path)", arg(STR), REST_URI),
  /** XQuery annotation. */
  _REST_ERROR("error(code[,...])", arg(STR), REST_URI, false),
  /** XQuery annotation. */
  _REST_CONSUMES("consumes(type[,...])", arg(STR), REST_URI, false),
  /** XQuery annotation. */
  _REST_PRODUCES("produces(type[,...])", arg(STR), REST_URI, false),
  /** XQuery annotation. */
  _REST_QUERY_PARAM("query-param(name,variable[,default,...])",
      arg(STR, STR, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_FORM_PARAM("form-param(name,variable[,default,...])",
      arg(STR, STR, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_HEADER_PARAM("header-param(name,variable[,default,...])",
      arg(STR, STR, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_COOKIE_PARAM("cookie-param(name,variable[,default,...])",
      arg(STR, STR, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_ERROR_PARAM("error-param(name,variable[,default,...])",
      arg(STR, STR, ITEM), REST_URI, false),
  /** XQuery annotation. */
  _REST_METHOD("method(name[,body])", arg(STR, STR), REST_URI, false),
  /** XQuery annotation. */
  _REST_GET("GET()", arg(), REST_URI),
  /** XQuery annotation. */
  _REST_POST("POST([body])", arg(STR), REST_URI),
  /** XQuery annotation. */
  _REST_PUT("PUT([body])", arg(STR), REST_URI),
  /** XQuery annotation. */
  _REST_DELETE("DELETE()", arg(), REST_URI),
  /** XQuery annotation. */
  _REST_HEAD("HEAD()", arg(), REST_URI),
  /** XQuery annotation. */
  _REST_OPTIONS("OPTIONS()", arg(), REST_URI),

  /** XQuery annotation. */
  _UNIT_AFTER("after([function])", arg(STR), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_AFTER_MODULE("after-module()", arg(), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_BEFORE("before([function])", arg(STR), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_BEFORE_MODULE("before-module()", arg(), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_IGNORE("ignore()", arg(), UNIT_URI),
  /** XQuery annotation. */
  _UNIT_TEST("test(['expected',error])", arg(STR, STR), UNIT_URI);

  /** Descriptions. */
  public final String desc;
  /** Argument types. */
  public final SeqType[] args;
  /** URI. */
  public final byte[] uri;
  /** Minimum and maximum number of arguments. */
  public final int[] minMax;
  /** Annotation must only occur once. */
  public final boolean single;

  /** Cached enums (faster). */
  public static final Annotation[] VALUES = values();

  /** Maps with QName and signature pairs. */
  private static final TokenObjMap<Annotation> MAP = new TokenObjMap<>();

  static {
    for(final Annotation sig : VALUES) MAP.put(new QNm(sig.local(), sig.uri).id(), sig);
  }

  /**
   * Constructor.
   * @param desc descriptive function string, containing the function name and its arguments.
   * @param args types of the annotation arguments
   * @param uri uri
   */
  Annotation(final String desc, final SeqType[] args, final byte[] uri) {
    this(desc, args, uri, true);
  }

  /**
   * Constructor.
   * @param desc descriptive function string, containing the function name and its arguments.
   * @param args types of the annotation arguments
   * @param uri uri
   * @param single annotation must only occur once
   */
  Annotation(final String desc, final SeqType[] args, final byte[] uri, final boolean single) {
    this.desc = desc;
    this.args = args;
    this.uri = uri;
    this.single = single;
    minMax = Function.minMax(desc, args);
  }

  /**
   * Returns an annotation with the specified name.
   * @param name name
   * @return annotation, or {@code null}
   */
  public static Annotation get(final QNm name) {
    return MAP.get(name.id());
  }

  /**
   * Returns an array representation of the specified sequence types.
   * @param arg arguments
   * @return array
   */
  private static SeqType[] arg(final SeqType... arg) { return arg; }

  /**
   * Returns the local name of the annotation.
   * @return name
   */
  public byte[] local() {
    return new TokenBuilder(desc.substring(0, desc.indexOf('('))).finish();
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
    return tb.add(desc.replace("()", "")).toString();
  }
}
