/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.service;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulkOverRegistry;
import org.apache.jena.sparql.service.single.ServiceExecutor;
import org.apache.jena.sparql.service.single.ServiceExecutorOverRegistry;
import org.apache.jena.sparql.util.Context;

/**
 * Entry into the service executor from SPARQL queries.
 */
public class ServiceExec {

    /**
     * Use {@link #exec(OpService, QueryIterator, ExecutionContext)} whose parameter order matches that of
     * {@link ServiceExecutorBulk#createExecution(OpService, QueryIterator, ExecutionContext)}.
     */
    @Deprecated(forRemoval = true, since = "5.4.0")
    public static QueryIterator exec(QueryIterator input, OpService opService, ExecutionContext execCxt) {
        return exec(opService, input, execCxt);
    }

    /**
     * Execute an OpService w.r.t. the execCxt's service executor registry.
     * This is the route from OpExecutor.
     *
     * This method can also be used to pass a modified request through the whole
     * service executor chain, as exemplified below:
     * <pre>{@code
     * ServiceExecutorRegistry.get().addBulkLink((opService, input, execCxt, chain) -> {
     *     if (canHandle(opService)) {
     *         OpService modifiedOp = modifyOp(opService);
     *         // Forward the request to the beginning of the chain.
     *         return ServiceExec.exec(modifiedOp, input, execCxt);
     *     } else {
     *         // Forward the request to the remaining handlers in the chain.
     *         return chain.createExecution(opService, input, execCxt);
     *     }
     * });
     * }</pre>
     */
    public static QueryIterator exec(OpService opService, QueryIterator input, ExecutionContext execCxt) {
        Context cxt = execCxt.getContext();
        ServiceExecutorRegistry registry = ServiceExecutorRegistry.chooseRegistry(cxt);
        ServiceExecutorBulk serviceExecutor = new ServiceExecutorBulkOverRegistry(registry);
        QueryIterator qIter = serviceExecutor.createExecution(opService, input, execCxt);
        return qIter;
    }

    /**
     * Execute an OpService w.r.t. the execCxt's service executor registry -
     * concretely its single chain which operates on a per-binding basis.
     */
    public static QueryIterator exec(OpService opExecute, OpService original, Binding binding, ExecutionContext execCxt) {
        Context cxt = execCxt.getContext();
        ServiceExecutorRegistry registry = ServiceExecutorRegistry.chooseRegistry(cxt);
        ServiceExecutor serviceExecutor = new ServiceExecutorOverRegistry(registry);
        QueryIterator qIter = serviceExecutor.createExecution(opExecute, original, binding, execCxt);
        return qIter;
    }
}
