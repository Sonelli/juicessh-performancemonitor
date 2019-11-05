/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.launcher;




public class Java7Checker {

    public static void check() {
        String version = System.getProperty("java.version");
        String[] elems = (version != null) ? version.split("\\.|_|-") : null;
        if (version != null && !version.isEmpty() && elems != null && elems.length >= 1) {
            try {
                int major = Integer.parseInt(elems[0]);
                int minor = 0;
                try {
                    // text minor such as 9-Ubuntu is allowed now
                    minor = elems.length > 1 ? Integer.parseInt(elems[1]) : 0;
                } catch (NumberFormatException ex) {}
                //int release = Integer.parseInt(elems[2]);
                if (major == 1 && minor < 7) {
                    System.err.println("Your Java version is not supported: " + version);
                    System.err.println("Ceylon needs Java 7 or newer. Please install it from http://www.java.com");
                    System.err.println("Aborting.");
                    System.exit(1);
                }
                return;
            } catch (NumberFormatException ex) {}
        }
        System.err.println("Unable to determine Java version (java.version property missing, empty or has unexpected format: '" + version +"'). Aborting.");
        System.exit(1);
    }
    
}
