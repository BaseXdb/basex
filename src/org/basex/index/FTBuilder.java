package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.IntList;
import org.basex.util.Num;
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
  final Tokenizer wp;
  /** Initial nodes for tfidf calculation. */
  final IntList nodes = new IntList();
  /** Container for maximal frequencies. */
  int[] maxfreq;
  /** Container for all frequencies. */
  IntList freq;
  /** Container for number of documents with token i. */
  int[] nmbdocwt;
  /** Scoring mode. 1 = document based, 2 = textnode based .*/
  int scm;
  /** Frequency counter. */
  int fc;
  /** Entry counter. */
  int c;
  /** Maximum indexed score. */
  int maxscore;
  
    
  /**
   * Constructor.
   * @param d data reference
   * @param pr database properties
   */
  protected FTBuilder(final Data d, final Prop pr) {
    super(d);
    scm = pr.num(Prop.FTSCTYPE);
    wp = scm > 0 ? new ScoringTokenizer(pr) : new Tokenizer(pr);    
  }

  /**
   * Extracts and indexes words from the specified data reference.
   * @throws IOException IO exception
   */
  final void index() throws IOException {
    for(id = 0; id < total; id++) {
      if(data.kind(id) != Data.TEXT) {
        if(data.kind(id) == Data.DOC && scm == 1) nodes.add(id);
        continue; 
      } 
      
      if (scm == 2) nodes.add(id);
      checkStop();
      wp.init(data.text(id));
      while(wp.more()) {
        final byte[] tok = wp.get();
        // skip too long tokens
        if(tok.length <= MAXLEN) index(tok);
      }
    }
    if (scm > 0) {
      freq = new IntList();
      getFreq();
    }
//    Performance.gc(5);
//    System.out.println(Performance.getMem());
    write();
    if (scm > 0) {
      data.meta.ftmaxscore = maxscore;
      data.meta.dirty = true;
    }    
  }

  /**
   * Write full-text data to disk.
   * @param out DataOutput for disk access
   * @param vpre compressed pre values
   * @param vpos compressed pos values 
   * @throws IOException IOException
   */
  
  final void writeFTData(final DataOutput out, final byte[] vpre, 
      final byte[] vpos) throws IOException {
    int lpre = 4;
    int lpos = 4;

    // fulltext data is stored here, with -scoreU, pre1, pos1, ...,
    // -scoreU, preU, posU
    final int pres = Num.size(vpre);
    final int poss = Num.size(vpos);
    int cn = scm == 1 ? 1 : 0; 
    int lastpre = -1;
    int pre = -1;
    while(lpre < pres && lpos < poss) {
      if (scm > 0) {
        if (lastpre < pre) fc++;
        pre = Num.read(vpre, lpre);
        
        while (cn < nodes.size() && nodes.get(cn) < pre) cn++;
        if (scm == 1 && (cn < nodes.size() && nodes.get(cn - 1) < pre &&
            nodes.get(cn) > pre || cn == nodes.size() && nodes.get(cn - 1) <
            pre) && pre != lastpre || scm == 2 && pre == nodes.get(cn)) {
          final int score = ScoringTokenizer.score(nodes.size(), 
              nmbdocwt[c], maxfreq[cn - (scm == 1 ? 1 : 0)], freq.get(fc));
          if (score > maxscore) maxscore = score;
          // first write score value
          out.write(Num.num(-score));
          if (scm == 2) {
            fc++;
            cn++;
          }
        }
        lastpre = pre;
      }
      
      // write fulltext data
      for(int z = 0, l = Num.len(vpre, lpre); z < l; z++)
        out.write(vpre[lpre++]);
      for(int z = 0, l = Num.len(vpos, lpos); z < l; z++)
        out.write(vpos[lpos++]);
    }
  }
  
  /**
   * Calculate frequencies for tfidf.
   * @param vpre pre values for a token
   */
  final void getFreq(final byte[] vpre) {
    int lpre = 4;
    final int size = Num.size(vpre);        
    int cr = 1;
    int co = 0;
    int pre = Num.read(vpre, lpre);
    int le = Num.len(vpre, lpre);
    while(lpre < size) {
      // find document root
      while (cr < nodes.size() && pre > nodes.get(cr)) cr++;
      while ((scm == 1 && (cr == nodes.size() || pre < nodes.get(cr))) ||
          scm == 2 && pre == nodes.get(cr - 1)) {              
        co++;
        lpre += le;
        if (lpre >= size) break;
        pre = Num.read(vpre, lpre);
        le = Num.len(vpre, lpre);            
      }
      if (co > 0) {
        maxfreq[cr - 1] = co > maxfreq[cr - 1] ? co : maxfreq[cr - 1];
        freq.add(co);
      }
      if (co > 0) nmbdocwt[c]++;
      co = 0;
      cr++;
    }
    c++;
  }  

  
  /**
   * Indexes a single token.
   * @param tok token to be indexed
   */
  abstract void index(final byte[] tok);

  /**
   * Writes the index data to disk.
   * @throws IOException I/O exception
   */
  abstract void write() throws IOException;
  
  /**
   * Evaluates the maximum frequencies for tfidf. 
   */
  abstract void getFreq();

  @Override
  public final String det() {
    return INDEXFTX;
  }
}
