package org.basex.query.util;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class contains data required for index operations.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class IndexContext {
  /** Data reference ({@code null} if root is set). */
  public final Data data;
  /** Root expression ({@code null} if data is set). */
  public final Expr root;
  /** Flag for iterative evaluation. */
  public final boolean iterable;

  /**
   * Constructor.
   * @param data data reference
   * @param iterable iterable flag
   */
  public IndexContext(final Data data, final boolean iterable) {
    this(data, null, iterable);
  }

  /**
   * Constructor.
   * @param root root expression
   * @param iterable iterable flag
   */
  public IndexContext(final Expr root, final boolean iterable) {
    this(null, root, iterable);
  }

  /**
   * Constructor.
   * @param data data reference (can be {@code null})
   * @param root root expression (can be {@code null})
   * @param iterable iterable flag
   */
  private IndexContext(final Data data, final Expr root, final boolean iterable) {
    this.data = data;
    this.root = root;
    this.iterable = iterable;
  }

  /**
   * Returns an expression for the index source for trace output.
   * @return expression
   */
  public Expr expr() {
    return data == null ? root : Str.get(data.meta.name);
  }

  /**
   * Returns a data reference.
   * @param qc query context
   * @param type index type
   * @param info input info
   * @return expression
   * @throws QueryException query exception
   */
  public Data data(final QueryContext qc, final IndexType type, final InputInfo info)
      throws QueryException {

    Data d = data;
    if(d == null) {
      final Value v = qc.value(root);
      d = v.data();
      if(d == null) throw BXDB_NOINDEX_X.get(info, v);
      if(!v.seqType().instanceOf(SeqType.DOC_ZM)) throw BXDB_DOC_X.get(info, v);
    }
    type.check(d, info);
    return d;
  }
}
