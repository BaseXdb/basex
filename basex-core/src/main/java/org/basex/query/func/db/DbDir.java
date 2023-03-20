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
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
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
    final String string = toString(arg(1), qc);

    String path = MetaData.normPath(string);
    if(path == null) throw DB_PATH_X.get(info, string);
    if(!path.isEmpty() && !Strings.endsWith(path, '/')) path += '/';

    final ValueBuilder vb = new ValueBuilder(qc);
    final HashSet<String> set = new HashSet<>();
    final Resources resources = data.resources;

    // list XML resources
    final IntList docs = resources.docs(path, true);
    final long ds = docs.size(), ts = data.meta.time;
    for(int d = 0; d < ds; d++) {
      final int pre = docs.get(d);
      String name = string(substring(data.text(pre, true), path.length()));
      final int i = name.indexOf('/');
      final boolean dir = i >= 0;
      if(dir) name = name.substring(0, i);
      if(set.add(name)) vb.add(elem(dir, name, ts, data.size(pre, Data.DOC), ResourceType.XML));
    }
    // list file resources
    if(!data.inMemory()) {
      for(final ResourceType type : Resources.BINARIES) {
        final IOFile bin = data.meta.file(path, type);
        for(final IOFile file : bin.children()) {
          final boolean dir = file.isDir();
          final String name = dir ? file.name() : type.dbPath(file.name());
          if(set.add(name)) vb.add(elem(file.isDir(), name, file.timeStamp(), file.length(), type));
        }
      }
    }
    return vb.value(this);
  }

  /**
   * Creates an element.
   * @param dir directory flag
   * @param path path
   * @param date modified date
   * @param size file size (can be {@code null})
   * @param type resource type
   * @return element node
   */
  private FElem elem(final boolean dir, final String path, final long date, final long size,
      final ResourceType type) {
    return dir ? dir(path, date) : resource(path, date, size, type);
  }
}
