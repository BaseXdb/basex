package org.basex.query.func.json;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.Options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class JsonSerialize extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final JsonSerialOptions options = toOptions(arg(1), new JsonSerialOptions(), true, qc);
    return Str.get(serialize(input, options(options), INVALIDOPT_X, qc));
  }

  /**
   * Creates parameters for options.
   * @param jopts json options
   * @return options
   */
  public static SerializerOptions options(final JsonSerialOptions jopts) {
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
    sopts.set(SerializerOptions.JSON, jopts);
    final Boolean indent = jopts.get(JsonSerialOptions.INDENT);
    if(indent != null) sopts.set(SerializerOptions.INDENT, indent ? YesNo.YES : YesNo.NO);
    return sopts;
  }
}
