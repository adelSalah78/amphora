package io.carbynestack.amphora.service.grpc.controllers;

import io.carbynestack.amphora.common.*;
import io.carbynestack.amphora.common.SecretShare;
import io.carbynestack.amphora.common.grpc.*;
import io.carbynestack.amphora.service.calculation.OutputDeliveryService;
import io.carbynestack.amphora.service.persistence.metadata.StorageService;
import io.grpc.stub.StreamObserver;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.carbynestack.amphora.common.rest.AmphoraRestApiEndpoints.CRITERIA_SEPARATOR;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@AllArgsConstructor
public class SecretShareService extends SecretShareServiceGrpc.SecretShareServiceImplBase {

    private final StorageService storageService;
    private final OutputDeliveryService outputDeliveryService;

    @SneakyThrows
    @Override
    public void getObjectList(GrpcGetObjectListRequest request, StreamObserver<GrpcMetadataPage> responseObserver) {
        List<TagFilter> tagFilters =
                StringUtils.hasText(request.getFilter()) ? parseTagFilters(request.getFilter()) : Collections.emptyList();
        Sort sort = getSort(request.getSortProperty(), request.getSortDirection());
        Page<Metadata> secretSpringPage;

        if (isEmpty(tagFilters)) {
            secretSpringPage =
                    (request.getPageSize() > 0 || request.getPageNumber() > 0)
                            ? storageService.getSecretList(getPageRequest(request.getPageNumber(), request.getPageSize(), sort))
                            : storageService.getSecretList(sort);
        } else {
            secretSpringPage =
                    (request.getPageSize() > 0 || request.getPageNumber() > 0)
                            ? storageService.getSecretList(tagFilters, getPageRequest(request.getPageNumber(), request.getPageSize(), sort))
                            : storageService.getSecretList(tagFilters, sort);
        }

        responseObserver.onNext(
                GrpcMetadataPage.newBuilder()
                        .addAllContent(Utils.createProtoMetadataList(secretSpringPage.getContent()))
                        .setNumber(secretSpringPage.getNumber())
                        .setSize(secretSpringPage.getSize())
                        .setTotalElements(secretSpringPage.getTotalElements())
                        .setTotalPages(secretSpringPage.getTotalPages())
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void getSecretShare(GrpcSecretShareRequest request, StreamObserver<GrpcVerifiableSecretShare> responseObserver) {
        Assert.notNull(request.getRequestId(), "Request identifier must not be omitted");
        SecretShare secretShare = storageService.getSecretShare(UUID.fromString(request.getSecretId()));
        OutputDeliveryObject outputDeliveryObject =
                outputDeliveryService.computeOutputDeliveryObject(secretShare, UUID.fromString(request.getRequestId()));
        responseObserver.onNext(
                Utils.createProtoVerifiableSecretShare(secretShare,outputDeliveryObject)
        );
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSecretShare(GrpcSecretShareRequest request, StreamObserver<GrpcEmpty> responseObserver) {
        storageService.deleteSecret(UUID.fromString(request.getSecretId()));
        responseObserver.onNext(GrpcEmpty.newBuilder().build());
        responseObserver.onCompleted();
    }

    Sort getSort(String sortProperty, String sortDirection) {
        if (StringUtils.hasText(sortProperty)) {
            return Try.of(() -> Sort.Direction.fromString(sortDirection))
                    .map(direction -> Sort.by(direction, sortProperty))
                    .getOrElse(Sort.by(sortProperty));
        }
        return Sort.unsorted();
    }

    PageRequest getPageRequest(int pageNumber, int pageSize, @NonNull Sort sort) {
        return PageRequest.of(Math.max(0, pageNumber), Math.max(1, pageSize), sort);
    }

    List<TagFilter> parseTagFilters(String filter) throws UnsupportedEncodingException {
        List<TagFilter> tagFilters = new ArrayList<>();
        for (String tagFilterString : filter.split(CRITERIA_SEPARATOR)) {
            tagFilters.add(TagFilter.fromString(tagFilterString));
        }
        return tagFilters;
    }
}
