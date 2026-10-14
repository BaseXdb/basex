package org.basex.util.ft;

import static org.basex.data.DataText.*;

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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StopWords {
  /** Tokens. */
  private final TokenSet set = new TokenSet();
  /** Tokens excluded from the default stop words (if {@code null}, no defaults are added). */
  private TokenSet excluded;
  /** Stop words with the defaults of {@link #resolvedData} (can be {@code null}). */
  private StopWords resolved;
  /** Database of the resolved stop words (can be {@code null}). */
  private Data resolvedData;

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
   * Returns the stop words of a database.
   * @param data data reference
   * @return stop words
   */
  public static StopWords get(final Data data) {
    final StopWords sw = new StopWords();
    final String path = data.meta.stopwords;
    if(path.isEmpty()) return sw;

    final IOFile file = data.meta.dbFile(DATASWL);
    try {
      if(data.inMemory()) {
        sw.read(IO.get(path), false);
      } else if(file.exists()) {
        try(DataInput in = new DataInput(file)) {
          sw.set.read(in);
        }
      }
    } catch(final IOException ex) {
      Util.debug(ex);
    }
    return sw;
  }

  /**
   * Requests the stop words of the database, which are added when the query is optimized.
   */
  public void requestDefaults() {
    excluded = new TokenSet();
  }

  /**
   * Returns stop words with the requested stop words of the database.
   * @param data data reference
   * @return new stop words, or self reference if no defaults were requested
   */
  public StopWords defaults(final Data data) {
    if(excluded == null) return this;
    if(resolved == null || resolvedData != data) {
      resolved = get(data);
      resolvedData = data;
      for(final byte[] token : excluded) resolved.set.remove(token);
      for(final byte[] token : set) resolved.set.add(token);
    }
    return resolved;
  }

  /**
   * Reads a stop words file.
   * @param file file reference
   * @param exclude exclude stop words
   * @throws IOException I/O exception
   */
  public void read(final IO file, final boolean exclude) throws IOException {
    for(final String term : file.readString().trim().split("\\s+")) {
      final byte[] token = Token.token(term);
      if(exclude) remove(token);
      else add(token);
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
   * Returns the stop words, normalized like the query tokens.
   * @param opt full-text options
   * @return normalized stop words
   */
  public StopWords normalize(final FTOpt opt) {
    final StopWords sw = new StopWords();
    final FTLexer lexer = new FTLexer(opt);
    for(final byte[] word : set) {
      lexer.init(word);
      final byte[] first = lexer.hasNext() ? lexer.nextToken() : null;
      // words that are not tokenized to a single token are adopted unchanged
      sw.add(first != null && !lexer.hasNext() ? first : word);
    }
    return sw;
  }

  /**
   * Removes the specified stopword.
   * @param token token to be removed
   */
  public void remove(final byte[] token) {
    set.remove(token);
    if(excluded != null) excluded.add(token);
  }

  /**
   * Adds the specified stopword.
   * @param token token to be added
   */
  public void add(final byte[] token) {
    set.add(token);
  }
}
