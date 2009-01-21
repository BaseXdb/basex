package org.basex.query.xquery;

import static org.basex.Text.*;
import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.io.IO;
import org.basex.query.FTOpt;
import org.basex.query.FTPos;
import org.basex.query.QueryContext;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.Dat;
import org.basex.query.xquery.item.Dtm;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Tim;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.Functions;
import org.basex.query.xquery.util.NSLocal;
import org.basex.query.xquery.util.Variables;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.StringList;

/**
 * XQuery Context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQContext extends QueryContext {
  /** Namespaces. */
  public NSLocal ns = new NSLocal();
  /** Functions. */
  public Functions fun = new Functions();
  /** Variables. */
  public Variables vars = new Variables();

  /** Current context. */
  public Item item;
  /** Current context position. */
  public int pos;
  /** Current context size. */
  public int size;
  /** Current leaf flag. */
  public boolean leaf;

  /** Current fulltext item. */
  public FTPosData ftdata;
  /** Current fulltext item. */
  public FTTokenizer ftitem;
  /** Current fulltext options. */
  public FTOpt ftopt = new FTOpt();
  /** Current fulltext position filter. */
  public FTPos ftpos;
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
  public DBNode[] docs = new DBNode[0];
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

  /** Default encoding (currently ignored). */
  public byte[] encoding = token(Prop.ENCODING);
  /** Preserve Namespaces (currently ignored). */
  public boolean nsPreserve = true;
  /** Inherit Namespaces (currently ignored). */
  public boolean nsInherit = true;
  /** Ordering mode (currently ignored). */
  public boolean ordered = false;
  /** Construction mode (currently ignored). */
  public boolean construct = false;
  /** Revalidation Mode (currently ignored). */
  public int revalidate = 0;

  /** Reference to the root expression. */
  private Expr root;

  /**
   * Parses the specified query.
   * @param q input query
   * @throws XQException xquery exception
   */
  public void parse(final String q) throws XQException {
    query = q;
    root = new XQParser(this).parse(q, file, null);
  }

  /**
   * Parses the specified module.
   * @param q input query
   * @throws XQException xquery exception
   */
  public void module(final String q) throws XQException {
    query = q;
    new XQParser(this).parse(q, file, Uri.EMPTY);
  }

  @Override
  public void compile(final Nodes nodes) throws XQException {
    try {
      // cache the initial context nodes
      if(nodes != null) {
        rootDocs = nodes.size;
        docs = new DBNode[rootDocs];
        for(int d = 0; d < docs.length; d++) {
          docs[d] = new DBNode(nodes.data, nodes.nodes[d]);
        }
        item = Seq.get(docs, docs.length);
        
        // add collection instances
        final NodIter col = new NodIter();
        for(final DBNode doc : docs) col.add(doc);
        collect = Array.add(collect, col);
        collName = Array.add(collName, token(nodes.data.meta.dbname));
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

  @Override
  public Result eval(final Nodes nodes) throws XQException {
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
   * @throws XQException query exception
   */
  public Iter iter() throws XQException {
    return iter(root);
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

  @Override
  public void plan(final Serializer ser) throws IOException {
    vars.plan(ser);
    fun.plan(ser);
    root.plan(ser);
  }

  /**
   * Evaluates the specified expression and returns an iterator.
   * @param e expression to be evaluated
   * @return iterator
   * @throws XQException query exception
   */
  public Iter iter(final Expr e) throws XQException {
    checkStop();
    return e.iter(this);
  }

  /**
   * Adds a database instance or returns an existing one.
   * @param db database name or file path
   * @param coll collection flag
   * @return database instance
   * @throws XQException evaluation exception
   */
  public DBNode doc(final byte[] db, final boolean coll) throws XQException {
    if(contains(db, '<') || contains(db, '>')) Err.or(INVDOC, db);

    // check if the collections contain the document
    for(final NodIter ni : collect) {
      for(int n = 0; n < ni.size; n++) {
        if(eq(db, ni.list[n].base())) return (DBNode) ni.list[n];
      }
    }

    // check if the database has already been opened
    String dbname = string(db);
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
   * @throws XQException evaluation exception
   */
  public NodIter coll(final byte[] coll) throws XQException {
    // no collection specified.. return default collection/current context set
    if(coll == null) {
      if(collName.length == 0) Err.or(COLLDEF);
      return new NodIter(collect[0].list, collect[0].size);
    }

    // invalid collection reference
    if(contains(coll, '<') || contains(coll, '\\')) {
      Err.or(COLLINV, coll.length > 20 ? 
          concat(substring(coll, 0 , 20), token("...")) : coll);
    }

    int c = -1;
    final int cl = collName.length;
    while(true) {
      if(++c == cl) addDocs(doc(coll, true));
      else if(!eq(collName[c], coll)) continue;
      return new NodIter(collect[c].list, collect[c].size);
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
   * @param nod collection nodes
   * @param name name
   */
  public void addColl(final NodIter nod, final byte[] name) {
    collect = Array.add(collect, nod);
    collName = Array.add(collName, name);
  }

  /**
   * Returns the database root as expression or null.
   * @return database root or null
   */
  public DBNode dbroot() {
    if(!(item instanceof DBNode)) return null;
    final DBNode db = (DBNode) item;
    return db.type == Type.DOC ? db : null;
  }

  @Override
  public String toString() {
    return "Context[" + file + "]";
  }
}
