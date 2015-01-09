package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
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
    if(exprs.length == 0) return listDBs(qc);

    final Data data = checkData(qc);
    final String path = string(exprs.length == 1 ? Token.EMPTY : toToken(exprs[1], qc));
    final IntList il = data.resources.docs(path);
    final TokenList tl = data.resources.binaries(path);

    return new Iter() {
      final int is = il.size(), ts = tl.size();
      int ip, tp;
      @Override
      public ANode get(final long i) {
        if(i < is) {
          final byte[] pt = data.text(il.get((int) i), true);
          return resource(pt, false, 0, token(MimeTypes.APP_XML), data.meta.time);
        }
        if(i < is + ts) {
          final byte[] pt = tl.get((int) i - is);
          final IOFile io = data.meta.binary(string(pt));
          return resource(pt, true, io.length(), token(MimeTypes.get(io.path())),
              io.timeStamp());
        }
        return null;
      }
      @Override
      public ANode next() {
        return ip < is ? get(ip++) : tp < ts ? get(ip + tp++) : null;
      }
      @Override
      public long size() { return ip + is; }
    };
  }

  /**
   * Performs the list-details for databases function.
   * @param qc query context
   * @return iterator
   */
  private Iter listDBs(final QueryContext qc) {
    final Context ctx = qc.context;
    final StringList sl = ctx.databases.listDBs();
    return new Iter() {
      int pos;
      @Override
      public ANode get(final long i) throws QueryException {
        final String name = sl.get((int) i);
        final MetaData meta = new MetaData(name, ctx.options, ctx.soptions);
        try {
          meta.read();
        } catch(final IOException ex) {
          throw BXDB_OPEN_X.get(info, ex);
        }

        final FElem res = new FElem(DATABASE);
        res.add(RESOURCES, token(meta.ndocs.intValue()));
        res.add(MDATE, DateTime.format(new Date(meta.dbtime()), DateTime.FULL));
        res.add(SIZE, token(meta.dbsize()));
        if(ctx.perm(Perm.CREATE, name)) res.add(PATH, meta.original);
        res.add(name);
        return res;
      }
      @Override
      public ANode next() throws QueryException { return pos < size() ? get(pos++) : null; }
      @Override
      public long size() { return sl.size(); }
    };
  }

  /**
   * Create a <code>&lt;resource/&gt;</code> node.
   * @param path path
   * @param raw is the resource a raw file
   * @param size size
   * @param ctype content type
   * @param mdate modified date
   * @return <code>&lt;resource/&gt;</code> node
   */
  private static FNode resource(final byte[] path, final boolean raw, final long size,
      final byte[] ctype, final long mdate) {

    final String tstamp = DateTime.format(new Date(mdate), DateTime.FULL);
    final FElem res = new FElem(RESOURCE).add(path).
        add(RAW, token(raw)).add(CTYPE, ctype).add(MDATE, tstamp);
    return raw ? res.add(SIZE, token(size)) : res;
  }
}
