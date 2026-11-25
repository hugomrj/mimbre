package com.app.configuracion.controller;

import com.app.AppPaths;
import com.app.BackupDto;
import com.app.BackupService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.views.View;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/ui/configuracion")
public class ConfiguracionUiController {

    private final BackupService backupService;

    public ConfiguracionUiController(BackupService backupService) {
        this.backupService = backupService;
    }

    @View("configuracion/index")
    @Get
    public Map<String, Object> index() {
        return model(null, null);
    }

    @View("configuracion/index")
    @Post(uri = "/backup", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> crearBackup() {
        try {
            backupService.respaldar();
            return model("Backup creado correctamente.", null);
        } catch (Exception e) {
            return model(null, "No se pudo crear el backup: " + e.getMessage());
        }
    }

    @View("configuracion/index")
    @Post(uri = "/restaurar", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> restaurar(@Body Map<String, String> body) {
        String archivo = body.get("archivo");
        if (backupService.prepararRestauracion(archivo)) {
            return model("Restauración preparada. Cerrá y volvé a abrir Mimbre para aplicarla.", null);
        }
        return model(null, "No se pudo preparar la restauración del backup seleccionado.");
    }

    @View("configuracion/index")
    @Post(uri = "/borrar", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> borrar(@Body Map<String, String> body) {
        if (!"ELIMINAR".equals(body.get("confirmacion"))) {
            return model(null, "Escribí ELIMINAR para confirmar el borrado de la base.");
        }
        try {
            backupService.prepararBorrado();
            return model("Borrado programado. Cerrá y volvé a abrir Mimbre para crear una base vacía.", null);
        } catch (IOException e) {
            return model(null, "No se pudo programar el borrado: " + e.getMessage());
        }
    }

    private Map<String, Object> model(String mensaje, String error) {
        List<BackupDto> backups = backupService.listar();
        Map<String, Object> model = new HashMap<>();
        model.put("backups", backups);
        model.put("dataPath", AppPaths.dataDir().toAbsolutePath().toString());
        if (mensaje != null) model.put("mensaje", mensaje);
        if (error != null) model.put("error", error);
        return model;
    }
}
