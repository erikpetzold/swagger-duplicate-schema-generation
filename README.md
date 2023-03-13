Demo project to reproduce bugs in swagger.

## Issue 1: parent class is ignored when not explicitly used in api

To see the example, just run [SwaggerDemo.java](src/main/java/de/epet/demo/issue1/ignoredparent/SwaggerDemo.java)

When generating a schema for a Class, the Superclass is ignored if is not used itself in the Api.

## Issue 2: schemas are generated multiple times, overriding correct versions

To see the example, just run [SwaggerDemo.java](src/main/java/de/epet/demo/issue2/overriddenschema/SwaggerDemo.java)

When the Superclass is used in the Api it generates the subclasses correctly, but these are then overridden again with wrong schema.
This is because return type and arguments of the api method are processed separately and the context with already generated schemas is not shared.

## Issue 3: @JsonTypeInfo is ignored

To see the example, just run [SwaggerDemo.java](src/main/java/de/epet/demo/issue3/typeinfoinmixin/SwaggerDemo.java)

When the mapping information is placed on a MixIn, then the JsonTypeInfo is ignored.

issue already exists: https://github.com/swagger-api/swagger-core/issues/3310



