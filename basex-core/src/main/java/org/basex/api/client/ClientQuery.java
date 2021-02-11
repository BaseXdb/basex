package org.basex.api.client;

import java.io.*;

import org.basex.api.dom.*;
import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * This class defines all methods for iteratively evaluating queries with the
 * client/server architecture. All sent data is received by the
 * {@link ClientListener} and interpreted by the {@link ServerQuery}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ClientQuery extends Query {
  /** Client session. */
  final ClientSession cs;
  /** Query id. */
  final String id;

  /**
   * Standard constructor.
   * @param query query to be run
   * @param session client session
   * @param output output stream
   * @throws IOException I/O exception
   */
  ClientQuery(final String query, final ClientSession session, final OutputStream output)
      throws IOException {
    cs = session;
    out = output;
    id = session.exec(ServerCmd.QUERY, query, null);
  }

  @Override
  public String info() throws IOException {
    return cs.exec(ServerCmd.INFO, id, null);
  }

  @Override
  public String options() throws IOException {
    return cs.exec(ServerCmd.OPTIONS, id, null);
  }

  @Override
  public boolean updating() throws IOException {
    return Boolean.parseBoolean(cs.exec(ServerCmd.UPDATING, id, null));
  }

  @Override
  public void bind(final String name, final Object value, final String type) throws IOException {
    cache = null;

    final Object vl = value instanceof BXNode ? ((BXNode) value).getNode() : value;
    String t = type == null ? "" : type;
    final String v;
    if(vl instanceof Value) {
      final Value val = (Value) vl;
      final Type tp = val.type;
      if(t.isEmpty()) t = val.isEmpty() ? QueryText.EMPTY_SEQUENCE + "()" : tp.toString();

      try {
        final TokenBuilder tb = new TokenBuilder();
        for(final Item item : val) {
          if(!tb.isEmpty()) tb.addByte((byte) 1);
          if(item instanceof ANode) {
            tb.add(item.serialize(SerializerMode.NOINDENT.get()).finish());
          } else {
            tb.add(item.string(null));
          }
          if(item.type != tp) tb.addByte((byte) 2).add(item.type);
        }
        v = tb.toString();
      } catch(final QueryException ex) {
        throw new QueryIOException(ex);
      }
    } else {
      v = value.toString();
    }

    final ServerCmd cmd = name == null ? ServerCmd.CONTEXT : ServerCmd.BIND;
    final String n = name == null ? "" : name + '\0';
    cs.exec(cmd, id + '\0' + n + v + '\0' + t, null);
  }

  @Override
  public void context(final Object value, final String type) throws IOException {
    bind(null, value, type);
  }

  @Override
  public String execute() throws IOException {
    return cs.exec(ServerCmd.EXEC, id, out);
  }

  @Override
  public void close() throws IOException {
    cs.exec(ServerCmd.CLOSE, id, null);
  }

  @Override
  public void cache(final boolean full) throws IOException {
    cs.sout.write((full ? ServerCmd.FULL : ServerCmd.RESULTS).code);
    cs.send(id);
    cs.sout.flush();

    final BufferInput bi = BufferInput.get(cs.sin);
    cache(bi, full);
    if(!ClientSession.ok(bi)) throw new BaseXException(bi.readString());
  }
}
