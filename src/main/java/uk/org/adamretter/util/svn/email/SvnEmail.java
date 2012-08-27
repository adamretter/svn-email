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
