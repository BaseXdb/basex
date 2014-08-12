package org.basex.query.func.json;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
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

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _JSON_PARSE:     return parse(qc);
      case _JSON_SERIALIZE: return serialize(qc);
      default:              return super.item(qc, ii);
    }
  }

  /**
   * Converts a JSON object to an item according to the given configuration.
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  private Item parse(final QueryContext qc) throws QueryException {
    final byte[] input = toToken(exprs[0], qc);
    final JsonParserOptions opts = toOptions(1, Q_OPTIONS, new JsonParserOptions(), qc);
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
   * @param qc query context
   * @return string representation
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    final JsonSerialOptions jopts = toOptions(1, Q_OPTIONS, new JsonSerialOptions(), qc);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
    sopts.set(SerializerOptions.JSON, jopts);
    return Str.get(delete(serialize(iter, sopts, INVALIDOPT_X), '\r'));
  }
}
