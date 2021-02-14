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

@RestController
@RequestMapping("books/antique")
public class AntiqueBookConroller {
    @Autowired
    ControllersUtils controllersUtils;

    @Autowired
    AntiqueBooksRepo antiqBooksRepo;

    @Autowired
    ApiErrors apiErrors;

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


    @PostMapping("")
    public Mono<ResponseEntity<Object>> postBook(@RequestBody AntiqueBook newBook) {
        String validationMessage = newBook.validateBook();
        if (!validationMessage.equals("OK")) {
            apiErrors.setMessage(validationMessage);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        } else {
            return antiqBooksRepo.save(newBook)
                    .map(book -> ResponseEntity.ok().body(book));
        }
    }


    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getBooksById(@PathVariable Long id) {
        return antiqBooksRepo.findById(id)
                .defaultIfEmpty(new AntiqueBook())
                .handle((book1, sink) -> {
                    if (null == book1.getName()) {
                        apiErrors.setMessage(notExistWithIdReply);
                        sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                    } else
                        sink.next(ResponseEntity.ok().body(book1));
                });
    }

    @GetMapping("/barcode/{value}")
    public Mono<ResponseEntity<Object>> getBooksByBarcode(@PathVariable String value) {
        return antiqBooksRepo.findByBarcode(value)
                .defaultIfEmpty(new AntiqueBook())
                .handle((book1, sink) -> {
                    if (null == book1.getName()) {
                        apiErrors.setMessage(notExistWithBarcodeReply);
                        sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                    } else
                        sink.next(ResponseEntity.ok().body(book1));
                });
    }

    @PutMapping("/barcode/{barcodeValue}/{field}/{fieldValue}")
    public Mono<ResponseEntity<Object>> updateValuesByBarcode(@PathVariable String barcodeValue,
                                                              @PathVariable String field,
                                                              @PathVariable String fieldValue
    ) {
        if (!barcodeValue.matches("[0-9]{13}")) {
            apiErrors.setMessage(barcodeNotValid);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        } else if (!controllersUtils.getBookClassesFieldList().contains(field)) {
            apiErrors.setMessage(noSuchField);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        } else {
            return antiqBooksRepo.findByBarcode(barcodeValue)
                    .defaultIfEmpty(new AntiqueBook())
                    .handle((book1, sink) -> {
                        if (null == book1.getName()) {
                            apiErrors.setMessage(notExistWithBarcodeReply);
                            sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                        } else {
                            Object objectResponseEntity = book1.updateField(field, fieldValue);
                            if (objectResponseEntity instanceof String) {
                                apiErrors.setMessage((String) objectResponseEntity);
                                sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                            } else {
                                antiqBooksRepo.save((AntiqueBook) objectResponseEntity)
                                        .defaultIfEmpty(new AntiqueBook())
                                        .subscribe(v -> {
                                                    if (null != v.getName()) {
                                                        sink.next(ResponseEntity.ok().body(v));
                                                    } else {
                                                        apiErrors.setMessage(dbOperationFailed);
                                                        sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                                                    }
                                                },
                                                err -> {
                                                    apiErrors.setMessage(err.getLocalizedMessage());
                                                    sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                                                });
                            }
                        }
                    });
        }
    }

    @GetMapping("/barcode/match/{value}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Flux<Object> getBarcodeMatch(@PathVariable String value) {
        if (!value.matches("[0-9]+")) {
            return Flux.just(barcodeNotValid);
        } else {
            return antiqBooksRepo.findByBarcodeContaining(value)
                    .defaultIfEmpty(new AntiqueBook())
                    .handle((book1, sink) -> {
                        if (null == book1.getName()) {
                            apiErrors.setMessage(notExistWithBarcodeReply);
                            sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                        } else
                            sink.next(book1);
                    });
        }
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateBookById(@PathVariable Long id, @RequestBody AntiqueBook updatedAntiqueBook) {
        String validationMessage = updatedAntiqueBook.validateBook();
        if (!validationMessage.equals("OK")) {
            apiErrors.setMessage(notExistWithIdReply);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        } else
            return antiqBooksRepo.findById(id)
                    .defaultIfEmpty(new AntiqueBook())
                    .handle((book1, sink) -> {
                        if (null == book1.getName()) {
                            apiErrors.setMessage(notExistWithIdReply);
                            sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                        } else {
                            updatedAntiqueBook.setId(id);
                            antiqBooksRepo.save(updatedAntiqueBook)
                                    .subscribe(v -> sink.next(ResponseEntity.ok().body(v)),
                                            err -> {
                                                apiErrors.setMessage(err.getLocalizedMessage());
                                                sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                                            });
                        }
                    });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> deleteBook(@PathVariable Long id) {
        return antiqBooksRepo.findById(id)
                .defaultIfEmpty(new AntiqueBook())
                .handle((book1, sink) -> {
                    if (null == book1.getName()) {
                        apiErrors.setMessage(notExistWithIdReply);
                        sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                    } else {
                        antiqBooksRepo.deleteById(id)
                                .subscribe(v -> {
                                        },
                                        err -> {
                                            apiErrors.setMessage(err.getLocalizedMessage());
                                            sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                                        },
                                        () -> sink.next(new ResponseEntity<>("Entity with id " + id + " deleted", HttpStatus.OK))
                                );
                    }
                });
    }

 
}
