package de.epet.demo.issue2.overriddenschema;

import java.util.Collections;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

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

        // now MyOtherChild is also wrong because it is generated second time overriding the correct schema
        System.out.println("As you can see MyOtherChild now also does not reference the parent.");
        System.out.println("The only change was to exchange MyOtherChild and MyOtherParent, so change return type and parameter.");
        System.out.println("When processing the MyOtherParent, swagger ModelResolver also processes the Subtypes and generates them correctly.");
        System.out.println("But then when processing the requestBody, a new ModelConverterContextImpl is created and processedTypes is empty.");
        System.out.println("As requestBody contains MyOtherChild, the schema for MyOtherChild is generated again, but without the parent reference.");
        System.out.println("This newly created schema is tehn overriding the existing correct one.");
        System.out.println("If issue 1 was fixed, then this would not be visible as it would be regenerated correctly.");
        System.out.println("But in my opinion it is already a bug that it is generated again and overrides the existing schema.");
        System.out.println("The result is that MyOtherChild is now also missing the @idtype discriminator.");
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
        public MyOtherParent otherRestEndpointUsingOtherChildAndParent(MyOtherChild body) {
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
