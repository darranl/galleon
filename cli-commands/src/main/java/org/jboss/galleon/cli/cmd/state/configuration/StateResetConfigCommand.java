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
package org.jboss.galleon.cli.cmd.state.configuration;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.StateActivators.ResetConfigCommandActivator;

/**
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "reset-config", description = HelpDescriptions.RESET_CONFIG, activator = ResetConfigCommandActivator.class)
public class StateResetConfigCommand extends AbstractStateCommand {

    @Argument(required = true, description = HelpDescriptions.CONFIGURATION_FULL_NAME,
            completer = ProvisionedConfigurationCompleter.class)
    private String configuration;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.configuration.core.CoreStateResetConfigCommand";
    }

    /**
     * @return the configuration
     */
    public String getConfiguration() {
        return configuration;
    }
}
