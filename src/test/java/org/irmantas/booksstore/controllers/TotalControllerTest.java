package org.irmantas.booksstore.controllers;

import org.irmantas.booksstore.exceptions.ApiErrors;
import org.irmantas.booksstore.repositories.AntiqueBooksRepo;
import org.irmantas.booksstore.repositories.BooksRepo;
import org.irmantas.booksstore.repositories.ScienceJournalRepo;
import org.irmantas.booksstore.util.LoadFlux;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@ActiveProfiles("test")
@Import({BooksRepo.class, ApiErrors.class})
class TotalControllerTest {

    @InjectMocks
    private TotalController totalController;

    LoadFlux bookStorage = new LoadFlux();

    @Autowired
    WebTestClient testClient;

    @MockBean
    BooksRepo booksRepo;

    @MockBean
    AntiqueBooksRepo antiqueBooksRepo;

    @MockBean
    ScienceJournalRepo scienceJournalRepo;

    @MockBean
    ControllersUtils controllersUtils;

    @Autowired
    ApiErrors apiErrors;


    @BeforeEach
    private void setUp(){
        BDDMockito.when(booksRepo.findByBarcodeContaining("321")).
                thenReturn(bookStorage.getBookFluxByBarcode());
        BDDMockito.when(antiqueBooksRepo.findByBarcodeContaining("321")).
                thenReturn(bookStorage.getAntiqueBookFluxByBarcode());
        BDDMockito.when(scienceJournalRepo.findByBarcodeContaining("321")).
                thenReturn(bookStorage.getScienceJournalFluxByBarcode());
    }

    @Test
    @DisplayName("Get tolal price if succesfull")
    public void testTotalPrice(){
        testClient
                .get()
                .uri("/totals/price/matching/barcode/321")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .equals("4100");


    }



}