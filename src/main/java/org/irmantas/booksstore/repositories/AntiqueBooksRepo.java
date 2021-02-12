package org.irmantas.booksstore.repositories;

import org.irmantas.booksstore.model.AntiqueBook;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AntiqueBooksRepo extends ReactiveCrudRepository<AntiqueBook, Long> {
    Mono<AntiqueBook> findByBarcode(String barcode);
    Flux<AntiqueBook> findByBarcodeContaining(String value);
}
