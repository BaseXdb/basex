package org.basex.http.ws;

import static org.basex.http.web.WebText.*;

import java.io.*;
import java.nio.*;
import java.util.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * Creates WebSocket responses.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Johannes Finckh
 */
public final class WsResponse extends WebResponse {
  /** WebSocket. */
  private final WebSocket ws;

  /** Function. */
  private WsFunction func;

  /**
   * Constructor.
   * @param ws WebSocket instance
   */
  WsResponse(final WebSocket ws) {
    super(ws.context);
    this.ws = ws;
  }

  @Override
  protected void init(final WebFunction function) throws QueryException {
    func = new WsFunction(function.function, qc, function.module);
    ctx.setExternal(ws);
    ctx.setExternal(new RequestContext(ws.request));
    qc.jc().type(WEBSOCKET);
    func.parse(ctx);
  }

  @Override
  protected void bind(final Expr[] args, final Object data) throws QueryException {
    func.bind(args, data, ws.headers, qc);
  }

  @Override
  public Response serialize(final boolean body) throws QueryException, IOException {
    qc.register(ctx);
    try {
      final ArrayList<Object> values = serialize(qc.iter(), func.output);
      // don't send anything if the WebSocket connection has been closed
      if(!func.matches(Annotation._WS_CLOSE, null) &&
         !func.matches(Annotation._WS_ERROR, null)) {
        for(final Object value : values) {
          final RemoteEndpoint remote = ws.getSession().getRemote();
          if(value instanceof ByteBuffer) {
            remote.sendBytes((ByteBuffer) value);
          } else {
            remote.sendString((String) value);
          }
        }
      }
    } finally {
      qc.close();
      qc.unregister(ctx);
    }
    return Response.STANDARD;
  }

  /**
   * Serializes an XQuery value.
   * @param iter value iterator
   * @param opts serializer options
   * @return serialized values (byte arrays and strings)
   * @throws QueryException query exception
   * @throws QueryIOException query I/O exception
   */
  static ArrayList<Object> serialize(final Iter iter, final SerializerOptions opts)
      throws QueryException, QueryIOException {

    final ArrayList<Object> list = new ArrayList<>();
    final SerialMethod method = opts.get(SerializerOptions.METHOD);
    for(Item item; (item = iter.next()) != null;) {
      // serialize maps and arrays as JSON
      final boolean json = method == SerialMethod.BASEX && item instanceof FItem;
      opts.set(SerializerOptions.METHOD, json ? SerialMethod.JSON : method);
      // interpret result as binary or string
      final ArrayOutput ao = item.serialize(opts);
      list.add(item instanceof Bin ? ByteBuffer.wrap(ao.toArray()) : ao.toString());
    }
    return list;
  }
}