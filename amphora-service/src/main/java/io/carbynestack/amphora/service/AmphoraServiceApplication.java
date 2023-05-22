/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/amphora.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.carbynestack.amphora.service;

import io.carbynestack.amphora.service.grpc.ServerStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class AmphoraServiceApplication {

  public static void main(String[] args) throws IOException, InterruptedException {
    ConfigurableApplicationContext context =
            SpringApplication.run(AmphoraServiceApplication.class, args);
    context.getBean(ServerStarter.class).startGrpcServer();
  }
}
