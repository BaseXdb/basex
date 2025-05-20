package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.in.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.options.*;

/**
 * JSON parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseJson extends ParseFn {
  /**
   * Returns the default conversion format.
   * @return format
   */
  protected abstract JsonFormat format();

  @Override
  final QueryError error() {
    return PARSE_JSON_X;
  }

  @Override
  protected final Options options(final QueryContext qc) throws QueryException {
    return toOptions(arg(1), new JsonParserOptions(), qc);
  }

  @Override
  final Value parse(final TextInput ti, final Options options, final QueryContext qc)
      throws QueryException, IOException {

    final JsonFormat format = format();
    if(format != null) options.set(JsonOptions.FORMAT, format);

    final JsonConverter converter = JsonConverter.get((JsonParserOptions) options);
    final JsonFormat jf = options.get(JsonOptions.FORMAT);
    if(options.get(JsonParserOptions.VALIDATE) != null && jf != JsonFormat.W3_XML &&
      jf != JsonFormat.BASIC) {
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
    if(nll != Empty.VALUE && jf != JsonFormat.W3 && jf != JsonFormat.XQUERY) {
      throw INVALIDOPTION_X.get(info, Options.unknown(JsonParserOptions.NULL));
    }
    converter.nullValue(nll);
    return converter.convert(ti, "", info, qc);
  }
}
