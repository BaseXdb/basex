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
   * @param json json string
   * @param xml convert to xml
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  final Item parse(final byte[] json, final boolean xml, final QueryContext qc)
      throws QueryException {

    final JsonParserOptions options = toOptions(arg(1), new JsonParserOptions(), false, qc);
    final boolean esc = options.get(JsonParserOptions.ESCAPE);
    final FuncItem fb = options.get(JsonParserOptions.FALLBACK);
    final FuncItem np = options.get(JsonParserOptions.NUMBER_PARSER);
    if(esc && fb != null) throw OPTION_JSON_X.get(info,
        "Escaping cannot be combined with a fallback function.");

    try {
      options.set(JsonOptions.FORMAT, xml ? JsonFormat.BASIC : JsonFormat.XQUERY);
      return JsonConverter.get(options).fallback(function(fb, qc)).
          numberParser(function(np, qc)).convert(Token.string(json), "");
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
   * Converts a function item to a function suitable for JSON parsing.
   * @param func function item
   * @param qc query context
   * @return function or {@code null}
   * @throws QueryException query exception
   */
  final QueryFunction<byte[], Item> function(final FuncItem func, final QueryContext qc)
      throws QueryException {
    if(func == null) return null;
    toFunction(func, 1, qc);
    return string -> func.invoke(qc, info, Atm.get(string)).item(qc, info);
  }
}
