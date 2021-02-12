package org.irmantas.booksstore.repositories;

import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.util.InfrastructureConfigurationRaw;
import org.irmantas.booksstore.util.LoadFlux;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@EnableR2dbcRepositories(basePackages = "org.irmantas.booksstore.repositories")
@SpringBootTest(classes = InfrastructureConfigurationRaw.class)
class BooksRepoTest {

    @Autowired
    LoadFlux booksStorage;

    @Autowired
    BooksRepo booksRepo;

    @Autowired
    DatabaseClient client;

    @BeforeEach
    public void setUp(){
        Hooks.onOperatorDebug();

        List<String> statements = Arrays.asList(
                "DROP TABLE IF EXISTS books;",
                "CREATE TABLE `books` (\n" +
                        "  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                        "  `name` varchar(40) DEFAULT '',\n" +
                        "  `author` varchar(40) NOT NULL DEFAULT '',\n" +
                        "  `barcode` varchar(20) NOT NULL,\n" +
                        "  `qty` int(11) ,\n" +
                        "  `price` decimal(15,2) NOT NULL DEFAULT 0.00);");

        statements
                .forEach(it -> client.sql(it) //
                        .fetch() //
                        .rowsUpdated() //
                        .as(StepVerifier::create) //
                        .expectNextCount(1) //
                        .verifyComplete());
    }

    @Test
    @DisplayName("Check if repository initialized")
    void checkRepository(){
        assertNotNull(booksRepo);
    }

    @Test
    @Order(1)
    @DisplayName("Save flux of all values")
    void testSaveAll(){
        booksRepo.saveAll(booksStorage.getBooks())
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextCount(16)
                .verifyComplete();
    }

    @Test
    @Order(2)
    @DisplayName("Save mono value")
    void testSaveOne(){
        booksRepo.save(booksStorage.getKnownBooksList().get(0)).subscribe();
        booksRepo.findAll()
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextMatches(v-> "Pirmoji knyga" == v.getName())
                .verifyComplete();
    }

    @Test
    @Order(3)
    @DisplayName("if succesfull finds item with id 1")
    void testFindById(){
        booksRepo.saveAll(booksStorage.getBooks()).subscribe();
        booksRepo.findById(1L)
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextMatches(v-> v.getId() == 1)
                .verifyComplete();
    }

    @Test
    @Order(4)
    @DisplayName("if succesfull deletes item with id 1")
    void testDelete(){
        booksRepo.save(booksStorage.getKnownBooksList().get(0)).subscribe();
        booksRepo.deleteById(1L)
                .as(StepVerifier::create)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @Order(5)
    @DisplayName("if succesfull updates item")
    void testUpate(){
        booksRepo.save(booksStorage.getKnownBooksList().get(0)).subscribe();
        Book original = booksStorage.getKnownBooksList().get(0);
        original.setQty(133);
        assertTrue(original.getQty() == 133);
        booksRepo.save(original)
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextMatches(v-> v.getQty() == 133)
                .verifyComplete();
    }


    @Test
    @DisplayName("if succesfull return book by barcode")
    void findByBarcode() {
        booksRepo.save(booksStorage.getKnownBooksList().get(0)).subscribe();
        booksRepo.findByBarcode("1234567890123")
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextMatches(v-> v.getQty() == 10)
                .verifyComplete();
    }

    @Test
    @DisplayName("if succesfull return books by barcode match")
    void findByBarcodeContaining() {
        booksRepo.saveAll(booksStorage.getKnownBooks()).subscribe();
        booksRepo.findByBarcodeContaining("123")
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }


}