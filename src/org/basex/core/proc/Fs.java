package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;
import org.basex.query.fs.*;

/**
 * Filesystem commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Fs extends Proc {
  /** Create option. */
  public static final String CAT = "cat";
  /** Create option. */
  public static final String CD = "cd";
  /** Create option. */
  public static final String CP = "cp";
  /** Create option. */
  public static final String DU = "du";
  /** Create option. */
  public static final String LOCATE = "locate";
  /** Create option. */
  public static final String LS = "ls";
  /** Create option. */
  public static final String MKDIR = "mkdir";
  /** Create option. */
  public static final String PWD = "pwd";
  /** Create option. */
  public static final String RM = "rm";
  /** Create option. */
  public static final String TOUCH = "touch";

  /** Filesystem command. */
  String comm;
  /** Filesystem command. */
  FSCmd fs;

  @Override
  protected boolean exec() {
    comm = cmd.arg(0).toLowerCase();

    if(comm.equals(CAT)) {
      fs = new CAT();
    } else if(comm.equals(CD)) {
      fs = new CD();
    } else if(comm.equals(CP)) {
      fs = new CP();
    } else if(comm.equals(DU)) {
      fs = new DU();
    } else if(comm.equals(LOCATE)) {
      fs = new LOCATE();
    } else if(comm.equals(LS)) {
      fs = new LS();
    } else if(comm.equals(MKDIR)) {
      fs = new MKDIR();
    } else if(comm.equals(PWD)) {
      fs = new PWD();
    } else if(comm.equals(RM)) {
      fs = new RM();
    } else if(comm.equals(TOUCH)) {
      fs = new TOUCH();
    }
    // unknown command...
    if(fs == null) throw new IllegalArgumentException();

    try {
      // evaluate arguments...
      fs.context(context);
      fs.args(cmd.args());
      return true;
    } catch(final FSException ex) {
      // internal command info..
      return error(ex.getMessage());
    }
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    try {
      fs.exec(cmd.args(), out);
      if(Prop.info) info(PROCTIME, perf.getTimer());
    } catch(final FSException ex) {
      // exception.. print as normal text
      out.println(ex.getMessage());
    }
  }
}
