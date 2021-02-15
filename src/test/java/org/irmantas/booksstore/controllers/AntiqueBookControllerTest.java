package org.irmantas.booksstore.controllers;

import lombok.SneakyThrows;
import org.irmantas.booksstore.exceptions.ApiErrors;
import org.irmantas.booksstore.model.AntiqueBook;
import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.repositories.AntiqueBooksRepo;
import org.irmantas.booksstore.repositories.BooksRepo;
import org.irmantas.booksstore.repositories.ScienceJournalRepo;
import org.irmantas.booksstore.util.LoadFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@ActiveProfiles("test")
@Import({AntiqueBooksRepo.class, ApiErrors.class})
public class AntiqueBookControllerTest {

    @InjectMocks
    private AntiqueBookConroller antiqueBookController;

    LoadFlux bookStorage = new LoadFlux();

    @Autowired
    WebTestClient testClient;

    @Autowired
    ApiErrors apiErrors;

    Book book;

    @MockBean
    BooksRepo booksRepo;

    @MockBean
    AntiqueBooksRepo antiqueBooksRepo;

    @MockBean
    ScienceJournalRepo scienceJournalRepo;

    @MockBean
    ControllersUtils controllersUtils;

    BodyInserter inserter;

    @BeforeEach
    void setRepo() {
        inserter = BodyInserters.fromValue(bookStorage.getKnownAntiqueBooksList().get(0));
        BDDMockito.when(antiqueBooksRepo.findAll()).thenReturn(bookStorage.getAntiqueBooks());
        BDDMockito.when(antiqueBooksRepo.findById(1L)).
                thenReturn(Mono.just(bookStorage.getKnownAntiqueBooksList().get(1)));
        BDDMockito.when(antiqueBooksRepo.findByBarcodeContaining("321")).
                thenReturn(bookStorage.getAntiqueBooks().take(5));
        BDDMockito.when(antiqueBooksRepo.findByBarcode("3210987654321")).
                thenReturn(Mono.just(bookStorage.getKnownAntiqueBooksList().get(1)));
        BDDMockito.when(antiqueBooksRepo.findById(0L)).
                thenReturn(Mono.just(bookStorage.getKnownAntiqueBooksList().get(0)));
        BDDMockito.when(antiqueBooksRepo.deleteById(0L)).
                thenReturn(bookStorage.getEmtpyBooks());
        BDDMockito.given(antiqueBooksRepo.save(ArgumentMatchers.any(AntiqueBook.class))).will(invocationOnMock ->
                invocationOnMock.getArgument(0, AntiqueBook.class).getName().equals("John") ?
                        Mono.just(bookStorage.getModifiedAntiqueBook()) :
                        Mono.just(bookStorage.getKnownAntiqueBooksList().get(0))
        );

        BDDMockito.when(controllersUtils.getBookClassesFieldList())
                .thenReturn(Arrays.asList("name", "author"));
    }

    @Test
    @DisplayName("Return  all books if successful")
    public void getBooksBy() {
        testClient
                .get()
                .uri("/books/antique")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(AntiqueBook.class)
                .hasSize(16)
                .value(System.out::println);
    }

    @Test
    @DisplayName("Return books  by id  if successfull")
    public void getBookByID() {
        testClient
                .get()
                .uri("/books/antique/1")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(AntiqueBook.class)
                .hasSize(1)
                .value(System.out::println);
    }

    @Test
    @DisplayName("Return books by barcode if successful")
    void getByBarcode() {
        testClient
                .get()
                .uri("/books/antique/barcode/3210987654321")
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
                .uri("/books/antique/barcode/match/321")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(AntiqueBook.class)
                .hasSize(5)
                .value(System.out::println);
    }

    @Test
    @DisplayName("Updated book fields if successful")
    void updateValuesByBarcode() {
        testClient
                .put()
                .uri("/books/antique/barcode/3210987654321/name/John")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("author").isEqualTo("John")
                .consumeWith(v -> System.out.println(v.toString()))
        ;
    }

    @Test
    @DisplayName("Post book if successfull and return book with id")
    void postBook() {
        inserter = BodyInserters.fromValue(bookStorage.getKnownAntiqueBooksList().get(0));
        testClient
                .post()
                .uri("/books/antique")
                .body(inserter)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(AntiqueBook.class)
                .hasSize(1)
                .value(System.out::println);
    }

    @Test
    @DisplayName("Update book if successfull and return book with id")
    void updateBookById() {
        
        testClient
                .put()
                .uri("/books/antique/0")
                .body(inserter)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(AntiqueBook.class)
                .hasSize(1)
                .value(System.out::println);
    }

    @Test
    @DisplayName("Delete book if successfull and return result confirmation")
    void deleteBook() {
        testClient
                .delete()
                .uri("/books/antique/0")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().consumeWith(v-> testReturnString(new String(v.getResponseBody(), StandardCharsets.UTF_8), "Entity with id 0 deleted"));
    }

    void testReturnString (String strToTest, String expected){
        Assertions.assertEquals(expected, strToTest);
    }

    @ParameterizedTest
    @DisplayName("Get error handling with wrong path parameters for ID")
    @MethodSource("wrongValuesProvider")
    void checkBadInputGet(String arg) {
        MyMatcher matcher = new MyMatcher();
        testClient
                .get()
                .uri("/books/antique/" + arg)
                .exchange()
                .expectStatus().value(matcher)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().value(v -> System.out.println("Status: " + v));
    }

    @ParameterizedTest
    @MethodSource("wrongValuesProvider")
    @DisplayName("Get error handling with wrong path parameters for barcode")
    void checkBadBarcodeInputGet(String arg) {
        MyMatcher matcher = new MyMatcher();
        testClient
                .get()
                .uri("/books/antique/barcode/" + arg)
                .exchange()
                .expectStatus().value(matcher)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().value(v -> System.out.println("Status: " + v));

    }

    @ParameterizedTest
    @MethodSource("wrongValuesProvider")
    @DisplayName("Get error handling with wrong path parameters for barcode match")
    void checkBadBarcodeMatchGet(String arg) {
        MyMatcher matcher = new MyMatcher();
        testClient
                .get()
                .uri("/books/antique/barcode/match/" + arg)
                .exchange()
                .expectStatus().value(matcher)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().value(v -> System.out.println("Status: " + v));

    }

    @ParameterizedTest
    @MethodSource("wrongValuesProvider")
    @DisplayName("Delete error handling with wrong path parameters for Id")
    void checkBadIdDelete(String arg) {
        MyMatcher matcher = new MyMatcher();
        testClient
                .delete()
                .uri("/books/antique/" + arg)
                .exchange()
                .expectStatus().value(matcher)
                .expectStatus().value(v -> System.out.println("Status: " + v));

    }

    @ParameterizedTest
    @MethodSource("wrongValuesProvider")
    @DisplayName("Post error handling with wrong path parameters for id")
    void checkBadIdPost(String arg) {
        MyMatcher matcher = new MyMatcher();
        testClient
                .post()
                .uri("/books/antique/" + arg)
                .exchange()
                .expectStatus().value(matcher)
                .expectStatus().value(v -> System.out.println("Status: " + v));
    }


    @ParameterizedTest
    @MethodSource("wrongBookInserter")
    @SneakyThrows
    @DisplayName("Post error handling with wrong Books for id")
    void checkBadBookPost(Book arg) {
        MyMatcher matcher = new MyMatcher();
        testClient
                .post()
                .uri("/books/antique/")
                .body(BodyInserters.fromValue(arg))
                .exchange()
                .expectStatus().value(matcher)
                .expectStatus().value(v -> System.out.println("Status: " + v));
    }

    @ParameterizedTest
    @MethodSource("wrongBookInserter")
    @SneakyThrows
    @DisplayName("Post error handling with wrong path parameters for id")
    void checkBadIdPut(Book arg) {
        MyMatcher matcher = new MyMatcher();
        testClient
                .put()
                .uri("/books/antique/1")
                .body(BodyInserters.fromValue(arg))
                .exchange()
                .expectStatus().value(matcher)
                .expectStatus().value(v -> System.out.println("Status: " + v));
    }


    static Stream<Book> wrongBookInserter() {
        return Stream.of("-30", "**", "-30.00", "-2.0", "4./8")
                .flatMap(value -> {
                    List<Book> bookList = new ArrayList<>();
                    bookList.add(new Book(value, "Pirmas autorius", "1234567890123", 10, 10.00));
                    bookList.add(new Book("Pirmoji knyga", "Pirmas autorius", "1234567890123", 10, 10.00));
                    bookList.add(new Book("Pirmoji knyga", value, "1234567890123", 10, 10.00));
                    bookList.add(new Book("Pirmoji knyga", "Pirmas autorius", value, 10, 10.00));
                    try {
                        bookList.add(new Book("Pirmoji knyga", "Pirmas autorius", "1234567890123",Integer.parseInt(value) , 10.00));
                    } catch (NumberFormatException e) {
                                           }
                    try {
                        bookList.add(new Book("Pirmoji knyga", "Pirmas autorius", "1234567890123", 10, Double.parseDouble(value)));
                    } catch (NumberFormatException e) {

                    }
                    return bookList.stream();
                });
    }


    static Stream<String> wrongValuesProvider() {
        return Stream.of("-30", "**", "-30.00", "-2.0", "4./8");
    }

}
