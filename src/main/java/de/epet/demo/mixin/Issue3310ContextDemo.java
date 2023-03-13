package de.epet.demo.mixin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class Issue3310ContextDemo {

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        om.addMixIn(AbstractId.class, AbstractIdMixin.class);

        System.out
                .println("when serializing, jackson is using the TypeInfo from Mixin and adding the @idtype property:");
        String json = om.writeValueAsString(new MyBrokenId("abc"));
        System.out.println(json);

        System.out.println();

        System.out.println(
                "Swagger is using the jackson introspection to get properties, which is not using the TypeInfo, so the @idtype is missing:");
        JavaType jacksonType = om.getTypeFactory().constructSimpleType(AbstractId.class, null);
        BeanDescription beanDescription = om.getDeserializationConfig().introspect(jacksonType);
        List<BeanPropertyDefinition> properties = beanDescription.findProperties();
        System.out.println(properties);
        System.out.println("-> we can see the mixin is applied in general, as the property is renamed by the mixin");
        System.out.println(
                "-> is this a bug in jackson (not respecting the annotation) or swagger (using the wrong method)?");

        System.out.println();

        System.out.println("this is already reported here: https://github.com/swagger-api/swagger-core/issues/3310");
        System.out.println(
                "we have a workaround (own ModelConverter) which is adding the discriminator to the parent type");
        System.out.println(
                "but the problem is, that the parent type is not referenced in some cases, see SwaggerDemo.java");
    }
}
