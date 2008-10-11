package org.basex.fs;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
import org.basex.util.Token;

/**
 * Performs a cat command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class Cat extends FSCmd {
  /** Line Feed. */
  private static final byte LF = 10;
  /**  Number the non-blank output lines, starting at 1.*/
  private boolean fnumberNonBlankLines;
  /** Number the output lines, starting at 1. */
  private boolean fnumberLines;
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args) throws FSException {
    // get all Options
    final GetOpts g = new GetOpts(args, "bn");
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'b':
          fnumberNonBlankLines = true;
          break;
        case 'n':
          fnumberLines = true;
          break;
      }
    }
    path = g.getPath();
    
    // no file/path was specified...
    if(path == null) error("", 100);
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    final int[] nodes = children(path);
    if(nodes.length == 1 && fs.isDir(nodes[0])) error(path, 21);
    cat(nodes, out);
  }

  /**
   * Performs a cat command.
   *
   *  @param nodes pre value of the files
   *  @param out output stream
   *  @throws IOException I/O exception
   */
  private void cat(final int[] nodes, final PrintOutput out)
      throws IOException {
    
    for(final int pre : nodes) {
      if(fs.isDir(pre)) continue;

      final IO io = IO.get(Token.string(fs.path(pre)));
      int numberLines = 1;
      if(io.exists()) {
        final byte[] content = io.content();
        byte lastChar = 0;
        for(int i = 0; i < content.length; ++i) {
          final byte c = content[i];
          if(fnumberLines || fnumberNonBlankLines) {
            // Firstline
            if(fnumberNonBlankLines && lastChar == 0 && c != LF) {
              out.print(numberLines++ + " ");
              out.print((char) c);
              lastChar = c;
            } else if(fnumberLines && numberLines == 1) {
              if(c == LF) {
                out.print(numberLines++ + " ");
                out.print((char) c);
                out.print(numberLines++ + " ");
              } else {
                out.print(numberLines++ + " ");
                out.print((char) c);
              }
              //  after line 1
            } else if (fnumberNonBlankLines && lastChar == LF && c != LF) {
              out.print(numberLines++ + " ");
              out.print((char) c);
              lastChar = c;
            } else if (fnumberLines && c == LF && i < content.length - 1) {
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
        error(io, 2);
      }
    }
  }
}
