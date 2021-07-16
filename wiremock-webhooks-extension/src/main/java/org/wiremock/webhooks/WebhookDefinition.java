package org.wiremock.webhooks;

import com.fasterxml.jackson.annotation.*;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;

public class WebhookDefinition {
    
    private String method;
    private String url;
    private List<HttpHeader> headers;
    private Body body = Body.none();
    private Parameters parameters;

    public static WebhookDefinition from(Parameters parameters) {
        return new WebhookDefinition(
                parameters.getString("method", "GET"),
                parameters.getString("url"),
                toHttpHeaders(parameters.getMetadata("headers", null)),
                parameters.getString("body", null),
                parameters.getString("base64Body", null),
                parameters
        );
    }

    private static HttpHeaders toHttpHeaders(Metadata headerMap) {
        if (headerMap == null || headerMap.isEmpty()) {
            return null;
        }

        return new HttpHeaders(
                headerMap.entrySet().stream()
                    .map(entry -> new HttpHeader(
                            entry.getKey(),
                            getHeaderValues(entry.getValue()))
                    )
                    .collect(Collectors.toList())
        );
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getHeaderValues(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof List) {
            return ((List<String>) obj);
        }

        return singletonList(obj.toString());
    }

    @JsonCreator
    public WebhookDefinition(String method,
                             String url,
                             HttpHeaders headers,
                             String body,
                             String base64Body,
                             Parameters parameters) {
        this.method = method;
        this.url = url;
        this.headers = headers != null ? new ArrayList<>(headers.all()) : null;

        if (body != null) {
            this.body = new Body(body);
        } else if (base64Body != null) {
            this.body = new Body(decodeBase64(base64Body));
        }

        this.parameters = parameters;
    }

    public WebhookDefinition() {
    }

    public String getMethod() {
        return method;
    }

    @JsonIgnore
    public RequestMethod getRequestMethod() {
        return RequestMethod.fromString(method);
    }

    public String getUrl() {
        return url;
    }

    public HttpHeaders getHeaders() {
        return new HttpHeaders(headers);
    }

    public String getBase64Body() {
        return body.isBinary() ? body.asBase64() : null;
    }

    public String getBody() {
        return body.isBinary() ? null : body.asString();
    }

    @JsonIgnore
    public Parameters getExtraParameters() {
        return parameters;
    }

    @JsonIgnore
    public byte[] getBinaryBody() {
        return body.asBytes();
    }

    public WebhookDefinition withMethod(String method) {
        this.method = method;
        return this;
    }

    public WebhookDefinition withMethod(RequestMethod method) {
        this.method = method.getName();
        return this;
    }

    public WebhookDefinition withUrl(URI url) {
        this.url = url.toString();
        return this;
    }

    public WebhookDefinition withUrl(String url) {
        this.url = url;
        return this;
    }

    public WebhookDefinition withHeaders(List<HttpHeader> headers) {
        this.headers = headers;
        return this;
    }

    public WebhookDefinition withHeader(String key, String... values) {
        if (headers == null) {
            headers = newArrayList();
        }

        headers.add(new HttpHeader(key, values));
        return this;
    }

    public WebhookDefinition withBody(String body) {
        this.body = new Body(body);
        return this;
    }

    public WebhookDefinition withBinaryBody(byte[] body) {
        this.body = new Body(body);
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getOtherFields() {
        return parameters;
    }

    @JsonAnySetter
    public WebhookDefinition withExtraParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new Parameters();
        }

        this.parameters.put(key, value);
        return this;
    }

    @JsonIgnore
    public boolean hasBody() {
        return body != null && body.isPresent();
    }
}
