package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class RequestParameter extends RequestFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = Token.string(toToken(exprs[0], qc));
    final HTTPParams hp = new HTTPParams(request(qc));
    try {
      final Value query = hp.query().get(name);
      final Value form = hp.form(qc.context.options).get(name);
      if(query == null && form == null)
        return exprs.length == 1 ? Empty.VALUE : exprs[1].value(qc);
      if(query == null) return form;
      if(form == null) return query;
      return ValueBuilder.concat(query, form, qc);
    } catch(final IOException ex) {
      throw REQUEST_PARAMETER.get(info, hp.queryString());
    }
  }
}
