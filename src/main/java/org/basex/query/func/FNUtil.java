package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.Compare.Flag;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Utility functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class FNUtil extends StandardFunc {
  /** Newline character. */
  private static final Str NL = Str.get("\n");
  /** Tab character. */
  private static final Str TAB = Str.get("\t");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNUtil(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _UTIL_EVAL:     return eval(ctx).iter();
      case _UTIL_RUN:      return run(ctx).iter();
      case _UTIL_TYPE:     return value(ctx).iter();
      default:             return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _UTIL_EVAL: return eval(ctx);
      case _UTIL_RUN:  return run(ctx);
      case _UTIL_TYPE: return comp(ctx).value(ctx);
      default:         return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _UTIL_NL:         return NL;
      case _UTIL_TAB:        return TAB;
      case _UTIL_FORMAT:     return format(ctx);
      case _UTIL_CRC32:      return crc32(ctx);
      case _UTIL_UUID:       return uuid();
      case _UTIL_DEEP_EQUAL: return deep(ctx);
      default:               return super.item(ctx, ii);
    }
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value eval(final QueryContext ctx) throws QueryException {
    return eval(ctx, checkEStr(expr[0], ctx));
  }

  /**
   * Evaluates the specified string.
   * @param ctx query context
   * @param qu query string
   * @return resulting value
   * @throws QueryException query exception
   */
  private static Value eval(final QueryContext ctx, final byte[] qu)
      throws QueryException {

    final QueryContext qc = new QueryContext(ctx.context);
    qc.parse(string(qu));
    qc.compile();
    return qc.value();
  }

  /**
   * Performs the run function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value run(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    final String path = string(checkStr(expr[0], ctx));
    final IO io = IO.get(path);
    if(!io.exists()) FILE_IO.thrw(info, path);
    try {
      return eval(ctx, io.read());
    } catch(final IOException ex) {
      throw IOERR.thrw(info, ex);
    }
  }

  @Override
  Expr comp(final QueryContext ctx) throws QueryException {
    if(sig == Function._UTIL_TYPE) {
      FNInfo.dump(Util.inf("{ type: %, size: % }", expr[0].type(), expr[0].size()),
          Token.token(expr[0].toString()), ctx);
      return expr[0];
    }
    return this;
  }

  /**
   * Formats a string according to the specified format.
   * @param ctx query context
   * @return formatted string
   * @throws QueryException query exception
   */
  private Str format(final QueryContext ctx) throws QueryException {
    final String form = string(checkStr(expr[0], ctx));
    final Object[] args = new Object[expr.length - 1];
    for(int e = 1; e < expr.length; e++) {
      args[e - 1] = expr[e].item(ctx, info).toJava();
    }
    try {
      return Str.get(String.format(form, args));
    } catch(final RuntimeException ex) {
      throw ERRFORM.thrw(info, Util.name(ex), ex);
    }
  }

  /**
   * Dumps the memory consumption.
   * @param min initial memory usage
   * @param msg message (can be {@code null})
   * @param ctx query context
   */
  static void dump(final long min, final byte[] msg, final QueryContext ctx) {
    Performance.gc(2);
    final long max = Performance.memory();
    final long mb = Math.max(0, max - min);
    FNInfo.dump(token(Performance.format(mb)), msg, ctx);
  }

  /**
   * Creates the CRC32 hash of the given xs:string.
   * @param ctx query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex crc32(final QueryContext ctx) throws QueryException {
    final CRC32 crc = new CRC32();
    crc.update(checkStr(expr[0], ctx));
    final byte[] r = new byte[4];
    for(int i = r.length, c = (int) crc.getValue(); i-- > 0; c >>>= 8) r[i] = (byte) c;
    return new Hex(r);
  }

  /**
   * Creates a random UUID.
   * @return random UUID
   */
  private static Str uuid() {
    return Str.get(UUID.randomUUID());
  }

  /**
   * Checks items for deep equality.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  private Item deep(final QueryContext ctx) throws QueryException {
    final Compare cmp = new Compare(info);
    final Flag[] flags = Flag.values();
    if(expr.length == 3) {
      final Iter ir = expr[2].iter(ctx);
      for(Item it; (it = ir.next()) != null;) {
        final byte[] key = uc(checkEStr(it));
        boolean found = false;
        for(final Flag f : flags) {
          found = eq(key, token(f.name()));
          if(found) {
            cmp.set(f);
            break;
          }
        }
        if(!found) ELMOPTION.thrw(info, key);
      }
    }
    return Bln.get(cmp.deep(ctx.iter(expr[0]), ctx.iter(expr[1])));
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT && oneOf(sig, _UTIL_EVAL, _UTIL_RUN, _UTIL_UUID) ||
        super.uses(u);
  }
}
