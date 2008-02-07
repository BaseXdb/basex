package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.core.Prop;

/**
 * Evaluates the 'optimize' command. Optimizes the current database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Optimize extends Proc {
  @Override
  protected boolean exec() {
    // not quite finished ;)
    
    return Prop.info ? timer(DBOPTIMIZED) : true;
  }
}
