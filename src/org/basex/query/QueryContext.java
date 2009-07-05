package org.basex.query;

import static org.basex.Text.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.HashMap;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.core.proc.Open;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.io.IO;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Root;
import org.basex.query.ft.FTOpt;
import org.basex.query.ft.Scoring;
import org.basex.query.item.DBNode;
import org.basex.query.item.Dat;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Tim;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.path.AxisPath;
import org.basex.query.util.Err;
import org.basex.query.util.Functions;
import org.basex.query.util.NSLocal;
import org.basex.query.util.Variables;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.basex.util.Tokenizer;

/**
 * This abstract query expression provides the architecture
 * for a compiled query.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class QueryContext extends Progress {
  /** Cached stop word files. */
  public HashMap<String, String> stop;
  /** Cached thesaurus files. */
  public HashMap<String, String> thes;
  /** Reference to the query file. */
  public IO file = Prop.xquery;
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

  /** Full-text position data (for visualization). */
  public FTPosData ftpos;
  /** Full-text token counter (for visualization). */
  public byte ftoknum;
  /** Fast full-text evaluation (stop after first hit). */
  public boolean ftfast;

  /** Scoring instance. */
  public Scoring score = new Scoring();
  /** Current full-text options. */
  public FTOpt ftopt = new FTOpt();
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
  /** Static Base URI. */
  public Uri baseURI = Uri.EMPTY;
  /** Default element namespace. */
  public byte[] nsElem = EMPTY;
  /** Default collation. */
  public Uri collation = Uri.uri(URLCOLL);

  /** Default boundary-space. */
  public boolean spaces = false;
  /** Empty Order mode. */
  public boolean orderGreatest = false;

  /** Default encoding. */
  public byte[] encoding = token(Prop.ENCODING);
  /** Preserve Namespaces. */
  public boolean nsPreserve = true;
  /** Inherit Namespaces. */
  public boolean nsInherit = true;
  /** Ordering mode. */
  public boolean ordered = false;
  /** Construction mode. */
  public boolean construct = false;
  /** Revalidation Mode. */
  public int revalidate;

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
  /** Used documents. */
  private DBNode[] doc = new DBNode[1];
  /** Initial number of documents. */
  private int rootDocs;
  /** Number of documents. */
  private int docs;

  /**
   * Parses the specified query.
   * @param q input query
   * @throws QueryException xquery exception
   */
  public void parse(final String q) throws QueryException {
    query = q;
    root = new QueryParser(this).parse(q, file, null);
  }

  /**
   * Parses the specified module.
   * @param q input query
   * @throws QueryException xquery exception
   */
  public void module(final String q) throws QueryException {
    query = q;
    new QueryParser(this).parse(q, file, Uri.EMPTY);
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
        // create document nodes
        final Data data = nodes.data;
        for(int d = 0; d < nodes.size(); d++) {
          final int p = nodes.nodes[d];
          if(data.kind(p) == Data.DOC) {
            addDoc(new DBNode(data, p));
            rootDocs++;
          }
        }
        if(rootDocs == 0) {
          addDoc(new DBNode(data, 0));
          rootDocs++;
        }

        final SeqIter si = new SeqIter();
        if(root instanceof AxisPath && ((AxisPath) root).root instanceof Root) {
          // query starts with root node - add document nodes (optimization)
          for(int d = 0; d < docs; d++) si.add(doc[d]);
        } else {
          // otherwise, add all context items
          for(int n = 0; n < nodes.size(); n++)
            si.add(new DBNode(data, nodes.nodes[n]));
        }
        item = si.finish();

        // add collection instances
        final NodIter ni = new NodIter();
        for(int d = 0; d < docs; d++) ni.add(doc[d]);
        addColl(ni, token(data.meta.dbname));
      }

      // evaluates the query and returns the result
      if(Prop.allInfo) compInfo(QUERYCOMP);
      fun.comp(this);
      vars.comp(this);
      root = root.comp(this);
      if(Prop.allInfo) compInfo(QUERYRESULT + "%", root);
    } catch(final StackOverflowError e) {
      if(Prop.debug) e.printStackTrace();
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
      for(int p = 0; p < pre.size; p++) ir.add(new DBNode(data, pre.list[p]));
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
      return iter(root);
    } catch(final StackOverflowError e) {
      if(Prop.debug) e.printStackTrace();
      Err.or(XPSTACK);
      return null;
    }
  }

  /**
   * Serializes the specified item.
   * @param ser serializer
   * @param i item to serialize
   * @throws IOException query exception
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
   * @throws IOException query exception
   */
  void close() throws IOException {
    for(int d = rootDocs; d < docs; d++) doc[d].data.close();
  }

  /**
   * Adds some optimization info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void compInfo(final String string, final Object... ext) {
    if(!Prop.allInfo) return;
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
    if(!Prop.allInfo) return;
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
   * @param db database name or file path
   * @param coll collection flag
   * @return database instance
   * @throws QueryException evaluation exception
   */
  public DBNode doc(final byte[] db, final boolean coll) throws QueryException {
    if(contains(db, '<') || contains(db, '>')) Err.or(INVDOC, db);

    // check if the collections contain the document
    for(int c = 0; c < colls; c++) {
      for(int n = 0; n < collect[c].size; n++) {
        if(eq(db, collect[c].item[n].base()))
          return (DBNode) collect[c].item[n];
      }
    }

    // check if the database has already been opened
    final String dbname = string(db);
    for(int d = 0; d < docs; d++)
      if(doc[d].data.meta.dbname.equals(dbname)) return doc[d];

    // check if the document has already been opened
    final IO bxw = IO.get(string(db));
    for(int d = 0; d < docs; d++) {
      if(doc[d].data.meta.file.eq(bxw)) return doc[d];
    }

    // get database instance
    Data data = null;

    if(Prop.web) {
      try {
        data = Open.open(dbname);
      } catch(final IOException ex) {
        Err.or(INVDOC, dbname);
      }
    } else {
      data = check(dbname, file == null, coll);
      if(data == null) data = check(file.merge(bxw).path(), true, coll);
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
  private void addDoc(final DBNode node) {
    if(docs == doc.length) doc = Array.extend(doc);
    doc[docs++] = node;

  }

  /**
   * Opens the database or creates a new database instance
   * for the specified document.
   * @param path document path
   * @param err error flag
   * @param coll collection flag
   * @return data instance
   * @throws QueryException query exception
   */
  private Data check(final String path, final boolean err, final boolean coll)
      throws QueryException {

    try {
      return Check.check(path);
    } catch(final IOException ex) {
      if(err) Err.or(coll ? NOCOLL : NODOC, ex.getMessage());
      return null;
    }
  }

  /**
   * Adds a collection instance or returns an existing one.
   * @param coll name of the collection to be returned.
   * @return collection
   * @throws QueryException evaluation exception
   */
  public Iter coll(final byte[] coll) throws QueryException {
    // no collection specified.. return default collection/current context set
    if(coll == null) {
      if(colls == 0) Err.or(COLLDEF);
      return new SeqIter(collect[0].item, collect[0].size);
    }

    // invalid collection reference
    if(contains(coll, '<') || contains(coll, '\\'))
      Err.or(COLLINV, Err.chop(coll));

    int c = -1;
    while(true) {
      if(++c == colls) addDocs(doc(coll, true));
      else if(!eq(collName[c], coll)) continue;
      return new SeqIter(collect[c].item, collect[c].size);
    }
  }

  /**
   * Adds database documents as a collection.
   * @param db database reference
   */
  private void addDocs(final DBNode db) {
    final NodIter col = new NodIter();
    final Data data = db.data;
    for(int p = 0; p < data.meta.size;) {
      col.add(new DBNode(data, p));
      p += data.size(p, data.kind(p));
    }
    addColl(col, token(data.meta.dbname));
  }

  /**
   * Adds a collection.
   * @param ni collection nodes
   * @param name name
   */
  public void addColl(final NodIter ni, final byte[] name) {
    if(colls == collect.length) {
      collect = Array.extend(collect);
      collName = Array.extend(collName);
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
    return getClass().getSimpleName() + "[" + file + "]";
  }
}
