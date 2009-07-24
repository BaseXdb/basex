package org.basex.core.proc;

import org.basex.core.Process;
import org.basex.core.Commands.CmdUpdate;
import org.basex.data.Data;
import org.basex.util.Token;

/**
 * Abstract class for database updates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class AUpdate extends Process {
  /** Insert type. */
  private final String type;

  /**
   * Constructor.
   * @param t update type
   * @param a arguments
   */
  protected AUpdate(final String t, final String... a) {
    super(DATAREF | UPDATING, a);
    type = t;
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
   * Returns the update type.
   * @return update type.
   */
  protected CmdUpdate getType() {
    return CmdUpdate.valueOf(type.toUpperCase());
  }
}
