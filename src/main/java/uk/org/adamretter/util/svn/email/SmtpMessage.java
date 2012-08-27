package uk.org.adamretter.util.svn.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.apache.log4j.Logger;

/**
 * Models and sends an SMTP Message via an SMTP Server
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class SmtpMessage {
    
    private final static Logger LOG = Logger.getLogger(SmtpMessage.class);
    
    private final static String SHORT_DAY_NAMES[] = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    private final static String SHORT_MONTH_NAMES[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    
    private OutputStream output = null;
    private PrintWriter pwOutput = null;
    private BufferedReader input = null;
    
    private final String smtpHostname;
    private final int smtpHostPort;
    private final String mailFrom;
    private final List<String> rcptsTo;
    private final Socket smtpSocket;
    
    private boolean prepared = false;
    
    
    /**
     * @param smtpHostname The SMTP Server Hostname
     * @param smtpHostPort The SMTP Server Port
     * @param mailFrom The senders email address
     * @param rcptTo The recipients email addresses
     */
    public SmtpMessage(final String smtpHostname, final int smtpHostPort, final String mailFrom, final List<String> rcptsTo) {
        this.smtpHostname = smtpHostname;
        this.smtpHostPort = smtpHostPort;
        this.mailFrom = mailFrom;
        this.rcptsTo = rcptsTo;
        
        smtpSocket = new Socket();
    }
    
    /**
     * Prepares an SMTP Message by setting up the transport with the SMTP Server
     * and sending appropriate SMTP Message Headers
     * 
     * @param subject The Subject of the email message
     * 
     * @return OutputStream An OutputStream that is ready to receive the content of the message
     */
    public OutputStream prepareForContent(final String subject) throws IOException {
        prepared = false;
        
        smtpSocket.connect(new InetSocketAddress(smtpHostname, smtpHostPort));
        output = smtpSocket.getOutputStream();
        pwOutput = new PrintWriter(new OutputStreamWriter(output));
        input = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));
	
        String smtpResult = null;
        
        //First line sent to us from the SMTP server should be "220 blah blah", 220 indicates okay
        smtpResult = input.readLine();
        if(!smtpResult.substring(0, 3).toString().equals("220")) {
            throw new IOException("Error - SMTP Server not ready!. SMTP Result: " + smtpResult);
        }
				
        //Say "HELO"
        pwOutput.println("HELO " + InetAddress.getLocalHost().getHostName());
        pwOutput.flush();
				
        //get "HELLO" response, should be "250 blah blah"
        smtpResult = input.readLine();
        if(!smtpResult.substring(0, 3).toString().equals("250")) {
            throw new IOException("Error - SMTP HELO Failed: " + smtpResult);
        }
			
        //Send "MAIL FROM:"
        pwOutput.println("MAIL FROM: " + mailFrom);
        pwOutput.flush();
				
        //Get "MAIL FROM:" response
        smtpResult = input.readLine();
        if(!smtpResult.substring(0, 3).toString().equals("250")) {
            throw new IOException("Error - SMTP MAIL FROM failed: " + smtpResult);
        }
			
        //RCPT TO should be issued for each to, cc and bcc recipient
        for(final String rcptTo : rcptsTo) {
            pwOutput.println("RCPT TO: " + rcptTo);
            pwOutput.flush();
        }

        //Get "RCPT TO:" response
        smtpResult = input.readLine();
        if(!smtpResult.substring(0, 3).toString().equals("250")) {
            throw new IOException("Error - SMTP RCPT TO failed: " + smtpResult);
        }
				
        //SEND "DATA" 
        pwOutput.println("DATA");
        pwOutput.flush();

        //Get "DATA" response, should be "354 blah blah"
        smtpResult = input.readLine();
        if(!smtpResult.substring(0, 3).toString().equals("354")) {
            throw new IOException("Error - SMTP DATA failed: " + smtpResult);
        }

        //add general headers to the message
        writeHeaders(pwOutput, mailFrom, rcptsTo, subject);
        
        //we have now prepared the message
        prepared = true;
        LOG.info("SMTPMessage prepared for " + rcptsTo + " from " + mailFrom + " subject " + subject);
        
        //return stream for message content
        return output;
    }
    
    /**
     * Sends the message
     * 
     * Note - prepareForContent() should be called first and then a message body should be written
     * before calling this function
     * 
     */
    public void send() throws IOException {   
        
        try {
            if(!prepared) {
                throw new IOException("SMTPMessage must be prepared before it can be sent. Call prepareForContent() first!");
            }

            //end the message, <cr><lf>.<cr><lf>
            pwOutput.println();
            pwOutput.println(".");
            pwOutput.println();
            pwOutput.flush();

            //Get end message response, should be "250 blah blah"
            final String smtpResult = input.readLine();
            if(!smtpResult.substring(0, 3).toString().equals("250")) {
                throw new IOException("Error - Message not accepted: " + smtpResult);
            }
            
            LOG.info("Message Sent");
            
        } finally {
            output.close();
            input.close();
            smtpSocket.close();
        }
    }
    
    /**
     * Writes out the SMTP Message Headers
     * 
     * @param pwOutput The output destination
     * @param mailFrom The from email address
     * @param rcptTo The to email address
     * @param subject The email subject
     */
    private void writeHeaders(final PrintWriter pwOutput, final String mailFrom, final List<String> rcptsTo, final String subject) {
        pwOutput.println("From: " + mailFrom);
        for(final String rcptTo : rcptsTo)
        {
            pwOutput.println("To: " + rcptTo);
        }
        pwOutput.println("Date: " + getDateRFC822());
        pwOutput.println("Subject: " + subject);
        pwOutput.println("X-Mailer: Landmark SVNEmail 1.0");
        pwOutput.println(); //separate headers and body
        pwOutput.flush();
    }
    
    private final String padZeroTwoString(final String str) {
        return str.length() == 1 ? "0" + str : str;
    }
    
    /**
     * Gets date and time in a suitable format for RFC822
     * 
     * @return RFC822 compliant date and time
     */
    private String getDateRFC822() {
        final StringBuilder dateString = new StringBuilder();
        
        final Calendar rightNow = Calendar.getInstance();
        
        //Day name
        dateString.append(SHORT_DAY_NAMES[rightNow.get(Calendar.DAY_OF_WEEK) - 1]);
        dateString.append(", ");

        //Day number
        dateString.append(rightNow.get(Calendar.DAY_OF_MONTH));
        dateString.append(" ");
        
        //Month
        dateString.append(SHORT_MONTH_NAMES[rightNow.get(Calendar.MONTH)]);
        dateString.append(" ");

        //Year
        dateString.append(rightNow.get(Calendar.YEAR));
        dateString.append(" ");

        //Time
        final String tHour = padZeroTwoString(Integer.toString(rightNow.get(Calendar.HOUR_OF_DAY)));
        final String tMinute = padZeroTwoString(Integer.toString(rightNow.get(Calendar.MINUTE)));
        final String tSecond = padZeroTwoString(Integer.toString(rightNow.get(Calendar.SECOND)));

        dateString.append(tHour);
        dateString.append(":");
        dateString.append(tMinute);
        dateString.append(":");
        dateString.append(tSecond);
        dateString.append(" ");

        //TimeZone Correction
        final TimeZone thisTimeZoen = TimeZone.getDefault();
        int timeZoneOffset = thisTimeZoen.getOffset(rightNow.get(Calendar.DATE)); //get timezone offset in milliseconds
        timeZoneOffset = (timeZoneOffset / 1000); //convert to seconds
        timeZoneOffset = (timeZoneOffset / 60); //convert to minutes

        //Sign
        final String tzSign;
        if(timeZoneOffset > 1) {
            tzSign = "+";
        } else {
            tzSign = "-";
        }

        final String tzHours;
        final String tzMinutes;
        
        //Calc Hours and Minutes?
        if(timeZoneOffset >= 60 || timeZoneOffset <= -60) {
            //Minutes and Hours
            tzHours = padZeroTwoString(Integer.toString(timeZoneOffset / 60)); //hours
            tzMinutes = padZeroTwoString(Integer.toString(timeZoneOffset % 60)); //minutes
        } else {
            //Just Minutes
            tzHours = "00";
            tzMinutes = padZeroTwoString(Integer.toString(timeZoneOffset));
        }

        dateString.append(tzSign);
        dateString.append(tzHours);
        dateString.append(tzMinutes);

        return dateString.toString();
    }
}
