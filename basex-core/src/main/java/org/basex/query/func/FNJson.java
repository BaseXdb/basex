package org.basex.query.func;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.build.file.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.json.*;
import org.basex.query.util.json.JsonParser.Spec;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Functions for parsing and serializing JSON objects.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNJson extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_OPTIONS = QNm.get("options", JSONURI);
  /** The {@code unescape} key. */
  private static final byte[] UNESCAPE = token("unescape");
  /** The {@code spec} key. */
  private static final byte[] SPEC = token("spec");
  /** The {@code type} key. */
  private static final byte[] FORMAT = token("format");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNJson(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _JSON_PARSE:        return parse(false, ctx);
      case _JSON_PARSE_ML:     return parse(true, ctx);
      case _JSON_SERIALIZE:    return serialize(false, ctx);
      case _JSON_SERIALIZE_ML: return serialize(true, ctx);
      default:                 return super.item(ctx, ii);
    }
  }

  /**
   * Converts a JSON object to an item according to the given configuration.
   * @param ml JSONML flag
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  private Item parse(final boolean ml, final QueryContext ctx) throws QueryException {
    final byte[] input = checkStr(expr[0], ctx);
    final Item opt = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    final boolean unesc = !map.contains(UNESCAPE) || eq(map.get(UNESCAPE), TRUE);
    final byte[] sp = map.get(SPEC);
    final Spec spec = sp != null ? Spec.find(string(sp)) : Spec.RFC4627;
    if(spec == null) BXJS_CONFIG.thrw(info, "Unknown spec '" + string(sp) + "'");

    final byte[] form = ml ? JsonConverter.JSONML : map.get(FORMAT);
    final JsonConverter conv = JsonConverter.get(form, spec, unesc, info);
    return conv.convert(string(input)).item(ctx, info);
  }

  /**
   * Serializes the specified XML document to JSON/JsonML.
   * @param ml ml flag
   * @param ctx query context
   * @return string representation
   * @throws QueryException query exception
   */
  private Str serialize(final boolean ml, final QueryContext ctx) throws QueryException {
    final ANode node = checkNode(expr[0], ctx);
    final Item opt = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    // create serialization properties
    final SerializerProp props = new SerializerProp();
    props.set(S_METHOD, M_JSON);
    // create json properties and set options
    final JsonProp jprop = new JsonProp();
    final byte[] unesc = map.get(UNESCAPE);
    if(unesc != null) jprop.set(JsonProp.UNESCAPE, Util.yes(string(unesc)));
    final byte[] spec = map.get(SPEC);
    if(spec != null) jprop.set(JsonProp.SPEC, string(spec));
    jprop.set(JsonProp.FORMAT, map.contains(FORMAT) ?
      string(map.get(FORMAT)) : ml ? M_JSONML : M_JSON);
    props.set(S_JSON, jprop.toString());

    // serialize node
    return Str.get(delete(serialize(node.iter(), props), '\r'));
  }
}
