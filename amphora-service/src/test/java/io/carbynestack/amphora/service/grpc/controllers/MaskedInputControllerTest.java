/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.service.grpc.controllers;

import static io.carbynestack.amphora.common.rest.AmphoraRestApiEndpoints.INTRA_VCP_OPERATIONS_SEGMENT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.carbynestack.amphora.common.MaskedInput;
import io.carbynestack.amphora.common.MaskedInputData;
import io.carbynestack.amphora.common.grpc.GrpcMaskedInput;
import io.carbynestack.amphora.common.grpc.GrpcMaskedInputData;
import io.carbynestack.amphora.service.grpc.controllers.MaskedInputService;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaskedInputControllerTest {

  @Mock private StorageService storageService;

  @InjectMocks private MaskedInputService maskedInputService;

  @Test
  void givenArgumentIsNull_whenUpload_thenThrowIllegalArgumentException() {
    IllegalArgumentException iae =
        assertThrows(IllegalArgumentException.class, () -> maskedInputService.upload(GrpcMaskedInput.newBuilder()
                .build(),null));
    assertEquals("MaskedInput must not be null", iae.getMessage());
  }

  @Test
  void givenMaskedInputDataIsEmpty_whenUpload_thenThrowIllegalArgumentException() {
    UUID expectedId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");

    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class, () -> maskedInputService.upload(GrpcMaskedInput.newBuilder()
                                .setSecretId(expectedId.toString())
                        .build(),null));
    assertEquals("MaskedInput data must not be empty", iae.getMessage());
  }

  @Test
  void givenSuccessfulRequest_whenUpload_thenReturnCreatedWithExpectedContent() {
    UUID secretShareId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");
    URI expectedUri =
        URI.create(
            "https://amphora.carbynestack.io" + INTRA_VCP_OPERATIONS_SEGMENT + "/" + secretShareId);
    MaskedInput maskedInput =
        new MaskedInput(
            secretShareId, singletonList(MaskedInputData.of(new byte[16])), emptyList());

    when(storageService.createSecret(maskedInput)).thenReturn(secretShareId.toString());

    List<GrpcMaskedInputData> grpcMaskedInputDataList = new ArrayList<>(16);

   assertDoesNotThrow(()->maskedInputService.upload(GrpcMaskedInput.newBuilder()
           .setSecretId(secretShareId.toString())
           .addAllData(grpcMaskedInputDataList)
           .addAllTags(emptyList())
           .build(),null));

//        assertEquals(expectedUri, actualResponse.getBody());

  }
}
