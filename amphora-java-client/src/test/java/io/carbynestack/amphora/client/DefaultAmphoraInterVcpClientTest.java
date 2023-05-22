/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import io.carbynestack.amphora.common.AmphoraServiceUri;
import io.carbynestack.amphora.common.FactorPair;
import io.carbynestack.amphora.common.MultiplicationExchangeObject;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.exceptions.AmphoraClientException;
import io.carbynestack.amphora.common.grpc.GrpcEmpty;
import io.carbynestack.amphora.common.grpc.GrpcMultiplicationExchangeObject;
import io.carbynestack.amphora.common.grpc.InterVcpServiceGrpc;
import io.carbynestack.httpclient.CsHttpClientException;
import io.vavr.control.Try;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultAmphoraInterVcpClientTest {
  private final String testUrl1 = "amphora.carbynestack.io:8080";
  private final String testUrl2 = "amphora.carbynestack.io:8081";
  private final UUID testOperationId = UUID.fromString("72753ecc-464f-42c5-954c-9efa7a5b886c");

  private DefaultAmphoraInterVcpClient amphoraInterVcpClient;

  private final List<String> amphoraServiceUris =
      Arrays.asList(testUrl1, testUrl2);

  List<InterVcpServiceGrpc.InterVcpServiceBlockingStub> stubs;

  MockedStatic<Utils> protoUtils;

  MockedStatic<DefaultAmphoraInterVcpClient> clientUtilities;

  @BeforeEach
  public void setUp() {
    stubs = new ArrayList<>();
    stubs.add(mock(InterVcpServiceGrpc.InterVcpServiceBlockingStub.class));
    stubs.add(mock(InterVcpServiceGrpc.InterVcpServiceBlockingStub.class));
    clientUtilities = Mockito.mockStatic(DefaultAmphoraInterVcpClient.class);
    clientUtilities
            .when(() -> DefaultAmphoraInterVcpClient.createStub(testUrl1))
            .thenReturn(stubs.get(0));
    clientUtilities
            .when(() -> DefaultAmphoraInterVcpClient.createStub(testUrl2))
            .thenReturn(stubs.get(1));
    this.amphoraInterVcpClient =
        new DefaultAmphoraInterVcpClient(amphoraServiceUris);
    protoUtils = Mockito.mockStatic(Utils.class);
  }

  @AfterEach
  public void afterEach(){
    protoUtils.close();
    clientUtilities.close();
  }

  @Test
  void givenNoServiceUriDefined_whenBuildingInterVcClient_thenThrowException() {
    List<String> serviceUrls = new ArrayList<>();
    serviceUrls.add(null);
    IllegalArgumentException actualIae =
        assertThrows(IllegalArgumentException.class, ()-> new DefaultAmphoraInterVcpClient(serviceUrls));
    assertEquals("At least one amphora service URI needs to be defined.", actualIae.getMessage());
  }

  @SneakyThrows
  @Test
  void givenValidExchangeObject_whenOpeningValues_thenSucceed() {
    MultiplicationExchangeObject exchangeObject = getMultiplicationExchangeObject(testOperationId);
    GrpcMultiplicationExchangeObject grpcMultiplicationExchangeObject = Utils.convertToProtoMultiplicationExchangeObject(exchangeObject);

    protoUtils
            .when(() -> Utils.convertToProtoMultiplicationExchangeObject(any()))
            .thenReturn(grpcMultiplicationExchangeObject);

    when(stubs.get(0).open(any())).thenReturn(GrpcEmpty.newBuilder().build());
    when(stubs.get(1).open(any())).thenReturn(GrpcEmpty.newBuilder().build());

    assertDoesNotThrow(()->amphoraInterVcpClient.open(exchangeObject));
  }

  @Test
  void givenOnePlayerCannotBeReached_whenOpeningValues_thenThrowException() {
    MultiplicationExchangeObject exchangeObject = getMultiplicationExchangeObject(testOperationId);
    GrpcMultiplicationExchangeObject grpcMultiplicationExchangeObject = Utils.convertToProtoMultiplicationExchangeObject(exchangeObject);

    protoUtils
            .when(() -> Utils.convertToProtoMultiplicationExchangeObject(any()))
            .thenReturn(grpcMultiplicationExchangeObject);

    when(stubs.get(0).open(any())).thenReturn(GrpcEmpty.newBuilder().build());
    when(stubs.get(1).open(any())).thenReturn(GrpcEmpty.newBuilder().build());
    List<String> serviceUris = new ArrayList<>(amphoraServiceUris);
    serviceUris.add("test:100");
    this.amphoraInterVcpClient =
            new DefaultAmphoraInterVcpClient(serviceUris);
    assertThrows(AmphoraClientException.class,()->amphoraInterVcpClient.open(exchangeObject));
  }

  @Test
  void givenOnePlayerRespondsUnsuccessful_whenOpeningValues_thenThrowException() {
    MultiplicationExchangeObject exchangeObject = getMultiplicationExchangeObject(testOperationId);
    GrpcMultiplicationExchangeObject grpcMultiplicationExchangeObject = Utils.convertToProtoMultiplicationExchangeObject(exchangeObject);

    protoUtils
            .when(() -> Utils.convertToProtoMultiplicationExchangeObject(any()))
            .thenReturn(grpcMultiplicationExchangeObject);

    when(stubs.get(0).open(any())).thenReturn(GrpcEmpty.newBuilder().build());
    when(stubs.get(1).open(any())).thenThrow(new RuntimeException());
    assertThrows(AmphoraClientException.class,()->amphoraInterVcpClient.open(exchangeObject));
  }

  @Test
  void givenMissingOperationId_whenOpeningValues_thenThrowException() {
    MultiplicationExchangeObject exchangeObject = getMultiplicationExchangeObject(null);
    NullPointerException npe =
        assertThrows(NullPointerException.class, () -> amphoraInterVcpClient.open(exchangeObject));
    assertThat(npe.getMessage()).contains("OperationId must not be null");
  }

  private MultiplicationExchangeObject getMultiplicationExchangeObject(UUID secretId) {
    return new MultiplicationExchangeObject(
        secretId, 0, Collections.singletonList(FactorPair.of(BigInteger.ONE, BigInteger.TEN)));
  }
}
