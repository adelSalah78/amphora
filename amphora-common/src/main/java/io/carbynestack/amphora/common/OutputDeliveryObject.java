/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.carbynestack.mpspdz.integration.MpSpdzIntegrationUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * An entity class used send the Amphora instance's secret shares to the requesting client. <br>
 * This format allows the client to verify the integrity of the Amphora instance as described in the
 * following paper:<br>
 * Ivan Damgård, Kasper Damgård, Kurt Nielsen, Peter Sebastian Nordholt, Tomas Toft: <br>
 * Confidential Benchmarking based on Multiparty Computation. IACR Cryptology ePrint Archive 2015:
 * 1006 (2015) <br>
 * https://eprint.iacr.org/2015/1006
 *
 * <p>The object is composed of the following values: <br>
 *
 * <ul>
 *   <li>The secrets-shares (&lt;y<sub>i</sub>&gt;)
 *   <li>Shares from random 'r<sub>i</sub>'
 *   <li>Shares from random 'v<sub>i</sub>'
 *   <li>Shares from 'authentication tag' 'w<sub>i</sub>' where &lt;w<sub>i</sub>&gt; =
 *       &lt;y<sub>i</sub>r<sub>i</sub>&gt;
 *   <li>Shares from 'authentication tag' 'u<sub>i</sub>' where &lt;u<sub>i</sub>&gt; =
 *       &lt;v<sub>i</sub>r<sub>i</sub>&gt;
 * </ul>
 *
 * <p>All data is stored in the gfp byte representation as used by the SPDZ based MPC
 * implementations. Each field is of the type byte array and must be a multiple of the word length
 * as defined by the {@link MpSpdzIntegrationUtils} (see {@link MpSpdzIntegrationUtils#WORD_WIDTH}).
 * Since there must be one <i>random r</i>, <i>random v</i>, <i>authentication value w</i> and
 * <i>authentication value u</i> for each word of the secret, all fields must be of the same length.
 *
 * <p>New {@link OutputDeliveryObject}s can be created using the {@link
 * OutputDeliveryObject#builder() builder}.
 */
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
public class OutputDeliveryObject extends Metadata {
  private static final long serialVersionUID = -8201039361986746504L;
  /** The actual share of the secret. */
  @JsonProperty(value = "secretShares", required = true)
  byte[] secretShares;
  /** The shares for the <i>random r</i>s. */
  @JsonProperty(value = "rShares", required = true)
  byte[] rShares;
  /** The shares for the <i>random v</i>s. */
  @JsonProperty(value = "vShares", required = true)
  byte[] vShares;
  /** The shares for the <i>authentication values w</i>. */
  @JsonProperty(value = "wShares", required = true)
  byte[] wShares;
  /** The shares for the <i>authentication values w</i>. */
  @JsonProperty(value = "uShares", required = true)
  byte[] uShares;

  /**
   * Implementation of the auto-generated {@link OutputDeliveryObjectBuilder}.<br>
   * This class is filled with further logic by <i>lombok</i>. Only {@link
   * OutputDeliveryObjectBuilderImpl#build()} is pre-implemented to provide custom logic.
   */
  static final class OutputDeliveryObjectBuilderImpl
      extends OutputDeliveryObject.OutputDeliveryObjectBuilder<
          OutputDeliveryObject, OutputDeliveryObject.OutputDeliveryObjectBuilderImpl> {
    /**
     * Creates a new {@link OutputDeliveryObject} build from the current configuration of this
     * builder.
     *
     * @return The new {@link OutputDeliveryObject}
     * @throws IllegalArgumentException if the given data for the shares is not of the same length.
     */
    public OutputDeliveryObject build() {
      if (super.rShares.length != super.secretShares.length
          || super.vShares.length != super.secretShares.length
          || super.wShares.length != super.secretShares.length
          || super.uShares.length != super.secretShares.length) {
        throw new IllegalArgumentException("The provided shares must be of the same length");
      }
      return new OutputDeliveryObject(this);
    }
  }
}
