package org.irmantas.booksstore.controllers;

import org.irmantas.booksstore.repositories.BooksRepo;
import org.irmantas.booksstore.util.InfrastructureConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = InfrastructureConfiguration.class)
class BookControllerTest {

    @Autowired
    BooksRepo booksRepo;



    @Test
    void getAllBooks() {
        StepVerifier.create(booksRepo.findAll())
                .expectNextCount(16)
                .verifyComplete();
    }

    @Test
    void getBooksById() {
    }

    @Test
    void getBooksByBarcode() {
    }

    @Test
    void updateValuesByBarcode() {
    }

    @Test
    void getBarcodeMatch() {
    }

    @Test
    void getNameMatch() {
    }

    @Test
    void postBook() {
    }

    @Test
    void updateBookById() {
    }

    @Test
    void deleteBook() {
    }
}