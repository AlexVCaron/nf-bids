/*
 * Copyright 2025, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nfneuro.plugin

import groovy.transform.CompileStatic
import nextflow.Session
import nextflow.plugin.extension.Factory
import nextflow.plugin.extension.PluginExtensionPoint

import groovyx.gpars.dataflow.DataflowWriteChannel
import nfneuro.plugin.channel.BidsChannelFactory

/**
 * Implements a custom function which can be imported by
 * Nextflow scripts.
 */
@CompileStatic
class BidsExtension extends PluginExtensionPoint {

    private Session session

    @Override
    protected void init(Session session) {
        this.session = session
    }
    /**
     * Parse BIDS dataset and return channel
     * 
     * Creates a channel from a BIDS dataset with structured data grouping
     * according to the provided configuration.
     * 
     * Usage:
     *   Channel.fromBIDS('/path/to/bids/dataset', 'config.yaml', [bids_validation: false])
     * 
     * @param bidsDir Path to BIDS dataset directory
     * @param configPath Path to configuration YAML file (optional)
     * @param options Additional options map (bids_validation, libbids_sh_path, etc.)
     * @return DataflowWriteChannel containing structured BIDS data
     * 
     * @reference Main workflow implementation:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L20-L56
     */
    @Factory
    DataflowWriteChannel fromBIDS(
        String bidsDir,
        String configPath = null,
        Map options = [:]
    ) {
        return new BidsChannelFactory(session).fromBIDS(bidsDir, configPath, options) as DataflowWriteChannel
    }

}
