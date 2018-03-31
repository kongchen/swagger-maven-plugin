package com.wordnik.sample.data;

import java.util.List;

/**
 * @author by amalagraba on 22/02/2018.
 */
public interface CrudService <E> {

    void delete(long id);

    E get(long id);

    E update(E entity);

    E save(E entity);

    List<E> getAll();
}
