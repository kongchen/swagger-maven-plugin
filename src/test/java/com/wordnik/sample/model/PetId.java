package com.wordnik.sample.model;

/**
 * Created by chekong on 15/5/19.
 */
public class PetId {
    private final long id;

    public PetId(long id){
        this.id = id;
    }

    public long value() {
        return id;
    }
}
