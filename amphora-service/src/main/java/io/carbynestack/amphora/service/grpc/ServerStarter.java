package io.carbynestack.amphora.service.grpc;

import io.carbynestack.amphora.service.config.AmphoraServiceProperties;
import io.carbynestack.amphora.service.config.GlobalGrpcExceptionHandler;
import io.carbynestack.amphora.service.config.GrpcServerConfig;
import io.carbynestack.amphora.service.config.GrpcServicesUtils;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import java.io.IOException;
import javax.annotation.PreDestroy;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The {@code ServerStarter} class is responsible for starting and stopping the gRPC server of the
 * Castor service. It uses a {@link GrpcServerConfig} instance to configure the server and register
 * the gRPC services that provide the Castor functionality.
 */
@Component
@Data
public class ServerStarter {
    /** The gRPC server instance that will be created and started. */
    Server server;

    /** The configuration instance used to configure and register the gRPC services. */
    @Autowired
    GrpcServicesUtils grpcServicesUtils;

    @Autowired
    AmphoraServiceProperties amphoraServiceProperties;

    /**
     * Starts the gRPC server instance by building it with the configuration parameters from {@link
     * GrpcServerConfig} and registering the Castor gRPC services to provide the Castor functionality.
     * Please note that this method is a blocking one to listen for gRPC client requests
     *
     * @throws IOException if the server failed to start due to an I/O error.
     * @throws InterruptedException if the thread is interrupted while waiting for the server to
     *     terminate.
     */
    public void startGrpcServer() throws IOException, InterruptedException {
        ServerBuilder serverBuilder =
                ServerBuilder.forPort(grpcServicesUtils.getGrpcServerConfig().getPort());
        serverBuilder = grpcServicesUtils.addServices(serverBuilder);
        serverBuilder =
                serverBuilder
                        .addService(ProtoReflectionService.newInstance())
                        .intercept(
                                new GlobalGrpcExceptionHandler(
                                        grpcServicesUtils.getGrpcServerConfig().getHostName()
                                                + ":"
                                                + grpcServicesUtils.getGrpcServerConfig().getPort()));
        server = serverBuilder.build();

        server.start();
        server.awaitTermination();
    }

    /** Shuts down the gRPC server and stops the services registered on it. */
    @PreDestroy
    void shutDownGrpcServer() {
        server = server.shutdownNow();
        byte counter = 100;
        while (!server.isTerminated() && counter > 0) {
            counter--;
        }
    }
}
