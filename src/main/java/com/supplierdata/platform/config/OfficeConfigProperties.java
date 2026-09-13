package com.supplierdata.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Mirrors the "Site Preferences / Office Setup" configuration concept from
 * the original .NET platform: per-office behavior toggles and preferences,
 * bound from application-{profile}.yml under the "office" prefix.
 */
@ConfigurationProperties(prefix = "office")
public class OfficeConfigProperties {

    /** Office/site code -> preference map, e.g. "LON": { currency: "GBP" } */
    private Map<String, OfficePreference> sites = new HashMap<>();

    public Map<String, OfficePreference> getSites() {
        return sites;
    }

    public void setSites(Map<String, OfficePreference> sites) {
        this.sites = sites;
    }

    public static class OfficePreference {
        private String defaultCurrency = "USD";
        private String defaultLocale = "en-US";
        private boolean autoApproveFareRules = false;
        private String supplierDataFormat = "CSV";

        public String getDefaultCurrency() { return defaultCurrency; }
        public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }

        public String getDefaultLocale() { return defaultLocale; }
        public void setDefaultLocale(String defaultLocale) { this.defaultLocale = defaultLocale; }

        public boolean isAutoApproveFareRules() { return autoApproveFareRules; }
        public void setAutoApproveFareRules(boolean autoApproveFareRules) { this.autoApproveFareRules = autoApproveFareRules; }

        public String getSupplierDataFormat() { return supplierDataFormat; }
        public void setSupplierDataFormat(String supplierDataFormat) { this.supplierDataFormat = supplierDataFormat; }
    }
}
