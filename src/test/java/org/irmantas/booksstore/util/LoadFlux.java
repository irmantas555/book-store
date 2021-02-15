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
    Random r1 = new Random();


    public Flux<Book> getBooks() {
        return Mono.fromCallable(() -> Files.readAllLines(path))
                .flux()
                .flatMap(Flux::fromIterable)
                .map(s -> Arrays.asList(s.split("-")))
                .map(list -> new Book(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice()));
    }

    public Flux<AntiqueBook> getAntiqueBooks() {
        return Mono.fromCallable(() -> Files.readAllLines(path))
                .flux()
                .flatMap(Flux::fromIterable)
                .map(s -> Arrays.asList(s.split("-")))
                .map(list -> new AntiqueBook(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice(), getAntiqueYear()));
    }

    public Flux<ScienceJournal> getScienceJournal() {
        return Mono.fromCallable(() -> Files.readAllLines(path))
                .flux()
                .flatMap(Flux::fromIterable)
                .map(s -> Arrays.asList(s.split("-")))
                .map(list -> new ScienceJournal(list.get(1), list.get(0), generateBarcode(), getQty(), genetaratePrice(), getScienceIndex()));
    }


    String generateBarcode() {

        long[] longs = r1.longs(1, 100000000000L, 1000000000000L).toArray();
        return String.valueOf(longs[0]);

    }

    double genetaratePrice() {

        return (double) r1.nextInt(3000) / 100 + 10.00;
    }

    int getAntiqueYear() {
        return r1.nextInt(300) + 1600;
    }

    int getScienceIndex() {
        return r1.nextInt(8) + 1;
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

    public List<AntiqueBook> getKnownAntiqueBooksList(){
        List<AntiqueBook> coupleBooks = new ArrayList<>();
        coupleBooks.add(new AntiqueBook("Pirmoji knyga", "Pirmas autorius", "1234567890123", 10, 10.00, 1800));
        coupleBooks.add(new AntiqueBook("Antra knyga", "Antras autorius", "3210987654321", 20, 20.00, 1800));
        return coupleBooks;
    }

    public List<ScienceJournal> getKnownScienceJournalList(){
        List<ScienceJournal> coupleBooks = new ArrayList<>();
        coupleBooks.add(new ScienceJournal("Pirmoji knyga", "Pirmas autorius", "1234567890123", 10, 10.00, 5));
        coupleBooks.add(new ScienceJournal("Antra knyga", "Antras autorius", "3210987654321", 20, 20.00, 5));
        return coupleBooks;
    }

    public Book getModifiedBook(){
        return new Book(1L,"Antra knyga", "John", "3210987654321", 20, BigDecimal.valueOf(20.00));
    }


    public AntiqueBook getModifiedAntiqueBook(){
        return new AntiqueBook(1L,"Antra knyga", "John", "3210987654321", 20, BigDecimal.valueOf(20.00),  1800);
    }


    public ScienceJournal getModifiedAScienceJournal(){
        return new ScienceJournal(1L,"Antra knyga", "John", "3210987654321", 20, BigDecimal.valueOf(20.00),  5);
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
