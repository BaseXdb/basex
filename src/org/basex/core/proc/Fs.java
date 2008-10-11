package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Commands;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.Commands.FS;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.fs.Cat;
import org.basex.fs.Cd;
import org.basex.fs.Cp;
import org.basex.fs.Du;
import org.basex.fs.DataFS;
import org.basex.fs.Ext;
import org.basex.fs.FSCmd;
import org.basex.fs.FSException;
import org.basex.fs.Help;
import org.basex.fs.Locate;
import org.basex.fs.Ls;
import org.basex.fs.Mkdir;
import org.basex.fs.Pwd;
import org.basex.fs.Rm;
import org.basex.fs.Touch;
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
      final Commands.FS cmd = FS.valueOf(args[0]);
      if(cmd == Commands.FS.EXIT) return fsmode(false);
      
      // evaluate arguments...
      fs = cmd(cmd);
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
   * Returns an instance for the specified command.
   * @param cmd command
   * @return command
   */
  private FSCmd cmd(final Commands.FS cmd) {
    switch(cmd) {
      case CAT:     return new Cat();
      case CD:      return new Cd();
      case CP:      return new Cp();
      case DU:      return new Du();
      case HELP:    return new Help();
      case LOCATE:  return new Locate();
      case LS:      return new Ls();
      case MKDIR:   return new Mkdir();
      case PWD:     return new Pwd();
      case RM:      return new Rm();
      case TOUCH:   return new Touch();
      default:      return new Ext();
    }
  }
  
  /**
   * Start/stop filesystem mode.
   * @param start start flag
   * @return true
   */
  private boolean fsmode(final boolean start) {
    final Data data = context.data();
    context.current(start ? new Nodes(DataFS.ROOTDIR, data) :
      new Nodes(data.doc(), context.data()));
    Prop.fsmode = start;
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
