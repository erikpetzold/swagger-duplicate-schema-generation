package de.epet.demo.issue1.ignoredparent;

import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;

import javax.ws.rs.POST;
import javax.ws.rs.Path;


public class SwaggerDemo {

    public static void main(String[] args) throws JsonProcessingException, OpenApiConfigurationException {

        // initialize swagger
        OpenApiContext context = new JaxrsOpenApiContextBuilder<>().openApiConfiguration(new SwaggerConfiguration()
                .objectMapperProcessorClass(MyObjectMapperProcessor.class.getName())
                .resourceClasses(Collections.singleton(Api.class.getName())).sortOutput(true)).buildContext(true);

        // generate openapi spec
        OpenAPI openAPI = context.read();
        String openapiYaml =
                context.getOutputYamlMapper().writer(new DefaultPrettyPrinter()).writeValueAsString(openAPI);
        System.out.println(openapiYaml);

        // see the difference in between MyChild and MyOtherChild
        System.out.println("As you can see MyOtherChild references the parent, whereas MyChild does not.");
        System.out.println("When processing the MyOtherParent, swagger ModelResolver also processes the Subtypes and generates them correctly.");
        System.out.println("But when the Parent is not used in the Api, it will not be generated and so the Child does not reference it.");

        // schema is not just different, it is wrong
        System.out.println("The problem is that the schema for MyChild is wrong, when sending a json with this schema, it cannot be parsed as the discriminator is missing.");
        System.out.println("See here:");
        ObjectMapper om = new ObjectMapper();
        MyChild myChild = om.readValue("""
                {
                    "id": "123",
                    "myChildProperty": 123
                }
                """, MyChild.class);
        System.out.println(myChild);

    }

    public static class MyObjectMapperProcessor implements ObjectMapperProcessor {
        @Override
        public void processJsonObjectMapper(final ObjectMapper mapper) {
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        }
    }

    static class Api {

        @Path("/my-path")
        @POST
        public void restEndpointUsingOnlyChild(MyChild body) {
            // api uses only MyChild, but not MyParent
        }

        @Path("/my-other-path")
        @POST
        public MyOtherChild otherRestEndpointUsingOtherChildAndParent(MyOtherParent body) {
            // api uses Child and Parent
            return null;
        }
    }

    static class MyChild extends MyParent {
        public MyChild(String value) {
            id = value;
        }

        int myChildProperty;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@idtype")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = MyChild.class, name = "MyChild")
    })
    static class MyParent {
        String id;
    }

    static class MyOtherChild extends MyOtherParent {
        public MyOtherChild(String value) {
            id = value;
        }

        int myOtherChildProperty;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@idtype")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = MyOtherChild.class, name = "MyOtherChild")
    })
    static class MyOtherParent {
        String id;
    }

}
