package org.basex.ws.serializers;

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

/**
 * Represents the standard serializer for WebsocketMessages.
 * @author BaseX Team 2005-18, BSD License
 */
public class WsStandardSerializer implements WsSerializer {

  @Override
  public void bind(final Expr[] args, final QueryContext qc,
                   final WebsocketMessage message, final ArrayList<RestXqParam> wsParameters,
                   final StaticFunc function, final WsXqFunction wsfunc)
          throws QueryException, UnsupportedEncodingException {

    for(final RestXqParam rxp: wsParameters) {
      final Var[] params = function.params;
      final int pl = params.length;
      final MESSAGETYPE msgType = message.getMsgType();

      if(msgType == MESSAGETYPE.STRING) {
        Value test = new Atm(URLDecoder.decode(message.getStringMessage(), Strings.UTF8));
        for(int p = 0; p < pl; p++) {
          final Var var = params[p];
          if(var.name.eq(rxp.var)) {
            final SeqType decl = var.declaredType();
            final Value val = test.seqType().instanceOf(decl) ? test :
              decl.cast(test, qc, function.sc, null);
            args[p] = var.checkType(val, qc, false);
            break;
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

  @Override
  public boolean generateOutput(final WebsocketConnection conn,
                                final WsXqFunction wxf, final QueryContext qc)
                 throws IOException, QueryException {
    ArrayOutput ao = new ArrayOutput();
    Serializer ser = Serializer.get(ao, wxf.output);
    Iter iter = qc.iter();

    // Dont send anything if Websocket gets closed
    if(wxf.matches(Annotation._WS_CLOSE)) {
      return true;
    }

    for(Item it; (it = iter.next()) != null;) {
      ser.reset();
      ser.serialize(it);
      if(it instanceof Bin) {
        //final byte[] bytes = ((Bin) it).binary(null);
        final byte[] bytes = ao.toArray();
        conn.sess.getRemote().sendBytes(ByteBuffer.wrap(bytes));
      } else {
        conn.sess.getRemote().sendString(ao.toString());
      }
      ao.reset();
    }
    return true;
  }
}
