package org.basex.query.func.db;

import static org.basex.util.Token.*;

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
 * @author BaseX Team 2005-22, BSD License
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
  /** Size. */
  static final String SIZE = "size";
  /** Type. */
  static final String TYPE = "type";
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
    final Data data = toData(qc);
    final String path = string(exprs.length == 1 ? EMPTY : toToken(exprs[1], qc));

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
    return (exprs.length == 0 ? visitor.lock((String) null) : dataLock(visitor, false, 0)) &&
        super.accept(visitor);
  }

  /**
   * Creates a directory element.
   * @param path path
   * @param mdate modified date
   * @return resource node
   */
  static FElem dir(final String path, final long mdate) {
    return new FElem(DIR).add(path).add(MODIFIED_DATE, DateTime.format(new Date(mdate)));
  }

  /**
   * Creates a resource element.
   * @param path path to resource
   * @param mdate modified date
   * @param size size
   * @param type resource type
   * @return resource node
   */
  static FElem resource(final String path, final long mdate, final long size,
      final ResourceType type) {

    final FElem elem = new FElem(RESOURCE).add(path);
    elem.add(TYPE, type.toString());
    elem.add(CONTENT_TYPE, type.contentType(path).toString());
    elem.add(MODIFIED_DATE, DateTime.format(new Date(mdate)));
    elem.add(SIZE, token(size));
    return elem;
  }
}
