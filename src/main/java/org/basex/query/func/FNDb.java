package org.basex.query.func;

import static org.basex.data.DataText.*;
import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
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
  static final QNm Q_SYSTEM = new QNm("system");
  /** Resource element name. */
  static final QNm Q_DATABASE = new QNm("database");
  /** Backup element name. */
  static final QNm Q_BACKUP = new QNm("backup");
  /** Resource element name. */
  static final QNm Q_RESOURCE = new QNm("resource");
  /** Resource element name. */
  static final QNm Q_RESOURCES = new QNm("resources");
  /** Path element name. */
  static final QNm Q_PATH = new QNm("path");
  /** Raw element name. */
  static final QNm Q_RAW = new QNm("raw");
  /** Size element name. */
  static final QNm Q_SIZE = new QNm("size");
  /** Content type element name. */
  static final QNm Q_CTYPE = new QNm("content-type");
  /** Modified date element name. */
  static final QNm Q_MDATE = new QNm("modified-date");

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
      case _DB_OPEN:            return open(ctx).iter();
      case _DB_BACKUPS:         return backups(ctx);
      case _DB_TEXT:            return valueAccess(true, ctx).iter(ctx);
      case _DB_TEXT_RANGE:      return rangeAccess(true, ctx).iter(ctx);
      case _DB_ATTRIBUTE:       return attribute(valueAccess(false, ctx), ctx, 2);
      case _DB_ATTRIBUTE_RANGE: return attribute(rangeAccess(false, ctx), ctx, 3);
      case _DB_FULLTEXT:        return fulltext(ctx);
      case _DB_LIST:            return list(ctx);
      case _DB_LIST_DETAILS:    return listDetails(ctx);
      case _DB_NODE_ID:         return node(ctx, true);
      case _DB_NODE_PRE:        return node(ctx, false);
      default:                  return super.iter(ctx);
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
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _DB_EVENT:        return event(ctx);
      case _DB_OUTPUT:       return output(ctx);
      case _DB_OPEN_ID:      return open(ctx, true);
      case _DB_OPEN_PRE:     return open(ctx, false);
      case _DB_SYSTEM:       return system(ctx);
      case _DB_INFO:         return info(ctx);
      case _DB_ADD:          return add(ctx);
      case _DB_DELETE:       return delete(ctx);
      case _DB_CREATE:       return create(ctx);
      case _DB_DROP:         return drop(ctx);
      case _DB_RENAME:       return rename(ctx);
      case _DB_REPLACE:      return replace(ctx);
      case _DB_OPTIMIZE:     return optimize(ctx);
      case _DB_STORE:        return store(ctx);
      case _DB_RETRIEVE:     return retrieve(ctx);
      case _DB_FLUSH:        return flush(ctx);
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
  private DBNode open(final QueryContext ctx, final boolean id) throws QueryException {
    final Data data = data(0, ctx);
    final int v = (int) checkItr(expr[1], ctx);
    final int pre = id ? data.pre(v) : v;
    if(pre < 0 || pre >= data.meta.size) BXDB_RANGE.thrw(info, this, v);
    return new DBNode(data, pre);
  }

  /**
   * Returns an index accessor.
   * @param text text/attribute flag
   * @param ctx query context
   * @return index accessor
   * @throws QueryException query exception
   */
  private ValueAccess valueAccess(final boolean text, final QueryContext ctx)
      throws QueryException {

    final IndexType it = text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    return new ValueAccess(info, expr[1], it, data(0, ctx), true);
  }

  /**
   * Returns a range index accessor.
   * @param text text/attribute flag
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private StringRangeAccess rangeAccess(final boolean text, final QueryContext ctx)
      throws QueryException {

    final byte[] min = checkStr(expr[1], ctx);
    final byte[] max = checkStr(expr[2], ctx);
    final IndexType it = text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    final StringRange sr = new StringRange(it, min, true, max, true);
    return new StringRangeAccess(info, sr, data(0, ctx), true);
  }

  /**
   * Performs the attribute function.
   * @param ia index access
   * @param ctx query context
   * @param a index of attribute argument
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter attribute(final IndexAccess ia, final QueryContext ctx, final int a)
      throws QueryException {

    // no attribute specified
    if(expr.length <= a) return ia.iter(ctx);

    // parse and compile the name test
    final QNm nm = new QNm(checkStr(expr[a], ctx), ctx);
    if(!nm.hasPrefix()) nm.uri(ctx.sc.ns.uri(Token.EMPTY));

    final NameTest nt = new NameTest(nm, NameTest.Mode.STD, true);
    // no results expected: return empty sequence
    if(!nt.compile(ctx)) return Empty.ITER;

    // wrap iterator with name test
    return new NodeIter() {
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
    return FNFt.search(data(0, ctx), ctx.value(expr[1]), null, this, ctx);
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
      for(final String s : ctx.context.databases.listDBs()) tl.add(s);
    } else {
      final Data data = data(0, ctx);
      final String path = string(el == 1 ? Token.EMPTY : checkStr(expr[1], ctx));
      // add xml resources
      final Resources res = data.resources;
      final IntList il = res.docs(path);
      final int is = il.size();
      for(int i = 0; i < is; i++) tl.add(data.text(il.get(i), true));
      // add binary resources
      for(final byte[] file : res.binaries(path)) tl.add(file);
    }
    tl.sort(Prop.CASE);

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
   * Performs the backups function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter backups(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    final String prefix = expr.length == 0 ? null :
      Token.string(checkStr(expr[0], ctx)) + '-';

    final StringList list = ctx.context.databases.backups(prefix);
    final IOFile dbpath = ctx.context.mprop.dbpath();
    return new Iter() {
      int up = -1;

      @Override
      public Item next() throws QueryException {
        if(++up >= list.size()) return null;
        final String name = list.get(up);
        final long length = new IOFile(dbpath, name).length();
        return new FElem(Q_BACKUP).add(name).add(Q_SIZE, Token.token(length));
      }
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
    final String path = string(expr.length == 1 ? Token.EMPTY : checkStr(expr[1], ctx));
    final IntList il = data.resources.docs(path);
    final TokenList tl = data.resources.binaries(path);

    return new Iter() {
      final int is = il.size(), ts = tl.size();
      int ip, tp;
      @Override
      public ANode get(final long i) throws QueryException {
        if(i < is) {
          final byte[] pt = data.text(il.get((int) i), true);
          return resource(pt, false, 0, token(MimeTypes.APP_XML), data.meta.time);
        }
        if(i < is + ts) {
          final byte[] pt = tl.get((int) i - is);
          final IOFile io = data.meta.binary(string(pt));
          return resource(pt, true, io.length(), token(MimeTypes.get(io.path())),
              io.timeStamp());
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
    final StringList sl = ctx.context.databases.listDBs();
    return new Iter() {
      int pos;
      @Override
      public ANode get(final long i) throws QueryException {
        final FElem res = new FElem(Q_DATABASE);
        final String name = sl.get((int) i);
        final MetaData meta = new MetaData(name, ctx.context);
        DataInput di = null;
        try {
          di = new DataInput(meta.dbfile(DATAINF));
          meta.read(di);
          res.add(Q_RESOURCES, token(meta.ndocs));
          res.add(Q_MDATE, DateTime.format(new Date(meta.dbtime()), DateTime.FULL));
          if(ctx.context.perm(Perm.CREATE, meta)) res.add(Q_PATH, meta.original);
          res.add(name);
        } catch(final IOException ex) {
          BXDB_OPEN.thrw(info, ex);
        } finally {
          if(di != null) try { di.close(); } catch(final IOException ignored) { }
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
    if(data.inMemory()) return Bln.FALSE;
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
      boolean raw = false;
      if(!data.inMemory()) {
        final IOFile io = data.meta.binary(path);
        raw = io.exists() && !io.isDir();
      }
      return Bln.get(raw || data.resources.doc(path) != -1);
    } catch(final QueryException ex) {
      if(ex.err() == BXDB_OPEN) return Bln.FALSE;
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
    if(!data.inMemory()) {
      final IOFile io = data.meta.binary(path);
      if(io.exists() && !io.isDir()) return Str.get(MimeTypes.get(path));
    }
    throw WHICHRES.thrw(info, path);
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
  static FNode resource(final byte[] path, final boolean raw, final long size,
      final byte[] ctype, final long mdate) {

    final String tstamp = DateTime.format(new Date(mdate), DateTime.FULL);
    final FElem res = new FElem(Q_RESOURCE).add(path).
        add(Q_RAW, token(raw)).add(Q_CTYPE, ctype).add(Q_MDATE, tstamp);
    return raw ? res.add(Q_SIZE, token(size)) : res;
  }

  /**
   * Performs the system function.
   * @param ctx query context
   * @return node
   */
  private static ANode system(final QueryContext ctx) {
    return toNode(Info.info(ctx.context), Q_SYSTEM);
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
    return toNode(InfoDB.db(data.meta, false, true, create), Q_DATABASE);
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
        if(!cols[1].isEmpty()) n.add(cols[1]);
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
    final Data data = checkWrite(data(0, ctx), ctx);
    final byte[] path = expr.length < 3 ? Token.EMPTY : token(path(2, ctx));
    final NewInput input = checkInput(checkItem(expr[1], ctx), path);
    ctx.updates.add(new DBAdd(data, input, ctx, info), ctx);
    return null;
  }

  /**
   * Performs the replace function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item replace(final QueryContext ctx) throws QueryException {
    final Data data = checkWrite(data(0, ctx), ctx);
    final String path = path(1, ctx);
    final NewInput input = checkInput(checkItem(expr[2], ctx), token(path));

    // remove old documents
    final Resources res = data.resources;
    final IntList pre = res.docs(path, true);
    for(int p = 0; p < pre.size(); p++) {
      ctx.updates.add(new DeleteNode(pre.get(p), data, info), ctx);
    }

    // delete binary resources
    final IOFile bin = data.inMemory() ? null : data.meta.binary(path);
    if(bin != null) {
      if(bin.exists()) {
        if(bin.isDir()) BXDB_SINGLE.thrw(info);
        ctx.updates.add(new DBStore(data, path, input, info), ctx);
      } else {
        ctx.updates.add(new DBAdd(data, input, ctx, info), ctx);
      }
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
    final Data data = checkWrite(data(0, ctx), ctx);
    final String path = path(1, ctx);

    // delete XML resources
    final IntList docs = data.resources.docs(path);
    final int is = docs.size();
    for(int i = 0; i < is; i++) {
      ctx.updates.add(new DeleteNode(docs.get(i), data, info), ctx);
    }
    // delete raw resources
    if(!data.inMemory()) {
      final IOFile bin = data.meta.binary(path);
      if(bin == null) UPDBDELERR.thrw(info, path);
      ctx.updates.add(new DBDelete(data, path, info), ctx);
    }
    return null;
  }

  /**
   * Performs the create function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item create(final QueryContext ctx) throws QueryException {
    final String name = string(checkStr(expr[0], ctx));
    if(!MetaData.validName(name, false)) BXDB_NAME.thrw(info, name);

    final TokenList paths = new TokenList();
    if(expr.length > 2) {
      final Iter ir = ctx.iter(expr[2]);
      for(Item it; (it = ir.next()) != null;) {
        final String path = string(checkStr(it));
        final String norm = MetaData.normPath(path);
        if(norm == null) RESINV.thrw(info, path);
        paths.add(norm);
      }
    }

    final int ps = paths.size();
    final List<NewInput> inputs = new ArrayList<NewInput>(ps);
    if(expr.length > 1) {
      final Value val = ctx.value(expr[1]);
      // number of specified inputs and paths must be identical
      final long is = val.size();
      if(ps != 0 && is != ps) BXDB_CREATEARGS.thrw(info, is, ps);

      for(int i = 0; i < is; i++) {
        final byte[] path = i < ps ? paths.get(i) : Token.EMPTY;
        inputs.add(checkInput(val.itemAt(i), path));
      }
    }

    ctx.updates.add(new DBCreate(info, name, inputs, ctx), ctx);
    return null;
  }

  /**
   * Performs the create function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item drop(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    final Data data = checkWrite(data(0, ctx), ctx);
    ctx.updates.add(new DBDrop(data, info, ctx), ctx);
    return null;
  }

  /**
   * Performs the rename function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item rename(final QueryContext ctx) throws QueryException {
    final Data data = checkWrite(data(0, ctx), ctx);
    final String source = path(1, ctx);
    final String target = path(2, ctx);

    // the first step of the path should be the database name
    final IntList il = data.resources.docs(source);
    final int is = il.size();
    for(int i = 0; i < is; i++) {
      final int pre = il.get(i);
      final String trg = Rename.target(data, pre, source, target);
      if(trg.isEmpty() || trg.endsWith("/") || trg.endsWith("."))
        BXDB_RENAME.thrw(info, this);
      ctx.updates.add(new ReplaceValue(pre, data, info, token(trg)), ctx);
    }
    // rename files
    if(!data.inMemory()) {
      final IOFile src = data.meta.binary(source);
      final IOFile trg = data.meta.binary(target);
      if(src == null || trg == null) UPDBRENAMEERR.thrw(info, src);
      ctx.updates.add(new DBRename(data, src.path(), trg.path(), info), ctx);
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
    final Data data = checkWrite(data(0, ctx), ctx);
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
    final Data data = checkWrite(data(0, ctx), ctx);
    final String path = path(1, ctx);
    if(data.inMemory()) BXDB_MEM.thrw(info, data.meta.name);
    final IOFile file = data.meta.binary(path);
    if(file == null || file.isDir()) RESINV.thrw(info, path);

    final Item it = checkItem(expr[2], ctx);
    ctx.updates.add(new DBStore(data, path, it, info), ctx);
    return null;
  }

  /**
   * Performs the flush function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item flush(final QueryContext ctx) throws QueryException {
    ctx.updates.add(new DBFlush(checkWrite(data(0, ctx), ctx), info), ctx);
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
    if(data.inMemory()) BXDB_MEM.thrw(info, data.meta.name);

    final IOFile file = data.meta.binary(path);
    if(file == null || !file.exists() || file.isDir()) WHICHRES.thrw(info, path);
    return new B64Stream(file, IOERR);
  }

  /**
   * Performs the node-pre and node-id function.
   * @param ctx query context
   * @param id id flag
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter node(final QueryContext ctx, final boolean id) throws QueryException {
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
    final ArrayOutput ao = ctx.value(expr[1]).serialize();
    // throw exception if event is unknown
    if(!ctx.context.events.notify(ctx.context, name, ao.toArray())) {
      BXDB_EVENT.thrw(info, name);
    }
    return null;
  }

  /**
   * Updating function: creates output which will be returned to the user after the
   * pending update list has been processed.
   * @param ctx query context
   * @return event result
   * @throws QueryException query exception
   */
  private Item output(final QueryContext ctx) throws QueryException {
    if(ctx.updates.mod instanceof TransformModifier) BASX_DBTRANSFORM.thrw(info);
    cache(ctx.iter(expr[0]), ctx.output, ctx);
    return null;
  }

  /**
   * Creates a {@link Data} instance for the specified document.
   * @param in input item
   * @param path optional path argument
   * @return database instance
   * @throws QueryException query exception
   */
  private NewInput checkInput(final Item in, final byte[] path) throws QueryException {
    final NewInput ni = new NewInput();

    if(in.type.isNode()) {
      if(endsWith(path, '.') || endsWith(path, '/')) RESINV.thrw(info, path);

      // ensure that the final name is not empty
      ANode nd = (ANode) in;
      byte[] name = path;
      if(name.length == 0) {
        // adopt name from document node
        name = nd.baseURI();
        final Data d = nd.data();
        // adopt path if node is part of disk database. otherwise, only adopt file name
        final int i = d == null || d.inMemory() ? lastIndexOf(name, '/') :
          indexOf(name, '/');
        if(i != -1) name = substring(name, i + 1);
        if(name.length == 0) RESINV.thrw(info, name);
      }

      // adding a document node
      if(nd.type != NodeType.DOC) {
        if(nd.type == NodeType.ATT) UPDOCTYPE.thrw(info, nd);
        nd = new FDoc(name).add(nd);
      }
      ni.node = nd;
      ni.path = name;
      return ni;
    }

    if(!in.type.isStringOrUntyped()) throw STRNODTYPE.thrw(info, this, in.type);

    final QueryInput qi = new QueryInput(Token.string(in.string(info)));
    if(!qi.io.exists()) WHICHRES.thrw(info, qi.original);

    // add slash to the target if the addressed file is an archive or directory
    String name = string(path);
    if(name.endsWith(".")) RESINV.thrw(info, path);
    if(!name.endsWith("/") && (qi.io.isDir() || qi.io.isArchive())) name += "/";
    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    // set name of document
    if(!name.isEmpty()) qi.io.name(name);
    // get name from io reference
    else if(!(qi.io instanceof IOContent)) name = qi.io.name();

    // ensure that the final name is not empty
    if(name.isEmpty()) RESINV.thrw(info, path);

    ni.io = qi.io;
    ni.dbname = token(name);
    ni.path = token(target);
    return ni;
  }

  @Override
  public boolean uses(final Use u) {
    final boolean up = oneOf(sig, _DB_ADD, _DB_DELETE, _DB_RENAME, _DB_REPLACE,
        _DB_OPTIMIZE, _DB_STORE, _DB_OUTPUT, _DB_FLUSH, _DB_CREATE, _DB_DROP);
    return
      // skip evaluation at compile time
      u == Use.NDT && (up || oneOf(sig, _DB_TEXT, _DB_ATTRIBUTE, _DB_TEXT_RANGE,
          _DB_ATTRIBUTE_RANGE, _DB_FULLTEXT, _DB_EVENT)) ||
      u == Use.UPD && up ||
      super.uses(u);
  }

  @Override
  public boolean databases(final StringList db) {
    if(!oneOf(_DB_SYSTEM, _DB_NODE_ID, _DB_NODE_PRE, _DB_EVENT, _DB_OUTPUT)) {
      if(expr.length == 0 || !(expr[0] instanceof Str)) return false;
      db.add(string(((Str) expr[0]).string()));
      return true;
    }
    return super.databases(db);
  }

  @Override
  public boolean iterable() {
    // index functions will always yield ordered and duplicate-free results
    return oneOf(sig, _DB_OPEN, _DB_TEXT, _DB_ATTRIBUTE, _DB_FULLTEXT) ||
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
  private String path(final int i, final QueryContext ctx) throws QueryException {
    final String path = string(checkStr(expr[i], ctx));
    final String norm = MetaData.normPath(path);
    if(norm == null) RESINV.thrw(info, path);
    return norm;
  }
}
