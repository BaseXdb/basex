package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FnParseJson extends Parse {
  /** Function taking and returning a string. */
  private static final FuncType STRFUNC = FuncType.get(SeqType.STRING_O, SeqType.STRING_O);

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    return item == Empty.VALUE ? Empty.VALUE : parse(toToken(item), false, qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Parses the specified JSON string.
   * @param json json string
   * @param xml convert to xml
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  final Item parse(final byte[] json, final boolean xml, final QueryContext qc)
      throws QueryException {

    final JsonParserOptions opts = new JsonParserOptions();
    if(exprs.length > 1) new FuncOptions(info).acceptUnknown().assign(toMap(exprs[1], qc), opts);

    final boolean esc = opts.get(JsonParserOptions.ESCAPE);
    final FuncItem fb = opts.get(JsonParserOptions.FALLBACK);
    final FItem fallback = fb == null ? null : STRFUNC.cast(fb, qc, sc, info);
    if(esc && fallback != null) throw OPTION_JSON_X.get(info,
        "Escaping cannot be combined with a fallback function.");

    try {
      opts.set(JsonOptions.FORMAT, xml ? JsonFormat.BASIC : JsonFormat.XQUERY);
      final JsonConverter conv = JsonConverter.get(opts);
      if(!esc && fallback != null) conv.fallback(string -> {
        try {
          final Item item = fallback.invoke(qc, info, Str.get(string)).item(qc, info);
          return Token.string(item.string(info));
        } catch(final QueryException ex) {
          throw new QueryRTException(ex);
        }
      });
      return conv.convert(Token.string(json), "");
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    } catch(final QueryIOException ex) {
      Util.debug(ex);
      final QueryException qe = ex.getCause(info);
      final QueryError error = qe.error();
      final String message = ex.getLocalizedMessage();
      if(error == JSON_PARSE_X_X_X) throw PARSE_JSON_X.get(info, message);
      if(error == JSON_DUPL_X_X_X) throw DUPLICATE_JSON_X.get(info, message);
      if(error == JSON_OPTIONS_X) throw OPTION_JSON_X.get(info, message);
      throw qe;
    }
  }
}
