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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class DbListDetails extends DbList {
  /** Resource element name. */
  private static final String DATABASE = "database";
  /** Resource element name. */
  private static final String RESOURCE = "resource";
  /** Resource element name. */
  private static final String RESOURCES = "resources";
  /** Path element name. */
  private static final String PATH = "path";
  /** Raw element name. */
  private static final String RAW = "raw";
  /** Size element name. */
  private static final String SIZE = "size";
  /** Content type element name. */
  private static final String CTYPE = "content-type";
  /** Modified date element name. */
  private static final String MDATE = "modified-date";

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return exprs.length == 0 ? list(qc) : resources(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc);
  }

  /**
   * Lists details for all databases.
   * @param qc query context
   * @return iterator
   */
  private static Iter list(final QueryContext qc) {
    final Context ctx = qc.context;
    final StringList dbs = ctx.filter(Perm.READ, ctx.databases.listDBs());
    return new BasicIter<FNode>(dbs.size()) {
      @Override
      public FElem get(final long i) {
        final String name = dbs.get((int) i);
        final FElem database = new FElem(DATABASE);
        final MetaData meta = new MetaData(name, ctx.options, ctx.soptions);
        try {
          meta.read();
          // count number of raw files
          final int bin = new IOFile(ctx.soptions.dbPath(name), IO.RAW).descendants().size();
          database.add(RESOURCES, token(meta.ndocs + bin));
          database.add(MDATE, DateTime.format(new Date(meta.dbtime())));
          database.add(SIZE, token(meta.dbsize()));
          if(ctx.perm(Perm.CREATE, name)) database.add(PATH, meta.original);
        } catch(final IOException ignore) {
          // invalid database will be ignored
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
    final Data data = checkData(qc);
    final String path = string(exprs.length == 1 ? EMPTY : toToken(exprs[1], qc));
    final IntList docs = data.resources.docs(path);
    final TokenList bins = data.resources.binaries(path);
    final int ds = docs.size(), size = ds + bins.size();
    return new BasicIter<FNode>(size) {
      @Override
      public FNode get(final long i) {
        if(i < ds) {
          final int pre = docs.get((int) i);
          final byte[] pt = data.text(pre, true);
          final int sz = data.size(pre, Data.DOC);
          return resource(pt, false, sz, MediaType.APPLICATION_XML, data.meta.time);
        }
        if(i < size) {
          final byte[] pt = bins.get((int) i - ds);
          final IOFile io = data.meta.binary(string(pt));
          return resource(pt, true, io.length(), MediaType.get(io.path()), io.timeStamp());
        }
        return null;
      }
    };
  }

  /**
   * Creates a resource node.
   * @param path path
   * @param raw is the resource a raw file
   * @param size size
   * @param type media type
   * @param mdate modified date
   * @return resource  node
   */
  private static FNode resource(final byte[] path, final boolean raw, final long size,
      final MediaType type, final long mdate) {

    return new FElem(RESOURCE).add(path).add(RAW, token(raw)).add(CTYPE, type.toString()).
        add(MDATE, DateTime.format(new Date(mdate))).add(SIZE, token(size));
  }
}
