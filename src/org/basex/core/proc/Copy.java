package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Evaluates the 'copy' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Copy extends Updates {
  @Override
  protected boolean exec() {
    final Data data = context.data();
    final int pos = Token.toInt(cmd.arg(0));
    if(pos < 0) return error(POSINVALID, cmd.arg(0));

    Nodes source;
    Nodes target;
    final boolean gui = cmd.nrArgs() == 1;
    if(gui) {
      source = context.copied();
      target = context.marked();
      context.copy(null);
    } else {
      // perform query and check if all result nodes reference tags
      source = query(cmd.arg(1), null);
      target = query(cmd.arg(2), COPYTAGS);
    }
    if(source == null || target == null) return false;

    data.meta.noIndex();

    final int size = source.size;
    final Data[] srcDocs = new Data[source.size];
    for(int c = 0; c < size; c++)
      srcDocs[c] = Insert.copy(source.data, source.pre[c]);

    final IntList marked = gui ? new IntList() : null;
    int copied = 0;

    for(int n = target.size - 1; n >= 0; n--) {
      final int par = target.pre[n];
      if(data.kind(par) != Data.ELEM) return error(COPYTAGS);

      for(int c = 0; c < size; c++) {
        final int pre = Insert.pre(par, pos, data);
        
        // merge text nodes if necessary
        // [CG] Copy.MergeText: might not cover all cases yet
        
        final int s = srcDocs[c].size - 1;
        final int up = s != 0 ? -1 :
          Insert.checkText(data, pre, par, srcDocs[c].kind(s));
        if(up != -1) {
          data.update(up, Token.concat(data.text(up), srcDocs[c].text(s)));
          if(gui && !marked.contains(up)) marked.add(up);
        } else {
          data.insert(pre, par, srcDocs[c]);
          if(gui) marked.add(pre);
        }
      }
      copied += size;
    }
    
    if(gui) {
      if(context.current().size > 1 || 
          context.current().pre[0] == source.pre[0]) {
        context.current(new Nodes(0, data));
      }
      context.marked(new Nodes(marked.finish(), data));
    }

    data.flush();
    return Prop.info ? timer(BaseX.info(INSERTINFO, copied)) : true;
  }
}
