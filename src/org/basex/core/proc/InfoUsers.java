package org.basex.core.proc;


import java.io.IOException;

import org.basex.io.PrintOutput;

/**
 * Evaluates the 'info users' command and returns user information.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class InfoUsers extends AInfo {
  
  /**
   * Default constructor.
   */
  public InfoUsers() {
    super(PRINTING);
  }
  
  @Override
  protected void out(final PrintOutput out) throws IOException {
    out.println(context.users.info());
  }

}
