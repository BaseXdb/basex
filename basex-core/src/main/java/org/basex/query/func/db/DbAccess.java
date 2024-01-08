package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class DbAccess extends StandardFunc {
  /** QName. */
  static final QNm Q_BACKUP = new QNm("backup");
  /** QName. */
  static final QNm Q_SIZE = new QNm("size");
  /** QName. */
  static final QNm Q_DATE = new QNm("date");
  /** QName. */
  static final QNm Q_DATABASE = new QNm("database");
  /** QName. */
  static final QNm Q_COMMENT = new QNm("comment");
  /** QName. */
  static final QNm Q_RESOURCE = new QNm("resource");
  /** QName. */
  static final QNm Q_RESOURCES = new QNm("resources");
  /** QName. */
  static final QNm Q_CONTENT_TYPE = new QNm("content-type");
  /** QName. */
  static final QNm Q_MODIFIED_DATE = new QNm("modified-date");
  /** QName. */
  static final QNm Q_DIR = new QNm("dir");
  /** QName. */
  static final QNm Q_TYPE = new QNm("type");
  /** QName. */
  static final QNm Q_PATH = new QNm("path");
  /** QName. */
  static final QNm Q_SYSTEM = new QNm("system");

  /**
   * Evaluates an expression to a normalized database path.
   * @param expr expression
   * @param qc query context
   * @return normalized path
   * @throws QueryException query exception
   */
  final String toDbPath(final Expr expr, final QueryContext qc) throws QueryException {
    return toDbPath(toString(expr, qc));
  }

  /**
   * Converts a path to a normalized database path.
   * @param path input path
   * @return normalized path
   * @throws QueryException query exception
   */
  final String toDbPath(final String path) throws QueryException {
    final String norm = MetaData.normPath(path);
    if(norm == null) throw RESINV_X.get(info, path);
    return norm;
  }

  /**
   * Evaluates an expression to a database name.
   * @param expr expression
   * @param empty allow empty name
   * @param qc query context
   * @return database name (empty string for general data)
   * @throws QueryException query exception
   */
  protected final String toName(final Expr expr, final boolean empty, final QueryContext qc)
      throws QueryException {
    return toName(expr, empty, DB_NAME_X, qc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(arg(0), false, visitor) && super.accept(visitor);
  }

  /**
   * Performs the attribute function.
   * @param expr expression (can be {@code Empty#UNDEFINED})
   * @param data data reference
   * @param ia index access
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  final Iter attribute(final Expr expr, final Data data, final IndexAccess ia,
      final QueryContext qc) throws QueryException {

    // no attribute specified: return iterator
    if(expr == Empty.UNDEFINED) return ia.iter(qc);

    // parse and compile the name test
    final byte[] name = toToken(expr, qc);
    final QNm qnm = qc.shared.qName(name, sc.ns.uri(prefix(name)));

    // return empty sequence if test will yield no results
    final NameTest nt = new NameTest(qnm, NamePart.FULL, NodeType.ATTRIBUTE, sc.elemNS);
    if(nt.optimize(data) == null) return Empty.ITER;

    // wrap iterator with name test
    final Iter iter = ia.iter(qc);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        Item item;
        while((item = qc.next(iter)) != null && !nt.matches(item));
        return item;
      }
    };
  }
}
