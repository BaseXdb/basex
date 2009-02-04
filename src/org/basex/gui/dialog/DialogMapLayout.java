package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.BaseXSlider;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for specifying the TreeMap layout.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogMapLayout extends Dialog {
  /** Map layouts. */
  private final BaseXListChooser choice;
  /** Layout slider. */
  private final BaseXSlider sizeSlider;
  /** Show attributes. */
  private final BaseXCheckBox atts;
  /** Select layout algorithm. */
  BaseXCombo propalgo;
  /** Size slider label. */
  private final BaseXLabel sizeLabel;
  
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapLayout(final GUI main) {
    super(main, MAPLAYOUTTITLE, false);
    
    final BaseXBack p = new BaseXBack();
    // [JH] complete layout
    p.setLayout(new TableLayout(4, 1, 0, 5));

    // create list
    choice = new BaseXListChooser(this, MAPLAYOUTCHOICE, HELPMAPLAYOUT);
    choice.setSize(210, 110);
    choice.setIndex(GUIProp.maplayout);
    p.add(choice);
    
    // create drop down
    final BaseXBack tmpback = new BaseXBack();
    tmpback.setLayout(new TableLayout(1, 2, 3, 5));
    final BaseXLabel label = new BaseXLabel(MAPPROPALGO);
    tmpback.add(label);
    propalgo = new BaseXCombo(new String[] { "SplitLayout",
        "SliceAndDice Layout", "SquarifiedLayout", "StripLayout"}, 
        HELPMODE, false, this);
    propalgo.setSelectedIndex(GUIProp.mapalgo);

    propalgo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final int s = propalgo.getSelectedIndex();
        if(s == GUIProp.mapalgo || !propalgo.isEnabled()) return;
        action(null);
      }
    });
    tmpback.add(propalgo);
    p.add(tmpback);

    // create slider
    sizeLabel = new BaseXLabel(MAPSIZE);
    sizeSlider = new BaseXSlider(gui, 0, 100, GUIProp.sizep, HELPMAPSIZE, this);
    BaseXLayout.setWidth(sizeSlider, p.getPreferredSize().width);
    
    // add slider only to dialog if we are using fs data
    final boolean fs = gui.context.data().fs != null;
    if(fs) {
      p.add(sizeLabel);
      p.add(sizeSlider);
    }

    // create checkbox
    atts = new BaseXCheckBox(MAPATTS, HELPMAPATTS, GUIProp.mapatts, this);
    if(!fs) p.add(atts);

    set(p, BorderLayout.CENTER);
    
    finish(GUIProp.maplayoutloc);
    action(null);
  }

  @Override
  public void action(final String cmd) {
    GUIProp.maplayout = choice.getIndex();
    GUIProp.mapalgo = propalgo.getSelectedIndex();
    GUIProp.mapatts = atts.isSelected();
    final int sizeprp = sizeSlider.value();
    GUIProp.sizep = sizeprp;
    sizeLabel.setText(MAPSIZE + " " + (sizeprp > 45 && sizeprp < 55 ? 
      MAPBOTH : sizeprp < 45 ?  MAPCHILDREN  : MAPFSSIZE));
    gui.notify.layout();
  }

  @Override
  public void close() {
    close(GUIProp.maplayoutloc);
    GUIProp.write();
  }

  @Override
  public void cancel() {
    close();
  }
}
