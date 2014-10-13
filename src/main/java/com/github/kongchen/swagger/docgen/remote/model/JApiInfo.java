package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.ApiInfo;

/**
 * Created by chekong on 10/11/14.
 */
public class JApiInfo implements CanBeSwaggerModel<ApiInfo> {
    private String title;
    private String description;
    private String termsOfServiceUrl;
    private String contact;
    private String license;
    private String licenseUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public ApiInfo toSwaggerModel() {
        return new ApiInfo(title, description, termsOfServiceUrl, contact, license, licenseUrl);
    }
}
