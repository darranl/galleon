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
package org.jboss.galleon.config.mixedloops;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.api.test.util.TestConfigHandlersProvisioningPlugin;
import org.jboss.galleon.api.test.util.TestProvisionedConfigHandler;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class RefAndStaticCapTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    public static class ConfigHandler extends TestProvisionedConfigHandler {
        @Override
        protected boolean loggingEnabled() {
            return false;
        }
        @Override
        protected String[] initEvents() {
            return new String[] {
                    batchStartEvent(),
                    featurePackEvent(FP_GAV),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b1")),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a1")),
                    batchEndEvent(),
                    // no batch because cap.b has already been provided by b1
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a2")),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b2")),
            };
        }
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .requiresCapability("cap.b")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .providesCapability("cap.b")
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.create("a"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1")
                            .setParam("a", "a1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a2"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b2")
                            .setParam("a", "a2"))
                    .build())
            .addPlugin(TestConfigHandlersProvisioningPlugin.class)
            .addService(ProvisionedConfigHandler.class, ConfigHandler.class);
    }

    @Override
    protected GalleonFeaturePackConfig featurePackConfig() {
        return GalleonFeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b1")).setConfigParam("a", "a1").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a2")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b2")).setConfigParam("a", "a2").build())
                        .build())
                .build();
    }
}
