package io.carbynestack.amphora.service.grpc.controllers;

import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.grpc.GrpcEmpty;
import io.carbynestack.amphora.common.grpc.GrpcMultiplicationExchangeObject;
import io.carbynestack.amphora.common.grpc.InterVcpServiceGrpc;
import io.carbynestack.amphora.service.persistence.cache.InterimValueCachingService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class InterVcpService extends InterVcpServiceGrpc.InterVcpServiceImplBase {

    private final InterimValueCachingService interimValueCachingService;
    @Override
    public void open(GrpcMultiplicationExchangeObject request, StreamObserver<GrpcEmpty> responseObserver) {
        log.debug("received interim values for operation #{}", request.getOperationId());
        interimValueCachingService.putInterimValues(Utils.convertFromProtoMultiplicationExchangeObject(request));
        responseObserver.onNext(GrpcEmpty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
