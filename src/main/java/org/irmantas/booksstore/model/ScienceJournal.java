package org.irmantas.booksstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
        if (this.scienceIndex < 1 || this.scienceIndex > 10){
            return "Invalid science index";
        } else {
            return "OK";
        }
    }

    @Override
    public BigDecimal acquireTotalPrice() {
        double unmodifiedValue = super.acquireTotalPrice().doubleValue();
        BigDecimal finalVAlue = BigDecimal.valueOf(unmodifiedValue * scienceIndex);
        return finalVAlue.setScale(2, RoundingMode.UP);
    }

    @Override
    @SneakyThrows
    public Object updateField(String field, String fieldValue) {
        if (chekForFiel(field)) {
            if (fieldValue.matches("[0-9][0]*") ) {
                this.scienceIndex = Integer.parseInt(fieldValue);
                return this;
            } else {
                return "Vaue should contain digit and should be between 1 and 10 ";
            }
        } else {
            return super.updateField(field, fieldValue);
        }
    }

    private boolean chekForFiel(String field) {
        long count = Arrays.stream(this.getClass().getDeclaredFields()).filter(field1 -> field1.getName().equals(field)).count();
        return count > 0;
    }

}
