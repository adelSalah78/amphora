/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.service.grpc.controllers;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.carbynestack.amphora.common.Tag;
import io.carbynestack.amphora.common.TagValueType;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.grpc.GrpcTag;
import io.carbynestack.amphora.common.grpc.GrpcTagRequest;
import io.carbynestack.amphora.common.grpc.GrpcTagValueType;
import io.carbynestack.amphora.common.grpc.GrpcUpdateTagRequest;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagsControllerTest {
  private final UUID testSecretId = UUID.fromString("3bcf8308-8f50-4d24-a37b-b0075bb5e779");
  private final Tag testTag =
      Tag.builder().key("key").value("value").valueType(TagValueType.STRING).build();

  @Mock private StorageService storageService;

  @InjectMocks private TagsService tagsService;

  @Test
  void givenSuccessfulRequest_whenGetTags_thenReturnOkWithExpectedContent() {
    List<Tag> expectedList = singletonList(testTag);

    StreamObserverTestUtils.TagsStreamObserver streamObserver = new StreamObserverTestUtils.TagsStreamObserver();

    when(storageService.retrieveTags(testSecretId)).thenReturn(expectedList);
    assertDoesNotThrow(()->tagsService.getTags(GrpcTagRequest.newBuilder()
            .setSecretId(testSecretId.toString())
            .build(),streamObserver));
    assertEquals(expectedList, Utils.createTagListFromProto(streamObserver.grpcTagsResponse.getTagsList()));
  }

  @Test
  void givenTagIsNull_whenCreateTag_thenThrowIllegalArgumentException() {
    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class, () -> tagsService.createTag(GrpcTagRequest.newBuilder()
                        .setSecretId(testSecretId.toString()).build(), null));
    verify(storageService, never()).storeTag(any(), any());
    assertEquals("Tag must not be empty", iae.getMessage());
  }

  @Test
  void givenSuccessfulRequest_whenCreateTag_thenReturnCreatedWithExpectedContent() {
    StreamObserverTestUtils.TagsStreamObserver streamObserver = new StreamObserverTestUtils.TagsStreamObserver();
    assertDoesNotThrow(()->tagsService.createTag(GrpcTagRequest.newBuilder()
                    .setSecretId(testSecretId.toString())
                    .setTag(GrpcTag.newBuilder().setKey(testTag.getKey()).setValue(testTag.getValue())
                            .setValueType(GrpcTagValueType.valueOf(testTag.getValueType().name())).build())
                    .build()
            ,streamObserver));
    verify(storageService, times(1)).storeTag(testSecretId, testTag);
  }

  @Test
  void givenTagsAreEmpty_whenUpdateTags_thenThrowIllegalArgumentException() {
    List<GrpcTag> emptyTags = emptyList();
    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class,
            () -> tagsService.updateTags(GrpcUpdateTagRequest.newBuilder()
                            .setSecretId(testSecretId.toString())
                            .addAllTags(emptyTags)
                    .build(),null));
    verify(storageService, never()).replaceTags(any(), any());
    assertEquals("At least one tag must be given.", iae.getMessage());
  }

  @Test
  void givenSuccessfulRequest_whenUpdateTags_thenReturnCreatedWithExpectedContent() {
    StreamObserverTestUtils.TagsStreamObserver tagsStreamObserver = new StreamObserverTestUtils.TagsStreamObserver();
    List<Tag> newTagList = singletonList(testTag);
    assertDoesNotThrow(()->tagsService.updateTags(
            GrpcUpdateTagRequest.newBuilder()
                    .setSecretId(testSecretId.toString())
                    .addAllTags(Utils.createTagListToProto(newTagList))
                    .build(),tagsStreamObserver
    ));
    verify(storageService, times(1)).replaceTags(testSecretId, newTagList);
  }

  @Test
  void givenSuccessfulRequest_whenGetTag_thenReturnOkWithExpectedContent() {
    StreamObserverTestUtils.TagsStreamObserver tagsStreamObserver = new StreamObserverTestUtils.TagsStreamObserver();
    when(storageService.retrieveTag(testSecretId, testTag.getKey())).thenReturn(testTag);

    assertDoesNotThrow(()->tagsService.getTag(GrpcTagRequest.newBuilder()
            .setTagKey(testTag.getKey())
            .setSecretId(testSecretId.toString())
            .build(),tagsStreamObserver));
    assertEquals(testTag.getKey(), tagsStreamObserver.grpcTag.getKey());
    assertEquals(testTag.getValue(), tagsStreamObserver.grpcTag.getValue());
    assertEquals(testTag.getValueType().name(), tagsStreamObserver.grpcTag.getValueType().name());
  }

  @Test
  void givenTagIsNull_whenPutTag_thenTrowIllegalArgumentException() {
    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class, () -> tagsService.putTag(GrpcTagRequest.newBuilder()
                        .setTagKey(testTag.getKey())
                        .setSecretId(testSecretId.toString())
                        .build(), null));
    verify(storageService, never()).updateTag(testSecretId, testTag);
    assertEquals("Tag must not be empty", iae.getMessage());
  }

  @Test
  void givenTagConfigurationDoesNotMatchAddressedKey_whenPutTag_thenTrowIllegalArgumentException() {
    StreamObserverTestUtils.TagsStreamObserver tagsStreamObserver = new StreamObserverTestUtils.TagsStreamObserver();
    String nonMatchingKey = testTag.getKey() + "_different";
    IllegalArgumentException iae =
        assertThrows(
            IllegalArgumentException.class,
            () -> tagsService.putTag(GrpcTagRequest.newBuilder()
                            .setTag(
                                    GrpcTag.newBuilder()
                                    .setKey(testTag.getKey())
                                            .setValue(testTag.getValue())
                                            .setValueType(GrpcTagValueType.valueOf(testTag.getValueType().name()))
                                    .build()
                            )
                    .setSecretId(testSecretId.toString())
                            .setTagKey(nonMatchingKey)
                    .build(),tagsStreamObserver));
    verify(storageService, never()).updateTag(testSecretId, testTag);
    String exceptionMessage = iae.getMessage();
    assertEquals(
        String.format(
            "The defined key and tag data do not match.%n%s <> %s", nonMatchingKey, testTag.getKey()),
        iae.getMessage());
  }

  @Test
  void givenSuccessfulRequest_whenPutTag_thenReturnOk() {
    StreamObserverTestUtils.TagsStreamObserver tagsStreamObserver = new StreamObserverTestUtils.TagsStreamObserver();
    assertDoesNotThrow(()->tagsService.putTag(GrpcTagRequest.newBuilder()
            .setTag(GrpcTag.newBuilder()
                    .setKey(testTag.getKey())
                    .setValue(testTag.getValue())
                    .setValueType(GrpcTagValueType.valueOf(testTag.getValueType().name()))
                    .build())
            .setTagKey(testTag.getKey())
            .setSecretId(testSecretId.toString())
            .build(),tagsStreamObserver));
    verify(storageService, times(1)).updateTag(testSecretId, testTag);
  }

  @Test
  void givenSuccessfulRequest_whenDeleteTag_thenReturnOk() {
    StreamObserverTestUtils.TagsStreamObserver tagsStreamObserver = new StreamObserverTestUtils.TagsStreamObserver();
    assertDoesNotThrow(()->tagsService.deleteTag(GrpcTagRequest.newBuilder()
            .setSecretId(testSecretId.toString())
            .setTagKey(testTag.getKey()).build(),tagsStreamObserver));
    verify(storageService, times(1)).deleteTag(testSecretId, testTag.getKey());
  }
}
