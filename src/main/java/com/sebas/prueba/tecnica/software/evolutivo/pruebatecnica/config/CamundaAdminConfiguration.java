package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.config;


import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 
* 🔐 CONFIGURACIÓN DE USUARIOS Y GRUPOS CAMUNDA
 *
 * Crea usuarios y grupos por defecto cuando la aplicación inicia.
 * Esto es necesario para poder acceder a las aplicaciones web de Camunda.
 *
 * @author Sebastian Rodriguez
 * @version 1.0.0
 */
@Component
@Slf4j
public class CamundaAdminConfiguration {

    @Autowired
    private IdentityService identityService;

    // Inyectamos directamente el AuthorizationService de Camunda
    @Autowired
    private AuthorizationService authorizationService;

    @EventListener
    public void processPostDeploy(PostDeployEvent event) {
        log.info("🔐 Configurando usuarios y grupos de Camunda...");

        try {
            createGroups();
            createUsers();
            assignUsersToGroups();
            createAuthorizations();

            log.info("✅ Configuración de usuarios completada exitosamente");
            log.info("👤 Usuario admin: admin / admin");
            log.info("🔗 Camunda Apps: http://localhost:8080/camunda");

        } catch (Exception e) {
            log.error("❌ Error configurando usuarios: {}", e.getMessage(), e);
        }
    }

    private void createGroups() {
        createGroupIfNotExists("camunda-admin", "Camunda Administrators", "SYSTEM");
        createGroupIfNotExists("supervisor", "Supervisores", "WORKFLOW");
        createGroupIfNotExists("manager", "Gerentes", "WORKFLOW");
        createGroupIfNotExists("ceo", "CEO", "WORKFLOW");
        createGroupIfNotExists("employee", "Empleados", "WORKFLOW");
    }

    private void createUsers() {
        createUserIfNotExists("admin", "admin", "Administrator", "BPM", "admin@softwareevolutivo.com");
        createUserIfNotExists("supervisor1", "supervisor123", "Juan", "Supervisor", "supervisor@empresa.com");
        createUserIfNotExists("manager1", "manager123", "María", "Gerente", "manager@empresa.com");
        createUserIfNotExists("ceo1", "ceo123", "Carlos", "CEO", "ceo@empresa.com");
        createUserIfNotExists("employee1", "employee123", "Ana", "Empleada", "empleado@empresa.com");
        createUserIfNotExists("demo", "demo", "Usuario", "Demo", "demo@empresa.com");
    }

    private void assignUsersToGroups() {
        assignUserToGroup("admin", "camunda-admin");
        assignUserToGroup("supervisor1", "supervisor");
        assignUserToGroup("manager1", "manager");
        assignUserToGroup("ceo1", "ceo");
        assignUserToGroup("employee1", "employee");
        assignUserToGroup("demo", "employee");

        assignUserToGroup("supervisor1", "employee");
        assignUserToGroup("manager1", "supervisor");
        assignUserToGroup("manager1", "employee");
        assignUserToGroup("ceo1", "manager");
        assignUserToGroup("ceo1", "supervisor");
        assignUserToGroup("ceo1", "employee");
    }



private void createAuthorizations() {

  // Grant de permisos para Tasklist
  Authorization tasklistAuth = authorizationService
    .createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
  tasklistAuth.setGroupId("camunda-admin");
  tasklistAuth.setResource(Resources.APPLICATION);
  tasklistAuth.setResourceId("tasklist");
  tasklistAuth.addPermission(Permissions.ALL);
  authorizationService.saveAuthorization(tasklistAuth);

  // Grant de permisos para Cockpit
  Authorization cockpitAuth = authorizationService
    .createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
  cockpitAuth.setGroupId("camunda-admin");
  cockpitAuth.setResource(Resources.APPLICATION);
  cockpitAuth.setResourceId("cockpit");
  cockpitAuth.addPermission(Permissions.ALL);
  authorizationService.saveAuthorization(cockpitAuth);

  log.info("✅ Permisos asignados al grupo camunda-admin");
}


    private void createGroupIfNotExists(String groupId, String groupName, String groupType) {
        Group existingGroup = identityService.createGroupQuery().groupId(groupId).singleResult();
        if (existingGroup == null) {
            Group group = identityService.newGroup(groupId);
            group.setName(groupName);
            group.setType(groupType);
            identityService.saveGroup(group);
            log.info("👥 Grupo creado: {} ({})", groupName, groupId);
        } else {
            log.debug("👥 Grupo ya existe: {}", groupId);
        }
    }

    private void createUserIfNotExists(String userId, String password, String firstName, String lastName, String email) {
        User existingUser = identityService.createUserQuery().userId(userId).singleResult();
        if (existingUser == null) {
            User user = identityService.newUser(userId);
            user.setPassword(password);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            identityService.saveUser(user);
            log.info("👤 Usuario creado: {} {} ({})", firstName, lastName, userId);
        } else {
            log.debug("👤 Usuario ya existe: {}", userId);
        }
    }

    private void assignUserToGroup(String userId, String groupId) {
        long count = identityService.createUserQuery()
            .userId(userId)
            .memberOfGroup(groupId)
            .count();
        if (count == 0) {
            identityService.createMembership(userId, groupId);
            log.info("🔗 Usuario {} asignado al grupo {}", userId, groupId);
        } else {
            log.debug("🔗 Usuario {} ya pertenece al grupo {}", userId, groupId);
        }
    }
}
