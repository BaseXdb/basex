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
  private BaseXListChooser choice;  
  /** Layout slider. */
  private BaseXSlider prop;
  /** Layout slider. */
  private BaseXSlider sizeSlider;
  /** Simple layout. */
  private BaseXCheckBox simple;
  /** Show attributes. */
  private BaseXCheckBox atts;
  /** Select Layoutalgo. */
  BaseXCombo propalgo;
  /** Temporary reference to the old map layout. */
  private final BaseXLabel propLabel;
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
    p.setLayout(new TableLayout(9, 1, 0, 5));

    // create list
    choice = new BaseXListChooser(this, MAPLAYOUTCHOICE, HELPMAPLAYOUT);
    choice.setSize(500, 300);
    choice.setIndex(GUIProp.maplayout);
    p.add(choice);

    // create checkbox
    simple = new BaseXCheckBox(MAPSIMPLE, HELPMAPSIMPLE,
        GUIProp.mapsimple, 0, this);
    p.add(simple);
    atts = new BaseXCheckBox(MAPATTS, HELPMAPATTS, GUIProp.mapatts, this);
    if(gui.context.data().fs != null) {
      atts.setEnabled(false);
    }
    p.add(atts);
    
    // create drop down
    BaseXBack tmpback = new BaseXBack();
    tmpback.setLayout(new TableLayout(1, 2, 3, 5));
    BaseXLabel label = new BaseXLabel(MAPPROPALGO);
    tmpback.add(label);
    propalgo = new BaseXCombo(new String[] {"SplitLayout", 
        "SliceAndDice Layout", "SquarifiedLayout", "StripLayout"}, HELPMODE, 
        false);
    propalgo.setSelectedIndex(GUIProp.mapalgo);

    propalgo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final int s = propalgo.getSelectedIndex();
        if(s == GUIProp.mapalgo || !propalgo.isEnabled()) return;
        action(null);
      }
    });
    tmpback.add(propalgo);
    if(GUIProp.algchanger) {
      p.add(tmpback);
    }
    // create slider
    propLabel = new BaseXLabel(MAPPROP);
    p.add(propLabel);
    prop = new BaseXSlider(gui, -234, 248, GUIProp.mapprop, HELPMAPALIGN, this);
    BaseXLayout.setWidth(prop, p.getPreferredSize().width);
    p.add(prop);
    set(p, BorderLayout.CENTER);
    sizeLabel = new BaseXLabel(MAPSIZE);
    sizeSlider = new BaseXSlider(gui, 0, 100, GUIProp.sizep, HELPMAPSIZE, this);
    BaseXLayout.setWidth(sizeSlider, p.getPreferredSize().width);
    
    // add slider only to dialog if we are using fs data
    if(gui.context.data().fs != null && GUIProp.fsslider) {
      p.add(sizeLabel);
      p.add(sizeSlider);
    }
    set(p, BorderLayout.CENTER);
    
    finish(GUIProp.maplayoutloc);
    action(null);
  }

  @Override
  public void action(final String cmd) {
    GUIProp.maplayout = choice.getIndex();
    final int prp = prop.value();
    GUIProp.mapprop = prp;
    GUIProp.mapalgo = propalgo.getSelectedIndex();
    GUIProp.mapsimple = simple.isSelected();
    GUIProp.mapatts = atts.isSelected();
    propLabel.setText(MAPPROP + " " + (prp > 2 && prp < 7 ? "Centered" :
      prp < 3 ? "Vertical" : "Horizontal"));
    final int sizeprp = sizeSlider.value();
    GUIProp.sizep = sizeprp;
    sizeLabel.setText(MAPSIZE + " " + (sizeprp > 45 && sizeprp < 55 ? 
      MAPBOTH :
      sizeprp < 45 ?  MAPCHILDREN  : MAPFSSIZE));
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
