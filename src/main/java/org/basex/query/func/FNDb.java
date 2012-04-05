package org.basex.query.func;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.Date;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Database functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
 */
public final class FNDb extends StandardFunc {
  /** Resource element name. */
  static final QNm SYSTEM = new QNm("system");
  /** Resource element name. */
  static final QNm DATABASE = new QNm("database");
  /** Resource element name. */
  static final QNm RESOURCE = new QNm("resource");
  /** Resource element name. */
  static final QNm RESOURCES = new QNm("resources");
  /** Path element name. */
  static final QNm PATH = new QNm("path");
  /** Raw element name. */
  static final QNm RAW = new QNm("raw");
  /** Size element name. */
  static final QNm SIZE = new QNm("size");
  /** Content type element name. */
  static final QNm CTYPE = new QNm("content-type");
  /** Modified date element name. */
  static final QNm MDATE = new QNm("modified-date");
  /** MIME type application/xml. */
  static final byte[] APP_XML = token(MimeTypes.APP_XML);

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
    switch(sig) {
      case _DB_OPEN:         return open(ctx).iter();
      case _DB_TEXT:         return text(ctx);
      case _DB_ATTRIBUTE:    return attribute(ctx);
      case _DB_FULLTEXT:     return fulltext(ctx);
      case _DB_LIST:         return list(ctx);
      case _DB_LIST_DETAILS: return listDetails(ctx);
      case _DB_NODE_ID:      return node(ctx, true);
      case _DB_NODE_PRE:     return node(ctx, false);
      default:               return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _DB_OPEN: return open(ctx);
      default:       return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    switch(sig) {
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
    return DBNodeSeq.get(data.resources.docs(path), data, true, path.isEmpty());
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
    if(pre < 0 || pre >= data.meta.size) IDINVALID.thrw(info, this, v);
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
    return new IndexAccess(info, expr[1], IndexType.TEXT, ic).iter(ctx);
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
        info, expr[1], IndexType.ATTRIBUTE, ic);

    // return iterator if no name test is specified
    if(expr.length < 3) return ia.iter(ctx);

    // parse and compile the name test
    final Item name = checkNoEmpty(expr[2].item(ctx, info));
    final QNm nm = new QNm(checkStr(name, ctx), ctx);
    if(!nm.hasPrefix()) nm.uri(ctx.sc.ns.uri(EMPTY));

    final NameTest nt = new NameTest(nm, NameTest.Mode.STD, true);
    // no results expected: return empty sequence
    if(!nt.comp(ctx)) return Empty.ITER;

    // wrap iterator with name test
    return new Iter() {
      final NodeIter ir = ia.iter(ctx);
      @Override
      public ANode next() throws QueryException {
        ANode n;
        while((n = ir.next()) != null && !nt.eq(n));
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
      for(final String s : ctx.context.databases().listDBs())
        tl.add(s);
    } else {
      final Data data = data(0, ctx);
      final String path = string(el == 1 ? EMPTY : checkStr(expr[1], ctx));
      // add xml resources
      final Resources res = data.resources;
      final IntList il = res.docs(path);
      final int is = il.size();
      for(int i = 0; i < is; i++) tl.add(data.text(il.get(i), true));
      // add binary resources
      for(final byte[] file : res.binaries(path)) tl.add(file);
    }
    tl.sort(!Prop.WIN);

    return new Iter() {
      int pos;
      @Override
      public Str get(final long i) { return Str.get(tl.get((int) i)); }
      @Override
      public Str next() { return pos < size() ? get(pos++) : null; }
      @Override
      public boolean reset() { pos = 0; return true; }
      @Override
      public long size() { return tl.size(); }
    };
  }

  /**
   * Performs the list-details function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter listDetails(final QueryContext ctx) throws QueryException {
    if(expr.length == 0) return listDBs(ctx);

    final Data data = data(0, ctx);
    final String path = string(expr.length == 1 ? EMPTY : checkStr(expr[1], ctx));
    final IntList il = data.resources.docs(path);
    final TokenList tl = data.resources.binaries(path);

    return new Iter() {
      final int is = il.size(), ts = tl.size();
      int ip, tp;
      @Override
      public ANode get(final long i) throws QueryException {
        if(i < is) {
          final byte[] pt = data.text(il.get((int) i), true);
          return resource(pt, false, 0, APP_XML, data.meta.time);
        }
        if(i < is + ts) {
          final byte[] pt = tl.get((int) i - is);
          final IO io = data.meta.binary(string(pt));
          return resource(pt, true, io.length(),
              token(MimeTypes.get(io.path())), io.timeStamp());
        }
        return null;
      }
      @Override
      public ANode next() throws QueryException {
        return ip < is ? get(ip++) : tp < ts ? get(ip + tp++) : null;
      }
      @Override
      public boolean reset() { ip = 0; tp = 0; return true; }
      @Override
      public long size() { return ip + is; }
    };
  }


  /**
   * Performs the list-details for databases function.
   * @param ctx query context
   * @return iterator
   */
  private Iter listDBs(final QueryContext ctx) {
    final StringList sl = ctx.context.databases().listDBs();
    return new Iter() {
      int pos;
      @Override
      public ANode get(final long i) throws QueryException {
        final FElem res = new FElem(DATABASE);
        final String name = sl.get((int) i);
        final MetaData meta = new MetaData(name, ctx.context);
        DataInput di = null;
        try {
          di = new DataInput(meta.dbfile(DATAINF));
          meta.read(di);
          res.add(new FAttr(RESOURCES, token(meta.ndocs)));
          final String tstamp = Dtm.FORMAT.format(new Date(meta.dbtime()));
          res.add(new FAttr(MDATE, token(tstamp)));
          if(ctx.context.perm(Perm.CREATE, meta))
            res.add(new FAttr(PATH, token(meta.original)));
          res.add(new FTxt(token(name)));
        } catch(final IOException ex) {
          NODB.thrw(info, name);
        } finally {
          if(di != null) try { di.close(); } catch(final IOException ex) { }
        }
        return res;
      }
      @Override
      public ANode next() throws QueryException {
        return pos < size() ? get(pos++) : null;
      }
      @Override
      public boolean reset() { pos = 0; return true; }
      @Override
      public long size() { return sl.size(); }
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
      return Bln.get(io.exists() && !io.isDir() ||
          data.resources.doc(path) != -1);
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
    return Bln.get(data.resources.doc(path) != -1);
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
    if(data.resources.doc(path) != -1) return Str.get(MimeTypes.APP_XML);
    final IOFile io = data.meta.binary(path);
    if(!io.exists() || io.isDir()) RESFNF.thrw(info, path);
    return Str.get(MimeTypes.get(path));
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
  static FNode resource(final byte[] path, final boolean raw,
      final long size, final byte[] ctype, final long mdate) {

    final String tstamp = Dtm.FORMAT.format(new Date(mdate));
    final FElem res = new FElem(RESOURCE).
        add(new FTxt(path)).
        add(new FAttr(RAW, token(raw))).
        add(new FAttr(CTYPE, ctype)).
        add(new FAttr(MDATE, token(tstamp)));
    return raw ? res.add(new FAttr(SIZE, token(size))) : res;
  }

  /**
   * Performs the system function.
   * @param ctx query context
   * @return node
   */
  private static ANode system(final QueryContext ctx) {
    return toNode(Info.info(ctx.context), SYSTEM);
  }

  /**
   * Performs the info function.
   * @param ctx query context
   * @return node
   * @throws QueryException query exception
   */
  private ANode info(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    final boolean create = ctx.context.user.has(Perm.CREATE);
    return toNode(InfoDB.db(data.meta, false, true, create), DATABASE);
  }

  /**
   * Converts the specified info string to a node fragment.
   * @param root name of the root node
   * @param str string to be converted
   * @return node
   */
  private static ANode toNode(final String str, final QNm root) {
    final FElem top = new FElem(root);
    FElem node = null;
    for(final String l : str.split("\r\n?|\n")) {
      final String[] cols = l.split(": ", 2);
      if(cols[0].isEmpty()) continue;

      final String name = cols[0].replaceAll(" |-", "");
      final FElem n = new FElem(new QNm(lc(token(name))));
      if(cols[0].startsWith(" ")) {
        if(node != null) node.add(n);
        if(!cols[1].isEmpty()) n.add(new FTxt(token(cols[1])));
      } else {
        node = n;
        top.add(n);
      }
    }
    return top;
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

    ctx.updates.add(new DBAdd(data, info, it, path, ctx.context), ctx);
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
    final Resources res = data.resources;
    final int pre = res.doc(path);
    if(pre != -1) {
      if(res.docs(path).size() != 1) DOCTRGMULT.thrw(info);
      ctx.updates.add(new DeleteNode(pre, data, info), ctx);
    }
    // delete binary resources
    final IOFile bin = data.meta.binary(path);
    if(bin != null) ctx.updates.add(new DBDelete(data, path, info), ctx);
    ctx.updates.add(new DBAdd(data, info, doc, path, ctx.context), ctx);

    final IOFile file = data.meta.binary(path);
    if(file != null && file.exists() && !file.isDir()) {
      final Item it = checkItem(doc, ctx);
      ctx.updates.add(new DBStore(data, token(path), it, info), ctx);
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
    final IntList docs = data.resources.docs(path);
    for(int i = 0, is = docs.size(); i < is; i++) {
      ctx.updates.add(new DeleteNode(docs.get(i), data, info), ctx);
    }
    // delete raw resources
    final IOFile bin = data.meta.binary(path);
    if(bin == null) UPDBDELERR.thrw(info, path);
    ctx.updates.add(new DBDelete(data, path, info), ctx);
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
    final String source = path(1, ctx);
    final String target = path(2, ctx);

    // the first step of the path should be the database name
    final IntList il = data.resources.docs(source);
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      final String trg = Rename.target(data, pre, source, target);
      if(trg.isEmpty()) EMPTYPATH.thrw(info, this);
      ctx.updates.add(new ReplaceValue(pre, data, info, token(trg)), ctx);
    }
    // rename files
    final IOFile src = data.meta.binary(source);
    final IOFile trg = data.meta.binary(target);
    if(src == null || trg == null) UPDBRENAMEERR.thrw(info, src);

    ctx.updates.add(new DBRename(data, src.path(), trg.path(), info), ctx);
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
    ctx.updates.add(new DBOptimize(data, ctx.context, all, info), ctx);
    return null;
  }

  /**
   * Performs the store function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item store(final QueryContext ctx) throws QueryException {
    checkWrite(ctx);

    final Data data = data(0, ctx);
    final String path = path(1, ctx);
    final IOFile file = data.meta.binary(path);
    if(file == null || file.isDir()) RESINV.thrw(info, path);

    final Item it = checkItem(expr[2], ctx);
    ctx.updates.add(new DBStore(data, token(path), it, info), ctx);
    return null;
  }

  /**
   * Performs the retrieve function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private B64Stream retrieve(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    final String path = path(1, ctx);
    final IOFile file = data.meta.binary(path);
    if(file == null || !file.exists() || file.isDir()) RESFNF.thrw(info, path);
    return new B64Stream(file, DBERR);
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
      public Int next() throws QueryException {
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
      final Serializer ser = Serializer.get(ao, ctx.serParams(true));
      final ValueIter ir = ctx.value(expr[1]).iter();
      for(Item it; (it = ir.next()) != null;) it.serialize(ser);
      ser.close();
    } catch(final SerializerException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      SERANY.thrw(info, ex);
    }
    // throw exception if event is unknown
    if(!ctx.context.events.notify(ctx.context, name, ao.toArray())) {
      NOEVENT.thrw(info, name);
    }
    return null;
  }

  @Override
  public boolean isVacuous() {
    return sig == Function._DB_EVENT;
  }

  @Override
  public boolean uses(final Use u) {
    final boolean up =
      sig == Function._DB_ADD || sig == Function._DB_DELETE ||
      sig == Function._DB_RENAME || sig == Function._DB_REPLACE ||
      sig == Function._DB_OPTIMIZE || sig == Function._DB_STORE;
    return
      // skip evaluation at compile time
      u == Use.CTX && (
        sig == Function._DB_TEXT || sig == Function._DB_ATTRIBUTE ||
        sig == Function._DB_FULLTEXT || sig == Function._DB_EVENT || up) ||
      u == Use.UPD && up ||
      super.uses(u);
  }

  @Override
  public boolean iterable() {
    // index functions will always yield ordered and duplicate-free results
    return sig == Function._DB_OPEN || sig == Function._DB_TEXT ||
      sig == Function._DB_ATTRIBUTE || sig == Function._DB_FULLTEXT ||
      super.iterable();
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
    if(norm == null) RESINV.thrw(info, path);
    return norm;
  }
}
