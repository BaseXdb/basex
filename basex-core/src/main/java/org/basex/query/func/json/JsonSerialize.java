package org.basex.query.func.json;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
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
    final Iter input = arg(0).iter(qc);
    final JsonSerialOptions options = options(1, JsonSerialOptions::new, qc);
    if(options.unsupportedMapping()) {
      throw INVALIDOPTION_X.get(info, Options.unknown(JsonOptions.MAPPING));
    }
    try {
      return Str.get(serialize(input, options(options), INVALIDOPTION_X, qc));
    } catch(final QueryException ex) {
      throw error(ex, ex.matches(ErrType.FOJS) ? JSON_SERIALIZE_X : null);
    }
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
