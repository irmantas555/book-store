package org.irmantas.booksstore.controllers;

import org.irmantas.booksstore.exceptions.ApiErrors;
import org.irmantas.booksstore.model.AntiqueBook;
import org.irmantas.booksstore.repositories.AntiqueBooksRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.irmantas.booksstore.model.ErrMsg.*;

@RestController
@RequestMapping("books/antique")
public class AntiqueBookConroller {
    @Autowired
    ControllersUtils controllersUtils;

    @Autowired
    AntiqueBooksRepo antiqBooksRepo;

    @Autowired
    ApiErrors apiErrors;

    @GetMapping("")
    public Flux<AntiqueBook> getAllBooks() {
        return antiqBooksRepo.findAll();
    }

    @PostMapping("")
    public Mono<ResponseEntity<AntiqueBook>> postBook(@RequestBody AntiqueBook book) {
        String validationMessage = book.validateBook();
        if (!validationMessage.equals("OK")) {
            return getResponseErrorMono(validationMessage);
        } else {
            return antiqBooksRepo.save(book)
                    .map(book1 -> ResponseEntity.ok().body(book));
        }
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AntiqueBook>> getBooksById(@PathVariable Long id) {
        return Mono.just(id)
                .map(Math::abs)
                .flatMap(aLong -> antiqBooksRepo.findById(id))
                .onErrorResume(e -> getBookErrorMono(DB_OPERATION_FAILED))
                .map(book1 -> ResponseEntity.ok().body(book1))
                .switchIfEmpty(getResponseErrorMono(NO_ENTITY_WITH_ID));
    }

    @GetMapping("/barcode/{value}")
    public Mono<ResponseEntity<AntiqueBook>> getBooksByBarcode(@PathVariable String value) {
        return Mono.just(value)
                .filter(val -> val.matches("[0-9]{13}"))
                .flatMap(val -> antiqBooksRepo.findByBarcode(value))
                .map(book1 -> ResponseEntity.ok().body(book1))
                .switchIfEmpty(getResponseErrorMono(NO_ENTITY_WITH_BARCODE));
    }

    @PutMapping("/barcode/{barcodeValue}/{field}/{fieldValue}")
    public Mono<ResponseEntity<AntiqueBook>> updateValuesByBarcode(@PathVariable String barcodeValue,
                                                            @PathVariable String field,
                                                            @PathVariable String fieldValue
    ) {
        if (!barcodeValue.matches("[0-9]{13}")) {
            return getResponseErrorMono(BARCODE_NOT_VALID);
        } else if (!controllersUtils.getBookClassesFieldList().contains(field)) {
            return getResponseErrorMono(NO_SUCH_FIELD);
        } else {
            return antiqBooksRepo.findByBarcode(barcodeValue)
                    .switchIfEmpty(getBookErrorMono(NO_ENTITY_WITH_BARCODE))
                    .flatMap(book -> {
                        Object validation = book.updateField(field, fieldValue);
                        if (validation instanceof String) {
                            return getResponseErrorMono((String) validation);
                        } else {
                            return Mono.just(book);
                        }
                    })
                    .flatMap(book -> antiqBooksRepo.save((AntiqueBook)book))
                    .map(book -> ResponseEntity.ok().body(book))
                    .switchIfEmpty(getResponseErrorMono(DB_OPERATION_FAILED));
        }
    }

    @GetMapping("/barcode/match/{value}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<AntiqueBook> getBarcodeMatch(@PathVariable String value) {
        if (!value.matches("[0-9]+")) {
            return getBookErrorFlux(BARCODE_NOT_VALID);
        } else {
            return antiqBooksRepo.findByBarcodeContaining(value)
                    .onErrorResume(e -> getBookErrorFlux(DB_OPERATION_FAILED))
                    .switchIfEmpty(getBookErrorFlux(NO_ENTITY_WITH_ID));
        }
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<AntiqueBook>> updateBookById(@PathVariable Long id, @RequestBody AntiqueBook updatedBook) {
        String validationMessage = updatedBook.validateBook();
        if (!validationMessage.equals("OK")) {
            return getResponseErrorMono(validationMessage);
        } else
            return antiqBooksRepo.findById(id)
                    .switchIfEmpty(getBookErrorMono(NO_ENTITY_WITH_ID))
                    .flatMap(book -> {
                        updatedBook.setId(id);
                        return antiqBooksRepo.save(updatedBook);
                    })
                    .map(bookMono -> ResponseEntity.ok().body(bookMono));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteBook(@PathVariable Long id) {
        if (!String.valueOf(id).matches("[0-9]+")) {
            return Mono.just(ResponseEntity.badRequest().body(NO_ENTITY_WITH_ID));
        } else
            return antiqBooksRepo.findById(id)
                    .switchIfEmpty(getBookErrorMono(NO_ENTITY_WITH_ID))
                    .then(antiqBooksRepo.deleteById(id))
                    .thenReturn("Ok")
                    .map(v -> ResponseEntity.ok().body("Entity with id " + id + " deleted"));
    }

    Mono<AntiqueBook> getBookErrorMono(String message) {
        apiErrors.setMessage(message);
        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

    Flux<AntiqueBook> getBookErrorFlux(String message) {
        apiErrors.setMessage(message);
        return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

    ResponseEntity<AntiqueBook> getRespBook(String message) {
        apiErrors.setMessage(message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    Mono<ResponseEntity<AntiqueBook>> getResponseErrorMono(String message) {
        apiErrors.setMessage(message);
        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

}
