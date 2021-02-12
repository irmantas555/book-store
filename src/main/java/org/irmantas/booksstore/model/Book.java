package org.irmantas.booksstore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "books")
public class Book implements BookHelpers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String author;
    private String barcode;
    private int qty;
    private BigDecimal price;

    public double getDoublePrice() {
        return price.doubleValue();
    }

    public void setDoublePrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public Book(String name, String author, String barcode, int qty, double price) {
        this.name = name;
        this.author = author;
        this.barcode = barcode;
        this.qty = qty;
        this.price = BigDecimal.valueOf(price);
    }

    public BigDecimal getTotalPrice(){
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
        Field field1 = this.getClass().getDeclaredField(field);
//        field1.setAccessible(true);
        if (field1.getGenericType().getTypeName().equals("long")) {
            Long newLong = Long.valueOf(fieldValue);
            if (null != newLong) {
                field1.setLong(this, newLong);
            }
        } else if (field1.getGenericType().getTypeName().equals("int")) {
            Integer newInt = Integer.valueOf(fieldValue);
            if (null != newInt) {
                field1.setInt(this, newInt);
            }
        } else if (field1.getGenericType().getTypeName().equals("java.lang.BigDecimal")) {
           this.price = BigDecimal.valueOf(Double.parseDouble(fieldValue));
           this.price.setScale(2, RoundingMode.UP);

        } else {
            field1.set(this, fieldValue);
        }
        String s = this.validateBook();
        if (s.equals("OK")) {
        return this;
        } else {
            return s;
        }
    }
}
