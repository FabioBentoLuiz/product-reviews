package org.globex.retail.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import org.everit.json.schema.ValidationException;
import org.globex.retail.service.KafkaService;

import org.globex.retail.validator.JsonSchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/review")
public class ProductReviewResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductReviewResource.class);

    @Inject
    JsonSchemaValidator validator;

    @Inject
    KafkaService kafkaService;

    
    


    @POST
    @Path("/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> submitReview(String payload) {
        return Uni.createFrom().item(() -> payload)//.onItem().invoke(p -> validator.validate(p))
                .onItem().invoke(p -> kafkaService.emit(p))
                .onItem().transform(p -> Response.status(200).build())
                .onFailure().recoverWithItem(throwable -> {
                    if (throwable instanceof ValidationException) {
                        LOGGER.error("Exception validating payload", throwable);
                        return Response.status(400, "Payload validation error").build();
                    } else {
                        LOGGER.error("Exception when processing payload", throwable);
                        return Response.status(500, "Processing error").build();
                    }
                });
    }

    }