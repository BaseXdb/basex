package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Info functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNInfo extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case ERROR: return error(qc);
      case TRACE: return trace(qc);
      case AVAILABLE_ENVIRONMENT_VARIABLES: return avlEnvVars();
      default: return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case ENVIRONMENT_VARIABLE: return envVar(qc);
      default: return super.item(qc, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    if(func == TRACE) seqType = exprs[0].seqType();
    return this;
  }

  @Override
  public boolean isVacuous() {
    return func == ERROR || super.isVacuous();
  }

  /**
   * Performs the error function.
   * @param qc query context
   * @return dummy iterator
   * @throws QueryException query exception
   */
  private Iter error(final QueryContext qc) throws QueryException {
    final int al = exprs.length;
    if(al == 0) throw FUNERR1.get(info);

    QNm name = toQNm(exprs[0], qc, sc, al != 1);
    if(name == null) name = FUNERR1.qname();

    String msg = FUNERR1.desc;
    if(al > 1) msg = Token.string(toToken(exprs[1], qc, true));
    final Value val = al > 2 ? qc.value(exprs[2]) : null;
    throw new QueryException(info, name, msg).value(val);
  }

  /**
   * Performs the trace function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter trace(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter ir = exprs[0].iter(qc);
      final byte[] label = toToken(exprs[1], qc);
      boolean empty = true;
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it != null) {
          dump(it, label, info, qc);
          empty = false;
        } else if(empty) {
          dump(null, label, info, qc);
        }
        return it;
      }
    };
  }

  /**
   * Returns all environment variables.
   * @return iterator
   */
  private static ValueIter avlEnvVars() {
    final ValueBuilder vb = new ValueBuilder();
    for(final Object k : System.getenv().keySet()) vb.add(Str.get(k.toString()));
    return vb;
  }

  /**
   * Returns a environment variable.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str envVar(final QueryContext qc) throws QueryException {
    final String e = System.getenv(Token.string(toToken(exprs[0], qc)));
    return e != null ? Str.get(e) : null;
  }

  /**
   * Dumps the specified item.
   * @param it item to be dumped
   * @param label label
   * @param info input info
   * @param qc query context
   * @throws QueryException query exception
   */
  public static void dump(final Item it, final byte[] label, final InputInfo info,
      final QueryContext qc) throws QueryException {
    try {
      final byte[] value;
      if(it == null) {
        value = Token.token(SeqType.EMP.toString());
      } else if(it.type == NodeType.ATT || it.type == NodeType.NSP) {
        value = Token.token(it.toString());
      } else {
        value = it.serialize(SerializerOptions.get(false)).finish();
      }
      dump(value, label, qc);
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }

  /**
   * Dumps the specified info to standard error or the info view of the GUI.
   * @param value traced value
   * @param label additional label to display (can be {@code null})
   * @param qc query context
   */
  public static void dump(final byte[] value, final byte[] label, final QueryContext qc) {
    final TokenBuilder tb = new TokenBuilder();
    if(label != null) tb.add(label);
    tb.add(value);
    final String info = tb.toString();

    // if GUI is used or client is calling, cache trace info
    if(qc.listen != null || qc.context.listener != null) {
      qc.evalInfo(info);
      if(qc.listen != null) qc.listen.info(info);
    } else {
      Util.errln(info);
    }
  }

  /**
   * Creates an error function instance.
   * @param ex query exception
   * @param tp type of the expression
   * @return function
   */
  public static StandardFunc error(final QueryException ex, final SeqType tp) {
    final StandardFunc e = ERROR.get(null, ex.info(), ex.qname(),
        Str.get(ex.getLocalizedMessage()));
    e.seqType(tp);
    return e;
  }
}
