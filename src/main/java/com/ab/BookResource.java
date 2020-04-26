package com.ab;

import jersey.repackaged.com.google.common.util.concurrent.FutureCallback;
import jersey.repackaged.com.google.common.util.concurrent.Futures;
import jersey.repackaged.com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.jersey.server.ManagedAsync;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.util.Collection;

/**
 * @author Arpit Bhardwaj
 */
@Path("/books")
public class BookResource {

    @Context
    Request request;
    @Context BookDao bookDao;
    //BookDao bookDao = new BookDao();

    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Book> getBooks(){
        return bookDao.getBooks();
    }*/

    @GET
    //@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces({"application/json;qs=1","application/xml;qs=0.5"})
    @ManagedAsync
    public void getBooks(@Suspended final AsyncResponse response){
        //response.resume(bookDao.getBooks());
        ListenableFuture<Collection<Book>> listenableFuture = bookDao.getBooksAsync();
        Futures.addCallback(listenableFuture, new FutureCallback<Collection<Book>>() {
            @Override
            public void onSuccess(Collection<Book> books) {
                response.resume(books);
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    /*@Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Book getBook(@PathParam("id") String id){
        return bookDao.getBook(id);
    }*/

    @Path("/{id}")
    @GET
    //@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces({"application/json;qs=1","application/xml;qs=0.5"})
    @ManagedAsync
    public void getBook(@PathParam("id") String id,@Suspended final AsyncResponse response){
        //response.resume(bookDao.getBook(id));
        ListenableFuture<Book> listenableFuture = bookDao.getBookAsync(id);
        Futures.addCallback(listenableFuture, new FutureCallback<Book>() {
            @Override
            public void onSuccess(Book book) {
                //response.resume(book);
                EntityTag entityTag = generateEntityTag(book);
                Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(entityTag);
                if (responseBuilder != null){
                    response.resume(responseBuilder.build()); //304 Response
                }else{
                    response.resume(Response.ok().tag(entityTag).entity(book).build());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    /*@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Book addBook(Book book){
        return bookDao.addBook(book);
    }*/

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void addBook(@Valid @NotNull Book book, @Suspended final AsyncResponse response){
        //response.resume(bookDao.addBook(book));
        ListenableFuture<Book> listenableFuture = bookDao.addBookAsync(book);
        Futures.addCallback(listenableFuture, new FutureCallback<Book>() {
            @Override
            public void onSuccess(Book book) {
                response.resume(book);
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    EntityTag generateEntityTag(Book book){
        return new EntityTag(DigestUtils.md5Hex(book.getAuthor()
                +book.getTitle()
                +book.getPublished()
                +book.getExtras()));
    }
}
