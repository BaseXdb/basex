package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.cmd.DropDB;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.query.ft.Scoring;
import org.basex.query.ft.StopWords;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.Tokenizer;
import org.basex.util.Util;

/**
 * This class contains common methods for full-text index builders.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class FTBuilder extends IndexBuilder {
  /** Word parser. */
  protected final Tokenizer wp;
  /** Scoring mode; see {@link Prop#SCORING}. */
  protected final int scm;
  /** Number of indexed tokens. */
  protected long ntok;
  /** Number of cached index structures. */
  protected int csize;

  /** Document units (all document or text nodes in a document). */
  private final IntList unit = new IntList();
  /** Container for all frequencies. TF: freq(i, j). */
  private final IntList freq = new IntList();
  /** Container for maximal frequencies. TF: max(l, freq(l, j)). */
  private int[] maxfreq;
  /** Container for number of documents with token i. IDF: n(i). */
  private int[] ntoken;
  /** Maximum scoring value. */
  private int max;
  /** Minimum scoring value. */
  private int min;
  /** Current token. */
  private int token;
  /** Current frequency. */
  private int fc;
  /** Stop word list. */
  private final StopWords sw;

  /**
   * Returns a new full-text index builder.
   * @param d data reference
   * @param wild wildcard index
   * @return index builder
   * @throws IOException IOException
   */
  public static FTBuilder get(final Data d, final boolean wild)
      throws IOException {
    return wild ? new FTTrieBuilder(d) : new FTFuzzyBuilder(d);
  }

  /**
   * Constructor.
   * @param d data reference
   * @throws IOException IOException
   */
  protected FTBuilder(final Data d) throws IOException {
    super(d);
    final Prop prop = d.meta.prop;
    scm = d.meta.scoring;
    wp = new Tokenizer(prop);
    max = -1;
    min = Integer.MAX_VALUE;
    sw = new StopWords(d, prop.get(Prop.STOPWORDS));
  }

  /**
   * Extracts and indexes words from the specified data reference.
   * @throws IOException IO exception
   */
  protected final void index() throws IOException {
    // delete old index
    abort();

    final Performance perf = Util.debug ? new Performance() : null;
    Util.debug(det());

    for(pre = 0; pre < size; ++pre) {
      if((pre & 0xFFFF) == 0) check();

      final int k = data.kind(pre);
      if(k != Data.TEXT) {
        if(scm == 1 && k == Data.DOC) unit.add(pre);
        continue;
      }
      if(scm == 2) unit.add(pre);

      wp.init(data.text(pre, true));
      while(wp.more()) {
        final byte[] tok = wp.get();
        // skip too long and stopword tokens
        if(tok.length <= MAXLEN && (sw.size() == 0 || sw.id(tok) == 0)) {
          // check if main-memory is exhausted
          if((ntok++ & 0xFFF) == 0 && scm == 0 && memFull()) {
            // currently no frequency support for tfidf based scoring
            writeIndex(csize++);
            Performance.gc(2);
          }
          index(tok);
        }
      }
    }

    // calculate term frequencies
    if(scm > 0) {
      maxfreq = new int[unit.size() + 1];
      ntoken = new int[nrTokens()];
      token = 0;
      calcFreq();
    }

    // write tokens
    token = 0;
    write();

    // set meta data
    if(scm > 0) {
      data.meta.ftscmax = max;
      data.meta.ftscmin = min;
    }
    data.meta.ftxindex = true;
    data.meta.dirty = true;

    Util.gc(perf);
  }

  /**
   * Calculates the tf-idf data for a single token.
   * @param vpre pre values for a token
   */
  protected final void calcFreq(final byte[] vpre) {
    int np = 4;
    int nl = Num.len(vpre, np);
    int p = Num.read(vpre, np);
    final int ns = Num.size(vpre);
    while(np < ns) {
      int u = unit.find(p);
      if(u < 0) u = -u - 1;

      int fr = 0;
      do {
        ++fr;
        np += nl;
        if(np >= ns) break;
        p = Num.read(vpre, np);
        nl = Num.len(vpre, np);
      } while(scm == 1 && (u == unit.size() || p < unit.get(u)) ||
          scm == 2 && p == unit.get(u));

      freq.add(fr);
      if(maxfreq[u] < fr) maxfreq[u] = fr;
      ntoken[token]++;
    }
    ++token;
  }

  /**
   * Writes the current index to disk.
   * @param cs current file pointer
   * @throws IOException I/O exception
   */
  protected abstract void writeIndex(final int cs) throws IOException;

  /**
   * Merges temporary indexes for the current token.
   * @param out full-text data
   * @param il array mapping
   * @param v full-text list
   * @return written size
   * @throws IOException I/O exception
   */
  protected final int merge(final DataOutput out, final IntList il,
      final FTList[] v) throws IOException {

    int s = 0;
    final TokenBuilder tbp = new TokenBuilder();
    final TokenBuilder tbo = new TokenBuilder();
    tbp.add(new byte[4]);
    tbo.add(new byte[4]);
    // merge full-text data of all sorted lists with the same token
    for(int j = 0; j < il.size(); ++j) {
      final int m = il.get(j);
      for(final int p : v[m].prv) tbp.add(Num.num(p));
      for(final int p : v[m].pov) tbo.add(Num.num(p));
      s += v[m].size;
      v[m].next();
    }
    // write compressed pre and pos arrays
    final byte[] pr = tbp.finish();
    Num.size(pr, pr.length);
    final byte[] po = tbo.finish();
    Num.size(po, po.length);

    // write full-text data
    writeFTData(out, pr, po);
    return s;
  }

  /**
   * Writes full-text data for a single token to disk.
   * @param out DataOutput for disk access
   * @param vpre compressed pre values
   * @param vpos compressed pos values
   * @throws IOException IOException
   */
  protected final void writeFTData(final DataOutput out, final byte[] vpre,
      final byte[] vpos) throws IOException {

    int np = 4, pp = 4, lp = -1, lu = -1;
    final int ns = Num.size(vpre);
    while(np < ns) {
      if(scm > 0) {
        final int p = Num.read(vpre, np);
        if(lp != p) {
          // find document root
          int u = unit.find(p);
          if(u < 0) u = -u - 1;

          if(lu != u) {
            final int s = Scoring.tfIDF(freq.get(fc++),
                maxfreq[u], unit.size(), ntoken[token]);
            if(max < s) max = s;
            if(min > s) min = s;
            if(np != 4) out.write(0);
            out.writeBytes(Num.num(s));
            lu = u;
          }
          lp = p;
        }
      }

      // full-text data is stored here, with -scoreU, pre1, pos1, ...,
      // -scoreU, preU, posU
      for(final int l = np + Num.len(vpre, np); np < l; ++np)
        out.write(vpre[np]);
      for(final int l = pp + Num.len(vpos, pp); pp < l; ++pp)
        out.write(vpos[pp]);
    }
    ++token;
  }

  /**
   * Checks if any unprocessed pre values are remaining.
   * @param lists lists
   * @return boolean
   */
  protected final boolean check(final FTList[] lists) {
    for(final FTList l : lists) if(l.tok.length > 0) return true;
    return false;
  }

  /**
   * Indexes a single token.
   * @param tok token to be indexed
   * @throws IOException IOException
   */
  abstract void index(final byte[] tok) throws IOException;

  /**
   * Returns the number of disjunct tokens.
   * @return number of tokens
   */
  abstract int nrTokens();

  /**
   * Evaluates the maximum frequencies for tfidf.
   */
  abstract void calcFreq();

  /**
   * Writes the index data to disk.
   * @throws IOException I/O exception
   */
  abstract void write() throws IOException;

  @Override
  public final void abort() {
    DropDB.drop(data.meta.name, DATAFTX + ".*" + IO.BASEXSUFFIX,
        data.meta.prop);
    data.meta.ftxindex = false;
  }

  @Override
  public final String det() {
    return INDEXFTX;
  }
}
