package org.basex.gui.view.xpath;

import static org.basex.gui.GUIConstants.*;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;
import org.basex.core.Commands;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.view.View;

/**
 * This class offers a real tree view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author ...
 */
public final class XPathView extends View {
  /** Input field for XPath queries. */
  protected final BaseXTextField input;
  /** Header string. */
  protected final BaseXLabel header;
  /** Button box. */
  protected final BaseXBack back;
  /** BasicComboPopup Menu. */
  public BasicComboPopup pop;
  /** Stringarray with all available Commands. */
  public String[] cmdList = {"ancestor-or-self::", "ancestor::",
      "attribute::", "child::", "comment()", "descendant-or-self::",
      "following-sibling::", "following::", "namespace::", "node()",
      "parent::", "preceding-sibling::", "preceding::",
      "processing-instruction()", "self::", "text()"};
  /** JComboBox. */
  public JComboBox box;
  /** String for slashes. */
  public String slashes;
  /** Boolean value if BasicComboPopup is initialized. */
  public boolean popInit = false;

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
        if(c == KeyEvent.VK_SLASH) {
          showPopAll();
        } else if(c == KeyEvent.VK_DELETE || c == KeyEvent.VK_BACK_SPACE) {
          if(input.getText().length() == 0) {
            slashes = "";
            pop.hide();
          }
          } else if(c == KeyEvent.VK_UP || c == KeyEvent.VK_DOWN
            || c == KeyEvent.VK_RIGHT || c == KeyEvent.VK_LEFT) return;
        else {
          if(popInit) {
            if(slashes.length() == 0) {
              pop.hide();
            } else {
          showSpecPop();
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
    back.add(input, BorderLayout.CENTER);

    add(back, BorderLayout.NORTH);

    refreshLayout();
  }
  
  /**
   * Shows the BasicComboPopup with all Entries.
   */
  public void showPopAll() {
    slashes = input.getText();
    box = new JComboBox(cmdList);
    popInit = true;
    box.setSelectedItem(null);
    //box.addItemListener(new ItemListener() {
      //public void itemStateChanged(final ItemEvent e) { 
        //input.setText(e.getItem().toString());
        //pop.hide();
      //}
    //});
    box.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (box.getSelectedItem() != null) {
        //input.setText(slashes + box.getSelectedItem());
        pop.hide();
        }
      }
    });
    pop = new BasicComboPopup(box);
    pop.show(input, 0, input.getHeight());
  }
  
  /**
   * Shows the special BasicComboPopup with correct entries only.
   */
  public void showSpecPop() {
    pop.hide();
    box.removeAllItems();
    String tmp = input.getText().substring(slashes.length());
    System.out.println("TMP: " + tmp);
    System.out.println("SLASHES: " + slashes.length());
    for (int i = 0; i < cmdList.length; i++) {
     // System.out.println(cmdList[i]);
      if(cmdList[i].startsWith(tmp)) {
        box.addItem(cmdList[i]);
      }
    }
    if(box.getComponentCount() != 0) {
      pop = new BasicComboPopup(box);
      pop.show(input, 0, input.getHeight());
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
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
}
