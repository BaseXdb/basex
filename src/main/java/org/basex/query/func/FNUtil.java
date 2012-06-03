package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

import org.basex.core.*;
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
 * Project specific functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class FNUtil extends StandardFunc {
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
      case _UTIL_MEM:      return mem(ctx);
      case _UTIL_TIME:     return time(ctx);
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
      case _UTIL_SLEEP:             return sleep(ctx);
      case _UTIL_FORMAT:            return format(ctx);
      case _UTIL_MD5:               return hash(ctx, "MD5");
      case _UTIL_SHA1:              return hash(ctx, "SHA");
      case _UTIL_CRC32:             return crc32(ctx);
      case _UTIL_UUID:              return uuid();
      case _UTIL_DEEP_EQUAL:        return deep(ctx);
      case _UTIL_PATH:              return filename(ctx);
      default:                      return super.item(ctx, ii);
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
    final Expr e = super.comp(ctx);
    if(sig == Function._UTIL_TYPE) {
      FNInfo.dump(Util.inf("{ type: %, size: % }", expr[0].type(), expr[0].size()),
          Token.token(expr[0].toString()), ctx);
      return expr[0];
    }
    return e;
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
   * Measures the memory consumption for the specified expression in MB.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Iter mem(final QueryContext ctx) throws QueryException {
    // measure initial memory consumption
    Performance.gc(3);
    final long min = Performance.memory();

    // optional message
    final byte[] msg = expr.length > 2 ? checkStr(expr[2], ctx) : null;

    // check caching flag
    if(expr.length > 1 && checkBln(expr[1], ctx)) {
      final Value v = ctx.value(expr[0]).cache().value();
      dump(min, msg, ctx);
      return v.iter();
    }

    return new Iter() {
      final Iter ir = expr[0].iter(ctx);
      @Override
      public Item next() throws QueryException {
        final Item i = ir.next();
        if(i == null) dump(min, msg, ctx);
        return i;
      }
    };
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
   * Measures the execution time for the specified expression in milliseconds.
   * @param ctx query context
   * @return time in milliseconds
   * @throws QueryException query exception
   */
  private Iter time(final QueryContext ctx) throws QueryException {
    // create timer
    final Performance p = new Performance();

    // optional message
    final byte[] msg = expr.length > 2 ? checkStr(expr[2], ctx) : null;

    // check caching flag
    if(expr.length > 1 && checkBln(expr[1], ctx)) {
      final Value v = ctx.value(expr[0]).cache().value();
      FNInfo.dump(token(p.getTime()), msg, ctx);
      return v.iter();
    }

    return new Iter() {
      final Iter ir = expr[0].iter(ctx);
      @Override
      public Item next() throws QueryException {
        final Item i = ir.next();
        if(i == null) FNInfo.dump(token(p.getTime()), msg, ctx);
        return i;
      }
    };
  }

  /**
   * Sleeps for the specified number of milliseconds.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item sleep(final QueryContext ctx) throws QueryException {
    Performance.sleep(checkItr(expr[0], ctx));
    return null;
  }

  /**
   * Creates the hash of the given xs:string, using the algorithm {@code algo}.
   * @param ctx query context
   * @param algo hashing algorithm
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex hash(final QueryContext ctx, final String algo) throws QueryException {
    final byte[] str = checkStr(expr[0], ctx);
    try {
      return new Hex(MessageDigest.getInstance(algo).digest(str));
    } catch(final NoSuchAlgorithmException ex) {
      throw Util.notexpected(ex);
    }
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
    final byte[] res = new byte[4];
    for(int i = res.length, c = (int) crc.getValue(); i-- > 0; c >>>= 8)
      res[i] = (byte) (c & 0xFF);
    return new Hex(res);
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

  /**
   * Returns the name of the query file, or {@code null} if none is given.
   * @param ctx query context
   * @return filename
   */
  private static Str filename(final QueryContext ctx) {
    final String fn = ctx.context.prop.get(Prop.QUERYPATH);
    return fn.isEmpty() ? null : Str.get(fn);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT && (sig == Function._UTIL_EVAL || sig == Function._UTIL_SLEEP ||
        sig == Function._UTIL_RUN || sig == Function._UTIL_MEM ||
        sig == Function._UTIL_TIME || sig == Function._UTIL_UUID) ||
      super.uses(u);
  }
}
