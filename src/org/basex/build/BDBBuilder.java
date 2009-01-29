package org.basex.build;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;

import java.io.IOException;

import org.basex.core.proc.DropDB;
import org.basex.data.BDBData;
import org.basex.data.BDBNode;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Token;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

/**
 * This class creates a BerkeleyDB based database instance.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Bastian Lemke
 */
public final class BDBBuilder extends Builder {
  /** database environment. */
  private Environment dbEnv = null;
  /** entity store. */
  private EntityStore store = null;
  /** primary index (pre values). */
  private PrimaryIndex<Integer, BDBNode> nodeByPre = null;
  /** Database texts. */
  private DataOutput xout;
  /** Database values. */
  private DataOutput vout;

  /** Text pointer. */
  private int txtlen;
  /** Attribute value pointer. */
  private int vallen;

  @Override
  public BDBBuilder init(final String db) throws IOException {
    meta = new MetaData(db);
    meta.file = parser.io;
    meta.filesize = meta.file.length();
    meta.time = meta.file.date();

    DropDB.drop(db);
    IO.dbpath(db).mkdirs();

    // calculate output buffer sizes: (1 << BLOCKPOWER) < bs < (1 << 22)
    int bs = IO.BLOCKSIZE;
    while(bs < meta.filesize && bs < (1 << 22))
      bs <<= 1;

    // configurations for db environment and entity store
    EnvironmentConfig envConfig = new EnvironmentConfig();
    StoreConfig storeConfig = new StoreConfig();

    // allow it to be created if it does not already exist
    envConfig.setAllowCreate(true);
    storeConfig.setAllowCreate(true);

    // enable or disable transactions
    envConfig.setTransactional(false);
    storeConfig.setDeferredWrite(false);

    try {
      // create environment and entity store
      dbEnv = new Environment(IO.dbpath(db), envConfig);
      store = new EntityStore(dbEnv, "EntityStore", storeConfig);
      // create indices
      nodeByPre = store.getPrimaryIndex(Integer.class, BDBNode.class);
    } catch(DatabaseException e) {
      throw new IOException(e);
    }

    xout = new DataOutput(db, DATATXT, bs);
    vout = new DataOutput(db, DATAATV, bs);
    return this;
  }

  @Override
  public synchronized BDBData finish() throws IOException {
    // check if data ranges exceed database limits
    if(tags.size() > 0x0FFF) throw new IOException(LIMITTAGS);
    if(atts.size() > 0x0FFF) throw new IOException(LIMITATTS);
    close();

    // close files
    final String db = meta.dbname;
    final DataOutput out = new DataOutput(meta.dbname, DATAINFO);
    meta.finish(out);
    tags.finish(out);
    atts.finish(out);
    skel.finish(out);
    ns.finish(out);
    out.close();

    // return database instance
    return new BDBData(db);
  }

  @Override
  public void close() throws IOException {
    try {
      if(store != null) store.close();
      if(dbEnv != null) dbEnv.close();
    } catch(DatabaseException e) {
      throw new IOException(e);
    }
    store = null;
    dbEnv = null;
    nodeByPre = null;
    if(xout != null) xout.close();
    xout = null;
    if(vout != null) vout.close();
    vout = null;
  }

  @Override
  public void addDoc(final byte[] txt) throws IOException {
    BDBNode node = new BDBNode();
    node.setPre(meta.size);
    node.setId(meta.size++);
    node.setKind(Data.DOC);
    inline(node, txt, true);
    node.setSize(0);
    node.setDist(1);
    try {
      nodeByPre.put(node);
    } catch(DatabaseException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void addElem(final int t, final int s, final int dis, final int as,
      final boolean n) throws IOException {
    BDBNode node = new BDBNode();
    node.setPre(meta.size);
    node.setId(meta.size++);
    node.setKind(Data.ELEM);
    node.setNameId(t);
    node.setNumAtts(as);
    node.setDist(dis);
    node.setSize(as);
    try {
      nodeByPre.put(node);
    } catch(DatabaseException e) {
      throw new IOException(e);
    }
    if(meta.size < 0) throw new IOException(LIMITRANGE);
  }

  @Override
  public void addAttr(final int t, final int s, final byte[] txt, final int dis)
      throws IOException {
    BDBNode node = new BDBNode();
    node.setPre(meta.size);
    node.setId(meta.size++);
    node.setKind(Data.ATTR);
    node.setNameId(t);
    inline(node, txt, false);
    node.setDist(dis);
    try {
      nodeByPre.put(node);
    } catch(DatabaseException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void addText(final byte[] txt, final int dis, final byte kind)
      throws IOException {
    BDBNode node = new BDBNode();
    node.setPre(meta.size);
    node.setId(meta.size++);
    node.setKind(kind);
    inline(node, txt, true);
    node.setDist(dis);
    try {
      nodeByPre.put(node);
    } catch(DatabaseException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void setSize(final int pre, final int val) throws IOException {
    BDBNode node;
    try {
      node = nodeByPre.get(pre);
      node.setSize(val);
      nodeByPre.put(node);
    } catch(DatabaseException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void setAttValue(final int pre, final byte[] val) throws IOException {
    BDBNode node;
    try {
      node = nodeByPre.get(pre);
      inline(node, val, false);
      nodeByPre.put(node);
    } catch(DatabaseException e) {
      throw new IOException(e);
    }
  }

  /**
   * Inlines the value in the node or stores it to the texts/values database and
   * stores the offset to the node.
   * @param node the node to store the text in
   * @param val value to be inlined
   * @param txt text/attribute flag
   * @throws IOException if writing to the text/values file failed.
   */
  private void inline(final BDBNode node, final byte[] val, final boolean txt)
      throws IOException {
    // inline integer values...
    int v = Token.toSimpleInt(val);
    if(v != Integer.MIN_VALUE) {
      node.setTextInlined(true);
    } else {
      node.setTextInlined(false);
      if(txt) {
        v = txtlen;
        txtlen += xout.writeBytes(val);
      } else {
        v = vallen;
        vallen += vout.writeBytes(val);
      }
    }
    node.setTextId(v);
  }
}
