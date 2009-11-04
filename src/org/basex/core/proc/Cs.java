package org.basex.core.proc;

import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'cs' command and sets a new initial context set.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Cs extends AQuery {
  /**
   * Default constructor.
   * @param query query
   */
  public Cs(final String query) {
    super(DATAREF | User.READ, query);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    queryNodes();
    if(result == null) return false;
    if(result.size() != 0) context.current((Nodes) result);
    return true;
  }

  @Override
  public String toString() {
    return Cmd.CS + " " + args[0];
  }
}
