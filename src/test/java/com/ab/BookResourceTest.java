package com.ab;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arpit Bhardwaj
 */
public class BookResourceTest extends JerseyTest {
    private String book_id1;
    private String book_id2;

    protected Application configure(){
        final BookDao bookDao = new BookDao();
        //enable(TestProperties.LOG_TRAFFIC);
        //enable(TestProperties.DUMP_ENTITY);
        return new BookApplication(bookDao);
    }

    @Before
    public void setUpBooks(){
        book_id1 = addBook("author1","title1",new Date(), "isbn1").readEntity(Book.class).getId();
        book_id2 = addBook("author2","title2",new Date(), "isbn2").readEntity(Book.class).getId();
    }
    @Test
    public void testAddBook(){

        Response response = addBook("author","title",new Date(), "isbn");
        assertEquals(200, response.getStatus());
        Book readEntity = response.readEntity(Book.class);
        assertNotNull(readEntity.getId());
        assertEquals("title",readEntity.getTitle());
        assertEquals("author",readEntity.getAuthor());

    }
    protected Response addBook(String author, String title, Date published, String isbn){
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublished(published);
        book.setIsbn(isbn);

        Entity<Book> bookEntity = Entity.entity(book, MediaType.APPLICATION_JSON_TYPE);
        return target("books").request().post(bookEntity);
    }
    @Test
    public void testGetBook(){
        Book response = target("books").path(book_id1).request().get(Book.class);
        assertNotNull(response);
    }

    @Test
    public void testGetBooks(){
        Collection<Book> response = target("books").request().get(new GenericType<Collection<Book>>() {});
        assertEquals(2,response.size());
    }

    /*@Test
    public void testDao(){
        Book response1 = target("books").path("1").request().get(Book.class);
        Book response2 = target("books").path("1").request().get(Book.class);
        assertEquals(response1.getPublished().getTime(),response2.getPublished().getTime());
    }*/

}
