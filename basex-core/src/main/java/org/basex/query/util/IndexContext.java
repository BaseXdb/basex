package org.basex.query.util;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class contains data required for index operations.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class IndexContext {
  /** Data reference ({@code null} if root is set). */
  public final Data data;
  /** Flag for iterative evaluation. */
  public final boolean iterable;
  /** Input expression yielding a database ({@code null} if data is set). */
  public Expr input;

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
   * @param input input expression
   * @param iterable iterable flag
   */
  public IndexContext(final Expr input, final boolean iterable) {
    this(null, input, iterable);
  }

  /**
   * Constructor.
   * @param data data reference (can be {@code null})
   * @param input input expression (can be {@code null})
   * @param iterable iterable flag
   */
  public IndexContext(final Data data, final Expr input, final boolean iterable) {
    this.data = data;
    this.input = input;
    this.iterable = iterable;
  }

  /**
   * Returns an expression for the index source.
   * @return expression
   */
  public Expr input() {
    return data == null ? input : Str.get(data.meta.name);
  }

  /**
   * Copies the index context.
   * @param cc compilation context
   * @param vm mapping from old variable IDs to new variable copies.
   * @return copied context
   */
  public IndexContext copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new IndexContext(data, input == null ? null : input.copy(cc, vm), iterable);
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
      final Value v = qc.value(input);
      d = v.data();
      if(d == null) throw BXDB_NOINDEX_X.get(info, v);
      if(!v.seqType().instanceOf(SeqType.DOC_ZM)) throw BXDB_DOC_X.get(info, v);
    }
    type.check(d, info);
    return d;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof IndexContext)) return false;
    final IndexContext ic = (IndexContext) obj;
    return data == ic.data && Objects.equals(input, ic.input) && iterable == ic.iterable;
  }
}
