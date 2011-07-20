package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.ArrayList;

import org.basex.core.User;
import org.basex.core.Commands.CmdIndexInfo;
import org.basex.core.cmd.ACreate;
import org.basex.core.cmd.Info;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.InfoIndex;
import org.basex.core.cmd.List;
import org.basex.data.Data;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.SerializerException;
import org.basex.io.serial.XMLSerializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.IndexAccess;
import org.basex.query.item.DBNodeSeq;
import org.basex.query.item.DBNode;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.ValueIter;
import org.basex.query.path.NameTest;
import org.basex.query.up.primitives.Add;
import org.basex.query.up.primitives.DeleteNode;
import org.basex.query.up.primitives.Optimize;
import org.basex.query.up.primitives.ReplaceValue;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.list.IntList;

/**
 * Database functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNDb extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNDb(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case DBOPEN:     return open(ctx).iter();
      case DBTEXT:     return text(ctx);
      case DBATTR:     return attribute(ctx);
      case DBFULLTEXT: return fulltext(ctx);
      case DBLIST:     return list(ctx);
      case DBNODEID:   return node(ctx, true);
      case DBNODEPRE:  return node(ctx, false);
      default:         return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case DBEVENT:    return event(ctx);
      case DBOPENID:   return open(ctx, true);
      case DBOPENPRE:  return open(ctx, false);
      case DBSYSTEM:   return system(ctx);
      case DBINFO:     return info(ctx);
      case DBADD:      return add(ctx);
      case DBDELETE:   return delete(ctx);
      case DBRENAME:   return rename(ctx);
      case DBREPLACE:  return replace(ctx);
      case DBOPTIMIZE: return optimize(ctx);
      default:         return super.item(ctx, ii);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      case DBOPEN: return open(ctx);
      default:     return super.value(ctx);
    }
  }

  /**
   * Performs the open function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Value open(final QueryContext ctx) throws QueryException {
    final byte[] str = checkStr(expr[0], ctx);
    final int s = indexOf(str, '/');
    final byte[] db = s == -1 ? str : substring(str, 0, s);
    final byte[] path = s == -1 ? EMPTY : substring(str, s + 1);

    final Data data = ctx.resource.data(db, input);
    return DBNodeSeq.get(data.doc(string(path)), data, true, s == -1);
  }

  /**
   * Performs the open-id and open-pre function.
   * @param ctx query context
   * @param id id flag
   * @return result
   * @throws QueryException query exception
   */
  private DBNode open(final QueryContext ctx, final boolean id)
      throws QueryException {

    final Data data = data(0, ctx);
    final int v = (int) checkItr(expr[1], ctx);
    final int pre = id ? data.pre(v) : v;
    if(pre < 0 || pre >= data.meta.size) IDINVALID.thrw(input, this, v);
    return new DBNode(data, pre);
  }

  /**
   * Performs the text function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter text(final QueryContext ctx) throws QueryException {
    final IndexContext ic = new IndexContext(ctx, data(0, ctx), null, true);
    return new IndexAccess(input, expr[1], IndexType.TEXT, ic).iter(ctx);
  }

  /**
   * Performs the attribute function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter attribute(final QueryContext ctx) throws QueryException {
    final IndexContext ic = new IndexContext(ctx, data(0, ctx), null, true);
    final IndexAccess ia = new IndexAccess(
        input, expr[1], IndexType.ATTRIBUTE, ic);

    // return iterator if no name test is specified
    if(expr.length < 3) return ia.iter(ctx);

    // parse and compile the name test
    final Item name = checkEmpty(expr[2].item(ctx, input));
    final QNm nm = new QNm(checkStr(name, ctx), ctx, input);

    final NameTest nt = new NameTest(nm, NameTest.Name.STD, true, input);
    // no results expected: return empty sequence
    if(!nt.comp(ctx)) return Empty.ITER;

    // wrap iterator with name test
    return new Iter() {
      final NodeIter ir = ia.iter(ctx);

      @Override
      public Item next() throws QueryException {
        ANode n;
        while((n = ir.next()) != null && !nt.eval(n));
        return n;
      }
    };
  }

  /**
   * Performs the fulltext function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter fulltext(final QueryContext ctx) throws QueryException {
    return FNFt.search(data(0, ctx), checkStr(expr[1], ctx), this, ctx);
  }

  /**
   * Performs the list function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter list(final QueryContext ctx) throws QueryException {
    final ItemCache ic = new ItemCache();
    if(expr.length == 0) {
      for(final String s : List.list(ctx.context)) ic.add(Str.get(s));
    } else {
      final byte[] str = checkStr(expr[0], ctx);
      final int s = indexOf(str, '/');
      final byte[] db = s == -1 ? str : substring(str, 0, s);
      final byte[] path = s == -1 ? EMPTY : substring(str, s + 1);

      // retrieve data instance; will be closed after query execution
      final Data data = ctx.resource.data(db, input);
      final IntList il = data.doc(string(path));
      for(int i = 0, is = il.size(); i < is; i++) {
        ic.add(Str.get(data.text(il.get(i), true)));
      }
    }
    return ic;
  }

  /**
   * Performs the system function.
   * @param ctx query context
   * @return iterator
   */
  private Str system(final QueryContext ctx) {
    return Str.get(Token.delete(Info.info(ctx.context), '\r'));
  }

  /**
   * Performs the info function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str info(final QueryContext ctx) throws QueryException {
    checkRead(ctx);

    final Data data = data(0, ctx);
    final byte[] info;
    if(expr.length == 1) {
      final boolean create = ctx.context.user.perm(User.CREATE);
      info = InfoDB.db(data.meta, false, true, create);
    } else {
      final byte[] tp = checkStr(expr[1], ctx);
      final CmdIndexInfo cmd = InfoIndex.info(string(tp));
      if(cmd == null) NOIDX.thrw(input, this);
      info = InfoIndex.info(cmd, data);
    }
    return Str.get(Token.delete(info, '\r'));
  }

  /**
   * Performs the add function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item add(final QueryContext ctx) throws QueryException {
    checkWrite(ctx);

    final Data data = data(0, ctx);
    final byte[] name = expr.length < 3 ? null : path(checkStr(expr[2], ctx));
    final byte[] path = expr.length < 4 ? null : path(checkStr(expr[3], ctx));

    // get all items representing document(s):
    final ArrayList<Item> docs = new ArrayList<Item>(
        (int) Math.max(expr[1].size(), 1));
    final Iter iter = ctx.iter(expr[1]);
    for(Item i; (i = iter.next()) != null;) docs.add(i);

    if(docs.size() > 0) {
      ctx.updates.add(new Add(data, input, docs, name, path, ctx.context), ctx);
    }
    return null;
  }

  /**
   * Performs the replace function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item replace(final QueryContext ctx) throws QueryException {
    checkWrite(ctx);

    final Data data = data(0, ctx);
    final byte[] trg = path(checkStr(expr[1], ctx));
    final Item doc = checkItem(expr[2], ctx);

    // collect all old documents
    final IntList old = data.doc(string(trg));
    if(old.size() > 0) {
      final int pre = old.get(0);
      if(old.size() > 1 || !eq(data.text(pre, true), trg))
        DOCTRGMULT.thrw(input);
      ctx.updates.add(new DeleteNode(pre, data, input), ctx);
    }

    final byte[] trgname;
    final byte[] trgpath;
    final int p = lastIndexOf(trg, '/');
    if(p < 0) {
      trgname = trg;
      trgpath = null;
    } else {
      trgname = subtoken(trg, p + 1);
      trgpath = subtoken(trg, 0, p);
    }

    final ArrayList<Item> docs = new ArrayList<Item>(1);
    docs.add(doc);
    final Add add = new Add(data, input, docs, trgname, trgpath, ctx.context);
    ctx.updates.add(add, ctx);

    return null;
  }

  /**
   * Performs the delete function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item delete(final QueryContext ctx) throws QueryException {
    checkWrite(ctx);

    final Data data = data(0, ctx);
    final byte[] target = path(checkStr(expr[1], ctx));
    final IntList il = data.doc(string(target));
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      ctx.updates.add(new DeleteNode(pre, data, input), ctx);
    }
    return null;
  }

  /**
   * Performs the rename function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item rename(final QueryContext ctx) throws QueryException {
    checkWrite(ctx);

    final Data data = data(0, ctx);
    final byte[] source = path(checkStr(expr[1], ctx));
    final byte[] target = path(checkStr(expr[2], ctx));

    // the first step of the path should be the database name
    final IntList il = data.doc(string(source));
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      final byte[] trg = ACreate.newName(data, pre, source, target);
      if(trg.length == 0) EMPTYPATH.thrw(input, this);
      ctx.updates.add(new ReplaceValue(pre, data, input, trg), ctx);
    }
    return null;
  }

  /**
   * Performs the optimize function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item optimize(final QueryContext ctx) throws QueryException {
    checkWrite(ctx);

    final boolean all = expr.length == 2 && checkBln(expr[1], ctx);
    final Data data = data(0, ctx);

    ctx.updates.add(new Optimize(data, ctx.context, all, input), ctx);
    return null;
  }

  /**
   * Performs the node-pre and node-id function.
   * @param ctx query context
   * @param id id flag
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter node(final QueryContext ctx, final boolean id)
      throws QueryException {

    return new Iter() {
      final Iter ir = ctx.iter(expr[0]);

      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) return null;
        final DBNode node = checkDBNode(it);
        return Itr.get(id ? node.data.id(node.pre) : node.pre);
      }
    };
  }

  /**
   * Sends an event to the registered sessions.
   * @param ctx query context
   * @return event result
   * @throws QueryException query exception
   */
  private Item event(final QueryContext ctx) throws QueryException {
    final byte[] name = checkStr(expr[0], ctx);
    if(expr.length == 3) expr[2].value(ctx);

    final ArrayOutput ao = new ArrayOutput();
    try {
      // run serialization
      final XMLSerializer xml = new XMLSerializer(ao);
      final ValueIter ir = expr[1].value(ctx).iter();
      for(Item it; (it = ir.next()) != null;) it.serialize(xml);
      xml.close();
    } catch(final SerializerException ex) {
      throw new QueryException(input, ex);
    } catch(final IOException ex) {
      SERANY.thrw(input, ex);
    }
    // throw exception if event is unknown
    if(!ctx.context.events.notify(ctx.context, name, ao.toArray())) {
      NOEVENT.thrw(input, name);
    }
    return null;
  }

  @Override
  public boolean vacuous() {
    return def == Function.DBEVENT;
  }

  @Override
  public boolean uses(final Use u) {
    final boolean up =
      def == Function.DBADD || def == Function.DBDELETE ||
      def == Function.DBRENAME || def == Function.DBREPLACE ||
      def == Function.DBOPTIMIZE;
    return
      u == Use.CTX && (def == Function.DBTEXT || def == Function.DBATTR ||
        def == Function.DBFULLTEXT || def == Function.DBEVENT || up) ||
      u == Use.UPD && up || super.uses(u);
  }

  @Override
  public boolean iterable() {
    // index functions will always yield ordered and duplicate-free results
    return def == Function.DBOPEN || def == Function.DBTEXT ||
      def == Function.DBATTR || def == Function.DBFULLTEXT || super.iterable();
  }

  /**
   * Returns the data instance for the specified argument.
   * @param arg argument
   * @param ctx query context
   * @return data instance
   * @throws QueryException query exception
   */
  private Data data(final int arg, final QueryContext ctx)
      throws QueryException {

    final Item it = checkEmpty(expr[arg].item(ctx, input));
    if(it.node()) return checkDBNode(it).data;
    if(it.str())  return ctx.resource.data(it.atom(input), input);
    throw STRNODTYPE.thrw(input, this, it.type);
  }

  /**
   * Normalizes the database path.
   * Removes duplicate, leading and trailing slashes
   * @param path input path
   * @return normalized path
   */
  private static byte[] path(final byte[] path) {
    return token(ACreate.path(string(path)));
  }
}
