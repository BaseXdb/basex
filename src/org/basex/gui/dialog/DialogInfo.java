package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.core.Commands;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.Performance;

/**
 * Info Database Dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogInfo extends Dialog {
  /** Index Checkbox. */
  private final BaseXCheckBox[] indexes = new BaseXCheckBox[3];

  /**
   * Default Constructor.
   * @param gui reference to main frame
   */
  public DialogInfo(final GUI gui) {
    super(gui, INFOTITLE);

    final BaseXBack info = new BaseXBack();
    info.setLayout(new TableLayout(10, 2));
    info.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 10, 10)));

    final Data data = GUI.context.data();
    final MetaData meta = data.meta;

    // get size of database
    final File dir = IO.dbpath(meta.dbname);
    long len = 0;
    for(final File f : dir.listFiles()) len += f.length();

    add(info, INFODBNAME, meta.dbname);
    add(info, INFODOC, meta.file.path());
    add(info, INFOTIME, new SimpleDateFormat(
        "dd.MM.yyyy hh:mm:ss").format(new Date(meta.time)));
    add(info, INFODOCSIZE, meta.filesize != 0 ?
        Performance.formatSize(meta.filesize) : "-");
    add(info, INFODBSIZE, Performance.formatSize(len));
    add(info, INFOENCODING, meta.encoding);
    add(info, INFONODES, Integer.toString(data.size));
    add(info, INFOHEIGHT, Integer.toString(meta.height));

    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new BorderLayout());
    pp.add(info, BorderLayout.NORTH);

    final BaseXBack check = new BaseXBack();
    check.setLayout(new TableLayout(3, 2, 0, 5));
    check.setBorder(10, 10, 10, 10);

    check.add(new BaseXLabel(INFOINDEX, true));
    check.add(new BaseXLabel(""));
    indexes[0] = add(check, INFOTXTINDEX, meta.txtindex);
    indexes[2] = add(check, INFOFTINDEX, meta.ftxindex);
    indexes[1] = add(check, INFOATVINDEX, meta.atvindex);
    pp.add(check, BorderLayout.CENTER);

    set(pp, BorderLayout.CENTER);

    set(BaseXLayout.newButtons(this, true,
        new String[] { BUTTONOPT, BUTTONOK, BUTTONCANCEL },
        new byte[][] { HELPOPT, HELPOK, HELPCANCEL }), BorderLayout.SOUTH);

    //setInfo();
    finish(gui);
  }

  /**
   * Adds two labels to the specified panel.
   * @param back panel reference
   * @param s1 first string
   * @param s2 second string
   */
  private void add(final BaseXBack back, final String s1, final String s2) {
    final BaseXLabel label = new BaseXLabel(s1.replace(":", "       "));
    label.setFont(getFont().deriveFont(1));
    back.add(label);
    back.add(new BaseXLabel(s2));
  }

  /**
   * Adds a checkbox to the specified panel.
   * @param back panel reference
   * @param s first string
   * @param v value
   * @return check box
   */
  private BaseXCheckBox add(final BaseXBack back, final String s,
      final boolean v) {
    final BaseXCheckBox check = new BaseXCheckBox(s + "        ",
        null, v, 0, this);
    back.add(check);
    return check;
  }

  /**
   * Returns an array with the chosen indexes.
   * @return check box
   */
  public boolean[] indexes() {
    final boolean[] ind = new boolean[indexes.length];
    for(int i = 0; i < indexes.length; i++) ind[i] = indexes[i].isSelected();
    return ind;
  }
  
  @Override
  public void action(final String cmd) {
    if(BUTTONOPT.equals(cmd)) GUI.get().execute(Commands.OPTIMIZE);
  }
}
