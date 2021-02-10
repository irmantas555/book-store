package org.irmantas.booksstore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.annotation.Generated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "books")
public class Book {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private long id;
 private String name;
 private String author;
 private long barcode;
 private int qty;
 private BigDecimal price;

 public Book(String name, String author, long barcode, int qty, BigDecimal price) {
  this.name = name;
  this.author = author;
  this.barcode = barcode;
  this.qty = qty;
  this.price = price;
 }
}
