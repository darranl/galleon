/*
 * Copyright 2016-2022 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.userchanges.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class UserChangesAfterUndoTestCase extends UserChangesTestBase {

    private FeaturePackLocation prod1;
    private FeaturePackLocation prod2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(prod1.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3")
                .writeContent("prod1/p1.txt", "prod1 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("prod1/p2.txt", "prod1 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("prod1/p3.txt", "prod1 p3")
                .getFeaturePack()
            .newPackage("common", true)
               .writeContent("common.txt", "prod1");

        prod2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(prod2.getFPID())
            .newPackage("p1", true)
                .writeContent("prod2/p1.txt", "prod2 p1")
                .writeContent("prod2/p2.txt", "prod2 p2")
                .getFeaturePack()
           .newPackage("common", true)
                .writeContent("common.txt", "prod2");

    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.install(prod1);
        pm.install(prod2);
        writeContent("new.txt", "user");
        writeContent("prod1/p2.txt", "user");
        writeContent("prod2/p2.txt", "user");
        writeContent("common.txt", "user");
        recursiveDelete("prod1/p3.txt");
        mkdirs("prod1/user");
        pm.undo();
    }


    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod1)
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("p1")
                        .addPackage("p2")
                        .addPackage("p3")
                        .addPackage("common")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("prod1/p1.txt", "prod1 p1")
                .addFile("prod1/p2.txt", "user")
                .addFile("new.txt", "user")
                .addFile("prod2/p2.txt", "user")
                .addFile("common.txt", "user")
                .addFile("common.txt.glnew", "prod1")
                .addDir("prod1/user")
                .build();
    }
}
