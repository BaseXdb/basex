package org.basex.http.ws;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.http.restxq.*;
import org.basex.http.util.*;
import org.basex.http.ws.response.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class represents a single Websocket-Function.
 * @author BaseX Team 2005-18, BSD License
 * */
public class WsXqFunction extends WebFunction implements Comparable<WsXqFunction> {
  /**
   * Constructor.
   * @param function associated user function
   * @param qc query context
   * @param module associated module
   */
  public WsXqFunction(final StaticFunc function,
      final QueryContext qc, final RestXqModule module) {
    super(function, qc, module);
  }

  /** The Path of the WsXqFunction. */
  public WsPath path;

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
   * Checks if an WEbsocket request matches this Annotation and Path.
   * @param ann Annotation the annotation parameter
   * @param pPath The Path to compare to
   * @return result of check
   */
  public boolean matches(final Annotation ann, final WsPath pPath) {
    boolean found = false;
    for(Ann checkAnn : function.anns) {
      if((path != null) && (checkAnn.sig == ann) && (path.compareTo(pPath) == 0)) {
        found = true;
      }
    }
    return found;
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

      final Item[] args = ann.args();
      // If the Annotation is a ws:param(..) annotation: Add the Params to wsParameters
      switch(sig) {
        case _WS_PARAM:
          final String name = toString(args[0]);
          final QNm var = checkVariable(toString(args[1]));
          final int al = args.length;
          final ItemList items = new ItemList(al - 2);
          for(int a = 2; a < al; a++) {
            items.add(args[a]);
          }
          RestXqParam test = new RestXqParam(var, name, items.value());
          headerParams.add(test);
          break;
        case _WS_CLOSE:
        case _WS_CONNECT:
        case _WS_ERROR:
          if(args.length >= 2) {
            final QNm varId = checkVariable(toString(args[1]));
            RestXqParam id = new RestXqParam(varId, "id", null);
            headerParams.add(id);
          }
          path = new WsPath(toString(args[0]));
          break;
        case _WS_MESSAGE:
          if(args.length < 2) {
            throw new Exception("More Params required");
          }
          final QNm varMsg = checkVariable(toString(args[1]));
          RestXqParam msg = new RestXqParam(varMsg, "message", null);
          headerParams.add(msg);
          if(args.length > 2) {
            final QNm varId = checkVariable(toString(args[2]));
            RestXqParam id = new RestXqParam(varId, "id", null);
            headerParams.add(id);
          }
          path = new WsPath(toString(args[0]));
          break;
        default:
          break;
      }
    }

    return found;
  }

  /**
   * Processes the websocket request.
   * Parses new modules and discards obsolete ones.
   * @param conn Websocket connection
   * @param message The WebsocketMessage
   * @param serializer The Wsseriaizer
   * @param header The header to set
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  public boolean process(final WebsocketConnection conn,
                         final Object message,
                         final WsResponse serializer,
                         final Map<String, String> header) throws Exception {
    try {
      return module.process(conn, this, message, serializer, header);
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
  @Override
  public QueryException error(final String msg, final Object... ext) {
    return error(function.info, Util.info(msg, ext));
  }

  /**
   * Creates an exception with the specific message.
   * @param info the StaticFunc info
   * @param msg the message
   * @param ext error extension
   * @return exception
   */
  private QueryException error(final InputInfo info, final String msg, final Object... ext) {
    return BASEX_WSXQ_X.get(info, Util.info(msg, ext));
  }

  /**
   * Binds the params of the Function.
   * @param args The Expr
   * @param qc The QueryContext
   * @param message The Messagestring
   * @param response The Message response
   * @param header The header to set
   * @throws QueryException  query exception
   * @throws UnsupportedEncodingException encoding excepiton
   */
  public void bind(final Expr[] args, final QueryContext qc,
      final Object message, final WsResponse response,
      final Map<String, String> header) throws QueryException,
        UnsupportedEncodingException {
      response.bind(args, qc, message, headerParams, function, this, header);
  }
}
