package org.basex.query.func.json;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonSerialize extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    Iter input = arg(0).iter(qc);
    JsonSerialOptions options = options(1, JsonSerialOptions::new, qc);
    if(options.unsupportedMapping()) {
      throw INVALIDOPTION_X.get(info, Options.unknown(JsonOptions.MAPPING));
    }
    if(options.get(JsonOptions.FORMAT) == JsonFormat.W3_MAPPING) {
      try {
        input = elements(input, options, qc).iter();
      } catch(final QueryException ex) {
        throw error(ex, ex.matches(ErrType.FOJS) ? JSON_SERIALIZE_X : null);
      }
      options = options.withoutMapping();
    }
    return Str.get(serialize(input, options(options), INVALIDOPTION_X, qc));
  }

  /**
   * Converts document and element nodes to maps.
   * @param input input
   * @param options options
   * @param qc query context
   * @return converted items
   * @throws QueryException query exception
   */
  private Value elements(final Iter input, final JsonSerialOptions options, final QueryContext qc)
      throws QueryException {
    final JsonMappingOptions mopts = toOptions(options.get(JsonOptions.MAPPING),
        new JsonMappingOptions(), qc);
    final String root = mopts.get(JsonMappingOptions.ROOT);
    final FnElementToMap func = (FnElementToMap) Function.ELEMENT_TO_MAP.get(info);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(input)) != null;) {
      if(item instanceof final XNode node &&
          (node.kind() == Kind.DOCUMENT || node.kind() == Kind.ELEMENT)) {
        final Value value = func.convert(item, mopts, qc);
        if(root != null && value instanceof final XQMap map) {
          // remove the root entry
          final Item key = map.keys().itemAt(0);
          if(!Token.eq(key.string(info), Token.token(root))) {
            throw JSON_SERIALIZE_X.get(info, Util.info("Root element '%' expected, found '%'",
                root, key.string(info)));
          }
          vb.add(map.get(key));
        } else {
          vb.add(value);
        }
      } else {
        vb.add(item);
      }
    }
    return vb.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(1, JsonSerialOptions::new, cc);
    return this;
  }

  /**
   * Creates serialization parameters for JSON options.
   * @param jopts JSON options
   * @return serialization parameters
   */
  public static SerializerOptions options(final JsonSerialOptions jopts) {
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
    sopts.set(SerializerOptions.JSON, jopts);
    return sopts;
  }
}
