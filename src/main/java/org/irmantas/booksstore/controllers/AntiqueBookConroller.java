package org.irmantas.booksstore.controllers;

import org.irmantas.booksstore.model.AntiqueBook;
import org.irmantas.booksstore.repositories.AntiqueBooksRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("books/antique")
public class AntiqueBookConroller {
    @Autowired
    ControllersUtils controllersUtils;

    @Autowired
    AntiqueBooksRepo antiqBooksRepo;

    Logger logger = LoggerFactory.getLogger(BookController.class);

    String notExistWithIdReply = "Entity with such Id not exists in DB";
    String notExistWithBarcodeReply = "Entity with such barcode not exists in DB";
    String barcodeNotValid = "Value of barcode for search is not valid";
    String notExistReply = "Such entity not exists in DB";
    String noSuchField = "No such field in in Books";
    String dbOperationFailed = "There was an error in DB operation";



    @GetMapping("")
    public Flux<AntiqueBook> getAllBooks() {
        return antiqBooksRepo.findAll();
    }


    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getBooksById(@PathVariable Long id) {
        return antiqBooksRepo.findById(id)
                .defaultIfEmpty(new AntiqueBook())
                .handle((book1, sink) -> {
                    logger.info("Book= ", book1);
                    if (null == book1.getName()) {
                        sink.next(new ResponseEntity<>(notExistWithIdReply, HttpStatus.BAD_REQUEST));
                    } else
                        sink.next(ResponseEntity.ok().body(book1));
                });
    }

    @GetMapping("/barcode/{value}")
    public Mono<ResponseEntity<Object>> getBooksByBarcode(@PathVariable String value) {
        return antiqBooksRepo.findByBarcode(value)
                .defaultIfEmpty(new AntiqueBook())
                .handle((book1, sink) -> {
                    logger.info("Book= ", book1);
                    if (null == book1.getName()) {
                        sink.next(new ResponseEntity<>(notExistWithBarcodeReply, HttpStatus.BAD_REQUEST));
                    } else
                        sink.next(ResponseEntity.ok().body(book1));
                });
    }

    @PutMapping("/barcode/{barcodeValue}/{field}/{fieldValue}")
    public Mono<ResponseEntity<Object>> updateValuesByBarcode(@PathVariable String barcodeValue,
                                                              @PathVariable String field,
                                                              @PathVariable String fieldValue
    ) {
        if (barcodeValue.matches("[0-9]{13}")) {
            return Mono.just(new ResponseEntity<>(barcodeNotValid, HttpStatus.BAD_REQUEST));
        } else if (!controllersUtils.getBookClassesFieldList().contains(field)) {
            return Mono.just(new ResponseEntity<>(noSuchField, HttpStatus.BAD_REQUEST));
        } else {
            return antiqBooksRepo.findByBarcode(barcodeValue)
                    .defaultIfEmpty(new AntiqueBook())
                    .handle((book1, sink) -> {
                        logger.info("Book= ", book1);
                        if (null == book1.getName()) {
                            sink.next(new ResponseEntity<>(notExistWithBarcodeReply, HttpStatus.BAD_REQUEST));
                        } else {
                            Object objectResponseEntity = book1.updateField(field, fieldValue);
                            if (objectResponseEntity instanceof String) {
                                sink.next(new ResponseEntity<>((String) objectResponseEntity, HttpStatus.BAD_REQUEST));
                            }
                            antiqBooksRepo.save((AntiqueBook) objectResponseEntity)
                                    .defaultIfEmpty(new AntiqueBook())
                                    .flatMap(antiqBooksRepo::save)
                                    .subscribe(v -> {
                                                if (null != v.getName()) {
                                                    ResponseEntity.ok().body(v);
                                                } else {
                                                    new ResponseEntity<>(dbOperationFailed, HttpStatus.BAD_REQUEST);
                                                }
                                            },
                                            err -> sink.next(new ResponseEntity<>(err.getLocalizedMessage(), HttpStatus.BAD_REQUEST)));

                        }

                        sink.next(ResponseEntity.ok().body(book1));
                    });
        }
    }

    @GetMapping("/barcode/match/{value}")
    public Flux<ResponseEntity<Object>> getBarcodeMatch(@PathVariable String value) {
        if (!value.matches("[0-9]{13}")) {
            return Flux.just(new ResponseEntity<>(barcodeNotValid, HttpStatus.BAD_REQUEST));
        } else {
            return antiqBooksRepo.findByBarcodeContaining(value)
                    .defaultIfEmpty(new AntiqueBook())
                    .handle((book1, sink) -> {
                        logger.info("Book= ", book1);
                        if (null == book1.getName()) {
                            sink.next(new ResponseEntity<>(notExistWithBarcodeReply, HttpStatus.BAD_REQUEST));
                        } else
                            sink.next(ResponseEntity.ok().body(book1));
                    });
        }
    }

    @PostMapping("")
    public Mono<ResponseEntity<Object>> postBook(@RequestBody AntiqueBook newBook) {
        String validationMessage = newBook.validateBook();
        if (!validationMessage.equals("OK")) {
            return Mono.just(new ResponseEntity<>(validationMessage, HttpStatus.BAD_REQUEST));
        } else
            return Mono.just(newBook)
                    .flatMap(book1 -> antiqBooksRepo.save(book1))
                    .map(book1 -> ResponseEntity.ok().body(book1));
    }



    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateBookById(@PathVariable Long id, @RequestBody AntiqueBook updatedBook) {
        String validationMessage = updatedBook.validateBook();
        if (!validationMessage.equals("OK")) {
            return Mono.just(new ResponseEntity<>(validationMessage, HttpStatus.BAD_REQUEST));
        } else
            return antiqBooksRepo.findById(id)
                    .defaultIfEmpty(new AntiqueBook())
                    .handle((book1, sink) -> {
                        if (null == book1.getName()) {
                            sink.next(new ResponseEntity<>(notExistWithIdReply, HttpStatus.BAD_REQUEST));
                        } else {
                            antiqBooksRepo.save(updatedBook)
                                    .subscribe(v -> sink.next(ResponseEntity.ok().body(v)),
                                            throwable -> sink.next(new ResponseEntity<>(throwable.getLocalizedMessage(), HttpStatus.BAD_REQUEST)));
                        }
                    });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> deleteBook(@PathVariable Long id) {
        return antiqBooksRepo.findById(id)
                .defaultIfEmpty(new AntiqueBook())
                .handle((book1, sink) -> {
                    if (null == book1.getName()) {
                        sink.next(new ResponseEntity<>(notExistWithIdReply, HttpStatus.BAD_REQUEST));
                    } else {
                        antiqBooksRepo.deleteById(id)
                                .subscribe(v -> {
                                        },
                                        throwable -> sink.next(new ResponseEntity<>(throwable.getLocalizedMessage(), HttpStatus.BAD_REQUEST)),
                                        () -> sink.next(new ResponseEntity<>("Entity with id " + id + " deleted", HttpStatus.OK)));
                    }
                });
    }
}
