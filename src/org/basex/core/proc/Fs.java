package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;
import org.basex.query.fs.FSQuery;

/**
 * Filesystem commands.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Fs extends Proc {
  /** Create option. */
  public static final String DU = "du";
  /** Create option. */
  public static final String LS = "ls";
  /** Create option. */
  public static final String CD = "cd";
  /** Create option. */
  public static final String PWD = "pwd";
  /** Create option. */
  public static final String TOUCH = "touch";
  /** Create option. */
  public static final String RM = "rm";
  
  

  
  /** Filesystem query reference. */
  FSQuery query;
  /** Filesystem command. */
  String comm;
  
  @Override
  protected boolean exec() {
    query = new FSQuery(context);
    comm = cmd.arg(0).toLowerCase();
    return comm.equals(DU) || comm.equals(LS) || comm.equals(PWD) ||
      comm.equals(CD);
  }
  

  @Override
  protected void out(final PrintOutput out) throws IOException {
    if(comm.equals(DU)) {
      query.du(cmd.args(), out);
    } else if(comm.equals(LS)) {
      query.ls(cmd.args(), out);
    } else if(comm.equals(PWD)) {
      query.pwd(cmd.args(), out); 
    } else if(comm.equals(CD)) {      
      query.cd(cmd.args(), out);    
    }
    if(Prop.info) timer(PROCTIME);
  }
}
