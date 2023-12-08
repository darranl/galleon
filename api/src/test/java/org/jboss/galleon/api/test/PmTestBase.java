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
package org.jboss.galleon.api.test;

import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.util.Arrays;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.api.Provisioning;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.util.fs.state.DirState;
import org.jboss.galleon.api.test.util.fs.state.DirState.DirBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PmTestBase extends FeaturePackRepoTestBase {

    private GalleonProvisioningConfig initialProvisioningConfig;
    private ProvisionedState initialProvisionedState;
    private DirState initialHomeDirState;

    protected abstract void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException;

    protected GalleonProvisioningConfig initialState() throws ProvisioningException {
        return null;
    }

    protected GalleonProvisioningConfig provisionedConfig() throws ProvisioningException {
        return initialProvisioningConfig;
    }

    protected ProvisionedState provisionedState() throws ProvisioningException {
        return initialProvisionedState;
    }

    protected DirState provisionedHomeDir() {
        return null;
    }

    protected boolean assertProvisionedHomeDir() {
        return true;
    }

    protected abstract void testPm(Provisioning pm, ProvisioningManager mgr) throws ProvisioningException;

    @Override
    protected void doBefore() throws Exception {
        super.doBefore();
        final FeaturePackCreator fpCreator = initCreator();
        createFeaturePacks(fpCreator);
        fpCreator.install();
        initialProvisioningConfig = initialState();
        if(initialProvisioningConfig != null) {
            try (Provisioning pm = getPm()) {
                pm.provision(initialProvisioningConfig);
                try (ProvisioningManager mgr = getCorePm()) {
                initialProvisionedState = mgr.getProvisionedState();
                }
            }
        }
        initialHomeDirState = DirState.rootBuilder().init(installHome).build();
    }

    protected DirBuilder newDirBuilder() {
        final DirBuilder builder = DirState.rootBuilder();
        if(isRecordState() || initialProvisioningConfig != null) {
            builder.skip(Constants.PROVISIONED_STATE_DIR);
        }
        return builder;
    }

    protected String[] pmErrors() throws ProvisioningException {
        return null;
    }

    @Test
    public void main() throws Throwable {
        final String[] errors = pmErrors();
        boolean failed = false;
        Provisioning pm = null;
        ProvisioningManager mgr = null;
        try {
            mgr = getCorePm();
            pm = getPm();
            testPm(pm, mgr);
            pmSuccess();
            if(errors != null) {
                Assert.fail("Expected failures: " + Arrays.asList(errors));
            }
            if(isRecordState()) {
                assertProvisionedConfig(pm);
                assertProvisionedState(mgr);
            } else if(initialProvisioningConfig != null) {
                pm.close();
                pm = getPm();
                assertProvisionedConfig(pm);
                assertProvisionedState(mgr);
            } else {
                assertNoState();
            }
        } catch(AssertionError e) {
            throw e;
        } catch(Throwable t) {
            failed = true;
            if (errors == null) {
                pmFailure(t);
            } else {
                assertErrors(t, errors);
            }
            if(pm != null) {
                try {
                    if (isRecordState()) {
                        assertProvisioningConfig(pm, initialProvisioningConfig);
                        assertProvisionedState(mgr, initialProvisionedState);
                    } else if (initialProvisioningConfig != null) {
                        pm.close();
                        pm = getPm();
                        assertProvisioningConfig(pm, initialProvisioningConfig);
                        assertProvisionedState(mgr, initialProvisionedState);
                    }
                } finally {
                    pm.close();
                }
            }
        } finally {
            if(pm != null) {
                pm.close();
            }
        }

        DirState expectedHomeDir = provisionedHomeDir();
        if(expectedHomeDir == null) {
            if(!assertProvisionedHomeDir()) {
                return;
            }
            if(failed || initialProvisioningConfig != null) {
                expectedHomeDir = initialHomeDirState;
            } else {
                expectedHomeDir = newDirBuilder().build();
            }
        }
        expectedHomeDir.assertState(installHome);
    }

    protected void pmSuccess() {
    }


    protected void pmFailure(Throwable t) throws Throwable {
        throw t;
    }

    protected void assertProvisionedState(final ProvisioningManager pm) throws ProvisioningException {
        assertProvisionedState(pm, provisionedState());
    }

    protected void assertProvisionedConfig(final Provisioning pm) throws ProvisioningException {
        assertProvisioningConfig(pm, provisionedConfig());
    }

    protected void assertNoState() throws ProvisioningException {
        if(Files.exists(installHome.resolve(Constants.PROVISIONED_STATE_DIR))) {
            fail("Unexpected provisioning state " + installHome.resolve(Constants.PROVISIONED_STATE_DIR));
        }
    }

    protected void assertErrors(Throwable t, String... msgs) {
        int i = 0;
        if(msgs != null) {
            while (t != null && i < msgs.length) {
                Assert.assertEquals(msgs[i++], t.getLocalizedMessage());
                t = t.getCause();
            }
        }
        if(t != null) {
            Assert.fail("Unexpected error: " + t.getLocalizedMessage());
        }
        if(i < msgs.length - 1) {
            Assert.fail("Not reported error: " + msgs[i]);
        }
    }
}
