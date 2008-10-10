package org.basex.query.xquery;

import static org.basex.Text.*;
import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.io.IO;
import org.basex.query.FTOpt;
import org.basex.query.FTPos;
import org.basex.query.QueryContext;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Dat;
import org.basex.query.xquery.item.Dtm;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Tim;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.Functions;
import org.basex.query.xquery.util.NSLocal;
import org.basex.query.xquery.util.SeqBuilder;
import org.basex.query.xquery.util.Variables;
import org.basex.util.Array;
import org.basex.util.Atts;
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

  /** Current fulltext item. */
  public FTTokenizer ftitem;
  /** Current fulltext options. */
  public FTOpt ftopt;
  /** Current fulltext position filter. */
  public FTPos ftpos;

  /** Current Date. */
  public Dat date;
  /** Current DateTime. */
  public Dtm dtm;
  /** Current Time. */
  public Tim time;

  /** Default function namespace. */
  public Uri nsFunc = Uri.FN;
  /** Static Base URI. */
  public Uri baseURI = Uri.EMPTY;
  /** Default element namespace. */
  public Uri nsElem = Uri.EMPTY;
  /** Default collation. */
  public Uri collation = Uri.uri(URLCOLL);

  /** List of modules. */
  StringList modules = new StringList();
  /** List of loaded modules. */
  StringList modLoaded = new StringList();
  /** Used documents. */
  DNode[] docs = new DNode[0];
  /** Collections. */
  NodIter[] collect = new NodIter[0];
  /** Collection names. */
  byte[][] collName = new byte[0][];

  /** Default fulltext options. */
  private final FTOpt ftoptions = new FTOpt();
  /** Default boundary-space. */
  public boolean spaces = false;
  /** Empty Order mode. */
  public boolean orderGreatest = false;

  /** Default encoding (currently ignored). */
  public byte[] encoding = token(Prop.ENCODING);
  /** Preserve Namespaces (currently ignored). */
  public boolean nsPreserve = false;
  /** Inherit Namespaces (currently ignored). */
  public boolean nsInherit = false;
  /** Ordering mode (currently ignored). */
  public boolean ordered = false;
  /** Construction mode (currently ignored). */
  public boolean construct = false;

  /** Reference to the root expression. */
  Expr root;

  @Override
  public XQContext compile(final Nodes nodes) throws XQException {
    // adds an existing document to the database array
    if(nodes != null) {
      docs = new DNode[nodes.size];
      for(int d = 0; d < docs.length; d++) {
        docs[d] = new DNode(nodes.data, nodes.nodes[d]);
      }
      item = Seq.get(docs, docs.length);
      final NodIter col = new NodIter();
      for(final DNode doc : docs)
        col.add(doc);
      collect = Array.add(collect, col);
      collName = Array.add(collName, token(nodes.data.meta.dbname));
    }

    // evaluates the query and returns the result
    inf = Prop.allInfo;
    if(inf) compInfo(QUERYCOMP);
    fun.comp(this);
    vars.comp(this);
    ftopt = ftoptions;
    root = root.comp(this);
    if(inf) compInfo(QUERYRESULT + "%", root);

    evalTime = System.nanoTime();
    return this;
  }

  @Override
  public XQResult eval(final Nodes nodes) throws XQException {
    // evaluates the query and returns the result
    return new XQResult(this, new SeqBuilder(iter()));
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws XQException query exception
   */
  public Iter iter() throws XQException {
    // evaluates the query and returns the result
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
   * @param it item to serialize
   * @throws IOException query exception
   */
  public void serialize(final Serializer ser, final Item it)
      throws IOException {

    // sets initial namespaces
    final Atts nsp = ser.ns;
    nsp.reset();
    if(nsElem != Uri.EMPTY) nsp.add(EMPTY, nsElem.str());
    it.serialize(ser);
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
   * @throws XQException evaluation exception
   */
  public Iter iter(final Expr e) throws XQException {
    checkStop();

    // skip query info for items
    final Iter ir = e.iter(this);
    if(inf && !e.i()) {
      final double t = ((System.nanoTime() - evalTime) / 10000) / 100d;
      evalInfo(t + MS + ": " + e.getClass().getSimpleName() + ": " + e);
      inf = ++cc < MAXDUMP;
      if(!inf) evalInfo(EVALSKIP);
    }
    return ir;
  }

  /**
   * Returns the specified expression as an item. Empty sequences are
   * handled by the empty flag.
   * @param expr expression to be evaluated
   * @param call calling expression
   * @param empty if set to true, empty sequences are returned as null.
   * Otherwise, an error is thrown
   * @return iterator
   * @throws XQException evaluation exception
   */
  public Item atomic(final Expr expr, final Expr call, final boolean empty)
      throws XQException {

    if(expr.e()) {
      if(!empty) Err.empty(call);
      return null;
    }
    return expr.i() ? (Item) expr : iter(expr).atomic(call, empty);
  }

  /**
   * Adds a database instance or returns an existing one.
   * @param db database name or file path
   * @return database instance
   * @throws XQException evaluation exception
   */
  public DNode doc(final byte[] db) throws XQException {
    if(contains(db, '<') || contains(db, '>')) Err.or(INVDOC, db);

    // check if the collections contain the document
    for(final NodIter ni : collect) {
      for(int n = 0; n < ni.size; n++) {
        if(eq(db, ni.list[n].base())) return (DNode) ni.list[n];
      }
    }

    // check if the database has already been opened
    String dbname = string(db);
    for(final DNode d : docs) if(d.data.meta.dbname.equals(dbname)) return d;

    // check if the document has already been opened
    final IO bxw = IO.get(string(db));
    for(final DNode d : docs) if(d.data.meta.file.eq(bxw)) return d;

    // get database instance
    Data data = null;
    try { data = Check.check(dbname); } catch(final IOException ex) { }

    if(data == null && file != null) {
      dbname = file.merge(bxw).path();
      try { data = Check.check(dbname); } catch(final IOException ex) { }
    }
    if(data == null) Err.or(NODOC, bxw);

    // add document to array
    final int dl = docs.length;
    docs = Array.add(docs, new DNode(data, 0));
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
      if(++c == cl) addDocs(doc(coll));
      else if(!eq(collName[c], coll)) continue;
      return new NodIter(collect[c].list, collect[c].size);
    }
  }

  /**
   * Adds database documents as a collection.
   * @param db database reference
   */
  private void addDocs(final DNode db) {
    final NodIter col = new NodIter();
    final Data data = db.data;
    for(int p = 0; p < data.size;) {
      col.add(new DNode(data, p));
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

  @Override
  public String toString() {
    return "Context[" + file + "]";
  }
}
