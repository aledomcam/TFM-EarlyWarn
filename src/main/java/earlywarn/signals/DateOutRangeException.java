package earlywarn.signals;

/**
 * Declaration of new RuntimeException to be thrown when an instance of the class EWarningGeneral have any trouble
 * with the dates at the time of its creation.
 */
public class DateOutRangeException extends RuntimeException {
    /**
     * Basic constructor that calls to super constructor of RuntimeException.
     * @param message Exception message error to be shown.
     */
    public DateOutRangeException(String message) {
        super(message);
    }
}
