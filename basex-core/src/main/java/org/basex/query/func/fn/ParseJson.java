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
   * Returns an XQuery value for the parsed data.
   * @param qc query context
   * @param format format (can be {@code null})
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final Value parse(final QueryContext qc, final JsonFormat format)
      throws QueryException {
    final byte[] source = toTokenOrNull(arg(0), qc);
    if(source == null) return Empty.VALUE;
    try(TextInput ti = new TextInput(source)) {
      return parse(qc, format, ti);
    } catch(final IOException ex) {
      throw PARSE_JSON_X.get(info, ex);
    }
  }

  /**
   * Returns a document node for the parsed data.
   * @param qc query context
   * @param format format
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final Value doc(final QueryContext qc, final JsonFormat format) throws QueryException {
    final Item source = arg(0).atomItem(qc, info);
    return source.isEmpty() ? Empty.VALUE : parse(source, false, format, PARSE_JSON_X, qc);
  }

  @Override
  final Value parse(final TextInput ti, final Object options, final QueryContext qc)
      throws QueryException, IOException {
    return parse(qc, (JsonFormat) options, ti);
  }

  /**
   * Parses the specified string.
   * @param format format
   * @param qc query context
   * @param ti text input
   * @return resulting item
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private Value parse(final QueryContext qc, final JsonFormat format, final TextInput ti)
      throws QueryException, IOException {

    final JsonParserOptions options = toOptions(arg(1), new JsonParserOptions(), qc);
    if(format != null) options.set(JsonOptions.FORMAT, format);

    final JsonConverter converter = JsonConverter.get(options);
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
    return converter.convert(Token.string(ti.content()), "", qc);
  }
}
