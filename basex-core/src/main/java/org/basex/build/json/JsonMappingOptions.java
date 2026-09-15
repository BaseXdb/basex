package org.basex.build.json;

import org.basex.query.*;
import org.basex.query.func.fn.PlanFn.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for converting between JSON and XML with the w3-mapping format.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonMappingOptions extends ElementsOptions {
  /** Option: name of the root element. */
  public static final StringOption ROOT = new StringOption("root");

  /**
   * Returns the options that are defined by the specified mapping.
   * @param mapping mapping (map or empty sequence)
   * @param qc query context (can be {@code null})
   * @param info input info (can be {@code null})
   * @return options
   * @throws QueryException query exception
   */
  public static JsonMappingOptions get(final Value mapping, final QueryContext qc,
      final InputInfo info) throws QueryException {
    final JsonMappingOptions mopts = new JsonMappingOptions();
    if(!mapping.isEmpty()) {
      if(!(mapping instanceof final XQMap map)) {
        throw QueryError.typeError(mapping, Types.MAP, info);
      }
      mopts.assign(map, qc, info);
    }
    return mopts;
  }
}
