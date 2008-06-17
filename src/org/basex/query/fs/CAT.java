package org.basex.query.fs;

import static org.basex.Text.*;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
import org.basex.util.Token;
/**
 * Performs a cat command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public class CAT {

  /** Data reference. */
  private final Context context;

  /** Line Feed. */
  private final byte lf = 10;

  /** Carriage Return. */
  private final byte cr = 13;

  /** current dir. */
  private int curDirPre;

  /** PrintOutPutStream. */
  private PrintOutput out;

  /**  Number the non-blank output lines, starting at 1.*/
  private boolean fnumberNonBlankLines;

  /** Number the output lines, starting at 1. */
  private boolean fnumberLines;

  /** Shows if an error occurs. */
  private boolean fError;


  /**
   * Simplified Constructor.
   * @param ctx data context
   * @param output output stream
   */
  public CAT(final Context ctx, final PrintOutput output) {
    this.context = ctx;
    curDirPre = ctx.current().pre[0];
    this.out = output;
  }

  /**
   * Performs an cat command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void catMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "bhn", 1);
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {   
        case 'b':
          fnumberNonBlankLines = true;
          break;        
        case 'h':
          printHelp();
          break;
        case 'n':
          fnumberLines = true;
          break;          
        case ':':         
          fError = true;
          out.print("cd: missing argument");
          break;  
        case '?':         
          fError = true;
          out.print("cd: illegal option");
          break;
      }      
      if(fError) {
        // more options ?
        return;
      }
      ch = g.getopt();
    }
    int nodeToPrint = 0;
    // if there is path expression go to work
    if(g.getPath() != null) {    
      nodeToPrint = FSUtils.getSpecificFile(context.data(), 
          curDirPre, g.getPath().getBytes()); 
      if(nodeToPrint == -1) {
        out.print("cat:" + g.getPath() + ": No such file or directory");
      } else {
        if(FSUtils.isDir(context.data(), nodeToPrint)) {
          out.print("cat:" + g.getPath() + ": Is a directory");
        } else {
          cat(nodeToPrint);
        }
      }
    } 
  }
  /**
   * Performs a cat command.
   *  
   *  @param nodeToPrint The pre value of the file
   *  @throws IOException in case of problems with the PrintOutput 
   */
  private void cat(final int nodeToPrint) throws IOException {    
    IO io = new IO(Token.string(FSUtils.getPath(context.data(), nodeToPrint)));
    int numberLines = 1;
    if(io.exists()) {
      byte[] content = io.content();
      byte lastChar = 0;

      for(byte c : content) {
        if(fnumberLines || fnumberNonBlankLines) {          
          // Firstline
          if(fnumberNonBlankLines && lastChar == 0 && c != cr) { 
            out.print(numberLines++ + " ");
            out.print((char) c);
            lastChar = c;
          } else if(fnumberLines && numberLines == 1) {
            out.print(numberLines++ + " ");
            out.print((char) c);
            //  File after line 1
          } else if (fnumberNonBlankLines && lastChar == lf && c != cr) {
            out.print(numberLines++ + " ");
            out.print((char) c);
            lastChar = c;
          } else if (fnumberLines && c == lf) {
            out.print((char) c);
            out.print(numberLines++ + " ");            
          } else {
            out.print((char) c);
            lastChar = c;
          }  
        } else {
          out.print((char) c);
        }
      }
      out.print(NL);
    } else {
      out.print("cat:" + FSUtils.getFileName(context.data(), nodeToPrint) +
      ": Is a directory");
    }
  } 

  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print("cat ...");

  }
}
