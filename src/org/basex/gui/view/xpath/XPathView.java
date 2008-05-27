package org.basex.gui.view.xpath;

import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.JCheckBox;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.proc.Optimize;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.view.View;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author ...
 */
public final class XPathView extends View {
  /** Database Context. */
  public static Context context = new Context();
  /** Input field for XPath queries. */
  final BaseXTextField input;
  /** Header string. */
  final BaseXLabel header;
  /** Button box. */
  final BaseXBack back;
  /** BasicComboPopup Menu. */
  public BasicComboPopup pop;
  /** StringList with all entries. */
  public StringList all = new StringList();
  /** JComboBox. */
  public JComboBox box;
  /** String for temporary input. */
  public String tmpIn;
  /** Int value to count slashes. */
  public int slashC = 0;
  /** Boolean value if BasicComboPopup is initialized. */
  public boolean popInit = false;
  /** Checkbox if popup will be shown. */
  public JCheckBox checkPop = new JCheckBox("Helpmode", false);

  /**
   * Default Constructor.
   */
  public XPathView() {
    super(null);

    back = new BaseXBack(FILL.NONE);
    back.setLayout(new BorderLayout());
    header = new BaseXLabel(GUIConstants.XPATHVIEW, 10);
    back.add(header, BorderLayout.NORTH);
    input = new BaseXTextField(null);

    input.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        int c = e.getKeyCode();
        if(checkPop.isSelected()) {
          if(all.size == 0) {
            String[] test = keys(GUI.context.data());
            for(int i = 1; i < test.length; i++) {
              all.add(test[i]);
            }
            String[] cmdList = { "ancestor-or-self::", "ancestor::",
                "attribute::", "child::", "comment()", "descendant-or-self::",
                "following-sibling::", "following::", "namespace::", "node()",
                "parent::", "preceding-sibling::", "preceding::",
                "processing-instruction()", "self::", "text()" };
            for(int j = 0; j < test.length; j++) {
              all.add(cmdList[j]);
            }
          }
          if(c == KeyEvent.VK_SLASH) {
            slashC++;
            showPopAll();
          } else if(c == KeyEvent.VK_DELETE || c == KeyEvent.VK_BACK_SPACE) {
            if(input.getText().length() == 0) {
              slashC = 0;
              tmpIn = "";
              pop.hide();
            }
          } else if(c == KeyEvent.VK_UP || c == KeyEvent.VK_DOWN
              || c == KeyEvent.VK_RIGHT || c == KeyEvent.VK_LEFT) return;
          else {
            if(popInit) {
              if(tmpIn.length() == 0) {
                pop.hide();
              } else {
                slashC = 0;
                showSpecPop();
              }
            }
          }
        }
        if(c == KeyEvent.VK_ESCAPE || c == KeyEvent.VK_ENTER) return;
        final String query = input.getText();
        GUI.get().execute(Commands.XPATH, query);
      }
    });

    setBorder(10, 10, 10, 10);
    setLayout(new BorderLayout(0, 4));
    checkPop.setContentAreaFilled(false);
    back.add(checkPop, BorderLayout.CENTER);
    back.add(input, BorderLayout.SOUTH);

    add(back, BorderLayout.NORTH);

    refreshLayout();
  }

  /**
   * Shows the BasicComboPopup with all Entries.
   */
  public void showPopAll() {
    tmpIn = input.getText();
    box = new JComboBox(all.finish());
    popInit = true;
    box.setSelectedItem(null);
    box.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {        
        if(e.getModifiers() == 16) {
          input.setText(tmpIn + box.getSelectedItem());
          pop.hide();
        }
      }
    });
    if(slashC <= 2) {
      pop = new BasicComboPopup(box);
      pop.show(input, 0, input.getHeight());
    } else {
      pop.hide();
    }
  }

  /**
   * Shows the special BasicComboPopup with correct entries only.
   */
  public void showSpecPop() {
    pop.hide();
    box.removeAllItems();
    String tmp = input.getText().substring(tmpIn.length());
    for(int i = 0; i < all.finish().length; i++) {
      if(all.finish()[i].startsWith(tmp)) {
        box.addItem(all.finish()[i]);
      }
    }
    if(box.getComponentCount() != 0) {
      pop = new BasicComboPopup(box);
      pop.show(input, 0, input.getHeight());
    }
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshFocus() {
    repaint();
    // TODO Auto-generated method stub

  }

  @Override
  protected void refreshInit() {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    header.setForeground(COLORS[16]);
  }

  @Override
  protected void refreshMark() {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshUpdate() {
  // TODO Auto-generated method stub

  }

  /**
   * Returns a string array with all distinct keys
   * and the keys of the specified set.
   * @param data data reference
   * @return key array
   */
  String[] keys(final Data data) {
    if(!data.tags.stats()) Optimize.stats(data);

    final StringList sl = new StringList();
    sl.add("");
    for(int i = 1; i <= data.tags.size(); i++) {
      if(data.tags.counter(i) == 0) continue;
      sl.add(Token.string(data.tags.key(i)));
    }
    for(int i = 1; i <= data.atts.size(); i++) {
      if(data.atts.counter(i) == 0) continue;
      sl.add("@" + Token.string(data.atts.key(i)));
    }
    final String[] vals = sl.finish();
    Arrays.sort(vals);
    vals[0] = "(" + (vals.length - 1) + " entries)";
    return vals;
  }
}
