package org.basex.data;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.index.name.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class resolves the namespace URIs of the element and attribute names of a database.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class NSNames {
  /** Name has not been found. */
  private static final int UNKNOWN = -1;
  /** Name is bound to several URIs. */
  private static final int SEVERAL = -2;
  /** Name has a different prefix. */
  private static final int OTHER = -3;
  /** Maximum number of nodes to be scanned. */
  private static final int MAX_SCAN = 1_000_000;

  /** Data reference. */
  private final Data data;
  /** URI IDs of the element and attribute names, indexed by prefix IDs. */
  private final IntObjectMap<int[][]> cache = new IntObjectMap<>();

  /**
   * Constructor.
   * @param data data reference
   */
  NSNames(final Data data) {
    this.data = data;
  }

  /**
   * Returns the namespace URI of a prefixed element or attribute name.
   * @param name name
   * @param element element flag
   * @return URI, or {@code null} if the name is bound to several URIs or cannot be resolved
   */
  synchronized byte[] uri(final byte[] name, final boolean element) {
    final Names names = element ? data.elemNames : data.attrNames;
    final byte[] prefix = prefix(name);
    final int nameId = names.index(name), prefId = data.nspaces.prefixId(prefix);
    if(nameId == 0 || prefId == 0 || !data.meta.counts) return null;

    final int id = cache.computeIfAbsent(prefId, () -> resolve(prefix, prefId))[
      element ? 0 : 1][nameId];
    return id == UNKNOWN || id == SEVERAL ? null : id == 0 ? EMPTY : data.nspaces.uri(id);
  }

  /**
   * Resolves the URI IDs of all element and attribute names with the specified prefix.
   * @param prefix prefix
   * @param prefId ID of prefix
   * @return URI IDs of the element and attribute names
   */
  private int[][] resolve(final byte[] prefix, final int prefId) {
    final Namespaces nspaces = data.nspaces;
    final int emptyId = nspaces.uriId(EMPTY), size = data.meta.size;

    // namespace nodes binding the prefix (index bs: region outside all nodes)
    final IntList bindings = nspaces.bindings(prefId);
    final int bs = bindings.size() >>> 1;
    final int[] pres = new int[bs], ends = new int[bs + 1], uris = new int[bs + 1];
    final long[] sizes = new long[bs + 1];
    ends[bs] = size;
    sizes[bs] = size;
    final IntList stack = new IntList();
    for(int b = 0; b < bs; b++) {
      final int pre = bindings.get(b << 1), uriId = bindings.get((b << 1) + 1);
      pres[b] = pre;
      ends[b] = pre + data.size(pre, Data.ELEM);
      uris[b] = uriId == emptyId ? 0 : uriId;
      while(!stack.isEmpty() && ends[stack.peek()] <= pre) stack.pop();
      final int s = ends[b] - pre;
      sizes[b] += s;
      sizes[stack.isEmpty() ? bs : stack.peek()] -= s;
      stack.push(b);
    }
    // scan all regions except for the largest one, whose names are derived from the statistics
    int largest = bs;
    for(int b = 0; b < bs; b++) {
      if(sizes[b] > sizes[largest]) largest = b;
    }

    final int[][] ids = { ids(data.elemNames, prefix), ids(data.attrNames, prefix) };
    // skip resolution if too many nodes would need to be scanned
    if(size - sizes[largest] > MAX_SCAN) {
      for(final int[] nids : ids) Arrays.fill(nids, SEVERAL);
      return ids;
    }
    final int[][] found = { new int[ids[0].length], new int[ids[1].length] };
    stack.reset();
    for(int pre = 0, b = 0; pre < size;) {
      while(!stack.isEmpty() && ends[stack.peek()] <= pre) stack.pop();
      if(b < bs && pres[b] == pre) stack.push(b++);
      final int region = stack.isEmpty() ? bs : stack.peek();
      if(region == largest) {
        // skip the region: continue with the next nested node or the end of the region
        pre = b < bs && pres[b] < ends[region] ? pres[b] : ends[region];
        continue;
      }
      final int kind = data.kind(pre);
      if(kind == Data.ELEM || kind == Data.ATTR) {
        final int n = kind == Data.ELEM ? 0 : 1, nameId = data.nameId(pre);
        final int id = ids[n][nameId];
        if(id != SEVERAL && id != OTHER) {
          final int uriId = data.uriId(pre, kind), u = uriId == emptyId ? 0 : uriId;
          ids[n][nameId] = id == UNKNOWN || id == u ? u : SEVERAL;
          found[n][nameId]++;
        }
      }
      pre++;
    }

    // names that have not been found in the scanned regions belong to the largest region
    for(int n = 0; n < 2; n++) {
      final Names names = n == 0 ? data.elemNames : data.attrNames;
      final int[] nids = ids[n];
      final int nl = nids.length;
      for(int nameId = 1; nameId < nl; nameId++) {
        final int id = nids[nameId];
        if(id == OTHER || id == SEVERAL) continue;
        if(found[n][nameId] < names.stats(nameId).count) {
          final int u = uris[largest];
          nids[nameId] = id == UNKNOWN || id == u ? u : SEVERAL;
        }
      }
    }
    return ids;
  }

  /**
   * Creates an array for the URI IDs of the names with the specified prefix.
   * @param names names
   * @param prefix prefix
   * @return array
   */
  private static int[] ids(final Names names, final byte[] prefix) {
    final int ns = names.size();
    final int[] ids = new int[ns + 1];
    Arrays.fill(ids, OTHER);
    for(int n = 1; n <= ns; n++) {
      if(eq(prefix(names.key(n)), prefix)) ids[n] = UNKNOWN;
    }
    return ids;
  }
}
