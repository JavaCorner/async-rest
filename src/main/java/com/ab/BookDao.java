package com.ab;

import jersey.repackaged.com.google.common.util.concurrent.ListenableFuture;
import jersey.repackaged.com.google.common.util.concurrent.ListeningExecutorService;
import jersey.repackaged.com.google.common.util.concurrent.MoreExecutors;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * @author Arpit Bhardwaj
 */
public class BookDao {

    private Map<String,Book> books;
    private ListeningExecutorService executorService;

    public BookDao() {
        /*books = new HashMap<>();

        Book book1 = new Book();
        book1.setId("1");
        book1.setTitle("Title1");
        book1.setAuthor("Author1");
        book1.setIsbn("1234");
        book1.setPublished(new Date());

        Book book2 = new Book();
        book2.setId("2");
        book2.setTitle("Title2");
        book2.setAuthor("Author2");
        book2.setIsbn("2345");
        book2.setPublished(new Date());

        books.put(book1.getId(),book1);
        books.put(book2.getId(),book2);*/

        books = new ConcurrentHashMap<>();
        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    }

    public Collection<Book> getBooks(){
        return books.values();
    }

    public ListenableFuture<Collection<Book>> getBooksAsync(){
        ListenableFuture<Collection<Book>> listenableFuture = executorService.submit(new Callable<Collection<Book>>() {
            @Override
            public Collection<Book> call() throws Exception {
                return getBooks();
            }
        });
        return listenableFuture;
    }

    public Book getBook(String id) throws BookNotFoundException{
        if (books.containsKey(id)){
            return books.get(id);
        }
        else{
            throw new BookNotFoundException("Book " + id + " is not found");
        }

    }

    public ListenableFuture<Book> getBookAsync(final String id){
        ListenableFuture<Book> listenableFuture = executorService.submit(new Callable<Book>() {
            @Override
            public Book call() throws Exception {
                return getBook(id);
            }
        });
        return listenableFuture;
    }

    public Book addBook(Book book){
        book.setId(UUID.randomUUID().toString());
        books.put(book.getId(),book);
        return book;
    }

    public ListenableFuture<Book> addBookAsync(final Book book){
        ListenableFuture<Book> listenableFuture = executorService.submit(new Callable<Book>() {
            @Override
            public Book call() throws Exception {
                return addBook(book);
            }
        });
        return listenableFuture;
    }
}
