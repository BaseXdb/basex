package org.basex.ws;

import static org.basex.util.Token.*;

import java.net.*;
import java.util.*;

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

/**
 * This class represents a single Websocket-Function.
 * @author BaseX Team 2005-18, BSD License
 * */
public class WsXqFunction implements Comparable<WsXqFunction> {

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
   * Checks a function for Websocket and permission Annotations.
   * @return {@code true} if function contains relevant annotations
   */
  public boolean parse() {
    boolean found = false;

    for(final Ann ann : function.anns) {
      final Annotation sig = ann.sig;
      if(sig == null) continue;

      found |= eq(sig.uri, QueryText.WS_URI);

      // If the Annotation is a ws:param(..) annotation: Add the Params to wsParameters
      if(sig == Annotation._WS_PARAM) {
        final Item[] args = ann.args();
        final String name = ((Str) args[0]).toJava();

        // TODO: Hier name uas den args nehmen und declared verwenden:
        // checkVariable(toString(args[1], declared);
        final QNm var = new QNm("nachricht");

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
   * @param message The MessageString
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  public boolean process(final WebsocketConnection conn, final String message) throws Exception {
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
   * Binds the params of the Function.
   * @param args The Expr
   * @param qc The QueryContext
   * @param message The Messagestring
   */
  public void bind(final Expr[] args, final QueryContext qc, final String message) {
    for(final RestXqParam rxp: wsParameters) {
      try {
        Value test = new Atm(URLDecoder.decode(message, Strings.UTF8));
        final Var[] params = function.params;
        final int pl = params.length;
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
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
}
