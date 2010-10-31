package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.data.SerializerProp;
import org.basex.io.IO;
import org.basex.query.expr.Expr;
import org.basex.query.ft.FTOpt;
import org.basex.query.ft.Scoring;
import org.basex.query.item.DBNode;
import org.basex.query.item.Dat;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.item.Tim;
import org.basex.query.item.Uri;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.ItemIter;
import org.basex.query.up.Updates;
import org.basex.query.util.Err;
import org.basex.query.util.Functions;
import org.basex.query.util.Namespaces;
import org.basex.query.util.Variables;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.IntList;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.basex.util.Tokenizer;
import org.basex.util.Util;

/**
 * This abstract query expression provides the architecture for a compiled
 * query. // *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class QueryContext extends Progress {
  /** Functions. */
  public final Functions funcs = new Functions();
  /** Variables. */
  public final Variables vars = new Variables();
  /** Scoring instance. */
  public final Scoring score = new Scoring();
  /** Namespaces. */
  public Namespaces ns = new Namespaces();

  /** Database context. */
  public final Context context;
  /** Query string. */
  public String query;
  /** XQuery version flag. */
  public boolean xquery11;

  /** Cached stop word files. */
  public HashMap<String, String> stop;
  /** Cached thesaurus files. */
  public HashMap<String, String> thes;

  /** Reference to the root expression. */
  public Expr root;
  /** Current context value. */
  public Value value;
  /** Current context position. */
  public long pos;
  /** Current context size. */
  public long size;

  /** Opened documents. */
  public DBNode[] doc = new DBNode[1];
  /** Number of documents. */
  public int docs;

  /** Current full-text options. */
  public FTOpt ftopt;
  /** Current full-text token. */
  public Tokenizer fttoken;

  /** Current Date. */
  public Dat date;
  /** Current DateTime. */
  public Dtm dtm;
  /** Current Time. */
  public Tim time;

  /** Default function namespace. */
  public byte[] nsFunc = FNURI;
  /** Default element namespace. */
  public byte[] nsElem = EMPTY;
  /** Static Base URI. */
  public Uri baseURI = Uri.EMPTY;
  /** Default collation. */
  public Uri collation = Uri.uri(URLCOLL);

  /** Default boundary-space. */
  public boolean spaces;
  /** Empty Order mode. */
  public boolean orderGreatest;
  /** Preserve Namespaces. */
  public boolean nsPreserve = true;
  /** Inherit Namespaces. */
  public boolean nsInherit = true;
  /** Ordering mode. */
  public boolean ordered;
  /** Construction mode. */
  public boolean construct;

  /** Full-text position data (only utilized by visualization). */
  public FTPosData ftpos;
  /** Full-text token counter (only utilized by visualization). */
  public byte ftoknum;

  /** Copied nodes, resulting from transform expression. */
  public final Set<Data> copiedNods = new HashSet<Data>();
  /** Pending updates. */
  public Updates updates = new Updates(false);
  /** Indicates if this query performs updates. */
  public boolean updating;

  /** Compilation flag: current node has leaves. */
  public boolean leaf;
  /** Compilation flag: FLWOR clause performs grouping. */
  public boolean grouping;
  /** Compilation flag: full-text evaluation can be stopped after first hit. */
  public boolean ftfast = true;

  /** String container for query background information. */
  private final TokenBuilder info = new TokenBuilder();
  /** Info flag. */
  private final boolean inf;
  /** Optimization flag. */
  private boolean firstOpt = true;
  /** Evaluation flag. */
  private boolean firstEval = true;

  /** List of modules. */
  final StringList modules = new StringList();
  /** List of loaded modules. */
  final StringList modLoaded = new StringList();
  /** Serializer options. */
  SerializerProp serProp;
  /** Initial context set (default: null). */
  Nodes nodes;

  /** Collections. */
  private NodIter[] coll = new NodIter[1];
  /** Collection names. */
  private byte[][] collName = new byte[1][];
  /** Collection counter. */
  private int colls;

  /** Initial number of documents. */
  private int rootDocs;

  /**
   * Constructor.
   * @param ctx context reference
   */
  public QueryContext(final Context ctx) {
    context = ctx;
    nodes = ctx.current;
    ftopt = new FTOpt(ctx.prop);
    xquery11 = ctx.prop.is(Prop.XQUERY11);
    inf = ctx.prop.is(Prop.QUERYINFO);
    if(ctx.query != null) baseURI = Uri.uri(token(ctx.query.url()));
  }

  /**
   * Parses the specified query.
   * @param q input query
   * @throws QueryException query exception
   */
  public void parse(final String q) throws QueryException {
    root = new QueryParser(q, this).parse(file(), null);
    query = q;
  }

  /**
   * Parses the specified module.
   * @param q input query
   * @throws QueryException query exception
   */
  public void module(final String q) throws QueryException {
    new QueryParser(q, this).parse(file(), Uri.EMPTY);
  }

  /**
   * Optimizes the expression.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    // add full-text container reference
    if(nodes != null && nodes.ftpos != null) ftpos = new FTPosData();

    try {
      // cache the initial context nodes
      if(nodes != null) {
        final Data data = nodes.data;
        if(!context.perm(User.READ, data.meta))
          Err.PERMNO.thrw(null, CmdPerm.READ);

        final int s = data.empty() ? 0 : (int) nodes.size();
        if(nodes.root) {
          // create document nodes
          doc = new DBNode[s];
          for(int n = 0; n < s; ++n) {
            addDoc(new DBNode(data, nodes.list[n], Data.DOC));
          }
        } else {
          final IntList il = data.doc();
          for(int p = 0; p < il.size(); p++)
            addDoc(new DBNode(data, il.get(p), Data.DOC));
        }
        rootDocs = docs;

        // create initial context items
        if(nodes.root) {
          value = Seq.get(doc, docs);
        } else {
          // otherwise, add all context items
          final ItemIter ir = new ItemIter(s);
          for(int n = 0; n < s; ++n) ir.add(new DBNode(data, nodes.list[n]));
          value = ir.finish();
        }
        // add default collection
        addCollection(new NodIter(doc, docs), token(data.meta.name));
      }

      // dump compilation info
      if(inf) compInfo(NL + QUERYCOMP);

      // cache initial context
      final boolean empty = value == null;
      if(empty) value = Item.DUMMY;

      // compile global functions.
      // variables will be compiled if called for the first time
      funcs.comp(this);
      // compile the expression
      root = root.comp(this);
      // reset initial context
      if(empty) value = null;

      // dump resulting query
      if(inf) info.add(NL + QUERYRESULT + funcs + root + NL);

    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      XPSTACK.thrw(null);
    }
  }

  /**
   * Evaluates the expression with the specified context set.
   * @return resulting value
   * @throws QueryException query exception
   */
  protected Result eval() throws QueryException {
    // evaluates the query
    final Iter it = iter();
    final ItemIter ir = new ItemIter();
    Item i;

    // check if all results belong to the database of the input context
    if(nodes != null) {
      final Data data = nodes.data;
      final IntList pre = new IntList();

      while((i = it.next()) != null) {
        checkStop();
        if(!(i instanceof DBNode)) break;
        final DBNode n = (DBNode) i;
        if(n.data != data) break;
        pre.add(((DBNode) i).pre);
      }

      // completed... return standard nodeset with full-text positions
      final int ps = pre.size();
      if(i == null)
        return ps == 0 ? ir : new Nodes(pre.toArray(), data, ftpos).checkRoot();

      // otherwise, add nodes to standard iterator
      for(int p = 0; p < ps; ++p) ir.add(new DBNode(data, pre.get(p)));
      ir.add(i);
    }

    // use standard iterator
    while((i = it.next()) != null) {
      checkStop();
      ir.add(i);
    }
    return ir;
  }

  /**
   * Determines if the given node has been constructed via a transform
   * expression.
   * @param nod node to be checked
   * @return true, if part of copied nodes
   */
  public boolean isCopiedNod(final Nod nod) {
    return nod instanceof DBNode && copiedNods.contains(((DBNode) nod).data);
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    try {
      final Iter iter = iter(root);
      if(!updating) return iter;

      final Value v = iter.finish();
      updates.apply(this);
      if(context.data != null) context.update();
      return v.iter(this);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      XPSTACK.thrw(null);
      return null;
    }
  }

  /**
   * Recursively serializes the query plan.
   * @param ser serializer
   * @throws Exception exception
   */
  protected void plan(final Serializer ser) throws Exception {
    // only show root node if functions or variables exist
    final boolean r = funcs.size() != 0 || vars.global().size != 0;
    if(r) ser.openElement(PLAN);
    funcs.plan(ser);
    root.plan(ser);
    if(r) ser.closeElement();
  }

  /**
   * Evaluates the specified expression and returns an iterator.
   * @param e expression to be evaluated
   * @return iterator
   * @throws QueryException query exception
   */
  public Iter iter(final Expr e) throws QueryException {
    checkStop();
    return e.iter(this);
  }

  /**
   * Closes the context.
   * @throws IOException I/O exception
   */
  void close() throws IOException {
    for(int d = rootDocs; d < docs; ++d) Close.close(context, doc[d].data);
    docs = 0;
  }

  /**
   * Copies properties of the specified context.
   * @param ctx context
   */
  public void copy(final QueryContext ctx) {
    baseURI = ctx.baseURI;
    spaces = ctx.spaces;
    construct = ctx.construct;
    nsInherit = ctx.nsInherit;
    nsPreserve = ctx.nsPreserve;
    collation = ctx.collation;
    nsElem = ctx.nsElem;
    nsFunc = ctx.nsFunc;
    orderGreatest = ctx.orderGreatest;
    ordered = ctx.ordered;
  }

  /**
   * Adds some optimization info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void compInfo(final String string, final Object... ext) {
    if(!inf) return;
    if(!firstOpt) info.add(QUERYSEP);
    firstOpt = false;
    info.addExt(string, ext);
    info.add(NL);
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   * @param msg message
   */
  public void evalInfo(final byte[] string, final String msg) {
    if(!inf) return;
    if(firstEval) info.add(NL + QUERYEVAL + NL);
    info.add(QUERYSEP);
    info.add(string);
    info.add(' ');
    info.add(msg);
    info.add(NL);
    firstEval = false;
  }

  /**
   * Opens an existing document/collection, or creates a new main memory
   * document instance.
   * @param input database name or file path
   * @param col collection flag
   * @param db database flag
   * @param ii input info
   * @return database instance
   * @throws QueryException query exception
   */
  public DBNode doc(final byte[] input, final boolean col, final boolean db,
      final InputInfo ii) throws QueryException {

    if(contains(input, '<') || contains(input, '>')) INVDOC.thrw(ii, input);

    // check if the existing collections contain the document
    for(int c = 0; c < colls; ++c) {
      for(int n = 0; n < coll[c].size(); ++n) {
        if(eq(input, coll[c].get(n).base())) return (DBNode) coll[c].get(n);
      }
    }

    // check if the database has already been opened
    final String in = string(input);
    for(int d = 0; d < docs; ++d) {
      if(doc[d].data.meta.name.equals(in)) return doc[d];
    }

    // check if the document has already been opened
    final IO io = IO.get(in);
    for(int d = 0; d < docs; ++d) {
      if(doc[d].data.meta.file.eq(io)) return doc[d];
    }

    // get database instance
    Data data = null;

    if(db) {
      try {
        data = Open.open(in, context);
      } catch(final IOException ex) {
        NODB.thrw(ii, in);
      }
    } else {
      final IO file = file();
      data = doc(in, file == null, col, ii);
      if(data == null) data = doc(file.merge(in).path(), true, col, ii);
    }

    // add node to global array
    final DBNode node = new DBNode(data, 0, Data.DOC);
    addDoc(node);
    return node;
  }

  /**
   * Opens the database or creates a new database instance for the specified
   * document.
   * @param path document path
   * @param err error flag
   * @param col collection flag
   * @param ii input info
   * @return data instance
   * @throws QueryException query exception
   */
  private Data doc(final String path, final boolean err, final boolean col,
      final InputInfo ii) throws QueryException {

    try {
      return Check.check(context, path);
    } catch(final IOException ex) {
      if(err) (col ? NOCOLL : NODOC).thrw(ii, ex.getMessage());
      return null;
    }
  }

  /**
   * Adds a collection instance or returns an existing one.
   * @param input name of the collection to be returned
   * @param ii input info
   * @return collection
   * @throws QueryException query exception
   */
  public NodIter coll(final byte[] input, final InputInfo ii)
      throws QueryException {

    // no collection specified.. return default collection/current context set
    int c = 0;
    if(input == null) {
      // no default collection was defined
      if(colls == 0) NODEFCOLL.thrw(ii);
    } else {
      // invalid collection reference
      if(contains(input, '<') || contains(input, '\\')) COLLINV.thrw(ii, input);
      // find specified collection
      while(c < colls && !eq(collName[c], input)) ++c;
      // add new collection if not found
      if(c == colls) {
        final int s = indexOf(input, '/');
        if(s == -1) {
          addDocs(doc(input, true, false, ii), EMPTY);
        } else {
          addDocs(doc(substring(input, 0, s), true, false, ii),
              substring(input, s + 1));
        }
      }
    }
    return new NodIter(coll[c].item, (int) coll[c].size());
  }

  /**
   * Adds database documents as a collection.
   * @param db database reference
   * @param input inner collection path
   */
  private void addDocs(final DBNode db, final byte[] input) {
    final NodIter col = new NodIter();
    final Data data = db.data;
    final IntList il = data.doc(string(input));
    for(int i = 0; i < il.size(); i++) col.add(new DBNode(data, il.get(i)));
    addCollection(col, token(data.meta.name));
  }

  /**
   * Adds a collection to the global document list.
   * @param node node to be added
   */
  private void addDoc(final DBNode node) {
    if(docs == doc.length) {
      final DBNode[] tmp = new DBNode[Array.newSize(docs)];
      System.arraycopy(doc, 0, tmp, 0, docs);
      doc = tmp;
    }
    doc[docs++] = node;
  }

  /**
   * Adds a collection to the global collection list.
   * @param ni collection nodes
   * @param name name
   */
  public void addCollection(final NodIter ni, final byte[] name) {
    if(colls == coll.length) {
      coll = Arrays.copyOf(coll, colls << 1);
      collName = Array.copyOf(collName, colls << 1);
    }
    coll[colls] = ni;
    collName[colls++] = name;
  }

  /**
   * Returns the common database reference of all items, or {@code null}.
   * @return database reference
   * @throws QueryException query exception
   */
  public Data data() throws QueryException {
    if(value == null) return null;
    if(docNodes()) return doc[0].data;

    final Iter iter = value.iter(this);
    Data data = null;
    Item it;
    while((it = iter.next()) != null) {
      if(!(it instanceof DBNode)) return null;
      final Data d = ((DBNode) it).data;
      if(data != null && d != data) return null;
      data = d;
    }
    return data;
  }

  /**
   * Returns true if the current context item contains all root document nodes.
   * @return result of check
   */
  public boolean docNodes() {
    return value instanceof Seq && value.sameAs(Seq.get(doc, docs));
  }

  /**
   * Returns an IO representation of the base uri.
   * @return IO reference
   */
  IO file() {
    return baseURI != Uri.EMPTY ? IO.get(string(baseURI.atom())) : null;
  }

  /**
   * Returns info on query compilation and evaluation.
   * @return query info
   */
  String info() {
    return info.toString();
  }
  
  /**
   * Returns the serialization properties.
   * @return serialization properties
   */
  public SerializerProp serProp() {
    return serProp;
  }

  @Override
  public String tit() {
    return QUERYEVAL;
  }

  @Override
  public String det() {
    return QUERYEVAL;
  }

  @Override
  public double prog() {
    return 0;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + file() + ']';
  }
}
