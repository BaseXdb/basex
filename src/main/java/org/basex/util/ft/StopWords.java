package org.basex.util.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.TokenSet;

/**
 * Simple stop words set for full-text requests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class StopWords extends TokenSet {
  /**
   * Default constructor.
   */
  public StopWords() { }

  /**
   * Constructor, reading stopword list from disk.
   * And creating database stopword file.
   * @param data data reference
   * @param file stopword list file
   * @throws IOException I/O exception
   */
  public StopWords(final Data data, final String file) throws IOException {
    if(!data.meta.prop.get(Prop.STOPWORDS).isEmpty()) read(IO.get(file), false);
    final DataOutput out = new DataOutput(data.meta.dbfile(DATASWL));
    write(out);
    out.close();
  }

  /**
   * Compiles the stop word list.
   * @param data data reference
   */
  public void comp(final Data data) {
    // no data reference, or stop words have already been defined..
    if(data == null || size() != 0 || data instanceof MemData) return;

    // try to parse the stop words file of the current database
    try {
      final IOFile file = data.meta.dbfile(DATASWL);
      if(!file.exists()) return;
      final DataInput in = new DataInput(data.meta.dbfile(DATASWL));
      try {
        read(in);
      } finally {
        in.close();
      }
    } catch(final Exception ex) {
      Util.debug(ex);
    }
  }

  /**
   * Reads a stop words file.
   * @param fl file reference
   * @param e except flag
   * @return true if everything went alright
   */
  public boolean read(final IO fl, final boolean e) {
    try {
      final byte[] content = norm(fl.read());
      final int s = Token.contains(content, ' ') ? ' ' : '\n';
      for(final byte[] sl : split(content, s)) {
        if(e) delete(sl);
        else if(!contains(sl)) add(sl);
      }
      return true;
    } catch(final IOException ex) {
      return false;
    }
  }
}
