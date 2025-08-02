# 🚀 BPM Consultant Demo - Software Evolutivo

Reto Técnico Fue desarrollado por Sebastian Burgos para la posición BPM Consultant - Camunda

Proyecto completo de automatización de procesos de negocio con **Camunda BPM**, validaciones avanzadas, reglas DMN, temporizadores, notificaciones, y dashboard ejecutivo.

---

## 📋 Resumen Ejecutivo

Este proyecto implementa un sistema completo de **gestión de solicitudes de compra**, demostrando:

- Automatización de procesos BPMN
- Validaciones multicapa (frontend, backend y DMN)
- Temporizadores y recordatorios
- Envío de notificaciones inteligentes
- Dashboard y métricas en tiempo real
- Arquitectura limpia (Hexagonal)
- APIs RESTful para interacción externa

Psdt: Para hacer uso de los recordatorios por e-mail, en el archivo application.properties, se debe introducir un correo de preferencia Gmail, con la contraseña de aplicación.
---

## 🏗 Arquitectura y Stack Tecnológico

### 🔧 Backend
- **Spring Boot 3.5.4**
- **Camunda Platform 7.23.0** (Motor embebido + External Task Pattern)
- Spring Data JPA, Lombok, Spring Mail, Spring Validation
- H2 (modo demo) / PostgreSQL (producción)

### 🎨 Frontend
- Thymeleaf + Bootstrap 5.3.2 + CSS personalizado
- Validaciones UX en tiempo real

### ⚙ Procesos y Reglas
- BPMN 2.0 (purchase-request-process.bpmn)
- DMN 1.3 (purchase-approval-rules.dmn)
- Camunda Modeler

---

## ✨ Look & Feel Personalizado

- 🎨 Estilo moderno
- 📱 Responsive con Bootstrap + CSS Grid
- ⚡ Animaciones suaves
- 📊 Dashboard interactivo con auto-refresh
- UX intuitiva y visual

---

## 📦 Estructura del Proyecto

\`\`\`
src/main/java/com/sebas/prueba/tecnica/
├── controller/         # REST APIs y Web Controllers
├── service/            # Lógica de negocio y reglas DMN
├── repository/         # Persistencia con JPA
├── entities/           # Modelos de dominio
├── dto/                # Transferencia de datos
├── config/             # Configuración Spring/Camunda
└── delegate/           # Delegates Camunda (Java)
\`\`\`

---

## 🚀 Guía de Instalación

### Prerrequisitos

\`\`\`bash
Java 17+ | Maven 3.8+ | Git 2.30+
Opcional: Node.js 18+, Docker 20+
\`\`\`

### Clonación y Configuración

\`\`\`bash
git clone https://github.com/SebaS00B/camunda-reto-software-evolutivo
cd camunda-reto-software-evolutivo
cp src/main/resources/application.properties.example src/main/resources/application.properties
\`\`\`

### Ejecución

\`\`\`bash
mvn clean install
mvn spring-boot:run
# Verificar estado
curl http://localhost:8080/actuator/health
\`\`\`

---

## 🌐 Accesos y URLs

\`\`\`
📊 Dashboard:        http://localhost:8080/process/dashboard
📝 Crear Solicitud:  http://localhost:8080/process/create
🎛️ Camunda Cockpit:  http://localhost:8080/camunda/app/cockpit
📋 Tasklist de solicitudes:
                      http://localhost:8080/camunda/app/tasklist
🗄️ H2 Console:       http://localhost:8080/h2-console
\`\`\`

---

## 🧪 Casos de Prueba Clave

- **Autoaprobación:** Monto ≤ $200 → Ruta: AUTO
- **Supervisor:** Monto $1,500 → Categoría: SOFTWARE → Ruta: SUPERVISOR
- **CEO:** Monto > $10,000 → Ruta: CEO
- **Timers:** Recordatorios automáticos tras 1–3 minutos

---

## 📧 Notificaciones Inteligentes

- Confirmación de creación
- Asignación de tareas
- Recordatorios por timer BPMN
- Notificación de cierre

Con templates HTML responsivos y enlaces directos a tareas.

---

## 📊 Dashboard Ejecutivo

- KPIs en tiempo real
- Alertas de SLA
- Análisis financiero
- Carga por aprobador

---

## 🧠 Motor de Reglas (DMN)

Tabla de decisiones basada en:
- \`totalAmount\`, \`category\`, \`priority\`
- Salida: \`approvalRoute\` (AUTO, SUPERVISOR, MANAGER, CEO)

---

## 📖 Documentación Técnica

- API REST con Swagger / OpenAPI
- BPMN y DMN modelados con Camunda Modeler
- Código organizado por Clean Architecture

---

## 👨‍💼 Usuarios Demo

\`\`\`
user: admin       pass: admin             (grupo: camunda-admin)
user: supervisor1 pass: supervisor123     (grupo: supervisor)
user: manager1    pass:manager123         (grupo: manager)
user: ceo1        pass:ceo123             (grupo: ceo)
user: employee1   pass: employee123       (grupo: employee)
\`\`\`

---

## 🧠 Decisiones Técnicas

- Se eligió Spring Boot para facilitar el desarrollo rápido y la integración con Camunda Embedded.
- Se utilizó el patrón External Task para desacoplar el motor de proceso del backend.
- Se implementó Clean Architecture para escalabilidad y mantenibilidad.
- Se usó H2 en modo demo por simplicidad, pero la aplicación es compatible con PostgreSQL para entornos reales.

## ☁ Despliegue en un Entorno Real

- La aplicación puede contenerse con Docker para simplificar el despliegue.
- Se recomienda usar PostgreSQL como base de datos y configurar un pipeline CI/CD para pruebas automáticas.
- Camunda Cockpit debe protegerse con autenticación basada en roles.
- SMTP debe conectarse a un servicio como Mailtrap (pruebas) o SendGrid (producción).
