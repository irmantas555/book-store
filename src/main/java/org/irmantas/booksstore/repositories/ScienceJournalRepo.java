package org.irmantas.booksstore.repositories;

import org.irmantas.booksstore.model.AntiqueBook;
import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.model.ScienceJournal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ScienceJournalRepo extends ReactiveCrudRepository<ScienceJournal, Long> {
    Mono<ScienceJournal> findByBarcode(String barcode);
    Flux<ScienceJournal> findByBarcodeContaining(String value);
}
