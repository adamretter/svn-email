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
package uk.org.adamretter.util.svn.email.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Constructs appropriate Actions based on supplied parameters
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class ActionFactory {
    
    /**
     * Gets an Action based on the supplied parameters
     * 
     * @param actionName The name of the Subversion action
     * @param repository The URI of the Subversion Repository
     * @param args Arguments for the Subversion Action
     * 
     * @return The Action to interrogate
     */
    public final static Action getAction(final String actionName, final String repository, final String args[]) throws InvalidArgumentsException, IOException {   
        if(actionName.equals("commit")) {
            return new CommitAction(repository, args);
        } else if(actionName.equals("propchange")) {
            return new PropChangeAction(repository, args, null/*readFromStdIn()*/);
        } else if(actionName.equals("lock") || actionName.equals("unlock")) {   
            return new LockUnlockAction(repository, LockUnlockAction.Type.valueOf(actionName), args, readFromStdIn());
        }
        
        throw new InvalidArgumentsException("Unrecognised Action: " + actionName);
    }
    
    /**
     * Reads lines from the Systems STDIN into a String List
     * 
     * @return String List of lines read from STDIN
     */
    private final static List<String> readFromStdIn() {
        //read modified path from stdin
        final Scanner stdin = new Scanner(System.in);
        List<String> data = null;
        while(stdin.hasNext()) {
            if(data == null) {
                data = new ArrayList<String>();
            }
            data.add(stdin.next());
        }
        
        return data;
    }
}
