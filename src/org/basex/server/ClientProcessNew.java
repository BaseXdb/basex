package org.basex.server;

import java.io.IOException;

import org.basex.core.AbstractProcess;
import org.basex.core.Context;
import org.basex.io.PrintOutput;

/**
 * This class sends client commands to the server instance over a socket.
 * It extends the {@link AbstractProcess} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class ClientProcessNew extends AbstractProcess {

  @SuppressWarnings("unused")
  @Override
  public boolean execute(final Context ctx) throws IOException {
    return false;
  }

  @SuppressWarnings("unused")
  @Override
  public void info(final PrintOutput out) throws IOException {
    
  }

  @SuppressWarnings("unused")
  @Override
  public void output(final PrintOutput out) throws IOException {
    
  }

}
