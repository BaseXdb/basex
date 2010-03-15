package org.basex.api.jaxrx;

import static org.basex.api.jaxrx.BXUtil.*;
import java.io.IOException;
import org.basex.core.proc.DropDB;
import org.basex.server.ClientSession;
import org.jaxrx.interfaces.IDelete;
import org.jaxrx.util.JAXRXException;

/**
 * This class offers an JAX-RX implementation to answer DELETE requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class BXDelete implements IDelete {
  @Override
  public void deleteResource(final String resource) {
    final ClientSession cs = session();
    run(cs, new Code() {
      @Override
      public void run() throws IOException {
        if(!cs.execute(new DropDB(resource)))
          throw new JAXRXException(404, cs.info());
      }
    });
  }
}
