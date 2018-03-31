package com.wordnik.springmvc;

import com.wordnik.sample.data.CrudService;
import com.wordnik.sample.exception.NotFoundException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author by amalagraba on 22/02/2018.
 */
public abstract class CrudController<E> {

    @GetMapping(value = "/")
    @ApiOperation(value = "Returns all entities")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Entity data found"),
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public ResponseEntity<List<E>> getAll() {
        return ResponseEntity.ok(getCrudService().getAll());
    }

    @GetMapping(value = "/{id}")
    @ApiOperation(value = "Find entity by ID",
            notes = "Returns an entity when ID < 10.  ID > 10 or nonintegers will simulate API error conditions"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Entity data found"),
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public ResponseEntity<E> get(
            @ApiParam(value = "ID of the entity that needs to be fetched", allowableValues = "range[1,5]", required = true)
            @PathVariable("id") Long id)
            throws NotFoundException {
        E entity = getCrudService().get(id);

        if (entity != null) {
            return ResponseEntity.ok(entity);
        } else {
            throw new NotFoundException(404, "Entity not found");
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Deletes an entity", nickname = "remove")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid entity value")})
    public ResponseEntity delete(
            @ApiParam(value = "Entity id to delete", required = true)
            @PathVariable("id") @Size() Long id) {
        getCrudService().delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(consumes = {"application/json", "application/xml"})
    @ApiOperation(value = "Add a new entity to the store")
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public ResponseEntity<E> save(
            @ApiParam(value = "Entity object that needs to be added to the store", required = true)
            @RequestBody E entity) {
        return ResponseEntity.ok(getCrudService().save(entity));
    }

    @PutMapping(consumes = {"application/json", "application/xml"})
    @ApiOperation(value = "Update an existing entity")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Entity not found"),
            @ApiResponse(code = 405, message = "Validation exception")})
    public ResponseEntity<E> update(
            @ApiParam(value = "Entity that needs to be updated", required = true)
            @RequestBody E entity) {
        return ResponseEntity.ok(getCrudService().update(entity));
    }

    protected abstract CrudService<E> getCrudService();
}
