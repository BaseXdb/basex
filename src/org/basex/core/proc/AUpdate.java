package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Process;
import org.basex.core.Commands.CmdUpdate;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * Abstract class for database updates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class AUpdate extends Process {
  /** GUI flag. */
  protected final boolean gui;
  /** Target position. */
  protected int pos;

  /**
   * Protected constructor.
   * @param g gui gui flag
   * @param a arguments
   */
  protected AUpdate(final boolean g, final String... a) {
    super(DATAREF | UPDATING, a);
    gui = g;
  }

  /**
   * Returns the update type.
   * @return update type.
   */
  protected CmdUpdate getType() {
    try {
      return CmdUpdate.valueOf(args[0].toUpperCase());
    } catch(final Exception ex) {
      error(CMDWHICH, args[0]);
      return null;
    }
  }

  /**
   * Retrieves the pre value for the specified child position.
   * @param par parent node
   * @param pos child position
   * @param data data reference
   * @return pre value
   */
  protected static int pre(final int par, final int pos, final Data data) {
    int k = data.kind(par);
    if(pos == 0) return par + data.size(par, k);
    int pre = par + data.attSize(par, k);
    for(int p = 1; p < pos; pre += data.size(pre, k), p++) {
      k = data.kind(pre);
      if(data.parent(pre, k) != par) break;
    }
    return pre;
  }

  /**
   * Checks the validity of the specified name.
   * @param name name to be checked
   * @return result of check
   */
  protected static boolean check(final byte[] name) {
    if(name.length == 0) return false;
    int i = -1;
    while(++i != name.length) {
      final byte c = name[i];
      if(Token.letter(c) || c == ':') continue;
      if(i == 0) break;
      if(!Token.digit(c) && c != '-' && c != '.') break;
    }
    return i == name.length;
  }

  /**
   * Returns a string array composed by the three arguments.
   * @param a first argument
   * @param b second argument
   * @param c second argument
   * @return string array
   */
  protected static String[] init(final String a, final String b,
      final String... c) {
    final StringList list = new StringList();
    list.add(a);
    list.add(b);
    for(final String d : c) list.add(d);
    return list.finish();
  }

  /**
   * Performs some update checks.
   * @return success flag
   */
  protected boolean checkDB() {
    final Data data = context.data();
    return data.ns.size() != 0 ? error(UPDATENS) : data instanceof MemData ?
        error(PROCMM) : pos < 0 ? error(POSINVALID) : true;
  }
}
