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
import org.basex.util.Tokenizer;

/**
 * This class provides a skeleton for full-text index builders.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class FTBuilder extends IndexBuilder {
  /** Word parser. */
  protected final Tokenizer wp;
  /** Scoring mode; see {@link Prop#SCORING}. */
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

  @Override
  public void abort() {
    data.meta.ftxindex = false;
  }

  /**
   * Extracts and indexes words from the specified data reference.
   * @throws IOException IO exception
   */
  protected final void index() throws IOException {
    final Performance perf = Prop.debug ? new Performance() : null;
    Main.debug("Full-texts:");

    for(pre = 0; pre < size; pre++) {
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
        // skip too long tokens
        if(tok.length <= MAXLEN && sw.id(tok) == 0) index(tok);
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

    if(scm > 0) {
      data.meta.ftscmax = max;
      data.meta.ftscmin = min;
    }
    data.meta.ftxindex = true;
    data.meta.dirty = true;

    Main.gc(perf);
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
