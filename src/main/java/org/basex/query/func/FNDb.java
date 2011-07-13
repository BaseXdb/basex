package org.basex.query.func;

import static org.basex.core.cmd.ACreate.*;
import static org.basex.core.cmd.Rename.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.ArrayList;

import org.basex.core.User;
import org.basex.core.Commands.CmdIndexInfo;
import org.basex.core.cmd.Info;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.InfoIndex;
import org.basex.core.cmd.List;
import org.basex.data.Data;
import org.basex.data.SerializerException;
import org.basex.data.XMLSerializer;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.ArrayOutput;
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
import org.basex.query.up.primitives.ReplaceValue;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;
import org.basex.util.Token;

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
      case OPEN:     return open(ctx).iter();
      case TEXT:     return text(ctx);
      case ATTR:     return attribute(ctx);
      case FULLTEXT: return fulltext(ctx);
      case LIST:     return list(ctx);
      case NODEID:   return node(ctx, true);
      case NODEPRE:  return node(ctx, false);
      default:       return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case EVENT:      return event(ctx);
      case OPENID:     return open(ctx, true);
      case OPENPRE:    return open(ctx, false);
      case SYSTEM:     return system(ctx);
      case INFO:       return info(ctx);
      case ADD:        return add(ctx);
      case DELETE:     return delete(ctx);
      case RENAME:     return rename(ctx);
      case REPLACEDOC: return replace(ctx);
      default:         return super.item(ctx, ii);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      case OPEN:     return open(ctx);
      default:       return super.value(ctx);
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

    final Data data = ctx.resource.data(checkStr(expr[0], ctx), input);
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
    final Data data = ctx.resource.data(checkStr(expr[0], ctx), input);
    final IndexContext ic = new IndexContext(ctx, data, null, true);
    return new IndexAccess(input, expr[1], IndexType.TEXT, ic).iter(ctx);
  }

  /**
   * Performs the attribute function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter attribute(final QueryContext ctx) throws QueryException {
    final Data data = ctx.resource.data(checkStr(expr[0], ctx), input);
    final IndexContext ic = new IndexContext(ctx, data, null, true);
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
    final Data data = ctx.resource.data(checkStr(expr[0], ctx), input);
    return FNFt.search(data, checkStr(expr[1], ctx), this, ctx);
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
      for(final int pre : data.doc(string(path)))
        ic.add(Str.get(data.text(pre, true)));
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
    final byte[] info;
    final Data d = ctx.resource.data(checkStr(expr[0], ctx), input);
    if(expr.length == 1) {
      final boolean create = ctx.context.user.perm(User.CREATE);
      info = InfoDB.db(d.meta, false, true, create);
    } else {
      final byte[] tp = checkStr(expr[1], ctx);
      final CmdIndexInfo cmd = InfoIndex.info(string(tp));
      if(cmd == null) NOIDX.thrw(input, this);
      info = InfoIndex.info(cmd, d);
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
    final byte[] name = expr.length < 3 ? null :
      token(path(string(checkStr(expr[2], ctx))));
    final byte[] path = expr.length < 4 ? null :
      token(path(string(checkStr(expr[3], ctx))));

    // get all items representing document(s):
    final ArrayList<Item> docs = new ArrayList<Item>();
    final Iter iter = ctx.iter(expr[1]);
    for(Item i; (i = iter.next()) != null;) docs.add(i);

    if(docs.size() > 0) {
      final Data data = ctx.resource.data(checkStr(expr[0], ctx), input);
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
    final String path = path(string(checkStr(expr[0], ctx)));

    // the first step of the path should be the database name
    final int pos = path.indexOf('/');
    if(pos <= 0) NODB.thrw(input, path);
    final byte[] db = token(path.substring(0, pos));
    final Data data = ctx.resource.data(db, input);

    // replace: source and target path are the same
    final String src = path.substring(pos + 1);
    final byte[] trg = token(src);

    final Item doc = checkItem(expr[1], ctx);

    // collect all old documents
    final int[] old = data.doc(src);
    if(old.length > 0) {
      final int pre = old[0];
      if(old.length > 1 || !eq(data.text(pre, true), trg))
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

    final ArrayList<Item> docs = new ArrayList<Item>(); docs.add(doc);
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
    final String path = path(string(checkStr(expr[0], ctx)));

    // the first step of the path should be the database name
    final int pos = path.indexOf('/');
    if(pos <= 0) NODB.thrw(input, path);
    final byte[] db = token(path.substring(0, pos));
    final Data data = ctx.resource.data(db, input);

    final String trg = path.substring(pos + 1);

    final int[] docs = data.doc(trg);
    for(final int pre : docs)
      ctx.updates.add(new DeleteNode(pre, data, input), ctx);

    return null;
  }

  /**
   * Performs the rename function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item rename(final QueryContext ctx) throws QueryException {
    final String path = path(string(checkStr(expr[0], ctx)));

    // the first step of the path should be the database name
    final int pos = path.indexOf('/');
    if(pos <= 0) NODB.thrw(input, path);
    final byte[] db = token(path.substring(0, pos));
    final Data data = ctx.resource.data(db, input);

    final byte[] src = token(path.substring(pos + 1));
    final byte[] trg = token(path(string(checkStr(expr[1], ctx))));

    final int[] docs = data.doc(string(src));
    for(final int pre : docs) {
      final byte[] nm = newName(data, pre, src, trg);
      ctx.updates.add(new ReplaceValue(pre, data, input, nm), ctx);
    }

    return null;
  }

  /**
   * Performs the node-id function.
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
    return def == Function.EVENT;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && (def == Function.TEXT || def == Function.ATTR ||
        def == Function.FULLTEXT || def == Function.EVENT) || super.uses(u);
  }

  @Override
  public boolean iterable() {
    // index functions will always yield ordered and duplicate-free results
    return def == Function.OPEN || def == Function.TEXT ||
      def == Function.ATTR || def == Function.FULLTEXT || super.iterable();
  }
}
