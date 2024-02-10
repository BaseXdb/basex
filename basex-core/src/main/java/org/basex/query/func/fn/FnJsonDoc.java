package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnJsonDoc extends Parse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item;
    try {
      item = unparsedText(qc, false, false);
    } catch(final QueryException ex) {
      Util.debug(ex);
      throw ex.error() == INVCHARS_X ? PARSE_JSON_X.get(info, ex.getLocalizedMessage()) : ex;
    }
    return item.isEmpty() ? Empty.VALUE : parse(item.string(info), false, qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Parses the specified JSON string.
   * @param json JSON string
   * @param xml convert to xml
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  final Item parse(final byte[] json, final boolean xml, final QueryContext qc)
      throws QueryException {

    try {
      final JsonFormat format = xml ? JsonFormat.BASIC : JsonFormat.XQUERY;
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

    final boolean dflt = format != null;
    final JsonParserOptions options = toOptions(arg(1), new JsonParserOptions(), !dflt, qc);
    if(dflt) options.set(JsonOptions.FORMAT, format);

    final JsonConverter jc = JsonConverter.get(options);
    final FuncItem fb = options.get(JsonParserOptions.FALLBACK);
    if(fb != null) {
      toFunction(fb, 1, qc);
      jc.fallback(s -> fb.invoke(qc, info, Str.get(s)).item(qc, info).string(info));
      if(options.get(JsonParserOptions.ESCAPE)) {
        throw OPTION_JSON_X.get(info, "Escape cannot be combined with fallback function.");
      }
    }
    final FuncItem np = options.get(JsonParserOptions.NUMBER_PARSER);
    if(np != null) {
      toFunction(np, 1, qc);
      jc.numberParser(s -> np.invoke(qc, info, Atm.get(s)).item(qc, info));
    }
    return jc;
  }
}
