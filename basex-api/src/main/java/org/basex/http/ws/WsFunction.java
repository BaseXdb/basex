package org.basex.http.ws;

import static org.basex.http.web.WebText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.http.restxq.*;
import org.basex.http.web.*;
import org.basex.http.ws.response.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class represents a single Websocket-Function.
 * @author BaseX Team 2005-18, BSD License
 */
public class WsFunction extends WebFunction implements Comparable<WsFunction> {
  /** Associated module. */
  private final RestXqModule module;

  /**
   * The Constructor.
   * @param function associated user function
   * @param qc query context
   * @param module associated module
   */
  public WsFunction(final StaticFunc function, final QueryContext qc, final RestXqModule module) {
    super(function, qc);
    this.module = module;
  }

  /** The Path of the WsFunction. */
  public WsPath path;

  /**
   * Checks if an Websocket request matches this Annotation.
   * @param ann Annotation the annotation parameter
   * @return boolean Result of the Check
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
   * Checks if an Websocket request matches this Annotation and Path.
   * @param ann Annotation the annotation parameter
   * @param pPath The Path to compare to
   * @return boolean Result of the check
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
   * Checks a function for Websocket Annotations.
   * @return {@code true} if function contains relevant annotations
   * @throws QueryException exception
   */
  public boolean parse() throws QueryException {
    boolean found = false;
    final boolean[] declared = new boolean[function.params.length];
    // Counts the Annotations which should occur only once
    int countOccureOnce = 0;
    // Checks if a _WS_PARAM Annotation is used
    boolean wsParamUsed = false;

    for(final Ann ann : function.anns) {
      final Annotation sig = ann.sig;
      if(sig == null) continue;

      found |= eq(sig.uri, QueryText.WS_URI);

      final Item[] args = ann.args();
      // If the Annotation is a ws:param(..) annotation: Add the Params to wsParameters
      switch(sig) {
        case _WS_PARAM:
          final String name = toString(args[0]);
          final QNm var = checkVariable(toString(args[1]), declared);
          final int al = args.length;
          final ItemList items = new ItemList(al - 2);
          for(int a = 2; a < al; a++) {
            items.add(args[a]);
          }
          WebParam test = new WebParam(var, name, items.value());
          headerParams.add(test);
          wsParamUsed = true;
          break;
        case _WS_CLOSE:
        case _WS_CONNECT:
          path = new WsPath(toString(args[0]));
          countOccureOnce++;
          break;
        case _WS_ERROR:
        case _WS_MESSAGE:
          if(args.length < 2) {
            throw error(PARAM_MISSING_X, "path,message");
          }
          final QNm varMsg = checkVariable(toString(args[1]), declared);
          WebParam msg = new WebParam(varMsg, "message", null);
          headerParams.add(msg);
          path = new WsPath(toString(args[0]));
          countOccureOnce++;
          break;
        default:
          break;
      }
    }

    if(found) {
      if(path == null) throw error(function.info, ANN_MISSING);
      if(countOccureOnce > 1 || ((countOccureOnce != 1) && wsParamUsed))
        throw error(function.info, ANN_CONFLICT);
      final int dl = declared.length;
      for(int d = 0; d < dl; d++) {
        if(declared[d]) continue;
        throw error(function.info, VAR_UNDEFINED_X, function.params[d].name.string());
      }
    }
    return found;
  }

  /**
   * Processes the Websocket request. Parses new modules and discards obsolete ones.
   * @param conn Websocket connection
   * @param message The WebsocketMessage
   * @param response The WSResponse
   * @param header The header to set
   * @param id The id of the WebsocketClient
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  public boolean process(final WsConnection conn, final Object message, final WsResponse response,
      final Map<String, String> header, final String id) throws Exception {
    try {
      return module.process(conn, this, message, response, header, id);
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(function.info);
      throw ex;
    }
  }

  @Override
  public int compareTo(final WsFunction wsxf) {
    return path.compareTo(wsxf.path);
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
    return BASEX_WS_X.get(info, Util.info(msg, ext));
  }

  /**
   * Binds the params of the Function.
   * @param args The Expr
   * @param qc The QueryContext
   * @param message The Messagestring
   * @param header The header to set
   * @throws QueryException query exception
   */
  public void bind(final Expr[] args, final QueryContext qc, final Object message,
      final Map<String, String> header) throws QueryException {
    // Create a Map with Values, done for the message wich could be byte[] or String
    Map<String, Value> valueMap = new HashMap<>();
    if(header != null) header.forEach((k, v) -> {
      if(v != null) try {
        valueMap.put(k, new Atm(URLDecoder.decode(v, Strings.UTF8)));
      } catch(UnsupportedEncodingException e) {
        valueMap.put(k, null);
      }
    });
    if(message instanceof String) {
      valueMap.put("message", Str.get((String) message));
    } else if(message instanceof byte[]) {
      valueMap.put("message", B64.get((byte[]) message));
    }

    for(final WebParam rxp : headerParams) {
      final QNm name = rxp.var;
      Value val = valueMap.get(rxp.name);
      bind(name, args, val, qc);
    }
  }
}
