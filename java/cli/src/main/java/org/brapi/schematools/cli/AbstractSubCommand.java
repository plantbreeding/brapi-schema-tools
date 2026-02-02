package org.brapi.schematools.cli;

import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public abstract class AbstractSubCommand implements Runnable {

    private PrintWriter err ;

    @CommandLine.Option(names = {"-x", "--throwExceptionOnFail"}, description = "Throw an exception on failure. False by default, if set to True if an exception is thrown when validation or generation fails.")
    private boolean throwExceptionOnFail = false;

    @CommandLine.Option(names = {"-t", "--stackTrace"}, description = "If an error is recorded output the stack trace.")
    private boolean stackTrace = false;

    @Override
    public final void run() {
        try {
            err = new PrintWriter(System.err) ;

            execute() ;
        } catch (Exception exception) {
            handleException(exception);
        } finally {
            err.close();
        }
    }

    protected abstract void execute() throws IOException;

    protected void handleException(Exception exception) {
        printException(exception) ;

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(exception.getMessage(), exception) ;
        }
    }

    protected void handleError(String message) {
        err.println(message);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message) ;
        }
    }

    protected void printStackTrace(Exception exception) {
        if (stackTrace) {
            exception.printStackTrace(err);
        }
    }

    protected void printException(Exception exception) {
        String message = String.format("%s: %s", exception.getClass().getSimpleName(), exception.getMessage()) ;
        err.println(message);

        printStackTrace(exception) ;
    }

    protected void printError(String errorMessage) {
        err.println(errorMessage);
    }

    protected void printErrors(String message, Collection<Response.Error> allErrors) {
        printError(message);

        allErrors.forEach(this::printError);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, allErrors) ;
        }
    }

    protected void printError(Response.Error error) {
        switch (error.getType()) {

            case VALIDATION -> {
                err.print("Validation Error :");
            }
            case PERMISSION, OTHER -> {
                err.print("Error :");
            }
        }
        err.print('\t');

        err.println(error.getMessage());
    }

    public void handleFail(Response<?> response) {
        handleError(response.getMessagesCombined(", ")) ;
    }
}
