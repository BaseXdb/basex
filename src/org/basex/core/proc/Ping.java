package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'ping' command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Ping extends Proc {
  @Override
  protected boolean exec() {
    return true;
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    out.print(PINGINFO);
  }
}
