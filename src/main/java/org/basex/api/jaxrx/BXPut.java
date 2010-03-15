package org.basex.api.jaxrx;

import static org.basex.api.jaxrx.BXUtil.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.basex.core.proc.CreateDB;
import org.basex.server.ClientSession;
import org.jaxrx.interfaces.IPut;
import org.jaxrx.util.JAXRXException;

/**
 * This class offers an implementation of the JAX-RX 'put' operation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class BXPut implements IPut {
  @Override
  public void putResource(final String resource, final InputStream in) {
    final ClientSession cs = session();
    run(cs, new Code() {
      @Override
      public void run() throws IOException {
        // create database from cached file
        final File file = cache(in);
        final boolean ok = cs.execute(new CreateDB(file.toString(), resource));
        file.delete();
        // return exception if process failed
        if(!ok) throw JAXRXException.badRequest(cs.info());
      }
    });
  }
}
