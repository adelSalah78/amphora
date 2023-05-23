/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.carbynestack.amphora.service.grpc.controllers;

import static io.carbynestack.amphora.service.grpc.controllers.InputMaskShareService.REQUEST_IDENTIFIER_MUST_NOT_BE_NULL_EXCEPTION_MSG;
import static io.carbynestack.amphora.service.grpc.controllers.InputMaskShareService.TOO_LESS_INPUT_MASKS_EXCEPTION_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.carbynestack.amphora.common.OutputDeliveryObject;
import io.carbynestack.amphora.common.grpc.GrpcInputMaskRequest;
import io.carbynestack.amphora.common.grpc.GrpcOutputDeliveryObject;
import io.carbynestack.amphora.service.AmphoraTestData;
import io.carbynestack.amphora.service.grpc.controllers.InputMaskShareService;
import io.carbynestack.amphora.service.persistence.cache.InputMaskCachingService;
import io.carbynestack.castor.common.entities.Field;
import io.carbynestack.castor.common.entities.InputMask;
import io.carbynestack.castor.common.entities.TupleList;
import java.util.UUID;

import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InputMaskShareControllerTest {
  private final UUID testRequestId = UUID.fromString("7520e090-1437-44da-9e4e-eea5b2200fea");
  private final int validNumberOfTuples = 1;
  private final int invalidNumberOfTuples = -1;

  @Mock private InputMaskCachingService inputMaskStore;

  @InjectMocks private InputMaskShareService inputMaskShareService;

  static class MyStreamObserver implements StreamObserver {

    public OutputDeliveryObject body;

    @Override
    public void onNext(Object o) {

      GrpcOutputDeliveryObject grpcOutputDeliveryObject = (GrpcOutputDeliveryObject) o;

      body = new OutputDeliveryObject(
              grpcOutputDeliveryObject.getSecretShares().toByteArray(),
              grpcOutputDeliveryObject.getRShares().toByteArray(),
              grpcOutputDeliveryObject.getVShares().toByteArray(),
              grpcOutputDeliveryObject.getWShares().toByteArray(),
              grpcOutputDeliveryObject.getUShares().toByteArray()
      );
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

    }
  }

  @SneakyThrows
  @Test
  void givenSuccessfulRequest_whenGetInputMasks_thenReturnExpectedResult() {
    TupleList<InputMask<Field.Gfp>, Field.Gfp> testInputMasks =
        AmphoraTestData.getRandomInputMaskList(validNumberOfTuples);
    OutputDeliveryObject testOdo =
        AmphoraTestData.getRandomOutputDeliveryObject(validNumberOfTuples);

    when(inputMaskStore.getInputMasksAsOutputDeliveryObject(testRequestId, testInputMasks.size()))
        .thenReturn(testOdo);

    MyStreamObserver response = new MyStreamObserver();
    inputMaskShareService.getInputMask(GrpcInputMaskRequest.newBuilder()
                        .setRequestId(testRequestId.toString()).setCount(validNumberOfTuples).build()
                , response);

    assertEquals(testOdo, response.body);
  }

  @Test
  void givenEmptyStringAsRequestId_whenGetInputMasks_thenThrowIllegalArgumentException() {
    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class,
            () -> inputMaskShareService.getInputMask(GrpcInputMaskRequest.newBuilder().build(), null));
    assertEquals(REQUEST_IDENTIFIER_MUST_NOT_BE_NULL_EXCEPTION_MSG, iae.getMessage());
  }

  @Test
  void givenInvalidCountArgument_whenGetInputMasks_thenThrowIllegalArgumentException() {
    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class,
            () -> inputMaskShareService.getInputMask(GrpcInputMaskRequest.newBuilder()
                    .setRequestId(testRequestId.toString()).setCount(invalidNumberOfTuples).build(),null));
    assertEquals(TOO_LESS_INPUT_MASKS_EXCEPTION_MSG, iae.getMessage());
  }
}
