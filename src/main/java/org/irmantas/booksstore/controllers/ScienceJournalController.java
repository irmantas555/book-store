package org.irmantas.booksstore.controllers;

import org.irmantas.booksstore.model.ScienceJournal;
import org.irmantas.booksstore.repositories.ScienceJournalRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("science/journals")
public class ScienceJournalController {
    @Autowired
    ControllersUtils controllersUtils;

    Logger logger = LoggerFactory.getLogger(BookController.class);

    String notExistWithIdReply = "Entity with such Id not exists in DB";
    String notExistWithBarcodeReply = "Entity with such barcode not exists in DB";
    String barcodeNotValid = "Value of barcode for search is not valid";
    String notExistReply = "Such entity not exists in DB";
    String noSuchField = "No such field in in Books";
    String dbOperationFailed = "There was an error in DB operation";

    @Autowired
    ScienceJournalRepo scienceJournalRepo;

    @GetMapping("")
    public Flux<ScienceJournal> getAllSciencJournal() {
        return scienceJournalRepo.findAll();
    }



    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getSciencJournalsById(@PathVariable Long id) {
        return scienceJournalRepo.findById(id)
                .defaultIfEmpty(new ScienceJournal())
                .handle((book1, sink) -> {
                    if (null == book1.getName()) {
                        sink.next(new ResponseEntity<>(notExistWithIdReply, HttpStatus.BAD_REQUEST));
                    } else
                        sink.next(ResponseEntity.ok().body(book1));
                });
    }

    @GetMapping("/barcode/{value}")
    public Mono<ResponseEntity<Object>> getSciencJournalByBarcode(@PathVariable String value) {
        return scienceJournalRepo.findByBarcode(value)
                .defaultIfEmpty(new ScienceJournal())
                .handle((book1, sink) -> {
                    if (null == book1.getName()) {
                        sink.next(new ResponseEntity<>(notExistWithBarcodeReply, HttpStatus.BAD_REQUEST));
                    } else
                        sink.next(ResponseEntity.ok().body(book1));
                });
    }

    @PutMapping("/barcode/{barcodeValue}/{field}/{fieldValue}")
    public Mono<ResponseEntity<Object>> updateSciencJournalValuesByBarcode(@PathVariable String barcodeValue,
                                                              @PathVariable String field,
                                                              @PathVariable String fieldValue
    ) {
        if (!barcodeValue.matches("[0-9]{13}")) {
            return Mono.just(new ResponseEntity<>(barcodeNotValid, HttpStatus.BAD_REQUEST));
        } else if (!controllersUtils.getBookClassesFieldList().contains(field)) {
            return Mono.just(new ResponseEntity<>(noSuchField, HttpStatus.BAD_REQUEST));
        } else {
            return scienceJournalRepo.findByBarcode(barcodeValue)
                    .defaultIfEmpty(new ScienceJournal())
                    .handle((book1, sink) -> {
                        if (null == book1.getName()) {
                            sink.next(new ResponseEntity<>(notExistWithBarcodeReply, HttpStatus.BAD_REQUEST));
                        } else {
                            Object objectResponseEntity = book1.updateField(field, fieldValue);
                            if (objectResponseEntity instanceof String) {
                                sink.next(new ResponseEntity<>((String) objectResponseEntity, HttpStatus.BAD_REQUEST));
                            } else {
                                scienceJournalRepo.save((ScienceJournal) objectResponseEntity)
                                        .defaultIfEmpty(new ScienceJournal())
                                        .flatMap(scienceJournalRepo::save)
                                        .subscribe(v -> {
                                                    if (null != v.getName()) {
                                                        sink.next(ResponseEntity.ok().body(v));
                                                    } else {
                                                        sink.next(new ResponseEntity<>(dbOperationFailed, HttpStatus.BAD_REQUEST));
                                                    }
                                                },
                                                err -> sink.next(new ResponseEntity<>(err.getLocalizedMessage(), HttpStatus.BAD_REQUEST)));
                            }
                        }
                    });
        }
    }
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/barcode/match/{value}")
    public Flux<Object> getSciencJournalBarcodeMatch(@PathVariable String value) {
        if (!value.matches("[0-9]+")) {
            return Flux.just(barcodeNotValid);
        } else {
            return scienceJournalRepo.findByBarcodeContaining(value.toString())
                    .defaultIfEmpty(new ScienceJournal())
                    .handle((book1, sink) -> {
                        if (null == book1.getName()) {
                            sink.next(new ResponseEntity<>(notExistWithBarcodeReply, HttpStatus.BAD_REQUEST));
                        } else
                            sink.next(book1);
                    });
        }
    }

    @PostMapping("")
    public Mono<ResponseEntity<Object>> postSciencJournal(@RequestBody ScienceJournal scienceJournal) {
        String validationMessage = scienceJournal.validateBook();
        if (!validationMessage.equals("OK")) {
            return Mono.just(new ResponseEntity<>(validationMessage, HttpStatus.BAD_REQUEST));
        } else
            return Mono.just(scienceJournal)
                    .flatMap(book1 -> scienceJournalRepo.save(book1))
                    .map(book1 -> ResponseEntity.ok().body(book1));
    }


    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateSciencJournalById(@PathVariable Long id, @RequestBody ScienceJournal updatedScienceJournal) {
        String validationMessage = updatedScienceJournal.validateBook();
        if (!validationMessage.equals("OK")) {
            return Mono.just(new ResponseEntity<>(validationMessage, HttpStatus.BAD_REQUEST));
        } else
            return scienceJournalRepo.findById(id)
                    .defaultIfEmpty(new ScienceJournal())
                    .handle((book1, sink) -> {
                        if (null == book1.getName()) {
                            sink.next(new ResponseEntity<>(notExistWithIdReply, HttpStatus.BAD_REQUEST));
                        } else {
                            scienceJournalRepo.save(updatedScienceJournal)
                                    .subscribe(v -> sink.next(ResponseEntity.ok().body(v)),
                                            throwable -> sink.next(new ResponseEntity<>(throwable.getLocalizedMessage(), HttpStatus.BAD_REQUEST)));
                        }
                    });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> deleteBook(@PathVariable Long id) {
        return scienceJournalRepo.findById(id)
                .defaultIfEmpty(new ScienceJournal())
                .handle((book1, sink) -> {
                    if (null == book1.getName()) {
                        sink.next(new ResponseEntity<>(notExistWithIdReply, HttpStatus.BAD_REQUEST));
                    } else {
                        scienceJournalRepo.deleteById(id)
                                .subscribe(v -> {
                                        },
                                        throwable -> sink.next(new ResponseEntity<>(throwable.getLocalizedMessage(), HttpStatus.BAD_REQUEST)),
                                        () -> sink.next(new ResponseEntity<>("Entity with id " + id + " deleted", HttpStatus.OK)));
                    }
                });
    }
}
