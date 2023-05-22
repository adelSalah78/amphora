package io.carbynestack.amphora.service.config;


import io.carbynestack.castor.common.exceptions.CastorClientException;
import io.carbynestack.castor.common.exceptions.CastorServiceException;
import io.grpc.*;

public class GlobalGrpcExceptionHandler implements ServerInterceptor {
    String address;

    public GlobalGrpcExceptionHandler(String address) {
        this.address = address;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        try {
            ServerCall.Listener<ReqT> delegate = serverCallHandler.startCall(serverCall, metadata);
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {
                @Override
                public void onMessage(ReqT message) {
                    try {
                        super.onMessage(message); // Here onNext is called (in case of client streams)
                    } catch (Exception e) {
                        handleEndpointException(e, serverCall);
                    }
                }

                @Override
                public void onHalfClose() {
                    try {
                        super.onHalfClose(); // Here onCompleted is called (in case of client streams)
                    } catch (Exception e) {
                        handleEndpointException(e, serverCall);
                    }
                }
            };
        } catch (Exception t) {
            return handleInterceptorException(
                    t, serverCall, Status.INTERNAL, "An exception occurred in a **subsequent** interceptor");
        }
    }

    private <ReqT, RespT> void handleEndpointException(
            Exception t, ServerCall<ReqT, RespT> serverCall) {
        String desc = "An exception occurred in the endpoint implementation";
        if (t instanceof CastorClientException || t instanceof CastorServiceException) {
            desc = t.getMessage();
        }
        serverCall.close(Status.INTERNAL.withCause(t).withDescription(desc), new Metadata());
    }

    private <ReqT, RespT> ServerCall.Listener<ReqT> handleInterceptorException(
            Exception t, ServerCall<ReqT, RespT> serverCall, Status status, String desc) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("Error_Message", Metadata.ASCII_STRING_MARSHALLER), desc);
        serverCall.close(status.withCause(t).withDescription(desc), metadata);

        return new ServerCall.Listener<ReqT>() {
            // no-op
        };
    }
}
