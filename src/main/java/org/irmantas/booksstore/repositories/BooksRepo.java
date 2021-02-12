package org.irmantas.booksstore.repositories;

import org.irmantas.booksstore.model.Book;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface BooksRepo extends ReactiveCrudRepository<Book, Long> {
        Mono<Book> findByBarcode(String barcode);

        Flux<Book> findByBarcodeContaining(String value);

}
