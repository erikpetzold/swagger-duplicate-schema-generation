package de.epet.demo.annotations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@idtype")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MyBrokenId.class, name = "MyBrokenId"),
        @JsonSubTypes.Type(value = MyNiceId.class, name = "MyNiceId")
})
public class AbstractId {
    String id;
}
