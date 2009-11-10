package org.deepfs.util;

import java.io.File;

/**
 * Produces tree(1)-like output of a directory hierarchy.
 * 
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Alexander Holupirek
 */
public final class TreePrinter implements FSTraversal {

    /** Maximal depth of indentation. */
    private static final int MAX_DEPTH = 64;
    /** Maximal depth of indentation. */
    private int level;
    /** Indentation strings for each level. */
    private String[] indentStrings = new String[MAX_DEPTH];
    
    /** 
     * Prints indentation prefix.
     * 
     * For each level a separate indentation string is stored in
     * {@link #indentStrings}.  This methods prints an indentation
     * string by concatenating all prior indent strings.
     * @param depth of indentation 
     */
    private void printIndent(final int depth) {
        for (int i = 0; i < depth; i++) System.out.print(indentStrings[i]);
    }
    
    /**
     * Prints a line for tree(1)-like output of a file node.
     * During a filesystem traversal this method is invoked whenever
     * a file node has to be printed.
     * 
     * It first prints an according indentation string for each level
     * using ({@link #printIndent(int)}.
     * 
     * Secondly it determines if it is the sole child of its parent
     * directory or if it is the last child in the list of children
     * of the parent directory.  In this case it stores the empty 
     * indentation string in {@link #indentStrings} and prints its
     * name prefixed with "`--".
     * 
     * Otherwise it stores "|   " as indentation string for the
     * subsequent file nodes and prints its name prefixed with "|-- ".
     * @param f the currently visited file node 
     */
    private void printFileNode(final File f) {
        printIndent(level);
        File[] ch = f.getParentFile().listFiles();
        if (ch.length == 1 || ch[ch.length - 1].equals(f)) {
            indentStrings[level] = "    ";
            System.out.println("`-- " + f.getName());
        } else {
            indentStrings[level] = "|   ";
            System.out.println("|-- " + f.getName());
        }    
    }
    
    /**
     * Prints a line for tree(1)-like output of a directory.
     * The line contains an indentation prefix and the name of the directory
     * and is printed whenever a directory is entered during a filesystem
     * traversal.
     */
    @Override
    public void preDirectoryVisit(final File d) {
        printFileNode(d);
    }
    
    /**
     * Nothing is done when a directory is left.  This method contains no code.
     */
    @Override
    public void postDirectoryVisit(final File d) { 
        /* NOT USED. */ 
    }

    /**
     * Prints a line for tree(1)-like output of a regular file.
     * The line contains an indentation prefix and the name of the file.
     * It is printed whenever a regular file is visited during a filesystem
     * traversal.
     */
    @Override
    public void regularFileVisit(final File f) {
        printFileNode(f);
    }
    
    /**
     * Prints a line for tree(1)-like output of a symbolic link.
     * The line contains an indentation prefix and the name of the link.
     * It is printed whenever a symbolic link is visited during a filesystem
     * traversal. The traversal will not follow the link.
     */
    @Override
    public void symLinkVisit(final File f) {
        printFileNode(f);
    }
        
    
    /**
     * Prints absolute path of directory to be visited subsequently.
     * @param d directory the traversal starts from
     */
    @Override
    public void preTraversalVisit(final File d) {
        System.out.println(d.getAbsolutePath());    
    }
    
    /**
     * Nothing special is done when the traversal is finished.
     * This method contains no code.
     */
    @Override
    public void postTraversalVisit(final File d) {
        /* NOT_USED */
    }

    @Override
    public void levelUpdate(final int l) {
      level = l;
    }
}
