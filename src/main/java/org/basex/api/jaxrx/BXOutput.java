package org.basex.api.jaxrx;

import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.Prop;
import org.basex.core.proc.Open;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.ResourcePath;

/**
 * Wrapper class for running JAX-RX code which creates output.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
abstract class BXOutput extends BXCode implements StreamingOutput {
  /** Optional path info. */
  final ResourcePath path;
  /** Output stream. */
  OutputStream out;
  
  /**
   * Constructor.
   * @param pt optional path info
   */
  BXOutput(final ResourcePath pt) {
    path = pt;
  }

  @Override
  public void write(final OutputStream os) throws IOException {
    out = os;
    if(path != null) {
      // open database if a single resource was specified
      if(path.depth() != 0 && !cs.execute(new Open(root(path)))) 
        throw new JaxRxException(404, cs.info());

      // set serialization parameters
      final String par = params(path);
      if(!cs.execute(new org.basex.core.proc.Set(Prop.SERIALIZER, par)))
        throw new JaxRxException(400, cs.info());
    }
    run();
  }

  /**
   * Executes the specified command. If command execution fails,
   * an exception is thrown.
   * @param command command to be executed
   * @param os output stream
   * @throws IOException I/O exception
   */
  void exec(final Object command, final OutputStream os) throws IOException {
    if(!cs.execute(command.toString(), os))
      throw new JaxRxException(400, cs.info());
  }
}
