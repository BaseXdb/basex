package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.ViewRect;

/**
 * Dialog window for additional TreeMap information.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogMapInfo extends Dialog {
  /** show information. */
  private BaseXCheckBox info;  
  /** Table Component. */
  private BaseXBack p = new BaseXBack();;
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapInfo(final GUI main) {
    super(main, MAPINFOTITLE, false);
    setResizable(true);
    
    // create checkbox
    info = new BaseXCheckBox(MAPINFOTOGGLE, HELPMAPINFO,
        GUIProp.mapinfo, 0, this);
    set(info, BorderLayout.NORTH);
    
    finish(GUIProp.mapinfoloc);
    action(null);
  }
  
  /**
   * Constructor.
   * 
   * @param main gui reference
   * @param nno number of nodes old map
   * @param nnn  number of nodes new map
   * @param recto size old map
   * @param rectn size new map
   * @param aaro avarage aspect ratio old
   * @param aarn average aspect ratio new
   * @param distance change between old and new
   */
  public DialogMapInfo(final GUI main, final int nno, final int nnn,
      final ViewRect recto, final ViewRect rectn, final double aaro, 
      final double aarn, final double distance) {
    this(main);

    // main table
    setValues(nno, nnn, recto, rectn, aaro, aarn, distance);
    
    finish(GUIProp.mapinfoloc);
    action(null);
  }

  /**
   * sets the table values.
   * @param nno number of nodes old map
   * @param nnn  number of nodes new map
   * @param recto size old map
   * @param rectn size new map
   * @param aaro avarage aspect ratio old
   * @param aarn average aspect ratio new
   * @param distance change between old and new
   */
  public void setValues(final int nno, final int nnn,
      final ViewRect recto, final ViewRect rectn, final double aaro, 
      final double aarn, final double distance) {    
    remove(p);
    // main table
    p.removeAll();
    p.setLayout(new TableLayout(9, 3, 8, 0));
    p.add(new BaseXLabel(""));
    p.add(new BaseXLabel("neue Map", true, true));
    p.add(new BaseXLabel("alte Map", true, true));
    p.add(new BaseXLabel("size: ", true, true));
    p.add(new BaseXLabel(Integer.toString(rectn.w) + "x" + 
        Integer.toString(rectn.h)));
    p.add(new BaseXLabel(Integer.toString(recto.w) + "x" + 
        Integer.toString(recto.h)));
    p.add(new BaseXLabel("nodes painted: ", true, true));
    p.add(new BaseXLabel(Integer.toString(nnn)));
    p.add(new BaseXLabel(Integer.toString(nno)));
    p.add(new BaseXLabel("Average aspect ratio: ", true, true));
    p.add(new BaseXLabel(Double.toString(aarn)));
    p.add(new BaseXLabel(Double.toString(aaro)));
    p.add(new BaseXLabel("distance change: ", true, true));
    p.add(new BaseXLabel(Double.toString(distance)));
    set(p, BorderLayout.CENTER);
    
    pack();
    validate();
  }
  @Override
  public void action(final String cmd) {
    GUIProp.mapinfo = info.isSelected();
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
