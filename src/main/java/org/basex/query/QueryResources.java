package org.basex.query;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.IntList;

/**
 * This class provides access to resources.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public final class QueryResources {
  /** Database context. */
  private final QueryContext ctx;
  /** Opened documents. */
  private DBNode[] doc = new DBNode[1];
  /** Number of documents. */
  private int docs;
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
   * @param qc query context
   */
  QueryResources(final QueryContext qc) {
    ctx = qc;
  }

  /**
   * Compiles the resources.
   * @param nodes input node set
   * @throws QueryException query exception
   */
  void compile(final Nodes nodes) throws QueryException {
    final Data data = nodes.data;
    if(!ctx.context.perm(User.READ, data.meta))
      Err.PERMNO.thrw(null, CmdPerm.READ);

    // create globally known document nodes
    final int s = data.empty() ? 0 : (int) nodes.size();
    if(nodes.root) {
      // use input node set, if it contains all documents of the database
      doc = new DBNode[s];
      for(int n = 0; n < s; ++n) {
        addDoc(new DBNode(data, nodes.list[n], Data.DOC));
      }
    } else {
      // otherwise, create new nodes from all documents of the database
      final IntList il = data.doc();
      for(int p = 0; p < il.size(); p++) {
        addDoc(new DBNode(data, il.get(p), Data.DOC));
      }
    }
    rootDocs = docs;

    // create initial context items
    if(nodes.root) {
      // input nodes contain all roots: assign document array
      ctx.value = Seq.get(doc, docs);
    } else {
      // otherwise, add all context items
      final ItemIter ir = new ItemIter(s);
      for(int n = 0; n < s; ++n) ir.add(new DBNode(data, nodes.list[n]));
      ctx.value = ir.finish();
    }

    // create default collection from document array
    addCollection(new NodIter(doc, docs), token(data.meta.name));
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
        if(eq(input, coll[c].get(n).base()))
          return (DBNode) coll[c].get(n);
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
        data = Open.open(in, ctx.context);
      } catch(final IOException ex) {
        NODB.thrw(ii, in);
      }
    } else {
      final IO file = ctx.base();
      data = doc(in, file == null, col, ii);
      if(data == null) data = doc(file.merge(in).path(), true, col, ii);
    }

    // add node to global array
    final DBNode node = new DBNode(data, 0, Data.DOC);
    if(!data.single() && !col) EXPSINGLE.thrw(ii);
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
      return Check.check(ctx.context, path);
    } catch(final IOException ex) {
      if(err) (col ? NOCOLL : NODOC).thrw(ii, ex);
      return null;
    }
  }

  /**
   * Adds a collection to the global document list.
   * @param node node to be added
   */
  public void addDoc(final DBNode node) {
    if(docs == doc.length) {
      final DBNode[] tmp = new DBNode[Array.newSize(docs)];
      System.arraycopy(doc, 0, tmp, 0, docs);
      doc = tmp;
    }
    doc[docs++] = node;
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
    if(ctx.value == null) return null;
    if(docNodes()) return doc[0].data;

    final Iter iter = ctx.value.iter();
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
    return ctx.value instanceof Seq && ctx.value.sameAs(Seq.get(doc, docs));
  }

  /**
   * Closes the context.
   * @throws IOException I/O exception
   */
  void close() throws IOException {
    for(int d = rootDocs; d < docs; ++d) Close.close(ctx.context, doc[d].data);
    docs = 0;
  }

}