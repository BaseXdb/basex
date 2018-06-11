package org.basex.http.ws.response;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import org.basex.http.util.*;
import org.basex.http.ws.*;
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

/**
 * Represents the standard serializer for WebsocketMessages.
 * @author BaseX Team 2005-18, BSD License
 */
public class WsStandardResponse implements WsResponse {

  /**
   * Checks the ParamName and sets it.
   * @param header Map of header params
   * @param var the var
   * @param qc the QueryContext
   * @param function The staticFunction
   * @param decl the Seqtype
   * @return Value
   * @throws QueryException query exception
   * @throws UnsupportedEncodingException encoding exception
   */
  private Value checkParam(final Map<String, String> header, final Var var,
                           final QueryContext qc, final StaticFunc function,
                           final SeqType decl)
                               throws QueryException, UnsupportedEncodingException {
    Value msg = null;
    String headerParam = header.get(var.name.toString());
    if(headerParam != null) {
      msg =  new Atm(URLDecoder.decode(
          headerParam, Strings.UTF8));
    }
    if(msg == null) {
      return null;
    }
    return msg.seqType().instanceOf(decl) ? msg :
      decl.cast(msg, qc, function.sc, null);
  }

  @Override
  public void bind(final Expr[] args, final QueryContext qc,
                   final Object value, final ArrayList<WebParam> wsParameters,
                   final StaticFunc function, final WsFunction wsfunc,
                   final Map<String, String> header)
          throws QueryException, UnsupportedEncodingException {

    for(final WebParam rxp: wsParameters) {
      final Var[] params = function.params;
      final int pl = params.length;

      for(int p = 0; p < pl; p++) {
        final Var var = params[p];
        final Value val;
        if(var.name.eq(rxp.var)) {
          final SeqType decl = var.declaredType();
          if(var.name.toString().equals("message")) {
            final Value msg;
              if(value instanceof String) {
                msg = Str.get((String) value);
              } else if(value instanceof byte[]) {
                msg = B64.get((byte[]) value);
              } else {
                break;
              }
              val = msg.seqType().instanceOf(decl) ? msg :
                decl.cast(msg, qc, function.sc, null);
          } else {
            val = checkParam(header, var, qc, function, decl);
          }
          args[p] = var.checkType(val, qc, false);
        }
      }
    }
  }

  @Override
  public boolean create(final WsConnection conn,
                                final WsFunction wxf, final QueryContext qc)
                 throws IOException, QueryException {
    ArrayOutput ao = new ArrayOutput();
    Serializer ser = Serializer.get(ao, wxf.output);
    Iter iter = qc.iter();

    for(Item it; (it = iter.next()) != null;) {
      // Dont send anything if Websocket gets closed
      if(wxf.matches(Annotation._WS_CLOSE)) continue;

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
