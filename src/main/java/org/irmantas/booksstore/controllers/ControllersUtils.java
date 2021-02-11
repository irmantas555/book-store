package org.irmantas.booksstore.controllers;

import org.irmantas.booksstore.model.AntiqueBook;
import org.irmantas.booksstore.model.Book;
import org.irmantas.booksstore.model.ScienceJournal;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ControllersUtils {
    private List<String> bookClassesFieldList;

    @PostConstruct
     void getFields() {
        Stream<Field> stream = Arrays.stream(Book.class.getDeclaredFields());
        Stream<Field> stream1 = Arrays.stream(AntiqueBook.class.getDeclaredFields());
        Stream<Field> stream2 = Arrays.stream(ScienceJournal.class.getDeclaredFields());
        bookClassesFieldList = Stream.concat(Stream.concat(stream, stream1), stream2)
                .map(field -> field.getName())
                .distinct()
                .collect(Collectors.toList());
    }

    public  List<String> getBookClassesFieldList() {
        return bookClassesFieldList;
    }



}
