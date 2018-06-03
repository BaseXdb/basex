package org.basex.ws.response;

import static org.basex.ws.WebsocketText.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import org.basex.http.restxq.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.ws.*;
import org.basex.ws.WebsocketMessage.*;
import org.basex.ws.response.stomp.*;

/**
 * Represents the Serializer for the Stomp Subprotocol.
 * @author BaseX Team 2005-18, BSD License
 */
public class StompResponse implements WsResponse {

  /**
   * Checks the ParamName and sets it.
   * @param message The Websocket Message
   * @param var the var
   * @param qc the QueryContext
   * @param function The staticFunction
   * @return Value
   * @throws QueryException query exception
   * @throws UnsupportedEncodingException encoding exception
   */
  private Value checkParam(final WebsocketMessage message, final Var var,
                           final QueryContext qc, final StaticFunc function)
                               throws QueryException, UnsupportedEncodingException {
    final SeqType decl = var.declaredType();
    Value msg = null;
    if(var.name.toString().equals("destination")) {
      String header = message.getStompMessage().getHeaders().get("destination");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
              header, Strings.UTF8));
    } else if(var.name.toString().equals("id")) {
      String header = message.getStompMessage().getHeaders().get("id");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else if(var.name.toString().equals("transaction")) {
      String header = message.getStompMessage().getHeaders().get("transaction");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else if(var.name.toString().equals("accept-version")) {
      String header = message.getStompMessage().getHeaders().get("accept-version");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else if(var.name.toString().equals("host")) {
      String header = message.getStompMessage().getHeaders().get("host");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else if(var.name.toString().equals("ack")) {
      String header = message.getStompMessage().getHeaders().get("ack");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else if(var.name.toString().equals("receipt")) {
      String header = message.getStompMessage().getHeaders().get("receipt");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else if(var.name.toString().equals("login")) {
      String header = message.getStompMessage().getHeaders().get("login");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else if(var.name.toString().equals("passcode")) {
      String header = message.getStompMessage().getHeaders().get("passcode");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else if(var.name.toString().equals("heart-beat")) {
      String header = message.getStompMessage().getHeaders().get("heart-beat");
      header = header != null ? header : "";
      msg = new Atm(URLDecoder.decode(
          header, Strings.UTF8));
    } else {
      msg = new Atm(URLDecoder.decode(
          message.getStompMessage().getBody().toString(), Strings.UTF8));
    }

    return msg.seqType().instanceOf(decl) ? msg :
      decl.cast(msg, qc, function.sc, null);
  }

  @Override
  public void bind(final Expr[] args, final QueryContext qc, final WebsocketMessage message,
                   final ArrayList<RestXqParam> wsParameters, final StaticFunc function,
                   final WsXqFunction wsfunc, final Map<String, String> header)
      throws QueryException, UnsupportedEncodingException {

    // If no Message is provided (e.g. in the handshake) no stompframe is provided
    if((message != null) &&
       (message.getMsgType() != MESSAGETYPE.STOMP)) {
      throw new QueryException("Wrong Message Type in StompSerializer: " +
                                message.getMsgType() + "! Needed STOMP");
    }

    for(final RestXqParam rxp: wsParameters) {
      final Var[] params = function.params;
      final int pl = params.length;
      final MESSAGETYPE msgType = message.getMsgType();

      if(msgType == MESSAGETYPE.STOMP) {

        for(int p = 0; p < pl; p++) {
          // Parameter over the xqueryfuncs wit %ws:param annotation
          final Var var = params[p];
          if(var.name.eq(rxp.var)) {
            // if var.name.eq.("destination") { final Value val = destHeasder.seqType....}
            final Value val = checkParam(message, var, qc, function);
            args[p] = var.checkType(val, qc, false);
          }
        }
      }
      else if(msgType == MESSAGETYPE.BINARY) {
        // TODO: Bind the binary message
      } else {
        throw wsfunc.error(WRONG_MSG_TYPE, msgType);
      }
    }
  }


  // TODO output for STOMP!
  @Override
  public boolean generateOutput(final WebsocketConnection conn, final WsXqFunction wxf,
                                final QueryContext qc)
      throws IOException, QueryException {
    ArrayOutput ao = new ArrayOutput();
    Serializer ser = Serializer.get(ao, wxf.output);
    Iter iter = qc.iter();

    // Dont send anything if Websocket gets closed
    if(wxf.matches(Annotation._WS_CLOSE)) {
      return true;
    }

    StompFrame frame;
    // TODO: ADD RESPONSE HEADERS!
    for(Item it; (it = iter.next()) != null;) {
      ser.reset();
      ser.serialize(it);
      if(it instanceof Bin) {
        //final byte[] bytes = ((Bin) it).binary(null);
        final byte[] bytes = ao.toArray();
        conn.sess.getRemote().sendBytes(ByteBuffer.wrap(bytes));
      } else {
        if(wxf.matches(Annotation._WS_CONNECT)) {
          frame = new ConnectedFrame(Commands.CONNECTED, null, ao.toString());
        } else if(wxf.matches(Annotation._WS_MESSAGE)) {
          frame = new MessageFrame(Commands.MESSAGE, null, ao.toString());
        } else if(wxf.matches(Annotation._WS_ERROR)) {
          frame = new ErrorFrame(Commands.MESSAGE, null, ao.toString());
        } else {
          frame = new MessageFrame(Commands.MESSAGE, null, ao.toString());
        }
        conn.sess.getRemote().sendString(frame.serializedFrame());
      }
      ao.reset();
    }
    return true;
  }
}
