package com.github.kongchen.swagger.docgen.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wordnik.swagger.annotations.Authorization;
import com.wordnik.swagger.model.AuthorizationScope;
import com.wordnik.swagger.model.AuthorizationType;
import com.wordnik.swagger.model.OAuth;
import com.wordnik.swagger.model.OAuthBuilder;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by m.karmazyn on 31/03/15.
 */
public class AuthorizationUtils {
    public static List<AuthorizationType> convertToAuthorizationTypes(Authorization[] authorizations) {
        ArrayList<AuthorizationType> types = Lists.newArrayList();
        for (Authorization authorization : authorizations) {
            String value = authorization.value();
            if ("oauth2".equals(value)) {
                OAuth oauth = new OAuthBuilder().scopes(convertScopes(authorization)).build();
                types.add(oauth);
            }
        }
        return types;
    }

    public static List<AuthorizationScope> convertScopes(Authorization authorization) {
        List<AuthorizationScope> list = Lists.newArrayList();
        for (com.wordnik.swagger.annotations.AuthorizationScope authorizationScope : authorization.scopes()) {
            list.add(new AuthorizationScope(authorizationScope.scope(), authorizationScope.description()));
        }
        return list;
    }

    public static void mergeAuthorizationTypes(List<AuthorizationType> authorizationTypes, List<AuthorizationType> types) {
            List<AuthorizationType> toAdd = Lists.newArrayList();
        for (AuthorizationType newType : types) {
            Iterator<AuthorizationType> iterator = authorizationTypes.iterator();
            boolean found = false;
            while (iterator.hasNext()) {
                AuthorizationType authorizationType = iterator.next();
                if(newType.type().equals(authorizationType.type())) {
                    if(newType instanceof OAuth) {
                        OAuth build = new OAuthBuilder().scopes(mergeScopes(((OAuth) newType).scopes(), ((OAuth)authorizationType).scopes())).build();
                        toAdd.add(build);
                        iterator.remove();
                    }
                    found = true;
                }
            }
            if(!found) {
                toAdd.add(newType);
            }
        }
        authorizationTypes.addAll(toAdd);
    }

    private static List<AuthorizationScope> mergeScopes(scala.collection.immutable.List<AuthorizationScope> authorizationScopes, scala.collection.immutable.List<AuthorizationScope> newScopes) {
        Map<String, String> scopes = Maps.newHashMap();
        for (AuthorizationScope scope : JavaConversions.asJavaList(authorizationScopes)) {
            scopes.put(scope.scope(), scope.description());
        }
        for (AuthorizationScope scope : JavaConversions.asJavaList(newScopes)) {
            scopes.put(scope.scope(), scope.description());
        }

        List<AuthorizationScope> merged = Lists.newArrayList();
        for (Map.Entry<String, String> entry : scopes.entrySet()) {
            merged.add(new AuthorizationScope(entry.getKey(),entry.getValue()));
        }
        return merged;
    }
}
