package issue17;

import com.sun.jersey.api.NotFoundException;
import com.wordnik.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 5/13/13
 */
@Path("/issue17")
@Api(value = "/issue17", description = "For issue 17")
@Produces({"application/json"})
public class IssueResource {
    @GET
    @Path("/{carId}")
    @ApiOperation(value = "For issue 17",responseClass = "issue17.Child")
    public Response getIssue17()
            throws NotFoundException {
        return Response.noContent().build();
    }
}
