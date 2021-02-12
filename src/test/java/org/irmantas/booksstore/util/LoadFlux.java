package org.irmantas.booksstore.util;

import org.irmantas.booksstore.model.AntiqueBook;
import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.model.ScienceJournal;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class LoadFlux {
    String filePath = "src/main/resources/books.txt";
    Path path = Paths.get(filePath);
    Flux<Book> bookFlux;

    public Flux<Book> getBooks() {
        return Mono.fromCallable(() -> Files.readAllLines(path))
                .flux()
                .flatMap(strings -> Flux.fromIterable(strings))
                .map(s -> Arrays.asList(s.split("-")))
                .map(list -> new Book(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice()));
    }

    String generateBarcode() {
        Random r1 = new Random();
        long[] longs = r1.longs(1, 100000000000L, 1000000000000L).toArray();
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

    public Flux<Book> getKnownBooks(){
        List<Book> coupleBooks = new ArrayList<>();
        coupleBooks.add(new Book("Pirmoji knyga", "Pirmas autorius", "1234567890123", 10, 10.00));
        coupleBooks.add(new Book("Antra knyga", "Antras autorius", "3210987654321", 20, 20.00));
        return Flux.fromIterable(coupleBooks);
    }
    public List<Book> getKnownBooksList(){
        List<Book> coupleBooks = new ArrayList<>();
        coupleBooks.add(new Book("Pirmoji knyga", "Pirmas autorius", "1234567890123", 10, 10.00));
        coupleBooks.add(new Book("Antra knyga", "Antras autorius", "3210987654321", 20, 20.00));
        return coupleBooks;
    }

    public Book getModifiedBook(){
        return new Book(1L,"Antra knyga", "John", "3210987654321", 20, BigDecimal.valueOf(20.00));
    }

    public Flux<Book> getBookFluxByBarcode(){
        return Flux.just(new Book(1L,"Antra knyga", "John", "3210987654321", 10, BigDecimal.valueOf(10.00)));
    }


    public Flux<AntiqueBook> getAntiqueBookFluxByBarcode(){
        return Flux.just(new AntiqueBook(1L,"Antra knyga", "John", "3210987654321", 10, BigDecimal.valueOf(10.00), 1821));
    }


    public Flux<ScienceJournal> getScienceJournalFluxByBarcode(){
        return Flux.just(new ScienceJournal(1L,"Antra knyga", "John", "3210987654321", 20, BigDecimal.valueOf(20.00), 5));
    }


    public Mono<Void> getEmtpyBooks(){
        return Mono.empty();
    }

    public Flux<Book> getErrorFlux(){
        return Flux.error(new UnsupportedOperationException("This opertation is not suppoorted"));
    }
}
