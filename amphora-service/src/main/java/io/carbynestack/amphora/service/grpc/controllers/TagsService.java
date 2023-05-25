package io.carbynestack.amphora.service.grpc.controllers;

import io.carbynestack.amphora.common.Tag;
import io.carbynestack.amphora.common.TagValueType;
import io.carbynestack.amphora.common.Utils;
import io.carbynestack.amphora.common.grpc.*;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class TagsService extends TagsServiceGrpc.TagsServiceImplBase {

    private final StorageService storageService;
    @Override
    public void getTags(GrpcTagRequest request, StreamObserver<GrpcTagsResponse> responseObserver) {
        responseObserver.onNext(
                GrpcTagsResponse.newBuilder().addAllTags(
                        Utils.createTagListToProto(storageService.retrieveTags(UUID.fromString(request.getSecretId())))
                ).build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void createTag(GrpcTagRequest request, StreamObserver<GrpcEmpty> responseObserver) {
        if(request.getTag() == null || !request.hasTag()){
            throw new IllegalArgumentException("Tag must not be empty");
        }
        Tag tag = Tag.builder()
                .key(request.getTag().getKey())
                .value(request.getTag().getValue())
                .valueType(TagValueType.valueOf(request.getTag().getValueType().name()))
                .build();
        storageService.storeTag(UUID.fromString(request.getSecretId()), tag);
        responseObserver.onNext(GrpcEmpty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateTags(GrpcUpdateTagRequest request, StreamObserver<GrpcEmpty> responseObserver) {
        Assert.notEmpty(request.getTagsList(), "At least one tag must be given.");
        storageService.replaceTags(UUID.fromString(request.getSecretId()), Utils.createTagListFromProto(request.getTagsList()));
        responseObserver.onNext(GrpcEmpty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTag(GrpcTagRequest request, StreamObserver<GrpcTag> responseObserver) {
        Tag tag = storageService.retrieveTag(UUID.fromString(request.getSecretId()), request.getTagKey());
        responseObserver.onNext(
                GrpcTag.newBuilder()
                        .setKey(tag.getKey())
                        .setValue(tag.getValue())
                        .setValueType(GrpcTagValueType.valueOf(tag.getValueType().name()))
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void putTag(GrpcTagRequest request, StreamObserver<GrpcEmpty> responseObserver) {
        if(request.getTag() == null || !request.hasTag()){
            throw new IllegalArgumentException("Tag must not be empty");
        }
        GrpcTag grpcTag = request.getTag();
        if (!request.getTagKey().equals(grpcTag.getKey())) {
            throw new IllegalArgumentException(
                    String.format("The defined key and tag data do not match.%n%s <> %s", request.getTagKey(), grpcTag.getKey()));
        }
        storageService.updateTag(UUID.fromString(request.getSecretId()),
                Tag.builder()
                        .key(grpcTag.getKey())
                        .value(grpcTag.getValue())
                        .valueType(TagValueType.valueOf(grpcTag.getValueType().name()))
                        .build()
        );
        responseObserver.onNext(GrpcEmpty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTag(GrpcTagRequest request, StreamObserver<GrpcEmpty> responseObserver) {
        storageService.deleteTag(UUID.fromString(request.getSecretId()), request.getTagKey());
        responseObserver.onNext(GrpcEmpty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
