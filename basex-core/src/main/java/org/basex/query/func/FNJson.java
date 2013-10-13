package org.basex.query.func;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.json.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

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
    final JsonOptions opts = checkOptions(1, Q_OPTIONS, new JsonOptions(), ctx);

    try {
      return JsonConverter.get(opts).convert(string(input)).item(ctx, info);
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
    final ANode node = checkNode(expr[0], ctx);
    final JsonOptions opts = checkOptions(1, Q_OPTIONS, new JsonOptions(), ctx);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(S_METHOD, M_JSON);
    sopts.set(S_JSON, opts.toString());
    return Str.get(delete(serialize(node.iter(), sopts), '\r'));
  }
}
