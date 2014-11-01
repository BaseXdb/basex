package org.basex.query.func.client;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ClientQuery extends ClientFn {
  /** Query pattern. */
  private static final Pattern QUERYPAT = Pattern.compile("\\[(.*?)\\] (.*)", Pattern.MULTILINE);

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final ClientSession cs = session(qc, false);
    final String query = Token.string(toToken(exprs[1], qc));
    final ValueBuilder vb = new ValueBuilder();
    try(org.basex.api.client.ClientQuery cq = cs.query(query)) {
      // bind variables and context value
      for(final Map.Entry<String, Value> binding : toBindings(2, qc).entrySet()) {
        final String k = binding.getKey();
        final Value value = binding.getValue();
        if(k.isEmpty()) cq.context(value);
        else cq.bind(k, value);
      }

      // evaluate query
      while(cq.more()) {
        final String result = cq.next();
        vb.add(cq.type().castString(result, qc, sc, info));
      }
      return vb.value();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final BaseXException ex) {
      final Matcher m = QUERYPAT.matcher(ex.getMessage());
      if(m.find()) {
        final QueryException exc = get(m.group(1), m.group(2), info);
        throw exc == null ? new QueryException(info, new QNm(m.group(1)), m.group(2)) : exc;
      }
      throw BXCL_QUERY_X.get(info, ex);
    } catch(final IOException ex) {
      throw BXCL_COMM_X.get(info, ex);
    }
  }
}
