package io.carbynestack.amphora.service.grpc.controllers;

import io.carbynestack.amphora.common.grpc.GrpcSecretShare;
import io.carbynestack.amphora.common.grpc.GrpcSecretShareResponse;
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
}
