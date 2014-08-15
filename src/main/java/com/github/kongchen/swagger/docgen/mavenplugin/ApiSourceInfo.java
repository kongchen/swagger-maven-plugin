package com.github.kongchen.swagger.docgen.mavenplugin;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by chekong on 8/15/14.
 */
public class ApiSourceInfo {
    @Parameter
    private String title;
    @Parameter
    private String description;
    @Parameter
    private String termsOfServiceUrl;
    @Parameter
    private String contact;
    @Parameter
    private String license;
    @Parameter
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
}
