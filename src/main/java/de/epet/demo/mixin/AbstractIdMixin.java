package de.epet.demo.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@idtype")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MyBrokenId.class, name = "MyBrokenId"),
        @JsonSubTypes.Type(value = MyNiceId.class, name = "MyNiceId")
})
abstract class AbstractIdMixin {

    @JsonProperty("mixin-id-property")
    String id;

}
