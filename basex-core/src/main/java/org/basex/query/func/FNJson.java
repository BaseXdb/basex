package org.basex.query.func;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.json.*;
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
      case _JSON_PARSE:        return parse(ctx);
      case _JSON_SERIALIZE:    return serialize(ctx);
      default:                 return super.item(ctx, ii);
    }
  }

  /**
   * Converts a JSON object to an item according to the given configuration.
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  private Item parse(final QueryContext ctx) throws QueryException {
    final byte[] input = checkStr(expr[0], ctx);
    final Item opt = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    // create json properties and set options
    try {
      final JsonConverter conv = JsonConverter.get(props(map), info);
      return conv.convert(string(input)).item(ctx, info);
    } catch(final SerializerException ex) {
      throw ex.getCause();
    }
  }

  /**
   * Serializes the specified XML document to JSON.
   * @param ctx query context
   * @return string representation
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext ctx) throws QueryException {
    final ANode node = checkNode(expr[0], ctx);
    final Item opt = expr.length > 1 ? expr[1].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    // create serialization properties
    final SerializerProp props = new SerializerProp();
    props.set(S_METHOD, M_JSON);
    props.set(S_JSON, props(map).toString());

    // serialize node
    return Str.get(delete(serialize(node.iter(), props), '\r'));
  }

  /**
   * Creates JSON properties.
   * @param map map
   * @return properties
   */
  private JsonProp props(final TokenMap map) {
    final JsonProp jprop = new JsonProp();
    final byte[] unesc = map.get(token(AProp.toString(JsonProp.UNESCAPE)));
    if(unesc != null) jprop.set(JsonProp.UNESCAPE, Util.yes(string(unesc)));

    final byte[] spec = map.get(token(AProp.toString(JsonProp.SPEC)));
    if(spec != null) jprop.set(JsonProp.SPEC, string(spec));

    final byte[] format = map.get(token(AProp.toString(JsonProp.FORMAT)));
    if(format != null) jprop.set(JsonProp.FORMAT, string(format));
    return jprop;
  }
}
