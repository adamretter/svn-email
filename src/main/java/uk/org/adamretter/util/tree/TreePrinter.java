/**
 * Copyright (c) 2012, Adam Retter <adam.retter@googlemail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Adam Retter Consulting nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.org.adamretter.util.tree;

import java.io.PrintWriter;
import java.util.Iterator;

/**
 * Utility Class for Printing aspects of a Tree
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class TreePrinter {
    
    /**
     * Prints the entire Tree down from the node provided
     * 
     * @out The output destination for printing the tree
     * @root The start node of the Tree
     */
    public static void printTree(final PrintWriter out, final TreeNode root) {
        for(final Iterator<TreeNode> itChild = root.iterateChildren(); itChild.hasNext();) {
            final TreeNode child = itChild.next();
            recursivePrinter(out, child, 0);
        }
        
        //flush the writer
        out.flush();
    }
    
    /**
     * Used by printTree to recursively print the tree
     * 
     * @param out The output destination for printing the tree
     * @param node The specific node to print
     * @param depth The current depth in the tree
     */
    private static void recursivePrinter(final PrintWriter out, final TreeNode node, final int depth) {
        String indent = new String();
        for(int i = 0; i < depth; i++) {
            indent += '\t';
        }
        
        out.println(indent + node.getName());
        
        for(final Iterator<TreeNode> itChild = node.iterateChildren(); itChild.hasNext();) {
             recursivePrinter(out, itChild.next(), depth+1);
        }
    }
    
    /**
     * Print the path of a Node in the tree
     * 
     * @param out The output destination for printing the path to
     * @param node The node to print the path for
     */
    public static void printNodePath(final PrintWriter out, final TreeNode node) {
        final StringBuilder builder = new StringBuilder(TreeNode.PATH_SEPARATOR + node.getName());
        
        TreeNode p = node;
        while((p = p.getParent()) != null) {
            if(p.getName() != null) {
                builder.insert(0, TreeNode.PATH_SEPARATOR + p.getName());
            }
        }
        
        out.print(builder.toString());
        out.flush();
    }
}
