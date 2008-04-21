package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.Num;
import org.basex.util.Token;

/**
 * This class builds a word-based index for attribute values and
 * text contents in a hash map.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class WordBuilder extends Progress implements IndexBuilder {
  /** Initial array size. */
  private static final int CAP = 1 << 6;
  /** Token container references. */
  private int[] token = new int[CAP];
  /** Pointers to the next token. */
  private int[] next = new int[CAP];
  /** Hash table buckets. */
  private int[] bucket = new int[CAP];
  /** IDs of a token entry. */
  private byte[][] ids = new byte[CAP][];
  /** Number of tokens in the index. */
  private int size = 1;
  /** Token container. */
  private byte[] cont = Num.newNum();
  /** Temporary token reference. */
  private byte[] tmptok;
  /** Temporary token start. */
  private int tmps;
  /** Temporary token end. */
  private int tmpe;
  /** Temporary token length. */
  private int tmpl;
  /** Current parsing value. */
  private int id;
  /** Current parsing value. */
  private int total;

  /**
   * Builds the index structure and returns an index instance.
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public Words build(final Data data) throws IOException {
    total = data.size;

    for(id = 0; id < total; id++) {
      checkStop();
      if(data.kind(id) == Data.TEXT) index(data.text(id), id);
    }
    
    final String db = data.meta.dbname;
    DataOutput out = new DataOutput(db, DATAWRD + 'b');
    out.writeInts(bucket);
    out.close();
    bucket = null;

    out = new DataOutput(db, DATAWRD + 'n');
    out.writeInts(next);
    out.close();
    next = null;

    out = new DataOutput(db, DATAWRD + 't');
    out.writeInts(token);
    out.close();
    token = null;

    out = new DataOutput(db, DATAWRD + 'c');
    for(final byte c : cont) out.write(c);
    out.close();
    cont = null;

    out = new DataOutput(db, DATAWRD + 'l');
    out.writeInt(ids.length);
    final DataOutput out2 = new DataOutput(db, DATAWRD + 'i');
    for(int i = 0; i < size; i++) {
      out2.writeInt(out.size());
      out.writeBytes(ids[i] == null ? Token.EMPTY : Num.finish(ids[i]));
      ids[i] = null;
    }
    out.close();
    out2.close();
    
    return new Words(db);
  }

  /**
   * Returns current progress value.
   * @return progress information
   */
  public int value() {
    return id;
  }

  /**
   * Extracts and indexes words from the specified byte array.
   * @param tok token to be extracted and indexed
   * @param pre pre value
   */
  private void index(final byte[] tok, final int pre) {
    int pos = 0;
    tmptok = tok;
    tmpe = -1;
    tmpl = tok.length;
    while(parse()) index(pre, pos++);
  }
  
  /**
   * Indexes a single token and returns its unique id.
   * @param pre pre value
   * @param pos word position
   */
  private void index(final int pre, final int pos) {
    if(tmpe - tmps > Token.MAXLEN) return;

    // resize tables if necessary
    if(size == token.length) rehash();

    // check if token exists
    final int p = Words.hash(tmptok, tmps, tmpe - tmps) & bucket.length - 1;
    for(int tid = bucket[p]; tid != 0; tid = next[tid]) {
      if(equal(token[tid])) {
        ids[tid] = Num.add(ids[tid], pre);
        ids[tid] = Num.add(ids[tid], pos);
        return;
      }
    }

    // create new entry
    next[size] = bucket[p];
    bucket[p] = size;
    ids[size] = Num.newNum(pre);
    ids[size] = Num.add(ids[size], pos);

    token[size] = Num.size(cont);
    final byte[] tok = new byte[tmpe - tmps];
    for(int t = 0; t < tok.length; t++) {
      tok[t] = (byte) Token.ftNorm(tmptok[tmps + t]);
    }
    cont = Num.add(cont, tok);
    size++;
  }

  /**
   * Compares two character arrays for equality.
   * @param cpos token container reference
   * @return true if the arrays are equal
   */
  private boolean equal(final int cpos) {
    int cp = cpos;
    final int cl = Num.read(cont, cp);
    cp += Num.len(cont, cp);

    if(cl != tmpe - tmps) return false;
    for(int i = tmps; i < tmpe; i++) {
      if(cont[cp++] != Token.ftNorm(tmptok[i])) return false;
    }
    return true;
  }

  /**
   * Parses the input byte array and calculates start and end positions
   * for single words. False is returned as soon as all tokens are parsed.
   * @return true if more tokens exist
   */
  private boolean parse() {
    tmps = -1;
    while(++tmpe <= tmpl) {
      if(tmps == -1) {
        if(tmpe < tmpl && Token.ftChar(tmptok[tmpe])) tmps = tmpe;
      } else if(tmpe == tmpl || !Token.ftChar(tmptok[tmpe])) {
        return true;
      }
    }
    tmptok = null;
    return false;
  }

  /**
   * Resizes the hash table.
   */
  private void rehash() {
    final int s = size << 1;
    final int[] tmp = new int[s];

    // move table entries into new table
    final int l = bucket.length;
    for(int i = 0; i < l; i++) {
      int j = bucket[i];
      while(j != 0) {
        final int cp = token[j];
        final int cl = Num.read(cont, cp);
        final int pos = Words.hash(cont, cp + Num.len(cont, cp), cl) & s - 1;
        final int nx = next[j];
        next[j] = tmp[pos];
        tmp[pos] = j;
        j = nx;
      }
    }
    bucket = tmp;
    token = Array.extend(token);
    next = Array.extend(next);
    ids = Array.extend(ids);
  }
  
  @Override
  public String tit() {
    return PROGINDEX;
  }

  @Override
  public String det() {
    return INDEXWRD;
  }

  @Override
  public double prog() {
    return (double) id / total;
  }
}
