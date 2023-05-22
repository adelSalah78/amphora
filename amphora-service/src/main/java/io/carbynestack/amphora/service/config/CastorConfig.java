/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.carbynestack.amphora.service.config;

import io.carbynestack.castor.client.download.CastorIntraVcpClient;
import io.carbynestack.castor.client.download.DefaultCastorIntraVcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {CastorClientProperties.class})
public class CastorConfig {

  @Bean
  CastorIntraVcpClient castorClient(CastorClientProperties castorProperties) {
    DefaultCastorIntraVcpClient.Builder clientBuilder =
        DefaultCastorIntraVcpClient.builder(castorProperties.getServiceUri());
    clientBuilder.withoutSslCertificateValidation();
    return clientBuilder.build();
  }
}
