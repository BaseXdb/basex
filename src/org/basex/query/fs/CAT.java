package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
import static org.basex.Text.NL;
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
          return;
        case 'n':
          fnumberLines = true;
          break;          
        case ':':         
          FSUtils.printError(out, "cat", g.getPath(), 99);
          return;  
        case '?':         
          FSUtils.printError(out, "cat", g.getPath(), 22);
          return;
      }      

      ch = g.getopt();
    }
    int[] nodeToPrint;
    // if there is path expression go to work
    if(g.getPath() != null) {    
      nodeToPrint = FSUtils.getSpecificFilesOrDirs(context.data(), 
          curDirPre, g.getPath()); 
      if(nodeToPrint.length == 1 && nodeToPrint[0] == -1) {
        FSUtils.printError(out, "cat", g.getPath(), 2);
      } else {
        if(nodeToPrint.length == 1 && 
            FSUtils.isDir(context.data(), nodeToPrint[0])) {
          FSUtils.printError(out, "cat", g.getPath(), 21);
        } else {
          cat(nodeToPrint);
        }
      }
    } 
  }
  /**
   * Performs a cat command.
   *  
   *  @param print The pre value of the file
   *  @throws IOException in case of problems with the PrintOutput 
   */
  private void cat(final int[] print) throws IOException {    
    for(int j : print) {
      int nodeToPrint = print[j];
      if(FSUtils.isDir(context.data(), nodeToPrint)) { 
        continue;
      }
      IO io = new IO(Token.string(
          FSUtils.getPath(context.data(), nodeToPrint)));
      int numberLines = 1;
      if(io.exists()) {
        byte[] content = io.content();
        byte lastChar = 0;
        for(int i = 0; i < content.length; ++i) {
          byte c = content[i];
          if(fnumberLines || fnumberNonBlankLines) {          
            // Firstline
            if(fnumberNonBlankLines && lastChar == 0 && c != cr && c != lf) { 
              out.print(numberLines++ + " ");
              out.print((char) c);
              lastChar = c;
            } else if(fnumberLines && numberLines == 1) {
              if(c == lf) {
                out.print(numberLines++ + " ");
                out.print((char) c);
                out.print(numberLines++ + " ");
              } else {
                out.print(numberLines++ + " ");
                out.print((char) c);
              }
              //  after line 1
            } else if (fnumberNonBlankLines && lastChar == lf &&
                c != cr && c != lf) {
              out.print(numberLines++ + " ");
              out.print((char) c);
              lastChar = c;
            } else if (fnumberLines && c == lf && i < content.length - 1) {
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
        FSUtils.printError(out, "cat", 
            FSUtils.getFileName(context.data(), nodeToPrint), 21);        
      }
    }
  } 

  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print(FSCAT);
  }
}
