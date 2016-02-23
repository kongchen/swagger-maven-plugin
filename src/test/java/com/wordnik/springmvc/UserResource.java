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

package com.wordnik.springmvc;

import com.wordnik.sample.data.UserData;
import com.wordnik.sample.exception.ApiException;
import com.wordnik.sample.model.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Api(value = "/user", description = "Operations about user")
@RequestMapping(value = "/user", produces = {"application/json", "application/xml"})
public class UserResource {
    static UserData userData = new UserData();


    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Create user",
            notes = "This can only be done by the logged in user.",
            position = 1)
    public ResponseEntity createUser(
            @ApiParam(value = "Created user object", required = true) User user, String arbitraryString) {
        userData.addUser(user);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/createWithArray", method = RequestMethod.POST)
    @ApiOperation(value = "Creates list of users with given input array",
            position = 2)
    public ResponseEntity createUsersWithArrayInput(@ApiParam(value = "List of user object", required = true) User[] users) {
        for (User user : users) {
            userData.addUser(user);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/createWithList", method = RequestMethod.POST)
    @ApiOperation(value = "Creates list of users with given input array",
            position = 3)
    public ResponseEntity createUsersWithListInput(@ApiParam(value = "List of user object", required = true) @RequestBody java.util.List<User> users) {
        for (User user : users) {
            userData.addUser(user);
        }
        return new ResponseEntity(HttpStatus.OK);
    }


    @RequestMapping(value = "/{username}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @ApiOperation(value = "Updated user",
            notes = "This can only be done by the logged in user.",
            position = 4)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid user supplied"),
            @ApiResponse(code = 404, message = "User not found")})
    public ResponseEntity updateUser(
            @ApiParam(value = "name that need to be deleted", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Updated user object", required = true) User user) {
        userData.addUser(user);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete user",
            notes = "This can only be done by the logged in user.",
            position = 5)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid username supplied"),
            @ApiResponse(code = 404, message = "User not found")})
    public ResponseEntity deleteUser(
            @ApiParam(value = "The name that needs to be deleted", required = true) @PathVariable("username") String username) {
        userData.removeUser(username);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.GET)
    @ApiOperation(value = "Get user by user name",
            response = User.class,
            position = 0)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid username supplied"),
            @ApiResponse(code = 404, message = "User not found")})
    public ResponseEntity<User> getUserByName(
            @ApiParam(value = "The name that needs to be fetched. Use user1 for testing. ", required = true) @PathVariable("username") String username)
            throws ApiException {
        User user = userData.findUserByName(username);
        if (user != null) {
            return new ResponseEntity<User>(user, HttpStatus.OK);
        } else {
            throw new com.wordnik.sample.exception.NotFoundException(404, "User not found");
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ApiOperation(value = "Logs user into the system",
            response = String.class,
            position = 6)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid username/password supplied")})
    public ResponseEntity<String> loginUser(
            @ApiParam(value = "The user name for login", required = true) @RequestParam("username") String username,
            @ApiParam(value = "The password for login in clear text", required = true) @RequestParam("password") String password) {
        return new ResponseEntity<String>("logged in user session:" + System.currentTimeMillis(), HttpStatus.OK);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ApiOperation(value = "Logs out current logged in user session",
            position = 7)
    public ResponseEntity logoutUser() {
        return new ResponseEntity(HttpStatus.OK);
    }
}
