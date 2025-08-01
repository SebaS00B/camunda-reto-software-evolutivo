package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RepositoryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CamundaDeployer {

  private final RepositoryService repositoryService;

  @PostConstruct
  public void deployProcess() {
    repositoryService.createDeployment()
      .name("Auto-deploy purchase-request-process")
      .addClasspathResource("processes/purchase-request-process.bpmn")
      .addClasspathResource("processes/purchase-approval-rules.dmn")
      .deploy();
  }
}