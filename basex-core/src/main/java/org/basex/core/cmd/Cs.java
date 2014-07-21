package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.data.*;

/**
 * Evaluates the 'cs' command and sets a new initial context set.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Cs extends AQuery {
  /**
   * Default constructor.
   * @param query query
   */
  public Cs(final String query) {
    super(Perm.READ, true, query);
  }

  @Override
  protected boolean run() {
    final DBNodes nodes = dbNodes();
    if(nodes != null && nodes.size() != 0) {
      context.current(nodes);
      result = nodes;
    }
    return true;
  }
}
