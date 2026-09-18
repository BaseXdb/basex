package org.basex.index.ft;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.expr.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * The buffer of an updatable full-text index (see {@link IndexBuffer}), persisted in the log file
 * {@code ftxb}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FTBuffer extends IndexBuffer implements FTSource {
  /**
   * Constructor.
   * @param data data reference
   * @param committed committed length of the log ({@code -1} if unknown)
   * @param refs number of references in the committed log
   * @throws IOException I/O exception
   */
  FTBuffer(final Data data, final long committed, final int refs) throws IOException {
    super(data, data.meta.dbFile(FTIndex.LOG), committed, refs);
  }

  @Override
  public void exact(final byte[] token, final IntList pres, final IntList poss) {
    live(token, pres, poss, true);
  }

  @Override
  public void wildcards(final FTWildcard wc, final boolean full, final IntList pres,
      final IntList poss) {
    final byte[] prefix = wc.prefix();
    collect(prefix.length, wc.max(full), token -> startsWith(token, prefix) && wc.match(token),
      pres, poss);
  }

  @Override
  public void fuzzy(final FTFuzzy fuzzy, final IntList pres, final IntList poss) {
    collect(fuzzy.minLength(), fuzzy.maxLength(), fuzzy::similar, pres, poss);
  }

  /**
   * Collects the live references of all matching tokens.
   * @param first first token length
   * @param last last token length
   * @param matcher token matcher
   * @param pres PRE values
   * @param poss positions
   */
  private void collect(final int first, final int last, final Predicate<byte[]> matcher,
      final IntList pres, final IntList poss) {
    final int l = Math.min(buckets() - 1, last);
    for(int s = first; s <= l; s++) {
      final TokenObjectMap<IntList> map = bucket(s);
      if(map == null) continue;
      final int ms = map.size();
      for(int m = 1; m <= ms; m++) {
        if(matcher.test(map.key(m))) live(map.value(m), pres, poss, true);
      }
    }
  }

  @Override
  public EntryIterator entries(final byte[] prefix) {
    return entries(prefix.length, Integer.MAX_VALUE, token -> startsWith(token, prefix));
  }

  @Override
  public EntryIterator entries(final FTFuzzy fuzzy) {
    return entries(fuzzy.minLength(), fuzzy.maxLength(), fuzzy::similar);
  }

  /**
   * Returns all matching tokens with live references, in ascending order.
   * @param first first token length
   * @param last last token length
   * @param matcher token matcher
   * @return entry iterator
   */
  private EntryIterator entries(final int first, final int last,
      final Predicate<byte[]> matcher) {
    final int l = Math.min(buckets() - 1, last);
    return new EntryIterator() {
      int s = first - 1, i, nr;
      TokenList list = new TokenList(0);

      @Override
      public byte[] next() {
        while(true) {
          if(i < list.size()) {
            final byte[] token = list.get(i++);
            nr = live(bucket(s).get(token), null, null, false);
            if(nr > 0) return token;
            continue;
          }
          if(++s > l) return null;
          final TokenObjectMap<IntList> map = bucket(s);
          if(map == null) continue;
          list = new TokenList();
          for(final byte[] token : map) {
            if(matcher.test(token)) list.add(token);
          }
          list.sort();
          i = 0;
        }
      }

      @Override
      public int count() {
        return nr;
      }
    };
  }
}
