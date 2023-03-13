package de.epet.demo.annotations;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

public class Api {

    @Path("/my-path")
    @POST
    public AbstractId restEndpointDestroyingBrokenIdWithBody(RequestBody body) {
        // Api method using AbstractId
        // and MyBrokenId is broken because it is also contained in RequestBody
        return new MyBrokenId("123");
    }

//        @Path("/my-other-path")
//        @POST
//        public MyNiceId restEndpointWithoutBody() {
//            // MyNiceId is not in RequestBody type and will not be overridden
//            // adding it here to use it somewhere and have it in the schema
//            return new MyNiceId("123");
//        }

}
