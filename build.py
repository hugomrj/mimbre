#!/usr/bin/env python3

import platform
import shutil
import subprocess
from pathlib import Path

PROJECT_NAME = "Desk"
JAR_NAME = "mimbre-0.1.jar"
MAIN_CLASS = "com.app.Application"


def run(command):
    print(">>>", " ".join(command), flush=True)
    subprocess.run(command, check=True)


def main():
    system = platform.system()

    # Seleccionar Maven según el sistema operativo
    if system == "Windows":
        maven = "mvn.cmd"
    else:
        maven = "./mvnw"
        Path("mvnw").chmod(0o755)

    # Compilar Micronaut
    run([
        maven,
        "clean",
        "package",
        "-DskipTests"
    ])

    # Limpiar distribución anterior
    release_dir = Path("release")

    if release_dir.exists():
        shutil.rmtree(release_dir)

    # Crear aplicación con Java incluido
    command = [
        "jpackage",
        "--name", PROJECT_NAME,
        "--input", "target",
        "--main-jar", JAR_NAME,
        "--main-class", MAIN_CLASS,
        "--type", "app-image",
        "--dest", "release",
    ]

    # Opciones específicas de Windows
    if system == "Windows":
        command.extend([
            "--java-options",
            "-Dorg.eclipse.swt.browser.DefaultType=edge"
        ])

    # Generar ejecutable
    run(command)

    print("\n===================================")
    print("COMPILACIÓN FINALIZADA")
    print("===================================")
    print(f"Sistema: {system}")
    print(f"Distribución: release/{PROJECT_NAME}")

    if system == "Windows":
        print("Ejecutable: release/Desk/Desk.exe")

    elif system == "Linux":
        print("Ejecutable: release/Desk/bin/Desk")

    elif system == "Darwin":
        print("Aplicación: release/Desk.app")


if __name__ == "__main__":
    main()
