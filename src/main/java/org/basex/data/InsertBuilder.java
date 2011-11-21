package org.basex.data;

import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.IO;
import org.basex.util.Atts;
import org.basex.util.hash.TokenMap;
import org.basex.util.list.IntList;

/**
 * This class modifies an existing database instance.
 *
 * @author Dave Glick, Data Research and Analysis Corp.
 */
public final class InsertBuilder extends Builder {
  /** The target insert pre */
  private final int ipre;
  /** The target insert parent pre */
  private final int ipar;
  /** The target database */
  private final Data data;
  
  /** The optimal buffer size */
  private final int buf = IO.BLOCKSIZE >> IO.NODEPOWER;
  
  /** Cache of document pres */
  private IntList docPres;
  /** Flag for a dummy document */
  private boolean dummy;
  /** Cache of namespace scopes */
  private TokenMap nsScope;
  /** All current pres */
  private IntList preStack;
  /** Cache of new namespace nodes */
  private Set<NSNode> newNodes;
  /** Cache of pres with ns flag */
  private IntList flagPres;
  /** Cache of insertion sizes for delayed set */
  private Stack<int[]> sizeStack; //[0] = pre, [1] = size

  /** The current insert pre */
  private int pre;
  /** The current insert distance */
  private int dis;
  /** The current insert parent */
  private int par;
 
  /**
   * @param tpre the target pre
   * @param tpar the target parent
   * @param d the target database
   * @param parse the source parser
   */
  public InsertBuilder(final int tpre, final int tpar, Data d, Parser parse) {
    super(d.meta.name, parse, d.meta.prop);   // We don't really need the name or prop for this Builder, but have to provide ones anyway
    ipre = tpre;
    ipar = tpar;
    data = d;
  }

  @Override
  public Data build() throws IOException {
    //Create a dummy meta for the Builder base - it assumes that the Builder is flushing the buffer
    //after every update and relies on an accurate meta.size internally
    meta = new MetaData(prop);
    meta.name = name;
    meta.textindex = false;
    meta.attrindex = false;
    meta.ftindex = false;
    meta.chop = data.meta.chop;
    
    //Put the target database in update mode
    data.meta.update();
    
    docPres = new IntList();
    dummy = false;
    nsScope = new TokenMap();
    preStack = new IntList();
    newNodes = new HashSet<NSNode>();
    flagPres = new IntList();  
    sizeStack = new Stack<int[]>();    
            
    // Resize the buffer to a reasonable value since we don't know how big the stream is
    data.buffer(buf);
    
    // Find all namespaces in scope to avoid duplicate declarations
    NSNode n = data.ns.current;
    do {
      for(int i = 0; i < n.vals.length; i += 2)
        nsScope.add(data.ns.pref(n.vals[i]), data.ns.uri(n.vals[i + 1]));
      final int pos = n.fnd(ipar);
      if(pos < 0) break;
      n = n.ch[pos];
    } while(n.pre <= ipar && ipar < n.pre + data.size(n.pre, Data.ELEM));
    
    // Store the root namespace so it can be reset later
    NSNode t = data.ns.current;
    
    // find nearest namespace node on the ancestor axis of the insert
    // location. possible candidates for this node are collected and
    // the match with the highest pre value between ancestors and candidates
    // is determined.
    // collect possible candidates for namespace root
    final List<NSNode> cand = new LinkedList<NSNode>();
    NSNode cn = data.ns.root;
    cand.add(cn);
    for(int cI; (cI = cn.fnd(ipar)) > -1;) {
      // add candidate to stack
      cn = cn.ch[cI];
      cand.add(0, cn);
    }

    cn = data.ns.root;
    if(cand.size() > 1) {
      // compare candidates to ancestors of par
      int ancPre = ipar;
      // take first candidate from stack
      NSNode curr = cand.remove(0);
      while(ancPre > -1 && cn == data.ns.root) {
        // this is the new root
        if(curr.pre == ancPre) cn = curr;
        // if the current candidate's pre value is lower than the current
        // ancestor of par or par itself we have to look for a potential
        // match for this candidate. therefore we iterate through ancestors
        // till we find one with a lower than or the same pre value as the
        // current candidate.
        else if(curr.pre < ancPre) {
          while((ancPre = data.parent(ancPre, data.kind(ancPre))) > curr.pre);
          if(curr.pre == ancPre) cn = curr;
        }
        // no potential for infinite loop, cause dummy root always a match,
        // in this case ancPre ends iteration
        if(cand.size() > 0) curr = cand.remove(0);
      }
    }
    data.ns.setNearestRoot(cn, ipar);   
    
    // Parse the input
    parse();
    
    //Close any remaining items
    while(preStack.size() != 0) data.ns.close(preStack.pop());
    
    // Reset the root namespace
    data.ns.setRoot(t);

    // Insert any remaining buffer
    if(data.bp != 0) data.insert(ipre + meta.size - 1 - (meta.size - 1) % buf);
    
    // reset buffer to old size
    data.buffer(1);
    
    // Now that all the buffers have been flushed, set sizes for inserted nodes
    while(sizeStack.size() != 0)
    {
      int[] size = sizeStack.pop();
      data.size(size[0], Data.ELEM, size[1]);
    }

    // set ns flags
    for(final int toFlag : flagPres.toArray())
      data.table.write2(toFlag, 1, data.name(toFlag) | 1 << 15);

    // increase size of ancestors
    int p = ipar;
    while(p >= 0) {
      final int k = data.kind(p);
      data.size(p, k, data.size(p, k) + meta.size);
      p = data.parent(p, k);
    }
    data.updateDist(ipre +  meta.size,  meta.size);

    // NSNodes have to be checked for pre value shifts after insert
    data.ns.update(ipre, meta.size, true, newNodes);
    
    // Update document index
    final IntList il = data.docindex.docs();
    int i = il.sortedIndexOf(ipre);
    if(i < 0) i = -i - 1;
    il.insert(i, docPres.toArray());
    il.move(meta.size, i + docPres.size());
    data.docindex.update();

    // delete old empty root node
    if(dummy) data.delete(0);
      
    return data;
  }
  
  /**
   * Updates the current state for each build event
   * @param doc flag to indicate if this is a doc event
   */
  private void update(boolean doc)
  {    
    // Flush the buffer if we're at capacity
    if(meta.size != 0 && meta.size % buf == 0)
      data.insert(ipre + meta.size - buf);
    
    // Update positions
    pre = ipre + meta.size;
    int dpar = doc ?  -1 : getPar();  //The Builder increases the level count for docs before calling overrides, while increase after for everything else
    dis = dpar >= 0 ? meta.size - dpar : ipar >= 0 ? pre - ipar : 0;
    par = dis == 0 ? -1 : pre - dis;
    
    // Close items
    while(preStack.size() != 0 && preStack.peek() > par)
      data.ns.close(preStack.pop());
  }
  
  @Override
  public void close() throws IOException {
    parser.close();
  }

  @Override
  protected void addDoc(byte[] value) throws IOException {
    update(true);

    // If addDoc is first event, check if data is empty, and if so set dummy = true
    if(data.empty() && meta.size == 0)
    {
      dummy = true;
    }
    
    // Cache the document pre
    docPres.add(pre);

    // Add document
    data.doc(pre, 0, value);
    data.meta.ndocs++;
    data.ns.open();
    preStack.push(pre);
    meta.size++;
  }
  
  /**
   * @return a attribute collection of namespaces
   */
  private Atts getNs()
  {
    //This replicates the behavior of Data.ns() using the Builder ns cache
    final Atts as = new Atts();
    final int[] nsp = ns.current.vals;
    for(int n = 0; n < nsp.length; n += 2)
      as.add(ns.pref(nsp[n]), ns.uri(nsp[n + 1]));
    return as;
  }

  @Override
  protected void addElem(final int dist, final int nm, final int asize,
      final int uri, final boolean nf)
      throws IOException {
    update(false);

    // Resolve namespaces
    boolean ne = false;    
    if(nf) {
      final Atts at = getNs();
      for(int a = 0; a < at.size; ++a) {
        // see if prefix has been declared/ is part of current ns scope
        final byte[] old = nsScope.get(at.key[a]);
        if(old == null || !eq(old, at.val[a])) {
          // we have to keep track of all new NSNodes that are added
          // to the Namespace structure, as their pre values must not
          // be updated. I.e. if an NSNode N with pre value 3 existed
          // prior to inserting and two new nodes are inserted at
          // location pre == 3 we have to make sure N and only N gets
          // updated.
          newNodes.add(ns.add(at.key[a], at.val[a], pre));
          ne = true;
        }
      }
    }    
    
    // Add element
    data.ns.open();
    data.elem(dis, data.tagindex.index(tags.key(nm), null, false), asize,
        0, data.ns.uri(tags.key(nm), true), ne);
    preStack.push(pre); 
    meta.size++;
  }

  @Override
  protected void addAttr(int nmi, byte[] value, int dist, int uri)
      throws IOException {
    update(false);
    
    // add attribute
    byte[] nm = atts.key(nmi);
    // check if prefix already in nsScope or not
    final byte[] attPref = pref(nm);
    // check if prefix of attribute has already been declared, otherwise
    // add declaration to parent node
    if(uri != 0 && (nsScope.get(attPref) == null)) {
      data.ns.add(par, preStack.size() == 0 ? -1 : preStack.peek(), attPref, ns.uri(uri));
      // save pre value to set ns flag later for this node. can't be done
      // here as direct table access would interfere with the buffer
      flagPres.add(par);
    }
    data.attr(pre, dist, data.atnindex.index(nm, null, false),
        value, data.ns.uri(nm, false), false);
    meta.size++;
  }

  @Override
  protected void addText(byte[] value, int dist, byte kind)
      throws IOException {
    update(false);
    
    data.text(pre, dist, value, kind);
    meta.size++;
  }

  @Override
  protected void setSize(int spre, int size) throws IOException
  {
    // Store the size in the cache so it can be set once the buffer is flushed 
    sizeStack.push(new int[]{ipre + spre, size});
  }

}
