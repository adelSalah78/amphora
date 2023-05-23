/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.service.grpc.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.carbynestack.amphora.common.MultiplicationExchangeObject;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.grpc.GrpcEmpty;
import io.carbynestack.amphora.service.grpc.controllers.InterVcpService;
import io.carbynestack.amphora.service.persistence.cache.InterimValueCachingService;
import java.util.Collections;
import java.util.UUID;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterVcpControllerTest {
  @Mock private InterimValueCachingService interimValueCachingService;

  @InjectMocks private InterVcpService interVcpService;

  @Test
  void givenSuccessfulRequest_whenReceivingInterimValues_thenStoreValuesInCache() {
    MultiplicationExchangeObject exchangeObject =
        new MultiplicationExchangeObject(
            UUID.fromString("ea983b9b-0e98-4cbb-8dbf-3c8362653c0d"), 0, Collections.emptyList());
    assertDoesNotThrow(()->interVcpService.open(Utils.convertToProtoMultiplicationExchangeObject(exchangeObject), new StreamObserver<GrpcEmpty>() {
      @Override
      public void onNext(GrpcEmpty grpcEmpty) {

      }

      @Override
      public void onError(Throwable throwable) {

      }

      @Override
      public void onCompleted() {

      }
    }));
    verify(interimValueCachingService, times(1)).putInterimValues(exchangeObject);
  }
}
