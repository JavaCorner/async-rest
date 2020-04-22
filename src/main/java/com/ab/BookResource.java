package com.ab;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * @author Arpit Bhardwaj
 */
@Path("/books")
public class BookResource {

    @Context BookDao bookDao;
    //BookDao bookDao = new BookDao();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Book> getBooks(){
        return bookDao.getBooks();
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Book getBook(@PathParam("id") String id){
        return bookDao.getBook(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Book addBook(Book book){
        return bookDao.addBook(book);
    }

}
