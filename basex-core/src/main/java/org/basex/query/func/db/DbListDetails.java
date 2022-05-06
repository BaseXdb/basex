package org.basex.query.func.db;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DbListDetails extends DbList {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // overwrites implementation of the super class
    return exprs.length == 0 ? list(qc) : resources(qc);
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
      public FElem get(final long i) {
        final String name = dbs.get((int) i);
        final FElem database = new FElem(DATABASE);
        final MetaData meta = new MetaData(name, ctx.options, ctx.soptions);
        try {
          meta.read();
          // count number of binary files
          final int binary = meta.binaryDir().descendants().size();
          database.add(RESOURCES, token(meta.ndocs + binary));
          database.add(MODIFIED_DATE, DateTime.format(new Date(meta.dbTime())));
          database.add(SIZE, token(meta.dbSize()));
          if(ctx.perm(Perm.CREATE, name)) database.add(PATH, meta.original);
        } catch(final IOException ex) {
          // invalid database will be ignored
          Util.debug(ex);
        }
        return database.add(name);
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
    final String path = string(exprs.length == 1 ? EMPTY : toToken(exprs[1], qc));
    final IntList docs = data.resources.docs(path);
    final TokenList binaries = data.resources.binaryPaths(path);
    final int ds = docs.size(), size = ds + binaries.size();
    return new BasicIter<FNode>(size) {
      @Override
      public FNode get(final long i) {
        if(i < ds) {
          final int pre = docs.get((int) i);
          final String pt = string(data.text(pre, true));
          final int sz = data.size(pre, Data.DOC);
          return resource(pt, false, MediaType.APPLICATION_XML, data.meta.time, (long) sz);
        }
        if(i < size) {
          final String pt = string(binaries.get((int) i - ds));
          final IOFile io = data.meta.binary(pt);
          return resource(pt, true, MediaType.get(io.path()), io.timeStamp(), io.length());
        }
        return null;
      }
    };
  }
}
