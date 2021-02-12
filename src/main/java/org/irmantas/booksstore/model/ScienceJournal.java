package org.irmantas.booksstore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@Table(value = "science_journals")
public class ScienceJournal extends Book{
    private int scienceIndex;

    public ScienceJournal(String name, String author, String barcode, int qty, double price, int scienceIndex) {
        super(name, author, barcode, qty, price);
        this.scienceIndex = scienceIndex;
    }

    public ScienceJournal(long id, String name, String author, String barcode, int qty, BigDecimal price, int scienceIndex) {
        super(id, name, author, barcode, qty, price);
        this.scienceIndex = scienceIndex;
    }

    public ScienceJournal(int scienceIndex) {
        this.scienceIndex = scienceIndex;
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

    String scienceIndexValidation(){
        if (scienceIndex < 1 || scienceIndex > 10){
            return "Invalid science index";
        } else {
            return "OK";
        }
    }

    @Override
    public BigDecimal getTotalPrice() {
        double unmodifiedValue = super.getTotalPrice().doubleValue();
        BigDecimal finalVAlue = BigDecimal.valueOf(unmodifiedValue * scienceIndex);
        return finalVAlue.setScale(2, RoundingMode.UP);
    }

}
