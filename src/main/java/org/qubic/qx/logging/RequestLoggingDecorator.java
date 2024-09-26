package org.qubic.qx.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Slf4j
public class RequestLoggingDecorator extends ServerHttpRequestDecorator {

    public RequestLoggingDecorator(ServerHttpRequest delegate) {
        super(delegate);
    }

    @Override
    public Flux<DataBuffer> getBody() {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            return super.getBody()
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(logBody(bos));
        } catch (IOException e) {
            handleIoException(e);
            return super.getBody();
        }
    }

    private static Consumer<DataBuffer> logBody(ByteArrayOutputStream bos) {
        return dataBuffer -> {
            try (DataBuffer.ByteBufferIterator byteBufferIterator = dataBuffer.readableByteBuffers()) {
                while (byteBufferIterator.hasNext()) {
                    try (WritableByteChannel channel = Channels.newChannel(bos)) {
                        channel.write(byteBufferIterator.next());
                    }
                    log.info("Request payload: {}", bos.toString(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                handleIoException(e);
            }
        };
    }

    private static void handleIoException(IOException e) {
        log.warn("IO exception trying to log request body. Ignoring.", e);
    }

}
