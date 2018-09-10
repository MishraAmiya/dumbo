package ir.sahab.nimbo.jimbo.main;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/entryPoint")
public class EntryPoint {

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test";
    }

    @GET
    @Path("/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public Greeting hello(@PathParam("param") String name) {
        return new Greeting(name);
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String helloUsingJson(Greeting greeting) {
        return greeting.getMessage() + "\n";
    }
}
