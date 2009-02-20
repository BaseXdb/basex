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
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.io.IO;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Root;
import org.basex.query.ft.FTOpt;
import org.basex.query.ft.FTPos;
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

/**
 * This abstract query expression provides the architecture
 * for a compiled query.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class QueryContext extends Progress {
  /** Cached stop word files. */
  public HashMap<String, String> stop;
  /** Reference to the query file. */
  public IO file = Prop.xquery;
  /** Query string. */
  public String query;
  
  /** Maximum number of evaluation dumps. */
  protected static final int MAXDUMP = 16;
  /** Query info counter. */
  protected int cc;
  /** Current evaluation time. */
  protected long evalTime;
  /** Info flag. */
  protected boolean inf;

  /** String container for query background information. */
  protected final TokenBuilder info = new TokenBuilder();
  /** Optimization flag. */
  protected boolean firstOpt = true;
  /** Evaluation flag. */
  protected boolean firstEval = true;

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

  /** Current fulltext item. */
  public FTPosData ftdata;
  /** Current fulltext item. */
  public FTTokenizer ftitem;
  /** Current fulltext options. */
  public FTOpt ftopt = new FTOpt();
  /** Current fulltext position filter. */
  public FTPos ftpos = new FTPos(null);
  /** Count number of FTIndex. */
  public int ftcount = 0;
  /** Temporary place for ftdata. */
  public IntList[] ftd;
  
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

  /** Used documents. */
  public DBNode[] docs = {};
  /** Initial number of documents. */
  public int rootDocs;

  /** List of modules. */
  StringList modules = new StringList();
  /** List of loaded modules. */
  StringList modLoaded = new StringList();
  /** Collections. */
  NodIter[] collect = new NodIter[0];
  /** Collection names. */
  byte[][] collName = new byte[0][];

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
  public int revalidate = 0;

  /** Reference to the root expression. */
  private Expr root;

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
   * @param nodes initial node set
   * @throws QueryException query exception
   */
  public void compile(final Nodes nodes) throws QueryException {
    try {
      // cache the initial context nodes
      final int s = nodes != null ? nodes.size() : 0;
      if(s != 0) {
        // create document nodes
        final Data data = nodes.data;
        docs = new DBNode[s];
        for(int d = 0; d < s; d++) {
          final int p = nodes.nodes[d];
          if(data.kind(p) == Data.DOC) docs[rootDocs++] = new DBNode(data, p);
        }
        if(rootDocs == 0) docs[rootDocs++] = new DBNode(data, 0);
        if(rootDocs != docs.length) docs = Array.finish(docs, rootDocs);

        final SeqIter si = new SeqIter();
        if(root instanceof AxisPath && ((AxisPath) root).root instanceof Root) {
          // query starts with root node - add document nodes
          for(final DBNode d : docs) si.add(d);
        } else {
          // otherwise, add all context items
          for(int d = 0; d < s; d++) si.add(new DBNode(data, nodes.nodes[d]));
        }
        item = si.finish();
        
        // add collection instances
        final NodIter ni = new NodIter();
        for(final DBNode d : docs) ni.add(d);
        addColl(ni, token(data.meta.dbname));
      }

      // evaluates the query and returns the result
      inf = Prop.allInfo;
      if(inf) compInfo(QUERYCOMP);
      fun.comp(this);
      vars.comp(this);
      root = root.comp(this);
      if(inf) compInfo(QUERYRESULT + "%", root);

      evalTime = System.nanoTime();
    } catch(final StackOverflowError e) {
      if(Prop.debug) e.printStackTrace();
      Err.or(XPSTACK);
    }
  }

  /**
   * Evaluates the expression with the specified context set.
   * @param nodes initial node set
   * @return resulting value
   * @throws QueryException query exception
   */
  protected Result eval(final Nodes nodes) throws QueryException {
    // add fulltext container reference
    if(nodes != null && nodes.ftpos != null) ftdata = new FTPosData();
    
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
      
      // completed... return standard nodeset with fulltext positions
      if(i == null) {
        final Nodes n = new Nodes(pre.finish(), data);
        n.ftpos = ftdata;
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
    ser.ns.reset();
    i.serialize(ser);
  }
  
  /**
   * Recursively serializes the query plan.
   * @param ser serializer
   * @throws Exception exception
   */
  protected void plan(final Serializer ser) throws Exception {
    //vars.plan(ser);
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
   * @param db database name or file path
   * @param coll collection flag
   * @return database instance
   * @throws QueryException evaluation exception
   */
  public DBNode doc(final byte[] db, final boolean coll) throws QueryException {
    if(contains(db, '<') || contains(db, '>')) Err.or(INVDOC, db);

    // check if the collections contain the document
    for(final NodIter ni : collect) {
      for(int n = 0; n < ni.size; n++) {
        if(eq(db, ni.list[n].base())) return (DBNode) ni.list[n];
      }
    }

    // check if the database has already been opened
    final String dbname = string(db);
    for(final DBNode d : docs) if(d.data.meta.dbname.equals(dbname)) return d;

    // check if the document has already been opened
    final IO bxw = IO.get(string(db));
    for(final DBNode d : docs) if(d.data.meta.file.eq(bxw)) return d;

    // get database instance
    Data data = null;
    String msg = bxw.toString();
    try {
      data = Check.check(dbname);
    } catch(final IOException ex) {
      msg = ex.getMessage();
      if(file != null) {
        try { data = Check.check(file.merge(bxw).path());
        } catch(final IOException e) { msg = e.getMessage(); }
      }
    }
    if(data == null) Err.or(coll ? NOCOLL : NODOC, msg);

    // add document to array
    final int dl = docs.length;
    docs = Array.add(docs, new DBNode(data, 0));
    return docs[dl];
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
      if(collName.length == 0) Err.or(COLLDEF);
      return SeqIter.get(collect[0].list, collect[0].size);
    }

    // invalid collection reference
    if(contains(coll, '<') || contains(coll, '\\'))
      Err.or(COLLINV, Err.chop(coll));

    int c = -1;
    final int cl = collName.length;
    while(true) {
      if(++c == cl) addDocs(doc(coll, true));
      else if(!eq(collName[c], coll)) continue;
      return SeqIter.get(collect[c].list, collect[c].size);
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
    collect = Array.add(collect, ni);
    collName = Array.add(collName, name);
  }

  /**
   * Returns the database root as expression or null.
   * @return database root or null
   */
  public Data data() {
    return item instanceof DBNode ? ((DBNode) item).data : null;
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
    return "Context[" + file + "]";
  }
}
