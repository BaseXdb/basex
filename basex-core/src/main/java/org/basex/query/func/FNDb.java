package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Database functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
 */
public final class FNDb extends StandardFunc {
  /** Element: parameters. */
  private static final QNm Q_OPTIONS = QNm.get("options");

  /** Resource element name. */
  private static final String SYSTEM = "system";
  /** Resource element name. */
  private static final String DATABASE = "database";
  /** Backup element name. */
  private static final String BACKUP = "backup";
  /** Resource element name. */
  private static final String RESOURCE = "resource";
  /** Resource element name. */
  private static final String RESOURCES = "resources";
  /** Path element name. */
  private static final String PATH = "path";
  /** Raw element name. */
  private static final String RAW = "raw";
  /** Size element name. */
  private static final String SIZE = "size";
  /** Content type element name. */
  private static final String CTYPE = "content-type";
  /** Modified date element name. */
  private static final String MDATE = "modified-date";

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNDb(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _DB_OPEN:            return open(qc).iter();
      case _DB_BACKUPS:         return backups(qc);
      case _DB_TEXT:            return valueAccess(true, qc).iter(qc);
      case _DB_TEXT_RANGE:      return rangeAccess(true, qc).iter(qc);
      case _DB_ATTRIBUTE:       return attribute(valueAccess(false, qc), qc, 2);
      case _DB_ATTRIBUTE_RANGE: return attribute(rangeAccess(false, qc), qc, 3);
      case _DB_LIST:            return list(qc);
      case _DB_LIST_DETAILS:    return listDetails(qc);
      case _DB_NODE_ID:         return node(qc, true);
      case _DB_NODE_PRE:        return node(qc, false);
      default:                  return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _DB_OPEN: return open(qc);
      default:       return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _DB_ADD:           return add(qc);
      case _DB_ALTER:         return copy(qc, false);
      case _DB_CONTENT_TYPE:  return contentType(qc);
      case _DB_COPY:          return copy(qc, true);
      case _DB_CREATE:        return create(qc);
      case _DB_CREATE_BACKUP: return createBackup(qc);
      case _DB_DELETE:        return delete(qc);
      case _DB_DROP:          return drop(qc);
      case _DB_DROP_BACKUP:   return dropBackup(qc);
      case _DB_EVENT:         return event(qc);
      case _DB_EXISTS:        return exists(qc);
      case _DB_EXPORT:        return export(qc);
      case _DB_FLUSH:         return flush(qc);
      case _DB_INFO:          return info(qc);
      case _DB_IS_RAW:        return isRaw(qc);
      case _DB_IS_XML:        return isXML(qc);
      case _DB_NAME:          return name(qc);
      case _DB_OPEN_ID:       return open(qc, true);
      case _DB_OPEN_PRE:      return open(qc, false);
      case _DB_OPTIMIZE:      return optimize(qc);
      case _DB_OUTPUT:        return output(qc);
      case _DB_PATH:          return path(qc);
      case _DB_RENAME:        return rename(qc);
      case _DB_REPLACE:       return replace(qc);
      case _DB_RESTORE:       return restore(qc);
      case _DB_RETRIEVE:      return retrieve(qc);
      case _DB_STORE:         return store(qc);
      case _DB_SYSTEM:        return system(qc);
      default:                return super.item(qc, ii);
    }
  }

  /**
   * Performs the open function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Value open(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = exprs.length < 2 ? "" : path(1, qc);
    return DBNodeSeq.get(data.resources.docs(path), data, true, path.isEmpty());
  }

  /**
   * Performs the open-id and open-pre function.
   * @param qc query context
   * @param id id flag
   * @return result
   * @throws QueryException query exception
   */
  private DBNode open(final QueryContext qc, final boolean id) throws QueryException {
    final Data data = checkData(qc);
    final int v = (int) checkItr(exprs[1], qc);
    final int pre = id ? data.pre(v) : v;
    if(pre >= 0 && pre < data.meta.size) return new DBNode(data, pre);
    throw BXDB_RANGE.get(info, data.meta.name, id ? "ID" : "pre", v);
  }

  /**
   * Returns an index accessor.
   * @param text text/attribute flag
   * @param qc query context
   * @return index accessor
   * @throws QueryException query exception
   */
  private ValueAccess valueAccess(final boolean text, final QueryContext qc)
      throws QueryException {
    return new ValueAccess(info, exprs[1], text, null, new IndexContext(checkData(qc), false));
  }

  /**
   * Returns a range index accessor.
   * @param text text/attribute flag
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private StringRangeAccess rangeAccess(final boolean text, final QueryContext qc)
      throws QueryException {

    final byte[] min = checkStr(exprs[1], qc);
    final byte[] max = checkStr(exprs[2], qc);
    final StringRange sr = new StringRange(text, min, true, max, true);
    return new StringRangeAccess(info, sr, new IndexContext(checkData(qc), false));
  }

  /**
   * Performs the attribute function.
   * @param ia index access
   * @param qc query context
   * @param a index of attribute argument
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter attribute(final IndexAccess ia, final QueryContext qc, final int a)
      throws QueryException {

    // no attribute specified
    if(exprs.length <= a) return ia.iter(qc);

    // parse and compile the name test
    final QNm nm = new QNm(checkStr(exprs[a], qc), sc);
    if(!nm.hasPrefix()) nm.uri(sc.ns.uri(Token.EMPTY));

    final NameTest nt = new NameTest(nm, NameTest.Kind.URI_NAME, true, sc.elemNS);
    // return empty sequence if test will yield no results
    if(!nt.optimize(qc)) return Empty.ITER;

    // wrap iterator with name test
    return new NodeIter() {
      final NodeIter ir = ia.iter(qc);
      @Override
      public ANode next() throws QueryException {
        ANode n;
        while((n = ir.next()) != null && !nt.eq(n));
        return n;
      }
    };
  }

  /**
   * Performs the list function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter list(final QueryContext qc) throws QueryException {
    final TokenList tl = new TokenList();
    final int el = exprs.length;
    if(el == 0) {
      for(final String s : qc.context.databases.listDBs()) tl.add(s);
    } else {
      final Data data = checkData(qc);
      final String path = string(el == 1 ? Token.EMPTY : checkStr(exprs[1], qc));
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
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter backups(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final String name = exprs.length == 0 ? null : string(checkStr(exprs[0], qc));

    final StringList backups = name == null ? qc.context.databases.backups() :
      qc.context.databases.backups(name);
    final IOFile dbpath = qc.context.globalopts.dbpath();
    return new Iter() {
      int up = -1;

      @Override
      public Item next() {
        if(++up >= backups.size()) return null;
        final String backup = backups.get(up);
        final long length = new IOFile(dbpath, backup + IO.ZIPSUFFIX).length();
        return new FElem(BACKUP).add(backup).add(SIZE, token(length));
      }
    };
  }

  /**
   * Performs the list-details function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter listDetails(final QueryContext qc) throws QueryException {
    if(exprs.length == 0) return listDBs(qc);

    final Data data = checkData(qc);
    final String path = string(exprs.length == 1 ? Token.EMPTY : checkStr(exprs[1], qc));
    final IntList il = data.resources.docs(path);
    final TokenList tl = data.resources.binaries(path);

    return new Iter() {
      final int is = il.size(), ts = tl.size();
      int ip, tp;
      @Override
      public ANode get(final long i) {
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
      public ANode next() {
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
   * @param qc query context
   * @return iterator
   */
  private Iter listDBs(final QueryContext qc) {
    final StringList sl = qc.context.databases.listDBs();
    return new Iter() {
      int pos;
      @Override
      public ANode get(final long i) throws QueryException {
        final String name = sl.get((int) i);
        final MetaData meta = new MetaData(name, qc.context);
        try {
          meta.read();
        } catch(final IOException ex) {
          throw BXDB_OPEN.get(info, ex);
        }

        final FElem res = new FElem(DATABASE);
        res.add(RESOURCES, token(meta.ndocs));
        res.add(MDATE, DateTime.format(new Date(meta.dbtime()), DateTime.FULL));
        res.add(SIZE, token(meta.dbsize()));
        if(qc.context.perm(Perm.CREATE, meta)) res.add(PATH, meta.original);
        res.add(name);
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
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln isRaw(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    if(data.inMemory()) return Bln.FALSE;
    final IOFile io = data.meta.binary(path);
    return Bln.get(io.exists() && !io.isDir());
  }

  /**
   * Performs the exists function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln exists(final QueryContext qc) throws QueryException {
    try {
      final Data data = checkData(qc);
      if(exprs.length == 1) return Bln.TRUE;
      // check if raw file or XML document exists
      final String path = path(1, qc);
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
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln isXML(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    return Bln.get(data.resources.doc(path) != -1);
  }

  /**
   * Performs the content-type function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Str contentType(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    final int pre = data.resources.doc(path);
    if(pre != -1) {
      // check mime type; return application/xml if returned string is not of type xml
      final String mt = MimeTypes.get(string(data.text(pre, true)));
      return Str.get(MimeTypes.isXML(mt) ? mt : MimeTypes.APP_XML);
    }
    if(!data.inMemory()) {
      final IOFile io = data.meta.binary(path);
      if(io.exists() && !io.isDir()) return Str.get(MimeTypes.get(path));
    }
    throw WHICHRES.get(info, path);
  }

  /**
   * Performs the export function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item export(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final Data data = checkData(qc);
    final String path = string(checkStr(exprs[1], qc));
    final Item it = exprs.length > 2 ? exprs[2].item(qc, info) : null;
    final SerializerOptions sopts = FuncOptions.serializer(it, info);
    try {
      Export.export(data, path, sopts, null);
    } catch(final IOException ex) {
      throw SERANY.get(info, ex);
    }
    return null;
  }

  /**
   * Performs the name function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str name(final QueryContext qc) throws QueryException {
    return Str.get(checkDBNode(exprs[0].item(qc, info)).data.meta.name);
  }

  /**
   * Performs the path function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str path(final QueryContext qc) throws QueryException {
    ANode node, par = checkNode(exprs[0], qc);
    do {
      node = par;
      par = node.parent();
    } while(par != null);
    final DBNode dbn = checkDBNode(node);
    return Str.get(dbn.data.text(dbn.pre, true));
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
  private static FNode resource(final byte[] path, final boolean raw, final long size,
      final byte[] ctype, final long mdate) {

    final String tstamp = DateTime.format(new Date(mdate), DateTime.FULL);
    final FElem res = new FElem(RESOURCE).add(path).
        add(RAW, token(raw)).add(CTYPE, ctype).add(MDATE, tstamp);
    return raw ? res.add(SIZE, token(size)) : res;
  }

  /**
   * Performs the system function.
   * @param qc query context
   * @return node
   */
  private static ANode system(final QueryContext qc) {
    return toNode(Info.info(qc.context), SYSTEM);
  }

  /**
   * Performs the info function.
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  private ANode info(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final boolean create = qc.context.user.has(Perm.CREATE);
    return toNode(InfoDB.db(data.meta, false, true, create), DATABASE);
  }

  /**
   * Converts the specified info string to a node fragment.
   * @param root name of the root node
   * @param str string to be converted
   * @return node
   */
  private static ANode toNode(final String str, final String root) {
    final FElem top = new FElem(root);
    FElem node = null;
    for(final String l : str.split("\r\n?|\n")) {
      final String[] cols = l.split(": ", 2);
      if(cols[0].isEmpty()) continue;

      final FElem n = new FElem(token(toName(cols[0])));
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
   * Converts the specified info key to an element name.
   * @param str string to be converted
   * @return resulting name
   */
  public static String toName(final String str) {
    return str.replaceAll("[ -:]", "").toLowerCase(Locale.ENGLISH);
  }

  /**
   * Performs the add function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item add(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final byte[] path = exprs.length < 3 ? Token.EMPTY : token(path(2, qc));
    final NewInput input = checkInput(checkItem(exprs[1], qc), path);
    final Options opts = checkOptions(3, Q_OPTIONS, new Options(), qc);
    qc.resources.updates().add(new DBAdd(data, input, opts, qc, info), qc);
    return null;
  }

  /**
   * Performs the replace function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item replace(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    final Item item = checkItem(exprs[2], qc);
    final Options opts = checkOptions(3, Q_OPTIONS, new Options(), qc);

    // remove old documents
    final Resources res = data.resources;
    final IntList pre = res.docs(path, true);
    final Updates updates = qc.resources.updates();
    for(int p = 0; p < pre.size(); p++) {
      updates.add(new DeleteNode(pre.get(p), data, info), qc);
    }

    // delete binary resources
    final IOFile bin = data.inMemory() ? null : data.meta.binary(path);
    if(bin != null) {
      if(bin.exists() || item instanceof Bin) {
        if(bin.isDir()) throw BXDB_DIR.get(info, path);
        updates.add(new DBStore(data, path, item, info), qc);
      } else {
        updates.add(new DBAdd(data, checkInput(item, token(path)), opts, qc, info), qc);
      }
    }
    return null;
  }

  /**
   * Performs the delete function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item delete(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);

    // delete XML resources
    final IntList docs = data.resources.docs(path);
    final int is = docs.size();
    final Updates updates = qc.resources.updates();
    for(int i = 0; i < is; i++) {
      updates.add(new DeleteNode(docs.get(i), data, info), qc);
    }
    // delete raw resources
    if(!data.inMemory()) {
      final IOFile bin = data.meta.binary(path);
      if(bin == null) throw UPDBDELERR.get(info, path);
      updates.add(new DBDelete(data, path, info), qc);
    }
    return null;
  }

  /**
   * Performs the copy function.
   * @param qc query context
   * @param keep keep copied database
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item copy(final QueryContext qc, final boolean keep) throws QueryException {
    final String name = string(checkStr(exprs[0], qc));
    final String newname = string(checkStr(exprs[1], qc));

    if(!Databases.validName(name)) throw BXDB_NAME.get(info, name);
    if(!Databases.validName(newname)) throw BXDB_NAME.get(info, newname);

    // source database does not exist
    final GlobalOptions goptions = qc.context.globalopts;
    if(!goptions.dbexists(name)) throw BXDB_WHICH.get(info, name);
    if(name.equals(newname)) throw BXDB_SAME.get(info, name, newname);

    qc.resources.updates().add(keep ? new DBCopy(name, newname, info, qc) :
      new DBAlter(name, newname, info, qc), qc);
    return null;
  }

  /**
   * Performs the create function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item create(final QueryContext qc) throws QueryException {
    final String name = string(checkStr(exprs[0], qc));
    if(!Databases.validName(name)) throw BXDB_NAME.get(info, name);

    final TokenList paths = new TokenList();
    if(exprs.length > 2) {
      final Iter ir = qc.iter(exprs[2]);
      for(Item it; (it = ir.next()) != null;) {
        final String path = string(checkStr(it));
        final String norm = MetaData.normPath(path);
        if(norm == null) throw RESINV.get(info, path);
        paths.add(norm);
      }
    }

    final int ps = paths.size();
    final List<NewInput> inputs = new ArrayList<>(ps);
    if(exprs.length > 1) {
      final Value val = qc.value(exprs[1]);
      // number of specified inputs and paths must be identical
      final long is = val.size();
      if(ps != 0 && is != ps) throw BXDB_CREATEARGS.get(info, is, ps);

      for(int i = 0; i < is; i++) {
        final byte[] path = i < ps ? paths.get(i) : Token.EMPTY;
        inputs.add(checkInput(val.itemAt(i), path));
      }
    }

    final Options opts = checkOptions(3, Q_OPTIONS, new Options(), qc);
    qc.resources.updates().add(new DBCreate(name, inputs, opts, qc, info), qc);
    return null;
  }

  /**
   * Performs the drop function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item drop(final QueryContext qc) throws QueryException {
    final String name = string(checkStr(exprs[0], qc));
    if(!Databases.validName(name)) throw BXDB_NAME.get(info, name);
    if(!qc.context.globalopts.dbexists(name)) throw BXDB_WHICH.get(info, name);
    qc.resources.updates().add(new DBDrop(name, info, qc), qc);
    return null;
  }

  /**
   * Performs the create-backup function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item createBackup(final QueryContext qc) throws QueryException {
    final String name = string(checkStr(exprs[0], qc));
    if(!Databases.validName(name)) throw BXDB_NAME.get(info, name);
    if(!qc.context.globalopts.dbexists(name)) throw BXDB_WHICH.get(info, name);

    qc.resources.updates().add(new BackupCreate(name, info, qc), qc);
    return null;
  }

  /**
   * Performs the drop-backup function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item dropBackup(final QueryContext qc) throws QueryException {
    final String name = string(checkStr(exprs[0], qc));
    if(!Databases.validName(name)) throw BXDB_NAME.get(info, name);

    final StringList backups = qc.context.databases.backups(name);
    if(backups.isEmpty()) throw BXDB_WHICHBACK.get(info, name);

    final Updates updates = qc.resources.updates();
    for(final String backup : backups) updates.add(new BackupDrop(backup, info, qc), qc);
    return null;
  }

  /**
   * Performs the restore function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item restore(final QueryContext qc) throws QueryException {
    // extract database name from backup file
    final String name = string(checkStr(exprs[0], qc));
    if(!Databases.validName(name)) throw BXDB_NAME.get(info, name);

    // find backup with or without date suffix
    final StringList backups = qc.context.databases.backups(name);
    if(backups.isEmpty()) throw BXDB_NOBACKUP.get(info, name);

    final String backup = backups.get(0);
    final String db = Databases.name(backup);
    qc.resources.updates().add(new DBRestore(db, backup, qc, info), qc);
    return null;
  }

  /**
   * Performs the rename function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item rename(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String source = path(1, qc);
    final String target = path(2, qc);

    // the first step of the path should be the database name
    final Updates updates = qc.resources.updates();
    final IntList il = data.resources.docs(source);
    final int is = il.size();
    for(int i = 0; i < is; i++) {
      final int pre = il.get(i);
      final String trg = Rename.target(data, pre, source, target);
      if(trg.isEmpty() || trg.endsWith("/") || trg.endsWith(".")) throw BXDB_RENAME.get(info, trg);
      updates.add(new ReplaceValue(pre, data, info, token(trg)), qc);
    }
    // rename files
    if(!data.inMemory()) {
      final IOFile src = data.meta.binary(source);
      final IOFile trg = data.meta.binary(target);
      if(src == null || trg == null) throw UPDBRENAMEERR.get(info, src);
      updates.add(new DBRename(data, src.path(), trg.path(), info), qc);
    }
    return null;
  }

  /**
   * Performs the optimize function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item optimize(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final boolean all = exprs.length > 1 && checkBln(exprs[1], qc);
    final Options opts = checkOptions(2, Q_OPTIONS, new Options(), qc);
    qc.resources.updates().add(new DBOptimize(data, all, opts, qc, info), qc);
    return null;
  }

  /**
   * Performs the store function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item store(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    final Item item = checkItem(exprs[2], qc);
    if(data.inMemory()) throw BXDB_MEM.get(info, data.meta.name);

    final IOFile file = data.meta.binary(path);
    if(file == null || file.isDir()) throw RESINV.get(info, path);
    qc.resources.updates().add(new DBStore(data, path, item, info), qc);
    return null;
  }

  /**
   * Performs the flush function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item flush(final QueryContext qc) throws QueryException {
    qc.resources.updates().add(new DBFlush(checkData(qc), info), qc);
    return null;
  }

  /**
   * Performs the retrieve function.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private B64Stream retrieve(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    if(data.inMemory()) throw BXDB_MEM.get(info, data.meta.name);

    final IOFile file = data.meta.binary(path);
    if(file == null || !file.exists() || file.isDir()) throw WHICHRES.get(info, path);
    return new B64Stream(file, IOERR);
  }

  /**
   * Performs the node-pre and node-id function.
   * @param qc query context
   * @param id id flag
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter node(final QueryContext qc, final boolean id) throws QueryException {
    return new Iter() {
      final Iter ir = qc.iter(exprs[0]);

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
   * @param qc query context
   * @return event result
   * @throws QueryException query exception
   */
  private Item event(final QueryContext qc) throws QueryException {
    final byte[] name = checkStr(exprs[0], qc);
    try {
      final ArrayOutput ao = qc.value(exprs[1]).serialize();
      // throw exception if event is unknown
      if(!qc.context.events.notify(qc.context, name, ao.finish())) throw BXDB_EVENT.get(info, name);
      return null;
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }

  /**
   * Updating function: creates output which will be returned to the user after the
   * pending update list has been processed.
   * @param qc query context
   * @return event result
   * @throws QueryException query exception
   */
  private Item output(final QueryContext qc) throws QueryException {
    if(qc.resources.updates().mod instanceof TransformModifier) throw BASX_DBTRANSFORM.get(info);
    cache(qc.iter(exprs[0]), qc.resources.output, qc);
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
      if(endsWith(path, '.') || endsWith(path, '/')) throw RESINV.get(info, path);

      // ensure that the final name is not empty
      ANode nd = (ANode) in;
      byte[] name = path;
      if(name.length == 0) {
        // adopt name from document node
        name = nd.baseURI();
        final Data d = nd.data();
        // adopt path if node is part of disk database. otherwise, only adopt file name
        final int i = d == null || d.inMemory() ? lastIndexOf(name, '/') : indexOf(name, '/');
        if(i != -1) name = substring(name, i + 1);
        if(name.length == 0) throw RESINV.get(info, name);
      }

      // adding a document node
      if(nd.type != NodeType.DOC) {
        if(nd.type == NodeType.ATT) throw UPDOCTYPE.get(info, nd);
        nd = new FDoc(name).add(nd);
      }
      ni.node = nd;
      ni.path = name;
      return ni;
    }

    if(!in.type.isStringOrUntyped()) throw STRNODTYPE.get(info, in.type, in);

    final QueryInput qi = new QueryInput(string(in.string(info)));
    if(!qi.input.exists()) throw WHICHRES.get(info, qi.original);

    // add slash to the target if the addressed file is an archive or directory
    String name = string(path);
    if(name.endsWith(".")) throw RESINV.get(info, path);
    if(!name.endsWith("/") && (qi.input.isDir() || qi.input.isArchive())) name += "/";
    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    // set name of document
    if(!name.isEmpty()) qi.input.name(name);
    // get name from io reference
    else if(!(qi.input instanceof IOContent)) name = qi.input.name();

    // ensure that the final name is not empty
    if(name.isEmpty()) throw RESINV.get(info, path);

    ni.io = qi.input;
    ni.dbname = token(name);
    ni.path = token(target);
    return ni;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(!oneOf(func, _DB_BACKUPS, _DB_NODE_ID, _DB_NODE_PRE, _DB_EVENT, _DB_OUTPUT, _DB_SYSTEM)) {
      if(exprs.length == 0) {
        if(!visitor.lock(null)) return false;
      } else {
        if(!dataLock(visitor, oneOf(func, _DB_COPY, _DB_ALTER) ? 2 : 1)) return false;
      }
    }
    return super.accept(visitor);
  }

  @Override
  public boolean iterable() {
    return oneOf(func, _DB_OPEN) || super.iterable();
  }

  /**
   * Returns the specified expression as normalized database path.
   * Throws an exception if the path is invalid.
   * @param i index of argument
   * @param qc query context
   * @return normalized path
   * @throws QueryException query exception
   */
  private String path(final int i, final QueryContext qc) throws QueryException {
    final String path = string(checkStr(exprs[i], qc));
    final String norm = MetaData.normPath(path);
    if(norm == null) throw RESINV.get(info, path);
    return norm;
  }
}
