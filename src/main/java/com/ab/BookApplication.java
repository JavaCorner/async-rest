package com.ab;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Arpit Bhardwaj
 */
public class BookApplication extends ResourceConfig {

    BookApplication(final BookDao bookDao){
        JacksonJsonProvider jsonProvider = new JacksonJsonProvider()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true);

        packages("com.ab");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(bookDao).to(BookDao.class);
            }
        });
        register(jsonProvider);
    }
}
