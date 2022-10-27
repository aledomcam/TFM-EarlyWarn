package earlywarn.signals;

/**
 * Declaration of new RuntimeException to be thrown when an instance of the class EWarningGeneral have any trouble
 * finding the desired countries to be studied.
 */
public class CountryUndefinedException extends RuntimeException {
    /**
     * Basic constructor that calls to super constructor of RuntimeException.
     * @param message Exception message error to be shown.
     */
    public CountryUndefinedException(String message) {
        super(message);
    }
}
