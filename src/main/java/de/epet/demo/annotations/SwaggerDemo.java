package de.epet.demo.annotations;

import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
        System.out.println("As you can see MyNiceId references the AbstractId, whereas MyBrokenId does not.");
        System.out.println("When processing the AbstractId, swagger ModelResolver also processes the Subtypes and generates them correctly.");
        System.out.println("But then when processing the requestBody, a new ModelConverterContextImpl is created and processedTypes is empty.");
        System.out.println("As RequestBody contains MyBrokenId, the schema for MyBrokenId is generated again, but without the parent reference.");
        System.out.println("We need the parent reference as we have additional information on the parent (which we need to add manually because of 3310, see Issue3310ContextDemo.java).");
        System.out.println();
        System.out.println("The same problem occurs, if AbstractId itself is not used within the Api at all.");
        System.out.println("Then the parent class is not processed at all, only the child class MyBrokenId is converted and Mixin from parent is ignored.");
        System.out.println("This can be tested by removing the return type in Api.java (the return was only added to have the AbstractId processed at all).");

    }

    public static class MyObjectMapperProcessor implements ObjectMapperProcessor {
        @Override
        public void processJsonObjectMapper(final ObjectMapper mapper) {
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        }
    }

}
