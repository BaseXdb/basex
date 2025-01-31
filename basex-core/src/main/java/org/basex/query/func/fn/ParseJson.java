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
 * JSON parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseJson extends ParseFn {
  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Returns an XDM value for the parsed data.
   * @param qc query context
   * @param format format (can be {@code null})
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final Value parse(final QueryContext qc, final JsonFormat format)
      throws QueryException {
    return parse(qc, format, toTokenOrNull(arg(0), qc));
  }

  /**
   * Returns a document node for the parsed data.
   * @param qc query context
   * @param format format
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final Value doc(final QueryContext qc, final JsonFormat format) throws QueryException {
    final Item item;
    try {
      item = unparsedText(qc, false, false, null);
    } catch(final QueryException ex) {
      throw error(ex, ex.error() == INVCHARS_X ? PARSE_JSON_X : null);
    }
    return item.isEmpty() ? Empty.VALUE : parse(qc, format, item.string(info));
  }

  /**
   * Parses the specified string.
   * @param data data to parse (can be {@code null})
   * @param format format
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Value parse(final QueryContext qc, final JsonFormat format, final byte[] data)
      throws QueryException {

    if(data == null) return Empty.VALUE;

    final JsonParserOptions options = toOptions(arg(1), new JsonParserOptions(), qc);
    if(format != null) options.set(JsonOptions.FORMAT, format);

    final JsonConverter converter = JsonConverter.get(options);
    final JsonFormat jf = options.get(JsonOptions.FORMAT);
    if(options.get(JsonParserOptions.VALIDATE) != null && jf != JsonFormat.BASIC) {
      throw INVALIDOPTION_X.get(info, Options.unknown(JsonParserOptions.VALIDATE));
    }

    final Value fallback = options.get(JsonParserOptions.FALLBACK);
    if(!fallback.isEmpty()) {
      final FItem fb = toFunction(fallback, 1, qc);
      converter.fallback(s -> toAtomItem(fb.invoke(qc, info, Str.get(s)), qc).string(info));
      if(options.get(JsonParserOptions.ESCAPE)) {
        throw OPTION_JSON_X.get(info, "Escape cannot be combined with fallback function.");
      }
    }
    final Value numberParser = options.get(JsonParserOptions.NUMBER_PARSER);
    if(!numberParser.isEmpty()) {
      final FItem np = toFunction(numberParser, 1, qc);
      converter.numberParser(s -> np.invoke(qc, info, Atm.get(s)).item(qc, info));
    }
    final Value nll = options.get(JsonParserOptions.NULL);
    if(nll != Empty.VALUE && jf != JsonFormat.XQUERY) {
      throw INVALIDOPTION_X.get(info, Options.unknown(JsonParserOptions.NULL));
    }
    converter.nullValue(nll);
    return converter.convert(Token.string(data), "");
  }
}
