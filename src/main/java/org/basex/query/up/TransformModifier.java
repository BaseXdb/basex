package org.basex.query.up;

import static org.basex.query.util.Err.*;

import java.util.HashSet;
import java.util.Set;

import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.UpdatePrimitive;

/**
 * The Transform context modifier carries out updates of a single transform
 * expression. It especially keeps track of all nodes that are copied in the
 * 'copy' statement of a transform expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class TransformModifier extends ContextModifier {
  /** Holds all data references created by the copy clause of a transform
   * expression. Adding an update primitive that is declared within the modify
   * clause of this transform expression will cause a query exception
   * (XUDY0014) if the data reference of the corresponding target node is not
   * part of this set, hence the target node has not been copied.
   */
  private final Set<Data> refs;

  /**
   * Constructor.
   */
  public TransformModifier() {
    refs = new HashSet<Data>();
  }

  /**
   * Adds a data reference to list which keeps track of the nodes copied
   * within a transform expression.
   * @param d reference
   */
  public void addDataReference(final Data d) {
    refs.add(d);
  }

  @Override
  public void add(final UpdatePrimitive p, final QueryContext ctx)
  throws QueryException {
    super.add(p, ctx);

    /* Check if the target node of the given primitive has been copied in the
     * 'copy' statement of this transform expression.
     */
    if(!refs.contains(p.data))
      UPNOTCOPIED.thrw(p.input, p.getTargetDBNode());
  }
}
