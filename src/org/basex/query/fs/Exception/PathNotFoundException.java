package org.basex.query.fs.Exception;

import static org.basex.Text.NL;

/**
 * Exception if File or Directory is not found.
 * 
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public final class PathNotFoundException extends RuntimeException {

  /** path expression. */ 
  private String path;
  
  /** name of program. */ 
  private String program;
  
  /**
   * The Constructor.
   */
  public PathNotFoundException() {
    super("No such file or directory" + NL);
  }
  
  /**
   * The Constructor.
   * 
   * @param prog - name of the program thrown the exception
   * @param pathexp the expression caused the exception
   * 
   */
  public PathNotFoundException(final String prog, final String pathexp) {
    super(prog + " No such file or directory \"" + pathexp + "\"" + NL);
    this.program = prog;
    this.path = pathexp;
  }

  /**
   * Get the path.
   * 
   * @return the path caused the exception
   */
  public String getPath() {
    return path;
  }

  /**
   * Get the program name.
   * 
   * @return name of the program thrown the exception
   */
  public String getProgram() {
    return program;
  }

  
}
