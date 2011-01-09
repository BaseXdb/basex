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
import org.basex.query.item.DBDocSeq;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * This class provides access to resources.
 *
 * @author BaseX Team 2005-11, ISC License
 */
public final class QueryResources {
  /** Database context. */
  private final QueryContext ctx;

  /** Opened databases. */
  private Data[] data = new Data[1];
  /** Number of databases. */
  private int datas;
  /** Flag for global data reference. */
  private boolean globalData;

  /** Collections: single nodes and sequences. */
  private Value[] coll = new Value[1];
  /** Names of collections. */
  private byte[][] collName = new byte[1][];
  /** Number of collections. */
  private int colls;

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
    final Data d = nodes.data;
    if(!ctx.context.perm(User.READ, d.meta))
      Err.PERMNO.thrw(null, CmdPerm.READ);

    // assign initial context value: use empty node set if database is empty
    ctx.value = DBDocSeq.get(d.empty() ? new int[0] : nodes.list, d);

    // create default collection: use initial node set if it contains all
    // documents of the database. otherwise, create new node set
    addCollection(nodes.root ? ctx.value :
        DBDocSeq.get(d.doc(), d), token(d.meta.name));

    // add global data reference has been added, set indicator to true
    globalData = true;
    addData(d);
  }

  /**
   * Closes the opened data references.
   * @throws IOException I/O exception
   */
  void close() throws IOException {
    for(int d = globalData ? 1 : 0; d < datas; ++d) {
      Close.close(ctx.context, data[d]);
    }
    datas = 0;
  }

  /**
   * Opens a new database or returns a reference to an already opened database.
   * @param name name of database
   * @param ii input info
   * @return database instance
   * @throws QueryException query exception
   */
  public Data data(final byte[] name, final InputInfo ii)
      throws QueryException {

    // check if a database with the same name has already been opened
    final String in = string(name);
    for(int d = 0; d < datas; ++d) {
      if(data[d].meta.name.equals(in)) return data[d];
    }

    try {
      // open database
      final Data d = Open.open(in, ctx.context);
      addData(d);
      return d;
    } catch(final IOException ex) {
      NODB.thrw(ii, in);
      return null;
    }
  }

  /**
   * Creates a new data reference for the specified input, or returns a
   * reference to an already opened file or database.
   * @param input file path or name of database
   * @param col collection flag
   * @param ii input info
   * @return data reference
   * @throws QueryException query exception
   */
  public Data data(final byte[] input, final boolean col, final InputInfo ii)
      throws QueryException {

    // check if a database with the same name has already been opened
    final String in = string(input);
    for(int d = 0; d < datas; ++d) {
      if(data[d].meta.name.equals(in)) return data[d];
    }

    // check if a database with the same file path has already been opened
    final IO io = IO.get(in);
    for(int d = 0; d < datas; ++d) {
      if(data[d].meta.file.eq(io)) return data[d];
    }

    // retrieve and add new data reference
    Data d = doc(in, ctx.baseURI == Uri.EMPTY, col, ii);
    if(d == null) d = doc(ctx.base().merge(in).path(), true, col, ii);
    addData(d);
    return d;
  }

  /**
   * Adds a collection instance or returns an existing one.
   * @param input name of the collection to be returned
   * @param ii input info
   * @return collection iterator
   * @throws QueryException query exception
   */
  public Iter collection(final byte[] input, final InputInfo ii)
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
          addCollection(data(input, true, ii), EMPTY);
        } else {
          addCollection(data(substring(input, 0, s), true, ii),
              substring(input, s + 1));
        }
      }
    }
    return coll[c].iter();
  }

  /**
   * Returns the common data reference of all context items, or {@code null}.
   * @return data reference
   * @throws QueryException query exception
   */
  public Data data() throws QueryException {
    if(ctx.value == null) return null;
    if(docNodes()) return data[0];

    final Iter iter = ctx.value.iter();
    Data db = null;
    Item it;
    while((it = iter.next()) != null) {
      if(!(it instanceof DBNode)) return null;
      final Data d = ((DBNode) it).data;
      if(db == null) db = d;
      else if(db != d) return null;
    }
    return db;
  }

  /**
   * Returns true if the current context item contains all root document nodes.
   * @return result of check
   */
  public boolean docNodes() {
    // check if a global data reference exists, if context value and first
    // collection reference are equal, and if first item is a document node
    final Value val = ctx.value;
    return globalData && val.sameAs(coll[0]) && val.size() != 0 &&
      (val.item() ? (Item) val : val.iter(ctx).next()).type == Type.DOC;
  }

  /**
   * Adds documents of the specified data reference as a collection.
   * @param name name of collection
   * @param inputs documents
   * @throws QueryException query exception
   */
  public void addCollection(final byte[] name, final byte[][] inputs)
      throws QueryException {

    final int ns = inputs.length;
    final DBNode[] nodes = new DBNode[ns];
    for(int n = 0; n < ns; n++) {
      nodes[n] = new DBNode(data(inputs[n], true, null), 0, Data.DOC);
    }
    addCollection(Seq.get(nodes, ns), name);
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Adds documents of the specified data reference as a collection.
   * @param d data reference
   * @param path inner collection path
   */
  private void addCollection(final Data d, final byte[] path) {
    addCollection(DBDocSeq.get(d.doc(string(path)), d), token(d.meta.name));
  }

  /**
   * Opens the database or creates a new database instance for the specified
   * document.
   * @param path document path
   * @param err error flag
   * @param col collection flag
   * @param ii input info
   * @return data reference
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
   * Adds a data reference to the global list.
   * @param d data reference to be added
   */
  private void addData(final Data d) {
    if(datas == data.length) {
      final Data[] tmp = new Data[Array.newSize(datas)];
      System.arraycopy(data, 0, tmp, 0, datas);
      data = tmp;
    }
    data[datas++] = d;
  }

  /**
   * Adds a collection to the global collection list.
   * @param nodes collection nodes
   * @param name name
   */
  private void addCollection(final Value nodes, final byte[] name) {
    if(colls == coll.length) {
      coll = Arrays.copyOf(coll, colls << 1);
      collName = Array.copyOf(collName, colls << 1);
    }
    coll[colls] = nodes;
    collName[colls++] = name;
  }
}