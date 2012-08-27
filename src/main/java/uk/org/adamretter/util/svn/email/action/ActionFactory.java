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
