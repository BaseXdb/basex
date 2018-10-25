package org.basex.http.ws.stomp;

import static org.basex.http.web.WebText.*;

import java.io.*;
import java.util.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.http.ws.*;
import org.basex.http.ws.stomp.frames.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Creates Stomp reponses.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public final class StompResponse extends WebResponse {
  /** WebSocket. */
  private WebSocket ws;

  /** Function. */
  private WsFunction func;

  /**
   * Constructor.
   * @param ws WebSocket instance
   */
  public StompResponse(final WebSocket ws) {
    super(ws.context);
    this.ws = ws;
  }

  @Override
  protected void init(final WebFunction function) throws QueryException, IOException {
    func = new WsFunction(function.function, qc, function.module);
    qc.putProperty(HTTPText.WEBSOCKET, ws);
    qc.putProperty(HTTPText.REQUEST, ws.req);
    qc.jc().type(WEBSOCKET);
    func.parse(ctx);
  }

  @Override
  protected void bind(final Expr[] args, final Object data) throws QueryException {
    func.bind(args, data, ws.headers, qc);
  }

  @Override
  public boolean serialize() throws QueryException, IOException {
    qc.register(ctx);
    try {
      for(@SuppressWarnings("unused") final Object value : serialize(qc.iter(), func.output)) {
        // Dont write anything back to the clients, the XQuery-User has to do this via
        // Ws-Module sendStomp for Sending Stomp-Message-Frames
        continue;
      }
    } finally {
      if(ws.headers.containsKey("receipt")) {
        Map<String, String> cHeader = new HashMap<>();
        cHeader.put("receipt-id", (String) ws.headers.get("receipt").toJava());
        StompFrame receiptFrame = new ReceiptFrame(Commands.RECEIPT, cHeader, "");
        ws.getSession().getRemote().sendStringByFuture(receiptFrame.serializedFrame());
      }
      qc.close();
      qc.unregister(ctx);
    }
    return true;
  }

  /**
   * Serializes an XQuery value.
   * @param iter value iterator
   * @param opts serializer options
   * @return serialized values (byte arrays and strings)
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  static ArrayList<Object> serialize(final Iter iter, final SerializerOptions opts)
      throws QueryException, IOException {

    final ArrayList<Object> list = new ArrayList<>();
    final ArrayOutput ao = new ArrayOutput();
    final Serializer ser = Serializer.get(ao, opts);
    for(Item item; (item = iter.next()) != null;) {
      ser.reset();
      ser.serialize(item);
      if(item instanceof Bin) {
        list.add(ao.toArray());
      } else {
        list.add(ao.toString());
      }
      ao.reset();
    }
    return list;
  }
}