package org.basex.query.func.db;

import static org.basex.query.func.db.DbAccess.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.resource.*;
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
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class DbList extends StandardFunc {

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return defined(0) ? resources(qc) : list(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return defined(0) ? resources(qc).value(qc, this) : list(qc);
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
    final Data data = toData(qc);
    final String path = defined(1) ? toString(arg(1), qc) : "";

    final IntList docs = data.resources.docs(path);
    final StringList binaries = data.resources.paths(path, ResourceType.BINARY);
    final StringList values = data.resources.paths(path, ResourceType.VALUE);
    final int ds = docs.size(), bs = ds + binaries.size(), size = bs + values.size();

    return new BasicIter<Str>(size) {
      @Override
      public Str get(final long i) {
        return i < size ? Str.get(path((int) i)) : null;
      }

      @Override
      public Value value(final QueryContext q, final Expr expr) throws QueryException {
        final TokenList tl = new TokenList(Seq.initialCapacity(size));
        for(int i = 0; i < size; i++) tl.add(path(i));
        return StrSeq.get(tl);
      }

      private byte[] path(final int i) {
        return i < ds ? data.text(docs.get(i), true) :
          Token.token(i < bs ? binaries.get(i - ds) : values.get(i - bs));
      }
    };
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (defined(0) ? dataLock(arg(0), false, visitor) : visitor.lock((String) null)) &&
        super.accept(visitor);
  }

  /**
   * Creates a directory element.
   * @param path path
   * @param mdate modified date
   * @return resource node
   */
  static FNode dir(final String path, final long mdate) {
    final String date = DateTime.format(new Date(mdate));
    return FElem.build(Q_DIR).add(path).add(Q_MODIFIED_DATE, date).finish();
  }

  /**
   * Creates a resource element.
   * @param path path to resource
   * @param mdate modified date
   * @param size size
   * @param type resource type
   * @return resource node
   */
  static FNode resource(final String path, final long mdate, final long size,
      final ResourceType type) {

    final FBuilder elem = FElem.build(Q_RESOURCE).add(path);
    elem.add(Q_TYPE, type);
    elem.add(Q_CONTENT_TYPE, type.contentType(path));
    elem.add(Q_MODIFIED_DATE, DateTime.format(new Date(mdate)));
    elem.add(Q_SIZE, size);
    return elem.finish();
  }
}
