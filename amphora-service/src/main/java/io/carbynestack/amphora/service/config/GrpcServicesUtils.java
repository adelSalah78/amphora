package io.carbynestack.amphora.service.config;

import io.carbynestack.amphora.service.calculation.OutputDeliveryService;
import io.carbynestack.amphora.service.grpc.controllers.*;
import io.carbynestack.amphora.service.persistence.cache.InputMaskCachingService;
import io.carbynestack.amphora.service.persistence.cache.InterimValueCachingService;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;
import io.grpc.ServerBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
public class GrpcServicesUtils {

    @Autowired
    GrpcServerConfig grpcServerConfig;
    @Autowired
    private final InputMaskCachingService inputMaskCachingService;
    @Autowired
    private final InterimValueCachingService interimValueCachingService;
    @Autowired
    private final StorageService storageService;
    @Autowired
    private final OutputDeliveryService outputDeliveryService;

    public ServerBuilder addServices(ServerBuilder serverBuilder) {
        return serverBuilder
                .addService(new InputMaskShareService(inputMaskCachingService))
                .addService(new InterVcpService(interimValueCachingService))
                .addService(new IntraVcpService(storageService))
                .addService(new MaskedInputService(storageService))
                .addService(new SecretShareService(storageService,outputDeliveryService))
                .addService(new TagsService(storageService));
    }
}
