package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.JsonFormat;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class FnParseJson extends Parse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    if(it == null) return null;
    return parse(toToken(it), qc, ii);
  }

  /**
   * Parses the specified JSON string.
   * @param json json string
   * @param qc query context
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  final Item parse(final byte[] json, final QueryContext qc, final InputInfo ii)
      throws QueryException {

    final JsonParserOptions opts = new JsonParserOptions();
    if(exprs.length > 1) {
      final Map options = toMap(exprs[1], qc);
      try {
        new FuncOptions(null, info).acceptUnknown().parse(options, opts);
      } catch(final QueryException ex) {
        throw JSON_OPT_X.get(ii, ex.getLocalizedMessage());
      }
    }

    final boolean unesc = opts.get(JsonParserOptions.UNESCAPE);
    final FuncItem fb = opts.get(JsonParserOptions.FALLBACK);
    if(fb != null) {
      final Type type = FuncType.get(SeqType.STR, SeqType.STR);
      if(!fb.type.instanceOf(type)) throw JSON_FUNC_OPT_X_X.get(ii, type, fb.type);
    }

    try {
      opts.set(JsonOptions.FORMAT, JsonFormat.MAP);
      final JsonConverter conv = JsonConverter.get(opts);
      if(unesc && fb != null) conv.fallback(new JsonFallback() {
        @Override
        public byte[] convert(final byte[] string) {
          try {
            return fb.invokeItem(qc, ii, Str.get(string)).string(ii);
          } catch(final QueryException ex) {
            throw new QueryRTException(ex);
          }
        }
      });
      return conv.convert(json, null);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    } catch(final QueryIOException ex) {
      Util.debug(ex);
      final QueryException qe = ex.getCause(info);
      final QueryError error = qe.error();
      final String message = ex.getLocalizedMessage();
      if(error == BXJS_PARSE_X_X_X) throw JSON_PARSE_X.get(ii, message);
      if(error == BXJS_DUPLICATE_X) throw JSON_DUPLICATE_X.get(ii, message);
      throw qe;
    }
  }
}
