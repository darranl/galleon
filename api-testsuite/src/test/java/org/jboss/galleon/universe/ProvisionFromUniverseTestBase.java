/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.universe;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.api.Provisioning;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ProvisionFromUniverseTestBase extends SingleUniverseTestBase {

    @Override
    protected GalleonProvisioningConfig provisionedConfig() throws ProvisioningException {
        return provisioningConfig();
    }

    protected abstract GalleonProvisioningConfig provisioningConfig() throws ProvisioningException;

    @Override
    protected void testPm(Provisioning pm, ProvisioningManager mgr) throws ProvisioningException {
        pm.provision(provisioningConfig());
    }
}
