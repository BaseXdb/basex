package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Commands.Cmd;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Evaluates the 'copy' command and copies nodes in the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Copy extends AUpdate {
  /**
   * Default constructor.
   * @param source source query
   * @param target target query
   * @param position target position
   */
  public Copy(final String source, final String target, final int position) {
    super(false, source, target);
    pos = position;
  }

  /**
   * Constructor, using 0 as target position.
   * @param source source query
   * @param target target query
   */
  public Copy(final String source, final String target) {
    this(source, target, 0);
  }

  /**
   * Constructor, used by the GUI.
   */
  public Copy() {
    super(true);
  }

  @Override
  protected boolean exec() {
    if(!checkDB()) return false;

    final Data data = context.data();
    Nodes src = gui ? context.copied() : query(args[0], null);
    Nodes trg = gui ? context.marked() : query(args[1], COPYTAGS);
    if(src == null || trg == null) return false;
    if(gui) context.copy(null);

    final int size = src.size();
    final Data[] srcDocs = new Data[src.size()];
    for(int c = 0; c < size; c++) srcDocs[c] = copy(src.data, src.nodes[c]);

    final IntList marked = gui ? new IntList() : null;
    int copied = 0;

    // check for duplicate source attributes
    final TokenList tl = new TokenList();
    for(int c = 0; c < size; c++) {
      if(srcDocs[c].meta.size == 1 && srcDocs[c].kind(0) == Data.ATTR) {
        final byte[] name = srcDocs[c].attName(0);
        if(tl.contains(name)) return error(ATTDUPL, srcDocs[c].attName(0));
        tl.add(name);
      }
    }

    for(int n = trg.size() - 1; n >= 0; n--) {
      final int par = trg.nodes[n];
      // source nodes can only be appended to elements
      if(data.kind(par) != Data.ELEM) return error(COPYTAGS);
      // check for duplicate target attributes
      for(int c = 0; c < size; c++) {
        if(srcDocs[c].meta.size == 1 && srcDocs[c].kind(0) == Data.ATTR) {
          final int att = srcDocs[c].attNameID(0);
          final int last = par + data.attSize(par, Data.ELEM);
          for(int p = par + 1; p < last; p++) {
            if(att == data.attNameID(p)) return error(ATTDUPL, data.attName(p));
          }
        }
      }
    }

    for(int n = trg.size() - 1; n >= 0; n--) {
      final int par = trg.nodes[n];
      for(int c = 0; c < size; c++) {
        int pre = pre(par, pos, data);

        // merge text nodes if necessary
        // [CG] Updates/MergeText: might not cover all cases yet
        final int s = srcDocs[c].meta.size - 1;
        final int txt = s != 0 ? -1 :
          Insert.checkText(data, pre, par, srcDocs[c].kind(s));
        if(txt != -1) {
          data.update(txt, Token.concat(data.text(txt), srcDocs[c].text(s)));
          if(gui && !marked.contains(txt)) marked.add(txt);
        } else {
          if(s == 0 && srcDocs[c].kind(0) == Data.ATTR) {
            pre = par + data.attSize(par, data.kind(par));
          }
          data.insert(pre, par, srcDocs[c]);
          if(gui) marked.add(pre);
        }
      }
      copied += size;
    }

    if(gui) {
      if(context.current().size() > 1 ||
          context.current().nodes[0] == src.nodes[0]) {
        context.current(new Nodes(0, data));
      }
      context.marked(new Nodes(marked.finish(), data));
    }

    data.flush();
    context.update();
    return info(INSERTINFO, copied, perf.getTimer());
  }

  /**
   * Creates a memory data instance from the specified database and pre value.
   * @param data data reference
   * @param pre pre value
   * @return database instance
   */
  public static MemData copy(final Data data, final int pre) {
    // size of the data instance
    final int size = data.size(pre, data.kind(pre));
    // create temporary data instance, adopting the indexes of the source data
    final MemData tmp = new MemData(size, data.tags, data.atts, data.ns,
        data.path, data.meta.prop);

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
    return Cmd.COPY + " " + args[0] + " " + INTO + " " +
      args[1] + (pos == 0 ? "" : " " + AT + " " + pos);
  }
}
