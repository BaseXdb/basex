package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.net.URLConnection;

import org.basex.core.Commands.CmdIndexInfo;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Info;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.InfoIndex;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Rename;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IOFile;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.IndexAccess;
import org.basex.query.item.ANode;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.DBNodeSeq;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.QNm;
import org.basex.query.item.Raw;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.ValueIter;
import org.basex.query.path.NameTest;
import org.basex.query.up.primitives.DBAdd;
import org.basex.query.up.primitives.DBDelete;
import org.basex.query.up.primitives.DBOptimize;
import org.basex.query.up.primitives.DBPut;
import org.basex.query.up.primitives.DBRename;
import org.basex.query.up.primitives.DeleteNode;
import org.basex.query.up.primitives.ReplaceValue;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.list.IntList;
import org.basex.util.list.ObjList;
import org.basex.util.list.TokenList;

/**
 * Database functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
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
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      case DBOPEN: return open(ctx);
      default:     return super.value(ctx);
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
      case DBSTORE:    return store(ctx);
      case DBRETRIEVE: return retrieve(ctx);
      case DBISRAW:    return isRaw(ctx);
      case DBCTYPE:    return contentType(ctx);
      case DBISXML:    return isXML(ctx);
      default:         return super.item(ctx, ii);
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
    // deprecated solution; slash will later be disallowed
    final int s = indexOf(str, '/');
    final byte[] db = s == -1 ? str : substring(str, 0, s);
    byte[] path = s == -1 ? EMPTY : substring(str, s + 1);
    if(expr.length == 2) path = checkStr(expr[1], ctx);

    final Data data = ctx.resource.data(db, input);
    return DBNodeSeq.get(data.docs(string(path)), data, true, s == -1);
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
    final TokenList tl = new TokenList();
    final int el = expr.length;
    if(el == 0) {
      for(final String s : List.list(ctx.context)) tl.add(s);
    } else {
      final Data data = data(0, ctx);
      final String path = string(el == 1 ? EMPTY : checkStr(expr[1], ctx));
      // add xml resources
      final IntList il = data.docs(path);
      final int is = il.size();
      for(int i = 0; i < is; i++) tl.add(data.text(il.get(i), true));
      // add binary resources
      for(final byte[] file : data.files(path)) tl.add(file);
    }
    tl.sort(!Prop.WIN);

    return new Iter() {
      int pos;
      @Override
      public Item get(final long i) { return Str.get(tl.get((int) i)); }
      @Override
      public Item next() { return pos < size() ? get(pos++) : null; }
      @Override
      public boolean reset() { pos = 0; return true; }
      @Override
      public long size() { return tl.size(); }
    };
  }

  /**
   * Performs the is-raw function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln isRaw(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    final String path = path(1, ctx);
    final IOFile io = data.meta.binary(path);
    return Bln.get(io.exists() && !io.isDir());
  }

  /**
   * Performs the content-type function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str contentType(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    final String path = path(1, ctx);
    final IOFile io = data.meta.binary(path);
    if(io.exists() && !io.isDir()) {
      final String ct = URLConnection.getFileNameMap().getContentTypeFor(path);
      return Str.get(ct == null ? DataText.APP_OCTET : ct);
    }
    if(isXML(ctx).bool(input)) return Str.get(DataText.APP_XML);
    throw RESFNF.thrw(input, path);
  }

  /**
   * Performs the is-xml function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln isXML(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    final String path = path(1, ctx);
    if(path.isEmpty()) return Bln.FALSE;

    // normalize path
    final byte[] exct = token(Prop.WIN ? path.toLowerCase() : path);
    final IntList il = data.docs(path);
    // check if one of the hits is exact, i.e., is no directory entry
    for(int i = 0; i < il.size(); i++) {
      final byte[] txt = data.text(il.get(i), true);
      if(eq(exct, Prop.WIN ? lc(txt) : txt)) return Bln.TRUE;
    }
    return Bln.FALSE;
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
    final String name = expr.length < 3 ? null : name(checkStr(expr[2], ctx));
    // ensure that the path is valid
    final String path = expr.length < 4 ? null : path(3, ctx);
    if(path != null && !new IOFile(path).valid()) RESINV.thrw(input, path);

    // get all items representing document(s):
    final ObjList<Item> docs = new ObjList<Item>(
        (int) Math.max(expr[1].size(), 1));
    final Iter iter = ctx.iter(expr[1]);
    for(Item i; (i = iter.next()) != null;) docs.add(i);

    if(docs.size() > 0) ctx.updates.add(
        new DBAdd(data, input, docs, name, path, ctx.context), ctx);

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
    final String trg = path(1, ctx);
    final Item doc = checkItem(expr[2], ctx);

    // collect all old documents
    final IntList old = data.docs(trg);
    if(old.size() > 0) {
      final int pre = old.get(0);
      if(old.size() > 1 || !eq(data.text(pre, true), token(trg)))
        DOCTRGMULT.thrw(input);
      ctx.updates.add(new DeleteNode(pre, data, input), ctx);
    }
    // delete raw resources
    final TokenList raw = Delete.files(data, trg);
    ctx.updates.add(new DBDelete(data, raw, input), ctx);

    final int p = trg.lastIndexOf('/');
    final String name = p < 0 ? trg : trg.substring(p + 1);
    final String path = p < 0 ? null : trg.substring(0, p);

    final ObjList<Item> docs = new ObjList<Item>(1);
    docs.add(doc);
    ctx.updates.add(new DBAdd(data, input, docs, name, path, ctx.context), ctx);

    if(data.meta.binary(path).exists()) {
      final byte[] val = checkBin(doc, ctx);
      ctx.updates.add(new DBPut(data, token(trg), val, input), ctx);
    }
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
    final String path = path(1, ctx);

    // delete XML resources
    final IntList docs = data.docs(path);
    for(int i = 0, is = docs.size(); i < is; i++) {
      ctx.updates.add(new DeleteNode(docs.get(i), data, input), ctx);
    }
    // delete raw resources
    final TokenList raw = Delete.files(data, path);
    ctx.updates.add(new DBDelete(data, raw, input), ctx);
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
    final String src = path(1, ctx);
    final String trg = path(2, ctx);
    if(!new IOFile(trg).valid()) RESINV.thrw(input, trg);

    // the first step of the path should be the database name
    final IntList il = data.docs(src);
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      final String target = Rename.target(data, pre, src, trg);
      if(target.isEmpty()) EMPTYPATH.thrw(input, this);
      ctx.updates.add(new ReplaceValue(pre, data, input, token(target)), ctx);
    }
    // rename files
    ctx.updates.add(new DBRename(data, data.meta.binary(src),
        data.meta.binary(trg), input), ctx);
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

    final Data data = data(0, ctx);
    final boolean all = expr.length == 2 && checkBln(expr[1], ctx);
    ctx.updates.add(new DBOptimize(data, ctx.context, all, input), ctx);
    return null;
  }

  /**
   * Performs the put function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item store(final QueryContext ctx) throws QueryException {
    checkWrite(ctx);

    final Data data = data(0, ctx);
    final String key = path(1, ctx);
    if(!new IOFile(key).valid()) RESINV.thrw(input, key);

    final byte[] val = checkBin(expr[2], ctx);
    ctx.updates.add(new DBPut(data, token(key), val, input), ctx);
    return null;
  }

  /**
   * Performs the get function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item retrieve(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    final String path = path(1, ctx);
    final IOFile file = data.meta.binary(path);
    if(!file.exists() || file.isDir()) RESFNF.thrw(input, path);
    return new Raw(file, path);
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
      final Serializer ser = Serializer.get(ao, ctx.serProp(true));
      final ValueIter ir = expr[1].value(ctx).iter();
      for(Item it; (it = ir.next()) != null;) it.serialize(ser);
      ser.close();
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
      def == Function.DBOPTIMIZE || def == Function.DBSTORE;
    return
      u == Use.CTX && (
        def == Function.DBTEXT || def == Function.DBATTR ||
        def == Function.DBFULLTEXT || def == Function.DBEVENT || up) ||
      u == Use.UPD && up ||
      super.uses(u);
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
   * Normalizes and checks the specified file name.
   * @param name input name
   * @return normalized path
   * @throws QueryException query exception
   */
  private String name(final byte[] name) throws QueryException {
    // check if path is valid
    final String nm = string(name);
    if(nm.endsWith(".") || nm.indexOf('/') != -1) RESINV.thrw(input, name);
    return nm;
  }

  /**
   * Normalizes the specified expression as normalized database path.
   * @param i expression index
   * @param ctx query context
   * @return normalized path
   * @throws QueryException query exception
   */
  private String path(final int i, final QueryContext ctx)
      throws QueryException {
    return IOFile.normalize(string(checkStr(expr[i], ctx)));
  }
}
