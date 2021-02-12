package org.irmantas.booksstore.model;

import org.irmantas.booksstore.util.LoadFlux;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BookTest {

    LoadFlux loadFlux = new LoadFlux();

    @Test
    @DisplayName("If successful display total price")
    void getTotalPrice() {
        Book book = loadFlux.getKnownBooksList().get(0);
        assertEquals(book.acquireTotalPrice().doubleValue(), 100.00);
    }

    @Test
    @DisplayName("If successful changes field value")
    void cahangesfieldValue() {
        Book book = loadFlux.getKnownBooksList().get(0);
        assertEquals(book.getAuthor(),"Pirmas autorius");
        book.updateField("author", "Trečias autorius");
        assertEquals(book.getAuthor(),"Trečias autorius");
    }


    @Test
    @DisplayName("If successful validates all validations")
    void testValidation() {
        Book book = loadFlux.getKnownBooksList().get(0);
        assertEquals(book.validateBook(),"OK");
        book.setName("A");
        assertNotEquals(book.validateBook(),"OK");
        book = loadFlux.getKnownBooksList().get(0);
        book.setAuthor("A");
        assertNotEquals(book.validateBook(),"OK");
        book = loadFlux.getKnownBooksList().get(0);
        book.setBarcode("123");
        assertNotEquals(book.validateBook(),"OK");
        book.setBarcode("123456789012a");
        assertNotEquals(book.validateBook(),"OK");
        book.setBarcode("123456789012 ");
        assertNotEquals(book.validateBook(),"OK");
        book.setBarcode(" 123456789012");
        assertNotEquals(book.validateBook(),"OK");
        book.setBarcode("123456789012_");
        assertNotEquals(book.validateBook(),"OK");
        book = loadFlux.getKnownBooksList().get(0);
        book.setQty(-20);
        assertNotEquals(book.validateBook(),"OK");
        book = loadFlux.getKnownBooksList().get(0);
        book.bigDecimalPriceFromDouble(-20.00);
        assertNotEquals(book.validateBook(),"OK");
        book = new Book();
        assertNotEquals(book.validateBook(),"OK");
    }

    @Test
    void validateBook() {
    }

    @Test
    void updateField() {
    }
}