package org.basex.api.jaxrx;

import static org.basex.api.jaxrx.BXUtil.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.StreamingOutput;
import org.jaxrx.constants.EURLParameter;
import org.jaxrx.interfaces.IGet;

/**
 * This class offers an implementation of the JAX-RX 'get' operation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class BXGet implements IGet {
  @Override
  public Set<EURLParameter> getAvailableParams() {
    final Set<EURLParameter> p = new HashSet<EURLParameter>();
    p.add(EURLParameter.COMMAND);
    p.add(EURLParameter.COUNT);
    p.add(EURLParameter.OUTPUT);
    p.add(EURLParameter.QUERY);
    p.add(EURLParameter.START);
    p.add(EURLParameter.WRAP);
    return p;
  }

  @Override
  public StreamingOutput getResource(final String resource,
      final Map<EURLParameter, String> p) {
    return query(resource, p);
  }

  @Override
  public StreamingOutput getResourcesNames() {
    // could be merged with getResource()
    return getResource(null, new HashMap<EURLParameter, String>());
  }
}
