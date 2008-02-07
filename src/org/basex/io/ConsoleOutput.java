package org.basex.io;

import java.io.IOException;
import java.io.PrintStream;

/**
 * This class is a stream-wrapper for textual data. Note that the internal
 * byte representation (usually UTF8) will be directly output without
 * further character conversion.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class ConsoleOutput extends PrintOutput {
  /**
   * Constructor given a print stream.
   * @param out the PrintStream to operate on
   */
  public ConsoleOutput(final PrintStream out) {
    super(new BufferedOutput(out));
  }
  
  @Override
  public void close() throws IOException {
    os.flush();
  }
}
