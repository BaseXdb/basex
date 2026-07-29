package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.AbstractMap.*;
import java.util.Map.*;
import java.util.function.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Candidates for the code completion of the {@link TextPanel}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class Completions {
  /** Replacement lists, ordered by relevance. */
  private static final ArrayList<ArrayList<Entry<String, String>>> LISTS = new ArrayList<>();
  /** Pattern for abbreviating function names. */
  private static final Pattern ABBR = Pattern.compile("(.)[^-A-Z]*-?");
  /** Pattern for abbreviating prefixed function names. */
  private static final Pattern ABBR_PREFIX = Pattern.compile("(:?.)[^-:A-Z]*-?");

  /* Reads in the property file. */
  static {
    for(int l = 0; l < 5; l++) LISTS.add(new ArrayList<>());
    final TokenObjectMap<byte[]> map = Util.properties("completions.properties");
    for(final byte[] key : map) {
      LISTS.getFirst().add(new SimpleEntry<>(Token.string(key), Token.string(map.get(key))));
    }
    // add functions (default functions first)
    for(final FuncDefinition fd : Functions.BUILT_IN.values()) {
      final String name = string(fd.name.prefixId(QueryText.FN_URI));
      final String value = name + (fd.params.length > 0 ? "(_)" : "()");
      final BiConsumer<Integer, String> add = (i, string) ->
        LISTS.get(i).add(new SimpleEntry<>(string.toLowerCase(Locale.ENGLISH), value));
      if(fd.name.uri() == QueryText.FN_URI) {
        add.accept(1, ABBR.matcher(name).replaceAll("$1"));
        add.accept(2, name);
      } else {
        add.accept(3, ABBR_PREFIX.matcher(name).replaceAll("$1"));
        add.accept(4, name);
      }
    }
  }

  /** Hidden constructor. */
  private Completions() { }

  /**
   * Returns the insertion candidates for the specified input.
   * @param input input string (lower case)
   * @return candidates, grouped by {@code null} separators
   */
  static ArrayList<Entry<String, String>> candidates(final String input) {
    final ArrayList<Entry<String, String>> pairs = new ArrayList<>();
    final Consumer<Entry<String, String>> add = pair -> {
      for(final Entry<String, String> p : pairs) {
        if(p != null && p.getValue().equals(pair.getValue())) return;
      }
      pairs.add(pair);
    };

    // add matches that start with the input string
    final int ll = LISTS.size();
    for(final ArrayList<Entry<String, String>> list : LISTS) {
      pairs.add(null);
      for(final Entry<String, String> pair : list) {
        final String name = pair.getKey();
        if(name.startsWith(input) || name.replace(":", "").startsWith(input)) add.accept(pair);
      }
    }
    // add matches that start with and contain the input string
    for(final boolean strt : new boolean[] { true, false }) {
      if(pairs.size() != ll + 1) {
        pairs.add(null);
        for(int l = 0; l < ll; l++) {
          for(final Entry<String, String> pair : LISTS.get(l)) {
            if(SmartStrings.containsChars(pair.getKey(), input, strt)) add.accept(pair);
          }
        }
      }
    }
    // remove duplicate and trailing separators
    for(int p = 0; p < pairs.size();) {
      if(pairs.get(p) == null && (p == 0 || p + 1 == pairs.size() || pairs.get(p + 1) == null)) {
        pairs.remove(p);
      } else {
        p++;
      }
    }
    return pairs;
  }
}
