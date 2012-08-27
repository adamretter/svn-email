package uk.org.adamretter.util.svn.email.action;

/**
 *
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class InvalidArgumentsException extends Exception {

    Action.ARGS expectedArgs[] = null;
    
    public InvalidArgumentsException(final String message) {
        super(message);
    }

    public InvalidArgumentsException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public void setExpectedArgs(final Action.ARGS expectedArgs[]) {
        this.expectedArgs = expectedArgs;
    }

    @Override
    public String getMessage() {
        final StringBuilder builder = new StringBuilder(super.getMessage());
        if(expectedArgs != null) {
            builder.append(" Expected arguments:");
            for(final Action.ARGS args : expectedArgs) {
                builder.append(" ");
                builder.append(args);
            }
        }
        
        return builder.toString();
    }
}
