package com.ab;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

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

    protected void configureClient(ClientConfig clientConfig){
        JacksonJsonProvider jsonProvider = new JacksonJsonProvider()
                .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        clientConfig.register(jsonProvider);
        clientConfig.connectorProvider(new GrizzlyConnectorProvider());
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

    @Test
    public void testGetBookAsXml(){
        String response = target("books").request(MediaType.APPLICATION_XML).get().readEntity(String.class);
        XML xml = new XMLDocument(response);

        assertNotNull(response);
        assertEquals("title1",xml.xpath("/books/book[@id ='"+book_id1+"']/title/text()").get(0));
        assertEquals("author1",xml.xpath("/books/book[@id ='"+book_id1+"']/author/text()").get(0));
    }

    public Map<String,Object> deserializeResponseToMap(Response response){
        return response.readEntity(new GenericType<Map<String, Object>>() {});
    }
    @Test
    public void addBookNoAuthor(){
        Response response = addBook(null,"title",new Date(), "isbn");
        assertEquals(400, response.getStatus());
        String message = response.readEntity(String.class);
        assertTrue(message.contains("author is a required field"));
    }

    @Test
    public void addBookNoTitle(){
        Response response = addBook("author",null,new Date(), "isbn");
        assertEquals(400, response.getStatus());
        String message = response.readEntity(String.class);
        assertTrue(message.contains("title is a required field"));
    }

    @Test
    public void addBookNoBook(){
        Response response = target("books").request().post(null);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void booknotFoundWithMessage(){
        Response response = target("books").path("1").request().get();
        assertEquals(404, response.getStatus());
        String message = response.readEntity(String.class);
        assertTrue(message.contains("Book 1 is not found"));
    }

    @Test
    public void bookEntityTagNotModified(){
        EntityTag entityTag = target("books").path(book_id1).request().get().getEntityTag();
        assertNotNull(entityTag);
        Response response = target("books").path(book_id1).request().header("if-None-Match", entityTag).get();
        assertEquals(304,response.getStatus());
    }

    @Test
    public void updateBookAuthor(){
        Map<String,Object> updates = new HashMap<>();
        updates.put("author", "updatedAuthor");
        Entity<Map<String,Object>> updateEntity = Entity.entity(updates, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("books").path(book_id1).request()
                .build("PATCH", updateEntity).invoke();
        assertEquals(200, response.getStatus());

        Response getResponse = target("books").path(book_id1).request().get();
        Map<String,Object> readEntity = deserializeResponseToMap(getResponse);
        assertEquals("updatedAuthor",readEntity.get("author"));
    }

    @Test
    public void updateIfMatch(){
        EntityTag entityTag = target("books").path(book_id1).request().get().getEntityTag();

        Map<String,Object> updates = new HashMap<>();
        updates.put("author", "updatedAuthor");
        Entity<Map<String,Object>> updateEntity = Entity.entity(updates, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("books").path(book_id1).request()
                .header("If-Match",entityTag)
                .build("PATCH", updateEntity).invoke();
        assertEquals(200, response.getStatus());

        Response updateResponse = target("books").path(book_id1).request()
                .header("If-Match",entityTag)
                .build("PATCH", updateEntity).invoke();
        assertEquals(412, updateResponse.getStatus());
    }

    @Test
    public void patchMethodOverride(){
        Map<String,Object> updates = new HashMap<>();
        updates.put("author", "updatedAuthor");
        Entity<Map<String,Object>> updateEntity = Entity.entity(updates, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("books").path(book_id1)
                .queryParam("_method", "PATCH")
                .request()
                .post(updateEntity);
        assertEquals(200, response.getStatus());

        Response getResponse = target("books").path(book_id1).request().get();
        Map<String,Object> readEntity = deserializeResponseToMap(getResponse);
        assertEquals("updatedAuthor",readEntity.get("author"));
    }

    @Test
    public void contentNegotiationExtension(){
        Response getXmlResponse = target("books").path(book_id1+".xml").request().get();
        assertEquals(MediaType.APPLICATION_XML_TYPE, getXmlResponse.getHeaderString("Content-Type"));

        Response getJsonResponse = target("books").path(book_id1+".json").request().get();
        assertEquals(MediaType.APPLICATION_JSON_TYPE, getJsonResponse.getHeaderString("Content-Type"));

    }
}
