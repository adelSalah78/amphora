/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.service.grpc.controllers;

import static io.carbynestack.amphora.common.rest.AmphoraRestApiEndpoints.INTRA_VCP_OPERATIONS_SEGMENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.carbynestack.amphora.common.SecretShare;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.grpc.GrpcDownloadSecretShareRequest;
import io.carbynestack.amphora.common.grpc.GrpcSecretShare;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntraVcpControllerTest {
  @Mock private StorageService storageService;

  @InjectMocks private IntraVcpService intraVcpService;

  @Test
  void givenArgumentIsNull_whenUploadSecretShare_thenThrowIllegalArgumentException() {
    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class, () -> intraVcpService.uploadSecretShare(null,new StreamObserverTestUtils.IntraVcpStreamObserver()));
    assertEquals("SecretShare must not be null", iae.getMessage());
  }

  @Test
  void givenSuccessfulRequest_whenUploadSecretShare_thenReturnCreatedWithExpectedContent() {
    UUID secretShareId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");
    SecretShare secretShare = SecretShare.builder().data(new byte[32]).tags(new ArrayList<>()).secretId(secretShareId).build();

    when(storageService.storeSecretShare(secretShare)).thenReturn(secretShareId.toString());

    StreamObserverTestUtils.IntraVcpStreamObserver streamObserver = new StreamObserverTestUtils.IntraVcpStreamObserver();

    assertDoesNotThrow(()->intraVcpService.uploadSecretShare(Utils.convertToProtoSecretShare(secretShare),streamObserver));
    assertEquals(Utils.convertToProtoSecretShare(secretShare).getUuid(), streamObserver.grpcSecretShareResponse.getUuid());
  }

  @Test
  void givenSuccessfulRequest_whenDownloadSecretShare_thenReturnOkWithExpectedContent() {
    UUID secretShareId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");
    SecretShare expectedSecretShare = SecretShare.builder().data(new byte[32]).tags(new ArrayList<>()).secretId(secretShareId).build();

    when(storageService.getSecretShare(secretShareId)).thenReturn(expectedSecretShare);

    StreamObserverTestUtils.IntraVcpStreamObserver streamObserver = new StreamObserverTestUtils.IntraVcpStreamObserver();

    assertDoesNotThrow(()->intraVcpService.downloadSecretShare(GrpcDownloadSecretShareRequest.newBuilder()
            .setUuid(secretShareId.toString()).build(),streamObserver));

    assertEquals(Utils.convertToProtoSecretShare(expectedSecretShare), streamObserver.secretShare);
  }
}
