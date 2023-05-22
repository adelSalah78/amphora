package io.carbynestack.amphora.service.grpc.controllers;

import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.grpc.GrpcEmpty;
import io.carbynestack.amphora.common.grpc.GrpcMaskedInput;
import io.carbynestack.amphora.common.grpc.MaskedInputServiceGrpc;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;

@Slf4j
@AllArgsConstructor
public class MaskedInputService extends MaskedInputServiceGrpc.MaskedInputServiceImplBase {

    private final StorageService storageService;
    @Override
    public void upload(GrpcMaskedInput request, StreamObserver<GrpcEmpty> responseObserver) {
        notNull(request, "MaskedInput must not be null");
        notEmpty(request.getDataList(), "MaskedInput data must not be empty");
        storageService.createSecret(Utils.convertFromGrpcMaskedInput(request));
        responseObserver.onNext(GrpcEmpty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
