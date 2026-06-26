#!/usr/bin/env python3
import shutil
import subprocess
import sys
from pathlib import Path

PROJECT_NAME = "Desk"
JAR_NAME = "mimbre-0.1.jar"
MAIN_CLASS = "com.app.Application"


def run(command):
    print(f"\n>>> {' '.join(command)}\n")
    subprocess.run(command, check=True, shell=True)


def main():

    # En Windows CI usamos Maven instalado en el sistema, en Linux usamos el wrapper
    if sys.platform == "win32":
        maven_cmd = "mvn"
    else:
        maven_cmd = "./mvnw"

    # Dar permisos de ejecución en Linux/Mac
    if sys.platform != "win32":
        subprocess.run(["chmod", "+x", "mvnw"], check=False)

    run([maven_cmd, "clean", "package", "-Pwindows", "-DskipTests"])

    release_dir = Path("release")
    if release_dir.exists():
        shutil.rmtree(release_dir)

    run([
        "jpackage",
        "--name", PROJECT_NAME,
        "--input", "target",
        "--main-jar", JAR_NAME,
        "--main-class", MAIN_CLASS,
        "--type", "app-image", # Si quieres un instalador .exe, cambia esto a "exe"
        "--dest", "release"
    ])

    print("\nBuild finalizado.")
    if sys.platform == "win32":
        print(f"Ejecutable: release\\{PROJECT_NAME}\\{PROJECT_NAME}.exe")
    else:
        print(f"Ejecutable: release/{PROJECT_NAME}/{PROJECT_NAME}.exe")

if __name__ == "__main__":
    main()