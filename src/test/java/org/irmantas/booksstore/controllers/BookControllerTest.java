package org.irmantas.booksstore.controllers;

import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.repositories.AntiqueBooksRepo;
import org.irmantas.booksstore.repositories.BooksRepo;
import org.irmantas.booksstore.repositories.ScienceJournalRepo;
import org.irmantas.booksstore.util.LoadFlux;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@ActiveProfiles("test")
@Import({BooksRepo.class})
public class BookControllerTest {

    @InjectMocks
    private BookController bookController;

    LoadFlux bookStorage = new LoadFlux();

    @Autowired
    WebTestClient testClient;

    Book book;

    @MockBean
    BooksRepo booksRepo;

    @MockBean
    AntiqueBooksRepo antiqueBooksRepo;

    @MockBean
    ScienceJournalRepo scienceJournalRepo;

    @MockBean
    ControllersUtils controllersUtils;


    @BeforeEach
    void setRepo() {
        BDDMockito.when(booksRepo.findAll()).thenReturn(bookStorage.getBooks());
        BDDMockito.when(booksRepo.findById(1L)).
                thenReturn(Mono.just(bookStorage.getKnownBooksList().get(1)));
        BDDMockito.when(booksRepo.findByBarcodeContaining("321")).
                thenReturn(bookStorage.getBooks().take(5));
        BDDMockito.when(booksRepo.findByBarcode("3210987654321")).
                thenReturn(Mono.just(bookStorage.getKnownBooksList().get(1)));
        BDDMockito.when(booksRepo.findById(0L)).
                thenReturn(Mono.just(bookStorage.getKnownBooksList().get(0)));
        BDDMockito.when(booksRepo.deleteById(0L)).
                thenReturn(bookStorage.getEmtpyBooks());
        BDDMockito.when(booksRepo.save(Matchers.any(Book.class))).
                thenReturn(Mono.just(bookStorage.getKnownBooksList().get(0)));

        BDDMockito.when(controllersUtils.getBookClassesFieldList())
                .thenReturn(Arrays.asList("name", "author"));
    }

    @Test
    @DisplayName("Return  all books if successful")
    public void getBooksBy() {
        testClient
                .get()
                .uri("/books")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Book.class)
                .hasSize(16)
                .value(list -> System.out.println(list));
    }

    @Test
    @DisplayName("Return books  by id  if successfull")
    public void getBookByID() {
        testClient
                .get()
                .uri("/books/1")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Book.class)
                .hasSize(1)
                .value(list -> System.out.println(list));
    }

    @Test
    @DisplayName("Return books by barcode if successful")
    void getByBarcode() {
        testClient
                .get()
                .uri("/books/barcode/3210987654321")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("name").isEqualTo("Antra knyga")
                .consumeWith(v -> System.out.println(v.toString()));
    }

    @Test
    @DisplayName("Return books  by barcode match if successful")
    void getBarcodeMatch() {
        testClient
                .get()
                .uri("/books/barcode/match/321")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Book.class)
                .hasSize(5)
                .value(list -> System.out.println(list));
    }

    @Test
    @DisplayName("Updated book fields if successful")
    void updateValuesByBarcode() {
        testClient
                .put()
                .uri("/books/barcode/3210987654321/name/John")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("name").isEqualTo("John")
                .consumeWith(v -> System.out.println(v.toString()))
        ;
    }

    @Test
    @DisplayName("Post book if successfull and return book with id")
    void postBook() {
        BodyInserter inserter = BodyInserters.fromValue(bookStorage.getKnownBooksList().get(0));
        testClient
                .post()
                .uri("/books")
                .body(inserter)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Book.class)
                .hasSize(1)
                .value(list -> System.out.println(list));
    }

    @Test
    void updateBookById() {
        BodyInserter inserter = BodyInserters.fromValue(bookStorage.getKnownBooksList().get(0));
        testClient
                .put()
                .uri("/books/0")
                .body(inserter)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Book.class)
                .hasSize(1)
                .value(list -> System.out.println(list));
    }

    @Test
    void deleteBook() {
        testClient
                .delete()
                .uri("/books/0")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .equals("Entity with id 0 deleted");
    }

}
