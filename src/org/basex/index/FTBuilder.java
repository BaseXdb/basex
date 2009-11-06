package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.util.IntList;
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
    final byte[][] st = {
        token("natural disaster"), token("wonder girls"), 
        token("plays of shakespeare"), token("cloud computing"), 
        token("evidence theory"), token("dempster schafer"), 
        token("opengl shading language"), token("rabindranath tagore"), 
        token("finland industry manufacturer"), token("saab sisu car"), 
        token("slumdog millionaire"), token("danny boyle"), 
        token("tiananmen square"), token("hard disk"), 
        token("stock exchange"), token("insider trading"), 
        token("game show"), token("hatha yoga"), 
        token("world wide web"), token("french revolution"), 
        token("global warming"), token("human activity"), 
        token("paul is dead"), token("virtual museums"), 
    };
    
    for(id = 0; id < total; id++) {
      if(data.kind(id) != Data.TEXT) {
        if(data.kind(id) == Data.DOC && scm == 1) nodes.add(id);
        continue; 
      } 
      
      if (scm == 2) nodes.add(id);
      checkStop();
      int p = 0, i = 0, j = 0;
      wp.init(data.text(id));
      while(wp.more()) {
        final byte[] tok = wp.get();
        // skip too long tokens
        if(tok.length <= MAXLEN) index(tok);
        
        // [SG] INEX index phrases
        if(wp instanceof ScoringTokenizer) {
          if(j == 0) {
            for(i = 0; i < st.length; i++) { 
              if(startsWith(st[i], tok)) {
                p++;
                j += tok.length + 1;
                break;
              }
            }
          } else {
            if(st[i].length >= j + tok.length &&
                eq(substring(st[i], j, j + tok.length), tok)) {
              if(j + tok.length == st[i].length) {
                index(st[i]);
                j = 0;
              } else {
                j += tok.length + 1;
              }
            } else {
              j = 0;
            }
          }                        
        }          
      }
    }
    if (scm > 0) {
      freq = new IntList();
      getFreq();
    }
    write();
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
