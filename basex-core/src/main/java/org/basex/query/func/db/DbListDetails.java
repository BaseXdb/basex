package org.basex.query.func.db;

import static org.basex.query.func.db.DbAccess.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
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
public final class DbListDetails extends DbList {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // overwrites implementation of the super class
    return defined(0) ? resources(qc) : list(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // overwrites implementation of the super class
    return iter(qc).value(qc, this);
  }

  /**
   * Lists details for all databases.
   * @param qc query context
   * @return iterator
   */
  private static Iter list(final QueryContext qc) {
    final Context ctx = qc.context;
    final StringList dbs = ctx.listDBs();
    return new BasicIter<FNode>(dbs.size()) {
      @Override
      public FNode get(final long i) {
        final String name = dbs.get((int) i);
        final FBuilder database = FElem.build(Q_DATABASE);
        final MetaData meta = new MetaData(name, ctx.options, ctx.soptions);
        try {
          meta.read();
          // count number of binary files
          final int binaries = meta.dir(ResourceType.BINARY).descendants().size();
          final int values = meta.dir(ResourceType.VALUE).descendants().size();
          database.add(Q_RESOURCES, meta.ndocs + binaries + values);
          database.add(Q_MODIFIED_DATE, DateTime.format(new Date(meta.dbTime())));
          database.add(Q_SIZE, meta.dbSize());
          if(ctx.perm(Perm.CREATE, name)) database.add(Q_PATH, meta.original);
        } catch(final IOException ex) {
          // invalid database will be ignored
          Util.debug(ex);
        }
        return database.add(name).finish();
      }
    };
  }

  /**
   * Returns an iterator over all resources in a databases.
   * @param qc query context
   * @return resource iterator
   * @throws QueryException query exception
   */
  private Iter resources(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final String path = defined(1) ? toString(arg(1), qc) : "";

    final IntList docs = data.resources.docs(path);
    final StringList binaries = data.resources.paths(path, ResourceType.BINARY);
    final StringList values = data.resources.paths(path, ResourceType.VALUE);
    final int ds = docs.size(), bs = ds + binaries.size(), size = bs + values.size();

    return new BasicIter<FNode>(size) {
      @Override
      public FNode get(final long i) {
        if(i >= size) return null;
        final ResourceType type;
        final String pt;
        final long mdate, sz;
        if(i < ds) {
          final int pre = docs.get((int) i);
          type = ResourceType.XML;
          pt = string(data.text(pre, true));
          mdate = data.meta.time;
          sz = data.size(pre, Data.DOC);
        } else {
          type = i >= bs ? ResourceType.VALUE : ResourceType.BINARY;
          pt = type == ResourceType.VALUE ? values.get((int) i - bs) : binaries.get((int) i - ds);
          final IOFile bin = type.filePath(data.meta.dir(type), pt);
          mdate = bin.timeStamp();
          sz = bin.length();
        }
        return resource(pt, mdate, sz, type);
      }
    };
  }
}
