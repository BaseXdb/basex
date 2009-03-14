package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Evaluates the 'copy' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Copy extends AUpdate {
  /**
   * Constructor.
   * @param a arguments
   */
  public Copy(final String... a) {
    super(null, a);
  }
  
  @Override
  protected boolean exec() {
    final Data data = context.data();
    final int pos = Token.toInt(args[0]);
    if(pos < 0) return error(POSINVALID, args[0]);

    Nodes src;
    Nodes trg;
    if(Prop.gui) {
      src = context.copied();
      trg = context.marked();
      context.copy(null);
    } else {
      // perform query and check if all result nodes reference tags
      src = query(args[1], null);
      trg = query(args[2], COPYTAGS);
      if(src == null || trg == null) return false;
    }
    data.meta.update();

    final int size = src.size();
    final Data[] srcDocs = new Data[src.size()];
    for(int c = 0; c < size; c++) srcDocs[c] = copy(src.data, src.nodes[c]);

    final IntList marked = Prop.gui ? new IntList() : null;
    int copied = 0;

    for(int n = trg.size() - 1; n >= 0; n--) {
      final int par = trg.nodes[n];
      if(data.kind(par) != Data.ELEM) return error(COPYTAGS);

      for(int c = 0; c < size; c++) {
        final int pre = pre(par, pos, data);
        
        // merge text nodes if necessary
        // [CG] Updates/MergeText: might not cover all cases yet
        
        final int s = srcDocs[c].meta.size - 1;
        final int up = s != 0 ? -1 :
          Insert.checkText(data, pre, par, srcDocs[c].kind(s));
        if(up != -1) {
          data.update(up, Token.concat(data.text(up), srcDocs[c].text(s)));
          if(Prop.gui && !marked.contains(up)) marked.add(up);
        } else {
          data.insert(pre, par, srcDocs[c]);
          if(Prop.gui) marked.add(pre);
        }
      }
      copied += size;
    }
    
    if(Prop.gui) {
      if(context.current().size() > 1 || 
          context.current().nodes[0] == src.nodes[0]) {
        context.current(new Nodes(0, data));
      }
      context.marked(new Nodes(marked.finish(), data));
    }

    data.flush();
    return Prop.info ? info(INSERTINFO, copied, perf.getTimer()) : true;
  }

  /**
   * Creates a memory data instance from the specified database and pre value.
   * @param data data reference
   * @param pre pre value
   * @return database instance
   */
  public static Data copy(final Data data, final int pre) {
    // size of the data instance
    final int size = data.size(pre, data.kind(pre));
    // create temporary data instance, adopting the indexes of the source data
    final MemData tmp = new MemData(size, data.tags, data.atts, data.ns,
        data.path);

    // copy all nodes
    for(int p = pre; p < pre + size; p++) {
      final int k = data.kind(p);
      final int d = p - data.parent(p, k);
      switch(k) {
        case Data.DOC:
          tmp.addDoc(data.text(p), data.size(p, k));
          break;
        case Data.ELEM:
          tmp.addElem(data.tagID(p), data.tagNS(p), d, data.attSize(p, k),
              data.size(p, k), data.ns(p).length != 0);
          break;
        case Data.ATTR:
          tmp.addAtt(data.attNameID(p), data.attNS(p), data.attValue(p), d);
          break;
        case Data.TEXT:
        case Data.COMM:
        case Data.PI:
          tmp.addText(data.text(p), d, k);
          break;
      }
    }
    return tmp;
  }

  @Override
  public String toString() {
    return name() +  " " + args[0] + " " + args[1] + ", " + args[2];
  }
}
