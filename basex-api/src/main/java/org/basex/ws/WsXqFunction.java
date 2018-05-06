package org.basex.ws;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.ws.WebsocketText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import org.basex.http.restxq.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.ws.WebsocketMessage.*;

/**
 * This class represents a single Websocket-Function.
 * @author BaseX Team 2005-18, BSD License
 * */
public class WsXqFunction implements Comparable<WsXqFunction> {

  /** Single template pattern. */
  private static final Pattern TEMPLATE = Pattern.compile("\\s*\\{\\s*\\$(.+?)\\s*}\\s*");
  /** Associated function. */
  public final StaticFunc function;
  /** Associated module. */
  private final RestXqModule module;
  /** Serialization parameters. */
  public final SerializerOptions output;
  /** Parameters of the Websocket Function. */
  final ArrayList<RestXqParam> wsParameters = new ArrayList<>();

  /**
   * Constructor.
   * @param function associated user function
   * @param module associated module
   * @param qc QueryContext
   */
  public WsXqFunction(final StaticFunc function, final QueryContext qc, final RestXqModule module) {
    this.function = function;
    this.module = module;
    output = qc.serParams();
  }

  /**
   * Checks if an WEbsocket request matches this Annotation.
   * @param ann Annotation the annotation parameter
   * @return result of check
   */
  public boolean matches(final Annotation ann) {
    boolean found = false;
    for(Ann checkAnn : function.anns) {
      if(checkAnn.sig == ann) {
        found = true;
      }
    }
    return found;
  }

  /**
   * Returns the specified item as a string.
   * @param item item
   * @return string
   * */
  static String toString(final Item item) {
    return ((Str) item).toJava();
  }

  /**
   * Checks the specified Template.
   * @param tmp template string
   * @return resulting variable
   * @throws QueryException query exception
   * TODO: Declared?
   * */
  QNm checkVariable(final String tmp) throws QueryException {
    final Matcher m = TEMPLATE.matcher(tmp);
    if(!m.find()) throw error(INV_TEMPLATE_X, tmp);
    final byte[] vn = token(m.group(1));
    if(!XMLToken.isQName(vn)) throw error(INV_VARNAME_X, vn);
    return new QNm(vn);
  }

  /**
   * Checks a function for Websocket and permission Annotations.
   * @return {@code true} if function contains relevant annotations
   * @throws Exception exception
   */
  public boolean parse() throws Exception {
    boolean found = false;

    for(final Ann ann : function.anns) {
      final Annotation sig = ann.sig;
      if(sig == null) continue;

      found |= eq(sig.uri, QueryText.WS_URI);

      // If the Annotation is a ws:param(..) annotation: Add the Params to wsParameters
      if(sig == Annotation._WS_PARAM) {
        final Item[] args = ann.args();
        final String name = toString(args[0]);

        final QNm var = checkVariable(toString(args[1]));

        final int al = args.length;
        final ItemList items = new ItemList(al - 2);
        for(int a = 2; a < al; a++) {
          items.add(args[a]);
        }

        RestXqParam test = new RestXqParam(var, name, items.value());
        wsParameters.add(test);
      }
    }

    return found;
  }

  /**
   * Processes the websocket request.
   * Parses new modules and discards obsolete ones.
   * @param conn Websocket connection
   * @param message The WebsocketMessage
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  public boolean process(final WebsocketConnection conn,
      final WebsocketMessage message) throws Exception {
    try {
      return module.process(conn, this, message);
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(function.info);
      throw ex;
    }
  }

  @Override
  public int compareTo(final WsXqFunction wsxf) {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return exception
   */
  private QueryException error(final String msg, final Object... ext) {
    InputInfo info = function.info;
    return BASEX_WSXQ_X.get(info, Util.info(msg, ext));
  }

  /**
   * Binds the params of the Function.
   * @param args The Expr
   * @param qc The QueryContext
   * @param message The Messagestring
   * @throws QueryException  query exception
   * @throws UnsupportedEncodingException encoding excepiton
   */
  public void bind(final Expr[] args, final QueryContext qc,
      final WebsocketMessage message) throws QueryException,
        UnsupportedEncodingException {
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
          throw error(WRONG_MSG_TYPE, msgType);
        }
    }
  }
}
