package com.rahul.kovil.common.util;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class LicenseValidation {

    public LicenseStatus checkLicense() {
        Path licensePath = Path.of("license.key");
        
        if (!Files.exists(licensePath)) {
            return new LicenseStatus(false, "License file missing.\nContact your developer.");
        }
        try {
            String savedId = Files.readString(licensePath).trim();
            String currentId = HardwareIdUtil.getHardwareId();

            System.err.println(savedId+"\nrahul-"+currentId);
            if (!currentId.equals(savedId)) {
                return new LicenseStatus(false, "Unauthorized machine.\nApp will now close.");
            }
        } catch (Exception e) {
            return new LicenseStatus(false, "License validation failed.");
        }

        return new LicenseStatus(true, "OK");
    }
}
