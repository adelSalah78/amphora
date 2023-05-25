/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.carbynestack.amphora.service.grpc.controllers;

import static io.carbynestack.amphora.common.TagFilterOperator.EQUALS;
import static io.carbynestack.amphora.common.TagFilterOperator.LESS_THAN;
import static io.carbynestack.amphora.common.rest.AmphoraRestApiEndpoints.CRITERIA_SEPARATOR;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.carbynestack.amphora.common.*;
import io.carbynestack.amphora.common.grpc.GrpcEmpty;
import io.carbynestack.amphora.common.grpc.GrpcGetObjectListRequest;
import io.carbynestack.amphora.common.grpc.GrpcSecretShareRequest;
import io.carbynestack.amphora.service.calculation.OutputDeliveryService;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class SecretShareControllerTest {

  @Mock private OutputDeliveryService outputDeliveryService;

  @Mock private StorageService storageService;

  @InjectMocks private SecretShareService secretShareService;

  @SneakyThrows
  @Test
  void
      givenSuccessfulRequestWithoutFilterAndWithoutPaging_whenGetObjectList_thenReturnExpectedContent() {
    List<Metadata> expectedMetadataList = emptyList();
    Page<Metadata> metadataSpringPage = new PageImpl<>(expectedMetadataList, Pageable.unpaged(), 0);
    String filter = "";
    int pageNumber = 0;
    int pageSize = 0;
    String sortProperty = "";
    String sortDirection = "";
    when(storageService.getSecretList(Sort.unsorted())).thenReturn(metadataSpringPage);

    StreamObserverTestUtils.SecretShareStreamObserver response = new StreamObserverTestUtils.SecretShareStreamObserver();
    assertDoesNotThrow(()->        secretShareService.getObjectList(GrpcGetObjectListRequest.newBuilder()
            .setFilter(filter)
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .setSortProperty(sortProperty)
            .setSortDirection(sortDirection)
            .build(),response));
    assertEquals(0, response.grpcMetadataPage.getNumber());
    assertEquals(1, response.grpcMetadataPage.getTotalPages());
    assertEquals(0, response.grpcMetadataPage.getTotalElements());
    assertEquals(expectedMetadataList, response.grpcMetadataPage.getContentList());
  }

  @SneakyThrows
  @Test
  void
      givenSuccessfulRequestWithoutFilterButWithPaging_whenGetObjectList_thenReturnExpectedContent() {
    List<Metadata> expectedMetadataList = emptyList();
    Page<Metadata> metadataSpringPage = new PageImpl<>(expectedMetadataList, Pageable.unpaged(), 0);
    String filter = "";
    int pageNumber = 0;
    int pageSize = 1;
    String sortProperty = "";
    String sortDirection = "";
    when(storageService.getSecretList(PageRequest.of(0, 1, Sort.unsorted())))
        .thenReturn(metadataSpringPage);

    StreamObserverTestUtils.SecretShareStreamObserver response = new StreamObserverTestUtils.SecretShareStreamObserver();
    assertDoesNotThrow(()->        secretShareService.getObjectList(GrpcGetObjectListRequest.newBuilder()
            .setFilter(filter)
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .setSortProperty(sortProperty)
            .setSortDirection(sortDirection)
            .build(),response));
    assertEquals(0, response.grpcMetadataPage.getNumber());
    assertEquals(1, response.grpcMetadataPage.getTotalPages());
    assertEquals(0, response.grpcMetadataPage.getTotalElements());
    assertEquals(Utils.createProtoMetadataList(expectedMetadataList), response.grpcMetadataPage.getContentList());
  }

  @SneakyThrows
  @Test
  void
      givenSuccessfulRequestWithFilterAndWithoutPaging_whenGetObjectList_thenReturnExpectedContent() {
    List<Metadata> expectedMetadataList = emptyList();
    Page<Metadata> metadataSpringPage = new PageImpl<>(expectedMetadataList, Pageable.unpaged(), 0);
    String filter = "key" + EQUALS + "value" + CRITERIA_SEPARATOR + "key2" + LESS_THAN + "42";
    List<TagFilter> expectedTagFilter =
        asList(TagFilter.with("key", "value", EQUALS), TagFilter.with("key2", "42", LESS_THAN));
    int pageNumber = 0;
    int pageSize = 0;
    String sortProperty = "";
    String sortDirection = "";
    when(storageService.getSecretList(expectedTagFilter, Sort.unsorted()))
        .thenReturn(metadataSpringPage);

    StreamObserverTestUtils.SecretShareStreamObserver response = new StreamObserverTestUtils.SecretShareStreamObserver();

    assertDoesNotThrow(()->        secretShareService.getObjectList(GrpcGetObjectListRequest.newBuilder()
            .setFilter(filter)
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .setSortProperty(sortProperty)
            .setSortDirection(sortDirection)
            .build(),response));
    assertEquals(0, response.grpcMetadataPage.getNumber());
    assertEquals(1, response.grpcMetadataPage.getTotalPages());
    assertEquals(0, response.grpcMetadataPage.getTotalElements());
    assertEquals(Utils.createProtoMetadataList(expectedMetadataList), response.grpcMetadataPage.getContentList());
  }

  @SneakyThrows
  @Test
  void givenSuccessfulRequestWithFilterButWithPaging_whenGetObjectList_thenReturnExpectedContent() {
    List<Metadata> expectedMetadataList = emptyList();
    Page<Metadata> metadataSpringPage = new PageImpl<>(expectedMetadataList, Pageable.unpaged(), 0);
    String filter = "key" + EQUALS + "value" + CRITERIA_SEPARATOR + "key2" + LESS_THAN + "42";
    List<TagFilter> expectedTagFilter =
        asList(TagFilter.with("key", "value", EQUALS), TagFilter.with("key2", "42", LESS_THAN));
    int pageNumber = 0;
    int pageSize = 1;
    String sortProperty = "";
    String sortDirection = "";
    when(storageService.getSecretList(expectedTagFilter, PageRequest.of(0, 1, Sort.unsorted())))
        .thenReturn(metadataSpringPage);

    StreamObserverTestUtils.SecretShareStreamObserver response = new StreamObserverTestUtils.SecretShareStreamObserver();

    assertDoesNotThrow(()->        secretShareService.getObjectList(GrpcGetObjectListRequest.newBuilder()
            .setFilter(filter)
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .setSortProperty(sortProperty)
            .setSortDirection(sortDirection)
            .build(),response));
    assertEquals(0, response.grpcMetadataPage.getNumber());
    assertEquals(1, response.grpcMetadataPage.getTotalPages());
    assertEquals(0, response.grpcMetadataPage.getTotalElements());
    assertEquals(Utils.createProtoMetadataList(expectedMetadataList), response.grpcMetadataPage.getContentList());
  }

  @Test
  void givenRequestIdArgumentIsNull_whenGetSecretShare_thenThrowIllegalArgumentException() {
    UUID secretId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");
    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class,
            () -> secretShareService.getSecretShare(GrpcSecretShareRequest.newBuilder()
                            .setSecretId(secretId.toString())
                    .build(), null));
    assertEquals("Request identifier must not be omitted", iae.getMessage());
  }

  @Test
  void givenSuccessfulRequest_whenGetSecretShare_thenReturnOkAndExpectedContent() {
    UUID secretId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");
    UUID requestId = UUID.fromString("d6d0f4ff-df28-4c96-b7df-95170320eaee");
    SecretShare secretShare = SecretShare.builder().secretId(requestId).tags(new ArrayList<>()).build();
    OutputDeliveryObject expectedOutputDeliveryObject = new OutputDeliveryObject(
            new byte[16],new byte[16],new byte[16],new byte[16],new byte[16]
    );

    when(storageService.getSecretShare(secretId)).thenReturn(secretShare);
    when(outputDeliveryService.computeOutputDeliveryObject(secretShare, requestId))
        .thenReturn(expectedOutputDeliveryObject);
    StreamObserverTestUtils.SecretShareStreamObserver response = new StreamObserverTestUtils.SecretShareStreamObserver();
    assertDoesNotThrow(()->secretShareService.getSecretShare(GrpcSecretShareRequest.newBuilder()
            .setSecretId(secretId.toString())
            .setRequestId(requestId.toString())
            .build(),response));
    assertEquals(
        Utils.createProtoVerifiableSecretShare(secretShare, expectedOutputDeliveryObject),
        response.grpcVerifiableSecretShare);
  }

  @Test
  void givenSuccessfulRequest_whenDeleteSecretShare_thenReturnOk() {
    UUID secretId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");
    assertDoesNotThrow(()->secretShareService.deleteSecretShare(GrpcSecretShareRequest.newBuilder()
            .setSecretId(secretId.toString())
            .build(), new StreamObserver<GrpcEmpty>() {
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
  }

  @Test
  void givenSortPropertyButInvalidDirection_whenGetSort_thenReturnSortAsc() {
    String expectedProperty = "key";
    String invalidDirection = "invalid";
    assertEquals(
        Sort.by(Sort.Direction.ASC, expectedProperty),
        secretShareService.getSort(expectedProperty, invalidDirection));
  }

  @Test
  void givenNoSortProperty_whenGetSort_thenReturnUnsorted() {
    String emptyProperty = "";
    String direction = Sort.Direction.ASC.toString();
    assertEquals(Sort.unsorted(), secretShareService.getSort(emptyProperty, direction));
  }

  @Test
  void givenValidConfiguration_whenGetSort_thenReturnExpectedContent() {
    String expectedProperty = "key";
    Sort.Direction expectedDirection = Sort.Direction.DESC;
    assertEquals(
        Sort.by(expectedDirection, expectedProperty),
        secretShareService.getSort(expectedProperty, expectedDirection.toString()));
  }
}
