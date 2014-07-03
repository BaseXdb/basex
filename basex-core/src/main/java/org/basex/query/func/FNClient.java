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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNClient extends StandardFunc {
  /** Query pattern. */
  private static final Pattern QUERYPAT = Pattern.compile("\\[(.*?)\\] (.*)", Pattern.MULTILINE);

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNClient(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    switch(func) {
      case _CLIENT_QUERY: return query(qc).iter(qc);
      default:            return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _CLIENT_QUERY: return query(qc);
      default:            return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    switch(func) {
      case _CLIENT_CONNECT: return connect(qc);
      case _CLIENT_EXECUTE: return execute(qc);
      case _CLIENT_INFO:    return info(qc);
      case _CLIENT_CLOSE:   return close(qc);
      default:              return super.item(qc, ii);
    }
  }

  /**
   * Establishes a connection to a remote database instance.
   * @param qc query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Uri connect(final QueryContext qc) throws QueryException {
    final String host = Token.string(checkStr(exprs[0], qc));
    final String user = Token.string(checkStr(exprs[2], qc));
    final String pass = Token.string(checkStr(exprs[3], qc));
    final int port = (int) checkItr(exprs[1], qc);
    try {
      return sessions(qc).add(new ClientSession(host, port, user, pass));
    } catch(final IOException ex) {
      throw BXCL_CONN.get(info, ex);
    }
  }

  /**
   * Executes a command and returns the result as string.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Str execute(final QueryContext qc) throws QueryException {
    final ClientSession cs = session(qc, false);
    final String cmd = Token.string(checkStr(exprs[1], qc));

    try {
      final ArrayOutput ao = new ArrayOutput();
      cs.setOutputStream(ao);
      cs.execute(cmd);
      cs.setOutputStream(null);
      return Str.get(ao.toArray());
    } catch(final BaseXException ex) {
      throw BXCL_COMMAND.get(info, ex);
    } catch(final IOException ex) {
      throw BXCL_COMM.get(info, ex);
    }
  }

  /**
   * Executes a command and returns the result as string.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Str info(final QueryContext qc) throws QueryException {
    return Str.get(session(qc, false).info().replaceAll("\r\n?", "\n").trim());
  }

  /**
   * Executes a query and returns the result as sequence.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Value query(final QueryContext qc) throws QueryException {
    final ClientSession cs = session(qc, false);
    final String query = Token.string(checkStr(exprs[1], qc));
    final ValueBuilder vb = new ValueBuilder();
    ClientQuery cq = null;
    try {
      cq = cs.query(query);
      // bind variables and context item
      for(final Map.Entry<String, Value> binding : bindings(2, qc).entrySet()) {
        final String k = binding.getKey();
        final Value v = binding.getValue();
        if(!v.isItem()) throw BXCL_ITEM.get(info, v);
        final Item it = (Item) v;
        final Type t = v.type;
        if(it instanceof FuncItem) throw FIVALUE.get(info, t);

        final Object value = t instanceof NodeType ? v.serialize() : Token.string(it.string(info));
        if(k.isEmpty()) cq.context(value, t.toString());
        else cq.bind(k, value, t.toString());
      }
      // evaluate query
      while(cq.more()) {
        final String result = cq.next();
        vb.add(cq.type().castString(result, qc, sc, info));
      }
      return vb.value();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final BaseXException ex) {
      final Matcher m = QUERYPAT.matcher(ex.getMessage());
      if(m.find()) {
        final QueryException exc = get(m.group(1), info, m.group(2));
        throw exc == null ? new QueryException(info, new QNm(m.group(1)), m.group(2)) : exc;
      }
      throw BXCL_QUERY.get(info, ex);
    } catch(final IOException ex) {
      throw BXCL_COMM.get(info, ex);
    } finally {
      if(cq != null) try { cq.close(); } catch(final IOException ignored) { }
    }
  }

  /**
   * Establishes a connection to a remote database instance.
   * @param qc query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Item close(final QueryContext qc) throws QueryException {
    try {
      session(qc, true).close();
      return null;
    } catch(final IOException ex) {
      throw BXCL_COMMAND.get(info, ex);
    }
  }

  /**
   * Returns a connection and removes it from list with opened connections if
   * requested.
   * @param qc query context
   * @param del flag indicating if connection has to be removed
   * @return connection
   * @throws QueryException query exception
   */
  private ClientSession session(final QueryContext qc, final boolean del) throws QueryException {
    final Uri id = (Uri) checkType(exprs[0].item(qc, info), AtomType.URI);
    final ClientSession cs = sessions(qc).get(id);
    if(cs == null) throw BXCL_NOTAVL.get(info, id);
    if(del) sessions(qc).remove(id);
    return cs;
  }

  /**
   * Returns the sessions handler.
   * @param qc query context
   * @return connection handler
   */
  private static ClientSessions sessions(final QueryContext qc) {
    ClientSessions res = qc.resources.get(ClientSessions.class);
    if(res == null) {
      res = new ClientSessions();
      qc.resources.add(res);
    }
    return res;
  }
}
