package org.basex.query.func.client;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.Map.*;
import java.util.regex.*;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ClientQuery extends ClientFn {
  /** Query pattern. */
  private static final Pattern QUERYPAT = Pattern.compile("\\[(.*?)] (.*)", Pattern.MULTILINE);

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final ClientSession cs = session(qc, false);
    final String query = Token.string(toToken(exprs[1], qc));
    final ValueBuilder vb = new ValueBuilder(qc);
    try(org.basex.api.client.ClientQuery cq = cs.query(query)) {
      // bind variables and context value
      for(final Entry<String, Value> binding : toBindings(2, qc).entrySet()) {
        final String key = binding.getKey();
        final Value value = binding.getValue();
        if(key.isEmpty()) cq.context(value);
        else cq.bind(key, value);
      }
      // evaluate query
      cq.cache(true);
      while(cq.more()) {
        final String result = cq.next();
        final Type type = cq.type();
        if(type instanceof FuncType) throw CLIENT_FITEM_X.get(info, result);
        vb.add(cq.type().castString(result, qc, sc, info));
      }
      return vb.value();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final BaseXException ex) {
      final Matcher m = QUERYPAT.matcher(ex.getMessage());
      if(m.find()) {
        final String name = m.group(1), msg = m.group(2);
        final QueryException exc = get(name, msg, info);
        throw exc == null ? new QueryException(info, new QNm(name), msg) : exc;
      }
      throw CLIENT_QUERY_X.get(info, ex);
    } catch(final IOException ex) {
      throw CLIENT_ERROR_X.get(info, ex);
    }
  }
}
