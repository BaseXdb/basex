package org.basex.gui.text;

import java.util.*;
import java.util.function.*;

import org.basex.util.*;

/**
 * Matcher for the code completion of the {@link TextPanel}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class Completions {
  /** Hidden constructor. */
  private Completions() { }

  /**
   * Returns the insertion candidates for the specified string, which will not be proposed itself.
   * @param word string at the cursor
   * @param lists candidates, ordered by relevance
   * @return matches, ordered by relevance and separated by {@code null} references
   */
  static ArrayList<Completion> candidates(final String word,
      final ArrayList<ArrayList<Completion>> lists) {

    final String input = word.toLowerCase(Locale.ENGLISH);
    final ArrayList<Completion> matches = new ArrayList<>();
    // the string at the cursor is no candidate, and duplicate insertions are skipped
    final HashSet<String> values = new HashSet<>();
    values.add(word);
    final Consumer<Completion> add = completion -> {
      // trailing whitespace is ignored: a candidate must insert more than the string at the cursor
      if(values.add(completion.value().stripTrailing())) matches.add(completion);
    };

    // add matches that start with the input string
    for(final ArrayList<Completion> list : lists) {
      final int size = matches.size();
      for(final Completion completion : list) {
        // the input is compared with the full name, the local name and the name without colon
        final String match = completion.match();
        final int c = completion.alias() ? -1 : match.indexOf(':');
        if(match.startsWith(input) || c != -1 && match.startsWith(input, c + 1) ||
          match.replace(":", "").startsWith(input)) {
          add.accept(completion);
        }
      }
      separate(matches, size);
    }
    // add matches that start with and contain the input string (skipped for a single candidate)
    if(matches.size() != 1) {
      for(final boolean strt : new boolean[] { true, false }) {
        final int size = matches.size();
        for(final ArrayList<Completion> list : lists) {
          for(final Completion completion : list) {
            if(!completion.alias() && SmartStrings.containsChars(completion.match(), input, strt)) {
              add.accept(completion);
            }
          }
        }
        separate(matches, size);
      }
    }
    return matches;
  }

  /**
   * Separates a new group of matches from the preceding ones.
   * @param matches matches
   * @param size number of matches before the new group was added
   */
  private static void separate(final ArrayList<Completion> matches, final int size) {
    if(size > 0 && matches.size() > size) matches.add(size, null);
  }
}
