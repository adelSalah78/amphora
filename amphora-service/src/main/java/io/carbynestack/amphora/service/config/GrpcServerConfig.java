package io.carbynestack.amphora.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for the GRPC server. Contains fields for the server port and various GRPC
 * services required by the server.
 */
@ConfigurationProperties(prefix = "carbynestack.amphora.grpc.server")
@Component
@Data
public class GrpcServerConfig {
    int port;
    String hostName;
}
