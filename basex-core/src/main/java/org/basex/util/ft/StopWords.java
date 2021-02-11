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
 * Stop words for full-text requests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StopWords {
  /** Tokens. */
  private final TokenSet set = new TokenSet();

  /**
   * Default constructor.
   */
  public StopWords() { }

  /**
   * Constructor, reading stopword list from disk.
   * And creating database stopword file.
   * @param data data reference
   * @param file stopwords file (can be empty string)
   * @throws IOException I/O exception
   */
  public StopWords(final Data data, final String file) throws IOException {
    if(!file.isEmpty()) read(IO.get(file), false);
    try(DataOutput out = new DataOutput(data.meta.dbFile(DATASWL))) {
      set.write(out);
    }
  }

  /**
   * Compiles the stop word list.
   * @param data data reference
   */
  public void compile(final Data data) {
    // stop words have not been initialized, database is on disk...
    if(set.isEmpty() && data != null && !data.inMemory()) {
      // try to parse the stop words file of the current database
      final IOFile file = data.meta.dbFile(DATASWL);
      if(!file.exists()) return;
      try(DataInput in = new DataInput(data.meta.dbFile(DATASWL))) {
        set.read(in);
      } catch(final Exception ex) {
        Util.debug(ex);
      }
    }
  }

  /**
   * Reads a stop words file.
   * @param file file reference
   * @param exclude exclude stop words
   * @throws IOException I/O exception
   */
  public void read(final IO file, final boolean exclude) throws IOException {
    final byte[] content = normalize(file.read());
    final int s = Token.contains(content, ' ') ? ' ' : '\n';
    for(final byte[] sl : split(content, s)) {
      if(exclude) set.remove(sl);
      else set.put(sl);
    }
  }

  /**
   * Checks if the specified stopword exists.
   * @param token token to be looked up
   * @return result of check
   */
  public boolean contains(final byte[] token) {
    return !set.isEmpty() && set.contains(token);
  }

  /**
   * Removes the specified stopword.
   * @param token token to be removed
   */
  public void remove(final byte[] token) {
    set.remove(token);
  }

  /**
   * Adds the specified stopword.
   * @param token token to be added
   */
  public void add(final byte[] token) {
    set.add(token);
  }
}
