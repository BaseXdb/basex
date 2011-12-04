package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;

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
import org.basex.data.MetaData;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IOFile;
import org.basex.io.MimeTypes;
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
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.FNode;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.QNm;
import org.basex.query.item.Raw;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.ValueIter;
import org.basex.query.path.NameTest;
import org.basex.query.up.primitives.DBAdd;
import org.basex.query.up.primitives.DBDelete;
import org.basex.query.up.primitives.DBOptimize;
import org.basex.query.up.primitives.DBRename;
import org.basex.query.up.primitives.DBStore;
import org.basex.query.up.primitives.DeleteNode;
import org.basex.query.up.primitives.ReplaceValue;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

/**
 * Database functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
 */
public final class FNDb extends FuncCall {
  /** Resource element name. */
  private static final QNm RESOURCE = new QNm(token("resource"));
  /** Path element name. */
  private static final QNm PATH = new QNm(token("path"));
  /** Raw element name. */
  private static final QNm RAW = new QNm(token("raw"));
  /** Size element name. */
  private static final QNm SIZE = new QNm(token("size"));
  /** Content type element name. */
  private static final QNm CTYPE = new QNm(token("content-type"));
  /** Modified date element name. */
  private static final QNm MDATE = new QNm(token("modified-date"));
  /** MIME type application/xml. */
  private static final byte[] APP_XML = token(MimeTypes.APP_XML);

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
      case _DB_OPEN:      return open(ctx).iter();
      case _DB_TEXT:      return text(ctx);
      case _DB_ATTRIBUTE: return attribute(ctx);
      case _DB_FULLTEXT:  return fulltext(ctx);
      case _DB_LIST:      return list(ctx);
      case _DB_NODE_ID:   return node(ctx, true);
      case _DB_NODE_PRE:  return node(ctx, false);
      default:            return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      case _DB_OPEN: return open(ctx);
      default:     return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    switch(def) {
      case _DB_EVENT:        return event(ctx);
      case _DB_OPEN_ID:      return open(ctx, true);
      case _DB_OPEN_PRE:     return open(ctx, false);
      case _DB_SYSTEM:       return system(ctx);
      case _DB_INFO:         return info(ctx);
      case _DB_ADD:          return add(ctx);
      case _DB_DELETE:       return delete(ctx);
      case _DB_RENAME:       return rename(ctx);
      case _DB_REPLACE:      return replace(ctx);
      case _DB_OPTIMIZE:     return optimize(ctx);
      case _DB_STORE:        return store(ctx);
      case _DB_RETRIEVE:     return retrieve(ctx);
      case _DB_IS_RAW:       return isRaw(ctx);
      case _DB_EXISTS:       return exists(ctx);
      case _DB_IS_XML:       return isXML(ctx);
      case _DB_CONTENT_TYPE: return contentType(ctx);
      case _DB_DETAILS:      return details(ctx);
      default:               return super.item(ctx, ii);
    }
  }

  /**
   * Performs the open function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Value open(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    final String path = expr.length < 2 ? "" : path(1, ctx);
    return DBNodeSeq.get(data.docs(path), data, true, path.isEmpty());
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
    final QNm nm = new QNm(checkStr(name, ctx), ctx);
    if(!nm.hasPrefix()) nm.uri(ctx.ns.uri(EMPTY));

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
   * Performs the exists function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln exists(final QueryContext ctx) throws QueryException {
    try {
      final Data data = data(0, ctx);
      if(expr.length == 1) return Bln.TRUE;
      // check if raw file or XML document exists
      final String path = path(1, ctx);
      final IOFile io = data.meta.binary(path);
      return Bln.get(io.exists() && !io.isDir() || data.doc(path) != -1);
    } catch(final QueryException ex) {
      if(ex.err() == NODB) return Bln.FALSE;
      throw ex;
    }
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
    return Bln.get(data.doc(path) != -1);
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
    if(data.doc(path) != -1) return Str.get(MimeTypes.APP_XML);
    final IOFile io = data.meta.binary(path);
    if(!io.exists() || io.isDir()) RESFNF.thrw(input, path);
    return Str.get(MimeTypes.get(path));
  }

  /**
   * Performs the details function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item details(final QueryContext ctx) throws QueryException {
    final Data d = data(0, ctx);
    final String path = path(1, ctx);

    // xml resource
    final int pre = d.doc(path);
    if(pre != -1)
      return resource(token(path), false, 0, APP_XML, d.meta.time);

    // binary resource
    final IOFile io = d.meta.binary(path);
    if(!io.exists() || io.isDir()) RESFNF.thrw(input, path);
    return resource(token(path), true, io.length(),
        token(MimeTypes.get(path)), io.date());
  }

  /**
   * Create a <code>&lt;resource/&gt;</code> node.
   * @param path path
   * @param raw is the resource a raw file
   * @param size size
   * @param ctype content type
   * @param mdate modified date
   * @return <code>&lt;resource/&gt;</code> node
   */
  private static FNode resource(final byte[] path, final boolean raw,
      final long size, final byte[] ctype, final long mdate) {
    final FElem res = new FElem(RESOURCE).
        add(new FAttr(PATH, path)).
        add(new FAttr(RAW, token(raw))).
        add(new FAttr(CTYPE, ctype)).
        add(new FAttr(MDATE, token(mdate)));
    return raw ? res.add(new FAttr(SIZE, token(size))) : res;
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
    final Item it = checkItem(expr[1], ctx);
    final String path = expr.length < 3 ? "" : path(2, ctx);

    ctx.updates.add(new DBAdd(data, input, it, path, ctx.context), ctx);
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
    final String path = path(1, ctx);
    final Item doc = checkItem(expr[2], ctx);

    // collect all old documents
    final int pre = data.doc(path);
    if(pre != -1) {
      if(data.docs(path).size() != 1) DOCTRGMULT.thrw(input);
      ctx.updates.add(new DeleteNode(pre, data, input), ctx);
    }
    // delete raw resources
    final TokenList raw = Delete.files(data, path);
    ctx.updates.add(new DBDelete(data, raw, input), ctx);
    ctx.updates.add(new DBAdd(data, input, doc, path, ctx.context), ctx);

    final IOFile file = data.meta.binary(path);
    if(file != null && file.exists() && !file.isDir()) {
      final byte[] val = checkBin(doc, ctx);
      ctx.updates.add(new DBStore(data, token(path), val, input), ctx);
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

    // the first step of the path should be the database name
    final IntList il = data.docs(src);
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      final String target = Rename.target(data, pre, src, trg);
      if(target.isEmpty()) EMPTYPATH.thrw(input, this);
      ctx.updates.add(new ReplaceValue(pre, data, input, token(target)), ctx);
    }
    // rename files
    if(data.meta.binary(src) != null && data.meta.binary(trg) != null)
      ctx.updates.add(new DBRename(data, src, trg, input), ctx);

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
    final String path = path(1, ctx);
    final IOFile file = data.meta.binary(path);
    if(file == null || file.isDir()) RESINV.thrw(input, path);

    final byte[] val = checkBin(expr[2], ctx);
    ctx.updates.add(new DBStore(data, token(path), val, input), ctx);
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
    if(file == null || !file.exists() || file.isDir()) RESFNF.thrw(input, path);
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
        return Int.get(id ? node.data.id(node.pre) : node.pre);
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
  public boolean isVacuous() {
    return def == Function._DB_EVENT;
  }

  @Override
  public boolean uses(final Use u) {
    final boolean up =
      def == Function._DB_ADD || def == Function._DB_DELETE ||
      def == Function._DB_RENAME || def == Function._DB_REPLACE ||
      def == Function._DB_OPTIMIZE || def == Function._DB_STORE;
    return
      // skip evaluation at compile time
      u == Use.CTX && (
        def == Function._DB_TEXT || def == Function._DB_ATTRIBUTE ||
        def == Function._DB_FULLTEXT || def == Function._DB_EVENT || up) ||
      u == Use.UPD && up ||
      super.uses(u);
  }

  @Override
  public boolean iterable() {
    // index functions will always yield ordered and duplicate-free results
    return def == Function._DB_OPEN || def == Function._DB_TEXT ||
      def == Function._DB_ATTRIBUTE || def == Function._DB_FULLTEXT ||
      super.iterable();
  }

  /**
   * Returns the data instance for the specified argument.
   * @param i index of argument
   * @param ctx query context
   * @return data instance
   * @throws QueryException query exception
   */
  private Data data(final int i, final QueryContext ctx)
      throws QueryException {

    final Item it = checkEmpty(expr[i].item(ctx, input));
    final Type ip = it.type;
    if(ip.isNode()) return checkDBNode(it).data;
    if(ip.isString())  {
      final String name = string(it.string(input));
      if(!MetaData.validName(name, false)) INVDB.thrw(input, name);
      return ctx.resource.data(name, input);
    }
    throw STRNODTYPE.thrw(input, this, ip);
  }

  /**
   * Returns the specified expression as normalized database path.
   * Throws an exception if the path is invalid.
   * @param i index of argument
   * @param ctx query context
   * @return normalized path
   * @throws QueryException query exception
   */
  private String path(final int i, final QueryContext ctx)
      throws QueryException {

    final String path = string(checkStr(expr[i], ctx));
    final String norm = MetaData.normPath(path);
    if(norm == null) RESINV.thrw(input, path);
    return norm;
  }
}
