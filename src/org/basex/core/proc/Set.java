package org.basex.core.proc;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'set' command and modifies database properties.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Set extends Proc {
  /**
   * Default constructor.
   * @param key property
   * @param value value to set (optional, depending on the property)
   */
  public Set(final Object key, final Object value) {
    super(STANDARD, (key instanceof Object[] ?
        ((Object[]) key)[0] : key).toString(),
        value == null ? null : value.toString());
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final String key = args[0].toUpperCase();
    String val = args[1];

    CmdSet s = null;
    try {
      s = Enum.valueOf(CmdSet.class, key);
    } catch(final Exception ex) { }

    try {
      final Object type = prop.object(key);

      if(type instanceof Boolean) {
        final boolean b = val == null ? !((Boolean) type).booleanValue() :
          val.equalsIgnoreCase(ON) || val.equalsIgnoreCase(TRUE);
        prop.set(key, b);

        final boolean all = ALL.equalsIgnoreCase(val);
        val = Main.flag(b);
        if(s == CmdSet.INFO) {
          prop.set(Prop.ALLINFO, all);
          if(all) {
            prop.set(Prop.INFO, true);
            val = INFOON + " (" + INFOALL + ")";
          }
        }
      } else if(type instanceof Integer) {
        prop.set(key, Integer.parseInt(val));
      } else if(type instanceof String) {
        prop.set(key, val);
      } else {
        Main.notexpected();
      }
      try {
        return info(Text.class.getField("INFO" + s).get(null).toString() +
            ": " + val);
      } catch(final Exception ex) {
        return info(key + ": " + val);
      }
    } catch(final Exception ex) {
      Main.debug(ex);
      return error(SETERR, key, val);
    }
  }
}
