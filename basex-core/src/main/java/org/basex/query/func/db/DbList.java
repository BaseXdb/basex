package org.basex.query.func.db;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class DbList extends StandardFunc {
  /** Resource element name. */
  static final String DATABASE = "database";
  /** Resource element name. */
  static final String RESOURCE = "resource";
  /** Resource element name. */
  static final String RESOURCES = "resources";
  /** Path. */
  static final String PATH = "path";
  /** Raw. */
  static final String RAW = "raw";
  /** Size. */
  static final String SIZE = "size";
  /** Content type. */
  static final String CONTENT_TYPE = "content-type";
  /** Modified date. */
  static final String MODIFIED_DATE = "modified-date";
  /** Directory flag. */
  static final String DIR = "dir";

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return exprs.length == 0 ? list(qc).iter() : resources(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs.length == 0 ? list(qc) : resources(qc).value(qc, this);
  }

  /**
   * Returns a list of all databases.
   * @param qc query context
   * @return databases
   */
  private static Value list(final QueryContext qc) {
    final Context ctx = qc.context;
    final StringList dbs = ctx.listDBs();
    final TokenList list = new TokenList(dbs.size());
    for(final String name : dbs) list.add(name);
    return StrSeq.get(list);
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

    return new BasicIter<Str>(size) {
      @Override
      public Str get(final long i) {
        return i < size ? Str.get(tokenAt((int) i)) : null;
      }
      @Override
      public Value value(final QueryContext q, final Expr expr) throws QueryException {
        final TokenList tl = new TokenList(Seq.initialCapacity(size));
        for(int i = 0; i < size; i++) tl.add(tokenAt(i));
        return StrSeq.get(tl);
      }
      private byte[] tokenAt(final int i) {
        return i < ds ? data.text(docs.get(i), true) : bins.get(i - ds);
      }
    };
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(exprs.length == 0) {
      if(!visitor.lock(null, false)) return false;
    } else {
      if(!dataLock(visitor, 0)) return false;
    }
    return super.accept(visitor);
  }

  /**
   * Creates a directory node.
   * @param path path
   * @param mdate modified date
   * @return resource node
   */
  static FNode dir(final byte[] path, final long mdate) {
    final FElem dir = new FElem(DIR);
    dir.add(path).add(MODIFIED_DATE, DateTime.format(new Date(mdate)));
    return dir;
  }

  /**
   * Creates a resource node.
   * @param path path
   * @param raw raw flag
   * @param type media type
   * @param mdate modified date
   * @param size size (can be {@code null})
   * @return resource node
   */
  static FNode resource(final byte[] path, final boolean raw, final MediaType type,
      final long mdate, final Long size) {

    final FElem resource = new FElem(RESOURCE).add(path);
    resource.add(RAW, token(raw));
    resource.add(CONTENT_TYPE, type.toString());
    resource.add(MODIFIED_DATE, DateTime.format(new Date(mdate)));
    if(size != null) resource.add(SIZE, token(size));
    return resource;
  }
}
