package org.basex.query.up;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;

/**
 * The Transform context modifier carries out updates of a single transform
 * expression. It especially keeps track of all nodes that are copied in the
 * 'copy' statement of a transform expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class TransformModifier extends ContextModifier {
  /** Holds all data references created by the copy clause of a transform
   * expression. Adding an update primitive that is declared within the modify
   * clause of this transform expression will cause a query exception
   * (XUDY0014) if the data reference of the corresponding target node is not
   * part of this set, hence the target node has not been copied. */
  private final Set<Data> refs = new HashSet<Data>();

  /**
   * Adds a data reference to list which keeps track of the nodes copied
   * within a transform expression.
   * @param d reference
   */
  public void addData(final Data d) {
    refs.add(d);
  }

  @Override
  void add(final Operation o, final QueryContext ctx) throws QueryException {
    /* Disallow side-effecting updates within transform expressions.
     * Currently, also fn:put() is rejected
     * (future discussion: https://www.w3.org/Bugs/Public/show_bug.cgi?id=13970). */
    if(o instanceof BasicOperation) BASX_DBTRANSFORM.thrw(o.getInfo());

    add(o);
    /* check if the target node of the given primitive has been copied in the
     * 'copy' statement of this transform expression. */
    if(!refs.contains(o.getData())) UPNOTCOPIED.thrw(o.getInfo(), o.getTargetNode());
  }
}
