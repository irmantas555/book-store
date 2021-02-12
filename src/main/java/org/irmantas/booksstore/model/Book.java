package org.irmantas.booksstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(value = "books")
public class Book implements BookHelpers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    protected String name;
    protected String author;
    protected String barcode;
    protected int qty;
    protected BigDecimal price;


    public double doublePriceFromBigDecimal() {
        return price.doubleValue();
    }

    public void bigDecimalPriceFromDouble(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public Book(String name, String author, String barcode, int qty, double price) {
        this.name = name;
        this.author = author;
        this.barcode = barcode;
        this.qty = qty;
        this.price = BigDecimal.valueOf(price);
    }

    public BigDecimal acquireTotalPrice(){
        return (BigDecimal.valueOf(price.doubleValue() * qty)).setScale(2, RoundingMode.UP);
    }

    public String validateBook() {
        String barcodeMatch = "[0-9]{13}";
        if (null == name || name.length() < 2) {
            return "Name should be at least 2 characters long";
        } else if (null == author || author.length() < 2) {
            return "Author should be at least 2 characters long";
        } else if (!barcode.matches(barcodeMatch)) {
            return "Invalid barcode, it shoulb only digits and 13 digits long";
        } else if (qty < 0) {
            return "Invalid quantity";
        } else if (null == price || price.doubleValue() <= 0.00) {
            return "Invalid price given";
        } else
            return "OK";
    }

    @SneakyThrows
    public Object updateField(String field, String fieldValue) {
        String barcodeMatch = "[0-9]{13}";
        String intMatch = "[1-2]{0,1}[0-9]{1,9}";
        String longMatch = "[0-9]{0,18}";
        String bigMatch = "[0-9]{0,18}[\\.]{0,1}[0-9]{0,18}";
        Field field1;
        if (chekForField(field)) {
            field1 = Book.class.getDeclaredField(field);
        } else {
            return "There is no such field";
        }
        field1.setAccessible(true);
        if (field1.getGenericType().getTypeName().equals("long")) {
            if (fieldValue.matches(longMatch)) {
                Long newLong = Long.valueOf(fieldValue);
                field1.setLong(this, newLong);
            } else {
                return "Price value should be digits";
            }
        } else if (field1.getGenericType().getTypeName().equals("int")) {
            if (fieldValue.matches(intMatch)) {
            Integer newInt = Integer.valueOf(fieldValue);
                field1.setInt(this, newInt);
            } else {
                return "Price value should be digits";
            }
        } else if (field1.getGenericType().getTypeName().equals("java.math.BigDecimal")) {
            if (fieldValue.matches(bigMatch)) {
                this.price = BigDecimal.valueOf(Double.parseDouble(fieldValue));
                this.price.setScale(2, RoundingMode.UP);
            } else {
                return "Price value should be digits and dot";
            }

        } else {
            if (fieldValue.length() > 1) {
            field1.set(this, fieldValue);
            } else {
                return "Value should be al least 2 characters long";
            }
        }
        String s = this.validateBook();
        if (s.equals("OK")) {
        return this;
        } else {
            return s;
        }
    }
    private boolean chekForField(String field) {
        long count = Arrays.stream(Book.class.getDeclaredFields()).filter(field1 -> field1.getName().equals(field)).count();
        return count > 0;
    }
}
