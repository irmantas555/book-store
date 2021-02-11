package org.irmantas.booksstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "antique_books")
public class AntiqueBook extends Book{
    private int releaseYear;

    public AntiqueBook(String name, String author, String  barcode, int qty, double price, int releaseYear) {
        super(name, author, barcode, qty, price);
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
        System.out.println("unmodifiedValue = " + unmodifiedValue);
       double antiqueCoeficient = (LocalDate.now().getYear() - releaseYear) /10;
        System.out.println("antiqueCoeficient = " + antiqueCoeficient);
       BigDecimal finalVAlue = BigDecimal.valueOf(unmodifiedValue * antiqueCoeficient);
       return finalVAlue.setScale(2, RoundingMode.UP);
    }

    String scienceIndexValidation(){
        if (releaseYear > 1900){
            return "Invalid release year for antique book";
        } else {
            return "OK";
        }
    }
}
