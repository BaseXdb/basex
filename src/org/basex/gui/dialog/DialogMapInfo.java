package org.basex.gui.dialog;

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.ViewRect;

/**
 * Dialog window for additional TreeMap information.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogMapInfo extends Dialog {
  /** Format for decimal numbers. */
  DecimalFormat f = new DecimalFormat("#0.00"); 
  /** New Dimensions. */
  private final BaseXLabel ndim = new BaseXLabel(" ");
  /** Old Dimensions. */
  private final BaseXLabel odim = new BaseXLabel(" ");
  /** New Number of Nodes. */
  private final BaseXLabel nsize = new BaseXLabel(" ");
  /** Old Number of Nodes. */
  private final BaseXLabel osize = new BaseXLabel(" ");
  /** New Number of Nodes. */
  private final BaseXLabel naar = new BaseXLabel(" ");
  /** Old Aspect ratio. */
  private final BaseXLabel oaar = new BaseXLabel(" ");
  /** Execution time. */
  private final BaseXLabel ntime = new BaseXLabel(" ");
  /** Execution time. */
  private final BaseXLabel otime = new BaseXLabel(" ");
  /** Last time. */
  private String timeo;
  /** Last number of nodes. */
  private int nno;
  /** Last aspect ratio. */
  private double aaro;
  /** Last rectangle. */
  private ViewRect recto;
  
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapInfo(final GUI main) {
    super(main, "Map Information", false);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(5, 3, 5, 0));
    p.add(new BaseXLabel(""));
    p.add(new BaseXLabel("New Map      ", true, true));
    p.add(new BaseXLabel("Old Map      ", true, true));
    p.add(new BaseXLabel("Size: ", true, true));
    p.add(ndim);
    p.add(odim);
    p.add(new BaseXLabel("Nodes painted: ", true, true));
    p.add(nsize);
    p.add(osize);
    p.add(new BaseXLabel("Average aspect ratio: ", true, true));
    p.add(naar);
    p.add(oaar);
    p.add(new BaseXLabel("Execution time: ", true, true));
    p.add(ntime);
    p.add(otime);

    //p.add(new BaseXLabel("Distance change: ", true, true));
    //p.add(dist);
    //p.add(new BaseXLabel(""));

    set(p, BorderLayout.NORTH);
    setResizable(true);
    finish(null);
  }

  /**
   * sets the table values.
   * @param nn number of nodes
   * @param rect main rectangle
   * @param aar average aspect ratio
   * @param time execution time
   */
  public void setValues(final int nn, final ViewRect rect, final double aar,
      final String time) {

    final int nw = rect != null ? rect.w : 0;
    final int nh = rect != null ? rect.h : 0;
    final int ow = recto != null ? recto.w : 0;
    final int oh = recto != null ? recto.h : 0;
    ndim.setText(nw + " x " + nh);
    odim.setText(ow + " x " + oh);
    nsize.setText(Integer.toString(nn));
    osize.setText(Integer.toString(nno));
    naar.setText(f.format(aar));
    oaar.setText(f.format(aaro));
    ntime.setText(time);
    otime.setText(timeo);
    timeo = time;
    recto = rect;
    aaro = aar;
    nno = nn;
  }
}
