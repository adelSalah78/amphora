/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.client;

import io.carbynestack.amphora.common.SecretShare;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.exceptions.AmphoraClientException;
import java.util.Objects;
import java.util.UUID;

import io.carbynestack.amphora.common.grpc.GrpcDownloadSecretShareRequest;
import io.carbynestack.amphora.common.grpc.IntraVcpServiceGrpc;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/** Client for all Service-to-Service operations. */
@Slf4j
@Setter(value = AccessLevel.NONE)
@ToString(callSuper = true)
@EqualsAndHashCode
public class DefaultAmphoraIntraVcpClient implements AmphoraIntraVcpClient {

  private final IntraVcpServiceGrpc.IntraVcpServiceBlockingStub stub;

  /**
   * @param serviceUrl Url of the Amphora Service
   * @throws IllegalArgumentException If @param serviceUrl is empty
   */
  @lombok.Builder(builderMethodName = "Builder")
  public DefaultAmphoraIntraVcpClient(String serviceUrl) {
    if (serviceUrl == null || serviceUrl.isEmpty()) {
      throw new IllegalArgumentException("Service URI must not be null");
    }
    stub = createStub(serviceUrl);
  }

  static IntraVcpServiceGrpc.IntraVcpServiceBlockingStub createStub(String url){
    return IntraVcpServiceGrpc.newBlockingStub(Utils.createGrpcChannel(url));
  }

  @Override
  public UUID uploadSecretShare(SecretShare secretShare) throws AmphoraClientException {
    try {
      return UUID.fromString(stub.uploadSecretShare(Utils.convertToProtoSecretShare(secretShare)).getUuid());
    }
    catch(Exception e){
      log.error("Creating SecretShare failed", e);
      throw new AmphoraClientException("Creating SecretShare failed", e);
    }
  }

  @Override
  public SecretShare getSecretShare(UUID secretId) throws AmphoraClientException {
    Objects.requireNonNull(secretId, "SecretId must not be null");
    try {
      return Utils.convertFromProtoSecretShare(
              stub.downloadSecretShare(GrpcDownloadSecretShareRequest.newBuilder().setUuid(secretId.toString()).build())
      );
    }
    catch(Exception e){
      log.error(String.format("Fetching secret #%s failed", secretId.toString()), e);
      throw new AmphoraClientException(
              String.format("Fetching secret #%s failed", secretId.toString()), e);
    }
  }
}
