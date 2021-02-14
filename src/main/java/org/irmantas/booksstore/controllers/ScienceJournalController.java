package org.irmantas.booksstore.controllers;


import org.irmantas.booksstore.exceptions.ApiErrors;
import org.irmantas.booksstore.model.ScienceJournal;
import org.irmantas.booksstore.repositories.ScienceJournalRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("science/journals")
public class ScienceJournalController {
    @Autowired
    ControllersUtils controllersUtils;

    @Autowired
    ScienceJournalRepo scienceJournalRepo;

    @Autowired
    ApiErrors apiErrors;
    
    Logger logger = LoggerFactory.getLogger(ScienceJournalController.class);

    String notExistWithIdReply = "Entity with such Id not exists in DB";
    String notExistWithBarcodeReply = "Entity with such barcode not exists in DB";
    String barcodeNotValid = "Value of barcode for search is not valid";
    String notExistReply = "Such entity not exists in DB";
    String noSuchField = "No such field in in ScienceJournal";
    String dbOperationFailed = "There was an error in DB operation";

    @GetMapping("")
    public Flux<ScienceJournal> getAllScienceJournal() {
        return scienceJournalRepo.findAll();
    }


    @PostMapping("")
    public Mono<ResponseEntity<Object>> postScienceJournal(@RequestBody ScienceJournal newScienceJournal) {
        String validationMessage = newScienceJournal.validateBook();
        if (!validationMessage.equals("OK")) {
            apiErrors.setMessage(validationMessage);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        } else {
            return scienceJournalRepo.save(newScienceJournal)
                    .map(scienceJournal -> ResponseEntity.ok().body(scienceJournal ));
        }
    }


    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getScienceJournalById(@PathVariable Long id) {
        return scienceJournalRepo.findById(id)
                .defaultIfEmpty(new ScienceJournal())
                .handle((scienceJournal1, sink) -> {
                    if (null == scienceJournal1.getName()) {
                        apiErrors.setMessage(notExistWithIdReply);
                        sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                    } else
                        sink.next(ResponseEntity.ok().body(scienceJournal1));
                });
    }

    @GetMapping("/barcode/{value}")
    public Mono<ResponseEntity<Object>> getScienceJournalByBarcode(@PathVariable String value) {
        return scienceJournalRepo.findByBarcode(value)
                .defaultIfEmpty(new ScienceJournal())
                .handle((scienceJournal1, sink) -> {
                    if (null == scienceJournal1.getName()) {
                        apiErrors.setMessage(notExistWithBarcodeReply);
                        sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                    } else
                        sink.next(ResponseEntity.ok().body(scienceJournal1));
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
            return scienceJournalRepo.findByBarcode(barcodeValue)
                    .defaultIfEmpty(new ScienceJournal())
                    .handle((scienceJournal1, sink) -> {
                        if (null == scienceJournal1.getName()) {
                            apiErrors.setMessage(notExistWithBarcodeReply);
                            sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                        } else {
                            Object objectResponseEntity = scienceJournal1.updateField(field, fieldValue);
                            if (objectResponseEntity instanceof String) {
                                apiErrors.setMessage((String) objectResponseEntity);
                                sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                            } else {
                                scienceJournalRepo.save((ScienceJournal) objectResponseEntity)
                                        .defaultIfEmpty(new ScienceJournal())
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
            return scienceJournalRepo.findByBarcodeContaining(value)
                    .defaultIfEmpty(new ScienceJournal())
                    .handle((scienceJournal1, sink) -> {
                        if (null == scienceJournal1.getName()) {
                            apiErrors.setMessage(notExistWithBarcodeReply);
                            sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                        } else
                            sink.next(scienceJournal1);
                    });
        }
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateScienceJournalById(@PathVariable Long id, @RequestBody ScienceJournal updatedscienceJournal) {
        String validationMessage = updatedscienceJournal.validateBook();
        if (!validationMessage.equals("OK")) {
            apiErrors.setMessage(notExistWithIdReply);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        } else
            return scienceJournalRepo.findById(id)
                    .defaultIfEmpty(new ScienceJournal())
                    .handle((scienceJournal1, sink) -> {
                        if (null == scienceJournal1.getName()) {
                            apiErrors.setMessage(notExistWithIdReply);
                            sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                        } else {
                            updatedscienceJournal.setId(id);
                            scienceJournalRepo.save(updatedscienceJournal)
                                    .subscribe(v -> sink.next(ResponseEntity.ok().body(v)),
                                            err -> {
                                                apiErrors.setMessage(err.getLocalizedMessage());
                                                sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                                            });
                        }
                    });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> deleteScienceJournal(@PathVariable Long id) {
        return scienceJournalRepo.findById(id)
                .defaultIfEmpty(new ScienceJournal())
                .handle((scienceJournal1, sink) -> {
                    if (null == scienceJournal1.getName()) {
                        apiErrors.setMessage(notExistWithIdReply);
                        sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                    } else {
                        scienceJournalRepo.deleteById(id)
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
