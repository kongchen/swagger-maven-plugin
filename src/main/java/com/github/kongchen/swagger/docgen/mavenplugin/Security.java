package com.github.kongchen.swagger.docgen.mavenplugin;

import io.swagger.models.SecurityRequirement;

import java.util.ArrayList;
import java.util.List;

public class Security {
	private String name;
	private List<String> scopes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	public SecurityRequirement generateSecurityRequirement() {
		SecurityRequirement securityRequirement = new SecurityRequirement();
		securityRequirement.setRequirements(name, scopes != null ? scopes : new ArrayList<>());

		return securityRequirement;
	}
}
