package de.epet.demo.issue3.typeinfoinmixin;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
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

        // see the difference in between MyParent and MyOtherParent
        System.out.println("As you can see MyParent has a discriminator, whereas MyOtherParent does not.");
        System.out.println("When processing the MyOtherParent, swagger does not use the JsonTypeInfo annotation on the MixIn.");
        System.out.println("But it uses the JsonTypeInfo annotation placed directly on MyParent.");

        System.out.println();
        printSomeAnalysis();
    }

    public static class MyObjectMapperProcessor implements ObjectMapperProcessor {
        @Override
        public void processJsonObjectMapper(final ObjectMapper mapper) {
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.addMixIn(MyOtherParent.class, MyOtherParentMixin.class);
        }
    }

    static class Api {

        @Path("/my-path")
        @POST
        public MyChild restEndpointUsingOnlyChild(MyParent body) {
            // api uses Child and Parent
            return null;
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


    static class MyOtherParent {
        String id;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@idtype")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = MyOtherChild.class, name = "MyOtherChild")
    })
    abstract class MyOtherParentMixin {

        @JsonProperty("mixin-id-property")
        String id;
    }

    private static void printSomeAnalysis() throws JsonProcessingException {

        System.out.println("=======================\nSome more background:");

        MyObjectMapperProcessor omp = new MyObjectMapperProcessor();
        ObjectMapper om = new ObjectMapper();
        omp.processJsonObjectMapper(om);

        System.out.println("when serializing, jackson is using the JsonTypeInfo from Mixin and adding the @idtype property:");
        String json = om.writeValueAsString(new MyOtherChild("abc"));
        System.out.println(json);

        System.out.println();

        System.out.println("Swagger is using the jackson introspection to get properties, similar to the following:");
        JavaType jacksonType = om.getTypeFactory().constructSimpleType(MyOtherChild.class, null);
        BeanDescription beanDescription = om.getDeserializationConfig().introspect(jacksonType);
        List<BeanPropertyDefinition> properties = beanDescription.findProperties();
        System.out.println(properties);
        JavaType jacksonParentType = om.getTypeFactory().constructSimpleType(MyOtherParent.class, null);
        BeanDescription beanParentDescription = om.getDeserializationConfig().introspect(jacksonParentType);
        List<BeanPropertyDefinition> parentProperties = beanParentDescription.findProperties();
        System.out.println(parentProperties);
        System.out.println("So the @idtype is missing");
        System.out.println("-> we can see the mixin is applied in general, as the property is renamed by the MixIn");
        System.out.println("-> is this a bug in jackson (not respecting the annotation) or swagger (using the wrong method)?");
    }

}
