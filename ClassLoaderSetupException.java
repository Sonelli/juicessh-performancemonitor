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

public class ClassLoaderSetupException extends Exception {
    private static final long serialVersionUID = -260387041605744118L;

    public ClassLoaderSetupException(Throwable cause){
        super(cause);
    }
}
