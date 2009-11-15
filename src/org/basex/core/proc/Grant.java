package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Commands.Cmd;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'grant' command and grants permissions to users.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Grant extends AAdmin {
  /**
   * Default constructor.
   * @param perm permission
   * @param name user name
   */
  public Grant(final Object perm, final String name) {
    this(perm, name, null);
  }

  /**
   * Constructor, specifying a certain database.
   * @param perm permission
   * @param name user name
   * @param db database
   */
  public Grant(final Object perm, final String name, final String db) {
    super(perm.toString(), name, db);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    return perm(true);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Cmd.GRANT + " " + args[0]);
    if(args[2] != null) sb.append(" " + ON + " " + args[2]);
    return sb.append(" " +  TO + " " + args[1]).toString();
  }
}
