package org.basex.data;

import static org.basex.data.DataText.*;

import java.io.IOException;

import org.basex.index.FTFuzzy;
import org.basex.index.FTTrie;
import org.basex.index.Index;
import org.basex.index.IndexToken;
import org.basex.index.Names;
import org.basex.index.Values;
import org.basex.index.IndexToken.Type;
import org.basex.io.DataAccess;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Token;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content with Berkeley DB JE. The storage equals the disk storage
 * in {@link DiskData}.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Bastian Lemke
 */
public class BDBData extends Data {
  /** database environment */
  private final Environment dbEnv;
  /** entity store */
  private final EntityStore store;
  /** primary index (pre values) */
  private final PrimaryIndex<Integer, BDBNode> nodeByPre;
  /** secondary index (node id) */
  private final SecondaryIndex<Integer, Integer, BDBNode> nodeById;
  /** Texts access file. */
  private final DataAccess texts;
  /** Values access file. */
  private final DataAccess values;

  /**
   * Default Constructor.
   * @param db name of database
   * @throws IOException if the connection to BerkeleyDB failed
   */
  public BDBData(final String db) throws IOException {
    this(db, false, false, false);
  }

  /**
   * Constructor, specifying if transactions and deferred write are enabled.
   * @param db name of database
   * @param transactional enable support for transactions
   * @param deferredWrite enable support for deferred writes
   * @param index open indexes
   * @throws IOException if the connection to BerkeleyDB failed
   */
  public BDBData(final String db, final boolean transactional,
      final boolean deferredWrite, final boolean index) throws IOException {
    DataInput in = null;
    try {
      in = new DataInput(db, DATAINFO);
      meta = new MetaData(db);
      meta.read(in);

      // read indexes
      tags = new Names(in);
      atts = new Names(in);
      skel = new Skeleton(this, in);
      ns = new Namespaces(in);
    } catch(IOException e) {
      throw e;
    } finally {
      if(in != null) {
        try {
          in.close();
        } catch(Exception e) {}
      }
    }

    // configurations for db environment and entity store
    EnvironmentConfig envConfig = new EnvironmentConfig();
    StoreConfig storeConfig = new StoreConfig();

    // enable or disable transactions
    envConfig.setTransactional(transactional);
    storeConfig.setDeferredWrite(deferredWrite);

    try {
      // create environment and entity store
      dbEnv = new Environment(IO.dbpath(db), envConfig);
      store = new EntityStore(dbEnv, "EntityStore", storeConfig);
      // create indices
      nodeByPre = store.getPrimaryIndex(Integer.class, BDBNode.class);
      nodeById = store.getSecondaryIndex(nodeByPre, Integer.class, "mId");
    } catch(DatabaseException e) {
      throw new IOException(e);
    }

    texts = new DataAccess(db, DATATXT);
    values = new DataAccess(db, DATAATV);

    if(index) {
      if(meta.txtindex) txtindex = new Values(this, db, true);
      if(meta.atvindex) atvindex = new Values(this, db, false);
      if(meta.ftxindex) ftxindex = meta.ftfz ? new FTFuzzy(this, db)
          : new FTTrie(this, db);
    }
    initNames();
  }

  /**
   * Closes the database connection.
   * @throws IOException if an error occurs while closing the connection
   */
  @Override
  public void close() throws IOException {
    // [CG] check if this is enough
    if(!meta.uptodate) flush();
    cls();
  }

  /**
   * Closes the database without writing data back to disk.
   * @throws IOException I/O exception
   */
  public void cls() throws IOException {
    try {
      store.close();
      dbEnv.cleanLog();
      dbEnv.close();
    } catch(DatabaseException e) {
      throw new IOException(e);
    }
    texts.close();
    values.close();
    closeIndex(IndexToken.Type.TXT);
    closeIndex(IndexToken.Type.ATV);
    closeIndex(IndexToken.Type.FTX);
  }

  @Override
  public int attLen(final int pre) {
    try {
      BDBNode node = nodeByPre.get(pre);
      int val = node.getTextId();
      return node.isTextInlined() ? Token.numDigits(val) : values.readNum(val);
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int attNS(final int pre) {
    // FIXME: implement
    return 0;
  }

  @Override
  public int attNameID(final int pre) {
    try {
      return nodeByPre.get(pre).getNameId();
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public double attNum(final int pre) {
    try {
      BDBNode node = nodeByPre.get(pre);
      int val = node.getTextId();
      return node.isTextInlined() ? val : Token.toDouble(values.readToken(val));
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1.0;
    }
  }

  @Override
  public int attSize(final int pre, final int kind) {
    try {
      return kind == ELEM ? nodeByPre.get(pre).getNumAtts() : 1;
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public byte[] attValue(final int pre) {
    try {
      BDBNode node = nodeByPre.get(pre);
      int val = node.getTextId();
      return node.isTextInlined() ? token(val) : values.readToken(val);
    } catch(DatabaseException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void closeIndex(final Type index) throws IOException {
    switch(index) {
      case TXT:
        if(txtindex != null) txtindex.close();
        break;
      case ATV:
        if(atvindex != null) atvindex.close();
        break;
      case FTX:
        if(ftxindex != null) ftxindex.close();
        break;
      default:
        break;
    }
  }

  @Override
  public void delete(final int pre) {
    final BDBNode node;
    try {
      node = nodeByPre.get(pre);

      int kind = node.getKind();
      // size of the subtree to delete
      final int s = size(pre, kind);
      // reduce size of ancestors
      BDBNode par;
      // check if we are an attribute (differenz size counters)
      if(kind == ATTR) {
        par = nodeByPre.get(parent(pre, ATTR));
        par.setNumAtts(par.getNumAtts() - 1);
        par.setSize(par.getSize() - 1);
        kind = par.getKind();
      } else {
        par = node;
      }
      // reduce size of remaining ancestors
      while(par.getId() > 0 && kind != DOC) {
        par = nodeByPre.get(parent(par.getId(), kind));
        kind = par.getKind();
        if(kind == ELEM || kind == DOC) par.setSize(par.getSize() - 1);
      }
      // delete node from BerkeleyDB and reduce document size
      nodeByPre.delete(pre);
      meta.size -= s;
      // database cannot be empty; instead, an empty root node is kept
      if(meta.size == 0) {
        meta.size = 1;
        // nodeByPre.get(0).getSize();
      }
      // FIXME: update dist of following siblings
    } catch(DatabaseException e) {
      e.printStackTrace();
      return;
    }
  }

  @Override
  public void flush() {
    try {
      store.sync();
      texts.flush();
      values.flush();
      // write meta data...
      final DataOutput out = new DataOutput(meta.dbname, DATAINFO);
      meta.finish(out);
      tags.finish(out);
      atts.finish(out);
      skel.finish(out);
      ns.finish(out);
      out.close();
    } catch(DatabaseException e) {
      e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public int id(final int pre) {
    try {
      return nodeByPre.get(pre).getId();
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public void insert(final int pre, final int par, final byte[] val,
      final int kind) {
    throw new NotImplementedException();
  }

  @Override
  public void insert(final int pre, final int par, final byte[] name,
      final byte[] val) {
    throw new NotImplementedException();
  }

  @Override
  public void insert(final int pre, final int par, final Data d) {
    throw new NotImplementedException();
  }

  @Override
  public int kind(final int pre) {
    try {
      return nodeByPre.get(pre).getKind();
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int[] ns(final int pre) {
    // FIXME: implement
    return new int[0];
  }

  @Override
  public int parent(final int pre, final int kind) {
    try {
      return pre - nodeByPre.get(pre).getDist();
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int pre(final int id) {
    try {
      return nodeById.get(id).getPre();
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public void setIndex(final Type type, final Index ind) {
    switch(type) {
      case TXT:
        if(meta.txtindex) txtindex = ind;
        break;
      case ATV:
        if(meta.atvindex) atvindex = ind;
        break;
      case FTX:
        if(meta.ftxindex) ftxindex = ind;
        break;
      default:
        break;
    }
  }

  @Override
  public int size(final int pre, final int kind) {
    try {
      return nodeByPre.get(pre).getSize();
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int tagID(final int pre) {
    try {
      return nodeByPre.get(pre).getNameId();
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int tagNS(final int pre) {
    // FIXME: implement
    return 0;
  }

  @Override
  public byte[] text(final int pre) {
    try {
      BDBNode node = nodeByPre.get(pre);
      int val = node.getTextId();
      return node.isTextInlined() ? token(val) : texts.readToken(val);
    } catch(DatabaseException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public int textLen(final int pre) {
    try {
      BDBNode node = nodeByPre.get(pre);
      int val = node.getTextId();
      return node.isTextInlined() ? Token.numDigits(val) : texts.readNum(val);
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public double textNum(final int pre) {
    try {
      BDBNode node = nodeByPre.get(pre);
      int val = node.getTextId();
      return node.isTextInlined() ? val : Token.toDouble(texts.readToken(val));
    } catch(DatabaseException e) {
      e.printStackTrace();
      return -1.0;
    }
  }

  @Override
  public void update(final int pre, final byte[] val) {
    throw new NotImplementedException();
  }

  @Override
  public void update(final int pre, final byte[] name, final byte[] val) {
    throw new NotImplementedException();
  }

  /**
   * Converts the specified long value into a byte array.
   * @param i int value to be converted
   * @return byte array
   */
  private static byte[] token(final long i) {
    int n = (int) i;
    if(n == 0) return Token.ZERO;
    int j = Token.numDigits(n);
    final byte[] num = new byte[j];

    // faster division by 10 for values < 81920 (see {@link Integer#getChars}
    while(n > 81919) {
      final int q = n / 10;
      num[--j] = (byte) (n - (q << 3) - (q << 1) + '0');
      n = q;
    }
    while(n != 0) {
      final int q = (n * 52429) >>> 19;
      num[--j] = (byte) (n - (q << 3) - (q << 1) + '0');
      n = q;
    }
    return num;
  }
}
