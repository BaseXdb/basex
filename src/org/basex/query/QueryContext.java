package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.core.proc.Close;
import org.basex.core.proc.Open;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.io.IO;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Expr.Use;
import org.basex.query.ft.FTOpt;
import org.basex.query.ft.Scoring;
import org.basex.query.item.DBNode;
import org.basex.query.item.Dat;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.Tim;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.PendingUpdates;
import org.basex.query.util.Err;
import org.basex.query.util.Functions;
import org.basex.query.util.NSLocal;
import org.basex.query.util.Variables;
import org.basex.util.IntList;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.basex.util.Tokenizer;

/**
 * This abstract query expression provides the architecture for a compiled
 * query. // *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class QueryContext extends Progress {
  /** Database context. */
  public final Context context;

  /** Cached stop word files. */
  public HashMap<String, String> stop;
  /** Cached thesaurus files. */
  public HashMap<String, String> thes;
  /** Query string. */
  public String query;

  /** Namespaces. */
  public NSLocal ns = new NSLocal();
  /** Functions. */
  public Functions fun = new Functions();
  /** Variables. */
  public Variables vars = new Variables();

  /** Current context. */
  public Item item;
  /** Current context position. */
  public long pos;
  /** Current context size. */
  public long size;
  /** Current leaf flag. */
  public boolean leaf;

  /** Used documents. */
  public DBNode[] doc = new DBNode[1];
  /** Number of documents. */
  public int docs;

  /** Full-text position data (for visualization). */
  public FTPosData ftpos;
  /** Full-text token counter (for visualization). */
  public byte ftoknum;
  /** Fast full-text evaluation (stop after first hit). */
  public boolean ftfast = true;

  /** Scoring instance. */
  public Scoring score = new Scoring();
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
  /** Revalidation Mode. */
  public int revalidate;
  /** Default encoding. */
  byte[] encoding = token(Prop.ENCODING);

  /** String container for query background information. */
  final TokenBuilder info = new TokenBuilder();
  /** Optimization flag. */
  boolean firstOpt = true;
  /** Evaluation flag. */
  boolean firstEval = true;

  /** List of modules. */
  StringList modules = new StringList();
  /** List of loaded modules. */
  StringList modLoaded = new StringList();
  /** Initial context set (default: null). */
  Nodes nodes;

  /** Collections. */
  private NodIter[] collect = new NodIter[1];
  /** Collection names. */
  private byte[][] collName = new byte[1][];
  /** Collection counter. */
  private int colls;

  /** Reference to the root expression. */
  private Expr root;
  /** Initial number of documents. */
  private int rootDocs;
  /** Info flag. */
  private final boolean inf;

  /** Pending updates. */
  public PendingUpdates updates = new PendingUpdates(false);
  /** Indicates if this query performs updates. */
  public boolean updating;

  /**
   * Constructor.
   * @param ctx context reference
   */
  public QueryContext(final Context ctx) {
    context = ctx;
    nodes = ctx.current;
    ftopt = new FTOpt(ctx.prop);
    inf = ctx.prop.is(Prop.ALLINFO);
    if(ctx.query != null) baseURI = Uri.uri(token(ctx.query.url()));
  }

  /**
   * Parses the specified query.
   * @param q input query
   * @throws QueryException query exception
   */
  public void parse(final String q) throws QueryException {
    query = q;
    root = new QueryParser(this).parse(q, file(), null);
  }

  /**
   * Parses the specified module.
   * @param q input query
   * @throws QueryException query exception
   */
  public void module(final String q) throws QueryException {
    query = q;
    new QueryParser(this).parse(q, file(), Uri.EMPTY);
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
        if(nodes.doc) doc = new DBNode[nodes.size()];

        // create document nodes
        final int dl = nodes.size();
        for(int d = 0; d < dl; d++) {
          final int p = nodes.nodes[d];
          if(nodes.doc || data.kind(p) == Data.DOC) {
            addDoc(new DBNode(data, p, Data.DOC));
          }
        }
        if(docs == 0) {
          for(final int p : data.doc()) addDoc(new DBNode(data, p));
        }
        rootDocs = docs;

        // create initial context items
        if(nodes.doc || !root.uses(Use.ELM, this)) {
          // optimization: all items are documents, or all query expressions
          // start with root node
          item = Seq.get(doc, docs);
        } else {
          // otherwise, add all context items
          final SeqIter si = new SeqIter(nodes.size());
          for(int n = 0; n < nodes.size(); n++) {
            si.add(new DBNode(data, nodes.nodes[n]));
          }
          item = si.finish();
        }

        // add collection instances
        addColl(new NodIter(doc, docs), token(data.meta.name));
      }
      if(doc == null) doc = new DBNode[1];

      // evaluates the query and returns the result
      if(inf) compInfo(QUERYCOMP);
      fun.comp(this);
      vars.comp(this);
      root = root.comp(this);
      if(inf) compInfo(QUERYRESULT + "%" + NL, root);
    } catch(final StackOverflowError ex) {
      if(Prop.debug) ex.printStackTrace();
      Err.or(XPSTACK);
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
    final SeqIter ir = new SeqIter(this);
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
      if(i == null) {
        final Nodes n = new Nodes(pre.finish(), data);
        n.ftpos = ftpos;
        return n;
      }

      // add nodes to standard iterator
      final int ps = pre.size();
      for(int p = 0; p < ps; p++)
        ir.add(new DBNode(data, pre.get(p)));
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
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    try {
      final Iter iter = iter(root);
      if(!updating) return iter;

      final Item i = iter.finish();
      updates.apply();
      if(context.data != null) context.update();
      return i.iter();
    } catch(final StackOverflowError ex) {
      if(Prop.debug) ex.printStackTrace();
      Err.or(XPSTACK);
      return null;
    }
  }

  /**
   * Serializes the specified item.
   * @param ser serializer
   * @param i item to serialize
   * @throws IOException I/O exception
   */
  public void serialize(final Serializer ser, final Item i) throws IOException {
    i.serialize(ser);
  }

  /**
   * Recursively serializes the query plan.
   * @param ser serializer
   * @throws Exception exception
   */
  protected void plan(final Serializer ser) throws Exception {
    fun.plan(ser);
    root.plan(ser);
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
    for(int d = rootDocs; d < docs; d++) Close.close(context, doc[d].data);
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
    info.add(string, ext);
    info.add(NL);
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void evalInfo(final String string, final Object... ext) {
    if(!inf) return;
    if(firstEval) {
      info.add(NL);
      info.add(QUERYEVAL);
      info.add(NL);
    }
    info.add(QUERYSEP);
    info.add(string, ext);
    info.add(NL);
    firstEval = false;
  }

  /**
   * Adds a database instance or returns an existing one.
   * @param path database name or file path
   * @param coll collection flag
   * @param db database flag
   * @return database instance
   * @throws QueryException query exception
   */
  public DBNode doc(final byte[] path, final boolean coll, final boolean db)
      throws QueryException {
    if(contains(path, '<') || contains(path, '>')) Err.or(INVDOC, path);

    // check if the collections contain the document
    for(int c = 0; c < colls; c++) {
      for(int n = 0; n < collect[c].size(); n++) {
        if(eq(path, collect[c].get(n).base())) {
          return (DBNode) collect[c].get(n);
        }
      }
    }

    // check if the database has already been opened
    final String nm = string(path);
    for(int d = 0; d < docs; d++)
      if(doc[d].data.meta.name.equals(nm)) return doc[d];

    // check if the document has already been opened
    final IO io = IO.get(string(path));
    for(int d = 0; d < docs; d++) {
      if(doc[d].data.meta.file.eq(io)) return doc[d];
    }

    // get database instance
    Data data = null;

    if(db) {
      try {
        data = Open.open(context, nm);
      } catch(final IOException ex) {
        Err.or(db ? NODB : NODOC, nm);
      }
    } else {
      final IO file = file();
      data = check(nm, file == null, coll);
      if(data == null) data = check(file.merge(io).path(), true, coll);
    }

    // add document to array
    final DBNode node = new DBNode(data, 0);
    addDoc(node);
    return node;
  }

  /**
   * Adds a document to the document array.
   * @param node node to be added
   */
  public void addDoc(final DBNode node) {
    if(docs == doc.length) doc = Arrays.copyOf(doc, docs << 1);
    doc[docs++] = node;
  }

  /**
   * Opens the database or creates a new database instance for the specified
   * document.
   * @param path document path
   * @param err error flag
   * @param coll collection flag
   * @return data instance
   * @throws QueryException query exception
   */
  private Data check(final String path, final boolean err, final boolean coll)
      throws QueryException {

    try {
      return Check.check(context, path);
    } catch(final IOException ex) {
      if(err) Err.or(coll ? NOCOLL : NODOC, path);
      return null;
    }
  }

  /**
   * Adds a collection instance or returns an existing one.
   * @param coll name of the collection to be returned.
   * @return collection
   * @throws QueryException query exception
   */
  public Iter coll(final byte[] coll) throws QueryException {
    // no collection specified.. return default collection/current context set
    if(coll == null) {
      if(colls == 0) Err.or(COLLDEF);
      return new SeqIter(collect[0].item, collect[0].size());
    }

    // invalid collection reference
    if(contains(coll, '<') || contains(coll, '\\')) Err.or(COLLINV,
        Err.chop(coll));

    int c = -1;
    while(true) {
      if(++c == colls) addDocs(doc(coll, true, false));
      else if(!eq(collName[c], coll)) continue;
      return new SeqIter(collect[c].item, collect[c].size());
    }
  }

  /**
   * Adds database documents as a collection.
   * @param db database reference
   */
  private void addDocs(final DBNode db) {
    final NodIter col = new NodIter();
    final Data data = db.data;
    for(int p = 0; p < data.meta.size; p += data.size(p, data.kind(p))) {
      col.add(new DBNode(data, p));
    }
    addColl(col, token(data.meta.name));
  }

  /**
   * Adds a collection.
   * @param ni collection nodes
   * @param name name
   */
  public void addColl(final NodIter ni, final byte[] name) {
    if(colls == collect.length) {
      collect = Arrays.copyOf(collect, colls << 1);
      collName = Arrays.copyOf(collName, colls << 1);
    }
    collect[colls] = ni;
    collName[colls++] = name;
  }

  /**
   * Returns the common database reference of all items or null.
   * @return database reference or null
   * @throws QueryException query exception
   */
  public Data data() throws QueryException {
    if(item == null) return null;
    Data data = null;

    if(item.size(this) == docs && item instanceof Seq
        && ((Seq) item).val == doc) return doc[0].data;

    final Iter iter = item.iter();
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
   * Returns an IO representation of the base uri.
   * @return IO reference
   */
  public IO file() {
    return baseURI != Uri.EMPTY ? IO.get(string(baseURI.str())) : null;
  }

  /**
   * Returns query background information.
   * @return warning
   */
  public String info() {
    return info.toString();
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
    return Main.name(this) + '[' + file() + ']';
  }
}
