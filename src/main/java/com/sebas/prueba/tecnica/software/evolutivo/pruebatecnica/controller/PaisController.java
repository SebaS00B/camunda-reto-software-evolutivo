package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paises")
public class PaisController {
@GetMapping
  public List<String> listar() { return List.of("Ecuador","Colombia","Perú"); }
}

