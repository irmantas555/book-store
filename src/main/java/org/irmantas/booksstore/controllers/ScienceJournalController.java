package org.irmantas.booksstore.controllers;


import org.irmantas.booksstore.exceptions.ApiErrors;
import org.irmantas.booksstore.model.ScienceJournal;
import org.irmantas.booksstore.repositories.ScienceJournalRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.irmantas.booksstore.model.ErrMsg.*;

@RestController
@RequestMapping("science/journals")
public class ScienceJournalController {
    @Autowired
    ControllersUtils controllersUtils;

    @Autowired
    ScienceJournalRepo scienceJournalRepo;

    @Autowired
    ApiErrors apiErrors;

    @GetMapping("")
    public Flux<ScienceJournal> getAllBooks() {
        return scienceJournalRepo.findAll();
    }

    @PostMapping("")
    public Mono<ResponseEntity<ScienceJournal>> postBook(@RequestBody ScienceJournal scienceJournal) {
        String validationMessage = scienceJournal.validateBook();
        if (!validationMessage.equals("OK")) {
            return getResponseErrorMono(validationMessage);
        } else {
            return scienceJournalRepo.save(scienceJournal)
                    .map(scienceJournal1 -> ResponseEntity.ok().body (scienceJournal1));
        }
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ScienceJournal>> getBooksById(@PathVariable Long id) {
        return Mono.just(id)
                .map(Math::abs)
                .flatMap(aLong -> scienceJournalRepo.findById(id))
                .onErrorResume(e -> getBookErrorMono(DB_OPERATION_FAILED))
                .map(scienceJournal1 -> ResponseEntity.ok().body(scienceJournal1))
                .switchIfEmpty(getResponseErrorMono(NO_ENTITY_WITH_ID));
    }

    @GetMapping("/barcode/{value}")
    public Mono<ResponseEntity<ScienceJournal>> getBooksByBarcode(@PathVariable String value) {
        return Mono.just(value)
                .filter(val -> val.matches("[0-9]{13}"))
                .flatMap(val -> scienceJournalRepo.findByBarcode(value))
                .map(scienceJournal1 -> ResponseEntity.ok().body(scienceJournal1))
                .switchIfEmpty(getResponseErrorMono(NO_ENTITY_WITH_BARCODE));
    }

    @PutMapping("/barcode/{barcodeValue}/{field}/{fieldValue}")
    public Mono<ResponseEntity<ScienceJournal>> updateValuesByBarcode(@PathVariable String barcodeValue,
                                                            @PathVariable String field,
                                                            @PathVariable String fieldValue
    ) {
        if (!barcodeValue.matches("[0-9]{13}")) {
            return getResponseErrorMono(BARCODE_NOT_VALID);
        } else if (!controllersUtils.getBookClassesFieldList().contains(field)) {
            return getResponseErrorMono(NO_SUCH_FIELD);
        } else {
            return scienceJournalRepo.findByBarcode(barcodeValue)
                    .switchIfEmpty(getBookErrorMono(NO_ENTITY_WITH_BARCODE))
                    .flatMap(scienceJournal -> {
                        Object validation = scienceJournal.updateField(field, fieldValue);
                        if (validation instanceof String) {
                            return getResponseErrorMono((String) validation);
                        } else {
                            return Mono.just(scienceJournal);
                        }
                    })
                    .flatMap(scienceJournal -> scienceJournalRepo.save( (ScienceJournal ) scienceJournal))
                    .map(scienceJournal -> ResponseEntity.ok().body(scienceJournal))
                    .switchIfEmpty(getResponseErrorMono(DB_OPERATION_FAILED));
        }
    }

    @GetMapping("/barcode/match/{value}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<ScienceJournal> getBarcodeMatch(@PathVariable String value) {
        if (!value.matches("[0-9]+")) {
            return getBookErrorFlux(BARCODE_NOT_VALID);
        } else {
            return scienceJournalRepo.findByBarcodeContaining(value)
                    .onErrorResume(e -> getBookErrorFlux(DB_OPERATION_FAILED))
                    .switchIfEmpty(getBookErrorFlux(NO_ENTITY_WITH_ID));
        }
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ScienceJournal>> updateBookById(@PathVariable Long id, @RequestBody ScienceJournal updatedscienceJournal) {
        String validationMessage = updatedscienceJournal.validateBook();
        if (!validationMessage.equals("OK")) {
            return getResponseErrorMono(NO_ENTITY_WITH_ID);
        } else
            return scienceJournalRepo.findById(id)
                    .switchIfEmpty(getBookErrorMono(NO_ENTITY_WITH_ID))
                    .flatMap(scienceJournal -> {
                        updatedscienceJournal.setId(id);
                        return scienceJournalRepo.save(updatedscienceJournal);
                    })
                    .map(scienceJournalMono -> ResponseEntity.ok().body(scienceJournalMono));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteBook(@PathVariable Long id) {
        if (!String.valueOf(id).matches("[0-9]+")) {
            return Mono.just(ResponseEntity.badRequest().body(NO_ENTITY_WITH_ID));
        } else
            return scienceJournalRepo.findById(id)
                    .switchIfEmpty(getBookErrorMono(NO_ENTITY_WITH_ID))
                    .then(scienceJournalRepo.deleteById(id))
                    .thenReturn("Ok")
                    .map(v -> ResponseEntity.ok().body("Entity with id " + id + " deleted"));
    }

    Mono<ScienceJournal> getBookErrorMono(String message) {
        apiErrors.setMessage(message);
        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

    Flux<ScienceJournal> getBookErrorFlux(String message) {
        apiErrors.setMessage(message);
        return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

    ResponseEntity<ScienceJournal> getRespBook(String message) {
        apiErrors.setMessage(message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    Mono<ResponseEntity<ScienceJournal>> getResponseErrorMono(String message) {
        apiErrors.setMessage(message);
        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }
}
