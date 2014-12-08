package org.basex.query.up;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.node.*;

/**
 * The Transform context modifier carries out updates of a single transform
 * expression. It especially keeps track of all nodes that are copied in the
 * 'copy' statement of a transform expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class TransformModifier extends ContextModifier {
  /** Holds all data references created by the copy clause of a transform
   * expression. Adding an update primitive that is declared within the modify
   * clause of this transform expression will cause a query exception
   * (XUDY0014) if the data reference of the corresponding target node is not
   * part of this set, hence the target node has not been copied. */
  private final Set<Data> refs = new HashSet<>();

  /**
   * Adds a data reference to list which keeps track of the nodes copied
   * within a transform expression.
   * @param d reference
   */
  public void addData(final Data d) {
    refs.add(d);
  }

  @Override
  void add(final Update up, final QueryContext qc) throws QueryException {
    // Disallow side-effecting updates within transform expressions.
    if(!(up instanceof NodeUpdate)) throw BASX_DBTRANSFORM.get(up.info());
    super.add(up, qc);

    // Check if the target node of the given primitive has been copied in the
    // 'copy' statement of this transform expression.
    final NodeUpdate nodeUp = (NodeUpdate) up;
    if(!refs.contains(nodeUp.data())) throw UPNOTCOPIED_X.get(nodeUp.info(), nodeUp.node());
  }
}
