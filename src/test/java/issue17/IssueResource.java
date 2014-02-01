package issue17;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    @ApiOperation(value = "For issue 17",response = issue17.Child.class)
    public Response getIssue17()
            throws NotFoundException {
        return Response.noContent().build();
    }
}
