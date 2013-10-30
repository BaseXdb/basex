package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Functions to connect remote database instances.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNClient extends StandardFunc {
  /** Query pattern. */
  static final Pattern QUERYPAT = Pattern.compile("\\[(.*?)\\] (.*)", Pattern.MULTILINE);

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNClient(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _CLIENT_QUERY: return query(ctx).iter(ctx);
      default:            return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _CLIENT_QUERY: return query(ctx);
      default:            return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _CLIENT_CONNECT: return connect(ctx);
      case _CLIENT_EXECUTE: return execute(ctx);
      case _CLIENT_INFO:    return info(ctx);
      case _CLIENT_CLOSE:   return close(ctx);
      default:              return super.item(ctx, ii);
    }
  }

  /**
   * Establishes a connection to a remote database instance.
   * @param ctx query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Uri connect(final QueryContext ctx) throws QueryException {
    final String host = Token.string(checkStr(expr[0], ctx));
    final String user = Token.string(checkStr(expr[2], ctx));
    final String pass = Token.string(checkStr(expr[3], ctx));
    final int port = (int) checkItr(expr[1], ctx);
    try {
      return ctx.sessions().add(new ClientSession(host, port, user, pass));
    } catch(final IOException ex) {
      throw BXCL_CONN.thrw(info, ex);
    }
  }

  /**
   * Executes a command and returns the result as string.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str execute(final QueryContext ctx) throws QueryException {
    final ClientSession cs = session(ctx, false);
    final String cmd = Token.string(checkStr(expr[1], ctx));

    try {
      final ArrayOutput ao = new ArrayOutput();
      cs.setOutputStream(ao);
      cs.execute(cmd);
      cs.setOutputStream(null);
      return Str.get(ao.toArray());
    } catch(final BaseXException ex) {
      throw BXCL_COMMAND.thrw(info, ex);
    } catch(final IOException ex) {
      throw BXCL_COMM.thrw(info, ex);
    }
  }

  /**
   * Executes a command and returns the result as string.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str info(final QueryContext ctx) throws QueryException {
    return Str.get(session(ctx, false).info().replaceAll("\r\n?", "\n").trim());
  }

  /**
   * Executes a query and returns the result as sequence.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Value query(final QueryContext ctx) throws QueryException {
    final ClientSession cs = session(ctx, false);
    final String query = Token.string(checkStr(expr[1], ctx));
    final ValueBuilder vb = new ValueBuilder();
    ClientQuery cq = null;
    try {
      cq = cs.query(query);
      // bind variables and context item
      for(final Map.Entry<String, Value> it : bindings(2, ctx).entrySet()) {
        final String k = it.getKey();
        final Value v = it.getValue();
        final ArrayOutput val = v.serialize();
        if(!v.isItem()) BXCL_ITEM.thrw(info, v);
        final String t = v.type().toString();
        if(k.isEmpty()) cq.context(val, t);
        else cq.bind(k, val, t);
      }
      // evaluate query
      while(cq.more()) {
        final String result = cq.next();
        vb.add(cq.type().castString(result, ctx, sc, info));
      }
      return vb.value();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final BaseXException ex) {
      final Matcher m = QUERYPAT.matcher(ex.getMessage());
      if(m.find()) {
        Err.thrw(m.group(1), info, m.group(2));
        throw new QueryException(info, new QNm(m.group(1)), m.group(2));
      }
      throw BXCL_QUERY.thrw(info, ex);
    } catch(final IOException ex) {
      throw BXCL_COMM.thrw(info, ex);
    } finally {
      if(cq != null) try { cq.close(); } catch(final IOException ignored) { }
    }
  }

  /**
   * Establishes a connection to a remote database instance.
   * @param ctx query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Item close(final QueryContext ctx) throws QueryException {
    try {
      session(ctx, true).close();
      return null;
    } catch(final IOException ex) {
      throw BXCL_COMMAND.thrw(info, ex);
    }
  }

  /**
   * Returns a connection and removes it from list with opened connections if
   * requested.
   * @param ctx query context
   * @param del flag indicating if connection has to be removed
   * @return connection
   * @throws QueryException query exception
   */
  private ClientSession session(final QueryContext ctx, final boolean del)
      throws QueryException {

    final Uri id = (Uri) checkType(expr[0].item(ctx, info), AtomType.URI);
    final ClientSession cs = ctx.sessions().get(id);
    if(cs == null) BXCL_NOTAVL.thrw(info, id);
    if(del) ctx.sessions().remove(id);
    return cs;
  }
}
