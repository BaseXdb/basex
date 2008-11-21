package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Commands;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdFS;
import org.basex.fs.FSCmd;
import org.basex.fs.FSException;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'fs' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Fs extends Process {
  /** Filesystem command. */
  FSCmd fs;
  
  /**
   * Constructor.
   * @param a arguments
   */
  public Fs(final String... a) {
    super(DATAREF | UPDATING | PRINTING, a);
  }
  
  @Override
  protected boolean exec() {
    if(context.data().fs == null) return error(PROCNOFS);
    if(args.length == 0) return fsmode(true);
    
    try {
      final Commands.CmdFS cmd = CmdFS.valueOf(args[0]);
      if(cmd == Commands.CmdFS.EXIT) return fsmode(false);
      
      // evaluate arguments...
      fs = FSCmd.get(cmd);
      fs.context(context);
      fs.args(args[1] == null ? "" : args[1]);
      return true;
    } catch(final FSException ex) {
      // internal command info..
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }
  
  /**
   * Start/stop filesystem mode.
   * @param start start flag
   * @return true
   */
  private boolean fsmode(final boolean start) {
    Prop.fsmode = start;
    context.flush();
    return true;
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    try {
      if(fs != null) fs.exec(out);
      if(Prop.info) info(PROCTIME, perf.getTimer());
    } catch(final FSException ex) {
      // exception.. print as normal text
      out.println(ex.getMessage());
    }
  }
}
