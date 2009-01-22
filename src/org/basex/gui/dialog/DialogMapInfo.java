package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for additional TreeMap information.
 * 
 * [JH] needs some more labels for average aspect ratio, distance change, 
 * number of drawn nodes, time needed, readability, ... if checkbox is enabled
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogMapInfo extends Dialog {
  /** show information. */
  private BaseXCheckBox info;
  /** average aspect ratios. */
//  private BaseXLabel aar1, aar2;
  
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapInfo(final GUI main) {
    super(main, MAPINFOTITLE, false);
    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(6, 2, 0, 5));
    this.setResizable(true);
    // create checkbox
    info = new BaseXCheckBox(MAPINFOTOGGLE, HELPMAPINFO,
        GUIProp.mapinfo, 0, this);
    
    set(info, BorderLayout.CENTER);
    finish(GUIProp.mapinfoloc);
    action(null);
  }

  @Override
  public void action(final String cmd) {
    gui.notify.layout();
  }

  @Override
  public void close() {
    close(GUIProp.mapinfoloc);
    GUIProp.write();
  }

  @Override
  public void cancel() {
    close();
  }
}
