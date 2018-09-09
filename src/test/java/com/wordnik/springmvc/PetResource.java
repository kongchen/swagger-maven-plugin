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

import com.wordnik.sample.JavaRestResourceUtil;
import com.wordnik.sample.data.PetData;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "/pet", description = "Operations about pets", authorizations = {
        @Authorization(value = "petstore_auth",
                scopes = {
                        @AuthorizationScope(scope = "write:pets", description = "modify pets in your account"),
                        @AuthorizationScope(scope = "read:pets", description = "read your pets")
                })
})

@RequestMapping(value = "/pet", produces = {"application/json", "application/xml"})
public class PetResource {

    static PetData petData = new PetData();
    static JavaRestResourceUtil ru = new JavaRestResourceUtil();

    @RequestMapping(value = "/{petId}", consumes = {"application/x-www-form-urlencoded"}, method = RequestMethod.POST)
    @ApiOperation(value = "Updates a pet in the store with form data", consumes = "application/x-www-form-urlencoded")
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public ResponseEntity<com.wordnik.sample.model.ApiResponse> updatePetWithForm(@ModelAttribute UpdatePetRequest updatePetRequest) {
        return new ResponseEntity<com.wordnik.sample.model.ApiResponse>(new com.wordnik.sample.model.ApiResponse(200, "SUCCESS"), HttpStatus.OK);
    }
}
