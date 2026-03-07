/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yuokada.quarkus.extension.fluency.fluentd.it;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import io.github.yuokada.quarkus.extension.fluency.fluentd.runtime.FluencyClient;
import io.github.yuokada.quarkus.extension.fluency.fluentd.runtime.ValidatingFluencyClient;

@Path("/quarkus-fluency-fluentd")
@ApplicationScoped
public class QuarkusFluencyFluentdResource {

    @Inject FluencyClient fluencyClient;

    @Inject ValidatingFluencyClient validatingFluencyClient;

    @GET
    public String hello() {
        return "Hello quarkus-fluency-fluentd";
    }

    /** Emits a test record and reports whether it was accepted. */
    @POST
    @Path("/emit")
    public Response emit(@QueryParam("tag") String tag, @QueryParam("message") String message) {
        String resolvedTag = tag != null ? tag : "myapp.default";
        String resolvedMessage = message != null ? message : "test";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", resolvedMessage);
        data.put("source", "quarkus-fluency-fluentd");

        boolean accepted = fluencyClient.emit(resolvedTag, data);
        if (accepted) {
            return Response.ok("Emitted to tag: " + resolvedTag).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Fluentd not available")
                    .build();
        }
    }

    /**
     * Like /emit but delegates to {@link ValidatingFluencyClient}. Returns 400 for invalid tag or
     * missing message instead of silently failing.
     */
    @POST
    @Path("/validated-emit")
    public Response validatedEmit(
            @QueryParam("tag") String tag, @QueryParam("message") String message) {
        String resolvedTag = tag != null ? tag : "";
        String resolvedMessage = message != null ? message : "test";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", resolvedMessage);
        data.put("source", "quarkus-fluency-fluentd");

        try {
            boolean accepted = validatingFluencyClient.emit(resolvedTag, data);
            if (accepted) {
                return Response.ok("Emitted to tag: " + resolvedTag).build();
            } else {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("Fluentd not available")
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /** Health-style check — reports whether the Fluency client is connected. */
    @GET
    @Path("/status")
    public Response status() {
        if (fluencyClient.isAvailable()) {
            return Response.ok("connected").build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("disconnected").build();
    }
}
