package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.in.*;
import org.basex.io.parse.*;
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

    final JsonParserOptions jopts = (JsonParserOptions) options;
    final boolean elements = jopts.get(JsonOptions.FORMAT) == JsonFormat.W3_MAPPING;
    final JsonConverter converter = JsonConverter.get(jopts, info);
    final Value fallback = options.get(JsonParserOptions.FALLBACK);
    if(!fallback.isEmpty()) {
      final FItem fb = toFunction(fallback, 1, qc);
      converter.fallback(s -> toAtomItem(fb.invoke(qc, info, Str.get(s)), qc).string(info));
    }
    if(!elements) converter.nullValue(options.get(JsonParserOptions.NULL));
    final Value value = converter.convert(ti, "", info, qc);
    return elements ? elements(value, jopts, qc) : value;
  }

  /**
   * Converts maps to document nodes.
   * @param value parsed value
   * @param options options
   * @param qc query context
   * @return document nodes
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private Value elements(final Value value, final JsonParserOptions options,
      final QueryContext qc) throws QueryException, IOException {
    final JsonMappingOptions mopts = JsonMappingOptions.get(options.get(JsonOptions.MAPPING),
        qc, info);
    final String root = mopts.get(JsonMappingOptions.ROOT);
    final MapToElement converter = new MapToElement(mopts, PlanFn.uris(qc, sc()), qc.shared,
        qc.context.options, info);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : value) {
      final NodeHandler handler = new NodeHandler("", false);
      converter.convert(item, root, handler);
      vb.add(handler.finish());
    }
    return vb.value();
  }
}
