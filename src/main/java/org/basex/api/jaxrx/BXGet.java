package org.basex.api.jaxrx;

import static org.basex.api.jaxrx.BXUtil.*;
import static org.basex.util.Token.*;
import static org.jaxrx.constants.URLConstants.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.proc.List;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.server.ClientSession;
import org.basex.util.Table;
import org.basex.util.TokenList;
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

    return new StreamingOutput() {
      @Override
      public void write(final OutputStream out) {
        query(resource, out, p.get(EURLParameter.QUERY),
            p.get(EURLParameter.WRAP), p.get(EURLParameter.OUTPUT),
            p.get(EURLParameter.START), p.get(EURLParameter.COUNT));
      }
    };
  }

  @Override
  public StreamingOutput getResourcesNames() {
    return new StreamingOutput() {
      @Override
      public void write(final OutputStream out) {
        final ClientSession cs = session();
        run(cs, new Code() {
          @Override
          public void run() throws IOException {
            final XMLSerializer xml = new XMLSerializer(out);
            xml.openElement(token(JAXRX + ":results"));
            xml.namespace(token(JAXRX), token(URL));

            // retrieve list of databases
            final CachedOutput co = new CachedOutput();
            cs.execute(new List(), co);
            final Table table = new Table(co.toString());
            // loop through all databases
            for(final TokenList l : table.contents) {
              // [CG] check if database is a resource or collection
              xml.emptyElement(token(JAXRX + ":" + "resource"),
                  token("name"), l.get(0));
            }
            xml.closeElement();
            xml.close();
          }
        });
      }
    };
  }
}
