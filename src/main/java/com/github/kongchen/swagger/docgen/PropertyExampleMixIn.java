package com.github.kongchen.swagger.docgen;

import com.fasterxml.jackson.annotation.JsonRawValue;

abstract class PropertyExampleMixIn {
    PropertyExampleMixIn() { }
    
    @JsonRawValue abstract Object getExample();
}
