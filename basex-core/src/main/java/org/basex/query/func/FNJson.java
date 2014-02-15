package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions for parsing and serializing JSON objects.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNJson extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_OPTIONS = QNm.get("json:options", JSONURI);

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNJson(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _JSON_PARSE:     return parse(ctx);
      case _JSON_SERIALIZE: return serialize(ctx);
      default:              return super.item(ctx, ii);
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
    final JsonParserOptions opts = checkOptions(1, Q_OPTIONS, new JsonParserOptions(), ctx);
    try {
      final JsonConverter conv = JsonConverter.get(opts);
      conv.convert(input, null);
      return conv.finish();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }

  /**
   * Serializes the specified XML document to JSON.
   * @param ctx query context
   * @return string representation
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);
    final JsonSerialOptions jopts = checkOptions(1, Q_OPTIONS, new JsonSerialOptions(), ctx);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
    sopts.set(SerializerOptions.JSON, jopts);
    return Str.get(delete(serialize(iter, sopts, INVALIDOPT), '\r'));
  }
}
