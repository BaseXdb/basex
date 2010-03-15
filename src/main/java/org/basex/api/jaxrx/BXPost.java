package org.basex.api.jaxrx;

import static org.basex.api.jaxrx.BXUtil.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.proc.Add;
import org.basex.core.proc.Open;
import org.basex.server.ClientSession;
import org.jaxrx.constants.EURLParameter;
import org.jaxrx.interfaces.IPost;

/**
 * This class offers an implementation of the JAX-RX 'post' operation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class BXPost implements IPost {

  @Override
  public void postResource(final String resource, final InputStream in) {
    final ClientSession cs = session();
    run(cs, new Code() {
      @Override
      public void run() throws IOException {
        // open database
        if(!cs.execute(new Open(resource))) notFound(cs.info());
        // add cached file to the database
        final File file = cache(in);
        final boolean ok = cs.execute(new Add(file.toString()));
        file.delete();
        // return exception if process failed
        if(!ok) badRequest(cs.info());
      }
    });
  }

  @Override
  public StreamingOutput postResource(final String resource,
      final Map<EURLParameter, String> queryParams) {
    return query(resource, queryParams);
  }
}
