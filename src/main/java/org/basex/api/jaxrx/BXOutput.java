package org.basex.api.jaxrx;

import java.io.OutputStream;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
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
  public void write(final OutputStream os) {
    out = os;

    if(path != null) {
      try {
        // open database if a resource path was specified
        if(path.getDepth() != 0) cs.execute(new Open(path.getResourcePath()));
      } catch(final BaseXException ex) {
        throw new JaxRxException(404, ex.getMessage());
      }
      try {
        // set serialization parameters
        cs.execute(new Set(Prop.SERIALIZER, params(path)));
      } catch(final BaseXException ex) {
        throw new JaxRxException(400, ex.getMessage());
      }
    }
    run();
  }

  /**
   * Executes the specified command.
   * If command execution fails, an exception is thrown.
   * @param command command to be executed
   * @param os output stream, or {@code null}
   * @return result, or {@code null} if output stream was specified
   */
  final String exec(final Object command, final OutputStream os) {
    cs.setOutputStream(os);
    try {
      return cs.execute(command.toString());
    } catch(final BaseXException ex) {
      throw new JaxRxException(400, ex.getMessage());
    }
  }
}
