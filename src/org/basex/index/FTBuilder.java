package org.basex.index;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.util.Token;
import org.basex.util.Tokenizer;

/**
 * This class provides a skeleton for full-text index builders.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class FTBuilder extends Progress implements IndexBuilder {
  /** Word parser. */
  final Tokenizer wp;
  /** Total parsing value. */
  int total;
  /** Current parsing value. */
  int id;

  /**
   * Constructor.
   * @param pr database properties
   */
  protected FTBuilder(final Prop pr) {
    wp = new Tokenizer(pr);
  }
  
  /**
   * Extracts and indexes words from the specified data reference.
   * @param data data reference
   * @throws IOException IO exception
   */
  final void index(final Data data) throws IOException {
    total = data.meta.size;
    for(id = 0; id < total; id++) {
      if(data.kind(id) == Data.TEXT) {
        checkStop();
        wp.init(data.text(id));
        while(wp.more()) {
          final byte[] tok = wp.get();
          // skip too long tokens
          if(tok.length <= Token.MAXLEN) index(tok);
        }
      }
    }
    write(data);
  }

  /**
   * Indexes a single token.
   * @param tok token to be indexed
   */
  abstract void index(final byte[] tok);

  /**
   * Writes the index data to disk.
   * @param data data reference
   * @throws IOException I/O exception
   */
  abstract void write(final Data data) throws IOException;

  @Override
  public final String tit() {
    return PROGINDEX;
  }

  @Override
  public final String det() {
    return INDEXFTX;
  }

  @Override
  public final double prog() {
    return (double) id / total;
  }
}
