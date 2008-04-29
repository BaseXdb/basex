package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.StatsKey.Kind;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * This class contains statistics on the current document.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class Stats extends Set {
  /** Hash values. */
  private StatsKey[] values = new StatsKey[CAP];
  /** Database name. */
  private final String dbname;
  /** Node kind Integer. */
  public static final byte INT = 0x01;
  /** Node kind Double. */
  public static final byte DBL = 0x02;
  /** Node kind Category. */
  public static final byte CAT = 0x03;
  /** Node kind Text. */
  public static final byte TEXT = 0x04;
  /** Node kind None. */
  public static final byte NONE = 0x05;

  /**
   * Constructor using given database name.
   * @param db database name
   */
  public Stats(final String db) {
    dbname = db;
  }

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param val value
   */
  public void index(final byte[] key, final byte[] val) {
    int i = add(key);
    if(i > 0) {
      values[i] = new StatsKey();
    } else {
      i = -i;
    }
    if(val != null) values[i].add(val);
  }

  /**
   * Returns the value for the specified key.
   * @param tok key to be found
   * @return value or null if nothing was found
   */
  public StatsKey get(final byte[] tok) {
    return values[id(tok)];
  }

  /**
   * Returns the specified value.
   * @param p value index
   * @return value
   */
  public StatsKey val(final int p) {
    return values[p];
  }

  /**
   * Finishes the statistics.
   */
  public void finish() {
    for(int k = 1; k < size; k++) values[k].finish();
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Array.extend(values);
  }

  /**
   * Reads statistics for current database from file.
   */
  public void read() {
    try {
      final DataInput in = new DataInput(dbname, DATASTAT);
      final int l = in.readInt();

      for(int i = 0; i < l; i++) {
        final byte kind = in.readByte();
        final byte[] tag = in.readBytes();
        final StatsKey toAdd = new StatsKey();
        toAdd.kind = Kind.INT;
        final int j = add(tag);

        boolean kInt = kind == INT;
        if(kInt || kind == DBL) {
          toAdd.kind = kInt ? Kind.INT : Kind.DBL;
          toAdd.min = Token.toDouble(in.readBytes());
          toAdd.max = Token.toDouble(in.readBytes());
        } else if(kind == CAT) {
          toAdd.kind = Kind.CAT;
          final int cl = in.readInt();
          for(int c = 0; c < cl; c++) {
            toAdd.cats.add(in.readBytes());
          }
        } else if(kind == TEXT) {
          toAdd.kind = Kind.TEXT;
        } else {
          toAdd.kind = Kind.NONE;
        }
        values[j] = toAdd;
      }
      in.close();
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
  }

  /**
   * Writes statistics to the specified path.
   * @throws IOException IO Exception
   */
  public synchronized void write() throws IOException {
    final DataOutput sta = new DataOutput(dbname, DATASTAT);

    int l = 0;
    final int vl = values.length;
    for(int i = 0; i < vl; i++) if(values[i] != null) l++;
    sta.writeInt(l);

    for(int i = 0; i < vl; i++) {
      if(values[i] != null) {
        final StatsKey key = values[i];
        final Kind kind = key.kind;
        final byte[] tag = key(i);

        boolean kInt = false;
        if((kInt = kind == Kind.INT) || kind == Kind.DBL) {
          final byte[] min = Token.token(key.min);
          final byte[] max = Token.token(key.max);
          writeOrdInfo(sta, tag, kInt ? INT : DBL, min, max);
        } else if(kind == Kind.CAT) {
          final byte[][] cats = key.cats.keys();
          writeCatInfo(sta, CAT, tag, cats);
        } else if(kind == Kind.TEXT) {
          writeInfo(sta, TEXT, tag);
        } else {
          writeInfo(sta, NONE, tag);
        }
      }
    }
    sta.close();
  }

  /**
   * Writes a node to database.
   * @param out DataOutput reference
   * @param kind node kind
   * @param tag tag name
   * @param cats statsKey categories
   * @throws IOException if problems occur while writing
   */
  private void writeCatInfo(final DataOutput out, final byte kind,
      final byte[] tag, final byte[][] cats) throws IOException {

    final int cl = cats.length;
    out.write(kind);
    out.writeBytes(tag);
    out.writeInt(cl);
    for(int i = 0; i < cl; i++) out.writeBytes(cats[i]);
  }

  /**
   * Writes a node to database.
   * @param out DataOutput reference
   * @param tag tag name
   * @param kind node kind
   * @param min min value of the key
   * @param max max value of the key
   * @throws IOException in case of writing problems
   */
  private void writeOrdInfo(final DataOutput out, final byte[] tag,
      final byte kind, final byte[] min, final byte[] max) throws IOException {

    out.write(kind);
    out.writeBytes(tag);
    out.writeBytes(min);
    out.writeBytes(max);
  }

  /**
   * Writes a node to database.
   * @param out DataOutput reference
   * @param kind node kind
   * @param name tag name
   * @throws IOException in case the info could not be written
   */
  private void writeInfo(final DataOutput out, final byte kind,
      final byte[] name) throws IOException {

    out.write(kind);
    out.writeBytes(name);
  }
}
