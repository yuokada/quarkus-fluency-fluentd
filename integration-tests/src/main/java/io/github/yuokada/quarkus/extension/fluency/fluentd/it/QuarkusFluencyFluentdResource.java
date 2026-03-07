/*
 * MIT License
 *
 * Copyright (c) 2026 Yukihiro Okada (yuokada)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
