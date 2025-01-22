package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnJsonDoc extends Parse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item;
    try {
      item = unparsedText(qc, false, false, null);
    } catch(final QueryException ex) {
      Util.debug(ex);
      throw ex.error() == INVCHARS_X ? PARSE_JSON_X.get(info, ex.getLocalizedMessage()) : ex;
    }
    return item.isEmpty() ? Empty.VALUE : parse(item.string(info), JsonFormat.XQUERY, qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Parses the specified JSON string.
   * @param json JSON string
   * @param format format
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  final Item parse(final byte[] json, final JsonFormat format, final QueryContext qc)
      throws QueryException {

    try {
      return converter(qc, format).convert(Token.string(json), "");
    } catch(final QueryException ex) {
      Util.debug(ex);
      final QueryError error = ex.error();
      final String message = ex.getLocalizedMessage();
      if(error == JSON_PARSE_X_X_X) throw PARSE_JSON_X.get(info, message);
      if(error == JSON_DUPL_X_X_X) throw DUPLICATE_JSON_X.get(info, message);
      if(error == JSON_OPTIONS_X) throw OPTION_JSON_X.get(info, message);
      throw ex;
    }
  }

  /**
   * Returns a JSON converter.
   * @param qc query context
   * @param format result format (can be {@code null})
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final JsonConverter converter(final QueryContext qc, final JsonFormat format)
      throws QueryException {

    final JsonParserOptions options = toOptions(arg(1), new JsonParserOptions(), qc);
    if(format != null) options.set(JsonOptions.FORMAT, format);

    final JsonConverter jc = JsonConverter.get(options);
    final JsonFormat jf = options.get(JsonOptions.FORMAT);
    if(options.get(JsonParserOptions.VALIDATE) != null && jf != JsonFormat.BASIC) {
      throw OPTION_X.get(info, Options.unknown(JsonParserOptions.VALIDATE));
    }

    final Value fallback = options.get(JsonParserOptions.FALLBACK);
    if(!fallback.isEmpty()) {
      final FItem fb = toFunction(fallback, 1, qc);
      jc.fallback(s -> toAtomItem(fb.invoke(qc, info, Str.get(s)), qc).string(info));
      if(options.get(JsonParserOptions.ESCAPE)) {
        throw OPTION_JSON_X.get(info, "Escape cannot be combined with fallback function.");
      }
    }
    final Value numberParser = options.get(JsonParserOptions.NUMBER_PARSER);
    if(!numberParser.isEmpty()) {
      final FItem np = toFunction(numberParser, 1, qc);
      jc.numberParser(s -> np.invoke(qc, info, Atm.get(s)).item(qc, info));
    }
    final Value nll = options.get(JsonParserOptions.NULL);
    if(nll != Empty.VALUE && jf != JsonFormat.XQUERY) {
      throw OPTION_X.get(info, Options.unknown(JsonParserOptions.NULL));
    }
    jc.nullValue(nll);
    return jc;
  }
}
