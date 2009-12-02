package org.basex.core.proc;

import org.basex.core.Proc;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'refresh' command and closes and opens a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class Refresh extends Proc {
  /**
   * Default constructor.
   * @param name name of database
   */
  public Refresh(final String name) {
    super(STANDARD, name);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    if(context.data != null && context.data.meta.name.equals(args[0])) {
    new Close().execute(context, out);
    new Open(args[0]).execute(context, out);
    return true;
    }
    return false;
  }
}
