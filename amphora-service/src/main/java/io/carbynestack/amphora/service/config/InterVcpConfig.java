/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.amphora.service.config;

import io.carbynestack.amphora.client.DefaultAmphoraInterVcpClient;
import io.carbynestack.amphora.common.exceptions.AmphoraClientException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackageClasses = {CastorClientProperties.class})
public class InterVcpConfig {

  @Bean
  DefaultAmphoraInterVcpClient interVcpClient(AmphoraServiceProperties serviceProperties)
      throws AmphoraClientException {
    List<String> serviceUrls = new ArrayList<>();
    serviceProperties.getVcPartners().forEach(vcp -> serviceUrls.add(vcp.getServiceUri().getHost() + ":" + vcp.getServiceUri().getPort()));
    return DefaultAmphoraInterVcpClient.Builder().serviceUrls(serviceUrls)
        .build();
  }
}
