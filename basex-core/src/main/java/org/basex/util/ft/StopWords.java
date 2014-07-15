package org.basex.util.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple stop words set for full-text requests.
 *
 * @author BaseX Team 2005-14, BSD License
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
    if(!data.meta.options.get(MainOptions.STOPWORDS).isEmpty()) read(IO.get(file), false);
    try(final DataOutput out = new DataOutput(data.meta.dbfile(DATASWL))) {
      write(out);
    }
  }

  /**
   * Compiles the stop word list.
   * @param data data reference
   */
  public void comp(final Data data) {
    // no data reference, or stop words have already been defined..
    if(data == null || size() != 0 || data.inMemory()) return;

    // try to parse the stop words file of the current database
    final IOFile file = data.meta.dbfile(DATASWL);
    if(!file.exists()) return;
    try(final DataInput in = new DataInput(data.meta.dbfile(DATASWL))) {
      read(in);
    } catch(final Exception ex) {
      Util.debug(ex);
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
      final byte[] content = norm(file.read());
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
