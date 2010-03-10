package org.basex.api.jaxrx;

import static org.basex.util.Token.*;
import static org.jaxrx.constants.URLConstants.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.data.XMLSerializer;
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
    final Set<EURLParameter> params = new HashSet<EURLParameter>();
    params.add(EURLParameter.COMMAND);
    params.add(EURLParameter.COUNT);
    params.add(EURLParameter.OUTPUT);
    params.add(EURLParameter.QUERY);
    params.add(EURLParameter.START);
    params.add(EURLParameter.WRAP);
    return params;
  }

  @Override
  public StreamingOutput getResource(final String resource,
      final Map<EURLParameter, String> params) {

    return new StreamingOutput() {
      @Override
      public void write(final OutputStream out) {
        BXUtil.query(resource, out,
            params.get(EURLParameter.QUERY),
            params.get(EURLParameter.WRAP),
            params.get(EURLParameter.OUTPUT),
            params.get(EURLParameter.START),
            params.get(EURLParameter.COUNT));
      }
    };
  }

  @Override
  public StreamingOutput getResourcesNames() {
    return new StreamingOutput() {
      @Override
      public void write(final OutputStream out) {
        final Context ctx = new Context();
        try {
          final XMLSerializer xml = new XMLSerializer(out);
          xml.openElement(token(JAXRX + ":results"));
          xml.namespace(token(JAXRX), token(URL));

          for(final String db : List.list(ctx)) {
            try {
              new Open(db).execute(ctx);
              xml.emptyElement(token(JAXRX + ":" +
                  (ctx.current.size() > 1 ? "collection" : "resource")),
                  token("name"), token(db));
            } catch(final BaseXException ex) {
              // ignore invalid databases
            }
          }
          xml.closeElement();
          xml.close();
        } catch(final IOException ex) {
          BXUtil.error(ex);
        } finally {
          ctx.close();
        }
      }
    };
  }
}
