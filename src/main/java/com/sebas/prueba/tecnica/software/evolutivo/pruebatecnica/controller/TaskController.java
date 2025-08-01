package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import org.camunda.bpm.engine.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/tasks")
public class TaskController {

  private final TaskService taskService;

  @GetMapping("/inbox")
  public String inbox(Model model) {
    log.info("✉️ Mostrando bandeja de tareas");
    var tasks = taskService.createTaskQuery()
                           .taskAssignee("demo")  // o tu usuario
                           .list();
    model.addAttribute("tasks", tasks);
    return "tasks/inbox";
  }

}