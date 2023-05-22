/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.client;

import io.carbynestack.amphora.common.MultiplicationExchangeObject;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.exceptions.AmphoraClientException;
import io.carbynestack.amphora.common.grpc.GrpcEmpty;
import io.carbynestack.amphora.common.grpc.InterVcpServiceGrpc;
import io.vavr.control.Try;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/** Client for all Service-to-Service operations. */
@Slf4j
@Setter(value = AccessLevel.NONE)
@Getter(value = AccessLevel.PACKAGE)
@ToString(callSuper = true)
@EqualsAndHashCode
public class DefaultAmphoraInterVcpClient implements AmphoraInterVcpClient {
  private final List<String> serviceUrls;

  private final List<InterVcpServiceGrpc.InterVcpServiceBlockingStub> stubs;

  /**
   * @param serviceUrls Urls of the Amphora Services of all partners in the VC
   ** @throws AmphoraClientException If the HTTP(S) client could not be instantiated
   */
  @lombok.Builder(builderMethodName = "Builder")
  DefaultAmphoraInterVcpClient(List<String> serviceUrls) {
    serviceUrls.forEach(serviceUrl -> {
      if (serviceUrl == null || serviceUrl.isEmpty()) {
        throw new IllegalArgumentException("At least one amphora service URI needs to be defined.");
      }
    });
    this.serviceUrls = serviceUrls;

    this.stubs = new ArrayList<>(this.serviceUrls.size());
    serviceUrls.forEach(uri -> stubs.add(createStub(uri)));
  }

  static InterVcpServiceGrpc.InterVcpServiceBlockingStub createStub(String uri){
    return InterVcpServiceGrpc.newBlockingStub(Utils.createGrpcChannel(uri));
  }

  @Override
  public void open(MultiplicationExchangeObject multiplicationExchangeObject)
      throws AmphoraClientException {
    Objects.requireNonNull(
        multiplicationExchangeObject.getOperationId(), "OperationId must not be null");
    Map<String, Try<GrpcEmpty>> responses = new HashMap<>();
    AtomicInteger index = new AtomicInteger();
    serviceUrls.forEach(url -> {
              responses.put(url, Try.of(
                      () -> stubs.get(index.get()).open(Utils.convertToProtoMultiplicationExchangeObject(multiplicationExchangeObject))
              ));
              index.getAndIncrement();
            }
    );
    checkSuccess(responses);
  }

  private void checkSuccess(Map<String, Try<GrpcEmpty>> uriResponseMap) throws AmphoraClientException {
    List<String> failedRequests =
        uriResponseMap.entrySet().parallelStream()
            .filter(uriTryEntry -> uriTryEntry.getValue().isFailure())
            .map(
                uriTryEntry ->
                    String.format(
                        "Request for endpoint \"%s\" has failed: %s",
                        uriTryEntry.getKey(), uriTryEntry.getValue().getCause()))
            .collect(Collectors.toList());
    if (!failedRequests.isEmpty()) {
      throw new AmphoraClientException(
          String.format(
              "At least one request has failed:%n\t%s",
              failedRequests.parallelStream().collect(Collectors.joining("\n\t"))));
    }
  }
}
