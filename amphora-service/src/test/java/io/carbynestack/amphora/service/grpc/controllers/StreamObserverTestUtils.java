package io.carbynestack.amphora.service.grpc.controllers;

import io.carbynestack.amphora.common.grpc.*;
import io.grpc.stub.StreamObserver;

public class StreamObserverTestUtils {
    public static class IntraVcpStreamObserver implements StreamObserver{
        GrpcSecretShareResponse grpcSecretShareResponse;
        GrpcSecretShare secretShare;
        @Override
        public void onNext(Object o) {
            try {
                grpcSecretShareResponse = (GrpcSecretShareResponse) o;
            }
            catch (ClassCastException e){
                secretShare = (GrpcSecretShare) o;
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }
    }

    public static class SecretShareStreamObserver implements StreamObserver{

        GrpcMetadataPage grpcMetadataPage;
        GrpcVerifiableSecretShare grpcVerifiableSecretShare;
        @Override
        public void onNext(Object o) {
            try {
                grpcMetadataPage = (GrpcMetadataPage) o;
            }
            catch (ClassCastException e){
                grpcVerifiableSecretShare = (GrpcVerifiableSecretShare) o;
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }
    }


    public static class TagsStreamObserver implements StreamObserver{

        GrpcTagsResponse grpcTagsResponse;

        GrpcTag grpcTag;
        @Override
        public void onNext(Object o) {
            try {
                grpcTagsResponse = (GrpcTagsResponse) o;
            }
            catch (ClassCastException e){
                try {
                    grpcTag = (GrpcTag) o;
                }
                catch (ClassCastException cce){
                    // GrpcEmpty object
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }
    }
}
