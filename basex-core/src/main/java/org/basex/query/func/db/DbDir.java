package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.util.*;
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
    final byte[] token = toToken(exprs[1], qc);

    String path = MetaData.normPath(string(token));
    if(path == null) throw DB_PATH_X.get(info, token);
    if(!path.isEmpty() && !Strings.endsWith(path, '/')) path += '/';

    final ValueBuilder vb = new ValueBuilder(qc);
    final HashSet<String> set = new HashSet<>();

    // list XML resources
    final IntList docs = data.resources.docs(path, false);
    final int ds = docs.size();
    for(int d = 0; d < ds; d++) {
      String pt = string(substring(data.text(docs.get(d), true), path.length()));
      final int i = pt.indexOf('/');
      final boolean dir = i >= 0;
      if(dir) pt = pt.substring(0, i);
      if(set.add(pt)) vb.add(dir ? dir(pt, data.meta.time) :
        resource(pt, data.meta.time, null, ResourceType.XML));
    }

    // list file resources
    for(final ResourceType type : Resources.BINARIES) {
      final IOFile bin = data.meta.file(path, type);
      if(bin != null) {
        for(final IOFile child : bin.children()) {
          final String pt = child.name();
          if(set.add(pt)) vb.add(child.isDir() ? dir(pt, child.timeStamp()) :
            resource(pt, child.timeStamp(), child.length(), type));
        }
      }
    }

    return vb.value(this);
  }
}
