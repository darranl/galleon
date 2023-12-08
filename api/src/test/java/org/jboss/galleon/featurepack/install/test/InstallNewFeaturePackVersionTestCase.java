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
package org.jboss.galleon.featurepack.install.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.api.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallNewFeaturePackVersionTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP1_101_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.1.Final");

    @Override
    protected void doBefore() throws Exception {
        super.doBefore();
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_100_GAV)
                .newPackage("p1", true)
                    .addDependency(PackageDependencySpec.optional("p2"))
                    .writeContent("fp1/p1.txt", "fp1 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2")
                    .writeContent("fp1/p2.txt", "fp1 1.0.0.Final p2")
                    .getFeaturePack()
                .newPackage("p3")
                    .writeContent("fp1/p3.txt", "fp1 1.0.0.Final p3")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP1_101_GAV)
                .newPackage("p1", true)
                    .addDependency(PackageDependencySpec.optional("p2"))
                    .writeContent("fp1/p1.txt", "fp1 1.0.1.Final p1")
                    .getFeaturePack()
                .newPackage("p2")
                    .writeContent("fp1/p2.txt", "fp1 1.0.1.Final p2")
                    .getFeaturePack()
                .newPackage("p3")
                    .writeContent("fp1/p3.txt", "fp1 1.0.1.Final p3")
                    .getFeaturePack();
    }

    @Override
    protected GalleonProvisioningConfig initialState() throws ProvisioningException {
        return GalleonProvisioningConfig.builder()
                .addFeaturePackDep(GalleonFeaturePackConfig.forLocation(FP1_100_GAV.getLocation()))
                .build();
    }

    @Override
    protected GalleonFeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return GalleonFeaturePackConfig.forLocation(FP1_101_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_101_GAV)
                        .addPackage("p2")
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 1.0.1.Final p1")
                .addFile("fp1/p2.txt", "fp1 1.0.1.Final p2")
                .build();
    }
}
