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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new BookApplication(bookDao);
    }

    @Before
    public void setUpBooks(){
        /*book_id1 = addBook("author1","title1",new Date(), "isbn1").readEntity(Book.class).getId();
        book_id2 = addBook("author2","title2",new Date(), "isbn2").readEntity(Book.class).getId();*/
        book_id1 = (String) deserializeResponseToMap(addBook("author1","title1",new Date(), "isbn1")).get("id");
        book_id2 = (String) deserializeResponseToMap(addBook("author2","title2",new Date(), "isbn2")).get("id");
    }
    @Test
    public void testAddBook(){
        Date thisDate = new Date();

        Response response = addBook("author","title",thisDate, "isbn");
        assertEquals(200, response.getStatus());

        /*Book readEntity = response.readEntity(Book.class);
        assertNotNull(readEntity.getId());
        assertEquals("title",readEntity.getTitle());
        assertEquals("author",readEntity.getAuthor());
        assertEquals(thisDate,readEntity.getPublished());
        assertEquals("isbn",readEntity.getIsbn());*/

        Map<String,Object> readEntity = deserializeResponseToMap(response);
        assertNotNull(readEntity.get("id"));
        assertEquals("title",readEntity.get("title"));
        assertEquals("author",readEntity.get("author"));
        /*SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss.SSSZ");
        try {
            assertEquals(thisDate,dateFormat.parse((String) readEntity.get("published")));
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        assertEquals("isbn",readEntity.get("isbn"));
    }
    protected Response addBook(String author, String title, Date published, String isbn, String... extras){
        /*Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublished(published);
        book.setIsbn(isbn);*/

        Map<String,Object> map = new HashMap<>();
        map.put("author",author);
        map.put("title",title);
        map.put("published",published);
        map.put("isbn", isbn);

        if (extras != null){
            int count = 1;
            for (String extra:
                 extras) {
                map.put("extra" + count++,extra);
            }
        }
        //Entity<Book> bookEntity = Entity.entity(book, MediaType.APPLICATION_JSON_TYPE);
        Entity<Map<String,Object>> bookEntity = Entity.entity(map, MediaType.APPLICATION_JSON_TYPE);
        return target("books").request().post(bookEntity);
    }
    @Test
    public void testGetBook(){
        //Book response = target("books").path(book_id1).request().get(Book.class);
        Map<String,Object> response = deserializeResponseToMap(target("books").path(book_id1).request().get());
        assertNotNull(response);
    }

    @Test
    public void testGetBooks(){
        //Collection<Book> response = target("books").request().get(new GenericType<Collection<Book>>() {});
        Collection<Map<String,Object>> response = target("books").request()
                .get(new GenericType<Collection<Map<String,Object>>>() {});
        assertEquals(2,response.size());
    }

    /*@Test
    public void testDao(){
        Book response1 = target("books").path("1").request().get(Book.class);
        Book response2 = target("books").path("1").request().get(Book.class);
        assertEquals(response1.getPublished().getTime(),response2.getPublished().getTime());
    }*/

    @Test
    public void testAddExtraField(){
        Response response = addBook("author","title",new Date(), "isbn","extra1");
        assertEquals(200, response.getStatus());
        Map<String,Object> readEntity = deserializeResponseToMap(response);
        assertNotNull(readEntity.get("id"));
        assertEquals("title",readEntity.get("title"));
        assertEquals("author",readEntity.get("author"));
        assertEquals("extra1",readEntity.get("extra1"));
    }

    public Map<String,Object> deserializeResponseToMap(Response response){
        return response.readEntity(new GenericType<Map<String, Object>>() {});
    }
}
