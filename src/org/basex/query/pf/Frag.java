package org.basex.query.pf;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.MetaData;
import org.basex.index.Names;

/**
 * Data fragment, extending {@link MemData}.
 * To speedup fragment construction and simplify merging of fragments,
 * all fragments use the same name index and meta data instance.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class Frag extends MemData {
  /** Static tag index. */
  private static Names tgs;
  /** Static attribute index. */
  private static Names ats;
  /** Text values. */
  private static MetaData met;
  
  /** Constructor. */
  Frag() {
    super(8, tgs == null ? new Names(true) : tgs, ats == null ?
        new Names(false) : ats);
    if(met == null) met = new MetaData("frag");
    tgs = tags;
    ats = atts;
  }

  /** Finishes the data fragment by removing static instances. */
  static void finish() { tgs = null; ats = null; met = null; }

  /**
   * Adds a tag to the index and returns the reference.
   * @param tag tag to be added
   * @return tag reference
   */
  int addTag(final byte[] tag) { return tags.index(tag); }

  /**
   * Copies data fragments. This is currently one of the bottlenecks of
   * XQuery processing, especially if a whole disk instance of a database
   * is added.
   * @param data data reference
   */
  void copy(final Data data) {
    if(data == null) return;
    final int s = data.size;
    
    if(data instanceof Frag) {
      // specified instance is of same type... copy directly
      final Frag d = (Frag) data;
      for(int p = 0; p < s; p++) append(d, p);
    } else {
      // another type found (e.g. DiskData).. copy by node type. 
      // MemData must also copied node by node as index references differ.
      final Data d = data;
      for(int p = 0; p < s; p++) {
        final int k = d.kind(p);
        final int r = p - d.parent(p, k);
  
        if(k == Data.ATTR) {
          addAtt(tags.index(d.attName(p)), d.attValue(p), r);
        } else if(k == Data.ELEM || k == Data.DOC) {
          addElem(tags.index(d.tag(p)), r, d.attSize(p, k), d.size(p, k), k);
        } else {
          addText(d.text(p), r, k);
        }
      }
    }
  }
}
