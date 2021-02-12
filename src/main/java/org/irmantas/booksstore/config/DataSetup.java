package org.irmantas.booksstore.config;

import io.r2dbc.spi.ConnectionFactory;
import org.irmantas.booksstore.model.AntiqueBook;
import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.model.ScienceJournal;
import org.irmantas.booksstore.repositories.AntiqueBooksRepo;
import org.irmantas.booksstore.repositories.BooksRepo;
import org.irmantas.booksstore.repositories.ScienceJournalRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Random;

@Component
public class DataSetup {

    Flux<Book> bookFlux;

    String filePath;

    Path path;

    Random random = new Random();

    @Autowired
    BooksRepo booksRepo;

    @Autowired
    AntiqueBooksRepo antiqueBooksRepo;

    @Autowired
    ScienceJournalRepo scienceJournalRepo;

    public DataSetup() {
        this.filePath = "src/main/resources/books.txt";
        ;
        this.path = Paths.get(filePath);
    }

    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        return initializer;
    }
    @PostConstruct
    public void getBooks() {
        Mono.fromCallable(() -> Files.readAllLines(path))
                .delaySubscription(Duration.ofSeconds(3))
                .flux()
                .flatMap(strings -> Flux.fromIterable(strings))
                .map(s -> Arrays.asList(s.split("-")))
                .handle((list, sink) -> {
                    int rndInt = random.nextInt(10);
                    System.out.println("RANDOM VAL: " + rndInt);
                    switch (rndInt< 4?1:rndInt<8 && rndInt > 3?2:3){
                        case 1:
                            Book newBook = new Book(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice());
                            booksRepo.save(newBook)
                                    .subscribe(v -> {
                                        sink.next(v);
                                    });
                            break;
                        case 2:
                            AntiqueBook antiqueBook = new AntiqueBook(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice(), getAntiqueYear());
                            antiqueBooksRepo.save(antiqueBook)
                                    .subscribe(v -> {
                                        sink.next(v);
                                    });
                            break;
                        case 3:
                            ScienceJournal scienceJournal = new ScienceJournal(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice(), getScienceIndex());
                            scienceJournalRepo.save(scienceJournal)
                                    .subscribe(v -> {
                                        sink.next(v);
                                    });
                            break;
                    }
                })
                .subscribe();
    }

    String generateBarcode() {
        Random r1 = new Random();
        long[] longs = r1.longs(1, 1000000000000L, 10000000000000L).toArray();
        return String.valueOf(longs[0]);

    }

    double genetaratePrice() {
        Random r2 = new Random();
        return Double.valueOf(r2.nextInt(3000)) / 100 + 10.00;
    }

    int getQty() {
        Random r1 = new Random();
        return r1.nextInt(30) + 5;
    }

    int getAntiqueYear() {
        return random.nextInt(300) + 1600;
    }

    int getScienceIndex() {
        return random.nextInt(8) + 1;
    }
}
