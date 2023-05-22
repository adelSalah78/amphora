package io.carbynestack.amphora.service.grpc.controllers;

import io.carbynestack.amphora.common.OutputDeliveryObject;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.grpc.GrpcInputMaskRequest;
import io.carbynestack.amphora.common.grpc.GrpcOutputDeliveryObject;
import io.carbynestack.amphora.common.grpc.InputMaskShareServiceGrpc;
import io.carbynestack.amphora.service.persistence.cache.InputMaskCachingService;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;

import java.util.UUID;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

@AllArgsConstructor
public class InputMaskShareService extends InputMaskShareServiceGrpc.InputMaskShareServiceImplBase {

    public static final String REQUEST_IDENTIFIER_MUST_NOT_BE_NULL_EXCEPTION_MSG =
            "Request identifier must not be null.";
    public static final String TOO_LESS_INPUT_MASKS_EXCEPTION_MSG =
            "The number of requested Input Masks has to be 1 or greater.";
    private final InputMaskCachingService inputMaskCachingService;

    @Override
    public void getInputMask(GrpcInputMaskRequest request, StreamObserver<GrpcOutputDeliveryObject> responseObserver) {
        notNull(request.getRequestId(), REQUEST_IDENTIFIER_MUST_NOT_BE_NULL_EXCEPTION_MSG);
        isTrue(request.getCount() > 0, TOO_LESS_INPUT_MASKS_EXCEPTION_MSG);
        OutputDeliveryObject outputDeliveryObject = inputMaskCachingService.getInputMasksAsOutputDeliveryObject(
                UUID.fromString(request.getRequestId())
                , request.getCount()
        );
        responseObserver.onNext(Utils.convertToProtoOutputDeliveryObject(outputDeliveryObject));
        responseObserver.onCompleted();
    }
}
