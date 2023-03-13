package de.epet.demo.mixin;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

class Api {

    @Path("/my-path")
    @POST
    public AbstractId restEndpointDestroyingBrokenIdWithBody(RequestBody body) {
        // Api method using AbstractId
        // and MyBrokenId is broken because it is also contained in RequestBody
        return new MyBrokenId("123");
    }

}
