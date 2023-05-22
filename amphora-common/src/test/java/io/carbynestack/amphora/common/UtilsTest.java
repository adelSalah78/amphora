package io.carbynestack.amphora.common;

import com.google.protobuf.ByteString;
import io.carbynestack.amphora.common.grpc.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

public class UtilsTest {

    @Test
    public void testConvertToProtoOutputDeliveryObject() {
        OutputDeliveryObject outputDeliveryObject = new OutputDeliveryObject(new byte[]{4, 5, 6},
                new byte[]{1, 2, 3},
                new byte[]{10, 11, 12},
                new byte[]{13, 14, 15},
                new byte[]{7, 8, 9}
                );

        GrpcOutputDeliveryObject protoObject = Utils.convertToProtoOutputDeliveryObject(outputDeliveryObject);

        Assertions.assertArrayEquals(outputDeliveryObject.getRShares(), protoObject.getRShares().toByteArray());
        Assertions.assertArrayEquals(outputDeliveryObject.getSecretShares(), protoObject.getSecretShares().toByteArray());
        Assertions.assertArrayEquals(outputDeliveryObject.getUShares(), protoObject.getUShares().toByteArray());
        Assertions.assertArrayEquals(outputDeliveryObject.getVShares(), protoObject.getVShares().toByteArray());
        Assertions.assertArrayEquals(outputDeliveryObject.getWShares(), protoObject.getWShares().toByteArray());
    }

    @Test
    public void testConvertFromProtoMultiplicationExchangeObject() {
        GrpcMultiplicationExchangeObject grpcObject = GrpcMultiplicationExchangeObject.newBuilder()
                .setOperationId("123e4567-e89b-12d3-a456-426655440000")
                .setPlayerId(1)
                .addFactorPair(GrpcFactorPair.newBuilder().setA(2).setB(3).build())
                .addFactorPair(GrpcFactorPair.newBuilder().setA(4).setB(5).build())
                .build();

        MultiplicationExchangeObject exchangeObject = Utils.convertFromProtoMultiplicationExchangeObject(grpcObject);

        Assertions.assertEquals(UUID.fromString(grpcObject.getOperationId()), exchangeObject.getOperationId());
        Assertions.assertEquals(grpcObject.getPlayerId(), exchangeObject.getPlayerId());
        Assertions.assertEquals(2, exchangeObject.getInterimValues().size());
        Assertions.assertEquals(BigInteger.valueOf(2), exchangeObject.getInterimValues().get(0).getA());
        Assertions.assertEquals(BigInteger.valueOf(3), exchangeObject.getInterimValues().get(0).getB());
        Assertions.assertEquals(BigInteger.valueOf(4), exchangeObject.getInterimValues().get(1).getA());
        Assertions.assertEquals(BigInteger.valueOf(5), exchangeObject.getInterimValues().get(1).getB());
    }

    @Test
    public void testConvertToProtoMultiplicationExchangeObject() {
        MultiplicationExchangeObject exchangeObject = new MultiplicationExchangeObject(
                UUID.fromString("123e4567-e89b-12d3-a456-426655440000"),
                1,
                Arrays.asList(
                        FactorPair.of(BigInteger.valueOf(2), BigInteger.valueOf(3)),
                        FactorPair.of(BigInteger.valueOf(4), BigInteger.valueOf(5))
                )
        );

        GrpcMultiplicationExchangeObject grpcObject = Utils.convertToProtoMultiplicationExchangeObject(exchangeObject);

        Assertions.assertEquals(exchangeObject.getOperationId().toString(), grpcObject.getOperationId());
        Assertions.assertEquals(exchangeObject.getPlayerId(), grpcObject.getPlayerId());
        Assertions.assertEquals(2, grpcObject.getFactorPairCount());
        Assertions.assertEquals(2, grpcObject.getFactorPair(0).getA());
        Assertions.assertEquals(3, grpcObject.getFactorPair(0).getB());
        Assertions.assertEquals(4, grpcObject.getFactorPair(1).getA());
        Assertions.assertEquals(5, grpcObject.getFactorPair(1).getB());
    }

    @Test
    public void testConvertFromProtoSecretShare() {
        Random random = new Random();
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);

        GrpcSecretShare grpcSecretShare = GrpcSecretShare.newBuilder()
                .setUuid("123e4567-e89b-12d3-a456-426655440000")
                .setData(ByteString.copyFrom(randomBytes))
                .addTags(GrpcTag.newBuilder().setKey("tag1").setValue("value1").setValueType(GrpcTagValueType.STRING).build())
                .addTags(GrpcTag.newBuilder().setKey("tag2").setValue("value2").setValueType(GrpcTagValueType.STRING).build())
                .build();

        SecretShare secretShare = Utils.convertFromProtoSecretShare(grpcSecretShare);

        Assertions.assertEquals(UUID.fromString(grpcSecretShare.getUuid()), secretShare.getSecretId());
        Assertions.assertArrayEquals(randomBytes, secretShare.getData());
        Assertions.assertEquals(2, secretShare.getTags().size());
        Assertions.assertEquals("tag1", secretShare.getTags().get(0).getKey());
        Assertions.assertEquals("value1", secretShare.getTags().get(0).getValue());
        Assertions.assertEquals(TagValueType.STRING, secretShare.getTags().get(0).getValueType());
        Assertions.assertEquals("tag2", secretShare.getTags().get(1).getKey());
        Assertions.assertEquals("value2", secretShare.getTags().get(1).getValue());
        Assertions.assertEquals(TagValueType.STRING, secretShare.getTags().get(1).getValueType());
    }

    @Test
    public void testCreateTagListFromProto() {
        List<GrpcTag> grpcTags = new ArrayList<>();
        grpcTags.add(GrpcTag.newBuilder().setKey("tag1").setValue("value1").setValueType(GrpcTagValueType.STRING).build());
        grpcTags.add(GrpcTag.newBuilder().setKey("tag2").setValue("value2").setValueType(GrpcTagValueType.STRING).build());

        List<Tag> tags = Utils.createTagListFromProto(grpcTags);

        Assertions.assertEquals(2, tags.size());
        Assertions.assertEquals("tag1", tags.get(0).getKey());
        Assertions.assertEquals("value1", tags.get(0).getValue());
        Assertions.assertEquals(TagValueType.STRING, tags.get(0).getValueType());
        Assertions.assertEquals("tag2", tags.get(1).getKey());
        Assertions.assertEquals("value2", tags.get(1).getValue());
        Assertions.assertEquals(TagValueType.STRING, tags.get(1).getValueType());
    }

    @Test
    public void testCreateTagListToProto() {
        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.builder().key("tag1").value("value1").valueType(TagValueType.STRING).build());
        tags.add(Tag.builder().key("tag2").value("value2").valueType(TagValueType.STRING).build());

        List<GrpcTag> grpcTags = Utils.createTagListToProto(tags);

        Assertions.assertEquals(2, grpcTags.size());
        Assertions.assertEquals("tag1", grpcTags.get(0).getKey());
        Assertions.assertEquals("value1", grpcTags.get(0).getValue());
        Assertions.assertEquals(GrpcTagValueType.STRING, grpcTags.get(0).getValueType());
        Assertions.assertEquals("tag2", grpcTags.get(1).getKey());
        Assertions.assertEquals("value2", grpcTags.get(1).getValue());
        Assertions.assertEquals(GrpcTagValueType.STRING, grpcTags.get(1).getValueType());
    }

    @Test
    public void testConvertToProtoSecretShare() {
        Random random = new Random();
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        SecretShare secretShare = SecretShare.builder()
                .secretId(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))
                .data(randomBytes)
                .tags(Arrays.asList(
                        Tag.builder().key("tag1").value("value1").valueType(TagValueType.STRING).build(),
                        Tag.builder().key("tag2").value("value2").valueType(TagValueType.STRING).build()
                ))
                .build();

        GrpcSecretShare grpcSecretShare = Utils.convertToProtoSecretShare(secretShare);

        Assertions.assertEquals(secretShare.getSecretId().toString(), grpcSecretShare.getUuid());
        Assertions.assertArrayEquals(randomBytes, grpcSecretShare.getData().toByteArray());
        Assertions.assertEquals(2, grpcSecretShare.getTagsCount());
        Assertions.assertEquals("tag1", grpcSecretShare.getTags(0).getKey());
        Assertions.assertEquals("value1", grpcSecretShare.getTags(0).getValue());
        Assertions.assertEquals(GrpcTagValueType.STRING, grpcSecretShare.getTags(0).getValueType());
        Assertions.assertEquals("tag2", grpcSecretShare.getTags(1).getKey());
        Assertions.assertEquals("value2", grpcSecretShare.getTags(1).getValue());
        Assertions.assertEquals(GrpcTagValueType.STRING, grpcSecretShare.getTags(1).getValueType());
    }

    @Test
    public void testConvertFromGrpcMaskedInput() {
        Random random = new Random();
        byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);

        GrpcMaskedInput grpcMaskedInput = GrpcMaskedInput.newBuilder()
                .setSecretId("123e4567-e89b-12d3-a456-426655440000")
                .addData(GrpcMaskedInputData.newBuilder().setValue(ByteString.copyFrom(randomBytes)).build())
                .addTags(GrpcTag.newBuilder().setKey("tag1").setValue("value1").setValueType(GrpcTagValueType.STRING).build())
                .addTags(GrpcTag.newBuilder().setKey("tag2").setValue("value2").setValueType(GrpcTagValueType.STRING).build())
                .build();

        MaskedInput maskedInput = Utils.convertFromGrpcMaskedInput(grpcMaskedInput);

        Assertions.assertEquals(UUID.fromString(grpcMaskedInput.getSecretId()), maskedInput.getSecretId());
        Assertions.assertEquals(1, maskedInput.getData().size());
        Assertions.assertArrayEquals(randomBytes, maskedInput.getData().get(0).getValue());
        Assertions.assertEquals(2, maskedInput.getTags().size());
        Assertions.assertEquals("tag1", maskedInput.getTags().get(0).getKey());
        Assertions.assertEquals("value1", maskedInput.getTags().get(0).getValue());
        Assertions.assertEquals(TagValueType.STRING, maskedInput.getTags().get(0).getValueType());
        Assertions.assertEquals("tag2", maskedInput.getTags().get(1).getKey());
        Assertions.assertEquals("value2", maskedInput.getTags().get(1).getValue());
        Assertions.assertEquals(TagValueType.STRING, maskedInput.getTags().get(1).getValueType());
    }

    @Test
    public void testCreateProtoMetadataList() {
        List<Metadata> metadataList = Arrays.asList(
                 Metadata.builder().secretId(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))
                         .tags(Arrays.asList(
                                 Tag.builder().key("tag1").value("value1").valueType(TagValueType.STRING).build(),
                                 Tag.builder().key("tag2").value("value2").valueType(TagValueType.STRING).build()
                         )).build(),
                Metadata.builder().secretId(UUID.fromString("987e6543-e21b-34d5-a654-654444655444")).tags(Arrays.asList(
                        Tag.builder().key("tag3").value("value3").valueType(TagValueType.STRING).build(),
                        Tag.builder().key("tag4").value("value4").valueType(TagValueType.STRING).build()
                )
        ).build());

        List<GrpcMetadata> grpcMetadataList = Utils.createProtoMetadataList(metadataList);

        Assertions.assertEquals(2, grpcMetadataList.size());

        GrpcMetadata grpcMetadata1 = grpcMetadataList.get(0);
        Assertions.assertEquals("123e4567-e89b-12d3-a456-426655440000", grpcMetadata1.getSecretId());
        Assertions.assertEquals(2, grpcMetadata1.getTagsCount());
        Assertions.assertEquals("tag1", grpcMetadata1.getTags(0).getKey());
        Assertions.assertEquals("value1", grpcMetadata1.getTags(0).getValue());
        Assertions.assertEquals(GrpcTagValueType.STRING, grpcMetadata1.getTags(0).getValueType());
        Assertions.assertEquals("tag2", grpcMetadata1.getTags(1).getKey());
        Assertions.assertEquals("value2", grpcMetadata1.getTags(1).getValue());
        Assertions.assertEquals(GrpcTagValueType.STRING, grpcMetadata1.getTags(1).getValueType());

        GrpcMetadata grpcMetadata2 = grpcMetadataList.get(1);
        Assertions.assertEquals("987e6543-e21b-34d5-a654-654444655444", grpcMetadata2.getSecretId());
        Assertions.assertEquals(2, grpcMetadata2.getTagsCount());
        Assertions.assertEquals("tag3", grpcMetadata2.getTags(0).getKey());
        Assertions.assertEquals("value3", grpcMetadata2.getTags(0).getValue());
        Assertions.assertEquals(GrpcTagValueType.STRING, grpcMetadata2.getTags(0).getValueType());
        Assertions.assertEquals("tag4", grpcMetadata2.getTags(1).getKey());
        Assertions.assertEquals("value4", grpcMetadata2.getTags(1).getValue());
        Assertions.assertEquals(GrpcTagValueType.STRING, grpcMetadata2.getTags(1).getValueType());
    }
}
