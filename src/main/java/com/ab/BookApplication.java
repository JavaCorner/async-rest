package com.ab;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Arpit Bhardwaj
 */
public class BookApplication extends ResourceConfig {

    BookApplication(final BookDao bookDao){
        packages("com.ab");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(bookDao).to(BookDao.class);
            }
        });
    }
}
