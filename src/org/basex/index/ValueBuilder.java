package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.io.IOConstants;
import org.basex.util.Array;
import org.basex.util.Num;
import org.basex.util.Token;

/**
 * This class builds an index for attribute values and text contents
 * in a hash map.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ValueBuilder extends Progress implements IndexBuilder {
  /** Value type (attributes/texts). */
  private final boolean text;
  /** Pointers to the next token. */
  private int[] next;
  /** Hash table buckets. */
  private int[] bucket;
  /** Text references. */
  private int[] pos;
  /** Text values. */
  private byte[] texts;
  /** Text length. */
  private int kl;
  /** IDs of an entry. */
  private int[][] ids;

  /** Number of hash entries. */
  private int size = 1;
  /** Current parsing value. */
  private int id;
  /** Maximum parsing value. */
  private int total;

  /**
   * Constructor.
   * @param txt value type (text/attribute)
   */
  public ValueBuilder(final boolean txt) {
    text = txt;
  }

  /**
   * Builds the index structure and returns an index instance.
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public Values build(final Data data) throws IOException {
    // calculate approximate final hash capacity to reduce rehashings
    final String db = data.meta.dbname;
    final String f = text ? DATATXT : DATAATV;
    int cap = 1 << 2;
    final int max = (int) (IOConstants.dbfile(db, f).length() >>> 7);
    while(cap < max && cap < (1 << 24)) cap <<= 1;

    next = new int[cap];
    bucket = new int[cap];
    ids = new int[cap][];
    texts = new byte[cap];
    pos = new int[cap];

    total = data.size;
    final int type = text ? Data.TEXT : Data.ATTR;
    for(id = 0; id < total; id++) {
      checkStop();
      if(data.kind(id) != type) continue;
      index(text ? data.text(id) : data.attValue(id), id);
    }
    texts = null;
    pos = null;

    final int bs = bucket.length;

    DataOutput out = new DataOutput(db, f + 'b');
    for(int i = 0; i < bs; i++) out.writeInt(bucket[i]);
    out.close();
    bucket = null;

    out = new DataOutput(db, f + 'n');
    out.writeInt(text ? 0 : 1);
    for(int i = 1; i < bs; i++) out.writeInt(next[i]);
    out.close();
    next = null;

    out = new DataOutput(db, f + 'l');
    out.writeInt(bs);
    final DataOutput out2 = new DataOutput(db, f + 'i');
    for(int i = 0; i < bs; i++) {
      out2.writeInt(out.size());
      out.writeBytes(ids[i] == null ? Token.EMPTY : Num.create(ids[i]));
      ids[i] = null;
    }
    ids = null;

    out.close();
    out2.close();
    
    return new Values(data, db, text);
  }

  /**
   * Indexes a single token and returns its unique id.
   * @param tok token to be indexed
   * @param pre pre value
   */
  private void index(final byte[] tok, final int pre) {
    // check if token exists
    if(tok.length > Token.MAXLEN || Token.ws(tok)) return;

    // resize tables if necessary
    if(size == next.length) rehash(size << 1);

    final int h = Token.hash(tok) & bucket.length - 1;
    for(int tid = bucket[h]; tid != 0; tid = next[tid]) {
      if(eq(tok, pos[tid])) {
        ids[tid][0] += 1;
        int s = ids[tid][0];
        if(s == ids[tid].length) {
          int[] t = new int[s + Math.max(1, s >> 3)];
          System.arraycopy(ids[tid], 0, t, 0, s);
          ids[tid] = t;
        }
        ids[tid][s] = pre;
        return;
      }
    }

    // create new entry
    next[size] = bucket[h];
    bucket[h] = size;
    pos[size] = kl;
    ids[size++] = new int[] { 1, pre };
    
    int tl = tok.length;
    while(kl + tl + 1 >= texts.length) texts = Array.extend(texts);
    texts[kl++] = (byte) tl;
    System.arraycopy(tok, 0, texts, kl, tl);
    kl += tl;
  }
  
  /**
   * Compares the specified token with the referenced text array.
   * @param k token to be compared
   * @param p referenced text
   * @return result of check
   */
  private boolean eq(final byte[] k, final int p) {
    final int l = k.length;
    if(l != texts[p]) return false;
    for(int i = 0, c = p + 1; i != l;) if(k[i++] != texts[c++]) return false;
    return true;
  }

  /**
   * Resizes the hash table.
   * @param s new size
   */
  private void rehash(final int s) {
    final int l = bucket.length;
    if(s == l) return;

    // move table entries into new table
    final int[] tmp = new int[s];
    for(int i = 0; i < l; i++) {
      int tid = bucket[i];
      while(tid != 0) {
        final int pp = hash(pos[tid]) & s - 1;
        final int nx = next[tid];
        next[tid] = tmp[pp];
        tmp[pp] = tid;
        tid = nx;
      }
    }
    bucket = tmp;
    next = Array.extend(next);
    ids = Array.extend(ids);
    pos = Array.extend(pos);
  }

  /**
   * Calculates the hash value of the referenced text.
   * @param p referenced text
   * @return hash value
   */
  private int hash(final int p) {
    int h = 0;
    final int l = texts[p];
    for(int i = 0, c = p + 1; i != l; i++) h = (h << 5) - h + texts[c++];
    return h;
  }
  
  @Override
  public String tit() {
    return PROGINDEX;
  }

  @Override
  public String det() {
    return text ? INDEXTXT : INDEXATT;
  }

  @Override
  public double prog() {
    return (double) id / total;
  }
}
