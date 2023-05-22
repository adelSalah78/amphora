package io.carbynestack.amphora.common;

import com.google.protobuf.ByteString;
import io.carbynestack.amphora.common.grpc.*;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utils {

    public static GrpcOutputDeliveryObject convertToProtoOutputDeliveryObject(OutputDeliveryObject outputDeliveryObject){
        return GrpcOutputDeliveryObject.newBuilder()
                .setRShares(ByteString.copyFrom(outputDeliveryObject.getRShares()))
                .setSecretShares(ByteString.copyFrom(outputDeliveryObject.getSecretShares()))
                .setUShares(ByteString.copyFrom(outputDeliveryObject.getUShares()))
                .setVShares(ByteString.copyFrom(outputDeliveryObject.getVShares()))
                .setWShares(ByteString.copyFrom(outputDeliveryObject.getWShares()))
                .build();
    }

    public static MultiplicationExchangeObject convertFromProtoMultiplicationExchangeObject(GrpcMultiplicationExchangeObject grpcMultiplicationExchangeObject) {
        List<FactorPair> factorPairs = new ArrayList<>();
        for(GrpcFactorPair grpcFactorPair : grpcMultiplicationExchangeObject.getFactorPairList() ){
            factorPairs.add(FactorPair.of(BigInteger.valueOf(grpcFactorPair.getA()), BigInteger.valueOf(grpcFactorPair.getB())));
        }
        return new MultiplicationExchangeObject(
                UUID.fromString(grpcMultiplicationExchangeObject.getOperationId()),
                grpcMultiplicationExchangeObject.getPlayerId(),
                factorPairs
        );
    }

    public static GrpcMultiplicationExchangeObject convertToProtoMultiplicationExchangeObject(MultiplicationExchangeObject multiplicationExchangeObject) {
        List<GrpcFactorPair> grpcFactorPairs = new ArrayList<>();
        for(FactorPair factorPair : multiplicationExchangeObject.getInterimValues() ){
            grpcFactorPairs.add(
                    GrpcFactorPair.newBuilder()
                            .setA(factorPair.getA().intValue())
                            .setB(factorPair.getB().intValue())
                            .build()
            );
        }
        return GrpcMultiplicationExchangeObject.newBuilder().setOperationId(multiplicationExchangeObject.getOperationId().toString())
                .setPlayerId(multiplicationExchangeObject.getPlayerId())
                .addAllFactorPair(grpcFactorPairs).build();
    }

    public static SecretShare convertFromProtoSecretShare(GrpcSecretShare grpcSecretShare) {
        List<Tag> tags = createTagListFromProto(grpcSecretShare.getTagsList());

        return SecretShare.builder()
                .secretId(UUID.fromString(grpcSecretShare.getUuid()))
                .data(grpcSecretShare.getData().toByteArray())
                .tags(tags).build();
    }

    public static List<Tag> createTagListFromProto(List<GrpcTag> grpcTags){
        List<Tag> tags = new ArrayList<>();
        grpcTags.forEach(tag -> {
            Tag newTag = Tag.builder()
                    .key(tag.getKey()).value(tag.getValue())
                    .valueType(TagValueType.valueOf(tag.getValueType().name()))
                    .build();
            tags.add(newTag);
        });
        return tags;
    }

    public static List<GrpcTag> createTagListToProto(List<Tag> tags){
        List<GrpcTag> grpcTags = new ArrayList<>();
        tags.forEach(tag -> {
            GrpcTag newTag = GrpcTag.newBuilder()
                    .setKey(tag.getKey()).setValue(tag.getValue())
                    .setValueType(GrpcTagValueType.valueOf(tag.getValueType().name()))
                    .build();
            grpcTags.add(newTag);
        });
        return grpcTags;
    }

    public static GrpcSecretShare convertToProtoSecretShare(SecretShare secretShare){
        List<GrpcTag> grpcTags = new ArrayList<>();
        secretShare.getTags().forEach(tag -> {
            grpcTags.add(GrpcTag.newBuilder()
                    .setKey(tag.getKey())
                            .setValue(tag.getValue())
                            .setValueType(GrpcTagValueType.valueOf(tag.getValueType().name()))
                    .build()
            );
        });
        return GrpcSecretShare.newBuilder()
                .setData(ByteString.copyFrom(secretShare.getData()))
                .setUuid(secretShare.getSecretId().toString())
                .addAllTags(grpcTags).build();
    }

    public static MaskedInput convertFromGrpcMaskedInput(GrpcMaskedInput grpcMaskedInput){
        List<MaskedInputData> maskedInputDataList = new ArrayList<>();
        grpcMaskedInput.getDataList().forEach(grpcMaskedInputItem ->{
            maskedInputDataList.add(MaskedInputData.of(grpcMaskedInputItem.getValue().toByteArray()));
        });
        return new MaskedInput(
                UUID.fromString(grpcMaskedInput.getSecretId()),
                maskedInputDataList,
                createTagListFromProto(grpcMaskedInput.getTagsList())
        );
    }

    public static List<GrpcMetadata> createProtoMetadataList(List<Metadata> metadataList){
        List<GrpcMetadata> grpcMetadataList = new ArrayList<>();
        metadataList.forEach(metadata -> {
            grpcMetadataList.add(
            GrpcMetadata.newBuilder()
                    .setSecretId(metadata.getSecretId().toString())
                    .addAllTags(createTagListToProto(metadata.getTags()))
                    .build()
            );
        });
        return grpcMetadataList;
    }

    public static GrpcVerifiableSecretShare createProtoVerifiableSecretShare(SecretShare secretShare,OutputDeliveryObject outputDeliveryObject) {
        return GrpcVerifiableSecretShare.newBuilder()
                .setMetadata(
                        GrpcMetadata.newBuilder()
                                .setSecretId(secretShare.getSecretId().toString())
                                .addAllTags(createTagListToProto(secretShare.getTags()))
                                .build()
                )
                .setOutputDeliveryObject(convertToProtoOutputDeliveryObject(outputDeliveryObject))
                .build();
    }

    public static Channel createGrpcChannel(String amphoraServiceUri) {
        String[] addressAndPort = amphoraServiceUri.split(":");
        String grpcClientAddress = addressAndPort[0];
        String grpcClientPort = addressAndPort[1];
        return ManagedChannelBuilder.forAddress(grpcClientAddress, Integer.parseInt(grpcClientPort))
                .usePlaintext()
                .build();
    }
}
