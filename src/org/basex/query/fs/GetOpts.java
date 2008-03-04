package org.basex.query.fs;

import static org.basex.Text.NL;

import java.io.IOException;

import org.basex.io.PrintOutput;

/**************************************************************************
 * This class splits the input String into its arguments und checks if 
 * there is a pathexpression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 * @version 0.1
/**************************************************************************/
public class GetOpts {

  
  /** Argument of an option is stored here. */
  private String optarg;
  
  /** Outputstream. */
  private final PrintOutput out;
  
  /** The path expression is stored here. */
  private String path;
  
  /** Index of option to be checked. */
  private int optindex = 0;
  
  /** The valid short options. */
  private String optString;
  
  /** Arguments passed to the program. */ 
  private String[] args;
  
  /** The name of the program. Is used in error messages.*/
  private String progname;
  
  /**
   * Construct a basic Getopt instance with the given input data.
   * 
   * @param arguments The String passed from the command line 
   *             
   * @param options A String containing a description of the 
   *                  valid options
   * @param output - the PrintOutputStream                   
   */
  public GetOpts(final String arguments, 
      final String options, final PrintOutput output) {
       
    this.args = arguments.split(" ");
    this.progname = this.args[0];
    this.optString = options;    
    this.optindex = 1;
    this.out = output;
  }
  
  /**
  * Getter of the index.
  * 
  * @return optindex - Index of the next option to be checked. Returns
  *                    -1 if it is at the end of the optString.      
  */
  public int getOptind() {
    return optindex;
  }
  
  /** 
  * For communication to the caller. No set method 
  * is provided because setting this variable has no effect.
  * 
  * @return When an option is found it is stored in optarg
  * and returned here.   
  */
  public String getOptarg() {
    return optarg;
  }
  
  /**
   * getPath is used to store a path expression.
   * 
   * @return path to go
   */
  public String getPath() {
    return path;
  }
  
  /**
  * This method checks the string passed from the command line. 
  * 
  * If an option is found it returns it and store possible arguments
  * in optarg. If an invalid option is found, a '?' is returned and an
  * error thrown. If there is no more to be checked -1 will be returned.
  * 
  * TODO<HS>: ERROR CODE -> EXCEPTION ?</HS>
  *  
  * @return see above
  * @throws IOException - in case of problems with the PrintOutput
  */
  public int getopt() throws IOException {
    
    optarg = null;
    path = null;    
    // parsed all input
    if (optindex >= args.length) {
      return -1;
    }
    
    // option found
    if(args[optindex].startsWith("-")) {
      // valid option ? 
      if(optString.indexOf(args[optindex].charAt(1)) > -1) {
        // return option - no argument handling atm 
        ++optindex;
        return args[optindex - 1].charAt(1);
      } else {
        out.print("ERROR: The Option -" + args[optindex].charAt(optindex) +
                  " is not avaiable in " + progname);
        out.print(NL);
        return '?';
      }             
    } else {      
      // path - no test 
      if(optindex == args.length - 1) {
        path = args[optindex];
        return -1;
      }
    }     
    return -1;
  }
}
