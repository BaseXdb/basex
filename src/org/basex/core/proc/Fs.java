package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.Commands.FS;
import org.basex.io.PrintOutput;
import org.basex.query.fs.CAT;
import org.basex.query.fs.CD;
import org.basex.query.fs.CP;
import org.basex.query.fs.DU;
import org.basex.query.fs.FSCmd;
import org.basex.query.fs.FSException;
import org.basex.query.fs.LOCATE;
import org.basex.query.fs.LS;
import org.basex.query.fs.MKDIR;
import org.basex.query.fs.PWD;
import org.basex.query.fs.RM;
import org.basex.query.fs.TOUCH;

/**
 * Evaluates the 'close' command. Removes the current database from
 * memory and releases memory resources.
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
    try {
      // evaluate arguments...
      fs = cmd(args[0]);
      fs.context(context);
      fs.args(args[1] == null ? "" : args[1], 0);
      return true;
    } catch(final FSException ex) {
      // internal command info..
      return error(ex.getMessage());
    }
  }
  
  /**
   * Returns a filesystem command for the specified argument.
   * @param c command
   * @return command
   */
  private FSCmd cmd(final String c) {
    switch(FS.valueOf(c)) {
      case CAT:     return new CAT();
      case CD:      return new CD();
      case CP:      return new CP();
      case DU:      return new DU();
      case LOCATE:  return new LOCATE();
      case LS:      return new LS();
      case MKDIR:   return new MKDIR();
      case PWD:     return new PWD();
      case RM:      return new RM();
      case TOUCH:   return new TOUCH();
      default:      return null;
    }
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    try {
      fs.exec(out);
      if(Prop.info) info(PROCTIME, perf.getTimer());
    } catch(final FSException ex) {
      // exception.. print as normal text
      out.println(ex.getMessage());
    }
  }
}
