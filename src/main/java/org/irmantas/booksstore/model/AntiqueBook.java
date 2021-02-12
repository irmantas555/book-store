package org.irmantas.booksstore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@Table(value = "antique_books")
public class AntiqueBook extends Book{
    private int releaseYear;

    public AntiqueBook(String name, String author, String  barcode, int qty, double price, int releaseYear) {
        super(name, author, barcode, qty, price);
        this.releaseYear = releaseYear;
    }

    public AntiqueBook(long id, String name, String author, String barcode, int qty, BigDecimal price, int releaseYear) {
        super(id, name, author, barcode, qty, price);
        this.releaseYear = releaseYear;
    }

    public AntiqueBook(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    @Override
    public String validateBook() {
        String validation = super.validateBook();
        if (!validation.equals("OK")) {
            return validation;
        } else {
            return scienceIndexValidation();
        }
    }

    @Override
    public BigDecimal getTotalPrice() {
       double unmodifiedValue = super.getTotalPrice().doubleValue();
       double antiqueCoeficient = (LocalDate.now().getYear() - releaseYear) /10;
       BigDecimal finalValue = BigDecimal.valueOf(unmodifiedValue * antiqueCoeficient);
       return finalValue.setScale(2, RoundingMode.UP);
    }

    String scienceIndexValidation(){
        if (releaseYear > 1900){
            return "Invalid release year for antique book";
        } else {
            return "OK";
        }
    }
}
