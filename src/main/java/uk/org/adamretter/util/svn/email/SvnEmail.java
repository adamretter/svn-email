package uk.org.adamretter.util.svn.email;

import java.io.IOException;
import org.apache.log4j.Logger;
import uk.org.adamretter.util.svn.email.action.Action;
import uk.org.adamretter.util.svn.email.action.ActionFactory;
import uk.org.adamretter.util.svn.email.action.InvalidArgumentsException;

//TODO PropChangeAction is not yet complete

/**
 * Program to hook into Subversion
 * Sends out a summary of each commit by email
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class SvnEmail {

    private final static Logger LOG = Logger.getLogger(SvnEmail.class);
    
    public static void main(String[] args) {  
        try {   
            //Check for config file
            final Config config = Config.getInstance();
            
            //check args
            if(args.length < 1) {
                throw new InvalidArgumentsException("Invalid Arguments");
            }
        
            //prep the parameters and arguments
            final String actionName = args[0];
            final String repos = args[1];
            final String actionArgs[] = new String[args.length - 2];
            System.arraycopy(args, 2, actionArgs, 0, args.length - 2);
            
            //prepate the action
            final Action action = ActionFactory.getAction(actionName, repos, actionArgs);
            
            //setup an email
            final SmtpMessage message = new SmtpMessage(config.getSMTPServer(), config.getSMTPServerPort(), config.getMailFromAddress(), config.getMailToAddresses());
            
            //execute the action
            action.execute(message);
            
            //send the email
            message.send();
        } catch(final IOException ioe) {
            LOG.error(ioe.getMessage(), ioe);
            System.exit(-1);
        } catch(final InvalidArgumentsException iae) {
            System.out.println(iae.getMessage());
            LOG.error(iae.getMessage(), iae);
            System.exit(-1);
        }
    }
}
