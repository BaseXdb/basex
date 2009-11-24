package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.query.ft.Scoring;
import org.basex.query.ft.StopWords;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.ScoringTokenizer;
import org.basex.util.Tokenizer;

/**
 * This class provides a skeleton for full-text index builders.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class FTBuilder extends IndexBuilder {
  /** Word parser. */
  protected final Tokenizer wp;
  /** Scoring mode; see {@link Prop#FTSCTYPE}. */
  protected final int scm;

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
  private StopWords sw;

  /**
   * Constructor.
   * @param d data reference
   * @param pr database properties
   * @throws IOException IOException
   */
  protected FTBuilder(final Data d, final Prop pr) throws IOException {
    super(d);
    scm = pr.num(Prop.FTSCTYPE);
    wp = scm > 0 ? new ScoringTokenizer(pr) : new Tokenizer(pr);
    
    max = -1;
    min = Integer.MAX_VALUE;
    sw = new StopWords(d, pr.get(Prop.FTSTOPW));
  }

  /**
   * Extracts and indexes words from the specified data reference.
   * @throws IOException IO exception
   */
  protected final void index() throws IOException {
    for(pre = 0; pre < total; pre++) {
      final int k = data.kind(pre);
      if(k != Data.TEXT) {
        if(scm == 1 && k == Data.DOC) unit.add(pre);
        continue;
      }
      if(scm == 2) unit.add(pre);

      checkStop();
      wp.init(data.text(pre));
      while(wp.more()) {
        final byte[] tok = wp.get();
        // skip too long tokens
        if(tok.length <= MAXLEN && !sw.contains(tok)) index(tok);
      }
    }
    if(scm > 0) {
      maxfreq = new int[unit.size() + 1];
      ntoken = new int[nrTokens()];
      token = 0;
      getFreq();
    }
    
    // normalization
    token = 0;
    write();
    
    if(Prop.debug) {
      Performance.gc(4);
      Main.debug("Memory: " + Performance.getMem());
    }

    if(scm > 0) {
      data.meta.ftscmax = max;
      data.meta.ftscmin = min;
      data.meta.ftsctype = scm;
      data.meta.dirty = true;
    }
  }

  /**
   * Calculates the tf-idf data for a single token.
   * @param vpre pre values for a token
   */
  protected final void getFreq(final byte[] vpre) {
    int np = 4;
    int nl = Num.len(vpre, np);
    int p = Num.read(vpre, np);
    final int ns = Num.size(vpre);
    while(np < ns) {
      int u = unit.find(p);
      if(u < 0) u = -u - 1;

      int fr = 0;
      do {
        fr++;
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
    token++;
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
        int p = Num.read(vpre, np);
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
            out.write(Num.num(s));
            lu = u;
          }
          lp = p;
        }
      }
      // fulltext data is stored here, with -scoreU, pre1, pos1, ...,
      // -scoreU, preU, posU
      for(final int l = np + Num.len(vpre, np); np < l; np++)
        out.write(vpre[np]);
      for(final int l = pp + Num.len(vpos, pp); pp < l; pp++)
        out.write(vpos[pp]);
    }
    token++;
  }

  /**
   * Indexes a single token.
   * @param tok token to be indexed
   */
  abstract void index(final byte[] tok);

  /**
   * Returns the number of disjunct tokens.
   * @return number of tokens
   */
  abstract int nrTokens();

  /**
   * Evaluates the maximum frequencies for tfidf.
   */
  abstract void getFreq();

  /**
   * Writes the index data to disk.
   * @throws IOException I/O exception
   */
  abstract void write() throws IOException;

  @Override
  public final String det() {
    return INDEXFTX;
  }
}
