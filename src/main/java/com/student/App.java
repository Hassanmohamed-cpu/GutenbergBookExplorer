
package com.student;

import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {

    static Scanner input = new Scanner(System.in);

    // Fixed-size arrays (No ArrayList)
    static Book[] books = new Book[1000];
    static Book[] readingList = new Book[20];

    static int bookCount = 0;
    static int readingCount = 0;

    static final String WEBSITE =
            "https://books.toscrape.com/";

    // Book class
    static class Book {
        String title;
        String author;
        String price;
        String availability;
        String url;

        Book(String title, String author, String price,
             String availability, String url) {

            this.title = title;
            this.author = author;
            this.price = price;
            this.availability = availability;
            this.url = url;
        }
    }

    // Main method
    public static void main(String[] args) {

        System.out.println("==================================");
        System.out.println("     GUTENBERG BOOK EXPLORER");
        System.out.println("==================================");

        System.out.println("\nLoading books from website...");

        loadBooks();

        if (bookCount == 0) {
            System.out.println("No books could be loaded.");
            System.out.println("Please check your internet connection.");
        } else {
            System.out.println(bookCount + " books loaded successfully!");
        }

        int choice;

        do {
            displayMenu();

            System.out.print("Enter your choice: ");
            choice = readNumber();

            switch (choice) {

                case 1:
                    exploreBooks();
                    break;

                case 2:
                    System.out.print("Enter book title: ");
                    String title = input.nextLine();
                    searchBook(title);
                    break;

                case 3:
                    System.out.print("Enter author name: ");
                    String author = input.nextLine();
                    searchBook(author, true);
                    break;

                case 4:
                    exploreBooks();
                    addBook();
                    break;

                case 5:
                    viewReadingList();
                    break;

                case 6:
                    removeBook();
                    break;

                case 7:
                    displayBookDetails();
                    break;

                case 0:
                    System.out.println("\nThank you for using the program!");
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice! Try again.");
            }

        } while (choice != 0);

        input.close();
    }

    // Display menu
    static void displayMenu() {

        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Explore Books");
        System.out.println("2. Search Books by Title");
        System.out.println("3. Search Books by Author");
        System.out.println("4. Add to Reading List");
        System.out.println("5. View Reading List");
        System.out.println("6. Remove a Book");
        System.out.println("7. Display Book Details");
        System.out.println("0. Exit");
        System.out.println("===============================");
    }

    // Read and validate integer input
    static int readNumber() {

        while (!input.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            input.nextLine();
        }

        int number = input.nextInt();
        input.nextLine();

        return number;
    }

    // Load books from website using JSoup
    static void loadBooks() {

        for (int page = 1; page <= 5; page++) {

            try {

                String pageURL;

                if (page == 1) {
                    pageURL = WEBSITE;
                } else {
                    pageURL = WEBSITE + "catalogue/page-" + page + ".html";
                }

                Document doc = Jsoup.connect(pageURL)
                        .userAgent("Mozilla/5.0")
                        .timeout(15000)
                        .get();

                Elements bookElements =
                        doc.select("article.product_pod");

                for (Element element : bookElements) {

                    if (bookCount >= books.length) {
                        return;
                    }

                    String title = element.select("h3 a")
                            .attr("title");

                    String price = element.select(".price_color")
                            .text();

                    String availability =
                            element.select(".availability")
                                    .text().trim();

                    // CORRECTED URL EXTRACTION
                    Element linkElement =
                            element.selectFirst("h3 a");

                    String bookURL = "";

                    if (linkElement != null) {
                        bookURL = linkElement.absUrl("href");
                    }

                    // Website does not provide author names
                    String author = "Not listed";

                    Book book = new Book(
                            title,
                            author,
                            price,
                            availability,
                            bookURL
                    );

                    books[bookCount] = book;
                    bookCount++;
                }

            } catch (Exception e) {

                System.out.println(
                        "Could not load page " + page
                );
            }
        }
    }

    // Explore and display books
    static void exploreBooks() {

        if (bookCount == 0) {
            System.out.println("No books available.");
            return;
        }

        System.out.println("\n========== AVAILABLE BOOKS ==========");

        int limit = Math.min(bookCount, 10);

        for (int i = 0; i < limit; i++) {

            System.out.println("\nBook Number: " + (i + 1));
            System.out.println("Title: " + books[i].title);
            System.out.println("Price: " + books[i].price);
            System.out.println("Availability: "
                    + books[i].availability);
        }

        System.out.println("\nTotal books loaded: " + bookCount);
    }

    // Overloaded method 1: Search by title
    static void searchBook(String title) {

        boolean found = false;

        System.out.println("\n========== SEARCH RESULTS ==========");

        for (int i = 0; i < bookCount; i++) {

            if (books[i].title.toLowerCase()
                    .contains(title.toLowerCase())) {

                System.out.println("\nBook Number: " + (i + 1));
                System.out.println("Title: " + books[i].title);
                System.out.println("Price: " + books[i].price);

                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching books found.");
        }
    }

    // Overloaded method 2: Search by author or title
    static void searchBook(String keyword,
                           boolean searchByAuthor) {

        boolean found = false;

        if (searchByAuthor) {

            System.out.println("\nSearching by author...");

            for (int i = 0; i < bookCount; i++) {

                if (!books[i].author.equals("Not listed")
                        && books[i].author.toLowerCase()
                        .contains(keyword.toLowerCase())) {

                    System.out.println(books[i].title);
                    found = true;
                }
            }

            if (!found) {
                System.out.println(
                        "Author information is not provided "
                        + "by Books to Scrape."
                );
            }

        } else {
            searchBook(keyword);
        }
    }

    // Add a book to reading list
    static void addBook() {

        if (readingCount >= readingList.length) {
            System.out.println("Reading list is full!");
            return;
        }

        if (bookCount == 0) {
            System.out.println("No books available.");
            return;
        }

        System.out.print(
                "\nEnter book number to add to reading list: "
        );

        int number = readNumber();

        if (number < 1 || number > bookCount) {
            System.out.println("Invalid book number!");
            return;
        }

        Book selectedBook = books[number - 1];

        // Check for duplicate books
        for (int i = 0; i < readingCount; i++) {

            if (readingList[i].url.equals(selectedBook.url)) {

                System.out.println(
                        "This book is already in your reading list."
                );

                return;
            }
        }

        readingList[readingCount] = selectedBook;
        readingCount++;

        System.out.println(
                "Book added successfully to your reading list!"
        );
    }

    // Display reading list using for loop
    static void viewReadingList() {

        System.out.println("\n========== MY READING LIST ==========");

        if (readingCount == 0) {
            System.out.println("Your reading list is empty.");
            return;
        }

        for (int i = 0; i < readingCount; i++) {

            System.out.println("\n" + (i + 1) + ". "
                    + readingList[i].title);

            System.out.println("Price: "
                    + readingList[i].price);
        }

        System.out.println("\nTotal saved books: " + readingCount);
    }

    // Remove a book from reading list
    static void removeBook() {

        if (readingCount == 0) {
            System.out.println("Your reading list is empty.");
            return;
        }

        viewReadingList();

        System.out.print(
                "\nEnter the book number to remove: "
        );

        int number = readNumber();

        if (number < 1 || number > readingCount) {
            System.out.println("Invalid book number!");
            return;
        }

        String removedTitle = readingList[number - 1].title;

        for (int i = number - 1; i < readingCount - 1; i++) {
            readingList[i] = readingList[i + 1];
        }

        readingList[readingCount - 1] = null;
        readingCount--;

        System.out.println(
                removedTitle + " removed successfully!"
        );
    }

    // Display book details
    static void displayBookDetails() {

        if (bookCount == 0) {
            System.out.println("No books available.");
            return;
        }

        System.out.print(
                "\nEnter book number to display details: "
        );

        int number = readNumber();

        if (number < 1 || number > bookCount) {
            System.out.println("Invalid book number!");
            return;
        }

        Book selectedBook = books[number - 1];

        System.out.println("\n========== BOOK DETAILS ==========");
        System.out.println("Title: " + selectedBook.title);
        System.out.println("Author: " + selectedBook.author);
        System.out.println("Price: " + selectedBook.price);
        System.out.println("Availability: "
                + selectedBook.availability);
        System.out.println("URL: " + selectedBook.url);
        System.out.println("==================================");
    }
}
