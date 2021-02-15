package org.irmantas.booksstore.repositories;

import org.irmantas.booksstore.model.AntiqueBook;
import org.irmantas.booksstore.util.InfrastructureConfiguration;
import org.irmantas.booksstore.util.LoadFlux;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@EnableR2dbcRepositories(basePackages = "org.irmantas.booksstore.repositories")
@SpringBootTest(classes = InfrastructureConfiguration.class)
class AntiqueBooksRepoTest {

    @Autowired
    LoadFlux booksStorage;

    @Autowired
    AntiqueBooksRepo antiqueBooksRepo;

    @Autowired
    DatabaseClient client;

    @BeforeEach
    public void setUp(){
        Hooks.onOperatorDebug();

        List<String> statements = Arrays.asList(
                "DROP TABLE IF EXISTS antique_books;",
                "CREATE TABLE `antique_books` (\n" +
                        "  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                        "  `name` varchar(40) DEFAULT '',\n" +
                        "  `author` varchar(40) NOT NULL DEFAULT '',\n" +
                        "  `barcode` varchar(20) NOT NULL,\n" +
                        "  `qty` int(11) ,\n" +
                        "  `price` decimal(15,2) NOT NULL DEFAULT 0.00, \n" +
                        "  `release_year` int(11) NOT NULL);");

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
        assertNotNull(antiqueBooksRepo);
    }

    @Test
    @Order(1)
    @DisplayName("Save flux of all values")
    void testSaveAll(){
        antiqueBooksRepo.saveAll(booksStorage.getAntiqueBooks())
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextCount(16)
                .verifyComplete();
    }

    @Test
    @Order(2)
    @DisplayName("Save mono value")
    void testSaveOne(){
        antiqueBooksRepo.save(booksStorage.getKnownAntiqueBooksList().get(0)).subscribe();
        antiqueBooksRepo.findAll()
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextMatches(v-> "Pirmoji knyga".equals(v.getName()))
                .verifyComplete();
    }

    @Test
    @Order(3)
    @DisplayName("if succesfull finds item with id 1")
    void testFindById(){
        antiqueBooksRepo.saveAll(booksStorage.getAntiqueBooks()).subscribe();
        antiqueBooksRepo.findById(1L)
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextMatches(v-> v.getId() == 1)
                .verifyComplete();
    }

    @Test
    @Order(4)
    @DisplayName("if succesfull deletes item with id 1")
    void testDelete(){
        antiqueBooksRepo.save(booksStorage.getKnownAntiqueBooksList().get(0)).subscribe();
        antiqueBooksRepo.deleteById(1L)
                .as(StepVerifier::create)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @Order(5)
    @DisplayName("if succesfull updates item")
    void testUpate(){
        antiqueBooksRepo.save(booksStorage.getKnownAntiqueBooksList().get(0)).subscribe();
        AntiqueBook original = booksStorage.getKnownAntiqueBooksList().get(0);
        original.setQty(133);
        assertEquals(original.getQty(), 133);
        antiqueBooksRepo.save(original)
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextMatches(v-> v.getQty() == 133)
                .verifyComplete();
    }


    @Test
    @DisplayName("if succesfull return book by barcode")
    void findByBarcode() {
        antiqueBooksRepo.save(booksStorage.getKnownAntiqueBooksList().get(0)).subscribe();
        antiqueBooksRepo.findByBarcode("1234567890123")
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextMatches(v-> v.getQty() == 10)
                .verifyComplete();
    }

    @Test
    @DisplayName("if succesfull return books by barcode match")
    void findByBarcodeContaining() {
        antiqueBooksRepo.saveAll(booksStorage.getKnownAntiqueBooksList()).subscribe();
        antiqueBooksRepo.findByBarcodeContaining("123")
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }


}