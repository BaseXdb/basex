package org.deepfs.util;

import java.io.File;

/**
 * Produces tree(1)-like output of a directory hierarchy.
 * 
 * @author Alexander Holupirek for group 42, XML & DB, Winter Term 2009
 */
public final class TreePrinter extends FSTraversal {

    /** Maximal depth of indentation. */
    private static final int MAX_DEPTH = 64;
    /** Indentation strings for each level. */
    private String[] indentStrings = new String[MAX_DEPTH];
    
    /** 
     * Prints indentation prefix.
     * 
     * For each level a separate indentation string is stored in
     * {@link #indentStrings}.  This methods prints an indentation
     * string by concatenating all prior indent strings.
     * @param level of indentation 
     */
    private void printIndent(final int level) {
        for (int i = 0; i < level; i++) System.out.print(indentStrings[i]);
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
    void preDirectoryVisit(final File d) {
        printFileNode(d);
    }
    
    /**
     * Nothing is done when a directory is left.  This method contains no code.
     */
    @Override
    void postDirectoryVisit(final File d) { 
        /* NOT USED. */ 
    }

    /**
     * Prints a line for tree(1)-like output of a regular file.
     * The line contains an indentation prefix and the name of the file.
     * It is printed whenever a regular file is visited during a filesystem
     * traversal.
     */
    @Override
    void regularFileVisit(final File f) {
        printFileNode(f);
    }
    
    /**
     * Prints a line for tree(1)-like output of a symbolic link.
     * The line contains an indentation prefix and the name of the link.
     * It is printed whenever a symbolic link is visited during a filesystem
     * traversal. The traversal will not follow the link.
     */
    @Override
    void symLinkVisit(final File f) {
        printFileNode(f);
    }
        
    
    /**
     * Prints absolute path of directory to be visited subsequently.
     * @param d directory the traversal starts from
     */
    @Override
    void preTraversalVisit(final File d) {
        System.out.println(d.getAbsolutePath());    
    }
    
    /**
     * Nothing special is done when the traversal is finished.
     * This method contains no code.
     */
    @Override
    void postTraversalVisit(final File d) {
        /* NOT_USED */
    }

}
