package io.carbynestack.amphora.service.grpc.controllers;

import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.grpc.*;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class IntraVcpService extends IntraVcpServiceGrpc.IntraVcpServiceImplBase {
    private final StorageService storageService;

    @Override
    public void uploadSecretShare(GrpcSecretShare request, StreamObserver<GrpcSecretShareResponse> responseObserver) {
        Assert.notNull(request, "SecretShare must not be null");
        String uuid = storageService.storeSecretShare(Utils.convertFromProtoSecretShare(request));
        responseObserver.onNext(GrpcSecretShareResponse.newBuilder().setUuid(uuid).build());
        responseObserver.onCompleted();
    }

    @Override
    public void downloadSecretShare(GrpcDownloadSecretShareRequest request, StreamObserver<GrpcSecretShare> responseObserver) {
        responseObserver.onNext(
                Utils.convertToProtoSecretShare(storageService.getSecretShare(UUID.fromString(request.getUuid())))
        );
        responseObserver.onCompleted();
    }
}
