package org.irmantas.booksstore.exceptions;

public class NotFoundInDbException extends BookStoreException{
    public NotFoundInDbException(String message) {
        super(message);
    }
}
