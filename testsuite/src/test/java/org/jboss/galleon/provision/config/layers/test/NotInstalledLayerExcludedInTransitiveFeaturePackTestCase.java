/*
 * Copyright 2016-2020 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.provision.config.layers.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class NotInstalledLayerExcludedInTransitiveFeaturePackTestCase extends ProvisionFromUniverseTestBase {

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
        prod2 = newFpl("prod2", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod1.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "spec"))
                    .addParam(FeatureParameterSpec.create("p2", "spec"))
                    .addParam(FeatureParameterSpec.create("p3", "spec"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "base-prod1")
                            .setParam("p2", "base"))
                    .addPackageDep("base")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("layer1")
                    .addLayerDep("base", true)
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "layer1-prod1")
                            .setParam("p2", "layer1"))
                    .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                    .includeLayer("layer1")
                    .build())
            .newPackage("base")
                .writeContent("base.txt", "base");

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .addDependency(FeaturePackConfig.builder(prod1)
                    .setInheritPackages(false)
                    .setInheritConfigs(false)
                    .includeDefaultConfig("model1", "name1")
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("layer1")
                    .addLayerDep("base", true)
                    .addFeature(new FeatureConfig("specB")
                            .setParam("id", "layer1-prod2"))
                    .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                    .excludeLayer("layer1")
                    .excludeLayer("base")
                    .build());

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod2).build())
                // if the config below is not added then it will make the config
                // defined in prod2 being the first on the stack which will be checked
                // for not installed excluded layers and the test will fail
                .addConfig(ConfigModel.builder("model1", "name1").build())
                .build();
    }

    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod2.getFPID())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .build();
    }
}