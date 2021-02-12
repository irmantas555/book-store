package org.irmantas.booksstore.controllers;

import org.irmantas.booksstore.repositories.AntiqueBooksRepo;
import org.irmantas.booksstore.repositories.BooksRepo;
import org.irmantas.booksstore.repositories.ScienceJournalRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("totals")
public class TotalController {
    @Autowired
    BooksRepo booksRepo;

    @Autowired
    AntiqueBooksRepo antiqueBooksRepo;

    @Autowired
    ScienceJournalRepo scienceJournalRepo;

    @GetMapping("/price/matching/barcode/{value}")
    public Mono<ResponseEntity<Object>> getTotalPriceMatching(@PathVariable String value) {
        return booksRepo.findByBarcodeContaining(value)
                .concatWith(antiqueBooksRepo.findByBarcodeContaining(value))
                .concatWith(scienceJournalRepo.findByBarcodeContaining(value))
                .map(o -> o.getTotalPrice())
                .reduce(0.00, (total, currentValue) -> {
                    total = total + currentValue.doubleValue();
                    return total;
                })
                .map(aDouble -> ResponseEntity.ok().body(aDouble));
    }
}
