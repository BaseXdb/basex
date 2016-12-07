package org.basex.util.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple stop words set for full-text requests.
 *
 * @author BaseX Team 2005-16, BSD License
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
   * @param file stopwords file
   * @throws IOException I/O exception
   */
  public StopWords(final Data data, final String file) throws IOException {
    if(!file.isEmpty()) read(IO.get(file), false);
    try(DataOutput out = new DataOutput(data.meta.dbfile(DATASWL))) {
      write(out);
    }
  }

  /**
   * Compiles the stop word list.
   * @param data data reference
   */
  public void comp(final Data data) {
    // stop words have not been initialized, database is on disk...
    if(isEmpty() && data != null && !data.inMemory()) {
      // try to parse the stop words file of the current database
      final IOFile file = data.meta.dbfile(DATASWL);
      if(!file.exists()) return;
      try(DataInput in = new DataInput(data.meta.dbfile(DATASWL))) {
        read(in);
      } catch(final Exception ex) {
        Util.debug(ex);
      }
    }
  }

  /**
   * Reads a stop words file.
   * @param file file reference
   * @param exclude exclude stop words
   * @return success flag
   */
  public boolean read(final IO file, final boolean exclude) {
    try {
      final byte[] content = normalize(file.read());
      final int s = Token.contains(content, ' ') ? ' ' : '\n';
      for(final byte[] sl : split(content, s)) {
        if(exclude) delete(sl);
        else put(sl);
      }
      return true;
    } catch(final IOException ex) {
      return false;
    }
  }
}
