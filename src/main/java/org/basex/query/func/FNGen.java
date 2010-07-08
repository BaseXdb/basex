package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.io.TextInput;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.expr.Expr;
import org.basex.query.item.Atm;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.Put;
import org.basex.query.util.Err;
import org.basex.util.TokenMap;

/**
 * Generating functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNGen extends Fun {
  /** Cached file contents. */
  private final TokenMap contents = new TokenMap();
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case DATA: return data(ctx);
      case COLL: return coll(ctx);
      case PUT:  return put(ctx);
      default:   return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case DOC:         return doc(ctx);
      case DOCAVL:      return docavl(ctx);
      case PARSETXT:    return parsetxt(ctx);
      case PARSETXTAVL: return parsetxtavl(ctx);
      case PARSEXML:
      case URICOLL:
      case SERIALIZE:   Err.or(NOTIMPL, func.desc); return null;
      default:          return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    if(func == FunDef.DOC)
      return expr[0].i() ? atomic(ctx) : this;
    if(func == FunDef.COLL)
      return expr.length != 0 && expr[0].i() ? iter(ctx).finish() : this;
    if(func == FunDef.PARSETXT) {
      return expr[0].i() && (expr.length == 1 || expr[1].i()) ?
        atomic(ctx) : this;
    }
    return this;
  }

  /**
   * Performs the data function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter data(final QueryContext ctx) throws QueryException {
    final Iter ir = ctx.iter(expr.length != 0 ? expr[0] : checkCtx(ctx));

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        return it != null ? atom(it) : null;
      }
    };
  }

  /**
   * Performs the collection function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter coll(final QueryContext ctx) throws QueryException {
    if(expr.length == 0) return ctx.coll(null);
    final Item it = expr[0].atomic(ctx);
    if(it == null) Err.empty(this);
    return ctx.coll(checkStr(it));
  }

  /**
   * Performs the put function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter put(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    final byte[] file = checkStr(expr[1], ctx);
    final Item it = expr[0].atomic(ctx);

    if(it == null || it.type != Type.DOC && it.type != Type.ELM)
      Err.or(UPFOTYPE, expr[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.valid()) Err.or(UPFOURI, file);
    ctx.updates.add(new Put((Nod) it, u), ctx);

    return Iter.EMPTY;
  }

  /**
   * Performs the doc function.
   * @param ctx query context
   * @return resulting node
   * @throws QueryException query exception
   */
  private Nod doc(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].atomic(ctx);
    return it == null ? null : ctx.doc(checkStr(it), false, false);
  }

  /**
   * Performs the doc-available function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln docavl(final QueryContext ctx) throws QueryException {
    try {
      return Bln.get(doc(ctx) != null);
    } catch(final QueryException ex) {
      if(!ex.code().startsWith(QueryText.FODC)) throw ex;
      return Bln.FALSE;
    }
  }

  /**
   * Performs the unparsed-text function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item parsetxt(final QueryContext ctx) throws QueryException {
    return parsetxt(ctx, false);
  }

  /**
   * Performs the unparsed-text-available function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln parsetxtavl(final QueryContext ctx) throws QueryException {
    try {
      return Bln.get(parsetxt(ctx, true) != null);
    } catch(final QueryException ex) {
      if(!ex.code().startsWith(QueryText.FODC)) throw ex;
      return Bln.FALSE;
    }
  }

  /**
   * Performs the unparsed-text function. The result is optionally cached.
   * @param ctx query context
   * @param cache flag for caching the result
   * @return resulting item
   * @throws QueryException query exception
   */
  private Str parsetxt(final QueryContext ctx, final boolean cache)
      throws QueryException {

    final IO io = checkIO(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkStr(expr[1], ctx));
    try {
      final byte[] path = token(io.path());
      byte[] cont = contents.get(path);
      // check if content has already been parsed; if not, read original file
      if(cont == null) {
        cont = TextInput.content(io, enc).finish();
        if(cache) contents.add(path, cont);
      }
      return Str.get(cont);
    } catch(final IOException ex) {
      Err.or(NODOC, ex.getMessage() != null ? ex.getMessage() : ex.toString());
      return null;
    }
  }
  
  /**
   * Atomizes the specified item.
   * @param it input item
   * @return atomized item
   */
  static Item atom(final Item it) {
    return it.node() ? it.type == Type.PI || it.type == Type.COM ?
        Str.get(it.str()) : new Atm(it.str()) : it;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.UPD ? func == FunDef.PUT : super.uses(u, ctx);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    if(func == FunDef.DATA) {
      final SeqType ret = expr[0].returned(ctx);
      return ret.type.node() ? new SeqType(Type.ATM, ret.occ) : ret;
    }
    return super.returned(ctx);
  }
}
