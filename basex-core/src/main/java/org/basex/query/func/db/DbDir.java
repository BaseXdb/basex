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
    byte[] path = toToken(exprs[1], qc);
    String root = MetaData.normPath(string(path));
    if(root == null) throw DB_PATH_X.get(info, path);

    if(!root.isEmpty() && !Strings.endsWith(root, '/')) root += '/';
    path = token(root);

    final ValueBuilder vb = new ValueBuilder(qc);
    final HashSet<String> set = new HashSet<>();

    final IntList docs = data.resources.docs(root, false);
    final int ds = docs.size();
    for(int d = 0; d < ds; d++) {
      String np = string(data.text(docs.get(d), true)).substring(path.length);
      final int i = np.indexOf('/');
      final boolean dir = i >= 0;
      if(dir) np = np.substring(0, i);
      if(set.add(np)) {
        vb.add(dir ? dir(np, data.meta.time) :
          resource(np, false, MediaType.APPLICATION_XML, data.meta.time, null));
      }
    }

    final IOFile bin = data.meta.binary(string(path));
    if(bin != null) {
      for(final IOFile io : bin.children()) {
        final String np = io.name();
        if(set.add(np)) {
          vb.add(io.isDir() ? dir(np, io.timeStamp()) :
            resource(np, true, MediaType.get(io.path()), io.timeStamp(), io.length()));
        }
      }
    }

    return vb.value(this);
  }
}
