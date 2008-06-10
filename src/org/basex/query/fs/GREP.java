package org.basex.query.fs;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;

/**
 * Performs a locate command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public final class GREP {

  /** Data reference. */
  private final Context context;

  /** Data reference. */
  private final Data data;

  /** current dir. */
  private int curDirPre;

  /** PrintOutPutStream. */
  private PrintOutput out;

  /** Shows if an error occurs. */
  private boolean fError;

  /** Shows if an error occurs. */
  private boolean fAccomplished;


  /**
   * Simplified Constructor.
   * @param ctx data context
   * @param output output stream
   */
  public GREP(final Context ctx, final PrintOutput output) {
    this.context = ctx;
    this.data = context.data();
    this.curDirPre = ctx.current().pre[0];
    this.out = output;
    this.fError = false;
    this.fAccomplished = false;
  }

  /**
   * Performs an GREP command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void grepMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "l:chV:", 1);
    char version = (char) -1;
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'V':
          version = g.getOptarg().charAt(0);
          break;          
        case 'h':
          printHelp();
          fAccomplished = true;
          break;
        case ':':         
          fError = true;
          out.print("ls: missing argument");
          break;  
        case '?':         
          fError = true;
          out.print("ls: illegal option");
          break;
      }      
      if(fError || fAccomplished) {
        // more options ?
        return;
      }
      ch = g.getopt();
    }


    // Version -  1 = use table
    //            2 = use xquery
    //            3 = use xquery + index
    switch (version) {
      case '1':
        grepTable();
        break;
      case '2':
      case '3':
        break;
      default:
        grepTable();
      break;
    }
  }

  /**
   *  Performs a grep command.
   *   
   * @throws IOException in case of problems with the PrintOutput 
   */
  private void grepTable() throws IOException {
    out.print("Perform grep: " + curDirPre + " " + data.size);
    
  }


  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print("help");
  }

}




