/**
 * Copyright 2014 Reverb Technologies, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wordnik.jaxrs;

import com.google.common.collect.Lists;
import com.wordnik.sample.JavaRestResourceUtil;
import com.wordnik.sample.data.StoreData;
import com.wordnik.sample.exception.NotFoundException;
import com.wordnik.sample.model.Order;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/store")
@Api(value = "/store")
@Produces({"application/json", "application/xml"})
public class PetStoreResource {
    static StoreData storeData = new StoreData();
    static JavaRestResourceUtil ru = new JavaRestResourceUtil();

    @GET
    @Path("/order/{orderId}")
    @ApiOperation(value = "Find purchase order by ID",
            notes = "For valid response try integer IDs with value <= 5 or > 10. Other values will generated exceptions",
            response = Order.class)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Order not found")})
    public Response getOrderById(
            @ApiParam(hidden = true, value = "this is a hidden parameter", required = false) @QueryParam("hiddenParam") String hiddenParam,
            @ApiParam(value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]", required = true) @PathParam("orderId") String orderId)
            throws NotFoundException {
        Order order = storeData.findOrderById(ru.getLong(0, 10000, 0, orderId));
        if (order != null) {
            return Response.ok().entity(order).build();
        } else {
            throw new NotFoundException(404, "Order not found");
        }
    }

    @GET
    @Path("/orders/{orderIds}")
    @ApiOperation(value = "Find multiple purchase orders by IDs",
            notes = "For valid response try integer IDs with value <= 5 or > 10. Other values will generated exceptions",
            responseContainer = "List", response = Order.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Order not found") })
    public Response getOrdersById(
            @ApiParam(value = "IDs of pets that needs to be fetched",
                    required = true) @PathParam("orderIds") List<String> orderIds)
            throws com.wordnik.sample.exception.NotFoundException {
        List<Order> orders = Lists.newArrayList();
        for (String orderId : orderIds) {
            Order order = storeData.findOrderById(ru.getLong(0, 10000, 0, orderId));
            if (order != null) {
                orders.add(order);
            } else {
                throw new NotFoundException(404, "Order #" + orderId + " not found");
            }

        }
        return Response.ok().entity(orders).build();
    }

    @POST
    @Path("/order")
    @ApiOperation(value = "Place an order for a pet")
    @ApiResponses({@ApiResponse(code = 400, message = "Invalid Order")})
    public Order placeOrder(
            @ApiParam(value = "order placed for purchasing the pet",
                    required = true) Order order) {
        storeData.placeOrder(order);
        return storeData.placeOrder(order);
    }

    @POST
    @Path("/pingPost")
    @ApiOperation(value = "Simple ping endpoint")
    @ApiResponses({@ApiResponse(code = 200, message = "Successful request - see response for 'pong'", response = String.class)})
    public String pingPost() {
        return "pingPost";
    }

    @PUT
    @Path("/pingPut")
    @ApiOperation(value = "Simple ping endpoint")
    @ApiResponses({@ApiResponse(code = 200, message = "Successful request - see response for 'pong'", response = String.class)})
    public String pingPut() {
        return "pong";
    }

    @GET
    @Path("/pingGet")
    @ApiOperation(value = "Simple ping endpoint")
    @ApiResponses({@ApiResponse(code = 200, message = "Successful request - see response for 'pong'", response = String.class)})
    public String pingGet() {
        return "pong";
    }

    @DELETE
    @Path("/order/{orderId}")
    @ApiOperation(value = "Delete purchase order by ID",
            notes = "For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers will generate API errors")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Order not found")})
    public Response deleteOrder(
            @ApiParam(value = "ID of the order that needs to be deleted", allowableValues = "range[1,infinity]", required = true) @PathParam("orderId") String orderId) {
        storeData.deleteOrder(ru.getLong(0, 10000, 0, orderId));
        return Response.ok().entity("").build();
    }
}
