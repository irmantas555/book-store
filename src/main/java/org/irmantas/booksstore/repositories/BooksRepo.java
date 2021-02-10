package org.irmantas.booksstore.repositories;

import org.irmantas.booksstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public interface BooksRepo extends ReactiveCrudRepository<Book, Long> {

}
