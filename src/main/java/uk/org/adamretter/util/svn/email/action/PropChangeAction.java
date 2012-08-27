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
import java.util.List;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import uk.org.adamretter.util.svn.email.SmtpMessage;

/**
 * Property Change Interrogation
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class PropChangeAction extends Action {
    
    private final static Logger LOG = Logger.getLogger(PropChangeAction.class);
    
    private int revision;
    private String author;
    private String propName;
    private char propAction;
    private final List<String> oldPropValue;
    
    public PropChangeAction(final String repository, final String args[], final List<String> oldPropValue) throws InvalidArgumentsException, IOException {
        super(repository, args);
        this.oldPropValue = oldPropValue;
    }

    @Override
    public ARGS[] getExpectedArgs() {
        return new ARGS[] { Action.ARGS.REPOS, Action.ARGS.REVISION, Action.ARGS.AUTHOR, Action.ARGS.PROPNAME, Action.ARGS.PROPACTION  };
    }
    
    
    @Override
    public void setupArgs(final String[] args) throws InvalidArgumentsException {
        if(args.length < 4) {
            throw new InvalidArgumentsException("For commit, both REPOS and REVISION are required");
        }
        
        final String rev = args[0];
        try {
            this.revision = Integer.parseInt(rev);
        } catch(final NumberFormatException nfe) {
            throw new InvalidArgumentsException("Revision is invalid: " + rev, nfe);
        }
        
        this.author = args[1];
        if(author == null || author.length() == 0) {
            throw new InvalidArgumentsException("Author is invalid");
        }
        
        this.propName = args[2];
        if(propName == null || propName.length() == 0) {
            throw new InvalidArgumentsException("Propname is invalid");
        }
        
        if(args[3] == null || args[3].length() != 1) {
            throw new InvalidArgumentsException("Propaction is invalid");
        }
        this.propAction = args[3].charAt(0);
    }

    /*
    @Override
    public String getSubject()
    {
        return "[SVN propchange] " + String.valueOf(revision) + ": " + svnRepositoryURL.getPath();   
    }
    */

    @Override
    public void execute(final SmtpMessage message) throws IOException {
        final SVNWCClient svnWCClient = svnClientManager.getWCClient();
        
        try {
            final SVNPropertyData svnPropData = svnWCClient.doGetProperty(svnRepositoryURL, propName, SVNRevision.create(revision), SVNRevision.create(revision));
            
            LOG.info("PROPCHANGE: getName()=" + svnPropData.getName() + " getValue()=" + svnPropData.getValue());
            System.out.println(svnPropData.getName() + ":" + svnPropData.getValue());
            
            //TODO write the prop change to the OutputStream
            
            //TODO dont forget old prop value
        } catch(final SVNException se) {
            throw new IOException("SVN communication error: " + se.getMessage(), se);
        }
    }   
}
