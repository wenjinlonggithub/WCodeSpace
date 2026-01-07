package com.architecture.designpattern.iterator;

import java.util.ArrayList;
import java.util.List;

public class IteratorExample {
    
    public interface Iterator<T> {
        boolean hasNext();
        T next();
    }
    
    public interface Aggregate<T> {
        Iterator<T> createIterator();
    }
    
    public static class BookShelf implements Aggregate<Book> {
        private List<Book> books;
        
        public BookShelf() {
            this.books = new ArrayList<>();
        }
        
        public void addBook(Book book) {
            books.add(book);
        }
        
        @Override
        public Iterator<Book> createIterator() {
            return new BookIterator(books);
        }
        
        public int size() {
            return books.size();
        }
    }
    
    public static class Book {
        private String title;
        private String author;
        
        public Book(String title, String author) {
            this.title = title;
            this.author = author;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getAuthor() {
            return author;
        }
        
        @Override
        public String toString() {
            return "'" + title + "' by " + author;
        }
    }
    
    public static class BookIterator implements Iterator<Book> {
        private List<Book> books;
        private int index;
        
        public BookIterator(List<Book> books) {
            this.books = books;
            this.index = 0;
        }
        
        @Override
        public boolean hasNext() {
            return index < books.size();
        }
        
        @Override
        public Book next() {
            if (hasNext()) {
                return books.get(index++);
            }
            return null;
        }
    }
    
    public static class ReverseBookIterator implements Iterator<Book> {
        private List<Book> books;
        private int index;
        
        public ReverseBookIterator(List<Book> books) {
            this.books = books;
            this.index = books.size() - 1;
        }
        
        @Override
        public boolean hasNext() {
            return index >= 0;
        }
        
        @Override
        public Book next() {
            if (hasNext()) {
                return books.get(index--);
            }
            return null;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Iterator Pattern Demo ===\n");
        
        BookShelf bookShelf = new BookShelf();
        bookShelf.addBook(new Book("Design Patterns", "Gang of Four"));
        bookShelf.addBook(new Book("Clean Code", "Robert Martin"));
        bookShelf.addBook(new Book("Effective Java", "Joshua Bloch"));
        bookShelf.addBook(new Book("Java Concurrency in Practice", "Brian Goetz"));
        
        System.out.println("Books in normal order:");
        Iterator<Book> iterator = bookShelf.createIterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            System.out.println("- " + book);
        }
        
        System.out.println("\nBooks in reverse order:");
        Iterator<Book> reverseIterator = new ReverseBookIterator(
            ((BookIterator) bookShelf.createIterator()).books
        );
        while (reverseIterator.hasNext()) {
            Book book = reverseIterator.next();
            System.out.println("- " + book);
        }
        
        System.out.println("\nTotal books: " + bookShelf.size());
    }
}