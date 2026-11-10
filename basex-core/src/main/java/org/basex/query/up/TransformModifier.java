package org.basex.query.up;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * The Transform context modifier carries out updates of a single transform
 * expression. It especially keeps track of all nodes that are copied in the
 * 'copy' statement of a transform expression.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class TransformModifier extends ContextModifier {
  /** Data references created by the copy clause. */
  private final Set<Data> refs;
  /** First error of a rejected update, raised after the modify clause (can be {@code null}). */
  private QueryException error;

  /**
   * Constructor.
   */
  TransformModifier() {
    this(null, new HashSet<>());
  }

  /**
   * Constructor.
   * @param outer outer context modifier (can be {@code null})
   * @param refs data references
   */
  private TransformModifier(final ContextModifier outer, final Set<Data> refs) {
    super(outer);
    this.refs = refs;
  }

  @Override
  public synchronized void addData(final Data data) {
    refs.add(data);
  }

  @Override
  ContextModifier nest() {
    return new TransformModifier(this, refs);
  }

  @Override
  synchronized void add(final Update up, final QueryContext qc) throws QueryException {
    // side-effecting updates and updates of nodes that have not been copied are rejected
    if(!(up instanceof final NodeUpdate nodeUp)) {
      reject(BASEX_UPDATE.get(up.info()));
    } else if(!refs.contains(nodeUp.data())) {
      reject(UPNOTCOPIED_X.get(nodeUp.info(), nodeUp.node()));
    } else {
      super.add(up, qc);
    }
  }

  @Override
  synchronized void addOutput(final Value value, final InputInfo info, final QueryContext qc) {
    reject(BASEX_UPDATE.get(info));
  }

  @Override
  synchronized void add(final ContextModifier nested) throws QueryException {
    super.add(nested);
    reject(((TransformModifier) nested).error);
  }

  /**
   * Records the error of a rejected update, unless an error has already been recorded.
   * @param ex error (can be {@code null})
   */
  private void reject(final QueryException ex) {
    if(error == null) error = ex;
  }

  @Override
  void check() throws QueryException {
    if(error != null) throw error;
  }
}
