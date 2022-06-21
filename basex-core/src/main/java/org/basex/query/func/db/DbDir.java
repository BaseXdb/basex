package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DbDir extends DbList {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // overwrites implementation of the super class
    return resources(qc).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // overwrites implementation of the super class
    return resources(qc);
  }

  /**
   * Returns an iterator over all resources in a databases.
   * @param qc query context
   * @return resource iterator
   * @throws QueryException query exception
   */
  private Value resources(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    byte[] token = toToken(exprs[1], qc);
    String path = MetaData.normPath(string(token));
    if(path == null) throw DB_PATH_X.get(info, token);

    if(!path.isEmpty() && !Strings.endsWith(path, '/')) path += '/';
    token = token(path);

    final ValueBuilder vb = new ValueBuilder(qc);
    final HashSet<String> set = new HashSet<>();

    // list XML resources
    final IntList docs = data.resources.docs(path, false);
    final int ds = docs.size();
    for(int d = 0; d < ds; d++) {
      String pt = string(data.text(docs.get(d), true)).substring(token.length);
      final int i = pt.indexOf('/');
      final boolean dir = i >= 0;
      if(dir) pt = pt.substring(0, i);
      if(set.add(pt)) {
        vb.add(dir ? dir(pt, data.meta.time) :
          resource(pt, false, MediaType.APPLICATION_XML, data.meta.time, null));
      }
    }

    // list file resources
    final IOFile bin = data.meta.binary(string(token));
    if(bin != null) {
      for(final IOFile child : bin.children()) {
        final String pt = child.name();
        if(set.add(pt)) {
          vb.add(child.isDir() ? dir(pt, child.timeStamp()) :
            resource(pt, true, MediaType.get(child.path()), child.timeStamp(), child.length()));
        }
      }
    }

    return vb.value(this);
  }
}
