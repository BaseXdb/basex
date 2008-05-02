package org.basex.core.proc;

import static org.basex.Text.*;

/**
 * Evaluates the 'optimize' command. Optimizes the current database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Optimize extends Proc {
  @Override
  protected boolean exec() {
    /* rebuild statistics
    final Data data = context.data();
    try {
      //Stats.create(data);
    } catch(final IOException ex) {
      return error(DBOPTERR1);
    }*/
    
    /*final Stats stats = new Stats();
    for(int pre = 0; pre < data.size; pre++) {
      final int kind = data.kind(pre);
      if(kind == Data.ELEM) {
        final byte[] tag = data.tag(pre);
        stats.index(tag, null);
        //final int par = data.parent(pre, kind);
        //while(l > 0 && parStack[l - 1] <= par) --l;
        //tagStack[l] = tag;
        //parStack[l++] = pre;
      } else if(kind == Data.ATTR) {
        stats.index(data.attName(pre), data.attValue(pre));
      } else if(kind == Data.TEXT) {
        //System.out.println(l + ": " + new String(tagStack[l - 1]) + ": " +
        //    new String(data.text(pre)));
        //stats.index(tagStack[l - 1], data.text(pre));
        byte[] txt = data.text(pre);
        byte[] key = data.tag(data.parent(pre, kind));
        stats.index(key, txt);
      }
    }
    stats.write(data.meta.dbname);
    */

    timer(DBOPT1 + NL);

    // rebuild indexes, minimize data files... not quite finished

    return true;
  }
}
