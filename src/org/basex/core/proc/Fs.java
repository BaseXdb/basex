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

  @Override
  protected boolean exec() {
    comm = cmd.arg(0).toLowerCase();
    return comm.equals(CAT) || comm.equals(CD) || comm.equals(CP)
    || comm.equals(DU) || comm.equals(LOCATE)
    || comm.equals(LS) || comm.equals(MKDIR) || comm.equals(PWD)
    || comm.equals(RM) || comm.equals(TOUCH);
  }


  @Override
  protected void out(final PrintOutput out) throws IOException {
    final FSQuery query = new FSQuery(context);
    final String args = cmd.args();

    if(comm.equals(CAT)) {
      query.cat(args, out);
    } else if(comm.equals(CD)) {
      query.cd(args, out);
    } else if(comm.equals(CP)) {
      query.cp(args, out);
    } else if(comm.equals(DU)) {
      query.du(args, out);
    } else if(comm.equals(LOCATE)) {
      query.locate(args, out);
    } else if(comm.equals(LS)) {
      query.ls(args, out);
    } else if(comm.equals(MKDIR)) {
      query.mkdir(args, out);
    } else if(comm.equals(PWD)) {
      query.pwd(args, out);
    } else if(comm.equals(RM)) {
      query.rm(args, out);
    } else if(comm.equals(TOUCH)) {
      query.touch(args, out);
    }
    if(Prop.info) timer(PROCTIME);
  }
}
