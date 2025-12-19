package io.github.shigaichi.jquants.client;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

interface HttpRequestExecutor {
    HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException;
}
