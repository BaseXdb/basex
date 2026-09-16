package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.query.*;
import org.basex.index.resource.*;
import org.basex.index.stats.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Inspects the storage structures of a database for inconsistencies.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Inspection {
  /** Checks. */
  public enum Check {
    /** Storage structures that cannot be read. */
    STORAGE_ACCESS,
    /** Nodes with an invalid kind. */
    NODE_KIND,
    /** Documents and elements with invalid sizes. */
    NODE_SIZE,
    /** Nodes with invalid parent references. */
    PARENT_REFERENCE,
    /** Nodes whose parent has an invalid kind. */
    PARENT_KIND,
    /** Nodes outside the descendant or attribute range of their parent. */
    PARENT_RANGE,
    /** Nodes whose parent is not the innermost enclosing node. */
    PARENT_NESTING,
    /** Top-level nodes that are no documents, and nested documents. */
    DOCUMENT_STRUCTURE,
    /** Difference between the number of documents and the meta data. */
    DOCUMENT_COUNT,
    /** Documents with invalid paths. */
    DOCUMENT_PATH,
    /** Documents missing in the resource index. */
    DOCUMENT_INDEX,
    /** Elements and attributes with invalid name references. */
    NAME_REFERENCE,
    /** Elements and attributes with invalid names. */
    NAME_SYNTAX,
    /** Names whose statistics counts differ from the actual occurrences. */
    NAME_STATISTICS,
    /** Elements and attributes with invalid namespace references. */
    NAMESPACE_REFERENCE,
    /** Prefixed names whose namespace differs from the in-scope namespace of the prefix. */
    NAMESPACE_BINDING,
    /** Elements with a namespace flag but without namespace declarations. */
    NAMESPACE_FLAG,
    /** Nodes with invalid text or attribute value references. */
    VALUE_REFERENCE,
    /** Values with invalid characters, and invalid comments and processing instructions. */
    VALUE_SYNTAX,
    /** Empty and adjacent text nodes. */
    TEXT_STRUCTURE,
    /** Nodes with invalid or duplicate IDs. */
    NODE_ID,
    /** Wrong ID/PRE mappings. */
    ID_PRE,
    /** Nodes missing in the path index, and wrong path statistics counts. */
    PATH_INDEX,
    /** Wrong or missing text index entries. */
    TEXT_INDEX,
    /** Wrong or missing attribute index entries. */
    ATTRIBUTE_INDEX,
    /** Wrong or missing token index entries. */
    TOKEN_INDEX,
    /** Missing index files. */
    INDEX_FILES,
    /** Interrupted update. */
    UPDATE_FILE;

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }
  }

  /**
   * Inconsistencies found by a single check.
   * @param check check
   * @param count number of inconsistencies
   * @param first PRE value of the first inconsistent node ({@code -1} if not applicable)
   */
  public record Issue(Check check, int count, int first) { }

  /** Number of inspected nodes. */
  public final int nodes;

  /** Data reference. */
  private final Data data;
  /** Job (for interrupting the inspection). */
  private final Job job;
  /** Performed checks. */
  private final EnumSet<Check> checks = EnumSet.allOf(Check.class);
  /** Number of inconsistencies per check. */
  private final int[] counts = new int[Check.values().length];
  /** First inconsistent node per check. */
  private final int[] firsts = new int[Check.values().length];
  /** Assigned node IDs. */
  private final BitSet ids = new BitSet();
  /** PRE values of all documents. */
  private final IntList docs = new IntList();
  /** PRE values of the enclosing documents and elements. */
  private final IntList ancestors = new IntList();
  /** Occurrences of element names (can be {@code null}). */
  private final int[] elemCounts;
  /** Occurrences of attribute names (can be {@code null}). */
  private final int[] attrCounts;
  /** Occurrences of path index nodes (can be {@code null}). */
  private final IdentityHashMap<PathNode, int[]> pathCounts;
  /** PRE value of the next top-level node. */
  private int next;

  /**
   * Constructor, performing the inspection.
   * @param data data reference
   * @param job job (for interrupting the inspection)
   */
  public Inspection(final Data data, final Job job) {
    this.data = data;
    this.job = job;
    final MetaData meta = data.meta;
    nodes = data.nodes();
    Arrays.fill(firsts, -1);
    if(!meta.updindex) checks.remove(Check.ID_PRE);
    if(!meta.counts) checks.remove(Check.NAME_STATISTICS);
    if(!meta.complete) checks.remove(Check.PATH_INDEX);
    if(!meta.textindex) checks.remove(Check.TEXT_INDEX);
    if(!meta.attrindex) checks.remove(Check.ATTRIBUTE_INDEX);
    if(!meta.tokenindex) checks.remove(Check.TOKEN_INDEX);
    if(data.inMemory()) checks.removeAll(EnumSet.of(Check.INDEX_FILES, Check.UPDATE_FILE));

    final boolean stats = checks.contains(Check.NAME_STATISTICS);
    elemCounts = stats ? new int[data.elemNames.size() + 1] : null;
    attrCounts = stats ? new int[data.attrNames.size() + 1] : null;
    pathCounts = checks.contains(Check.PATH_INDEX) && meta.counts ? new IdentityHashMap<>() : null;

    for(int pre = 0; pre < nodes; pre++) {
      if((pre & 0xFFFF) == 0) job.checkStop();
      try {
        node(pre);
      } catch(final RuntimeException ex) {
        Util.debug(ex);
        add(Check.STORAGE_ACCESS, pre);
      }
    }
    add(Check.DOCUMENT_COUNT, Math.abs(meta.ndocs - docs.size()), -1);

    run(this::documents);
    if(checks.contains(Check.ID_PRE)) run(this::idPre);
    if(stats) run(this::statistics);
    if(pathCounts != null) run(() -> paths(data.paths().root().get(0)));
    if(checks.contains(Check.TEXT_INDEX)) run(() -> index(IndexType.TEXT, Check.TEXT_INDEX));
    if(checks.contains(Check.ATTRIBUTE_INDEX)) {
      run(() -> index(IndexType.ATTRIBUTE, Check.ATTRIBUTE_INDEX));
    }
    if(checks.contains(Check.TOKEN_INDEX)) run(() -> index(IndexType.TOKEN, Check.TOKEN_INDEX));

    if(!data.inMemory()) {
      final String[] files = { meta.textindex ? DATATXT : null, meta.attrindex ? DATAATV : null,
        meta.tokenindex ? DATATOK : null };
      for(final String file : files) {
        if(file == null) continue;
        for(final char c : new char[] { 'l', 'r' }) {
          if(!meta.dbFile(file + c).exists()) add(Check.INDEX_FILES, 1, -1);
        }
      }
      if(meta.updateFile().exists()) add(Check.UPDATE_FILE, 1, -1);
    }
  }

  /**
   * Checks a single node.
   * @param pre PRE value
   */
  private void node(final int pre) {
    final int kind = data.kind(pre);
    // document structure: top-level nodes must be documents, and documents must be top-level
    if(pre == next) {
      if(kind != Data.DOC) add(Check.DOCUMENT_STRUCTURE, pre);
      next = pre + Math.max(1, data.size(pre, kind));
    } else if(kind == Data.DOC) {
      add(Check.DOCUMENT_STRUCTURE, pre);
    }
    if(kind == Data.DOC) docs.add(pre);
    if(kind > Data.PI) {
      add(Check.NODE_KIND, pre);
      return;
    }

    final boolean container = kind == Data.ELEM || kind == Data.DOC;
    if(container) {
      final int size = data.size(pre, kind), asize = data.attSize(pre, kind);
      if(asize < 1 || size < asize || (long) pre + size > nodes) add(Check.NODE_SIZE, pre);
    }

    // parent: valid reference and kind, node within range, innermost enclosing node
    while(!ancestors.isEmpty() && pre >= end(ancestors.peek())) ancestors.pop();
    final int par = kind == Data.DOC ? -1 : data.parent(pre, kind);
    final boolean parent = par >= 0 && par < pre;
    if(kind != Data.DOC) {
      if(!parent) {
        add(Check.PARENT_REFERENCE, pre);
      } else {
        final int pkind = data.kind(par);
        if(pkind != Data.ELEM && (kind == Data.ATTR || pkind != Data.DOC)) {
          add(Check.PARENT_KIND, pre);
        } else {
          final int asize = par + data.attSize(par, pkind);
          if(pre >= (long) par + data.size(par, pkind) ||
              (kind == Data.ATTR ? pre >= asize : pre < asize)) {
            add(Check.PARENT_RANGE, pre);
          } else if(ancestors.isEmpty() || ancestors.peek() != par) {
            add(Check.PARENT_NESTING, pre);
          }
        }
      }
    }
    if(container) {
      if(!ancestors.isEmpty() && end(pre) > end(ancestors.peek())) add(Check.NODE_SIZE, pre);
      ancestors.push(pre);
    }

    if(kind == Data.ELEM || kind == Data.ATTR) name(pre, kind, parent ? par : -1);
    if(kind != Data.ELEM) {
      if(data.validText(pre, kind != Data.ATTR)) value(pre, kind, par);
      else add(Check.VALUE_REFERENCE, pre);
    }

    final int id = data.id(pre);
    if(id < 0 || id > data.lastid || ids.get(id)) {
      add(Check.NODE_ID, pre);
    } else {
      ids.set(id);
      if(checks.contains(Check.ID_PRE) && data.pre(id) != pre) add(Check.ID_PRE, pre);
    }

    if(checks.contains(Check.PATH_INDEX)) {
      final PathNode node = data.paths().node(pre);
      if(node == null) add(Check.PATH_INDEX, pre);
      else if(pathCounts != null) pathCounts.computeIfAbsent(node, n -> new int[1])[0]++;
    }
  }

  /**
   * Checks the name and namespace of an element or attribute.
   * @param pre PRE value
   * @param kind node kind
   * @param par PRE value of the parent node ({@code -1} if the reference is invalid)
   */
  private void name(final int pre, final int kind, final int par) {
    final boolean elem = kind == Data.ELEM;
    if(elem && data.nsFlag(pre) && data.namespaces(pre).isEmpty()) {
      add(Check.NAMESPACE_FLAG, pre);
    }
    final Names names = elem ? data.elemNames : data.attrNames;
    final int id = data.nameId(pre);
    if(id < 1 || id > names.size()) {
      add(Check.NAME_REFERENCE, pre);
      return;
    }
    if(elemCounts != null) (elem ? elemCounts : attrCounts)[id]++;

    final byte[] name = names.key(id), prefix = prefix(name);
    if(!XMLToken.isQName(name) || eq(prefix, XMLNS) || !elem && eq(name, XMLNS)) {
      add(Check.NAME_SYNTAX, pre);
    }
    final int uriId = data.uriId(pre, kind);
    if(uriId > data.nspaces.size()) {
      add(Check.NAMESPACE_REFERENCE, pre);
    } else if(prefix.length == 0 ? !elem && uriId != 0 : !eq(prefix, XML) && (uriId == 0 ||
        (elem || par != -1) && data.nspaces.uriIdForPrefix(prefix, elem ? pre : par, data) !=
        uriId)) {
      add(Check.NAMESPACE_BINDING, pre);
    }
  }

  /**
   * Checks the value of a node.
   * @param pre PRE value
   * @param kind node kind
   * @param par PRE value of the parent node
   */
  private void value(final int pre, final int kind, final int par) {
    final byte[] value = data.text(pre, kind != Data.ATTR);
    boolean valid = true;
    final int vl = value.length;
    for(int v = 0; v < vl && valid; v += cl(value, v)) valid = XMLToken.valid11(cp(value, v));

    switch(kind) {
      case Data.COMM -> valid &= !contains(value, token("--")) && !endsWith(value, '-');
      case Data.PI -> {
        final byte[] target = data.name(pre, kind);
        valid &= XMLToken.isNCName(target) && !eq(lc(target), XML) && !contains(value, token("?>"));
      }
      case Data.TEXT -> {
        if(vl == 0 || pre > 0 && data.kind(pre - 1) == Data.TEXT &&
            data.parent(pre - 1, Data.TEXT) == par) add(Check.TEXT_STRUCTURE, pre);
      }
      case Data.DOC -> {
        final String path = string(value);
        if(!path.equals(MetaData.normPath(path))) add(Check.DOCUMENT_PATH, pre);
      }
      default -> { }
    }
    if(!valid) add(Check.VALUE_SYNTAX, pre);
  }

  /**
   * Checks if the resource index contains all documents.
   */
  private void documents() {
    if(counts[Check.DOCUMENT_STRUCTURE.ordinal()] + counts[Check.NODE_SIZE.ordinal()] +
        counts[Check.NODE_KIND.ordinal()] + counts[Check.STORAGE_ACCESS.ordinal()] != 0) return;

    final Resources resources = data.resources;
    final IntList list = resources.docs();
    final int ds = docs.size();
    for(int d = 0; d < ds; d++) {
      final int pre = docs.get(d);
      if(d >= list.size() || list.get(d) != pre ||
          !resources.docs(string(data.text(pre, true))).contains(pre)) {
        add(Check.DOCUMENT_INDEX, pre);
      }
    }
    if(list.size() > ds) add(Check.DOCUMENT_INDEX, list.size() - ds, list.get(ds));
  }

  /**
   * Checks if unassigned IDs are mapped to PRE values.
   */
  private void idPre() {
    for(int id = 0; id <= data.lastid; id++) {
      if((id & 0xFFFF) == 0) job.checkStop();
      if(!ids.get(id)) {
        final int pre = data.pre(id);
        if(pre != -1) add(Check.ID_PRE, pre);
      }
    }
  }

  /**
   * Compares the statistics counts of all names with their occurrences.
   */
  private void statistics() {
    for(final boolean elem : new boolean[] { true, false }) {
      final Names names = elem ? data.elemNames : data.attrNames;
      final int[] occ = elem ? elemCounts : attrCounts;
      final int ns = names.size();
      for(int id = 1; id <= ns; id++) {
        final Stats stats = names.stats(id);
        if((stats != null ? stats.count : 0) != occ[id]) add(Check.NAME_STATISTICS, 1, -1);
      }
    }
  }

  /**
   * Compares the statistics counts of a path node and its descendants with their occurrences.
   * @param node path node
   */
  private void paths(final PathNode node) {
    final int[] occ = pathCounts.get(node);
    if(node.stats.count != (occ != null ? occ[0] : 0)) add(Check.PATH_INDEX, 1, -1);
    for(final PathNode child : node.children) paths(child);
  }

  /**
   * Compares the entries of a value index with the database nodes.
   * @param type index type
   * @param check check
   */
  private void index(final IndexType type, final Check check) {
    final Index index = data.index(type);
    final boolean text = type == IndexType.TEXT, tokenize = type == IndexType.TOKEN;
    final IndexNames names = new IndexNames(type, data);
    final int maxlen = data.meta.maxlen;

    // check index entries: sorted, existing nodes, matching values
    final int[] found = new int[nodes];
    final EntryIterator entries = index.entries(new IndexEntries(EMPTY, type));
    for(byte[] entry; (entry = entries.next()) != null;) {
      job.checkStop();
      final byte[] key = entry;
      final IndexIterator iter = index.iter(new StringToken(type, key));
      int last = -1, size = 0;
      while(iter.more()) {
        final int pre = iter.pre();
        size++;
        if(pre <= last || pre >= nodes || !names.unit(pre) || (tokenize ?
          !Arrays.stream(distinctTokens(data.text(pre, false))).anyMatch(t -> eq(t, key)) :
          !eq(data.text(pre, text), key))) {
          add(check, pre);
        } else {
          found[pre]++;
          last = pre;
        }
      }
      if(size != entries.count()) add(check, 1, -1);
    }

    // check if all nodes are indexed
    for(int pre = 0; pre < nodes; pre++) {
      if((pre & 0xFFFF) == 0) job.checkStop();
      final int expected = !names.unit(pre) ? 0 : tokenize ?
        distinctTokens(data.text(pre, false)).length : data.textLen(pre, text) <= maxlen ? 1 : 0;
      if(found[pre] != expected) add(check, pre);
    }
  }

  /**
   * Runs a check and registers failed storage access.
   * @param check check
   */
  private void run(final Runnable check) {
    try {
      check.run();
    } catch(final RuntimeException ex) {
      if(ex instanceof JobException) throw ex;
      Util.debug(ex);
      add(Check.STORAGE_ACCESS, 1, -1);
    }
  }

  /**
   * Returns the end of the range of a document or element.
   * @param pre PRE value
   * @return end of the range
   */
  private long end(final int pre) {
    return (long) pre + data.size(pre, data.kind(pre));
  }

  /**
   * Returns the issues of all failed checks.
   * @return issues
   */
  public ArrayList<Issue> issues() {
    final ArrayList<Issue> issues = new ArrayList<>();
    for(final Check check : checks) {
      final int c = check.ordinal();
      if(counts[c] > 0) issues.add(new Issue(check, counts[c], firsts[c]));
    }
    return issues;
  }

  /**
   * Returns a textual summary of all performed checks.
   * @return info string
   */
  public String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add("Nodes: ").addInt(nodes).add(Prop.NL);
    tb.add("Checks:").add(Prop.NL);
    int failed = 0;
    for(final Check check : checks) {
      final int c = check.ordinal();
      tb.add("- ").add(check.toString()).add(": ");
      if(counts[c] == 0) {
        tb.add("OK");
      } else {
        tb.addInt(counts[c]);
        if(firsts[c] != -1) tb.add(" (first: pre ").addInt(firsts[c]).add(')');
        failed++;
      }
      tb.add(Prop.NL);
    }
    tb.add(failed == 0 ? "No inconsistencies found." :
      Util.info("Database is inconsistent: % of % checks failed.", failed, checks.size()));
    return tb.add(Prop.NL).toString();
  }

  /**
   * Registers an inconsistent node.
   * @param check check
   * @param pre PRE value
   */
  private void add(final Check check, final int pre) {
    add(check, 1, pre);
  }

  /**
   * Registers inconsistencies.
   * @param check check
   * @param count number of inconsistencies
   * @param pre PRE value of the first inconsistent node ({@code -1} if not applicable)
   */
  private void add(final Check check, final int count, final int pre) {
    final int c = check.ordinal();
    counts[c] += count;
    if(firsts[c] == -1) firsts[c] = pre;
  }
}
