package org.irmantas.booksstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;


@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(value = "antique_books")
public class AntiqueBook extends Book {
    protected int releaseYear;

    public AntiqueBook(String name, String author, String barcode, int qty, double price, int releaseYear) {
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
    public BigDecimal acquireTotalPrice() {
        double unmodifiedValue = super.acquireTotalPrice().doubleValue();
        double antiqueCoeficient = (LocalDate.now().getYear() - releaseYear) / 10;
        BigDecimal finalValue = BigDecimal.valueOf(unmodifiedValue * antiqueCoeficient);
        return finalValue.setScale(2, RoundingMode.UP);
    }

    String scienceIndexValidation() {
        if (releaseYear > 1900) {
            return "Invalid release year for antique book";
        } else {
            return "OK";
        }
    }

    @Override
    @SneakyThrows
    public Object updateField(String field, String fieldValue) {
        if (chekForFiel(field)) {
            if (fieldValue.matches("1[0-8][0-9]{2}")) {
                this.releaseYear = Integer.parseInt(fieldValue);
                return this;
            } else {
                return "Year value should contain only digits and should be between 1000 and 1900";
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
