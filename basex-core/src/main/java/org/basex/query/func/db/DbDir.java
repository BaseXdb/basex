package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.http.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final Data data = checkData(qc);
    byte[] path = toToken(exprs[1], qc);
    String root = MetaData.normPath(string(path));
    if(root == null) throw DB_PATH_X.get(info, path);

    if(!root.isEmpty() && !Strings.endsWith(root, '/')) root += '/';
    path = token(root);

    final ValueBuilder vb = new ValueBuilder(qc);
    final TokenSet map = new TokenSet();

    final IntList docs = data.resources.docs(root, false);
    final int ds = docs.size();
    for(int d = 0; d < ds; d++) {
      byte[] np = data.text(docs.get(d), true);
      np = substring(np, path.length, np.length);

      final int i = indexOf(np, SLASH);
      final boolean dir = i >= 0;
      if(dir) np = substring(np, 0, i);
      if(map.contains(np)) continue;

      map.put(np);
      vb.add(dir ? dir(np, data.meta.time) :
        resource(np, false, MediaType.APPLICATION_XML, data.meta.time, null));
    }

    final IOFile file = data.meta.binary(string(path));
    if(file != null) {
      for(final IOFile io : file.children()) {
        final byte[] np = token(io.name());
        if(map.contains(np)) continue;

        map.put(np);
        vb.add(io.isDir() ? dir(np, io.timeStamp()) :
          resource(np, true, MediaType.get(io.path()), io.timeStamp(), io.length()));
      }
    }

    return vb.value(this);
  }
}
