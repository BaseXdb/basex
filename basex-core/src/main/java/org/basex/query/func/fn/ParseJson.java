package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.in.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
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
   * @return format, or {@code null}
   */
  protected abstract JsonFormat format();

  @Override
  protected Expr opt(final CompileContext cc) {
    // a JSON null can be mapped to an empty sequence
    return optFirst(false, true, null);
  }

  @Override
  final QueryError error() {
    return PARSE_JSON_X;
  }

  @Override
  protected final Options options(final QueryContext qc) throws QueryException {
    return toOptions(arg(1), new JsonParserOptions(), qc);
  }

  @Override
  public final int hofOffsets() {
    return functionOption(1) ? Integer.MAX_VALUE : 0;
  }

  @Override
  final Value parse(final TextInput ti, final Options options, final QueryContext qc)
      throws QueryException, IOException {

    final JsonFormat format = format();
    if(format != null) {
      if(options.get(JsonParserOptions.JSON_LINES)) {
        throw INVALIDOPTION_X.get(info, Options.unknown(JsonParserOptions.JSON_LINES));
      }
      options.set(JsonOptions.FORMAT, format);
    }

    final JsonConverter converter = JsonConverter.get((JsonParserOptions) options, info);
    final Value fallback = options.get(JsonParserOptions.FALLBACK);
    if(!fallback.isEmpty()) {
      final FItem fb = toFunction(fallback, 1, qc);
      converter.fallback(s -> toAtomItem(fb.invoke(qc, info, Str.get(s)), qc).string(info));
    }
    converter.nullValue(options.get(JsonParserOptions.NULL));
    return converter.convert(ti, "", info, qc);
  }
}
