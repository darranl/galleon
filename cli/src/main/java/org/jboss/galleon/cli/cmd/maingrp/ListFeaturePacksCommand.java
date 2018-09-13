/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.maingrp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.aesh.utils.Config;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.UniverseManager.UniverseVisitor;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.Headers;
import org.jboss.galleon.cli.cmd.Table;
import org.jboss.galleon.cli.cmd.state.StateInfoUtil;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.UniverseSpec;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "list-feature-packs", description = HelpDescriptions.LIST)
public class ListFeaturePacksCommand extends PmSessionCommand {

    private static final String NONE = "NONE";

    @Option(required = false, name = "universe", description = HelpDescriptions.LIST_UNIVERSE)
    private String fromUniverse;

    @Option(required = false, name = "all-frequencies", hasValue = false, description = HelpDescriptions.LIST_ALL_FREQUENCIES)
    private Boolean allFrequencies;

    @Override
    public void runCommand(PmCommandInvocation invoc)
            throws CommandExecutionException {
        Map<UniverseSpec, Table> tables = new HashMap<>();
        Map<UniverseSpec, Exception> exceptions = new HashMap<>();
        UniverseVisitor visitor = new UniverseVisitor() {
            @Override
            public void visit(Producer<?> producer, FeaturePackLocation loc) {
                if (loc.getFrequency() == null) {
                    return;
                }
                if (allFrequencies || loc.getFrequency().equals(producer.getDefaultFrequency())) {
                    Table table = tables.get(loc.getUniverse());
                    if (table == null) {
                        table = new Table(Headers.PRODUCT, Headers.UPDATE_CHANNEL, Headers.LATEST_BUILD);
                        tables.put(loc.getUniverse(), table);
                    }
                    loc = invoc.getPmSession().getExposedLocation(loc);
                    table.addLine(producer.getName(), StateInfoUtil.formatChannel(loc),
                            (loc.getBuild() == null ? NONE : loc.getBuild()));
                }
            }

            @Override
            public void exception(UniverseSpec spec, Exception ex) {
                exceptions.put(spec, ex);
            }
        };
        try {
            if (fromUniverse != null) {
                invoc.getPmSession().getUniverse().
                        visitUniverse(UniverseSpec.fromString(fromUniverse), visitor, true);
            } else {
                invoc.getPmSession().getUniverse().
                        visitAllUniverses(visitor, true);
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(invoc.getPmSession(),
                    CliErrors.resolvedUniverseFailed(), ex);
        }
        FindCommand.printExceptions(invoc, exceptions);
        for (Entry<UniverseSpec, Table> entry : tables.entrySet()) {
            UniverseSpec universeSpec = entry.getKey();
            String universeName = invoc.getPmSession().getUniverse().getUniverseName(universeSpec);
            universeName = universeName == null ? universeSpec.toString() : universeName;
            invoc.println(Config.getLineSeparator() + "Universe " + universeName
                    + Config.getLineSeparator());
            if (!exceptions.containsKey(entry.getKey())) {
                Table table = entry.getValue();
                table.sort(Table.SortType.ASCENDANT);
                invoc.println(table.build());
            }
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
