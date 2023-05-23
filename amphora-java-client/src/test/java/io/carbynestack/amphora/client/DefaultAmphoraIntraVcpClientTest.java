/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.client;

import static io.carbynestack.amphora.client.TestData.getTags;
import static io.carbynestack.amphora.common.rest.AmphoraRestApiEndpoints.SECRET_SHARES_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.carbynestack.amphora.common.SecretShare;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.exceptions.AmphoraClientException;
import io.carbynestack.amphora.common.grpc.GrpcSecretShare;
import io.carbynestack.amphora.common.grpc.GrpcSecretShareResponse;
import io.carbynestack.amphora.common.grpc.IntraVcpServiceGrpc;
import io.carbynestack.mpspdz.integration.MpSpdzIntegrationUtils;
import java.math.BigInteger;
import java.net.URI;
import java.util.Random;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultAmphoraIntraVcpClientTest {
  private final String testUrl = "amphora.carbynestack.io:8080";
  private final UUID testSecretId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");

  private DefaultAmphoraIntraVcpClient amphoraIntraVcpClient;

  private MpSpdzIntegrationUtils spdzUtil;

  IntraVcpServiceGrpc.IntraVcpServiceBlockingStub stub;

  MockedStatic<Utils> protoUtils;

  MockedStatic<DefaultAmphoraIntraVcpClient> clientUtilities;

  private final Random rnd = new Random(42);

  @BeforeEach
  public void setUp() {
    stub = mock(IntraVcpServiceGrpc.IntraVcpServiceBlockingStub.class);
    clientUtilities = Mockito.mockStatic(DefaultAmphoraIntraVcpClient.class);
    clientUtilities
            .when(() -> DefaultAmphoraIntraVcpClient.createStub(testUrl))
            .thenReturn(stub);
    spdzUtil =
        MpSpdzIntegrationUtils.of(
            new BigInteger("198766463529478683931867765928436695041"),
            new BigInteger("141515903391459779531506841503331516415"),
            new BigInteger("133854242216446749056083838363708373830"));
    this.amphoraIntraVcpClient =
        new DefaultAmphoraIntraVcpClient(testUrl);
    protoUtils = Mockito.mockStatic(Utils.class);
  }

  @AfterEach
  public void afterEach(){
    protoUtils.close();
    clientUtilities.close();
  }

  @Test
  void givenNoServiceUriDefined_whenBuildingInterVcpClient_thenThrowException() {
    IllegalArgumentException actualIae =
        assertThrows(IllegalArgumentException.class,()-> new DefaultAmphoraIntraVcpClient(null));
    assertThat(actualIae.getMessage()).contains("Service URI must not be null");
  }

  @SneakyThrows
  @Test
  void givenShareIsValid_whenUploadSecretShare_thenSucceed() {
//    URI expectedUri = URI.create(testUrl + SECRET_SHARES_ENDPOINT + "/" + testSecretId);
    SecretShare secretShare = getSecretShare(testSecretId);
    protoUtils.when(()->Utils.convertToProtoSecretShare(secretShare)).thenReturn(GrpcSecretShare.newBuilder()
            .setUuid(testSecretId.toString()).build());
    when(stub.uploadSecretShare(any())).thenReturn(GrpcSecretShareResponse.newBuilder().setUuid(testSecretId.toString()).build());
    assertEquals(testSecretId, amphoraIntraVcpClient.uploadSecretShare(secretShare));
    assertDoesNotThrow(()->amphoraIntraVcpClient.uploadSecretShare(secretShare));
  }

  @SneakyThrows
  @Test
  void givenIdIsValid_whenDownloadingSecretShare_thenReturnSecretShare() {
    SecretShare secretShare = getSecretShare(testSecretId);
    protoUtils.when(()->Utils.convertFromProtoSecretShare(any())).thenReturn(secretShare);
    when(stub.downloadSecretShare(any())).thenReturn(GrpcSecretShare.newBuilder().build());
    assertDoesNotThrow(()->amphoraIntraVcpClient.getSecretShare(testSecretId));
  }

  @Test
  void givenIdIsNull_whenDownloadingSecretShare_thenThrowException() {
    NullPointerException npe =
        assertThrows(NullPointerException.class, () -> amphoraIntraVcpClient.getSecretShare(null));
    assertThat(npe.getMessage()).contains("SecretId must not be null");
  }

  @Test
  void givenSecretShareWithIdDoesNotExist_whenDownloadingSecretShare_thenThrowException() {
    when(stub.downloadSecretShare(any())).thenThrow(RuntimeException.class);
    AmphoraClientException ace =
        assertThrows(
            AmphoraClientException.class, () -> amphoraIntraVcpClient.getSecretShare(testSecretId));
    assertEquals(String.format("Fetching secret #%s failed", testSecretId), ace.getMessage());
  }

  private SecretShare getSecretShare(UUID id) {
    return SecretShare.builder()
        .secretId(id)
        .data(
            ArrayUtils.addAll(
                spdzUtil.toGfp(BigInteger.valueOf(Math.abs(rnd.nextLong()))),
                new byte[MpSpdzIntegrationUtils.WORD_WIDTH]))
        .tags(getTags())
        .build();
  }
}
