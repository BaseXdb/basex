package org.basex.query.func;

import static org.basex.core.Text.*;
import static org.basex.query.QueryText.*;

import org.basex.core.Main;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Atm;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.Put;
import org.basex.query.util.Err;

/**
 * Generating functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FNGen extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case DATA:
        return data(ctx.iter(expr[0]));
      case COLLECT:
        if(expr.length == 0) return ctx.coll(null);
        Iter iter = ctx.iter(expr[0]);
        Item it = iter.next();
        if(it == null) Err.empty(this);
        return ctx.coll(checkStr(it));
      case PUT:
        /* [LK] fn:put() operations should be moved to pending list
         * - FOUP0002 could be revised (kinda dirty right now)
         * - XUDY0031 missing
         * - as usual, check other notes in specs (2.6.1)
         */
        if(!ctx.context.user.perm(User.ADMIN))
          throw new QueryException(Main.info(PERMNO, CmdPerm.ADMIN));
        final byte[] file = checkStr(expr[1], ctx);
        it = expr[0].atomic(ctx);

        if(it == null || it.type != Type.DOC && it.type != Type.ELM)
          Err.or(UPFOTYPE, expr[0]);
        
        final Uri u = Uri.uri(file);
        if(u == Uri.EMPTY || !u.valid()) Err.or(UPFOURI, file);
        ctx.updates.add(new Put((Nod) it, u), ctx);
        
        return Iter.EMPTY;
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);

    switch(func) {
      case DOC:
        Item it = iter.next();
        return it == null ? null : it.type == Type.DOC ? it :
          ctx.doc(checkStr(it), false, false);
      case DOCAVAIL:
        it = iter.next();
        if(it != null) {
          final byte[] file = checkStr(it);
          try {
            ctx.doc(file, false, false);
            return Bln.TRUE;
          } catch(final QueryException ex) { }
        }
        return Bln.FALSE;
      default:
        return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    if(func == FunDef.DOC) {
      if(!expr[0].i()) return this;
      final Item it = (Item) expr[0];
      return it.type == Type.DOC ? it : ctx.doc(checkStr(it), false, false);
    } else if(func == FunDef.COLLECT) {
      if(expr.length == 0 || !expr[0].i()) return this;
      final Item it = (Item) expr[0];
      return it.type == Type.STR ? ctx.coll(checkStr(it)).finish() : this;
    }
    return this;
  }

  /**
   * Performs the data function.
   * @param iter iterator.
   * @return resulting iterator
   */
  private Iter data(final Iter iter) {
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = iter.next();
        return it != null ? atom(it) : null;
      }
    };
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
}
