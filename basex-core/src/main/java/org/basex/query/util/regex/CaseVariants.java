package org.basex.query.util.regex;

import java.util.*;

import org.basex.util.list.*;

/**
 * Case variants of characters, as defined for the {@code i} flag of regular expressions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class CaseVariants {
  /** Case variants. */
  private static final Table TABLE = table();

  /** Private constructor. */
  private CaseVariants() { }

  /**
   * Returns the case variants of a code point.
   * @param cp code point
   * @return case variants, including the code point itself, or {@code null} if there are none
   */
  static int[] get(final int cp) {
    final int i = Arrays.binarySearch(TABLE.cased, cp);
    return i >= 0 ? TABLE.variants[i] : null;
  }

  /**
   * Returns the case variants of a character range that lie outside the range.
   * @param left left character
   * @param right right character
   * @return case variants
   */
  static int[] get(final int left, final int right) {
    final int[] cased = TABLE.cased;
    final BitSet set = new BitSet();
    final int s = Arrays.binarySearch(cased, left), cl = cased.length;
    for(int c = s >= 0 ? s : -s - 1; c < cl && cased[c] <= right; c++) {
      for(final int v : TABLE.variants[c]) {
        if(v < left || v > right) set.set(v);
      }
    }
    return set.stream().toArray();
  }

  /**
   * Builds the table of case variants.
   * @return table
   */
  private static Table table() {
    // group characters with case mappings (first two Unicode planes) by lower and upper case
    final Map<String, IntList> lower = new HashMap<>(), upper = new HashMap<>();
    final IntList cps = new IntList();
    final ArrayList<IntList[]> groups = new ArrayList<>();
    for(int cp = 0; cp <= 0x1FFFF; cp++) {
      if(Character.toLowerCase(cp) != cp || Character.toUpperCase(cp) != cp ||
          Character.toTitleCase(cp) != cp || Character.isLowerCase(cp) ||
          Character.isUpperCase(cp)) {
        final String string = Character.toString(cp);
        cps.add(cp);
        groups.add(new IntList[] {
          lower.computeIfAbsent(string.toLowerCase(Locale.ENGLISH), k -> new IntList(1)).add(cp),
          upper.computeIfAbsent(string.toUpperCase(Locale.ENGLISH), k -> new IntList(1)).add(cp)
        });
      }
    }

    // assign variants to all characters that have at least one other variant
    final IntList cased = new IntList();
    final ArrayList<int[]> variants = new ArrayList<>();
    final int cl = cps.size();
    for(int c = 0; c < cl; c++) {
      final IntList vs = new IntList(groups.get(c)[0].toArray());
      for(final int v : groups.get(c)[1].toArray()) vs.addUnique(v);
      if(vs.size() > 1) {
        cased.add(cps.get(c));
        variants.add(vs.sort().finish());
      }
    }
    return new Table(cased.finish(), variants.toArray(int[][]::new));
  }

  /**
   * Case variants.
   * @param cased sorted code points that have case variants
   * @param variants case variants of the code points, including the code point itself
   */
  private record Table(int[] cased, int[][] variants) { }
}
