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
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.trace.TraceObserver

/**
 * Nextflow trace observer for the nf-bids plugin.
 *
 * <p>Hooks into the Nextflow execution lifecycle to emit startup and
 * completion messages.  Extended in future releases to emit BIDS-specific
 * run summaries.</p>
 */
@Slf4j
@CompileStatic
class BidsObserver implements TraceObserver {

    /**
     * Called when the Nextflow pipeline session is created and about to start.
     *
     * @param session the newly created Nextflow session
     */
    @Override
    void onFlowCreate(Session session) {
        println "Pipeline is starting! 🚀"
    }

    /**
     * Called when the Nextflow pipeline has finished executing all processes.
     */
    @Override
    void onFlowComplete() {
        println "Pipeline complete! 👋"
    }

}
