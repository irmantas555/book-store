package org.irmantas.booksstore.services;

import org.irmantas.booksstore.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class CustomQueryService {
    @Autowired
    DatabaseClient client;

    String query1 = "SELECT * FROM books WHERE INSTR(barcode,'";

    public Flux<Book> getBooksMatchingBarcode(String match){
        return this.client.sql(query1 + match + "')")
                .fetch()
                .all().ofType(Book.class);
    }

}
